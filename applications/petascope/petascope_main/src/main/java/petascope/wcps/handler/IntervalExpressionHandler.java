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
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.util.StringUtil;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.IntervalExpression;
import petascope.wcps.subset_axis.model.WcpsSliceSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/**
 * Handler for expression
 // scalarExpression COLON scalarExpression  (e.g: 5:10)
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class IntervalExpressionHandler extends Handler {
    
    public IntervalExpressionHandler() {
        
    }
    
    public IntervalExpressionHandler create(StringScalarHandler lowerBoundHandler, StringScalarHandler upperBoundHandler) {
        IntervalExpressionHandler result = new IntervalExpressionHandler();
        result.setChildren(Arrays.asList(lowerBoundHandler, upperBoundHandler));
        
        return result;
    }

    @Override
    public VisitorResult handle() throws PetascopeException {
        String lowerBound = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        String upperBound = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        
        VisitorResult result = this.handle(lowerBound, upperBound);
        return result;
    }
    
    private VisitorResult handle(String lowerBound, String upperBound) {
        IntervalExpression intervalExpression = new IntervalExpression(lowerBound, upperBound);
        return intervalExpression;
    }
    
}
