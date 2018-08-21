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
import petascope.wms.exception.WMSDuplicateStyleForLayerException;
import petascope.wms.exception.WMSLayerNotExistException;
import petascope.wms.exception.WMSMissingRequestParameter;
import petascope.wms.exception.WMSStyleNotFoundException;
import petascope.wms.handlers.service.WMSGetMapCachingService;

/**
 * Handle the InsertStyle request to insert a Style to a layer, e.g:
 * service=WMS&version=1.3.0&request=InsertStyle&name=FireMarkup&
 * layer=test_wms_4326&abstract=This style marks the areas where fires are in
 * progress with the color red&rasqlTransformFragment=case $Iterator when
 * ($Iterator + 2) > 20 then {255,0,0} when ($Iterator + 5 + 25 - 25) > 10+5
 * then {0,255,0} when (2+$Iterator+0.5-0.25*(0.5+5)) < 10-5+2 then {0,0,255}
 * else {0,0,0} end
 *
 * NOTE: This is a made up request as only Rasdaman supports this kind of style
 * by Rasql fragment query.
 *
 *
 * @author
 * <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWMSInsertUpdateStyleHandler extends KVPWMSAbstractHandler {

    private static Logger log = LoggerFactory.getLogger(KVPWMSInsertUpdateStyleHandler.class);

    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;

    public KVPWMSInsertUpdateStyleHandler() {

    }

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws WMSException {
        // Check if layer parameter does exist from the request        
        String[] layerParam = kvpParameters.get(KVPSymbols.KEY_WMS_LAYER);
        if (layerParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_LAYER);
        }

        // Check if name of style parameter does exist from the request
        String[] nameParam = kvpParameters.get(KVPSymbols.KEY_WMS_NAME);
        if (nameParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_NAME);
        }
        
        // Check if WCPS query fragment does exist from the request
        String[] wcpsQueryFragmentParam = kvpParameters.get(KVPSymbols.KEY_WMS_WCPS_QUERY_FRAGMENT);
        if (wcpsQueryFragmentParam == null) {
            // Check if Rasql transform fragment does exist from the request
            String[] rasqlFragmentParam = kvpParameters.get(KVPSymbols.KEY_WMS_RASQL_TRANSFORM_FRAGMENT);
            if (rasqlFragmentParam == null) {
                // Neither wcpsQueryFragment or rasqlTransformFragment does exist, so throw exception for this style
                throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_WCPS_QUERY_FRAGMENT + " or " + KVPSymbols.KEY_WMS_RASQL_TRANSFORM_FRAGMENT);
            }
        }
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        // Validate before handling the request
        this.validate(kvpParameters);

        // Check if layer does exist in database
        String layerName = kvpParameters.get(KVPSymbols.KEY_WMS_LAYER)[0];
        String styleName = kvpParameters.get(KVPSymbols.KEY_WMS_NAME)[0];
        // NOTE: if title param does not exist, title is as same as name
        String styleTitle = kvpParameters.get(KVPSymbols.KEY_WMS_TITLE) == null ? styleName : kvpParameters.get(KVPSymbols.KEY_WMS_TITLE)[0];
        String styleAbstract = kvpParameters.get(KVPSymbols.KEY_WMS_ABSTRACT) == null ? "" : kvpParameters.get(KVPSymbols.KEY_WMS_ABSTRACT)[0];        
        Layer layer = this.wmsRepostioryService.readLayerByNameFromDatabase(layerName);
        
        if (layer == null) {
            throw new WMSLayerNotExistException(layerName);
        }

        String request = kvpParameters.get(KVPSymbols.KEY_REQUEST)[0];
        Style style = null;
        if (request.equals(KVPSymbols.VALUE_WMS_INSERT_STYLE)) {
            // Check if style does exist
            style = layer.getStyle(styleName);
            if (style != null) {
                // Cannot add same style name for a layer
                throw new WMSDuplicateStyleForLayerException(styleName, layerName);
            } else {
                // create new style
                style = new Style();
                layer.getStyles().add(style);
            }
        } else {
            style = layer.getStyle(styleName);
        }
        
        if (style == null) {
            throw new WMSStyleNotFoundException(styleName, layerName);
        }

        style.setName(styleName);
        style.setTitle(styleTitle);
        style.setStyleAbstract(styleAbstract);
        
        // A style must have a value for wcpsQueryFragment or rasqlTransformFragment
        if (kvpParameters.get(KVPSymbols.KEY_WMS_WCPS_QUERY_FRAGMENT) != null) {
            style.setWcpsQueryFragment(kvpParameters.get(KVPSymbols.KEY_WMS_WCPS_QUERY_FRAGMENT)[0]);
        } else if (kvpParameters.get(KVPSymbols.KEY_WMS_RASQL_TRANSFORM_FRAGMENT) != null) {
            // deprecated
            style.setRasqlQueryTransformFragment(kvpParameters.get(KVPSymbols.KEY_WMS_RASQL_TRANSFORM_FRAGMENT)[0]);
        }

        // Then update the layer with the new updated/added style to database.
        this.wmsRepostioryService.saveLayer(layer);
        log.info("WMS Style '" + style.getName() + "' is persisted in database.");

        if (!request.equals(KVPSymbols.VALUE_WMS_INSERT_STYLE)) {
            // Remove all the cached GetMap response from cache as style is updated
            this.wmsGetMapCachingService.removeStyleGetMapInCache(layerName, styleName);
        }

        // InsertStyle, UpdateStyle returns empty string as a success
        return new Response(Arrays.asList("".getBytes()), MIMEUtil.MIME_GML, null);
    }
}
