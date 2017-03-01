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

import petascope.wcps2.result.WcpsResult;

/**
 * Translation node from wcps to rasql for unary boolean expressions.
 * Example:
 * <code>
 * not($c1), bit($c1)
 * </code>
 * translates to
 * <code>
 * not(c1), bit(c1)
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class UnaryBooleanExpressionHandler {

    public static WcpsResult handle(WcpsResult coverageExp, WcpsResult scalarExp) {
        String template;
        //if realNumberConst exists, we deal with a bit operation
        if (scalarExp != null) {
            template = TEMPLATE_BIT.replace("$coverageExp", coverageExp.getRasql()).replace("$scalarExp", scalarExp.getRasql());
        } else {
            //not expression
            template = TEMPLATE_NOT.replace("$coverageExp", coverageExp.getRasql());
        }
        return new WcpsResult(coverageExp.getMetadata(), template);
    }

    private static final String TEMPLATE_NOT = "NOT($coverageExp)";
    private static final String TEMPLATE_BIT = "BIT($coverageExp, $scalarExp)";
}
