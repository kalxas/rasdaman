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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.metadata.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.util.ListUtil;
import static petascope.wcps.handler.ForClauseHandler.AS;

/**
 * This class has the purpose of keeping information about coverage aliases inside 1 query (e.g. "for c in mr" means
 * that c is an alias for mr in this query).  1 instance of this object is created for every Wcps query.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CoverageAliasRegistry {
    
    @Autowired
    private CollectionAliasRegistry collectionAliasRegistry;

    // NOTE: a coverage variable can be alias for multiple coverage names
    private LinkedHashMap<String, List<Pair<String, String>>> coverageMappings = new LinkedHashMap<>();

    public CoverageAliasRegistry() {
        
    }
    
    /**
     * As this bean exists for a HTTP request, so in case of WCS multipart, it still contains the data from the first WCPS query, so need to clear it
     */
    public void clear() {
        coverageMappings = new LinkedHashMap<>();
    }
    
    public void remove(String coverageAlias) {
        coverageMappings.remove(coverageAlias);
    }
   
    public void updateCoverageMapping(String coverageAlias, String coverageName, String rasdamanCollectionName) {
        List<Pair<String, String>> values = coverageMappings.get(coverageAlias);
        if (values != null) {
            coverageMappings.put(coverageAlias, ListUtil.valuesToList(new Pair<>(coverageName, rasdamanCollectionName)));
        }
    }

    public void addCoverageMapping(String coverageAlias, String coverageName, String rasdamanCollectionName) {
        List<Pair<String, String>> values = coverageMappings.get(coverageAlias);
        if (values != null) {
            // If key -> value exist then just add new coverage name to value
            coverageMappings.get(coverageAlias).add(new Pair<>(coverageName, rasdamanCollectionName));
        } else {
            // if key does not exist then need to add key first then add value for this key
            coverageMappings.put(coverageAlias, ListUtil.valuesToList(new Pair<>(coverageName, rasdamanCollectionName)));
        }
    }

    /* Always get the first coverageName in the arrayList to create defaultRasql query
    * e.g: for c in (mr, rgb) ... then rgb can be used later to create another Rasql query for multipart
    * NOTE: due to coverage variable name (e.g: c) and axis iterator is same syntax (e.g: $px)
    * then check if the alias is for an existing coverage first, if not then check if it is an axis iterator.
    */
    public String getCoverageName(String alias) {
        String coverageName = null;
        if (coverageMappings.get(alias) != null) {
            coverageName = coverageMappings.get(alias).get(0).fst;
        }

        return coverageName;
    }
    
    /**
     * Get rasdamanCollectionName by coverage's alias (e.g: for c in (test_mean_summer_airtemp))
     * test_mean_summer_airtemp is coverageName, test_mean_sumer_airtemp_datetime is rasdamanCollectionName
     * @param alias: coverage iterator (c)
     * @return rasdamanCollectionName
     */
    public String getRasdamanCollectionNameByAlias(String alias) {
        String rasdamanCollectionName = null;
        if (coverageMappings.get(alias) != null) {
            rasdamanCollectionName = coverageMappings.get(alias).get(0).snd;
        }
        return rasdamanCollectionName;
    }
    
    /**
     * Return the alias by a coverage name (e.g: for c in (test_mr, test_rgb))
     * 
     * @param coverageName input coverage name (e.g: test_mr)
     * @return alias (e.g: c)
     */
    public String getAliasByCoverageName(String coverageName) {
        for (Map.Entry<String, List<Pair<String, String>>> entry : this.coverageMappings.entrySet()) {
            for (Pair<String, String> pair : entry.getValue()) {
                if (pair.fst.equals(coverageName)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
    
    /**
     * Return the rasdaman collection name by coverage name
     * @param coverageName input coverage name
     * @return coverage's rasdaman collection name
     */
    public String getRasdamanCollectionNameByCoverageName(String coverageName) {
        for (Map.Entry<String, List<Pair<String, String>>> entry : this.coverageMappings.entrySet()) {
            for (Pair<String, String> pair : entry.getValue()) {
                if (pair.fst.equals(coverageName)) {
                    return pair.snd;
                }
            }
        }
        return null;
    }
    

    /**
     * return list( $coverageVariableName -> arrayList (coverageNames) )
     * e.g: $c -> (mr, rgb), $d -> (mr1, rgb1)
     * @return
     */
    public LinkedHashMap<String, List<Pair<String, String>>> getCoverageMappings() {
        return this.coverageMappings;
    }
    
    /**
     * Return the string representing this Map of coverage iterators and rasdaman collection names
     * e.g: c -> test_mean_summer_airtemp, d -> test_mean_summer_airtemp_repeat
     * @return String: test_mean_summer_airtemp as c, test_mean_summer_airtemp_repeat as d
     */
    public String getRasqlFromClause() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, List<Pair<String, String>>> entry : this.coverageMappings.entrySet()) {
            String coverageIterator = entry.getKey();
            List<String> tmpList = new ArrayList<>();
            for (Pair<String, String> pair : entry.getValue()) {
                // e.g: test_mean_summer_airtemp as c, not test_mean_summer_airtemp as $c
                if (pair.snd != null) {
                    tmpList.add(pair.snd + " " + AS + " " + coverageIterator.replace("$", ""));
                }
            }
            
            // e.g: test_mean_summer_airtemp as c
            if (tmpList.size() > 0) {
                String tmpOuput = ListUtil.join(tmpList, ", ");
                list.add(tmpOuput);
            }
        }
        
        for (Map.Entry<String, Pair<String, String>> entry : this.collectionAliasRegistry.getAliasMap().entrySet()) {
            // e.g: utm31 as c0
            String clause = entry.getValue().fst + " " + AS + " " + entry.getKey();
            list.add(clause);
        }
         
        // test_mean_summer_airtemp as c, test_mean_summer_airtemp as d
        String output = ListUtil.join(list, ", ");
        
        return output;
    } 
    
    /**
     * Return the map of coverage Ids -> coverage alias
     * e.g: test_mean_summer_airtemp -> $c
     */
    public Map<String, String> getCoverageAliasMap() {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, List<Pair<String, String>>> entry : this.coverageMappings.entrySet()) {
            String alias = entry.getKey();
            for (Pair<String, String> pair : entry.getValue()) {
                String coverageId = pair.fst;
                map.put(coverageId, alias.replace("$", ""));
            }
        }
        
        return map;
    }
}
