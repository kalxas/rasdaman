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
package com.rasdaman.admin.layer.service;


// -- rasdaman enterprise begin

import com.rasdaman.admin.service.AbstractAdminService;

// -- rasdaman enterprise end

import petascope.core.response.Response;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.AbstractController;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.KEY_COVERAGE_ID;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WMSException;
import petascope.util.SetUtil;
import petascope.wms.handlers.service.WMSGetMapCachingService;

/**
 * Deactivate (delete) the existing WMS layer of a coverage.
 * 
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class AdminDeactivateLayerService extends AbstractAdminService {

    private static Logger log = LoggerFactory.getLogger(AdminDeactivateLayerService.class);
    private static Set<String> VALID_PARAMETERS = SetUtil.createLowercaseHashSet(KEY_COVERAGE_ID);
    
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;
    
    public AdminDeactivateLayerService() {

    }
    
    public void validate(Map<String, String[]> kvpParameters) throws WMSException, PetascopeException {
        this.validateRequiredParameters(kvpParameters, VALID_PARAMETERS);
    }

    @Override
    public Response handle(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {     
        // Validate before handling the request
        this.validate(kvpParameters);
        
        String layerName = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_COVERAGEID);
        if (!this.wmsRepostioryService.isInLocalCache(layerName)) {
            throw new PetascopeException(ExceptionCode.NoSuchLayer, "Layer '" + layerName + "' does not exist in local database.");
        }
        
        Layer layer = this.wmsRepostioryService.readLayerByNameFromDatabase(layerName);

        // Then delete the layer from database.
        this.wmsRepostioryService.deleteLayer(layer);
        
        // Then remove the GetMap request which contains layers and styles from cache
        this.wmsGetMapCachingService.removeLayerGetMapInCache(layerName);
        log.info("Layer '" + layerName + "' is deactivated.");

        // Request returns empty string as a success
        return new Response();
    }    
}
