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
 * Translator class for *coverageExpression is null*.
 * e.g: for c in (test_mr) return encode(c is null, "csv")
 * to rasql:
 * select encode(c is null, "csv") from test_mr as c.
 * 
 * The same is applied to is not null.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CoverageIsNullHandler extends Handler {
    
    public CoverageIsNullHandler() {
        
    }
    
    public CoverageIsNullHandler create(Handler coverageExpressionHandler, StringScalarHandler booleanValueHandler) {
        CoverageIsNullHandler result = new CoverageIsNullHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, booleanValueHandler));
        return result;
    }
    
    @Override
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpression = (WcpsResult) this.getFirstChild().handle();
        boolean isNull = Boolean.valueOf(((WcpsResult) this.getSecondChild().handle()).getRasql());
        
        WcpsResult result = this.handle(coverageExpression, isNull);
        return result;
    }    

    private WcpsResult handle(WcpsResult coverageExpression, boolean isNull) {
        String rasqlResult = null;
        if (isNull) {
            rasqlResult = coverageExpression.getRasql() + " is null ";
        } else {
            rasqlResult = coverageExpression.getRasql() + " is not null ";
        }
        
        WcpsResult wcpsResult = new WcpsResult(coverageExpression.getMetadata(), rasqlResult);
        
        return wcpsResult;
    }

}