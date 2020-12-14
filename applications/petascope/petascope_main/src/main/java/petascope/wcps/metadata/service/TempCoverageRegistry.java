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
package petascope.wcps.metadata.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.util.ras.RasUtil;

/**
 * Store the map of positional parameters and their temp coverage ids of temp coverages
 * and local file paths created during the processing of a WCPS query. 
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TempCoverageRegistry {
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    
    // e.g: $1 -> (tmp_covA, "/tmp/rasdaman_petascope/rasdaman...test.tif")
    private Map<String, Pair<String, String>> positionalParametersMap = new HashMap<>();
    
    public TempCoverageRegistry() {
        
    }
    
    public void add(String positionalParameter, String coverageId, String filePath) {
        // e.g: $1 -> (tmp_covA, "/tmp/rasdaman_petascope/rasdaman...test.tif")
        this.positionalParametersMap.put(positionalParameter, new Pair<>(coverageId, filePath));
    }
    
    /**
     * Return the temp coverage id created by a positional parameter,
     * e.g: $1 -> tempCovId
     */
    public String getTempCoverageId(String positionalParameter) {
        Pair<String, String> pair = this.positionalParametersMap.get(positionalParameter);
        String result = null;
        
        if (pair != null) {
            result = pair.fst;
        }
        
        return result;
    }
    
    
    /**
     * Removed stored temp coverage objects and temp collections
     */
    public void clear() throws PetascopeException {
        List<String> tempCoverageIds = this.getListTempCoverageIds();
        String username = ConfigManager.RASDAMAN_ADMIN_USER;
        String password = ConfigManager.RASDAMAN_ADMIN_PASS;
        
        for (String coverageId : tempCoverageIds) {
            // Delete temp rasdaman collection
            // @TODO: this temp rasdaman collection was created until SELECT decode() rasql works fine            
            RasUtil.deleteFromRasdaman(coverageId, username, password);
            this.coverageRepositoryService.removeFromLocalCacheMap(coverageId);
        }        
        
        this.positionalParametersMap.clear();
    }

    /**
     * Return the list of temporary coverage ids created for a WCPS query
     */
    private List<String> getListTempCoverageIds() {
        List<String> results = new ArrayList<>();
        
        // Pair(tempCoverageId, filePath)
        for (Pair<String, String> pair : this.positionalParametersMap.values()) {
            String tempCoverageId = pair.fst;
            
            results.add(tempCoverageId);
        }
        
        return results;
    }
}
