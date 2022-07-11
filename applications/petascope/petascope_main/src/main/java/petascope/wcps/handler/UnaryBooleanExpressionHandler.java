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
 * Translation node from wcps to rasql for unary boolean expressions. Example:  <code>
 * not($c1), bit($c1)
 * </code> translates to  <code>
 * not(c1), bit(c1)
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UnaryBooleanExpressionHandler extends Handler {
    
    public UnaryBooleanExpressionHandler() {
        
    }
    
    public UnaryBooleanExpressionHandler create(Handler coverageExpressionHandler, Handler scalarExpressionHandler) {
        UnaryBooleanExpressionHandler result = new UnaryBooleanExpressionHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, scalarExpressionHandler));
        
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpression = (WcpsResult) this.getFirstChild().handle();
        WcpsResult scalarExpression = null;
        if (this.getSecondChild() != null) {
            scalarExpression = (WcpsResult) this.getSecondChild().handle();
        }
        
        WcpsResult result = this.handle(coverageExpression, scalarExpression);
        return result;
    }

    private WcpsResult handle(WcpsResult coverageExpression, WcpsResult scalarExpression) {
        String template;
        //if realNumberConst exists, we deal with a bit operation
        if (scalarExpression != null) {
            template = TEMPLATE_BIT.replace("$coverageExp", coverageExpression.getRasql()).replace("$scalarExp", scalarExpression.getRasql());
        } else {
            //not expression
            template = TEMPLATE_NOT.replace("$coverageExp", coverageExpression.getRasql());
        }
        return new WcpsResult(coverageExpression.getMetadata(), template);
    }

    private final String TEMPLATE_NOT = "NOT($coverageExp)";
    private final String TEMPLATE_BIT = "BIT($coverageExp, $scalarExp)";
}
