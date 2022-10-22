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
package com.rasdaman.admin.layer.service;

// -- rasdaman enterprise begin

import com.rasdaman.admin.service.AbstractAdminService;
import petascope.core.response.Response;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.AbstractController;
import static petascope.core.KVPSymbols.KEY_COVERAGE_ID;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.SetUtil;

/**
 * Activate (create) a WMS layer which doesn't exist yet for a geo-referenced coverage
 * <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class AdminActivateLayerService extends AbstractAdminService {

    private static Logger log = LoggerFactory.getLogger(AdminActivateLayerService.class);
    
    private static Set<String> VALID_PARAMETERS = SetUtil.createLowercaseHashSet(KEY_COVERAGE_ID);
    
    @Autowired
    private AdminCreateOrUpdateLayerService createOrUpdateLayerService;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;

    public AdminActivateLayerService() {

    }

    private void validate(Map<String, String[]> kvpParameters) throws PetascopeException {
        this.validateRequiredParameters(kvpParameters, VALID_PARAMETERS);
    }

    @Override
    public Response handle(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
        // Validate before handling the request
        this.validate(kvpParameters);
        
        String coverageId = AbstractController.getValueByKey(kvpParameters, KEY_COVERAGE_ID);
        
        if (this.wmsRepostioryService.isInLocalCache(coverageId)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Layer '" + coverageId + "' already exists in local database.");
        }
        
        // create a new layer if not exist, or update the existing layer from the existing coverage
        this.createOrUpdateLayerService.save(coverageId, null);
        
        log.info("Layer '" + coverageId + "' is activated.");
        
        Response result = new Response();
        return result;
    }
}
