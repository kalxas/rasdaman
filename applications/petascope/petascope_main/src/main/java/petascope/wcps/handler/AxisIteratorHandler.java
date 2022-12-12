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
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsUtil;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.AxisIterator;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/** 
 * Class to handler WCPS:
 * 
 * coverageVariableName dimensionIntervalElement, e.g. $px X(0:20)
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AxisIteratorHandler extends Handler {
    
    public AxisIteratorHandler() {
        
    }
    
    public AxisIteratorHandler create(Handler axisIteratorNameHandler, 
                                    Handler axisNameHandler,
                                    Handler coverageExpressionLowerBoundHandler,
                                    Handler coverageExpressionUpperBoundHandler) {
        AxisIteratorHandler result = new AxisIteratorHandler();
        result.setChildren(Arrays.asList(axisIteratorNameHandler, axisNameHandler, 
                                        coverageExpressionLowerBoundHandler, coverageExpressionUpperBoundHandler));
        
        return result;
    }

    @Override
    public VisitorResult handle() throws PetascopeException {
        // e.g. $px X( 0:20 )
        
        // e.g. $px
        String axisIteratorName = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        // e.g. X
        String axisName = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        
        String gridLowerBound = ((WcpsResult)this.getThirdChild().handle()).getRasql();
        String gridUpperBound = ((WcpsResult)this.getFourthChild().handle()).getRasql();
        
        WcpsSubsetDimension subsetDimension = new WcpsTrimSubsetDimension(axisName, CrsUtil.GRID_CRS, gridLowerBound, gridUpperBound);
        
        AxisIterator axisIterator = new AxisIterator(axisIteratorName, axisName, subsetDimension, null);
        return axisIterator;
    }
    
}
