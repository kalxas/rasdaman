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
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.controller.handler.service;

import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.AbstractController;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import petascope.exceptions.WMSException;
import petascope.wmts.handlers.kvp.KVPWMTSGetCapabilitiesHandler;
import petascope.wmts.handlers.kvp.KVPWMTSGetTileHandler;

/**
 * Main handler for all WMTS requests.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWMTSServiceHandler extends AbstractHandler {

    @Autowired
    private KVPWMTSGetCapabilitiesHandler getCapabilitiesHandler;
    @Autowired
    private KVPWMTSGetTileHandler getTileHandler;

    public KVPWMTSServiceHandler() {        
        service = KVPSymbols.WMTS_SERVICE;

        requestServices.add(KVPSymbols.VALUE_WMTS_GET_CAPABILITIES);
        requestServices.add(KVPSymbols.VALUE_WMTS_GET_TILE);
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws WCSException, IOException, PetascopeException, SecoreException, WMSException, Exception {
        String requestService = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_REQUEST);
        Response response = null;

        if (requestService.equals(KVPSymbols.VALUE_WMTS_GET_CAPABILITIES)) {
            // GetCapabilities
            response = getCapabilitiesHandler.handle(kvpParameters);
        } else if (requestService.equals(KVPSymbols.VALUE_WMTS_GET_TILE)) {
            // GetTile
            response = getTileHandler.handle(kvpParameters);
        }

        return response;
    }
}
