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
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.WcpsSliceSubsetDimension;

/**
 Handler for expression:
// axisName (COLON crsName)?  LEFT_PARENTHESIS   coverageExpression   RIGHT_PARENTHESIS
// e.g: i(0)
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SliceDimensionIntervalElementHandler extends Handler {
    
    public SliceDimensionIntervalElementHandler() {
        
    }
    
    public SliceDimensionIntervalElementHandler create(StringScalarHandler axisNameHandler, StringScalarHandler crsHandler,
                                                       Handler coverageExpressionHandler) {
        SliceDimensionIntervalElementHandler result = new SliceDimensionIntervalElementHandler();
        result.setChildren(Arrays.asList(axisNameHandler, crsHandler, coverageExpressionHandler));
        
        return result;
    }

    @Override
    public VisitorResult handle() throws PetascopeException {
        String axisName = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        String crs = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        WcpsResult coverageExpression = (WcpsResult)this.getThirdChild().handle();
        
        VisitorResult result = this.handle(axisName, crs, coverageExpression);
        return result;
    }
    
    private VisitorResult handle(String axisName, String crs, WcpsResult coverageExpression) throws PetascopeException {
        // axisName (COLON crsName)?  LEFT_PARENTHESIS   coverageExpression   RIGHT_PARENTHESIS
        // e.g: i(0)
        
        String bound = coverageExpression.getRasql();
        WcpsSliceSubsetDimension result = null;
  
        result = new WcpsSliceSubsetDimension(axisName, crs, bound);
        return result;
    }
    
}
