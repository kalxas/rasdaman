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
import petascope.wcps.result.WcpsResult;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;

/**
 * Class to translate a reduce boolean expression to rasql  <code>
 * some($c > 1)
 * </code>
 * <p/>
 * translates to
 * <p/>
 * <code>
 * some_cells(c > 1)
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ReduceExpressionHandler extends Handler {
    
    public ReduceExpressionHandler() {
        
    }
    
    public ReduceExpressionHandler create(StringScalarHandler operatorHandler, Handler reduceExpressionHandler) {
        ReduceExpressionHandler result = new ReduceExpressionHandler();
        result.setChildren(Arrays.asList(operatorHandler, reduceExpressionHandler));
        return result;
    }
    
    @Override
    public WcpsResult handle() throws PetascopeException {
        String operator = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        WcpsResult reduceExpression = (WcpsResult)this.getSecondChild().handle();
        
        WcpsResult result = this.handle(operator, reduceExpression);
        return result;
    }

    private WcpsResult handle(String operator, WcpsResult reduceExpression) {
        return new WcpsResult(null, 
                        TEMPLATE.replace("$reduceOperation", operationTranslator.get(operator.toLowerCase()))
                                .replace("$reduceParameter", reduceExpression.getRasql()));
    }

    private final String TEMPLATE = " $reduceOperation($reduceParameter) ";
    private final Map<String, String> operationTranslator = new HashMap<>();

    {
        operationTranslator.put("some", "some_cells");
        operationTranslator.put("all", "all_cells");
        operationTranslator.put("avg", "avg_cells");
        operationTranslator.put("add", "add_cells");
        operationTranslator.put("sum", "add_cells");
        operationTranslator.put("min", "min_cells");
        operationTranslator.put("max", "max_cells");
        operationTranslator.put("count", "count_cells");
    }
}
