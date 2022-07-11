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

import java.util.Arrays;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.result.WcpsResult;

/**
 * Translation class for Boolean numerical comparisons.  <code>
 * avg($c) > 1
 * </code> translates to  <code>
 * avg_cells(c) > 1
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BooleanNumericalComparisonScalarHandler extends Handler {
    
    public BooleanNumericalComparisonScalarHandler() {
        
    }
    
    public BooleanNumericalComparisonScalarHandler create(Handler leftCoverageExpressionHandler, 
                                                          Handler operatorHandler,
                                                          Handler rightCoverageExpressionHandler) {
        BooleanNumericalComparisonScalarHandler result = new BooleanNumericalComparisonScalarHandler();
        result.setChildren(Arrays.asList(leftCoverageExpressionHandler, operatorHandler, rightCoverageExpressionHandler));
        
        return result;
    }
    
    @Override
    public WcpsResult handle() throws PetascopeException {
        WcpsResult leftCoverageExpression = (WcpsResult) this.getFirstChild().handle();
        String operator = ((WcpsResult) this.getSecondChild().handle()).getRasql();
        WcpsResult rightCoverageExpression = (WcpsResult) this.getThirdChild().handle();
        
        WcpsResult result = this.handle(leftCoverageExpression, operator, rightCoverageExpression);
        return result;
    }

    public WcpsResult handle(WcpsResult leftCoverageExpression, String operator, WcpsResult rightCoverageExpression) {
        return new WcpsResult(null, 
                TEMPLATE.replace("$leftOperand", leftCoverageExpression.getRasql())
                        .replace("$operator", operator)
                        .replace("$rightOperand", rightCoverageExpression.getRasql()));
    }

    private final String TEMPLATE = " $leftOperand $operator $rightOperand ";
}
