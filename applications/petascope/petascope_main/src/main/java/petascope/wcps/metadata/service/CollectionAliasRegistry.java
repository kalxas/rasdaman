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
 * Copyright 2003 - 2019 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.metadata.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.core.Pair;

/**
 * 
 * This class keeps the alias of rasdaman collections used in a WCPS query
 * e.g: FROM c0 as utm31, c1 as utm32, c2 as utm33,...
 * 
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CollectionAliasRegistry {

    // c0 -> Pair<collection1, coverage1>
    private HashMap<String, Pair<String, String>> aliasMap = new LinkedHashMap<>();

    public CollectionAliasRegistry() {
        
    }

    public HashMap<String, Pair<String, String>> getAliasMap() {
        return aliasMap;
    }

    public void update(String aliasName, String rasdamanCollectionName, String coverageId) {
        aliasMap.put(aliasName, new Pair<>(rasdamanCollectionName, coverageId));
    }

    public void add(String aliasName, String rasdamanCollectionName, String coverageId) {
        if (aliasName != null && rasdamanCollectionName != null) {
            this.update(aliasName, rasdamanCollectionName, coverageId);
        }
    }
    
    public void remove(String coverageAlias) {
        aliasMap.remove(coverageAlias);
    }

    /**
     * e.g: c0 -> utm31
     */
    public String getCollectionName(String aliasName) {
        return aliasMap.get(aliasName).fst;
    }
    
    /**
     * e.g: utm31 -> c0
     */
    public String getAliasName(String collectionName) {
        
        for (Map.Entry<String, Pair<String, String>> entry : this.aliasMap.entrySet()) {
            if (entry.getValue().fst.equals(collectionName)) {
                return entry.getKey();
            }
        }
        
        return null;
    }
}
