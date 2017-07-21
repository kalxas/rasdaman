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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.rasdaman.config.ConfigManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import petascope.controller.handler.service.AbstractHandler;
import petascope.core.KVPSymbols;
import petascope.core.XMLSymbols;
import petascope.core.response.MultipartResponse;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.exceptions.WMSException;
import petascope.util.MIMEUtil;
import petascope.util.XMLUtil;

/**
 * Abstract class for controllers
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public abstract class AbstractController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AbstractController.class);

    @Autowired
    protected HttpServletResponse httpServletResponse;

    @Resource
    // Spring finds all the subclass of AbstractHandler and injects to the list
    List<AbstractHandler> handlers;

    /**
     * Handler GET request
     *
     * @param httpServletRequest
     * @throws WCSException
     * @throws IOException
     * @throws PetascopeException
     * @throws SecoreException
     * @throws Exception
     */
    abstract protected void handleGet(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception;

    /**
     * From the GET request query string to map of keys, values which is encoded
     * @param queryString
     * @return
     * @throws Exception 
     */
    protected Map<String, String[]> buildGetRequestKvpParametersMap(String queryString) throws Exception {
        if (queryString == null) {
            queryString = "";
        }
        queryString = queryString.trim();

        // Query is already encoded from Browser or client, but the "+" is not encoded, so encode it correctly.
        // NOTE: it does not matter if space character is encoded to "+" or "%2B", just replace it when a query string is encoded from client).
        queryString = queryString.replaceAll("\\+", "%2B");

        return this.buildKvpParametersMap(queryString);
    }
    
    /**
     * From the POST request query string to a map of keys, values which is
     * encoded or raw.
     *
     * @param queryString
     * @return
     * @throws Exception
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
     *
     * @param queryString
     * @return
     * @throws Exception
     */
    private Map<String, String[]> buildKvpParametersMap(String queryString) throws Exception {
        // It needs to relax the key parameters with case insensitive, e.g: request=DescribeCoverage or REQUEST=DescribeCoverage is ok
        Map<String, String[]> kvpParameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        // then can decode the query string
        queryString = URLDecoder.decode(queryString, "utf-8");

        if (!XMLUtil.isXmlString(queryString)) {
            // The request is in KVP GET/POST requests
            kvpParameters = parseKVPParameters(queryString);
        } else {
            // NOTE: As only WCS 2.0.1 supports POST XML/SOAP
            // @TODO: It can use a simple XML parser to get the version element to check it correctly.
            kvpParameters.put(KVPSymbols.KEY_VERSION, new String[]{ConfigManager.WCS_VERSIONS});

            // NOTE: Try to parse the query string in POST/SOAP XML as it have to be KVP and data url-encoded
            // e.g: query=<?xml.....> or without query= (in case of POST with curl --data, this has problem with "+" as it is not encoded as %2B, so just for backwards compatibility            
            String requestBody = queryString;
            if (requestBody.startsWith("query=")) {
                // Only get the request content in XML
                requestBody = requestBody.split("query=")[1];
            }
            // The request is in POST XML or SOAP requests with XML syntax
            String root = XMLUtil.getRootElementName(requestBody);

            // NOTE: current only WCS supports POST XML, SOAP, add the service name so WCS handlers can handle
            kvpParameters.put(KVPSymbols.KEY_SERVICE, new String[]{KVPSymbols.WCS_SERVICE});
            if (root.equals(XMLSymbols.LABEL_ENVELOPE)) {
                // It is a SOAP request (WCPS 1.0) or WCS, so extract the query content
                requestBody = XMLUtil.extractWcsRequest(queryString);
                // NOTE: response also needed to be add in SOAP body, not like XML which has same result as KVP
                kvpParameters.put(KVPSymbols.KEY_REQUEST, new String[]{KVPSymbols.VALUE_REQUEST_WCS_SOAP});
            } else {
                kvpParameters.put(KVPSymbols.KEY_REQUEST, new String[]{KVPSymbols.VALUE_REQUEST_WCS_XML});
            }

            kvpParameters.put(KVPSymbols.KEY_REQUEST_BODY, new String[]{requestBody});
        }

        // Validate the parsed KVP maps for all requirement parameters (only when it has at least 1 parameter as an empty request will return WCS-Client)
        if (kvpParameters.isEmpty()) {
            return kvpParameters;
        }

        // backwards compatibility for WCPS ows?query="" is ok to handle
        if (kvpParameters.containsKey(KVPSymbols.KEY_QUERY)) {
            kvpParameters.put(KVPSymbols.KEY_SERVICE, new String[]{KVPSymbols.WCS_SERVICE});
            kvpParameters.put(KVPSymbols.KEY_VERSION, new String[]{ConfigManager.WCS_VERSIONS});
            kvpParameters.put(KVPSymbols.KEY_REQUEST, new String[]{KVPSymbols.VALUE_PROCESS_COVERAGES});
        }

        // e.g: Rasql servlet does not contains these requirement parameters
        if (kvpParameters.get(KVPSymbols.KEY_SERVICE) != null) {
            String service = getValuesByKey(kvpParameters, KVPSymbols.KEY_SERVICE)[0];
            String request = getValuesByKey(kvpParameters, KVPSymbols.KEY_REQUEST)[0];
            String versions[] = kvpParameters.get(KVPSymbols.KEY_VERSION);

            // NOTE: WMS allows version is null, so just use the latest WMS version
            if (service.equals(KVPSymbols.WMS_SERVICE) && versions == null) {
                log.debug("WMS received request without version parameter, use the default version: " + ConfigManager.WMS_VERSIONS);
                kvpParameters.put(KVPSymbols.KEY_VERSION, new String[]{ConfigManager.WMS_VERSIONS});
            } else if (service.equals(KVPSymbols.WCS_SERVICE) && request.equals(KVPSymbols.VALUE_GET_CAPABILITIES)) {
                // NOTE: backwards compatibility for old clients which send WCS GetCapabilities with version parameter
                if (versions != null) {
                    log.warn("Using VERSION in a GetCapabilities request is invalid.");
                } else {
                    // It should use AcceptVersions for WCS GetCapabilities
                    String[] version = getValuesByKey(kvpParameters, KVPSymbols.KEY_ACCEPTVERSIONS);
                    kvpParameters.put(KVPSymbols.KEY_VERSION, version);
                }
            }

            validateRequiredParameters(kvpParameters);
        }

        return kvpParameters;
    }

    /**
     * Validate all the strict requirement parameters.
     *
     * @param kvpParameters
     * @throws PetascopeException
     */
    private void validateRequiredParameters(Map<String, String[]> kvpParameters) throws PetascopeException {

        // Do some check requirements
        String[] service = kvpParameters.get(KVPSymbols.KEY_SERVICE);
        String[] version = kvpParameters.get(KVPSymbols.KEY_VERSION);
        String[] request = kvpParameters.get(KVPSymbols.KEY_REQUEST);

        if (service == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Missing required parameter: " + KVPSymbols.KEY_SERVICE + ".");
        } else if (version == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Missing required parameter: " + KVPSymbols.KEY_VERSION + ".");
        } else if (request == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Missing required parameter: " + KVPSymbols.KEY_REQUEST + ".");
        }
    }

    /**
     * Depend on the requested service then pass the map of keys, values
     * parameters to the corresponding handler
     *
     * @param kvpParameters
     * @throws IOException
     * @throws PetascopeException
     * @throws petascope.exceptions.SecoreException
     * @throws petascope.exceptions.WMSException
     */
    abstract protected void requestDispatcher(Map<String, String[]> kvpParameters) throws IOException, PetascopeException, SecoreException, WMSException;

    /**
     * Parse the POST request body from the input HTTP request
     *
     * @param httpServletRequest
     * @return
     * @throws java.io.IOException
     */
    protected String getPOSTRequestBody(HttpServletRequest httpServletRequest) throws IOException {
        String requestBody;
        if (httpServletRequest.getQueryString() != null) {
            // case 1: POST a file with a KVP request to server (e.g: rasql post file to server to import)    
            // or POST a KVP request (as same as GET a KVP request)
            requestBody = httpServletRequest.getQueryString();
        } else {
            // case 2: a POST XML, SOAP body
            requestBody = IOUtils.toString(httpServletRequest.getReader());
        }

        return requestBody;
    }

    /**
     * Write the response as text or binary to the requesting client.
     *
     * @param response
     * @throws java.io.IOException
     * @throws petascope.exceptions.PetascopeException
     */
    protected void writeResponseResult(Response response) throws IOException, PetascopeException {
        OutputStream outputStream = httpServletResponse.getOutputStream();
        // This one is needed as normally it write the result with HTTP:200, but for SOAP case when error message is enclosed in envelope, it can return HTTP:400, 404
        httpServletResponse.setStatus(response.getHTTPCode());

        String mimeType = response.getFormatType();
        if (!response.getCoverageID().equals(Response.DEFAULT_COVERAGE_ID)) {
            String fileName = response.getCoverageID() + "." + MIMEUtil.getFileNameExtension(mimeType);
            httpServletResponse.setHeader("File-name", fileName);
            // If multipart then must download file from Browser
            if (response.getDatas() != null && response.getDatas().size() > 1) {
                httpServletResponse.setHeader("Content-disposition", "attachment; filename=" + fileName);
            } else if (response.getCoverageID() != null) {
                // If a GetCoverage request or a processCoverage request with coverageId result, then download the result with file name
                // e.g: query=for c in (test_mr) return encode(c, "tiff"), then download file: test_mr.tiff
                // NOTE: Content-Disposition: attachment; will download file in WebBrowser instead of trying to display (GML/PNG/JPEG) type.
                httpServletResponse.setHeader("Content-disposition", "inline; filename=" + fileName);                
            }
        }

        // NOTE: to display application/gml+xml in browser, change in HTTP response to text/xml
        if (mimeType.equals(MIMEUtil.MIME_GML)) {
            mimeType = MIMEUtil.MIME_XML;
        }

        try {
            if (response.getDatas() == null) {
                // In wcst_import like deleteCoverage, it just returns empty string as a success
                IOUtils.write("", outputStream);
            } else {
                // The data returns can contain multipart (i.e: gml, binary or binary, binary for WCS or WCPS)
                if (response.getDatas().size() > 1) {
                    MultipartResponse multi = new MultipartResponse(httpServletResponse);
                    for (byte[] data : response.getDatas()) {
                        multi.startPart(mimeType);
                        IOUtils.write(data, outputStream);
                        multi.endPart();
                    }
                    multi.finish();
                } else {
                    // Not multipart, so response should contain either 1 binary data (e.g: png, tiff)
                    // or 1 xml data (e.g: GetCapabilities, DescribeCoverage, getCoverage in application/gml+xml)
                    httpServletResponse.setContentType(mimeType);
                    IOUtils.write(response.getDatas().get(0), outputStream);
                }
            }
        } finally {
            IOUtils.closeQuietly(outputStream);
            // NOTE: it must release the data occupied by byte[] so doing like this will release memory right after the response is done.            
            response = null;
            System.gc();
        }
    }

    /**
     * Log the request GET/POST from kvpParameters map
     *
     * @param kvpParametersMap
     * @return
     */
    protected String getRequestRepresentation(Map<String, String[]> kvpParametersMap) {
        String request = "";
        for (Map.Entry<String, String[]> entry : kvpParametersMap.entrySet()) {
            for (String value : entry.getValue()) {
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
     *
     * @param queryString
     * @return
     */
    private Map<String, String[]> parseKVPParameters(String queryString) {
        Map<String, String[]> parametersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (!queryString.equals("")) {
            String[] keyValues = queryString.split("&");
            for (String keyValue : keyValues) {
                // No parse keyValue when it empty such as: &service=DescribeCoverage&coverageId=test_mr, then the first & is empty string
                if (keyValue.isEmpty()) {
                    continue;
                }
                String key = null, value = null;
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

                if (parametersMap.get(key) == null) {
                    parametersMap.put(key, new String[]{value});
                } else {
                    String[] values = parametersMap.get(key);
                    String[] newValues = new String[values.length + 1];
                    for (int i = 0; i < values.length; i++) {
                        newValues[i] = values[i];
                    }
                    newValues[values.length] = value;

                    parametersMap.put(key, newValues);
                }
            }
        }

        return parametersMap;
    }

    /**
     * Return the single value of a key in KVP parameters
     *
     * @param kvpParameters
     * @param key
     */
    private String[] getValuesByKey(Map<String, String[]> kvpParameters, String key) throws PetascopeException {
        String[] values = kvpParameters.get(key);
        if (values == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Cannot find value from KVP parameters map for key parameter, given: " + key + ".");
        }

        return values;
    }
}

