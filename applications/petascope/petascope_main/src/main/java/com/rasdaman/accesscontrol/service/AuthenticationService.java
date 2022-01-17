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
package com.rasdaman.accesscontrol.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.rasdaman.config.ConfigManager;
import petascope.controller.AuthenticationController;
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
            result = new Pair<>(values[0].trim(), values[1].trim());
        }
        
        return result;
    }
    
    /**
     * If credentials don't exist in the request, return pair of rasguest credentials instead
     */
    public static Pair<String, String> getBasicAuthCredentialsOrRasguest(HttpServletRequest httpServletRequest) throws PetascopeException {
        String username = ConfigManager.RASDAMAN_USER;
        String passwd = ConfigManager.RASDAMAN_PASS;
        Pair<String, String> pair = AuthenticationService.getBasicAuthUsernamePassword(httpServletRequest);
        if (pair != null) {
            username = pair.fst;
            passwd = pair.snd;
        }
        
        return new Pair<>(username, passwd);
    }
    
    /**
     * 
     * Set basic authentication header credentials to a URL to request and return input stream
     * 
    **/    
    public static InputStream getInputStreamWithBasicAuthCredentials(URL url, HttpServletRequest httpServletRequest) throws IOException, PetascopeException {
        URLConnection urlConnection;
        urlConnection = url.openConnection();
        
        Pair<String, String> credentialsPair = getBasicAuthUsernamePassword(httpServletRequest);
        
        if (credentialsPair != null) {
            String userpass = credentialsPair.fst + ":" + credentialsPair.snd;
            String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
            urlConnection.setRequestProperty("Authorization", basicAuth);
        }
        
        InputStream inputStream = urlConnection.getInputStream();
        return inputStream;
    }
    
}
