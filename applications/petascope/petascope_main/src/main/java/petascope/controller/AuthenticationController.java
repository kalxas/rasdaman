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
import static org.rasdaman.config.ConfigManager.OWS_ADMIN;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import petascope.core.Pair;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.MIMEUtil;
import petascope.util.ras.RasUtil;

/**
 * Endpoints to validate credentials from clients.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@RestController
public class AuthenticationController extends AbstractController {
    
    private static final String CHECK_RESULT_TRUE = "true";
    private static final String CHECK_RESULT_FALSE = "false";
    private static final String CHECK_PETASCOPE_ADMIN_USER_CREDENTIALS_REQUEST = "CheckPetascopeAdminUserCredentials";
    
    /**
     * Check if the credentials are valid of petascope admin user to login in wsclient's admin tab.
     */
    @RequestMapping(OWS_ADMIN + "/" + CHECK_PETASCOPE_ADMIN_USER_CREDENTIALS_REQUEST)
    private void handleCheckPetascopeAdminUserCredentials(HttpServletRequest httpServletRequest) throws PetascopeException, IOException {
        Pair<String, String> resultPair = AuthenticationService.getBasicAuthUsernamePassword(httpServletRequest);
        
        if (resultPair == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Missing Authorization basic header from request.");
        }
        
        String username = resultPair.fst;
        String password = resultPair.snd;

        // Valid credentials for petascope admin user
        if (!(ConfigManager.PETASCOPE_ADMIN_USERNAME.equals(username)
            && ConfigManager.PETASCOPE_ADMIN_PASSWORD.equals(password))) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Given credentials for petascope admin user are not valid.");
        }
        Response response = new Response(Arrays.asList("".getBytes()), MIMEUtil.MIME_TEXT);
        this.writeResponseResult(response);
        
    }
    
    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws Exception {
    }

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
    }
    
}
