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
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;

/**
 * Translation class for boolean unary scalar expression. Example  <code>
 *     NOT(avg_cells(c) > 10)
 * </code> translates to  <code>
 *     not(avg_cells(c) > 10)
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BooleanUnaryScalarExpressionHandler extends Handler {
    
    public BooleanUnaryScalarExpressionHandler() {
        
    }
    
    public BooleanUnaryScalarExpressionHandler create(StringScalarHandler operatorHandler, Handler scalarExpressionHandler) {
        BooleanUnaryScalarExpressionHandler result = new BooleanUnaryScalarExpressionHandler();
        result.setChildren(Arrays.asList(operatorHandler, scalarExpressionHandler));
        return result;
    }
    
    @Override
    public VisitorResult handle() throws PetascopeException {
        String operator = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        WcpsResult scalarExpression = (WcpsResult)this.getSecondChild().handle();
        
        WcpsResult result = this.handle(operator, scalarExpression);
        return result;
    }

    public WcpsResult handle(String operator, WcpsResult scalarExpression) {
        return new WcpsResult(scalarExpression.getMetadata(), 
                              TEMPLATE.replace("$operand", operator)
                                      .replace("$scalarExpression", scalarExpression.getRasql()));
    }

    private final String TEMPLATE = "$operand($scalarExpression)";

}
