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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
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
import static petascope.core.KVPSymbols.VALUE_ADD_PYRAMID_MEMBER;
import static petascope.core.KVPSymbols.VALUE_CREATE_PYRAMID_MEMBER;
import static petascope.core.KVPSymbols.VALUE_LIST_PYRAMID_MEMBERS;
import static petascope.core.KVPSymbols.VALUE_REMOVE_PYRAMID_MEMBER;
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
    
    // these requests are sent from wcst_import to create pyramids from overviews / scale_levels
    private static final List<String> PYRAMID_REQUESTS = Arrays.asList(new String[] { VALUE_LIST_PYRAMID_MEMBERS, VALUE_ADD_PYRAMID_MEMBER, 
                                                                                      VALUE_CREATE_PYRAMID_MEMBER,
                                                                                      VALUE_REMOVE_PYRAMID_MEMBER });
    private static final List<String> PYRAMID_WRITE_REQUESTS = Arrays.asList(new String[] {
            VALUE_ADD_PYRAMID_MEMBER, VALUE_CREATE_PYRAMID_MEMBER, VALUE_REMOVE_PYRAMID_MEMBER
            });
    
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
            log.info("Received request '" + this.buildRequestQueryString(kvpParameters) + "'.");
            
            String request = this.getValueByKeyAllowNull(kvpParameters, KEY_REQUEST);
            if (!this.PYRAMID_REQUESTS.contains(request)) {
                // request to /rasadmin/admin with parameters, then check if the request has valid credentials
                // for petascope admin user via basic authentication headers
                AuthenticationService.validatePetascopeAdminUser(httpServletRequest);
            } else {
                // Check if it is allowed to send write requests from external ip addresses
                this.validateWriteRequestFromIP(PYRAMID_WRITE_REQUESTS, request, this.getRequesIPAddress());
            }
            
            Response response = null;
            String service = this.getValueByKeyAllowNull(kvpParameters, KEY_SERVICE);
            
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

