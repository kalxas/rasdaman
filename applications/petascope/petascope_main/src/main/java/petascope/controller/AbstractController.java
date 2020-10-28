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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import static org.rasdaman.config.ConfigManager.UPLOADED_FILE_DIR_TMP;
import static org.rasdaman.config.ConfigManager.UPLOAD_FILE_PREFIX;
import org.rasdaman.config.VersionManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import petascope.controller.handler.service.AbstractHandler;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.KEY_VERSION;
import static petascope.core.KVPSymbols.WCS_SERVICE;
import static petascope.core.KVPSymbols.WMS_SERVICE;
import petascope.core.XMLSymbols;
import petascope.core.response.MultipartResponse;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.MIMEUtil;
import petascope.util.StringUtil;
import petascope.util.XMLUtil;

/**
 * Abstract class for controllers
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public abstract class AbstractController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AbstractController.class);
    
    // NOTE: if WCS GetCapabilities does not have version or acceptversions parameter, it uses this default version.
    public static final String WCS_GETCAPABILITIES_DEFAULT_ACCEPTVERSIONS = "2.0.1";
    
    // When petascope cannot start for some reasons, just not throw the exception until it can start the web application and throw exception to user via HTTP request
    public static Exception startException;

    @Autowired
    protected HttpServletResponse injectedHttpServletResponse;
  
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
    
    /**
     * Depend on the requested service then pass the map of keys, values
     * parameters to the corresponding handler
     */
    abstract protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception;

    /**
     * From the GET request query string to map of key / values which is encoded
     */
    public static Map<String, String[]> buildGetRequestKvpParametersMap(String queryString) throws Exception {
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
    protected Map<String, String[]> buildPostRequestKvpParametersMap(String queryString) throws Exception {
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
    private static Map<String, String[]> buildKvpParametersMap(String queryString) throws Exception {
        // It needs to relax the key parameters with case insensitive, e.g: request=DescribeCoverage or REQUEST=DescribeCoverage is ok
        Map<String, String[]> kvpParameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        
        final String QUERY = "query=";

        List<String> wcsVersions = VersionManager.getAllSupportedVersions(KVPSymbols.WCS_SERVICE);
        String[] supportedWCSVersions = wcsVersions.toArray(new String[wcsVersions.size()]);
        
        List<String> wcpsVersions = VersionManager.getAllSupportedVersions(KVPSymbols.WCPS_SERVICE);
        String[] supportedWCPSVersions = wcpsVersions.toArray(new String[wcpsVersions.size()]);
        
        boolean decoded = false; 
        
        if (!(queryString.contains(KVPSymbols.KEY_COVERAGE + "=")
            || queryString.contains(KVPSymbols.KEY_INPUT_COVERAGE + "="))) {
            // NOTE: WCS-T Insert/Update coverage requests posted by wcst_import with character & between key value pairs
            // are not encoded.
            queryString = URLDecoder.decode(queryString, "utf-8");
            decoded = true;
        }

        if (!XMLUtil.isXmlString(queryString.replace(QUERY, ""))) {
            // The request is in KVP GET/POST requests
            kvpParameters = parseKVPParameters(queryString, decoded);
        } else {
            // NOTE: As only WCS 2.0.1 supports POST XML/SOAP
            // @TODO: It can use a simple XML parser to get the version element to check it correctly.            
            kvpParameters.put(KVPSymbols.KEY_VERSION, supportedWCSVersions);

            // NOTE: Try to parse the query string in POST/SOAP XML as it have to be KVP and data url-encoded
            // e.g: query=<?xml.....> or without query= (in case of POST with curl --data, this has problem with "+" as it is not encoded as %2B, so just for backwards compatibility            
            String requestBody = queryString;
            if (requestBody.startsWith(QUERY)) {
                // Only get the request content in XML
                requestBody = requestBody.split(QUERY)[1];
            }
            
            // The request is in POST XML or SOAP requests with XML syntax
            String root = XMLUtil.getRootElementName(requestBody);

            // NOTE: current only WCS supports POST XML, SOAP, add the service name so WCS handlers can handle
            kvpParameters.put(KVPSymbols.KEY_SERVICE, new String[] {KVPSymbols.WCS_SERVICE});
            if (root.equals(XMLSymbols.LABEL_ENVELOPE)) {
                // It is a SOAP request (WCPS 1.0) or WCS, so extract the query content
                requestBody = XMLUtil.extractWcsRequest(queryString);
                // NOTE: response also needed to be add in SOAP body, not like XML which has same result as KVP
                kvpParameters.put(KVPSymbols.KEY_REQUEST, new String[] {KVPSymbols.VALUE_REQUEST_WCS_SOAP});
            } else {
                kvpParameters.put(KVPSymbols.KEY_REQUEST, new String[] {KVPSymbols.VALUE_REQUEST_WCS_XML});
            }

            kvpParameters.put(KVPSymbols.KEY_REQUEST_BODY, new String[] {requestBody});
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

        // e.g: Rasql servlet does not contains these requirement parameters
        if (kvpParameters.get(KVPSymbols.KEY_SERVICE) != null) {
            String service = getValueByKey(kvpParameters, KVPSymbols.KEY_SERVICE);
            String request = getValueByKeyAllowNull(kvpParameters, KVPSymbols.KEY_REQUEST);
            String versions[] = getValuesByKeyAllowNull(kvpParameters, KVPSymbols.KEY_VERSION);

            // NOTE: WMS allows version is null, so just use the latest WMS version
            if (service.equals(KVPSymbols.WMS_SERVICE) && versions == null) {
                log.debug("WMS received request without version parameter, use the default version: " + VersionManager.getLatestVersion(WMS_SERVICE));
                kvpParameters.put(KVPSymbols.KEY_VERSION, new String[] {VersionManager.getLatestVersion(WMS_SERVICE)});
            } else if (service.equals(KVPSymbols.WCS_SERVICE) && request.equals(KVPSymbols.VALUE_GET_CAPABILITIES)) {
                // NOTE: backwards compatibility for old clients which send WCS GetCapabilities with version parameter
                if (versions != null) {
                    log.warn("Using '" + KEY_VERSION + "' in a GetCapabilities request is invalid.");
                } else {                     
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
    protected String storeUploadFileOnServer(String uploadedFileName, byte[] bytes) throws PetascopeException {

        // Check if temp folder exist first
        File folderPath = new File(UPLOADED_FILE_DIR_TMP);
        if (!folderPath.exists()) {
            folderPath.mkdir();
        }
        String fileName = StringUtil.addDateTimeSuffix(UPLOAD_FILE_PREFIX + uploadedFileName);
        String filePath = UPLOADED_FILE_DIR_TMP + "/" + fileName;
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
    protected String getPOSTRequestBody(HttpServletRequest httpServletRequest) throws IOException {
        String requestBody = "";
        if (httpServletRequest.getQueryString() != null) {
            // case 1: POST a file with a KVP request to server (e.g: rasql post file to server to import)    
            // or POST a KVP request (as same as GET a KVP request)
            requestBody = httpServletRequest.getQueryString();
        }
        
        if (httpServletRequest.getReader() != null) {
            // case 2: a POST XML, SOAP body
            if (requestBody.isEmpty()) {
                requestBody = IOUtils.toString(httpServletRequest.getReader());
            } else {
                requestBody += "&" + IOUtils.toString(httpServletRequest.getReader());
            }
        }
        return requestBody;
    }

    /**
     * Write the response as text or binary to the requesting client.
     */
    protected void writeResponseResult(Response response) throws IOException, PetascopeException {
        // This one is needed as normally it write the result with HTTP:200, 
        // but for SOAP case when error message is enclosed in envelope, it can return HTTP:400, 404
        injectedHttpServletResponse.setStatus(response.getHTTPCode());
        addFileNameToHeader(response);
        OutputStream os = injectedHttpServletResponse.getOutputStream();
        try {
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
        } finally {
            IOUtils.closeQuietly(os);
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
    
    protected void addFileNameToHeader(Response response) throws IOException, PetascopeException {
        if (response.getCoverageID().equals(Response.DEFAULT_COVERAGE_ID))
            return;
        
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
        for (byte[] data : response.getDatas()) {
            multi.startPart(mimeType);
            IOUtils.write(data, os);
            multi.endPart();
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
    protected String getRequestRepresentation(Map<String, String[]> kvpParametersMap) {
        String request = "";
        for (Map.Entry<String, String[]> entry : kvpParametersMap.entrySet()) {
            for (String value : entry.getValue()) {
                
                // As they contain long GML text, no need to show
                if (entry.getKey().equalsIgnoreCase(KVPSymbols.KEY_COVERAGE)
                  || entry.getKey().equalsIgnoreCase(KVPSymbols.KEY_INPUT_COVERAGE)) {
                    value = value.substring(0, 50) + "...";
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
     * Parse the KVP parameters to map of keys and values
     */
    private static Map<String, String[]> parseKVPParameters(String queryString, boolean decoded) throws UnsupportedEncodingException {
        Map<String, String[]> parametersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (!queryString.equals("")) {
            
            String[] keyValues = queryString.split("&");
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
                    value = URLDecoder.decode(value, "utf-8");
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
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                    "Cannot find value from KVP parameters map for key: " + key + ".");
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
                    "Cannot find value from KVP parameters map for key: " + key + ".");
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
}

