/*
* Copyright (C) 2007-2020 Rasdaman GmbH
 */
// -- Begin Rasdaman Enterprise
package petascope.controller;

import com.rasdaman.admin.service.AbstractAdminService;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.rasdaman.AuthenticationService;
import static org.rasdaman.config.ConfigManager.ADMIN;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import static petascope.core.KVPSymbols.KEY_REQUEST;
import static petascope.core.KVPSymbols.KEY_SERVICE;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import static petascope.util.MIMEUtil.MIME_HTML;

/**
 * End point to receive requests for admin features in petascope
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@RestController
public class AdminController extends AbstractController {
    
    private static org.slf4j.Logger log = LoggerFactory.getLogger(AdminController.class);
    
    @Resource
    // Spring finds all the subclass of AbstractHandler and injects to the list
    List<AbstractAdminService> handlers;
    
    @Override
    @RequestMapping(value = ADMIN, method = RequestMethod.GET)
    protected void handleGet(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, String[]> kvpParameters = this.buildGetRequestKvpParametersMap(httpServletRequest.getQueryString());
        this.requestDispatcher(httpServletRequest, kvpParameters);
    }
    
    @RequestMapping(value = ADMIN, method = RequestMethod.POST)
    protected void handlePost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) 
            throws IOException, PetascopeException, WCSException, SecoreException, Exception {
        String postBody = this.getPOSTRequestBody(httpServletRequest);        
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        
        this.requestDispatcher(httpServletRequest, kvpParameters);
    }

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
               
        if (kvpParameters.isEmpty()) {
            // request to /rasdaman/admin without any parameter, then return petascope admin client
            this.returnAdminHomePage();
        } else {
            Response response = null;
            String service = this.getValueByKeyAllowNull(kvpParameters, KEY_SERVICE);
            String request = this.getValueByKeyAllowNull(kvpParameters, KEY_REQUEST);
            
            log.info("Received request '" + httpServletRequest.getQueryString() + "'.");
            
            // Check if any handlers can handle the request
            for (AbstractAdminService handler : handlers) {
                if (handler.canHandle(service, request)) {
                    response = handler.handle(httpServletRequest, kvpParameters);
                    break;
                }
            }
            
            if (response == null) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, "Cannot find the handler for the request with service: '" + service + "'.");
            }
            
            this.writeResponseResult(response);
        }       
    }
    
    /**
     * Just return the admin home page.
     */
    private void returnAdminHomePage() throws Exception {
        byte[] bytes = IOUtils.toString(this.getClass().getResourceAsStream("/public/interface-admin-servlet.html")).getBytes();
        Response response = new Response(Arrays.asList(bytes), MIME_HTML);
        this.writeResponseResult(response);
    }   
}

// -- End Rasdaman Enterprise
