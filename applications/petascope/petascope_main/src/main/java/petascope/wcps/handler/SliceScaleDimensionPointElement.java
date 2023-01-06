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

/**
 Handler for expression:
// axisName LEFT_PARENTHESIS   number   RIGHT_PARENTHESIS
// e.g: i(0.5) and is used for scaleaxes, scalesize expression, e.g: scale(c, [i(0.5)])
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SliceScaleDimensionPointElement extends Handler {
    
    public SliceScaleDimensionPointElement() {
        
    }
    
    public SliceScaleDimensionPointElement create(StringScalarHandler axisLabelHandler, Handler scaleFactorHandler) {
        SliceScaleDimensionPointElement result = new SliceScaleDimensionPointElement();
        result.setChildren(Arrays.asList(axisLabelHandler, scaleFactorHandler));
        
        return result;
    }

    @Override
    public VisitorResult handle() throws PetascopeException {
        String axisLabel = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        String scaleFactor = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        
        VisitorResult result = this.handle(axisLabel, scaleFactor);
        return result;
    }
    
    private VisitorResult handle(String axisLabel, String scaleFactor) throws PetascopeException {
        // axisName LEFT_PARENTHESIS   number   RIGHT_PARENTHESIS
        // e.g: i(0.5) and is used for scaleaxes, scalesize expression, e.g: scale(c, [i(0.5)])
        
        AbstractWcpsScaleDimension scaleAxesDimension = new WcpsSliceScaleDimension(axisLabel, scaleFactor);
        return scaleAxesDimension;
    }
    
}
