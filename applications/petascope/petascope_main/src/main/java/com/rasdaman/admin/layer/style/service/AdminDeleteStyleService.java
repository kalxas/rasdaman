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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package com.rasdaman.admin.layer.style.service;

import com.rasdaman.admin.service.AbstractAdminService;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
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
import static petascope.core.KVPSymbols.KEY_COVERAGE_ID;
import static petascope.core.KVPSymbols.KEY_STYLE_ID;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WMSException;
import petascope.util.MIMEUtil;
import petascope.util.SetUtil;
import petascope.wms.handlers.service.WMSGetMapCachingService;

/**
 * Service to delete an existing style of a WMS layer
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class AdminDeleteStyleService extends AbstractAdminService {
    
    private static Logger log = LoggerFactory.getLogger(AdminDeleteStyleService.class);
    
    private static Set<String> VALID_PARAMETERS = SetUtil.createLowercaseHashSet(KEY_COVERAGE_ID, KEY_STYLE_ID);
    
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;

    public AdminDeleteStyleService() {

    }
    
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException {
        this.validateRequiredParameters(kvpParameters, VALID_PARAMETERS);
    }

    @Override
    public Response handle(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {        
        
        // Validate before handling the request
        this.validate(kvpParameters);

        // Check if layer does exist in database
        String layerName = AbstractController.getValueByKey(kvpParameters, KEY_COVERAGE_ID);
        String styleName = AbstractController.getValueByKey(kvpParameters, KEY_STYLE_ID);
        
        if (!this.wmsRepostioryService.isInLocalCache(layerName)) {
            throw new WMSException(ExceptionCode.NoSuchLayer, "Layer '" + layerName + "' does not exist in local database.");
        }
        Layer layer = this.wmsRepostioryService.readLayerByNameFromLocalCache(layerName);
        if (!layer.hasStyle(styleName)) {
            throw new WMSException(ExceptionCode.InvalidRequest, "Style '" + styleName + "' does not exist in layer '" + layerName + "'.");
        }
        
        layer = this.wmsRepostioryService.readLayerByNameFromDatabase(layerName);
        int i = 0;
        for (Style style : layer.getStyles()) {
            if (style.getName().equals(styleName)) {
                break;
            }
            i++;
        }
        
        Style style = layer.getStyle(styleName);
        this.wmsRepostioryService.deleteStyle(style);

        layer.getStyles().remove(i);
        
        // Then delete the style of the layer from database.
        this.wmsRepostioryService.saveLayer(layer);
        
        // Then remove the GetMap request which contains layers and styles from cache
        this.wmsGetMapCachingService.removeStyleGetMapInCache(layerName, styleName);

        // DeleteStyle returns empty string as a success
        return new Response(Arrays.asList("".getBytes()), MIMEUtil.MIME_GML, layerName);
    }    
    
}
