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
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.DimensionIntervalList;

/**
Handler for this expression:

//  coverageExpression LEFT_BRACKET letClauseDimensionIntervalList RIGHT_BRACKET
// e.g: c[$a] with $a := [Lat(0:20), Long(0:30)]
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShortHandSubsetWithLetClauseVariableHandler extends Handler {
    
    @Autowired
    private SubsetExpressionHandler subsetExpressionHandler;
    
    public ShortHandSubsetWithLetClauseVariableHandler() {
        
    }
    
    public ShortHandSubsetWithLetClauseVariableHandler create(Handler coverageVariableNameHandler, Handler letClauseVariableHandler) {
        ShortHandSubsetWithLetClauseVariableHandler result = new ShortHandSubsetWithLetClauseVariableHandler();
        result.setChildren(Arrays.asList(coverageVariableNameHandler, letClauseVariableHandler));
        result.subsetExpressionHandler = subsetExpressionHandler;
        
        return result;
    }
    
    @Override
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageVariableNameExpression = (WcpsResult)this.getFirstChild().handle();
        WcpsResult letClauseVariableNameExpression = (WcpsResult) this.getSecondChild().handle();
        return this.handle(coverageVariableNameExpression, letClauseVariableNameExpression);
    }

    private WcpsResult handle(WcpsResult coverageVariableNameExpression, WcpsResult letClauseVariableNameExpression) throws WCPSException, PetascopeException {
        //  coverageExpression LEFT_BRACKET dimensionIntervalList RIGHT_BRACKET
        // e.g: c[Lat(0:20), Long(0:30)] - Trim
        WcpsResult wcpsResult = subsetExpressionHandler.handle(coverageVariableNameExpression, letClauseVariableNameExpression.getDimensionIntervalList());
        return wcpsResult;
    }

}
