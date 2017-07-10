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
package petascope.wcps2.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps2.result.WcpsResult;

/**
 * Translation class for boolean coverage expression in switch case. Example  <code>
 *     switch case c > 1000 return
 *            case 100 < c return
 *            case 0 = c return
 *            case 1 > 0 return case c > c return
 * </code> translates to  <code>
 *     (c)>(1000)
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class BooleanSwitchCaseCoverageExpressionHandler {

    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;

    public WcpsResult handle(WcpsResult leftCoverageExpr, String operand, WcpsResult rightCoverageExpr) {
        String leftCoverageExprStr = leftCoverageExpr.getRasql();
        String rightCoverageExprStr = rightCoverageExpr.getRasql();

        String rasql = TEMPLATE.replace("$leftCoverageExpr", leftCoverageExprStr)
                .replace("$operand", operand)
                .replace("$rightCoverageExpr", rightCoverageExprStr);

        //create the resulting metadata from both of coverageExpression (choose 1 of 2)
        WcpsCoverageMetadata metadata = wcpsCoverageMetadataService.getResultingMetadata(leftCoverageExpr.getMetadata(),
                rightCoverageExpr.getMetadata());

        return new WcpsResult(metadata, rasql);
    }

    private final String TEMPLATE = "$leftCoverageExpr $operand $rightCoverageExpr";
}
