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

import java.io.IOException;
import java.util.Map;
import org.rasdaman.config.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import petascope.wcs2.handlers.kvp.KVPWCSDescribeCoverageHandler;
import petascope.wcs2.handlers.kvp.KVPWCSGetCapabilitiesHandler;
import petascope.core.response.Response;
import petascope.exceptions.WMSException;
import petascope.wcs2.handlers.kvp.KVPWCSGetCoverageHandler;

/**
 * The main handler for all WCS requests in KVP request
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class KVPWCSServiceHandler extends AbstractHandler {

    @Autowired
    private KVPWCSGetCapabilitiesHandler getCapabilitiesHandler;
    @Autowired
    private KVPWCSDescribeCoverageHandler describeCoverageHandler;
    @Autowired
    private KVPWCSGetCoverageHandler getCoverageHandler;

    public KVPWCSServiceHandler() {
        // WCS2 is a part of WCS
        service = KVPSymbols.WCS_SERVICE;
        version = ConfigManager.WCS_VERSIONS;

        requestServices.add(KVPSymbols.VALUE_GET_CAPABILITIES);
        requestServices.add(KVPSymbols.VALUE_DESCRIBE_COVERAGE);
        requestServices.add(KVPSymbols.VALUE_GET_COVERAGE);
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws WCSException, IOException, PetascopeException, SecoreException, WMSException {
        String requestService = kvpParameters.get(KVPSymbols.KEY_REQUEST)[0];
        Response response = null;

        // GetCapabilities
        if (requestService.equals(KVPSymbols.VALUE_GET_CAPABILITIES)) {
            response = getCapabilitiesHandler.handle(kvpParameters);
        } // DescribeCoverage
        else if (requestService.equals(KVPSymbols.VALUE_DESCRIBE_COVERAGE)) {
            response = describeCoverageHandler.handle(kvpParameters);
        } else if (requestService.equals(KVPSymbols.KEY_GET_COVERAGE)) {
            response = getCoverageHandler.handle(kvpParameters);
        }

        return response;
    }
}
