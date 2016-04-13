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
package petascope.wcps2.translator;

/**
 * Translation node from wcps to rasql.
 * Example:
 * <code>
 * for $c1 in cov1 for $c2 in cov 2 return encode($c1 + $c2, "csv")
 * </code>
 * translates to
 * <code>
 * SELECT csv(c1 + c2) FROM cov1 as c1, cov2 as c2
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class WcpsQuery extends IParseTreeNode {

    public WcpsQuery(IParseTreeNode forClauseList, IParseTreeNode whereClause, IParseTreeNode returnClause) {
        this.forClauseList = forClauseList;
        this.whereClause = whereClause;
        this.returnClause = returnClause;
        addChild(forClauseList);
        if (whereClause != null) addChild(whereClause);
        addChild(returnClause);
    }

    @Override
    public String toRasql() {
        //SELECT c1+c2
        String template = this.returnClause.toRasql();
        //FROM cov1 as c1, cov2 as c2
        template = template.concat(forClauseList.toRasql());
        //append where if exists
        if (this.whereClause != null) {
            template = template.concat(this.whereClause.toRasql());
        }
        return template;
    }

    private IParseTreeNode forClauseList;
    private IParseTreeNode whereClause;
    private IParseTreeNode returnClause;
}
