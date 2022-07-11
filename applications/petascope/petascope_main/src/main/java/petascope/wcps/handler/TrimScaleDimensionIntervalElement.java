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
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.AbstractWcpsScaleDimension;
import petascope.wcps.subset_axis.model.WcpsSliceScaleDimension;
import petascope.wcps.subset_axis.model.WcpsTrimScaleDimension;

/**
 Handler for expression:
// axisName LEFT_PARENTHESIS   number   RIGHT_PARENTHESIS
// e.g: i(20:30) and is used for scaleextent expression, e.g: scale(c, [i(20:30)])
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TrimScaleDimensionIntervalElement extends Handler {
    
    public TrimScaleDimensionIntervalElement() {
        
    }
    
    public TrimScaleDimensionIntervalElement create(StringScalarHandler axisLabelHandler,
                                                    StringScalarHandler lowerBoundHandler, StringScalarHandler upperBoundhandler) {
        TrimScaleDimensionIntervalElement result = new TrimScaleDimensionIntervalElement();
        result.setChildren(Arrays.asList(axisLabelHandler, lowerBoundHandler, upperBoundhandler));
        
        return result;
    }

    @Override
    public VisitorResult handle() throws PetascopeException {
        String axisLabel = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        String lowerBound = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        String upperBound = ((WcpsResult)this.getThirdChild().handle()).getRasql();
        
        VisitorResult result = this.handle(axisLabel, lowerBound, upperBound);
        return result;
    }
    
    private VisitorResult handle(String axisLabel, String lowerBound, String upperBound) throws PetascopeException {
        // axisName LEFT_PARENTHESIS   number   RIGHT_PARENTHESIS
        // e.g: i(20:30) and is used for scaleextent expression, e.g: scale(c, [i(20:30)])
        AbstractWcpsScaleDimension scaleAxesDimension = new WcpsTrimScaleDimension(axisLabel, 
                                                                        lowerBound,
                                                                        upperBound);
        
        return scaleAxesDimension;
    }
    
}
