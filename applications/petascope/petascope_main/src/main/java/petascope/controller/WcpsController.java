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
import java.net.URLDecoder;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import static org.rasdaman.config.ConfigManager.OWS;
import static org.rasdaman.config.ConfigManager.WCPS;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.exceptions.WMSException;
import petascope.wcs2.handlers.kvp.KVPWCSProcessCoverageHandler;

/**
 * Controller for legacy WCPS servlet controller (i.e:
 * http://localhost:8080/rasdaman/ows/wcps) which will allow to POST a WCPS
 * request and get result from the server.
 *
 * <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Controller
public class WcpsController extends AbstractController {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(WcpsController.class);

    // Handlers for the POST request
    @Autowired
    KVPWCSProcessCoverageHandler kvpProcessCoverageHandler;

    @RequestMapping(value = OWS + "/" + WCPS, method = RequestMethod.POST)
    protected void handlePost(@RequestBody String postBody) throws Exception {        
        // @TODO: it only allows to post WCPS in abstract syntax (i.e: text) now
        postBody = URLDecoder.decode(postBody, "utf-8");
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        this.requestDispatcher(kvpParameters);
    }

    /**
     * return the HTML page with the form to submit a WCPS Post request
     * NOTE: although the string is returned, it actually returns wcps.jsp file.
     * @param httpServletRequest
     * @return
     * @throws WCSException
     * @throws IOException
     * @throws PetascopeException
     * @throws SecoreException
     * @throws Exception 
     */
    @RequestMapping(value = OWS + "/" + WCPS, method = RequestMethod.GET)      
    public String returnWebPage(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        return "wcps";
    }

    @Override
    protected void requestDispatcher(Map<String, String[]> kvpParameters) throws IOException, PetascopeException, WCSException, SecoreException, WMSException {
        log.debug("Received request: " + this.getRequestRepresentation(kvpParameters));
        Response response = kvpProcessCoverageHandler.handle(kvpParameters);
        this.writeResponseResult(response);
    }

    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        // Do nothing for the get request
    }
}
