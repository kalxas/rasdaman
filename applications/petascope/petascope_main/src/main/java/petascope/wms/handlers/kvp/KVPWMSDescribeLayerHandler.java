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
package petascope.wms.handlers.kvp;

import java.util.Arrays;
import java.util.Map;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WMSException;
import petascope.util.MIMEUtil;
import petascope.wms.exception.WMSLayerNotExistException;
import petascope.wms.exception.WMSMissingRequestParameter;

/**
 * A non-standard handler to check if a layer exists or not in database.
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class KVPWMSDescribeLayerHandler extends KVPWMSAbstractHandler {
    
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    
    @Override
    public void validate(Map<String, String[]> kvpParameters) throws WMSException {
        // Check if layer parameter does exist from the request
        String[] layerParam = kvpParameters.get(KVPSymbols.KEY_WMS_LAYER);
        if (layerParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_LAYER);
        }
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {        
        // Validate before handling the request
        this.validate(kvpParameters);

        // Check if layer does exist in database
        String layerName = kvpParameters.get(KVPSymbols.KEY_WMS_LAYER)[0];
        // layer doesn't exist, then exception
        if (!this.wmsRepostioryService.isInLocalCache(layerName)) {
            throw new WMSLayerNotExistException(layerName);
        }
        
        return new Response(Arrays.asList(layerName.getBytes()), MIMEUtil.MIME_GML, layerName);
    }
    
}
