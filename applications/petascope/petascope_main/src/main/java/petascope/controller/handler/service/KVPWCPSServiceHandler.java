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
import petascope.wcs2.handlers.kvp.KVPWCSProcessCoverageHandler;
import petascope.core.response.Response;
import petascope.exceptions.WMSException;

/**
 * Class to handle WCPS request in KVP
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWCPSServiceHandler extends AbstractHandler {

    @Autowired
    private KVPWCSProcessCoverageHandler processCoverageHandler;

    public KVPWCPSServiceHandler() {
        // WCPS is a part of WCS2
        service = KVPSymbols.WCS_SERVICE;

        this.requestServices.add(KVPSymbols.KEY_PROCESS_COVERAGES);
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws WCSException, IOException, PetascopeException, SecoreException, WMSException {
        Response response = this.processCoverageHandler.handle(kvpParameters);
        return response;
    }
}
