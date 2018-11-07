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
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import petascope.exceptions.WMSException;
import static petascope.core.KVPSymbols.KEY_UPLOADED_FILE_VALUE;
import petascope.exceptions.ExceptionReport;
import petascope.util.ExceptionUtil;
import petascope.util.MIMEUtil;

/**
 * A Controller for all WCS (WCPS, WCS-T), WMS requests
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@RestController
public class PetascopeController extends AbstractController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PetascopeController.class);
    
    @Autowired
    HttpServletRequest httpServletRequest;

    public PetascopeController() {

    }
    
    @RequestMapping(value = OWS, method = RequestMethod.POST)
    protected void handlePost(HttpServletRequest httpServletRequest, @RequestParam(value = KEY_UPLOADED_FILE_VALUE, required = false) MultipartFile uploadedFile) 
            throws IOException, PetascopeException, WCSException, SecoreException, Exception {
        String postBody = this.getPOSTRequestBody(httpServletRequest);        
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        // A file is uploaded e.g: with WCS clipping extension and WKT text is big string in a text file
        String uploadedFilePath = null;
        if (uploadedFile != null) {
            uploadedFilePath = this.storeUploadFileOnServer(uploadedFile);
            kvpParameters.put(KEY_UPLOADED_FILE_VALUE, new String[] {uploadedFilePath});
        }
        this.requestDispatcher(kvpParameters);
    }

    @RequestMapping(value = OWS + "/", method = RequestMethod.POST)
    private void handlePostFallBack(HttpServletRequest httpServletRequest, @RequestParam(value = KEY_UPLOADED_FILE_VALUE, required = false) MultipartFile uploadedFile) 
            throws IOException, PetascopeException, WCSException, SecoreException, Exception {
        this.handlePost(httpServletRequest, uploadedFile);
    }

//    @RequestMapping(value = OWS, method = RequestMethod.POST)
//    protected void handlePost(@RequestBody String postBody) throws Exception {        
//        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
//        this.requestDispatcher(kvpParameters);
//    }
    @RequestMapping(value = OWS, method = RequestMethod.GET)
    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        Map<String, String[]> kvpParameters = this.buildGetRequestKvpParametersMap(httpServletRequest.getQueryString());
        this.requestDispatcher(kvpParameters);
    }
    
    @RequestMapping(value = OWS + "/", method = RequestMethod.GET)
    protected void handleGetFallBack(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        this.handleGet(httpServletRequest);
    }

    /**
     * Depend on the request parameter to handle the request (WCS, WCPS,...)
     *
     * @param kvpParameters
     * @throws WCSException
     * @throws IOException
     * @throws PetascopeException
     * @throws petascope.exceptions.SecoreException
     * @throws petascope.exceptions.WMSException
     */
    @Override
    protected void requestDispatcher(Map<String, String[]> kvpParameters) throws IOException {
        // WCS GetCoverage request can contain multiple duplicate subset parameters (e.g: subset=i(0,10)&subset=k(40,50)         
        try {            
            log.info("Received request: " + this.getRequestRepresentation(kvpParameters));
            long start = System.currentTimeMillis();
            
            // no url for petascope is defined in petascope.properties, only now can have the HTTP request object to set this value
            if (StringUtils.isEmpty(PETASCOPE_ENDPOINT_URL)) {
                // use the requesting URL to Petascope (not always: http://localhost:8080/rasdaman/ows)
                PETASCOPE_ENDPOINT_URL = this.httpServletRequest.getRequestURL().toString();
            }
                
            if (startException != null) {
                throwStartException();
            }

            Response response = null;
            if (kvpParameters.isEmpty()) {
                // a WCS request without any params, so returns WCS-Client            
                byte[] bytes = IOUtils.toString(this.getClass().getResourceAsStream("/" + "public/interface-servlet.html")).getBytes();
                response = new Response(Arrays.asList(bytes), "text/html");
            } else {

                // e.g: WCS, WMS
                String service = kvpParameters.get(KVPSymbols.KEY_SERVICE)[0];
                // e.g: 2.0.1, 2.1.0 (WCS)
                String[] versions = kvpParameters.get(KVPSymbols.KEY_VERSION);
                // e.g: GetCapabilities, DescribeCoverage
                String requestService = kvpParameters.get(KVPSymbols.KEY_REQUEST)[0];

                // Check if any handlers can handle the request
                for (AbstractHandler handler : handlers) {
                    if (handler.canHandle(service, versions, requestService)) {                    
                        response = handler.handle(kvpParameters);
                        break;
                    }
                }
                if (response == null) {
                    throw new PetascopeException(ExceptionCode.NoApplicableCode, "Cannot find the handler for the request.");
                }
            }

            // Dump the response result to client
            this.writeResponseResult(response);
            long end = System.currentTimeMillis();
            long totalTime = end - start;
            log.info("Request processed in '" + String.valueOf(totalTime) + "' ms.");
        } catch(Exception ex) {
            String[] versions = kvpParameters.get(KVPSymbols.KEY_VERSION);
            String version = null;
            if (versions != null) {
                version = versions[0];
            }

            ExceptionUtil.handle(version, ex, httpServletResponse);            
        } finally {
             // Here, the uploaded file (if exists) should be removed
            if (kvpParameters.get(KEY_UPLOADED_FILE_VALUE) != null) {
                String uploadedFilePath = kvpParameters.get(KEY_UPLOADED_FILE_VALUE)[0];
                File file = new File(uploadedFilePath);
                if (file.exists()) {
                    file.delete();
                }
            }            
        }
    }
}

