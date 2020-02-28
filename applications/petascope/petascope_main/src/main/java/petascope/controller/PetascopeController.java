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
import petascope.controller.handler.service.AbstractHandler;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.rasdaman.config.ConfigManager;
import static org.rasdaman.config.ConfigManager.OWS;
import static org.rasdaman.config.ConfigManager.PETASCOPE_ENDPOINT_URL;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import static petascope.core.KVPSymbols.KEY_UPLOADED_FILE_VALUE;
import static petascope.core.KVPSymbols.VALUE_DELETE_COVERAGE;
import static petascope.core.KVPSymbols.VALUE_DELETE_SCALE_LEVEL;
import static petascope.core.KVPSymbols.VALUE_INSERT_COVERAGE;
import static petascope.core.KVPSymbols.VALUE_INSERT_SCALE_LEVEL;
import static petascope.core.KVPSymbols.VALUE_UPDATE_COVERAGE;
import static petascope.core.KVPSymbols.VALUE_WMS_DELETE_STYLE;
import static petascope.core.KVPSymbols.VALUE_WMS_INSERT_STYLE;
import static petascope.core.KVPSymbols.VALUE_WMS_INSERT_WCS_LAYER;
import static petascope.core.KVPSymbols.VALUE_WMS_UPDATE_STYLE;
import static petascope.core.KVPSymbols.VALUE_WMS_UPDATE_WCS_LAYER;
import petascope.util.ExceptionUtil;

/**
 * A Controller for all WCS (WCPS, WCS-T), WMS requests
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@RestController
public class PetascopeController extends AbstractController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PetascopeController.class);
    
    // These write requests will need to check if requesting IP address is in petascope's whitelist.
    private static final List WRITE_REQUESTS = Arrays.asList(new String[]{ VALUE_INSERT_COVERAGE, VALUE_UPDATE_COVERAGE, VALUE_DELETE_COVERAGE, 
                                                    VALUE_INSERT_SCALE_LEVEL, VALUE_DELETE_SCALE_LEVEL,
                                                    VALUE_WMS_INSERT_WCS_LAYER, VALUE_WMS_UPDATE_WCS_LAYER,
                                                    VALUE_WMS_INSERT_STYLE, VALUE_WMS_UPDATE_STYLE,
                                                    VALUE_WMS_DELETE_STYLE
                                                   });


    public PetascopeController() {

    }
    
    @RequestMapping(value = OWS, method = RequestMethod.POST)
    protected void handlePost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                              @RequestParam(value = KEY_UPLOADED_FILE_VALUE, required = false) MultipartFile uploadedMultipartFile) 
            throws IOException, PetascopeException, WCSException, SecoreException, Exception {
        String postBody = this.getPOSTRequestBody(httpServletRequest);        
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        // A file is uploaded e.g: with WCS clipping extension and WKT text is big string in a text file
        String uploadedFilePath = null;
        if (uploadedMultipartFile != null) {
            byte[] bytes = this.getUploadedMultipartFileContent(uploadedMultipartFile);
            uploadedFilePath = this.storeUploadFileOnServer(uploadedMultipartFile.getOriginalFilename(), bytes);
            kvpParameters.put(KEY_UPLOADED_FILE_VALUE, new String[] {uploadedFilePath});
        }
        this.requestDispatcher(httpServletRequest, kvpParameters);
    }

    @RequestMapping(value = OWS + "/", method = RequestMethod.POST)
    private void handlePostFallBack(HttpServletRequest httpServletRequest, @RequestParam(value = KEY_UPLOADED_FILE_VALUE, required = false) MultipartFile uploadedFile) 
            throws IOException, PetascopeException, WCSException, SecoreException, Exception {
        this.handlePost(httpServletRequest, injectedHttpServletResponse, uploadedFile);
    }

    @RequestMapping(value = OWS, method = RequestMethod.GET)
    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        Map<String, String[]> kvpParameters = this.buildGetRequestKvpParametersMap(httpServletRequest.getQueryString());
        this.requestDispatcher(httpServletRequest, kvpParameters);
    }
    
    @RequestMapping(value = OWS + "/", method = RequestMethod.GET)
    protected void handleGetFallBack(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        this.handleGet(httpServletRequest);
    }
    
    /**
     * Check if a source IP address can send a write request to petascope.
     */
    private void validateWriteRequestFromIP(String request, String sourceIP) throws PetascopeException {
        
        if (!ConfigManager.ALLOW_WRITE_REQUESTS_FROM.contains(ConfigManager.PUBLIC_WRITE_REQUESTS_FROM)) {
            // localhost IP in servlet
            if (sourceIP.equals("0:0:0:0:0:0:0:1") || sourceIP.equals("::1")) {
                sourceIP = "127.0.0.1";
            }

            if (this.WRITE_REQUESTS.contains(request)) {
                if (!ConfigManager.ALLOW_WRITE_REQUESTS_FROM.contains(sourceIP)) {
                    throw new PetascopeException(ExceptionCode.AccessDenied, 
                                                "Write request '" + request + "' is not permitted from IP address '" + sourceIP + "'.");
                }
            }
        }
    }
    
    /**
     * Return WSClient page with some extra parameters if needed 
     */
    private Response returnWSClientPage() throws IOException {        
        String wsclientHtmlContent = IOUtils.toString(this.getClass().getResourceAsStream("/" + "public/ows/index.html"));
        
        byte[] bytes = wsclientHtmlContent.getBytes();
        Response response = new Response(Arrays.asList(bytes), "text/html");
        return response;
    }

    /**
     * Depend on the request parameter to handle the request (WCS, WCPS,...)
     *
     */
    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws IOException, PetascopeException, Exception {
        // WCS GetCoverage request can contain multiple duplicate subset parameters (e.g: subset=i(0,10)&subset=k(40,50)     
        
        if (startException != null) {
           throwStartException();
        }
        
        log.info("Received request: " + this.getRequestRepresentation(kvpParameters));
        long start = System.currentTimeMillis();
        
        String service = null;
        String request = null;
        Response response = null;
        boolean requestSuccess = true;
        
        try {            
            // no url for petascope is defined in petascope.properties, only now can have the HTTP request object to set this value
            if (StringUtils.isEmpty(PETASCOPE_ENDPOINT_URL)) {
                // use the requesting URL to Petascope (not always: http://localhost:8080/rasdaman/ows)
                PETASCOPE_ENDPOINT_URL = httpServletRequest.getRequestURL().toString();
            }
                
            if (kvpParameters.isEmpty()) {               
                response = this.returnWSClientPage();
            } else {

                // e.g: WCS, WMS
                service = kvpParameters.get(KVPSymbols.KEY_SERVICE)[0];
                // e.g: 2.0.1, 2.1.0 (WCS)
                String[] versions = kvpParameters.get(KVPSymbols.KEY_VERSION);
                // e.g: GetCapabilities, DescribeCoverage
                String requestService = kvpParameters.get(KVPSymbols.KEY_REQUEST)[0];
                request = requestService;
                
                String sourceIP = httpServletRequest.getHeader("X-FORWARDED-FOR");
                if (sourceIP == null) {
                    // In case petascope is not proxied by apache
                    sourceIP = httpServletRequest.getRemoteAddr();
                }
                                
                this.validateWriteRequestFromIP(request, sourceIP);

                // Check if any handlers can handle the request
                for (AbstractHandler handler : handlers) {
                    if (handler.canHandle(service, versions, requestService)) {                    
                        response = handler.handle(kvpParameters);
                        service = handler.getService();
                        break;
                    }
                }
                if (response == null) {
                    throw new PetascopeException(ExceptionCode.NoApplicableCode, "Cannot find the handler for the request.");
                }
            }

            // Dump the response result to client
            this.writeResponseResult(response);
        } catch(Exception ex) {
            requestSuccess = false;
            String[] versions = kvpParameters.get(KVPSymbols.KEY_VERSION);
            String version = null;
            if (versions != null) {
                version = versions[0];
            }

            ExceptionUtil.handle(version, ex, injectedHttpServletResponse);
        } finally {
             // Here, the uploaded file (if exists) should be removed
            if (kvpParameters.get(KEY_UPLOADED_FILE_VALUE) != null) {
                String uploadedFilePath = kvpParameters.get(KEY_UPLOADED_FILE_VALUE)[0];
                File file = new File(uploadedFilePath);
                if (file.exists()) {
                    file.delete();
                }
            }  
            
            long end = System.currentTimeMillis();
            long totalTime = end - start;
            log.info("Request processed in " + String.valueOf(totalTime) + " ms.");

        }
    }
}

