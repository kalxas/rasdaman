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

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.rasdaman.config.ConfigManager;
import static org.rasdaman.config.ConfigManager.OWS;
import static org.rasdaman.config.ConfigManager.WCPS;
import org.rasdaman.config.VersionManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.PetascopeRuntimeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.wcs2.handlers.kvp.KVPWCSProcessCoverageHandler;
import petascope.wcs2.parsers.request.xml.XMLProcessCoverageParser;

/**
 * Controller for legacy WCPS servlet controller (i.e:
 * http://localhost:8080/rasdaman/ows/wcps) which will allow to POST a WCPS
 * request and get result from the server.
 *
 * <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@RestController
public class WcpsController extends AbstractController {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(WcpsController.class);

    // Handlers for the POST request
    @Autowired
    KVPWCSProcessCoverageHandler kvpProcessCoverageHandler;
    // To parse WCPS in XML syntax to abstract syntax before doing anything
    @Autowired
    XMLProcessCoverageParser xmlProcessCoverageParser;

    @RequestMapping(value = OWS + "/" + WCPS, method = RequestMethod.POST)
    protected void handlePost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        String postBody = this.getPOSTRequestBody(httpServletRequest); 
        // @TODO: it only allows to post WCPS in abstract syntax (i.e: text) now
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        this.requestDispatcher(httpServletRequest, kvpParameters);
    }

    /**
     * return the HTML page with the form to submit a WCPS Post request
     * NOTE: although the string is returned, it actually returns wcps.jsp file.
     * @param httpServletRequest
     * @throws WCSException
     * @throws IOException
     * @throws PetascopeException
     * @throws SecoreException
     * @throws Exception 
     */
    @RequestMapping(value = OWS + "/" + WCPS, method = RequestMethod.GET)      
    public void returnWebPage(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        byte[] bytes = IOUtils.toString(this.getClass().getResourceAsStream("/" + "public/wcps.html")).getBytes();
        Response response = new Response(Arrays.asList(bytes), "text/html");
        
        // Dump the response result to client
        this.writeResponseResult(response);
    }

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws PetascopeException, Exception {
        
        if (startException != null) {
            throwStartException();
        }        
        
        RequestHandlerInterface requestHandlerInterface = () -> {
        
            Response response = null;
            boolean requestSuccess = true;
        
            long startTime = System.currentTimeMillis();
            try {
                // NOTE: this posted WCPS query in requestBody can be WCPS in XML syntax beside the abstract syntax
                if (kvpParameters.get(KVPSymbols.KEY_REQUEST_BODY) != null) {
                    String requestBody = kvpParameters.get(KVPSymbols.KEY_REQUEST_BODY)[0];
                    // It should be WCPS in XML elements
                    String abstractWcpsQuery = xmlProcessCoverageParser.parseWCPSQueryFromXML(requestBody);
                    // Parsed the WCPS query to abstract syntax and handle it as a POST KVP
                    kvpParameters.remove(KVPSymbols.KEY_REQUEST_BODY);
                    kvpParameters.put(KVPSymbols.KEY_QUERY, new String[]{abstractWcpsQuery});
                }        

                response = kvpProcessCoverageHandler.handle(kvpParameters);
                this.writeResponseResult(response);            
            } catch (Exception ex) {
                requestSuccess = false;
                throw new PetascopeRuntimeException(VersionManager.getLatestVersion(KVPSymbols.WCPS_SERVICE), ex);
            }
        };
        
        super.handleRequest(kvpParameters, requestHandlerInterface);
    }

    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        // Do nothing for the get request
    }
}
