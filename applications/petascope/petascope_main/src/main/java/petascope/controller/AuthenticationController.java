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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.controller;

import org.rasdaman.AuthenticationService;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import petascope.core.Pair;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.ListUtil;
import petascope.util.MIMEUtil;
import petascope.util.ras.RasUtil;

/**
 * Endpoints to validate credentials from clients.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@RestController
public class AuthenticationController extends AbstractController {
    
    // Endpoint to check authentication and return the roles of an user to clients
    public static final String ROLE_ADMIN = "admin";
    public static final String LOGIN = "login";
    
    /**
     * The client shows a login form and the credentials are validated by petascope to return the roles of the user.
     */
    @RequestMapping(value = LOGIN)
    private void handleLogin(HttpServletRequest httpServletRequest) throws PetascopeException, IOException, Exception {
        Pair<String, String> resultPair = AuthenticationService.getBasicAuthUsernamePassword(httpServletRequest);
        
        if (resultPair == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Missing Authorization basic header from request.");
        }
        
        String username = resultPair.fst;
        String password = resultPair.snd;
        
        String result = "";  
        
        if (AuthenticationService.isPetascopeAdminUser(username, password)) {
            result = ROLE_ADMIN;
        } else {            
            if (AuthenticationService.isRasdamanAdminUser(username, password)) {
                result = ROLE_ADMIN;
            } else {
                // user does not exist or password is wrong
                throw new PetascopeException(ExceptionCode.InvalidCredentials);
            }
        }
               
        
        Response response = new Response(Arrays.asList(result.getBytes()), MIMEUtil.MIME_TEXT);
        this.writeResponseResult(response);
    }
    
    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws Exception {
    }

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
    }
    
}
