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
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.result.VisitorResult;
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
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ForClauseListHandler extends Handler {
    
    public ForClauseListHandler() {
        
    }
    
    public ForClauseListHandler create(List<Handler> childHandlers) {
        ForClauseListHandler result = new ForClauseListHandler();
        result.setChildren(childHandlers);
        return result;
    }
    
    
    public WcpsResult handle() throws PetascopeException {
        List<WcpsResult> temps = new ArrayList<>();
        for (Handler childHandler : this.getChildren()) {
            WcpsResult result = (WcpsResult) childHandler.handle();
            temps.add(result);
        }
        
        WcpsResult result = this.handle(temps);
        return result;
    }

    private WcpsResult handle(List<WcpsResult> forClauses) {
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
