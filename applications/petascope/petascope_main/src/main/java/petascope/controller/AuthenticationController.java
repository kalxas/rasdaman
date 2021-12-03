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

import com.rasdaman.accesscontrol.service.AuthenticationService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.System.in;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.rasnet.util.DigestUtils;
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
    
    public static final String LOGIN = "login";
    
    public static final String READ_WRITE_RIGHTS = "RW";
    
    /**
     * Check the credentials provided by the user. If the credentials are valid, return the list of roles for the requesting user.
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
        
        if (RasUtil.checkValidUserCredentials(username, password)) {
            // NOTE: with rasdaman community, if user has permissions CASIRW permissions in rascontrol, then he is admin user
            // export RASLOGIN=rasadmin:d293a15562d3e70b6fdc5ee452eaed40 && rascontrol -q -e -x list user -rights
            
            Set<String> roleNames = this.parseRolesFromRascontrol(username);
            
            if (roleNames != null) {
                // Return the list of rolenames for this user
                // e.g: admin,info,readwrite,PRIV_TYPE_MGMT,PRIV_COLLECTION_MGMT,PRIV_TRIGGER_MGMT,PRIV_USER_MGMT,PRIV_OWS_ADMIN,...
                result = ListUtil.join(new ArrayList(roleNames), ",");
            }
        } else {
            // user does not exist or password is wrong
            throw new PetascopeException(ExceptionCode.InvalidCredentials);
        }
        
        Response response = new Response(Arrays.asList(result.getBytes()), MIMEUtil.MIME_TEXT);
        this.writeResponseResult(response);
    }
    
    /**
     * Return the list of roles for the requesting user via rascontrol
     * @TODO: this can be done faster and better with protobuf/grpc
     */
    public static Set<String> parseRolesFromRascontrol(String username) throws IOException {
        // export RASLOGIN=rasadmin:d293a15562d3e70b6fdc5ee452eaed40 && rascontrol -q -e -x list user -rights
        Runtime runtime = Runtime.getRuntime();
        
        Set<String> roleNames = null;
        
        String loginEnv = ConfigManager.RASDAMAN_ADMIN_USER + ":" + DigestUtils.MD5(ConfigManager.RASDAMAN_ADMIN_PASS);
        String[] envp = new String[] {"RASLOGIN=" + loginEnv};
        Process process = runtime.exec("rascontrol -q -e -x list user -rights", envp);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        Pattern p = Pattern.compile("\\[(.*?)\\]");
        
        while ((line = stdInput.readLine()) != null) {
            if (line.contains("[")) {
                String[] tmps = line.trim().split(" ");
                String usernameTmp = tmps[1].trim();
                
                if (usernameTmp.equals(username)) {
                    
                    Matcher m = p.matcher(line);
                    String rights = "";
                    while(m.find()) {
                        rights += m.group(1);
                    }
                    
                    if (rights.contains(".")) {
                        // Here user has a missing right, e.g [R.] so he is not admin
                        break; 
                    } else if (rights.contains("RW")) {
                        // e.g rasadmin with rights [AISC] -[RW]
                        
                        // then, the user is admin and it has these mapping roles - NOTE: it is used *internaly* only for WSClient
                        roleNames = new LinkedHashSet<>(Arrays.asList(READ_WRITE_RIGHTS));
                    }
                    
                    break;
                }
            }
        }
        
        return roleNames;
    }
    
    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws Exception {
    }

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
    }
    
}
