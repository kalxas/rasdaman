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
package petascope.wms.handlers.kvp;

import petascope.core.response.Response;
import java.util.Arrays;
import java.util.Map;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.domain.wms.Style;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.KVPSymbols;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.MIMEUtil;
import petascope.exceptions.WMSException;
import petascope.wms.exception.WMSMissingRequestParameter;
import petascope.wms.exception.WMSStyleNotFoundException;
import petascope.wms.handlers.service.WMSGetMapCachingService;

/**
 * Handle the DeleteStyle in WMS 1.3 request, e.g: service=WMS&version=1.3.0&
 * request=DeleteStyle&layer=test_wms_4326&style=TempStyle NOTE: This is a made up request as
 * only Rasdaman supports Rasql fragment as style for WMS layer. A layer in WMS
 * is a coverage in WCS, so when WCS DeleteCoverage, WMS will also need to
 * remove the layer and all children elements of this layer.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWMSDeleteStyleHandler extends KVPWMSAbstractHandler {

    private static Logger log = LoggerFactory.getLogger(KVPWMSDeleteStyleHandler.class);
    
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;

    public KVPWMSDeleteStyleHandler() {

    }
    
    @Override
    public void validate(Map<String, String[]> kvpParameters) throws WMSException {
        // Check if layer parameter does exist from the request
        String[] layerParam = kvpParameters.get(KVPSymbols.KEY_WMS_LAYER);
        if (layerParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_LAYER);
        }

        String[] nameParam = kvpParameters.get(KVPSymbols.KEY_WMS_NAME);
        if (nameParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_NAME);
        }
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {        
        // Validate before handling the request
        this.validate(kvpParameters);

        // Check if layer does exist in database
        String layerName = kvpParameters.get(KVPSymbols.KEY_WMS_LAYER)[0];
        String styleName = kvpParameters.get(KVPSymbols.KEY_WMS_NAME)[0];
        Layer layer = this.wmsRepostioryService.readLayerByNameFromDatabase(layerName);

        // Then check if style does exist of requesting layer
        int i = 0;
        Style requestingStyle = null;
        for (Style style : layer.getStyles()) {
            if (style.getName().equals(styleName)) {
                requestingStyle = style;
                break;
            }
            i++;
        }

        // Style does not exist, so cannot delete it.
        if (requestingStyle == null) {
            throw new WMSStyleNotFoundException(styleName, layerName);
        }
        
        layer.getStyles().remove(i);

        // Then delete the style of the layer from database.
        this.wmsRepostioryService.saveLayer(layer);
        
        // Then remove the GetMap request which contains layers and styles from cache
        this.wmsGetMapCachingService.removeStyleGetMapInCache(layerName, styleName);

        // DeleteStyle returns empty string as a success
        return new Response(Arrays.asList("".getBytes()), MIMEUtil.MIME_GML, layerName);
    }    
}
