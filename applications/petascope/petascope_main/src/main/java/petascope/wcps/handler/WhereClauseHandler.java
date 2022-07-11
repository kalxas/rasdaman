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
 * Translation node from wcps to rasql for the where clause. Example:  <code>
 * WHERE c.red > 10
 * </code> translates to  <code>
 * WHERE c.red > 10
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WhereClauseHandler extends Handler {
    
    public WhereClauseHandler create(Handler coverageExpressionHandler) {
        WhereClauseHandler result = new WhereClauseHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler));
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpressionVisitorResult = (WcpsResult) this.getFirstChild().handle();
        WcpsResult result = this.handle(coverageExpressionVisitorResult);
        return result;
    }

    private WcpsResult handle(WcpsResult expression) {
        return new WcpsResult(expression.getMetadata(), TEMPLATE.replace("$booleanExpression", expression.getRasql()));
    }

    private final String TEMPLATE = " WHERE $booleanExpression ";

}
