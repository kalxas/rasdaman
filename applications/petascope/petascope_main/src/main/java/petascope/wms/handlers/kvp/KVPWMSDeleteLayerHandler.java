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


import petascope.core.response.Response;
import java.util.Arrays;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.AbstractController;
import petascope.core.KVPSymbols;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.MIMEUtil;
import petascope.exceptions.WMSException;
import petascope.wms.exception.WMSMissingRequestParameter;
import petascope.wms.handlers.service.WMSGetMapCachingService;

/**
 * Handle the DeleteLayer in WMS 1.3 request to delete a WMS layer without deleting the WCS associated coverage,
 * e.g:   SERVICE=WMS&VERSION=1.3.0
 *        &REQUEST=DeleteLayer
 *        &LAYER=MyLayer
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWMSDeleteLayerHandler extends KVPWMSAbstractHandler {

    private static Logger log = LoggerFactory.getLogger(KVPWMSDeleteLayerHandler.class);
    
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;
    
    public KVPWMSDeleteLayerHandler() {

    }
    
    @Override
    public void validate(Map<String, String[]> kvpParameters) throws WMSException {
        // Check if layer does exist from the request        
        String[] layerParam = kvpParameters.get(KVPSymbols.KEY_WMS_LAYER);
        if (layerParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_LAYER);
        }
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {        
        // Validate before handling the request
        this.validate(kvpParameters);

        String layerName = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMS_LAYER);
        Layer layer = this.wmsRepostioryService.readLayerByNameFromDatabase(layerName);
                

        // Then delete the layer from database.
        this.wmsRepostioryService.deleteLayer(layer);
        
        // Then remove the GetMap request which contains layers and styles from cache
        this.wmsGetMapCachingService.removeLayerGetMapInCache(layerName);

        // Request returns empty string as a success
        return new Response(Arrays.asList("".getBytes()), MIMEUtil.MIME_GML, layerName);
    }    
}
