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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.handler;

import java.util.List;
import petascope.wcps2.metadata.service.CoverageAliasRegistry;
import petascope.wcps2.result.WcpsResult;

/**
 * Translation node from wcps to rasql for the for clause.
 * Example:
 * <code>
 * for $c1 in COL1
 * </code>
 * translates to
 * <code>
 * COL1 as c1
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ForClauseHandler {

    public static WcpsResult handle(String coverageIterator, List<String> coverageNames,
                                    CoverageAliasRegistry coverageAliasRegistry) {
        //add the mapping in the coverageRegistry
        for (String coverageName : coverageNames) {
            coverageAliasRegistry.addCoverageMapping(coverageIterator, coverageName);
        }
        String translatedCoverageIterator = coverageIterator;
        //if the coverageVariable starts with $, remove it to make it valid rasql
        if (coverageIterator.startsWith(COVERAGE_VARIABLE_PREFIX)) {
            translatedCoverageIterator = coverageIterator.replace(COVERAGE_VARIABLE_PREFIX, "");
        }

        // when coverageNames has size() > 0 then it is multipart and it will be handle in RasqlRewriteMultipartQueriesService
        // Then need to get only the first coverageName to create a rasql first.
        String template = "";
        if (coverageNames.size() > 1) {
            // Multipart query
            template = TEMPLATE.replace("$iterator", translatedCoverageIterator)
                       .replace("$collectionName", COLLECTION_NAME + "_" + translatedCoverageIterator);
        } else {
            template = TEMPLATE.replace("$iterator", translatedCoverageIterator)
                       .replace("$collectionName", coverageNames.get(0));
        }
        //metadata is loaded in the return clause, no meta needed here
        WcpsResult result = new WcpsResult(null, template);
        return result;
    }

    private final static String TEMPLATE = "$collectionName AS $iterator";
    private final static String COVERAGE_VARIABLE_PREFIX = "$";
    public final static String COLLECTION_NAME = "$collectionName";
}
