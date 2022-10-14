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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.controller;

import com.rasdaman.accesscontrol.service.AuthenticationService;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.config.VersionManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import static petascope.controller.AuthenticationController.READ_WRITE_RIGHTS;
import petascope.controller.handler.service.AbstractHandler;
import petascope.controller.handler.service.XMLWCSServiceHandler;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.KEY_ACCEPTVERSIONS;


import static petascope.core.KVPSymbols.KEY_REQUEST;
import static petascope.core.KVPSymbols.VALUE_INSERT_COVERAGE;
import static petascope.core.KVPSymbols.VALUE_UPDATE_COVERAGE;
import static petascope.core.KVPSymbols.WCS_SERVICE;
import static petascope.core.KVPSymbols.WMS_SERVICE;
import petascope.core.Pair;
import petascope.core.XMLSymbols;
import petascope.core.response.MultipartResponse;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.PetascopeRuntimeException;
import petascope.exceptions.WMTSException;
import petascope.util.ExceptionUtil;
import petascope.util.IOUtil;
import petascope.util.ListUtil;
import petascope.util.MIMEUtil;
import petascope.util.StringUtil;
import static petascope.util.StringUtil.AND_SIGN;
import static petascope.util.StringUtil.DOLLAR_SIGN;
import static petascope.util.StringUtil.EQUAL_SIGN;
import static petascope.util.StringUtil.POST_STRING_CONTENT_TYPE;
import static petascope.util.StringUtil.POST_TEXT_PLAIN_CONTENT_TYPE;
import static petascope.util.StringUtil.POST_XML_CONTENT_TYPE;
import static petascope.util.StringUtil.POST_XML_SOAP_CONTENT_TYPE;
import petascope.util.XMLUtil;
import petascope.util.ras.RasUtil;

/**
 * Abstract class for controllers
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public abstract class AbstractController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AbstractController.class);
    
    private static long REQUEST_COUNTER = 0;
    private static final String REQUEST_COUNTER_PREFIX = "req-";
    
    // When petascope cannot start for some reasons, just not throw the exception until it can start the web application and throw exception to user via HTTP request
    public static Exception startException;

    @Autowired
    protected HttpServletRequest injectedHttpServletRequest;
    @Autowired
    protected HttpServletResponse injectedHttpServletResponse;
    @Autowired
    private XMLWCSServiceHandler xmlWCSServiceHandler;
  
    @Resource
    // Spring finds all the subclass of AbstractHandler and injects to the list
    List<AbstractHandler> handlers;
    
    // **************** Configuration for controllers which needs to be logged in before processing request ****************
    // store the loggin information to session
    protected static final String USERNAME_SESSION = "username_session";
    protected static final String IS_SUCCESS_ATTRIBUTE = "isSuccess";

    protected static final String USERNAME = "username";
    protected static final String PASSWORD = "password";

    protected static final String CLIENT_REQUEST_URI_ATTRIBUTE = "rascontrol";
    protected static final String CLIENT_SUCCESS_ATTRIBUTE = "success";
    
    // If URL contains this parameter (e.g: ?logout) then remove the logged in session and returns the login.jsp page
    public static final String LOGOUT_PARAM = "logout";
    
    // When result_bytes of all processed requests > this number (500MB), call the gabarge co
    private static final Long GARBAGE_COLLECTION_THRESHOLD = 500000000l;
    // All the returned bytes to clients up to this current request
    private static Long totalReturnedBytes = 0l;
    
    /**
     * If an exception occurs when petascope starts, just defer it until web application can start and exception can be thrown via controllers
     * @throws PetascopeException 
     */
    protected void throwStartException() throws PetascopeException {
        throw new PetascopeException(ExceptionCode.InternalComponentError, 
                "Cannot start petascope, reason '" + startException.getMessage() + "'.", startException);
    }

    /**
     * Handler for GET requests.
     */
    abstract protected void handleGet(HttpServletRequest httpServletRequest) throws Exception;

    private static String getSubmittedFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1); // MSIE fix.
            }
        }
        
        return null;
    }
    
    /**
     * Handle POST request with/without attached files in the POST body
     */
    protected void handlePost(HttpServletRequest httpServletRequest) throws IOException, Exception {
        Map<String, String[]> kvpParameters = this.parsePostRequestToKVPMap(httpServletRequest);
        this.requestDispatcher(httpServletRequest, kvpParameters);
    }
    
    /**
     * Parse the content of POST request to a map of key values pair
     */
    protected Map<String, String[]> parsePostRequestToKVPMap(HttpServletRequest httpServletRequest) throws IOException, Exception {
        Map<String, String[]> kvpParameters = new LinkedHashMap<>();;
        String queryString = httpServletRequest.getQueryString();
        // in case with POST KVP format
        
        boolean isMultipart = httpServletRequest instanceof StandardMultipartHttpServletRequest;
            
        if (isMultipart) {
            StandardMultipartHttpServletRequest request = (StandardMultipartHttpServletRequest)httpServletRequest;
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                String key = entry.getKey().toLowerCase();
                String[] values = entry.getValue();
                kvpParameters.put(key, values);
            }
            
            for (Map.Entry<String, List<MultipartFile>> entry : request.getMultiFileMap().entrySet()) {
                MultipartFile firstMultipartFile = entry.getValue().get(0);
                String fileName = firstMultipartFile.getOriginalFilename();
                
                String uploadedFilePath = this.getUploadedFilePathOnServer(fileName, firstMultipartFile.getBytes());
                // stored file in servere.g: /tmp/rasdaman_petascope/rasdman.test.1122332.tif
                String key = entry.getKey();
                if (key.equals(KVPSymbols.KEY_UPLOADED_FILE)) {
                    // non-positional parameters (e.g. key=1 or key=2 for WCPS decode())
                    key = KVPSymbols.KEY_INTERNAL_UPLOADED_FILE_PATH.toLowerCase();
                }
                
                String[] values = new String[] {uploadedFilePath};
                kvpParameters.put(key, values);
            }
            
        } else {
            String requestContentType = httpServletRequest.getContentType();
            if (requestContentType == null || requestContentType.equals(POST_STRING_CONTENT_TYPE)
                || requestContentType.contains(POST_TEXT_PLAIN_CONTENT_TYPE)
                || requestContentType.contains(POST_XML_CONTENT_TYPE)
                || requestContentType.contains(POST_XML_SOAP_CONTENT_TYPE)) {
                // post request without files in body
                String postBody = this.getPOSTRequestBody(httpServletRequest);
                kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
            } else {
                // post request with files in body
                for (Part part : httpServletRequest.getParts()) {
                    // e.g: query=for ...
                    String key = part.getName();
                    byte[] bytes = IOUtils.toByteArray(part.getInputStream());

                    if (part.getContentType() == null) {
                        // KEY=VALUE as string                        
                        String value = new String(bytes);
                        queryString += AND_SIGN + key + EQUAL_SIGN + value;
                    } else {
                        // KEY=Uploaded_File_Content as binary (e.g: $1=/tmp/test.tif)                        
                        String fileName = getSubmittedFileName(part);
                        // stored file in servere.g: /tmp/rasdaman_petascope/rasdman.test.1122332.tif
                        String uploadedFilePath = this.getUploadedFilePathOnServer(fileName, bytes);
                        queryString += AND_SIGN + key + EQUAL_SIGN + uploadedFilePath;
                    }
                }

                kvpParameters = this.buildPostRequestKvpParametersMap(queryString);
            }
        }        
        
        return kvpParameters;
    }

    /**
     * Depend on the requested service then pass the map of keys, values
     * parameters to the corresponding handler
     */
    abstract protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception;

    /**
     * From the GET request query string to map of key / values which is encoded
     */
    public Map<String, String[]> buildGetRequestKvpParametersMap(String queryString) throws PetascopeException {
        if (queryString == null) {
            queryString = "";
        }
        queryString = queryString.trim();

        // Query is already encoded from Browser or client, but the "+" is not encoded, so encode it correctly.
        // NOTE: it does not matter if space character is encoded to "+" or "%2B", just replace it when a query string is encoded from client).
        queryString = queryString.replaceAll("\\+", "%2B");

        return buildKvpParametersMap(queryString);
    }
    
    /**
     * From the POST request query string to a map of keys, values which is
     * encoded or raw.
     */
    protected Map<String, String[]> buildPostRequestKvpParametersMap(String queryString) throws PetascopeException {
        if (queryString == null) {
            queryString = "";
        }
        queryString = queryString.trim();

        if (queryString.contains("%")) {
            // NOTE: it does not matter if space character is encoded to "+" or "%2B", just replace it when a query string is encoded from client).
            queryString = queryString.replaceAll("\\+", " ");
        } else {
            // If queryString is not encoded (raw in POST request), so must keep "+" by encoding it correctly      
            queryString = queryString.replaceAll("\\+", "%2B");
        }
        
        return this.buildKvpParametersMap(queryString);
    }

    /**
     * Build the map of keys values for both GET/POST request
     */
    private Map<String, String[]> buildKvpParametersMap(String queryString) throws PetascopeException {
        // It needs to relax the key parameters with case insensitive, e.g: request=DescribeCoverage or REQUEST=DescribeCoverage is ok
        
        Map<String, String[]> kvpParameters = new LinkedCaseInsensitiveMap<>();
        
        final String QUERY = "query=";

        List<String> wcpsVersions = VersionManager.getAllSupportedVersions(KVPSymbols.WCPS_SERVICE);
        String[] supportedWCPSVersions = wcpsVersions.toArray(new String[wcpsVersions.size()]);
        
        boolean decoded = false; 
        
        if (!(queryString.contains(KVPSymbols.KEY_COVERAGE + "=")
            || queryString.contains(KVPSymbols.KEY_INPUT_COVERAGE + "="))) {
            // NOTE: WCS-T Insert/Update coverage requests posted by wcst_import with character & between key value pairs
            // are not encoded.
            queryString = StringUtil.decodeUTF8(queryString);
            decoded = true;
        }

        if (!XMLUtil.isXmlString(queryString.replace(QUERY, ""))) {
            // The request is in KVP GET/POST requests
            kvpParameters = parseKVPParameters(queryString, decoded);
        } else {
            // NOTE: Try to parse the query string in POST/SOAP XML as it have to be KVP and data url-encoded
            // e.g: query=<?xml.....> or without query= (in case of POST with curl --data, this has problem with "+" as it is not encoded as %2B, so just for backwards compatibility            
            String requestBody = queryString;
            if (requestBody.startsWith(QUERY)) {
                // Only get the request content in XML
                requestBody = requestBody.split(QUERY)[1];
            }

            // The request is in POST XML or SOAP requests with XML syntax
            String root = XMLUtil.getRootElementName(requestBody);   
            boolean isSoap = false;
            if (root.equals(XMLSymbols.LABEL_ENVELOPE)) {
                isSoap = true;
                requestBody = XMLUtil.extractWcsRequest(queryString);
            }
            
            try {
                kvpParameters = xmlWCSServiceHandler.parseRequestBodyToKVPMaps(requestBody);
            } catch (PetascopeException ex) {
                // NOTE: SOAP request exception must be wrapped by SOAP body for OGC CITEs tests
                throw new PetascopeException(ex.getExceptionCode(), ex, isSoap);
            }
            
            if (isSoap) {
                // NOTE: In case request is SOAP (e.g. from OGC CITE tests), then it needs special treatment for output encoded in SOAP body
                // This is very important, otherwise the SOAP tests from OGC CITE test will fail (!)
                if (this.getValueByKeyAllowNull(kvpParameters, KVPSymbols.KEY_SERVICE) != null) {
                    kvpParameters.put(KVPSymbols.KEY_SERVICE, new String[] { KVPSymbols.KEY_SOAP });
                    kvpParameters.put(KVPSymbols.KEY_REQUEST_BODY, new String[] { requestBody });
                    
                    return kvpParameters;
                }
            }

        }

        // Validate the parsed KVP maps for all requirement parameters (only when it has at least 1 parameter as an empty request will return WCS-Client)
        if (kvpParameters.isEmpty()) {
            return kvpParameters;
        }

        // backwards compatibility for WCPS ows?query="" is ok to handle
        if ((kvpParameters.containsKey(KVPSymbols.KEY_QUERY) 
            ||  kvpParameters.containsKey(KVPSymbols.KEY_QUERY_SHORT_HAND))
                && !kvpParameters.containsKey(KVPSymbols.KEY_USERNAME)) {
            kvpParameters.put(KVPSymbols.KEY_SERVICE, new String[] {KVPSymbols.WCPS_SERVICE});
            kvpParameters.put(KVPSymbols.KEY_VERSION, supportedWCPSVersions);
            kvpParameters.put(KVPSymbols.KEY_REQUEST, new String[] {KVPSymbols.VALUE_PROCESS_COVERAGES});
        }

        String service = getValueByKeyAllowNull(kvpParameters, KVPSymbols.KEY_SERVICE);
        String request = getValueByKeyAllowNull(kvpParameters, KVPSymbols.KEY_REQUEST);
        String versions[] = getValuesByKeyAllowNull(kvpParameters, KVPSymbols.KEY_VERSION);
        
        if (VersionManager.isWMSRequest(versions) && service == null) {
            kvpParameters.put(KVPSymbols.KEY_SERVICE, new String[] { KVPSymbols.WMS_SERVICE });
        }        

        // e.g: Rasql servlet does not contains these requirement parameters
        if (service != null) {
            
            if (service != null && request == null) {
                if (service.equals(KVPSymbols.WMTS_SERVICE)) {
                    throw new WMTSException(new ExceptionCode(ExceptionCode.MissingParameterValue, KVPSymbols.KEY_REQUEST)); 
                } else {
                    throw new PetascopeException(new ExceptionCode(ExceptionCode.MissingParameterValue, KVPSymbols.KEY_REQUEST)); 
                }
            }
            
            // NOTE: WMS allows version is null, so just use the latest WMS version
            if (service.equals(KVPSymbols.WMS_SERVICE) && versions == null) {
                log.debug("WMS received request without version parameter, use the default version: " + VersionManager.getLatestVersion(WMS_SERVICE));
                kvpParameters.put(KVPSymbols.KEY_VERSION, new String[] {VersionManager.getLatestVersion(WMS_SERVICE)});
            } else if (service.equals(KVPSymbols.WCS_SERVICE) 
                        && request.equals(KVPSymbols.VALUE_GET_CAPABILITIES)) {
                // It should use AcceptVersions for WCS GetCapabilities
                if (kvpParameters.get(KVPSymbols.KEY_ACCEPTVERSIONS) != null) {
                    String value = getValuesByKey(kvpParameters, KVPSymbols.KEY_ACCEPTVERSIONS)[0];
                    versions = value.split(",");
                    kvpParameters.put(KVPSymbols.KEY_VERSION, versions);
                }

                if (versions == null) {
                    // NOTE: only petascope allows to request GetCapabilities without known versions before-hand
                    versions = new String[] {VersionManager.getLatestVersion(WCS_SERVICE)};
                    kvpParameters.put(KVPSymbols.KEY_VERSION, versions);
                }
            } else if (request.equalsIgnoreCase(KVPSymbols.VALUE_GET_CAPABILITIES)) {
                String[] acceptVersions = getValuesByKeyAllowNull(kvpParameters, KEY_ACCEPTVERSIONS);
                if (acceptVersions != null) {
                    kvpParameters.put(KVPSymbols.KEY_VERSION, acceptVersions);
                }
            }
        }

        return kvpParameters;
    }

    /**
     * Check if petascope can read file's content from uploaded multipart file. If yes, then get file's content.
     */
    protected byte[] getUploadedMultipartFileContent(MultipartFile uploadedFile) throws PetascopeException {
        byte[] bytes = null;            
            try {
                bytes = uploadedFile.getBytes();
            } catch (IOException ex) {
                throw new PetascopeException(ExceptionCode.IOConnectionError, 
                        "Cannot get data from uploaded file. Reason: " + ex.getMessage() + ".", ex);
            }
            
        return bytes;
    }
    
    /**
     * Write the uploaded file from client and store to a folder in server
     * @return the stored file path in server
     */
    protected String getUploadedFilePathOnServer(String uploadedFileName, byte[] bytes) throws PetascopeException {
        
        DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
        String date = formatter.format(new Date());
        String dataDirPath = ConfigManager.UPLOADED_FILE_DIR_TMP + "/" + date;
        IOUtil.makeDir(dataDirPath);
        
        uploadedFileName = StringUtil.replaceSpecialCharacters(uploadedFileName);
        String filePath = dataDirPath + "/" + uploadedFileName;
        if (Files.exists(Paths.get(filePath))) {
            filePath = StringUtil.addDateTimeSuffix(filePath);
        }

        Path path = Paths.get(filePath);
        try {
            Files.write(path, bytes);
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.IOConnectionError, 
                    "Cannot store uploaded file to '" + filePath + "'. Reason: " + ex.getMessage() + ".", ex);
        }
        
        log.debug("Uploaded file to '" + filePath + "'.");
        
        return filePath;
    }

    /**
     * Parse the POST request body from the input HTTP request
     */
    protected String getPOSTRequestBody(HttpServletRequest httpServletRequest) throws PetascopeException {
        String requestBody = "";
        if (httpServletRequest.getQueryString() != null) {
            // case 1: POST a file with a KVP request to server (e.g: rasql post file to server to import)    
            // or POST a KVP request (as same as GET a KVP request)
            requestBody = httpServletRequest.getQueryString();
        }
        
        try {
            if (httpServletRequest.getReader() != null) {
                // case 2: a POST XML, SOAP body
                if (requestBody.isEmpty()) {
                    requestBody = IOUtils.toString(httpServletRequest.getReader());
                } else {
                    requestBody += "&" + IOUtils.toString(httpServletRequest.getReader());
                }
            }
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.IOConnectionError, "Cannot get POST request body from HTTPServlet header. Reason: " + ex.getMessage());
        }
        
        log.debug("Received POST body: " + requestBody);
            
        return requestBody;
    }

    /**
     * Write the response as text or binary to the requesting client.
     */
    protected void writeResponseResult(Response response) throws PetascopeException {
        // This one is needed as normally it write the result with HTTP:200, 
        // but for SOAP case when error message is enclosed in envelope, it can return HTTP:400, 404
        injectedHttpServletResponse.setStatus(response.getHTTPCode());
        injectedHttpServletResponse.setContentType(response.getMimeType());
        addFileNameToHeader(response);
        OutputStream os = null;
        
        try {
            os = injectedHttpServletResponse.getOutputStream();
            String mimeType = getMimeType(response);
            if (!response.hasDatas())
                writeEmptyResponse(os);
            else if (response.isMultipart())
                writeMultipartResponse(response, mimeType, os);
            else
                writeSinglepartResponse(response, mimeType, os);
        } catch(Exception ex) {
            if (ex.getMessage().contains("Connection reset by peer")) {
                // e.g: when client sends a request to return large data and it cancels when the download is not finished yet
                log.error("Lost connection to client.");
            }
            throw new PetascopeException(ExceptionCode.IOConnectionError, "Cannot write respond result to client. Reason: " + ex.getMessage(), ex);
        } finally {
            try {
                if (os != null) {
                    IOUtils.closeQuietly(os);
                }
            } catch(Exception ex) {
                throw new PetascopeException(ExceptionCode.IOConnectionError, "Cannot close OutputStream while writing response. Reason: " + ex.getMessage(), ex);
            }
            runGarbageCollectionIfNeeded(response);
            // Release the data occupied by byte[] right now
            response = null;
        }
    }
    
    protected String getMimeType(Response response) {
        String mimeType = response.getFormatType();
        // To display application/gml+xml in browser, change in HTTP response to text/xml
        if (mimeType.equals(MIMEUtil.MIME_GML)) {
            mimeType = MIMEUtil.MIME_XML;
        }
        return mimeType;
    }
    
    protected void addFileNameToHeader(Response response) throws PetascopeException {
        
        String mimeType = response.getFormatType();
        String fileName = response.getCoverageID() + "." + MIMEUtil.getFileNameExtension(mimeType);
        injectedHttpServletResponse.setHeader("File-name", fileName);
        // If multipart then must download file from Browser
        if (response.hasDatas()) {
            if (response.isMultipart()) {
                injectedHttpServletResponse.setHeader("Content-disposition", "attachment; filename=" + fileName);
            } else {
                // If a GetCoverage request or a processCoverage request with coverageId result, then download the result with file name
                // e.g: query=for c in (test_mr) return encode(c, "tiff"), then download file: test_mr.tiff
                // NOTE: Content-Disposition: attachment; will download file in WebBrowser instead of trying to display (GML/PNG/JPEG) type.
                injectedHttpServletResponse.setHeader("Content-disposition", "inline; filename=" + fileName);                
            }
        }
    }
    
    /**
     * Write single result to output stream os. Does not do any checks, assumes
     * that response contains at least one result.
     */
    protected void writeSinglepartResponse(Response response, String mimeType, OutputStream os) throws IOException {
        injectedHttpServletResponse.setContentType(mimeType);
        IOUtils.write(response.getDatas().get(0), os);
    }
    
    /**
     * Write multiple results to output stream os. Does not do any checks, assumes
     * that response contains at least one result.
     */
    protected void writeMultipartResponse(Response response, String mimeType, OutputStream os) throws IOException {
        MultipartResponse multi;
        multi = new MultipartResponse(injectedHttpServletResponse);
        int i = 0;
        for (byte[] data : response.getDatas()) {
            if (i > 0) {
                multi.addLine();
            }
            multi.endPart();
            multi.writeContentType(mimeType);
            IOUtils.write(data, os);            
            
            i++;            
        }
        multi.finish();
    }
    
    /**
     * In wcst_import like deleteCoverage, it just returns empty string as a success.
     */
    protected void writeEmptyResponse(OutputStream os) throws IOException {
        IOUtils.write("", os);
    }
    
    private void runGarbageCollectionIfNeeded(Response response) {                           
        if (response.hasDatas()) {
            for (byte[] bytes : response.getDatas()) {
                totalReturnedBytes += bytes.length;
            }
        }
        // Don't call gabarge collector in every case, it will slow down the time 
        // to receive new request to petascope controller.
        // Only call it when the cumulative result of past requests reaches > 500 MB
        if (totalReturnedBytes > GARBAGE_COLLECTION_THRESHOLD) {
            System.gc();
            totalReturnedBytes = 0l;
        }
    }

    /**
     * Log the request GET/POST from kvpParameters map
     */
    public static String getRequestRepresentation(Map<String, String[]> kvpParametersMap) {
        String request = "";
        boolean importCoverage = false;
        for (Map.Entry<String, String[]> entry : kvpParametersMap.entrySet()) {
            String key = entry.getKey();
            if (key.equalsIgnoreCase(KEY_REQUEST)) {
                String value = getValueByKeyAllowNull(kvpParametersMap, key);
                if (value != null && (value.equalsIgnoreCase(VALUE_INSERT_COVERAGE) || value.equalsIgnoreCase(VALUE_UPDATE_COVERAGE))) {
                    importCoverage = true;
                    break;
                }
            }
        }
        
        for (Map.Entry<String, String[]> entry : kvpParametersMap.entrySet()) {
            String key = entry.getKey();
            
            for (String value : entry.getValue()) {
                
                // As they contain long GML text, no need to show
                if (importCoverage) {
                    if (entry.getKey().equalsIgnoreCase(KVPSymbols.KEY_COVERAGE)
                      || entry.getKey().equalsIgnoreCase(KVPSymbols.KEY_INPUT_COVERAGE)) {
                        int size = 50;
                        if (value.length() < 50) {
                            size = value.length();
                        }
                        value = value.substring(0, size) + "...";
                    }
                }
                
                if (key.equalsIgnoreCase(KVPSymbols.KEY_WMS_LEGEND_GRAPHIC)) {
                    int size = 50;
                    if (value.length() < 50) {
                        size = value.length();
                    }
                    
                    value = value.substring(0, size) + "...";
                }
                
                request = request + entry.getKey() + "=";
                request = request + value + "&";
            }
        }
        if (!request.isEmpty()) {
            return request.substring(0, request.length() - 1);
        } else {
            return request;
        }
    }
    
    /**
     * Write response as string
     */
    protected void writeTextResponse(Object obj) throws IOException, PetascopeException {
        byte[] bytes = obj.toString().getBytes();
        List<byte[]> bytesList = new ArrayList<>();
        bytesList.add(bytes);

        Response response = new Response(bytesList, MIMEUtil.MIME_TEXT);
        this.writeResponseResult(response);
    }

    /**
     * Parse the KVP parameters to map of keys and values
     */
    private static Map<String, String[]> parseKVPParameters(String queryString, boolean decoded) throws PetascopeException {
        
        // decode e.g. request=GetMap&amp;format=png -> request=GetMap&format=png
        queryString = StringEscapeUtils.unescapeHtml4(queryString);
        
        Map<String, String[]> parametersMap = new LinkedCaseInsensitiveMap<>();
        if (!queryString.equals("")) {
            
            String[] keyValuesTmp = queryString.split("&");
            List<String> keyValues = new ArrayList<>();
            for (int i = 0; i < keyValuesTmp.length; i++) {
                String currentItem = keyValuesTmp[i];
                String correctKeyValue = currentItem;
                                    
                if (i < keyValuesTmp.length - 1) {

                    // NOTE: for the case with special character &lt; &gt; and &amp; in escaped XML string       
                    // e.g. &ColorTableDefinition=....label="&lt; 0.25" 
                        
                    for (int j = i + 1; j < keyValuesTmp.length; j++) {
                        
                        String nextItem = keyValuesTmp[j];
                        if (nextItem.startsWith("lt;") || nextItem.startsWith("gt;") || nextItem.startsWith("amp;")) {
                            correctKeyValue += "&" + nextItem;
                            i = j;
                        } else {
                            break;
                        }
                    }           
                }
                
                keyValues.add(correctKeyValue);
            }
            
            for (String keyValue : keyValues) {
                // No parse keyValue when it empty such as: &service=DescribeCoverage&coverageId=test_mr, then the first & is empty string
                if (keyValue.isEmpty()) {
                    continue;
                }
                String key = null, value = "";
                // e.g: request=DescribeCoverage
                if (keyValue.contains("=")) {
                    // e.g: it only splits the first occurence (query=select c from (test_mr) encode(c, "nodata=200") is still a key=value)
                    String[] tmp = keyValue.split("=", 2);
                    if (tmp[0] != null) {
                        key = tmp[0];
                    }
                    if (tmp.length > 1) {
                        // e.g: WMS Styles=&
                        value = tmp[1];
                    }
                } else {
                    // e.g: WMS: Style=&
                    key = keyValue;
                }
                
                if (!decoded) {                    
                    value = StringUtil.decodeUTF8(value);
                }
                
                if (key.startsWith(DOLLAR_SIGN) && parametersMap.get(key) != null) {
                    throw new PetascopeException(ExceptionCode.InvalidRequest, 
                            "Positonal parameter must not be duplicate in the request. "
                            + "Given parameter '" + key.replace(DOLLAR_SIGN, "") + "'.");
                }

                if (parametersMap.get(key) == null) {
                    parametersMap.put(key.toLowerCase(), new String[]{value});
                } else {
                    String[] values = parametersMap.get(key);
                    String[] newValues = new String[values.length + 1];
                    for (int i = 0; i < values.length; i++) {
                        newValues[i] = values[i];
                    }
                    newValues[values.length] = value;

                    parametersMap.put(key.toLowerCase(), newValues);
                }
            }
        }

        return parametersMap;
    }

    /**
     * Return the single value of a key in KVP parameters
     */
    public static String[] getValuesByKey(Map<String, String[]> kvpParameters, String key) throws PetascopeException {
        String[] values = kvpParameters.get(key.toLowerCase());
        if (values == null) {
            throw new PetascopeException(ExceptionCode.BadResponseHandler, 
                    "Cannot find value from KVP parameters map for key '" + key + "'.");
        }
        
        if (values[0].contains(",")) {
            values = values[0].split(",");
        }

        return values;
    }
    
    /**
     * Return the single value of a key in KVP parameters
     * It is ok if the value is null 
     */
    public static String[] getValuesByKeyAllowNull(Map<String, String[]> kvpParameters, String key) throws PetascopeException {
        String[] values = kvpParameters.get(key.toLowerCase());
        if (values == null) {
            return values;
        }
        
        if (values[0].contains(",")) {
            values = values[0].split(",");
        }

        return values;
    }
    
    /**
     * Return the single value of a key in KVP parameters
     */
    public static String getValueByKey(Map<String, String[]> kvpParameters, String key) throws PetascopeException {
        String[] values = kvpParameters.get(key.toLowerCase());
        if (values == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                    "Cannot find value from KVP parameters map for key '" + key + "'.");
        }
        
        return values[0].trim();
    }
    
    /**
     * Return the single value of a key in KVP parameters
     * It is ok if the value is null 
     */
    public static String getValueByKeyAllowNull(Map<String, String[]> kvpParameters, String key) {
        String[] values = kvpParameters.get(key.toLowerCase());
        if (values == null) {
            return null;
        }
        
        return values[0].trim();
    }
    
    /**
     * Return a string representing a HTTP query string from a given map of key value pairs
     */
    public static String buildRequestQueryString(Map<String, String[]> kvpParameters) {
        List<String> results = new ArrayList<>();
        
        for (Map.Entry<String, String[]> entry: kvpParameters.entrySet()) {
            for (String value : entry.getValue()) {
                results.add(entry.getKey() + "=" + value);
            }
        }
        
        // e.g: request=GetCapabilities&version=2.0.1
        return ListUtil.join(results, "&");
    }
    

    /**
     * Check if a source IP address can send a write request to petascope.
     */
    protected void validateWriteRequestFromIP(List<String> writeRequest, String request, String sourceIP) throws PetascopeException {
        
        if (!ConfigManager.ALLOW_WRITE_REQUESTS_FROM.contains(ConfigManager.PUBLIC_WRITE_REQUESTS_FROM)) {
            // localhost IP in servlet
            if (sourceIP.equals("0:0:0:0:0:0:0:1") || sourceIP.equals("::1")) {
                sourceIP = "127.0.0.1";
            }

            if (writeRequest.contains(request)) {
                if (!ConfigManager.ALLOW_WRITE_REQUESTS_FROM.contains(sourceIP)) {
                    throw new PetascopeException(ExceptionCode.AccessDenied, 
                                                "Write request '" + request + "' is not permitted from IP address '" + sourceIP + "'.");
                }
            }
        }
    }
    
    /**
     * Check if a source IP address can send a write request to petascope.
     */
    protected void validateWriteRequestFromIP(String request, String sourceIP) throws PetascopeException {
        
        if (!ConfigManager.ALLOW_WRITE_REQUESTS_FROM.contains(ConfigManager.PUBLIC_WRITE_REQUESTS_FROM)) {
            // localhost IP in servlet
            if (sourceIP.equals("0:0:0:0:0:0:0:1") || sourceIP.equals("::1")) {
                sourceIP = "127.0.0.1";
            }

            if (!ConfigManager.ALLOW_WRITE_REQUESTS_FROM.contains(sourceIP)) {
                throw new PetascopeException(ExceptionCode.AccessDenied, 
                                            "Write request '" + request + "' is not permitted from IP address '" + sourceIP + "'.");
            }
        }
    }
    
    /**
     * Get request IP address from client to petascope
     */
    protected String getRequestIPAddress() {
        // in case, petascope is behind Apache proxy, then get the forwared IP via proxy
        String sourceIP = this.injectedHttpServletRequest.getHeader("X-FORWARDED-FOR");
        if (sourceIP == null) {
            // In case petascope is not proxied by apache
            sourceIP = this.injectedHttpServletRequest.getRemoteAddr();
        }
        
        return sourceIP;
    }
    
    public void logReceivedRequest(Map<String, String[]> kvpParameters) throws PetascopeException {
        String requestTmp = StringUtil.enquoteSingleIfNotEnquotedAlready(this.injectedHttpServletRequest.getRequestURI() + "?" + this.getRequestRepresentation(kvpParameters));
      
        log.info("Received request: " + requestTmp);
    }
    
    public void logHandledRequest(boolean requestSuccess, long start, Map<String, String[]> kvpParameters, Exception ex) throws PetascopeException {
        String requestTmp = StringUtil.enquoteSingleIfNotEnquotedAlready(this.injectedHttpServletRequest.getRequestURI() + "?" + this.getRequestRepresentation(kvpParameters));

        long end = System.currentTimeMillis();
        long totalTime = end - start;
        if (requestSuccess) {
            log.info("Processed request: " + requestTmp + " in " + String.valueOf(totalTime) + " ms.");
        } else {
            String errorMessage = "Failed processing request " + requestTmp 
                                + ", evaluation time " + String.valueOf(totalTime) + " ms. Reason: " + ex.getMessage();
            log.error(errorMessage);
        }
    }
    
    /**
     * This method is used to log the received request and time to process the request if it is successful
     */
    private Pair<String, Long> getRequestRepresentationAndTimeForLog(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws PetascopeException {
        String requestTmp = httpServletRequest.getRequestURI();
        
        if (kvpParameters != null) {
            requestTmp = httpServletRequest.getRequestURI() + "?" + this.getRequestRepresentation(kvpParameters);
        }
        
        // e.g req-10
        String requestCounter = REQUEST_COUNTER_PREFIX + REQUEST_COUNTER;
        REQUEST_COUNTER++;
        
        log.info("Received request " + requestCounter + ": " + requestTmp);
        
        return new Pair<>(requestCounter, System.currentTimeMillis());
    } 
    
    private void logError(String requestCounter, long startTime, Exception ex) throws Exception {
        // default it is 2.0.1
        String version = VersionManager.WCS_VERSION_20;
        if (ex instanceof PetascopeRuntimeException) {
            PetascopeRuntimeException pex = ((PetascopeRuntimeException)ex);
            ex = pex.getException();
            version = pex.getVersion();
        }
        String errorMessage = "Failed processing request " + requestCounter 
                                + ", evaluation time " + String.valueOf(System.currentTimeMillis() - startTime) + " ms. Reason: " + ex.getMessage();
        log.error(errorMessage);      
        ExceptionUtil.handle(version, ex, injectedHttpServletResponse);
    }
    
    private void logSuccess(String requestCounter, long startTime) {
        log.info("Processed request " + requestCounter + " in " + String.valueOf(System.currentTimeMillis() - startTime) + " ms.");
    }
    
    /**
     * Depend on if request is GET/POST to create map of KVP pairs
     */
    public Map<String, String[]> parseKvpParametersFromRequest(HttpServletRequest httpServletRequest) throws PetascopeException {
        Map<String, String[]> kvpParameters = this.buildGetRequestKvpParametersMap(httpServletRequest.getQueryString());
        if (this.isPostRequest(httpServletRequest)) {
            String postBody = this.getPOSTRequestBody(httpServletRequest);
            kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        }
        
        return kvpParameters;
    }
    
    /**
     * Handle received requests in child controllers and log which requests received and time to process requests
     */
    public void handleRequest(Map<String, String[]> kvpParameters, RequestHandlerInterface requestHandlerInterface) throws Exception {
        Pair<String, Long> requestStartingTimePair = null;
        boolean requestSuccess = true;
        try {
            requestStartingTimePair = this.getRequestRepresentationAndTimeForLog(this.injectedHttpServletRequest, kvpParameters);
            requestHandlerInterface.handle();            
        } catch(Exception ex) {
            requestSuccess = false;
            if (requestStartingTimePair != null) {
                this.logError(requestStartingTimePair.fst, requestStartingTimePair.snd, ex);
            }
        } finally {
            if (requestSuccess) {
                this.logSuccess(requestStartingTimePair.fst, requestStartingTimePair.snd);
            }
        }
    }
    
    
    
    // --------------------------- validate roles / ips
   
        
    /**
     * Get request IP address from client to petascope
     */
    public String getRequestIPAddress(HttpServletRequest httpServletRequest) {
        // in case, petascope is behind Apache proxy, then get the forwared IP via proxy
        String sourceIP = httpServletRequest.getHeader("X-FORWARDED-FOR");
        if (sourceIP == null) {
            // In case petascope is not proxied by apache
            sourceIP = httpServletRequest.getRemoteAddr();
        }
        
        return sourceIP;
    }

    /**
     * If basic authentication header is not enabled, then petascope checks if write request from IP address is valid or not
     * before processing.
     */
    public void validateWriteRequestFromIP(HttpServletRequest httpServletRequest) throws PetascopeException {
        if (!ConfigManager.ALLOW_WRITE_REQUESTS_FROM.contains(ConfigManager.PUBLIC_WRITE_REQUESTS_FROM)) {
            
            String sourceIP = this.getRequestIPAddress(httpServletRequest);
            // localhost IP in servlet
            if (sourceIP.equals("0:0:0:0:0:0:0:1") || sourceIP.equals("::1")) {
                sourceIP = "127.0.0.1";
            }

            if (!ConfigManager.ALLOW_WRITE_REQUESTS_FROM.contains(sourceIP)) {
                
                // -- rasdaman community only
                
                //  If user's IP is not allowed, check if request contains valid admin credentials in basic header
                Pair<String, String> resultPair = AuthenticationService.getBasicAuthUsernamePassword(httpServletRequest);
                if (resultPair != null) {
                    String username = resultPair.fst;
                    String password = resultPair.snd;
        
                    RasUtil.checkValidUserCredentials(username, password);

                    // Check if credentials are valid
                    Set<String> roleNames = AuthenticationController.parseRolesFromRascontrol(username);
                    if (!roleNames.contains(AuthenticationController.READ_WRITE_RIGHTS)) {
                        // and user has RW rights
                        String requestRepresentation = this.getRequestPresentationWithEncodedAmpersands(httpServletRequest);
                        throw new PetascopeException(ExceptionCode.AccessDenied, 
                                "The user '" + username + "' specified in the request basic header does not have '" + READ_WRITE_RIGHTS 
                                + "' permissions for executing the write request '" + requestRepresentation + "'");
                    }
                    
                }
                
                // -- rasdaman community only
                
                else {
                    // no basic header exists, request from non-allowed IP
                    String requestRepresentation = this.getRequestPresentationWithEncodedAmpersands(httpServletRequest);
                    throw new PetascopeException(ExceptionCode.AccessDenied, 
                                                 "Write request '" + requestRepresentation + "' is not permitted from IP address '" + sourceIP + "'.");
                }
            }
        }
    }
    
    private String getRequestPresentation(HttpServletRequest httpServletRequest) throws PetascopeException {
        String result = httpServletRequest.getRequestURI();
        Map<String, String[]> kvpParameters = this.parseKvpParametersFromRequest(httpServletRequest);

        if (kvpParameters != null) {
            result = httpServletRequest.getRequestURI() + "?" + this.getRequestRepresentation(kvpParameters);
        }
        
        return result;
    }
    
    /**
     *  e.g a=b&c=d -> a=b&amp;c=d
     * NOTE: & must be escaped as &amp; in XML string to be valid
     */
    public String getRequestPresentationWithEncodedAmpersands(HttpServletRequest httpServletRequest) throws PetascopeException {
        return StringUtil.escapeAmpersands(this.getRequestPresentation(httpServletRequest));
    }
    
    public boolean isPostRequest(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getMethod().toLowerCase().equals("post");
    }
}

