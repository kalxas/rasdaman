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

import org.springframework.stereotype.Service;
import petascope.wcps.result.WcpsResult;

/**
 * Translation node from wcps to rasql for unary mod(er) expressions. Example:  <code>
 * mod($c1, 2)
 * </code> translates to  <code>
 * mod(c1, 2)
 * </code>
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class UnaryModExpressionHandler extends AbstractOperatorHandler {

    public WcpsResult handle(WcpsResult coverageExp, WcpsResult scalarExp) {
        String template = TEMPLATE.replace("$coverageExp", coverageExp.getRasql())
                                  .replace("$scalarExp", scalarExp.getRasql());

        return new WcpsResult(coverageExp.getMetadata(), template);
    }

    private final String TEMPLATE = "MOD($coverageExp, $scalarExp)";
}
