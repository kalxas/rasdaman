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
package petascope.wcps.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.util.ListUtil;
import petascope.util.StringUtil;
import static petascope.wcps.handler.ForClauseHandler.AS;
import petascope.wcps.metadata.service.CollectionAliasRegistry;
import petascope.wcps.metadata.service.CoverageAliasRegistry;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;

/**
 * Translation node from wcps to rasql. Example:  <code>
 * for $c1 in cov1 for $c2 in cov 2 return encode($c1 + $c2, "csv")
 * </code> translates to  <code>
 * SELECT csv(c1 + c2) FROM cov1 as c1, cov2 as c2
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RootHandler extends Handler {
    
    @Autowired
    private CoverageAliasRegistry coverageAliasRegistry;
    @Autowired
    private CollectionAliasRegistry collectionAliasRegistry;
    
    
    public RootHandler create(Handler forClauseListHandler, Handler letClauseListHandler, Handler whereClauseHandler, Handler returnClauseHandler) {
        RootHandler result = new RootHandler();
        result.setChildren(Arrays.asList(forClauseListHandler, letClauseListHandler, whereClauseHandler, returnClauseHandler));
        result.coverageAliasRegistry = this.coverageAliasRegistry;
        result.collectionAliasRegistry = this.collectionAliasRegistry;
        
        
        return result;
    }
    
    public VisitorResult handle() throws PetascopeException {
        
        WcpsResult forClauseListVisitorResult = (WcpsResult) this.getFirstChild().handle();
        
        WcpsResult letClauseListVisitorResult = null;
        if (this.getSecondChild() instanceof LetClauseListHandler) {
            letClauseListVisitorResult = (WcpsResult) this.getSecondChild().handle();
        }
        
        WcpsResult whereClauseVisitorResult = null;
        if (this.getThirdChild()instanceof WhereClauseHandler) {
            whereClauseVisitorResult = (WcpsResult) this.getThirdChild().handle();
        }

        VisitorResult returnClauseVisitorResult = this.getFourthChild().handle();
        
        VisitorResult finalResult = returnClauseVisitorResult;
        
        if (returnClauseVisitorResult instanceof WcpsResult) {
            // rasql query
            finalResult = this.handle(forClauseListVisitorResult, letClauseListVisitorResult, 
                                      whereClauseVisitorResult, (WcpsResult) returnClauseVisitorResult);
        }
        
        return finalResult;
    }
    
    private WcpsResult handle(WcpsResult forClauseList, WcpsResult letClauseList, WcpsResult whereClause, WcpsResult returnClause) throws PetascopeException {
        // SELECT c1 + c2
        String rasql = returnClause.getRasql();

        String whereClauseStr = null;
        
        if (whereClause != null) {
            whereClauseStr = whereClause.getRasql();
        }
        
        List<String> finalRasqlQueries = this.createFinalRasqlQueries(rasql, whereClauseStr);
        returnClause.setFinalRasqlQueries(finalRasqlQueries);
        
        return returnClause;
    }
    
    
    /***
     * Normally for c in (cov1) return c; returns 1 query, 
     * but if for c in (cov1, cov2) then it returns 2 rasql queries (one for collection1 as c and one for collection2 as c)
     * 
     */
    private List<String> createFinalRasqlQueries(String defaultRasql, String whereClause) throws PetascopeException {
        List<String> finalRaslQueries = new ArrayList<>();
        
        List<List<String>> listsTmp = new ArrayList<>();
        // e.g. c -> [ (cov11:collection11), (cov12: collection12), ...]
        for (Map.Entry<String, List<Pair<String, String>>> entry : this.coverageAliasRegistry.getCoverageMappings().entrySet()) {
            // e.g. c
            String coverageVarableName = entry.getKey();
            List<String> collectionVariableNamesList = new ArrayList<>();

            // e.g. [ (cov11:collection11), (cov12: collection12), ...]
            List<Pair<String, String>> coverageIdsCollectionNamesList = entry.getValue();
            for (Pair<String, String> pair : coverageIdsCollectionNamesList) {
                String collectionName = pair.snd;
                if (collectionName != null) {
                    // e.g collection11 as c
                    String clause = pair.snd + " AS " + StringUtil.stripDollarSign(coverageVarableName);
                    collectionVariableNamesList.add(clause);
                }

            }            
            
            if (!collectionVariableNamesList.isEmpty()) {
                listsTmp.add(collectionVariableNamesList);
            }
        }
        
        
        // for virtual coverages, their rasdaman collection names are null -> collect source coverages' collections
        for (Map.Entry<String, Pair<String, String>> entry : this.collectionAliasRegistry.getAliasMap().entrySet()) {
            // e.g: utm31 as c0
            String clause = entry.getValue().fst + " " + AS + " " + entry.getKey();
            boolean mustAdd = true;
            for (List<String> list : listsTmp) {
                for (String str : list) {
                    if (str.equals(clause)) {
                        mustAdd = false;
                        break;
                    }
                }
            }
            
            if (mustAdd) {
                List<String> collectionVariableNamesList = new ArrayList<>();            
                collectionVariableNamesList.add(clause);

                listsTmp.add(collectionVariableNamesList);
            }
        }  
        
        // e.g. with list1[ collection11 as c, collection12 as c ], list2[ collection21 as d]
        // returns list[ list1[ collection 11 as c, collection21 as d ], list2[ collection12 as c, collection21 as d ] ]
        List<List<String>> catersianProductList = ListUtil.cartesianProduct(listsTmp);
        for (List<String> list : catersianProductList) {
            String fromClause = " FROM " + ListUtil.join(list, ", ");
            String rasqlQuery = defaultRasql + fromClause;
            if (whereClause != null) {
                rasqlQuery += " " + whereClause;
            }
            finalRaslQueries.add(rasqlQuery);
        }
        
        return finalRaslQueries;
    }
    
}
