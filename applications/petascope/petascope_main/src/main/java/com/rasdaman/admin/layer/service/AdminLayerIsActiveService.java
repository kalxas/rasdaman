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

import com.rasdaman.admin.service.AbstractAdminService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.AbstractController;
import static petascope.controller.AbstractController.getValueByKey;
import static petascope.core.KVPSymbols.KEY_COVERAGEID;
import static petascope.core.KVPSymbols.KEY_COVERAGE_ID;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.MIMEUtil;
import petascope.util.SetUtil;

/**
 * Service to check if a layer exist in local / remote
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class AdminLayerIsActiveService extends AbstractAdminService {
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    
    private static Set<String> VALID_PARAMETERS = SetUtil.createLowercaseHashSet(KEY_COVERAGE_ID);

    private void validate(Map<String, String[]> kvpParameters) throws PetascopeException {
        this.validateRequiredParameters(kvpParameters, VALID_PARAMETERS);
    }

    private String parseBaseCoverageId(Map<String, String[]> kvpParameters) throws PetascopeException {
        String baseCoverageId = getValueByKey(kvpParameters, KEY_COVERAGE_ID);
        if (!this.coverageRepositoryService.isInCache(baseCoverageId)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Base coverage '" + baseCoverageId + "' does not exist.");
        }

        return baseCoverageId;
    }

    @Override
    public Response handle(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
        
        this.validate(kvpParameters);
        this.parseBaseCoverageId(kvpParameters);
        
        String coverageId = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_COVERAGEID);
        
        Boolean exist = false;
        
        if (coverageId == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                    "Missing one of mandatory request parameters '" + KEY_COVERAGEID + "'.");
        } else {
            exist = this.wmsRepostioryService.isInLocalCache(coverageId);
        }
        
        List<byte[]> datas = Arrays.asList(exist.toString().getBytes());
        
        Response result = new Response(datas, MIMEUtil.MIME_TEXT);
        return result;
    }
    
}
