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

import java.util.Arrays;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;

/**
 * Translation node from wcps to rasql for unary pow(er) expressions. Example:  <code>
 * pow($c1, 2)
 * </code> translates to  <code>
 * pow(c1, 2)
 * </code>
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UnaryPowerExpressionHandler extends Handler {
    
    public UnaryPowerExpressionHandler() {
        
    }
    
    public UnaryPowerExpressionHandler create(Handler coverageExpressionHandler, Handler scalarExpressionHandler) {
        UnaryPowerExpressionHandler result = new UnaryPowerExpressionHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, scalarExpressionHandler));
        return result;
    }
    
    @Override
    public VisitorResult handle() throws PetascopeException {
        WcpsResult coverageExpression = (WcpsResult) this.getFirstChild().handle();
        WcpsResult scalarExpression = (WcpsResult) this.getSecondChild().handle();
        
        WcpsResult result = this.handle(coverageExpression, scalarExpression);
        return result;
    }

    public WcpsResult handle(WcpsResult coverageExpression, WcpsResult scalarExpression) {
        // NOTE: It is implemented same as UnaryBooleanExpression with case bit
        String template = TEMPLATE.replace("$coverageExp", coverageExpression.getRasql())
                                  .replace("$scalarExp", scalarExpression.getRasql());

        return new WcpsResult(coverageExpression.getMetadata(), template);
    }

    private final String TEMPLATE = "POW($coverageExp, $scalarExp)";
}
