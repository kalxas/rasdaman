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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import static petascope.wcps.handler.ForClauseListHandler.FROM;
import petascope.wcps.metadata.service.CoverageAliasRegistry;
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
public class WcpsQueryHandler {
    
    @Autowired
    private CoverageAliasRegistry coverageAliasRegistry;
    
    /**
     * Invoked in case collection names should be updated in the rasql FROM clause
     */
    public String getUpdatedForClauseListRasql() {
        String rasql = this.coverageAliasRegistry.getRasqlFromClause();
        if (!rasql.trim().isEmpty()) {
            rasql = FROM + " " + rasql;
        }
        return rasql;
    }

    public WcpsResult handle(WcpsResult forClauseList, WcpsResult whereClause, WcpsResult returnClause) throws PetascopeException {
        //SELECT c1+c2
        String rasql = returnClause.getRasql();

        //FROM cov1 as c1, cov2 as c2
        String updatedRasql = this.getUpdatedForClauseListRasql();
        rasql = rasql.concat(updatedRasql);
        
        //append where if exists
        if (whereClause != null) {
            rasql = rasql.concat(whereClause.getRasql());
        }

        returnClause.setRasql(rasql);
        return returnClause;
    }
}
