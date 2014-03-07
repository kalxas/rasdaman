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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.CrsUtil;
import petascope.util.Pair;
import petascope.util.WcsUtil;
import petascope.util.XMLSymbols;
import petascope.util.ras.RasQueryResult;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;

/**
 * Return coverage as a JPEG2000 file.
 *
 * The only coverage types supported by this specification are GridCoverage and
 * RectifiedGridCoverage with exactly 2 dimensions.
 *
 * @author <a href="mailto:m.rusu@jacobs-university.de">Mihaela Rusu</a>
 */
public class JPEG2000FormatExtension extends AbstractFormatExtension {

    /* Member */
    CrsUtil.CrsProperties crsProperties;
    private static final Logger log = LoggerFactory.getLogger(JPEG2000FormatExtension.class);

    /* Interface */
    public CrsUtil.CrsProperties getCrsProperties() {
        return crsProperties;
    }

    /* Methods */
    @Override
    public boolean canHandle(GetCoverageRequest req) {
        return !req.isMultipart() && getMimeType().equals(req.getFormat());
        //return true;
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
            throw new WCSException(ExceptionCode.InvalidRequest, "The JPEG2000 format extension "
                    + "only supports regularly gridded coverages with exactly two dimensions");
        }

        Pair<Object, String> p = null;
        if (m.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
            // return plain JPEG
            crsProperties = new CrsUtil.CrsProperties();
            p = executeRasqlQuery(request, m.getMetadata(), meta, JP2_ENCODING, null);
        } else {
            // RectifiedGrid: geometry is associated with a CRS -> return JPEG2000 with geo-metadata
            // Need to use the GetCoverage metadata which has updated bounds [see super.setBounds()]
            String[] domLo = m.getGisDomLow().split(" ");
            String[] domHi = m.getGisDomHigh().split(" ");
            if (domLo.length != 2 || domHi.length != 2) {
                // Output grid dimensions have already been checked (see above), but double-check on the domain bounds:
                log.error("Cannot format JPEG2000: output dimensionality is not 2.");
                throw new WCSException(ExceptionCode.InvalidRequest, "Output dimensionality of the requested coverage is " +
                        (domLo.length==2?domHi.length:domLo.length) + " whereas JPEG2000 requires 2-dimensional grids.");
            }
            crsProperties = new CrsUtil.CrsProperties(domLo[0], domHi[0], domLo[1], domHi[1], m.getBbox().getCrsName());
            p = executeRasqlQuery(request, m.getMetadata(), meta, JP2_ENCODING, crsProperties.toString());
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
        return ExtensionsRegistry.JPEG2000_IDENTIFIER;
    }

    public String getMimeType() {
        return MIME_JP2;
    }
}
