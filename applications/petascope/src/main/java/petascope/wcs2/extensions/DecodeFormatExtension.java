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
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.Pair;
import petascope.util.WcsUtil;
import petascope.util.XMLSymbols;
import petascope.util.ras.RasQueryResult;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;
import petascope.wcs2.parsers.subsets.DimensionSlice;
import petascope.wcs2.parsers.subsets.DimensionSubset;
import petascope.wcs2.parsers.subsets.DimensionTrim;
import petascope.wcs2.templates.Templates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import static petascope.core.DbMetadataSource.TABLE_MULTIPOINT;
import petascope.wcps.metadata.DomainElement;

/**
 * Common class for all format extensions.
 *
 * @author <a href="mailto:s.timilsina@jacobs-university.de">Sulav Timilsina</a>
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class DecodeFormatExtension extends AbstractFormatExtension {

    public static final Set<String> SUPPORTED_FORMATS = new HashSet<String>(Arrays.asList(MIME_GML, MIME_PNG, MIME_TIFF, MIME_JP2, MIME_NETCDF));
    // this will store the map from MIME type to file extesion (e.g: application/netcdf -> file extension .nc)
    private static final Map<String, String> MIME_TO_FILE_EXTENSION = new HashMap<String, String>() {
        {
            put(MIME_NETCDF, "nc");
        }
    };

    public static final String DATATYPE_URN_PREFIX = "urn:ogc:def:dataType:OGC:1.1:"; // FIXME: now URNs are deprecated
    /* Member */

    /**
     * GDAL configuration option to enable custom GML box to be included in the file.
     * Associated value needs to be the absolute path to a file containing the GML fragment (+ XML header).
     */
    private static final String GMLJP2OVERRIDE_COPTION_KEY = "GMLJP2OVERRIDE";
    private static final Logger log = LoggerFactory.getLogger(DecodeFormatExtension.class);

    private final String extensionId;
    private final Boolean hasParent;
    private final String parentExtensionId;
    private String mimeType;

    /**
     * Gets the extension identifier for the mimeType
     * @param mimeType - mimeType
     * @return extension Identifier
     * @throws petascope.exceptions.WCSException
     */
    public String getExtensionIdentifier(String mimeType) throws WCSException {
        if (mimeType == null) {
            mimeType = MIME_GML;
        }
        if (ExtensionsRegistry.mimeToIdentifier.containsKey(mimeType)) {
            return ExtensionsRegistry.mimeToIdentifier.get(mimeType);
        } else {
            // If request contains unsupported MIME then will throw this exception
            log.error("MIME type: " + mimeType + " cannot be handled.");
            throw new WCSException(ExceptionCode.InvalidParameterValue, mimeType + " MIME type cannot be handled.");
        }
    }

    /**
     * Constructor for registering formats and also used while handling multipart requests
     * as two base requests
     * @param mime
     * @throws petascope.exceptions.WCSException
     */
    public DecodeFormatExtension(String mime) throws WCSException {
        this.mimeType = mime;
        this.extensionId = getExtensionIdentifier(mime);
        if (mime.equals(MIME_GML)) {
            this.hasParent = true;
            this.parentExtensionId = ExtensionsRegistry.GMLCOV_IDENTIFIER;
        } else {
            this.hasParent = false;
            this.parentExtensionId = "";
        }
    }

    /**
     * Gets encoding for the given mimeType.
     * @param mimeType - mimeType
     * @return encoding
     * @throws petascope.exceptions.WCSException
     */
    public String getEncoding(String mimeType) throws WCSException {

        if (ExtensionsRegistry.mimeToEncoding.containsKey(mimeType)) {
            return (String) ExtensionsRegistry.mimeToEncoding.get(mimeType);
        } else {
            log.error("MIME type: " + mimeType + " cannot be handled.");
            throw new WCSException(ExceptionCode.InvalidParameterValue, "MIME type: " + mimeType + " cannot be handled.");
        }
    }

    /**
     *Checks if the request can be handled by this class.
     *Only called once before handling the user request.
     *Not called when helper functions of handle() calls handle() with a new instance while handling multipart requests.
     * @param req GetCoverage request
     * @return if the req can be handled
     * @throws petascope.exceptions.WCSException
     */
    @Override
    public boolean canHandle(GetCoverageRequest req) throws WCSException {
        // check if mime is supported
        String encodeType = ExtensionsRegistry.mimeToIdentifier.get(req.getFormat());
        this.mimeType = req.getFormat();

        // validate the request
        if (encodeType == null) {
            // e.g: format=image/NotSupport
            throw new WCSException(ExceptionCode.InvalidParameterValue, "MIME type: " + req.getFormat() + " cannot be handled.");
        }
        return true;
    }

    /**
     * @param request
     * @param meta
     * @return multipart response for png,tiff and netcdf formats
     * @throws PetascopeException
     * @throws SecoreException
     */
    private Response getMultiPartResponse(GetCoverageRequest request, DbMetadataSource meta) throws PetascopeException, SecoreException {
        Response multipartResponse;
        //getMultiPart is set to false to handle multipart requests as two different single requests.
        Response gml = this.getGmlResponse(request, meta);
        // get the image/netcdf file
        Response image = this.getImageResponse(request, meta);
        // return multipart response
        String xml = gml.getXml()[0].replace("{coverageData}",
                                             "<File>"
                                             + "<fileName>" + "file" + "</fileName>"
                                             + "<fileStructure>Record Interleaved</fileStructure>"
                                             + "<mimeType>" + request.getFormat() + "</mimeType>"
                                             + "</File>");
        multipartResponse = new Response(image.getData(), new String[] {xml}, request.getFormat(), image.getCoverageID());
        multipartResponse.setMultipart(true);
        return multipartResponse;
    }

    /**
     * This function handles the Gml requests
     *
     * @param request
     * @param meta
     * @return gmlCoverage response
     * @throws PetascopeException
     * @throws SecoreException
     */
    private Response getGmlResponse(GetCoverageRequest request, DbMetadataSource meta) throws PetascopeException, SecoreException {
        GetCoverageMetadata m = new GetCoverageMetadata(request, meta);

        // First, transform possible non-native CRS subsets
        CRSExtension crsExtension = (CRSExtension) ExtensionsRegistry.getExtension(ExtensionsRegistry.CRS_IDENTIFIER);
        crsExtension.handle(request, m);

        //Handle the range subset feature
        RangeSubsettingExtension rsubExt = (RangeSubsettingExtension) ExtensionsRegistry.getExtension(ExtensionsRegistry.RANGE_SUBSETTING_IDENTIFIER);
        rsubExt.handle(request, m);
        if (WcsUtil.isMultiPoint(m.getCoverageType())) {
            Response r = handleMultiPoint(request, request.getCoverageId(), meta, m);
            return new Response(r.getData(), r.getXml(), r.getFormatType());

        } else if (WcsUtil.isGrid(m.getCoverageType())) {

            // Use the GridCoverage template, which works with any subtype of AbstractGridCoverage via the {domainSetaddition}
            try {
                // GetCoverage metadata was initialized with native coverage metadata, but subsets may have changed it:
                updateGetCoverageMetadata(request, m, meta);
            } catch (PetascopeException pEx) {
                throw pEx;
            }

            String gml = WcsUtil.getGML(m, Templates.COVERAGE, meta);
            gml = addCoverageData(gml, request, meta, m);

            // RGBV coverages
            if (m.getCoverageType().equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
                gml = WcsUtil.addCoefficients(gml, m, meta);
                // Grid and Coverage bounds need to be updated, now we know the coefficients
                // NOTE: we will not translate subsets from subsettingCRS to nativeCrs again (translated above in updateGetCoverageMetadata).
                if (!StringUtils.isEmpty(m.getSubsettingCrs())) {
                    m.setSubsettingCrs(null);
                    // and will set outputCrs to subsettingCrs if it is null
                    if (m.getOutputCrs() == null) {
                        m.setOutputCrs(m.getSubsettingCrs());
                    }
                }
                updateGetCoverageMetadata(request, m, meta);
                gml = WcsUtil.getBounds(gml, m);
            }


            return new Response(null, new String[] {gml}, FormatExtension.MIME_XML, m.getCoverageId());
            // TODO : use XOM serializer (current problem: license header is trimmed to one line and namespaces need to be added)
            //Builder xmlBuilder = new Builder();
            //try {
            //    Document gmlDoc = xmlBuilder.build(new StringReader(gml));
            //    return new Response(null, serialize(gmlDoc), FormatExtension.MIME_XML);
            //} catch (IOException ex) {
            //    throw new WCSException(ExceptionCode.IOConnectionError,
            //        "Error serializing constructed document", ex);
            //} catch (ParsingException ex) {
            //    throw new WCSException(ExceptionCode.InternalComponentError,
            //        "Error creating the GML response document.", ex);
            //}
        } else {
            throw new WCSException(ExceptionCode.UnsupportedCoverageConfiguration,
                                   "The coverage type '" + m.getCoverageType() + "' is not supported.");
        }
    }

    /**
     * handles image requests (e.g: netcdf, png, jpeg,...)
     *
     * @param request
     * @param meta
     * @return image response
     * @throws PetascopeException
     * @throws SecoreException
     */
    private Response getImageResponse(GetCoverageRequest request, DbMetadataSource meta) throws PetascopeException, SecoreException {

        GetCoverageMetadata m = new GetCoverageMetadata(request, meta);
        // First, transform possible non-native CRS subsets
        CRSExtension crsExtension = (CRSExtension) ExtensionsRegistry.getExtension(ExtensionsRegistry.CRS_IDENTIFIER);
        crsExtension.handle(request, m);

        //Handle the range subset feature
        RangeSubsettingExtension rsubExt = (RangeSubsettingExtension) ExtensionsRegistry.getExtension(ExtensionsRegistry.RANGE_SUBSETTING_IDENTIFIER);
        rsubExt.handle(request, m);

        try {
            // GetCoverage metadata was initialized with native coverage metadata, but subsets may have changed it:
            updateGetCoverageMetadata(request, m, meta);
        } catch (PetascopeException pEx) {
            throw pEx;
        }
        if ((request.getFormat().equals(MIME_NETCDF))) {
            // validate the request in netCDF format
            if (!(WcsUtil.isGrid(m.getCoverageType()))) {
                throw new WCSException(ExceptionCode.NoApplicableCode, "The Netcdf format extension "
                                       + "only supports " + XMLSymbols.LABEL_GRID_COVERAGE + ", " + XMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE + ", "
                                       + XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE);
            }
        } else {
            // validate the request in image format (tiff, jpeg, png)
            if (m.getGridDimension() != 2 || m.hasIrregularAxis() || !(WcsUtil.isGrid(m.getCoverageType()))) {
                throw new WCSException(ExceptionCode.InvalidRequest, "The " + request.getFormat() + " format extension "
                                       + "only supports regularly gridded coverages with exactly two dimensions");
            }
        }

        Pair<Object, String> p = null;
        // just pass the coverage metadata to WCPS, it will add the bounding box, outputCRS in WCPS.
        if (!request.getFormat().equals(MIME_JP2)) {
            p = executeWcsRequest(request, m, meta, getEncoding(request.getFormat()), null);
        } else {
            // Only JPEG2000 need a special GML file as GDAL parameter
            p = getJPEG2000CoverageOutput(request, m, meta);
        }

        RasQueryResult res = new RasQueryResult(p.fst);
        if (res.getMdds().isEmpty()) {
            return new Response(null, null, request.getFormat());
        } else {
            List<byte[]> data = new ArrayList<byte[]>();
            data.add(res.getMdds().get(0));
            return new Response(data, null, request.getFormat(), m.getCoverageId());
        }
    }

    /**
     * Adds GML to JP2 or output file will not have geo-referenced metadata
     * NOTE: must use OpenJPEG2000 and a temporary GML file to set correct geo-referenced metadata.
     * example rasql query:
     * SELECT encode(c, "JP2OpenJPEG" ,
     *                  "CODEC=jp2;
     *                  config=GMLJP2OVERRIDE /home/rasdaman/Tomcat/temp/test_mean_summer_airtemp_jpeg20001582287581546422039.tmp;
     *                  xmin=111.975;ymin=-44.525;xmax=156.275;ymax=-8.975;crs=EPSG:4326")
     * FROM test_mean_summer_airtemp AS c
     *
     * @param request
     * @param m
     * @param meta
     * @return result of executing the query for GML+JP2
     * @throws PetascopeException
     * @throws SecoreException
     */
    private Pair<Object, String> getJPEG2000CoverageOutput(GetCoverageRequest request, GetCoverageMetadata m, DbMetadataSource meta) throws PetascopeException, SecoreException {
        Pair<Object, String> p = null;
        String gdalParameters = "codec=jp2;";
        // Get GML description and store it in a tmp file for GDAL
        String gml = WcsUtil.getGML(m, Templates.GMLJP2_COVERAGE_COLLECTION, meta);
        File gmlFile;
        try {
            gmlFile = File.createTempFile(m.getCoverageId() + '_' + FORMAT_ID_JP2, null); // arbitrary prefix (at least 3 chars)
            if (gmlFile.canWrite()) {
                PrintWriter gmlWriter = new PrintWriter(gmlFile.getAbsolutePath());
                gmlWriter.println(gml);
                gmlWriter.close();
            } else {
                log.error("JVM cannot write to the default directory for tmp files " + gmlFile.getAbsolutePath());
                throw new PetascopeException(ExceptionCode.InternalComponentError,
                                             "Cannot write temporary file " + gmlFile.getAbsolutePath() + " for GML box in GMLJP2 encoding.");
            }

            // Set the configuration option for GDAL
            gdalParameters += "config=" + GMLJP2OVERRIDE_COPTION_KEY + " " + gmlFile.getAbsolutePath();

            // get the data
            p = executeWcsRequest(request, m, meta, FORMAT_ID_OPENJP2, gdalParameters);
            // delete the GML temporary file
            gmlFile.delete();

        } catch (SecurityException ex) {
            log.error("Could not write/delete tmp file (permission problems?): " + ex.getMessage());
            throw new PetascopeException(ExceptionCode.InternalComponentError, ex);
        } catch (FileNotFoundException ex) {
            log.error("Java writer cannot find target file.");
            throw new PetascopeException(ExceptionCode.InternalComponentError, ex);
        } catch (IOException ex) {
            log.error("IO exception while writing GML to tmp file: ", ex.getMessage());
            throw new PetascopeException(ExceptionCode.InternalComponentError, ex);
        }
        return p;
    }

    /**
     * Checks the type of request and channels to the helper functions where
     * requests are handled
     *
     * @param request
     * @param meta    database metadata
     * @return the requested WCS response
     * @throws PetascopeException
     * @throws WCSException
     * @throws SecoreException
     */
    @Override
    public Response handle(GetCoverageRequest request, DbMetadataSource meta) throws PetascopeException, WCSException, SecoreException {
        //getFormat never returns null. if request has format null, the default format gml is set
        // NOTE: OGC CITE getCoverage returning GML and multipart is valid (e.g: GetCoverage&coverageid=test_mean_summer_airtemp&mediatype=multipart/related)
        // but by default, Petascope already returns GML so just ignore the multipart
        if (request.isMultiPart() && !request.getFormat().equals(MIME_GML)) {
            return getMultiPartResponse(request, meta);
        }

        GetCoverageMetadata m = new GetCoverageMetadata(request, meta);

        //Handle the range subset feature
        RangeSubsettingExtension rsubExt = (RangeSubsettingExtension) ExtensionsRegistry.getExtension(ExtensionsRegistry.RANGE_SUBSETTING_IDENTIFIER);
        rsubExt.handle(request, m);
        
        // Check if subsets belong to existing axes (e.g: subset=dimension(-44.525,-8.975) is not valid).
        for (DimensionSubset subset : request.getSubsets()) {
            String dim = subset.getDimension();
            //Check if the supplied axis is in the coverage axes and throw exception if not
            if (m.getMetadata().getDomainByName(dim) == null) {
                throw new WCSException(ExceptionCode.InvalidAxisLabel,
                        "The axis label " + dim + " was not found in the list of available axes");
            }
        }

        // Handle return only GML output
        if (request.getFormat().equals(MIME_GML)) {
            return getGmlResponse(request, meta);
        } else {
            // Handle return image coverage output
            return getImageResponse(request, meta);
        }
    }

    /**
     * Inserts rangeSet values for grid-coverages.
     *
     * @param gml
     * @param request
     * @param meta
     * @param m
     * @return
     * @throws WCSException
     * @throws PetascopeException
     */
    protected String addCoverageData(String gml, GetCoverageRequest request, DbMetadataSource meta, GetCoverageMetadata m)
    throws WCSException, PetascopeException {
        //return gml without {coverageData} replaced for multipart requests
        if (request.isMultiPart()) {
            return gml;
        }
        RasQueryResult res = new RasQueryResult(executeWcsRequest(request, m, meta, FORMAT_ID_CSV, null).fst);
        if (!res.getMdds().isEmpty()) {
            String data = new String(res.getMdds().get(0));
            data = WcsUtil.csv2tupleList(data);
            gml = gml.replace("{" + Templates.KEY_COVERAGEDATA + "}", data);
        }
        return gml;
    }

    /**
     * Handles a request for MultiPoint Coverages and returns a response XML
     *
     * @param req
     * @param coverageName
     * @throws WCSException
     */
    private Response handleMultiPoint(GetCoverageRequest req, String coverageName, DbMetadataSource meta, GetCoverageMetadata m)
    throws WCSException, SecoreException {
        CoverageMetadata cov = m.getMetadata();
        String ret = WcsUtil.getGML(m, Templates.COVERAGE, meta);
        String pointMembers = "";
        String rangeMembers = "";
        String low = "", high = "";
        StringBuilder sb = new StringBuilder();

        try {

            List<CellDomainElement> cellDomainList = cov.getCellDomainList();

            /* check for subsetting */
            List<DimensionSubset> subsets = req.getSubsets();
            if (!subsets.isEmpty()) {

                /* subsetting ON: get coverage metadata */
                ListIterator<DimensionSubset> listIterator = subsets.
                        listIterator();
                while (listIterator.hasNext()) {

                    DimensionSubset subsetElement = listIterator.next();
                    String dimension = subsetElement.getDimension();
                    int dimIndex = cov.getDomainIndexByName(dimension);
                    CellDomainElement cellDomain = cellDomainList.get(dimIndex);

                    if (subsetElement instanceof DimensionTrim) {
                        DimensionTrim trim = (DimensionTrim) subsetElement;
                        cellDomain.setHi(trim.getTrimHigh());
                        cellDomain.setLo(trim.getTrimLow());
                        cellDomain.setSubsetElement(subsetElement);
                    }

                    if (subsetElement instanceof DimensionSlice) {
                        DimensionSlice slice = (DimensionSlice) subsetElement;

                        String[] boundary = slice.getSlicePoint().split(":");
                        cellDomain.setLo(boundary[0]);
                        if (boundary.length == 2) {
                            cellDomain.setHi(boundary[1]);
                        } else if (boundary.length == 1) {
                            cellDomain.setHi(boundary[0]);
                        }
                        cellDomain.setSubsetElement(subsetElement);
                    }
                }
            }

            // Add domainSet and rangeSet of the points
            String[] members = meta.multipointDomainRangeData(TABLE_MULTIPOINT, meta.coverageID(coverageName), coverageName, cellDomainList);
            pointMembers = members[0];
            rangeMembers = members[1];
            String[] split1 = ret.split("\\{" + Templates.KEY_POINTMEMBERS + "\\}");
            String[] split2 = split1[1].split("\\{" + Templates.KEY_COVERAGEDATA + "\\}");
            sb.append(split1[0]).append(pointMembers).append(split2[0]).append(rangeMembers).append(split2[1]);

        } catch (PetascopeException ex) {
            log.error("Error", ex);
        }
        return new Response(new String[] {sb.toString()});
    }

    @Override
    public String getExtensionIdentifier() {
        return extensionId;
    }

    /**
     * @return if the Format has a parent
     */
    @Override
    public Boolean hasParent() {
        return hasParent;
    }

    /**
     * @return The identifier of the parent extension.
     */
    @Override
    public String getParentExtensionIdentifier() {
        return parentExtensionId;
    }

    /**
     * @return The mimetype of the format this class points
     */
    @Override
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Return the file name extension from MIME type (e.g: application/netcdf -> nc)
     * @param mimeType
     * @return
     */
    public static String getFileExtension(String mimeType) {
        return MIME_TO_FILE_EXTENSION.get(mimeType.trim());
    }
}
