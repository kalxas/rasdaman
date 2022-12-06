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
 * Translation node from wcps unary arithmetic expression to rasql Example:  <code>
 * abs($c1)
 * </code> translates to  <code>
 * abs(c1)
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UnaryArithmeticExpressionHandler extends Handler {
    
    public UnaryArithmeticExpressionHandler() {
        
    }
    
    public UnaryArithmeticExpressionHandler create(StringScalarHandler operatorHandler, Handler coverageExpressionHandler, 
                                                    StringScalarHandler leftParenthesis,
                                                    StringScalarHandler rightParenthesis) {
        UnaryArithmeticExpressionHandler result = new UnaryArithmeticExpressionHandler();
        result.setChildren(Arrays.asList(operatorHandler, coverageExpressionHandler, leftParenthesis, rightParenthesis));
                
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        String operator = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        WcpsResult coverageExpression = ((WcpsResult)this.getSecondChild().handle());
        String leftParenthesis = null;
        String rightParenthesis = null;
        
        if (this.getThirdChild() != null) {
            leftParenthesis = ((WcpsResult)this.getThirdChild().handle()).getRasql();
        }
        if (this.getFourthChild()!= null) {
            rightParenthesis = ((WcpsResult)this.getFourthChild().handle()).getRasql();
        }
        
        WcpsResult result = this.handle(operator, coverageExpression, leftParenthesis, rightParenthesis);
        return result;
    }

    private WcpsResult handle(String operator, WcpsResult coverageExpression, String leftParenthesis, String rightParenthesis) {
        String template = TEMPLATE.replace("$coverage", coverageExpression.getRasql());
        
        // real and imaginary translate to postfix operations in rasql
        // yielding .re and .im
        if (operator.toLowerCase().equals(POST_REAL) || operator.toLowerCase().equals(POST_IMAGINARY)) {
            template = template.replace("$preOperator", "").replace("$postOperator", "." + operator);
        } else {
            if (leftParenthesis != null) {
                template = template.replace("$preOperator", operator + "(").replace("$postOperator", ")");
            } else {
                template = operator + coverageExpression.getRasql();
            }
        }
        
        return new WcpsResult(coverageExpression.getMetadata(), template);
    }

    private final String TEMPLATE = "$preOperator $coverage $postOperator";
    private final String POST_REAL = "re";
    private final String POST_IMAGINARY = "im";
}
