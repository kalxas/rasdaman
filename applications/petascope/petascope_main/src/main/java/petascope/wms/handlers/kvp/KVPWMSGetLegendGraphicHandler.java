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
package petascope.wms.handlers.kvp;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.domain.wms.Style;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.AbstractController;
import petascope.controller.PetascopeController;
import static petascope.core.KVPSymbols.KEY_WMS_FORMAT;
import static petascope.core.KVPSymbols.KEY_WMS_LAYER;
import static petascope.core.KVPSymbols.KEY_WMS_STYLE;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WMSException;
import petascope.wms.exception.WMSLayerNotExistException;
import petascope.wms.exception.WMSStyleNotFoundException;

/**
 * Handler for WMS GetLegendGraphic request which returns a stored image in database of a style of a layer
 * 
 * http://localhost:8080/rasdaman/ows?service=WMS&request=GetLegendGraphic&format=image/png&layer=cov1&style=color
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class KVPWMSGetLegendGraphicHandler extends KVPWMSAbstractHandler {

    private static Logger log = LoggerFactory.getLogger(KVPWMSGetLegendGraphicHandler.class);    
    
    @Autowired
    private PetascopeController petascopeController;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        
    }
    
    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws Exception {
        this.validate(kvpParameters);
        
        String layerName = AbstractController.getValueByKey(kvpParameters, KEY_WMS_LAYER);
        // NOTE: GetLegendGraphic does not need style parameter
        String styleName = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_WMS_STYLE);
        // e.g. image/png
        String format = AbstractController.getValueByKey(kvpParameters, KEY_WMS_FORMAT);
        
        // other parameters are ignored as they are optional
        Layer layer = this.wmsRepostioryService.readLayerByName(layerName);
        if (layer == null) {
            throw new WMSLayerNotExistException(layerName);
        }
        
        Style style;
        if (styleName == null) {
            style = layer.getDefaultStyle();
        } else {
            style = layer.getStyle(styleName);
            if (style == null) {
                throw new WMSStyleNotFoundException(styleName, layerName);
            }
        }
        
        if (style.getLegendURL() == null) {
            throw new WMSException(ExceptionCode.InvalidRequest, 
                    "Style: " + styleName  + " of layer: "  + layerName + " does not have legend graphic.");
        }
        
        String storedFormat = style.getLegendURL().getFormat();
        if (!format.equals(storedFormat)) {
            throw new WMSException(ExceptionCode.InvalidRequest, 
                    "Style: " + styleName  + " of layer: "  + layerName + " only supports format: " + storedFormat + ". Given request format: " + format);
        }
        
        
        // data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAATEAAAB+CA....
        String base64WithMIME = style.getLegendURL().getLegendGraphicBase64();
        // iVBORw0KGgoAAAANSUhEUgAAATEAAAB+CA....
        String base64 = base64WithMIME.substring(base64WithMIME.indexOf(",") + 1, base64WithMIME.length());
        byte[] image = Base64.getDecoder().decode(base64);
        
        return new Response(Arrays.asList(image), format);
    }
    
}
