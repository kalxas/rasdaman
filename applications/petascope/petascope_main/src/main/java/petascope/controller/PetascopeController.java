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
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import com.rasdaman.accesscontrol.service.AuthenticationService;
import org.rasdaman.config.ConfigManager;
import static org.rasdaman.config.ConfigManager.INSPIRE_COMMON_URL;
import static org.rasdaman.config.ConfigManager.OWS;
import static org.rasdaman.config.ConfigManager.PETASCOPE_ENDPOINT_URL;
import static org.rasdaman.config.ConfigManager.UPLOADED_FILE_DIR_TMP;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeRuntimeException;
import petascope.util.ExceptionUtil;
import petascope.util.StringUtil;

/**
 * A Controller for all WCS (WCPS, WCS-T), WMS requests
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@RestController
public class PetascopeController extends AbstractController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PetascopeController.class);


    public PetascopeController() {

    }
    
    @RequestMapping(value = OWS, method = RequestMethod.POST)
    protected void handlePost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) 
            throws IOException, PetascopeException, WCSException, SecoreException, Exception {
        
        super.handlePost(httpServletRequest);
    }

    @RequestMapping(value = OWS + "/", method = RequestMethod.POST)
    private void handlePostFallBack(HttpServletRequest httpServletRequest) 
            throws IOException, PetascopeException, WCSException, SecoreException, Exception {
        this.handlePost(httpServletRequest, injectedHttpServletResponse);
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
     * Return WSClient page with some extra parameters if needed 
     */
    private Response returnWSClientPage() throws IOException {        
        String wsclientHtmlContent = IOUtils.toString(this.getClass().getResourceAsStream("/public/ows/index.html"));
        byte[] bytes = wsclientHtmlContent.getBytes();
        Response response = new Response(Arrays.asList(bytes), "text/html");
        return response;
    }

    /**
     * Depend on the request parameter to handle the request (WCS, WCPS,...)
     *
     */
    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
        if (startException != null) {
            throwStartException();
        }
        
        if (kvpParameters.isEmpty()) {               
            Response response = this.returnWSClientPage();
            // Dump the response result to client
            this.writeResponseResult(response);
            return;
        }
        
        RequestHandlerInterface requestHandlerInterface = () -> {

            String service = null;
            String request = null;
            Response response = null;
            boolean requestSuccess = true;
            long startTime = System.currentTimeMillis();
            String version = null;

            try {            
                // no url for petascope is defined in petascope.properties, only now can have the HTTP request object to set this value
                if (StringUtils.isEmpty(PETASCOPE_ENDPOINT_URL)) {
                    // use the requesting URL to Petascope (not always: http://localhost:8080/rasdaman/ows)
                    
                    PETASCOPE_ENDPOINT_URL = httpServletRequest.getRequestURL().toString();
                    String protocol = httpServletRequest.getHeader("X-Forwarded-Proto");
                    
                    if (protocol != null) {
                        // e.g. in case using https in apache2 proxy for http on local tomcat
                        String[] tmps = PETASCOPE_ENDPOINT_URL.split("://");
                        PETASCOPE_ENDPOINT_URL = protocol + "://" + tmps[1];
                    }
                }

                if (INSPIRE_COMMON_URL.isEmpty()) {
                    INSPIRE_COMMON_URL = PETASCOPE_ENDPOINT_URL;
                }

                // e.g: WCS, WMS
                service = getValueByKey(kvpParameters, KVPSymbols.KEY_SERVICE);
                // e.g: 2.0.1, 2.1.0 (WCS)
                String[] versions = getValuesByKey(kvpParameters, KVPSymbols.KEY_VERSION);
                // e.g: GetCapabilities, DescribeCoverage
                String requestService = getValueByKey(kvpParameters, KVPSymbols.KEY_REQUEST);
                request = requestService;

                // Check if any handlers can handle the request
                for (AbstractHandler handler : handlers) {
                    if (handler.canHandle(service, versions, requestService)) {                    
                        response = handler.handle(kvpParameters);
                        service = handler.getService();
                        break;
                    }
                }
                if (response == null) {
                    throw new PetascopeException(ExceptionCode.NoApplicableCode, 
                                                "Cannot find the handler for the request '" + StringUtil.escapeAmpersands(this.getRequestRepresentation(kvpParameters)) + "'.");
                }

                version = this.getValueByKey(kvpParameters, KVPSymbols.KEY_VERSION);

                // Dump the response result to client
                this.writeResponseResult(response);
            } catch (Exception ex) {
                requestSuccess = false;
                throw new PetascopeRuntimeException(version, ex);
            } finally {

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;

            }
        };
        
        super.handleRequest(kvpParameters, requestHandlerInterface);
    }
}

