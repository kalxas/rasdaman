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

import petascope.controller.handler.service.AbstractHandler;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import static org.rasdaman.config.ConfigManager.OWS;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import petascope.exceptions.WMSException;

/**
 * A Controller for all WCS (WCPS, WCS-T), WMS requests
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@RestController
public class PetascopeController extends AbstractController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PetascopeController.class);

    public PetascopeController() {

    }

    @RequestMapping(value = OWS, method = RequestMethod.POST)
    protected void handlePost(HttpServletRequest httpServletRequest) throws IOException, PetascopeException, WCSException, SecoreException, Exception {
        String postBody = this.getPOSTRequestBody(httpServletRequest);        
        Map<String, String[]> kvpParameters = this.buildRequestKvpParametersMap(postBody);
        this.requestDispatcher(kvpParameters);
    }

//    @RequestMapping(value = OWS, method = RequestMethod.POST)
//    protected void handlePost(@RequestBody String postBody) throws Exception {        
//        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
//        this.requestDispatcher(kvpParameters);
//    }
    @RequestMapping(value = OWS, method = RequestMethod.GET)
    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        Map<String, String[]> kvpParameters = this.buildRequestKvpParametersMap(httpServletRequest.getQueryString());
        this.requestDispatcher(kvpParameters);
    }

    /**
     * Depend on the request parameter to handle the request (WCS, WCPS,...)
     *
     * @param kvpParameters
     * @throws WCSException
     * @throws IOException
     * @throws PetascopeException
     * @throws petascope.exceptions.SecoreException
     */
    @Override
    protected void requestDispatcher(Map<String, String[]> kvpParameters) throws IOException, PetascopeException, WCSException, SecoreException, WMSException {
        // WCS GetCoverage request can contain multiple duplicate subset parameters (e.g: subset=i(0,10)&subset=k(40,50)                
        log.debug("Received request: " + this.getRequestRepresentation(kvpParameters));

        Response response = null;
        if (kvpParameters.isEmpty()) {
            // a WCS request without any params, so returns WCS-Client            
            byte[] bytes = IOUtils.toString(this.getClass().getResourceAsStream("/" + "public/interface-servlet.html")).getBytes();
            response = new Response(Arrays.asList(bytes), "text/html");
        } else {

            // e.g: WCS, WMS
            String service = kvpParameters.get(KVPSymbols.KEY_SERVICE)[0];
            // e.g: 2.0.1 (WCS), 1.3.0 (WMS)
            String version = kvpParameters.get(KVPSymbols.KEY_VERSION)[0];
            // e.g: GetCapabilities, DescribeCoverage
            String requestService = kvpParameters.get(KVPSymbols.KEY_REQUEST)[0];

            // Check if any handlers can handle the request
            for (AbstractHandler handler : handlers) {
                if (handler.canHandle(service, version, requestService)) {
                    response = handler.handle(kvpParameters);
                }
            }
            if (response == null) {
                throw new PetascopeException(ExceptionCode.NoApplicableCode, "Cannot find the handler for the request.");
            }
        }

        // Dump the response result to client
        this.writeResponseResult(response);
    }
}
