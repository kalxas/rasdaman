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
package org.rasdaman;

import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.rasdaman.config.ConfigManager;
import petascope.core.Pair;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 * Utility for authentication handlers
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class AuthenticationService {
    
    /**
     * Check if the request is requesting under admin user with basic authentication headers
     */
    public static boolean isAdminUser(HttpServletRequest httpServletRequest) throws PetascopeException {
        Pair<String, String> credentialsPair = getBasicAuthUsernamePassword(httpServletRequest);
        
        if (credentialsPair != null) {
            // In case the user requesting with credentials
            return isPetascopeAdminUser(credentialsPair.fst, credentialsPair.snd)
                  || isRasdamanAdminUser(credentialsPair.fst, credentialsPair.snd);
        }
        
        return false;
    }
    
    /**
     * Parse header to get username and password encoded in Base64 for basic authentication header
     */
    public static Pair<String, String> getBasicAuthUsernamePassword(HttpServletRequest httpServletRequest) throws PetascopeException {
        
        final String authorization = httpServletRequest.getHeader("Authorization");
        Pair<String, String> result = null;
        
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            // Example from curl (encoded username:password in Base64)
            // Authorization:Basic dXNlcm5hbWU6cGFzc3dvcmQ=
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.decodeBase64(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            if (!credentials.contains(":")) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, "Basic authentication header credentials must be encoded in Base64 string with format username:password");
            }
            // decoded username:password
            final String[] values = credentials.split(":", 2);
            result = new Pair<>(values[0], values[1]);
        }
        
        return result;
    }
    
    /**
     * Check if user is petascope admin user
     */
    public static void validatePetascopeAdminUser(HttpServletRequest httpServletRequest) throws PetascopeException {
        Pair<String, String> credentialsPair = getBasicAuthUsernamePassword(httpServletRequest);
        if (credentialsPair == null) {
            throw new PetascopeException(ExceptionCode.AccessDenied, "Missing credentials for admin user in basic authentication headers");
        } else if (!isAdminUser(httpServletRequest)) {
            throw new PetascopeException(ExceptionCode.AccessDenied, "Invalid credentials for admin user");
        }
    }
    
        
    /**
     * Check if given credentials are valid for petascope admin user     
     */
    public static boolean isPetascopeAdminUser(String username, String password) {
        return ConfigManager.PETASCOPE_ADMIN_USERNAME.equals(username) 
            && ConfigManager.PETASCOPE_ADMIN_PASSWORD.equals(password);
    }
    
    /**
     * Check if given credentials are valid for rasdaman admin user     
     */
    public static boolean isRasdamanAdminUser(String username, String password) {
        return ConfigManager.RASDAMAN_ADMIN_USER.equals(username) 
            && ConfigManager.RASDAMAN_ADMIN_PASS.equals(password);
    }
}
