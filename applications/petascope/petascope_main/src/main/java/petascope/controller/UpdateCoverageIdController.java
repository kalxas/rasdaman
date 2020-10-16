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
package petascope.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import static org.rasdaman.config.ConfigManager.ADMIN;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import static petascope.core.KVPSymbols.KEY_COVERAGE_ID;
import static petascope.core.KVPSymbols.KEY_NEW_ID;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 * Endpoint to handle update coverage id request.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@RestController
public class UpdateCoverageIdController extends AbstractController {
    
    private static org.slf4j.Logger log = LoggerFactory.getLogger(UpdateCoverageIdController.class);
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    
    private Set<String> VALID_PARAMETERS = new HashSet<>(Arrays.asList(KEY_COVERAGE_ID.toLowerCase(), KEY_NEW_ID.toLowerCase()));
    
    private void validate(Map<String, String[]> kvpParameters) throws PetascopeException {
        for (Entry<String, String[]> entry : kvpParameters.entrySet()) {
            String key = entry.getKey();
            if (!VALID_PARAMETERS.contains(key.toLowerCase())) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, "Key parameter '" + key + "' is not valid.");
            }
        }
        
        String currentCoverageId = this.getValueByKey(kvpParameters, KEY_COVERAGE_ID);
        String newCoverageId = this.getValueByKey(kvpParameters, KEY_NEW_ID);
        
        if (!this.coverageRepositoryService.isInLocalCache(currentCoverageId)) {
            throw new PetascopeException(ExceptionCode.NoSuchCoverage, "Coverage '" + currentCoverageId + "' not found.");
        }
        if (this.coverageRepositoryService.isInLocalCache(newCoverageId)) {
            throw new PetascopeException(ExceptionCode.NoSuchCoverage, "New coverage id '" + newCoverageId + "' already exists.");
        }
    }
    
    @Override
    @RequestMapping(value = ADMIN + "/UpdateCoverageId", method = RequestMethod.GET)
    protected void handleGet(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, String[]> kvpParameters = this.buildGetRequestKvpParametersMap(httpServletRequest.getQueryString());
        this.requestDispatcher(httpServletRequest, kvpParameters);
    }

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
        this.validate(kvpParameters);
        
        String currentCoverageId = this.getValueByKey(kvpParameters, KEY_COVERAGE_ID);
        String newCoverageId = this.getValueByKey(kvpParameters, KEY_NEW_ID);
        
        this.coverageRepositoryService.updateCoverageId(currentCoverageId, newCoverageId);
        this.wmsRepostioryService.updateLayerName(currentCoverageId, newCoverageId);
    }
    
}
