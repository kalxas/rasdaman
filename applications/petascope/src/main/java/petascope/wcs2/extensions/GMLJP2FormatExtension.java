/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.extensions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.GdalParameters;
import static petascope.util.GdalParameters.GDAL_CODEC_PARAM;
import petascope.util.Pair;
import petascope.util.WcsUtil;
import petascope.util.XMLSymbols;
import petascope.util.ras.RasQueryResult;
import static petascope.wcs2.extensions.FormatExtension.MIME_JP2;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;
import petascope.wcs2.templates.Templates;

/**
 * Encodes coverage as a GMLJP2 file.
 * JPEG2000 encoding with additional box containing GML metadata.
 * More than one coverage can be embedded in the file, though
 * currently we limit to 2D datasets.
 * GML metadata follows the ad-hoc schema for GMLJP2 standard, no GMLCOV.
 *
 * @see "http://www.opengeospatial.org/standards/gmljp2"
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class GMLJP2FormatExtension extends AbstractFormatExtension {

    /* Member */
    /**
     * Code for GMLJP2 encoded files.
     * J2K is the alternative but does not allow extraboxes.
     */
    private static final String JP2_CODEC = "jp2";
    /**
     * GDAL configuration option to enable custom GML box to be included in the file.
     * Associated value needs to be the absolute path to a file containing the GML fragment (+ XML header).
     */
    private static final String GMLJP2OVERRIDE_COPTION_KEY = "GMLJP2OVERRIDE";
    private GdalParameters gdalParams;
    private static final Logger log = LoggerFactory.getLogger(GMLJP2FormatExtension.class);

    /* Interface */
    public GdalParameters getGdalParameters() {
        return gdalParams;
    }

    /* Methods */
    @Override
    public boolean canHandle(GetCoverageRequest req) {
        return req.isMultipart() && getMimeType().equals(req.getFormat());
    }

    @Override
    public Response handle(GetCoverageRequest request, DbMetadataSource meta)
            throws PetascopeException, WCSException, SecoreException {
        GetCoverageMetadata m = new GetCoverageMetadata(request, meta);

        //Handle the range subset feature
        RangeSubsettingExtension rsubExt = (RangeSubsettingExtension) ExtensionsRegistry.getExtension(ExtensionsRegistry.RANGE_SUBSETTING_IDENTIFIER);
        rsubExt.handle(request, m);

        try {
            // GetCoverage metadata was initialized with native coverage metadata, but subsets may have changed it:
            updateGetCoverageMetadata(request, m, meta);
        } catch (PetascopeException pEx) {
            throw pEx;
        }

        if (m.getGridDimension() != 2 || m.hasIrregularAxis() || !(WcsUtil.isGrid(m.getCoverageType()))) {
            throw new WCSException(ExceptionCode.InvalidRequest,
                    "The JPEG2000 format extension only supports regularly gridded coverages with exactly two dimensions");
        }

        Pair<Object, String> p = null;
        if (m.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
            // return plain JPEG
            gdalParams = new GdalParameters();
            p = executeRasqlQuery(request, m.getMetadata(), meta, OPENJP2_ENCODING, null);
        } else {

            // RectifiedGrid: geometry is associated with a CRS -> return JPEG2000 with geo-metadata
            // Need to use the GetCoverage metadata which has updated bounds [see super.setBounds()]
            String[] domLo = m.getGisDomLow().split(" ");
            String[] domHi = m.getGisDomHigh().split(" ");
            if (domLo.length != 2 || domHi.length != 2) {
                // Output grid dimensions have already been checked (see above), but double-check on the domain bounds:
                log.error("Cannot format JPEG2000 (+GML): output dimensionality is not 2.");
                throw new WCSException(ExceptionCode.InvalidRequest, "Output dimensionality of the requested coverage is " +
                        (domLo.length==2?domHi.length:domLo.length) + " whereas Petascope currently supports 2-dimensional grids.");
            }

            // Set the GDAL extra parameters (explicit codec in this case)
            // See http://osgeo-org.1560.x6.nabble.com/gdal-dev-JP2-J2K-codecs-in-JP2OpenJPEG-dataset-td5129932.html
            gdalParams = new GdalParameters(domLo[0], domHi[0], domLo[1], domHi[1], m.getCrs());
            gdalParams.addExtraParams(GDAL_CODEC_PARAM + '=' + JP2_CODEC);

            // Get GML description and store it in a tmp file for GDAL
            String gml = WcsUtil.getGML(m, Templates.GMLJP2_COVERAGE_COLLECTION, meta);
            File gmlFile;
            try {
                gmlFile = File.createTempFile(m.getCoverageId() + '_' + OPENJP2_ENCODING, null); // arbitrary prefix (at least 3 chars)
                if (gmlFile.canWrite()) {
                    PrintWriter gmlWriter = new PrintWriter(gmlFile.getAbsolutePath());
                    gmlWriter.println(gml);
                    gmlWriter.close();
                } else {
                    log.error("JVM cannot write to the default directory for tmp files " + gmlFile.getAbsolutePath());
                    throw new PetascopeException(ExceptionCode.InternalComponentError,
                            "Cannot write temporary file "  + gmlFile.getAbsolutePath() + " for GML box in GMLJP2 encoding.");
                }

                // Set the configuration option for GDAL
                gdalParams.setConfigKeyValue(GMLJP2OVERRIDE_COPTION_KEY, gmlFile.getAbsolutePath());

                // get the data
                p = executeRasqlQuery(request, m.getMetadata(), meta, OPENJP2_ENCODING, gdalParams.toString());
                gmlFile.delete();

            } catch (SecurityException ex) {
                log.warn("Could not write/delete tmp file (permission problems?): " + ex.getMessage());
            } catch (FileNotFoundException ex) {
                log.error("Java writer cannot find target file.");
                throw new PetascopeException(ExceptionCode.InternalComponentError, ex);
            } catch (IOException ex) {
                log.error("IO exception while writing GML to tmp file: ", ex.getMessage());
                throw new PetascopeException(ExceptionCode.InternalComponentError, ex);
            }
        }

        RasQueryResult res = new RasQueryResult(p.fst);
        if (res.getMdds().isEmpty()) {
            return new Response(null, null, getMimeType());
        } else {
            return new Response(res.getMdds().get(0), null, getMimeType());
        }
    }

    @Override
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.GMLJP2_IDENTIFIER;
    }

    /**
     * @return False: this extension has is no parent extension with identifier.
     */
    public Boolean hasParent() {
        return false;
    }

    /**
     * @return The identifier of the parent extension.
     */
    public String getParentExtensionIdentifier() {
        return "";
    }

    public String getMimeType() {
        return MIME_JP2;
    }
}
