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
import petascope.util.GdalParameters;
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

import static petascope.core.DbMetadataSource.TABLE_MULTIPOINT;
import static petascope.util.GdalParameters.GDAL_CODEC_PARAM;

/**
 * Common class for all format extensions.
 *
 * @author <a href="mailto:s.timilsina@jacobs-university.de">Sulav Timilsina</a>
 */
public class DecodeFormatExtension extends AbstractFormatExtension {

    public static final Set<String> SUPPORTED_FORMATS = new HashSet<String>(Arrays.asList(MIME_GML, MIME_PNG, MIME_TIFF, MIME_JP2, MIME_NETCDF));
    private GdalParameters gdalParams;
    public static final String DATATYPE_URN_PREFIX = "urn:ogc:def:dataType:OGC:1.1:"; // FIXME: now URNs are deprecated
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

    private static final Logger log = LoggerFactory.getLogger(DecodeFormatExtension.class);

    private String mimeType;
    private String extensionId;
    private Boolean hasParent;
    private String parentExtensionId;
    private Boolean multiPart;

    /**
     * Gets the extension identifier for the mimeType
     * @param mime - mimeType
     * @return extension Identifier
     */
    public String getExtensionIdentifier(String mime) {
        if (ExtensionsRegistry.mimeToIdentifier.containsKey(mime)) {
            return ExtensionsRegistry.mimeToIdentifier.get(mime);
        } else if (mime.equals(MIME_JP2)) {
            if (multiPart != null && multiPart)
                return ExtensionsRegistry.GMLJP2_IDENTIFIER;
            else
                return ExtensionsRegistry.JPEG2000_IDENTIFIER;
        } else {
            //never reaches here.
            log.error("DecodeFormatExtension class cannot handle " + mime + " MIMEtype");
            throw new IllegalArgumentException(mime + " MimeType cannot be handled");
        }
    }

    /**
     * Constructor for registering formats and also used while handling multipart requests
     * as two base requests
     */
    DecodeFormatExtension(String mime, Boolean isMultiPart) {
        this.mimeType = mime;
        this.multiPart = isMultiPart;
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
     * @param mime - mimeType
     * @return encoding
     */
    public String getEncoding(String mime) {

        if (ExtensionsRegistry.mimeToEncoding.containsKey(mime)) {
            return (String) ExtensionsRegistry.mimeToEncoding.get(mime);
        } else {
            //never reaches here.
            log.error("DecodeFormatExtension class cannot handle " + mime + " MIMEtype");
            throw new IllegalArgumentException(mime + "mimeType cannot be handled");
        }
    }

    /**
     *Checks if the request can be handled by this class.
     *Only called once before handling the user request.
     *Not called when helper functions of handle() calls handle() with a new instance while handling multipart requests.
     * @param req GetCoverage request
     * @return if the req can be handled
     */
    @Override
    public boolean canHandle(GetCoverageRequest req) {
        mimeType = req.getFormat().replace(" ", "+");
        multiPart = req.isMultiPart();
        // convert back from application/gml xml -> application/gml+xml
        String regFormat = req.getFormat().replace(" ", "+");
        boolean a = SUPPORTED_FORMATS.contains(regFormat) && !req.isMultiPart();
        boolean b = req.isMultiPart() && SUPPORTED_FORMATS.contains(regFormat) && !regFormat.equals(MIME_GML);
        return a || b;
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
        Response gml = (new DecodeFormatExtension(MIME_GML, false)).handle(request, meta);
        // get the image/netcdf file
        Response image = (new DecodeFormatExtension(request.getFormat(), false)).handle(request, meta);// ExtensionsRegistry.getFormatExtension(false, request.getFormat()).handle(request, meta);
        // return multipart response
        String xml = gml.getXml()[0].replace("{coverageData}",
                "<File>"
                        + "<fileName>" + "file" + "</fileName>"
                        + "<fileStructure>Record Interleaved</fileStructure>"
                        + "<mimeType>" + request.getFormat() + "</mimeType>"
                        + "</File>");
        multipartResponse = new Response(image.getData(), new String[]{xml}, request.getFormat());
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
                updateGetCoverageMetadata(request, m, meta);
                gml = WcsUtil.getBounds(gml, m);
            }


            return new Response(null, new String[]{gml}, FormatExtension.MIME_XML);
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
     * handles image requests for netcdf,png,geotiff, jpeg2000 and jp2+gml(multipart jpeg2000)
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
            if (!(WcsUtil.isGrid(m.getCoverageType()))) {
                throw new WCSException(ExceptionCode.NoApplicableCode, "The Netcdf format extension "
                        + "only supports GridCoverage and RectifiedGridCoverage");
            }
        } else {
            String type = request.getFormat().equals(MIME_TIFF) ?
                    "GeoTIFF" : getEncoding(request.getFormat()).toUpperCase();
            if (m.getGridDimension() != 2 || m.hasIrregularAxis() || !(WcsUtil.isGrid(m.getCoverageType()))) {
                throw new WCSException(ExceptionCode.InvalidRequest, "The " + type + " format extension "
                        + "only supports regularly gridded coverages with exactly two dimensions");
            }
        }

        Pair<Object, String> p = null;
        if (request.getFormat().equals(MIME_TIFF) || request.getFormat().equals(MIME_PNG) || m.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
            p = executeRasqlQuery(request, m, meta, getEncoding(request.getFormat()), null);
        } else {
            // RectifiedGrid: geometry is associated with a CRS -> return Netcdf with geo-metadata
            // Need to use the GetCoverage metadata which has updated bounds [see super.setBounds()]
            String[] domLo = m.getGisDomLow().split(" ");
            String[] domHi = m.getGisDomHigh().split(" ");

            if (request.getFormat().equals(MIME_JP2)) {
                if (domLo.length != 2 || domHi.length != 2) {
                    String containsGml = request.isMultiPart() ? "(+GML)" : "";
                    // Output grid dimensions have already been checked (see above), but double-check on the domain bounds:
                    log.error("Cannot format JPEG2000" + containsGml + ": output dimensionality is not 2.");
                    throw new WCSException(ExceptionCode.InvalidRequest, "Output dimensionality of the requested coverage is "
                            + (domLo.length == 2 ? domHi.length : domLo.length) + " whereas JPEG2000" + containsGml + "requires 2-dimensional grids.");
                }
            }

            gdalParams = new GdalParameters(domLo[0], domHi[0], domLo[1], domHi[1], m.getCrs());
            //checking if request is gml+jp2? multipart was set at canHandle()
            if (multiPart == null || multiPart)
                p = executeRasqlQuery(request, m, meta, getEncoding(request.getFormat()), gdalParams.toString());
            else {
                p = addGMLtoJP2(request, m, meta);
            }
        }
        RasQueryResult res = new RasQueryResult(p.fst);
        if (res.getMdds().isEmpty()) {
            return new Response(null, null, request.getFormat());
        } else {
            List<byte[]> data = new ArrayList<byte[]>();
            data.add(res.getMdds().get(0));
            return new Response(data, null, request.getFormat());
        }
    }

    /**
     * Adds GML to JP2 for multipart JP2 request
     *
     * @param request
     * @param m
     * @param meta
     * @return result of executing the query for GML+JP2
     * @throws PetascopeException
     * @throws SecoreException
     */
    private Pair<Object, String> addGMLtoJP2(GetCoverageRequest request, GetCoverageMetadata m, DbMetadataSource meta) throws PetascopeException, SecoreException {
        Pair<Object, String> p = null;
        gdalParams.addExtraParams(GDAL_CODEC_PARAM + '=' + JP2_CODEC);
        // Get GML description and store it in a tmp file for GDAL
        String gml = WcsUtil.getGML(m, Templates.GMLJP2_COVERAGE_COLLECTION, meta);
        File gmlFile;
        try {
            gmlFile = File.createTempFile(m.getCoverageId() + '_' + JP2_ENCODING, null); // arbitrary prefix (at least 3 chars)
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
            gdalParams.setConfigKeyValue(GMLJP2OVERRIDE_COPTION_KEY, gmlFile.getAbsolutePath());

            // get the data
            p = executeRasqlQuery(request, m, meta, JP2_ENCODING, gdalParams.toString());
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
        //for mime gml, getFormat() might return "application/gml xml"
        //getFormat never returns null. if request has format null, the default format gml is set
        String format = request.getFormat().replace(" ", "+");
        // multipart extensions except for jp2+gml are handled here
        boolean isMultiPart = request.isMultiPart() && multiPart != null && multiPart;
        boolean canbeHandled = SUPPORTED_FORMATS.contains(format) && !format.equals(MIME_JP2) && !format.equals(MIME_GML);
        if (isMultiPart && canbeHandled) {
            return getMultiPartResponse(request, meta);
        }
        GetCoverageMetadata m = new GetCoverageMetadata(request, meta);

        //Handle the range subset feature
        RangeSubsettingExtension rsubExt = (RangeSubsettingExtension) ExtensionsRegistry.getExtension(ExtensionsRegistry.RANGE_SUBSETTING_IDENTIFIER);
        rsubExt.handle(request, m);
        //netcdf,png,geotiff, jp2 and jp2+gml are handled inside this if statement
        // images for multipart formats are also handled here
        if (SUPPORTED_FORMATS.contains(format) && !format.equals(MIME_GML) && mimeType != null && !mimeType.equals(MIME_GML)) {
            return getImageResponse(request, meta);
        }
        // gml request and the gml part of multipart request is handled below.
        else if (format.equals(MIME_GML) || (mimeType != null && mimeType.equals(MIME_GML))) {
            return getGmlResponse(request, meta);
        } else {
            //never reaches here.
            log.error("DecodeFormatExtension.handle() could not handle the request:" + request + " meta:" + meta + " Class variables:" +
                    "multiPart,mimeType:" + multiPart + " " + mimeType);
            throw new PetascopeException(ExceptionCode.InternalComponentError, "Request did not first pass through canHandle(). see log!");
        }
    }

    /**
     * Inserts rangeSet values for grid-coverages.
     *
     * @param gml
     * @param request
     * @param meta
     * @param m
     * @throws WCSException
     * @throws PetascopeException
     */
    protected String addCoverageData(String gml, GetCoverageRequest request, DbMetadataSource meta, GetCoverageMetadata m)
            throws WCSException, PetascopeException {
        //return gml without {coverageData} replaced for multipart requests
        if (request.isMultiPart())
            return gml;
        RasQueryResult res = new RasQueryResult(executeRasqlQuery(request, m, meta, CSV_ENCODING, null).fst);
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
        return new Response(new String[]{sb.toString()});
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
}
