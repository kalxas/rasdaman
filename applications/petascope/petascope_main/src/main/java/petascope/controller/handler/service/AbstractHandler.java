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
package petascope.controller.handler.service;

import org.rasdaman.config.VersionManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import petascope.core.KVPSymbols;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.response.Response;
import petascope.exceptions.WMSException;

/**
 *
 * Abstract handler class for all requests in KVP or XML
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public abstract class AbstractHandler {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AbstractHandler.class);
    // A Handler can only handle 1 kind of service (WCS / WMS)
    protected String service;
    // But it can handle multiple kind of service types (e.g: GetCapabilities, DescribeCoverage, ProcessCoverages)
    protected List<String> requestServices = new ArrayList<>();

    /**
     * Build the queryString (e.g: a=1&b=2&c=3...) from map of keys values
     *
     * @param kvpParameters
     * @return
     */
    public static String getQueryString(Map<String, String[]> kvpParameters) {
        String queryString = "";
        // Build the queryString from parameters map for the old parsers
        for (Map.Entry<String, String[]> entry : kvpParameters.entrySet()) {
            // current + is missing in date time string, so just replace " 00:00" with "+00:00"
            // @TODO: replace this fix when supporting this plus sign
            String value = entry.getValue()[0].replace(" 00:00", "+00:00");
            queryString += entry.getKey() + "=" + value + "&";
        }
        // Remove the trailing & which cause error in parse subsets
        queryString = queryString.substring(0, queryString.length() - 1);

        return queryString;
    }

    /**
     * Check if a RequestHandler can handle a input request for a service, e.g:
     * WCS: GetCapabilities, DescribeCoverage, GetCoverage, ProcessCoverages
     * WMS: GetCapaiblities, GetMap, InsertWCSLayer, InsertStyle, DeleteStyle,
     * DeleteLayer
     *
     * @param service
     * @param version
     * @param requestService
     * @return
     */
    public boolean canHandle(String service, String[] versions, String requestService) {
        // Handler could handle a service (WCS / WMS)
        for (String version : versions) {
            if (this.service.equals(service) && VersionManager.isSupported(service, version)) {
                for (String handableRequestService : requestServices) {
                    if (handableRequestService.equals(requestService)) {
                        log.debug("Found the request handler: " + this.getClass().getCanonicalName());
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public abstract Response handle(Map<String, String[]> kvpParameters)
                    throws Exception;
    
    
    /**
     * Return the service (e.g: WCS, WCPS or WMS) of handler.
     */
    public String getService() {
        return this.service;
    }
}
