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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.metadata.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import petascope.wcps2.error.managed.processing.MetaResultMultipartException;
import petascope.wcps2.handler.ForClauseHandler;
import petascope.wcps2.result.VisitorResult;
import petascope.wcps2.result.WcpsMetadataResult;

/**
 * This class has the purpose of rewriting a Rasql query into multiple Rasql queries
 * from 1 WCPS query for returning multipart results.
 * e.g: for c in (mr, rgb) return encode(c, "png")
 * then it will returns 2 Rasql queries
 * select encode(c, "png") from mr
 * select encode(c, "rgb") from rgb
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class RasqlRewriteMultipartQueriesService {

    private final CoverageAliasRegistry coverageAliasRegistry;
    private final VisitorResult wcpsResult;

    public RasqlRewriteMultipartQueriesService(CoverageAliasRegistry coverageAliasRegistry, VisitorResult wcpsResult) {
        this.coverageAliasRegistry = coverageAliasRegistry;
        this.wcpsResult = wcpsResult;

        checkValid();
    }

    /**
     * This function will create multiple rasql queries from the Rasql query result
     * @param coverageAliasRegistry
     * @param defaultRasql
     * @return
     */
    public Stack<String> rewriteQuery(CoverageAliasRegistry coverageAliasRegistry, String defaultRasql) {
        // Store the Rasql queries to update with coverage names in 2 stacks (1 is the final results, 1 is temporary)
        Stack<String> stackUpdatedQueries = new Stack<String>();
        Stack<String> stackTmp = new Stack<String>();

        if (!this.isMultiPart()) {
            stackUpdatedQueries.push(defaultRasql);
        } else {
            // In case of multipart, it need to make separate Rasql queries from coverge iterators and their coverage names
            Iterator it = this.coverageAliasRegistry.getCoverageMappings().entrySet().iterator();
            boolean isFirstIterator = true;
            String rasql = "";
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                // Only add Rasql query if coverage iterator has more than 1 coverage (collection) name
                ArrayList<String> collectionNames = (ArrayList<String>)pair.getValue();
                int size = collectionNames.size();
                String coverageIteratorName = pair.getKey().toString();

                // If coverage iterator has more than 1 coverage name (e.g: for $c in (mr, rgb)), then its coverage name
                // will replace the default string (e.g: $collectionName_c with mr)
                if (size > 1) {
                    // e.g: collectionName_c, collectionName_d
                    String updateName = ForClauseHandler.COLLECTION_NAME + "_" + coverageIteratorName;
                    if (isFirstIterator) {
                        for (int i = 0; i < size; i++) {
                            rasql = defaultRasql;
                            //replace $collectionName with the coverage name from multipart axis iterator
                            rasql = rasql.replace(updateName, collectionNames.get(i));
                            stackUpdatedQueries.push(rasql);
                        }
                    } else {
                        // next multipart coverage iterator will use Rasql from stack to update it with coverage (collection) names.
                        while (!stackUpdatedQueries.isEmpty()) {
                            String rasqlTemplate = stackUpdatedQueries.pop();
                            for (int i = 0; i < size; i++) {
                                // from second multipartcoverage iterator, use Rasql queries in listRasqlQueries to create new query
                                rasql = rasqlTemplate.replace(updateName, collectionNames.get(i));
                                stackTmp.push(rasql);
                            }
                        }

                        // Add values to the first stack and continue to add new Rasql query by updating coverage names.
                        stackUpdatedQueries = (Stack<String>)stackTmp.clone();
                        stackTmp.clear();
                    }
                    isFirstIterator = false;
                }
            }
        }

        return stackUpdatedQueries;
    }

    // Check if in forClauseList has a coverageVariableName which is alias for multiple coverageName
    // e.g: $c -> (mr, rgb)
    public boolean isMultiPart() {
        // if $c -> (mr, rgb) then size > 1 and it is multipart
        for (ArrayList<String> coverageNames: this.coverageAliasRegistry.getCoverageMappings().values()) {
            if (coverageNames.size() > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * check if input for multipart is not wcps metadata result (e.g: identifier());
     */
    private void checkValid() {
        // Check if wcpsResult is not WcpsMetadataResult (as cannot return multipart for this kind of WCPS query)
        // it will create multiple WCPS queries and rerun to get metadata (but not support now)
        if ((this.isMultiPart()) && (this.wcpsResult instanceof WcpsMetadataResult)) {
            throw new MetaResultMultipartException();
        }
    }
}
