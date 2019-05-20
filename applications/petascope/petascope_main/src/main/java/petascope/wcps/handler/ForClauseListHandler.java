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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import petascope.wcps.result.WcpsResult;

/**
 * Translation node from wcps coverage list to rasql for the FOR clause.
 * Example:  <code>
 * for $c1 in COL1
 * for $c2 in COL2
 * for $c3 in COL3
 * </code> translates to  <code>
 * FROM COL1 as c1, COL2 as c2, COL3 as c3
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class ForClauseListHandler {

    public WcpsResult handle(List<WcpsResult> forClauses) {
        List<String> rasqls = new ArrayList();
        for (WcpsResult forClause : forClauses) {
            rasqls.add(forClause.getRasql());
        }
        String template = TEMPLATE.replace("$forClausesList", StringUtils.join(rasqls, FROM_CLAUSE_SEPARATOR));
        WcpsResult result = new WcpsResult(null, template);
        return result;
    }
    
    public static final String FROM = "FROM";

    private static final String TEMPLATE = FROM + " $forClausesList";
    private static final String FROM_CLAUSE_SEPARATOR = ",";
}
