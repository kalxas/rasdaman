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
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.DimensionIntervalList;

/**
Handler for this expression:

// coverageExpression LEFT_BRACKET dimensionPointList RIGHT_BRACKET
// e.g: c[Lat(0)]
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShorthandSliceHandler extends Handler {
    
    @Autowired
    private SubsetExpressionHandler subsetExpressionHandler;
    
    public ShorthandSliceHandler() {
        
    }
    
    public ShorthandSliceHandler create(Handler coverageExpressionHandler, Handler dimensionIntervalListHandler) {
        ShorthandSliceHandler result = new ShorthandSliceHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, dimensionIntervalListHandler));
        result.subsetExpressionHandler = subsetExpressionHandler;
        
        return result;
    }
    
    @Override
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpression = (WcpsResult) this.getFirstChild().handle();
        WcpsResult dimensionIntervalListExpression = (WcpsResult) this.getSecondChild().handle();
        return this.handle(coverageExpression, dimensionIntervalListExpression);
    }

    private WcpsResult handle(WcpsResult coverageExpression, VisitorResult dimensionIntervalListExpression) throws WCPSException, PetascopeException {
        // coverageExpression LEFT_BRACKET dimensionPointList RIGHT_BRACKET
        // e.g: c[Lat(0)]
        DimensionIntervalList dimensionIntervalList = (DimensionIntervalList)dimensionIntervalListExpression;
        WcpsResult wcpsResult = subsetExpressionHandler.handle(coverageExpression, dimensionIntervalList);
        return wcpsResult;
    }

}
