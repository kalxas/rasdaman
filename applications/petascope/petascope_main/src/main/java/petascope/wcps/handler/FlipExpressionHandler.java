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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsUtil;
import petascope.wcps.exception.processing.CoverageAxisNotFoundExeption;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.IrregularAxis;
import petascope.wcps.result.WcpsResult;

/**
 *
 * Handler for the flip expression to flip values along an axis.
 * e.g. FLIP $c + 30 ALONG ansi
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class FlipExpressionHandler extends Handler {
    
    // e.g: FLIP ... ALONG ...
    private final String COVERAGE_EXPRESSION_TEMPLATE = "$coverageExpression";
    private final String AXIS_LABEL_INDEX_TEMPLATE = "$axisLabelIndex";
    
    private final String RASQL_TEMPLATE = "FLIP " + COVERAGE_EXPRESSION_TEMPLATE + " ALONG " + AXIS_LABEL_INDEX_TEMPLATE;
    
    public FlipExpressionHandler() {
        
    }
    
    public FlipExpressionHandler create(Handler coverageExpressionHandler, StringScalarHandler axisLabelHandler) {
        FlipExpressionHandler result = new FlipExpressionHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, axisLabelHandler));
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpression = (WcpsResult) this.getFirstChild().handle();
        // e.g. Lat
        String axisLabel = ((WcpsResult) this.getSecondChild().handle()).getRasql();
        int axisLabelGridIndex = -1;
        int i = 0;
        
        for (Axis axis : coverageExpression.getMetadata().getSortedAxesByGridOrder()) {
            if (CrsUtil.axisLabelsMatch(axis.getLabel(), axisLabel)) {
                // NOTE: here it needs to use grid order not CRS order
                axisLabelGridIndex = i;
                break;
            }
            
            i++;
        }
        
        if (axisLabelGridIndex == -1) {
            throw new CoverageAxisNotFoundExeption(axisLabel);
        }
        
        WcpsResult result = this.handle(coverageExpression, axisLabel, axisLabelGridIndex);
        return result;
    }

    /**
     * Handle the flip operator
     */
    private WcpsResult handle(WcpsResult coverageExpression, String axisLabel, int axisLabelGridIndex) {
        WcpsResult result = coverageExpression;
        
        Axis axis = coverageExpression.getMetadata().getAxisByName(axisLabel);
        
        // original bounds
        BigDecimal originalGeoLowerBound = axis.getOriginalGeoBounds().getLowerLimit();
        BigDecimal originalGeoUpperBound = axis.getOriginalGeoBounds().getUpperLimit();
        axis.getOriginalGeoBounds().setLowerLimit(originalGeoUpperBound);
        axis.getOriginalGeoBounds().setUpperLimit(originalGeoLowerBound);
        
        // subset bounds
        BigDecimal geoLowerBound = axis.getGeoBounds().getLowerLimit();
        BigDecimal geoUpperBound = axis.getGeoBounds().getUpperLimit();
        
        axis.getGeoBounds().setLowerLimit(geoUpperBound);
        axis.getGeoBounds().setUpperLimit(geoLowerBound);
        
        if (axis instanceof IrregularAxis) {
            IrregularAxis irregularAxis = (IrregularAxis)axis;
            // Reverse the order of coefficients as well
            Collections.reverse(irregularAxis.getOriginalDirectPositions());
            Collections.reverse(irregularAxis.getDirectPositions());
        }
        
        String rasql = this.RASQL_TEMPLATE.replace(COVERAGE_EXPRESSION_TEMPLATE, coverageExpression.getRasql())
                                          .replace(AXIS_LABEL_INDEX_TEMPLATE, String.valueOf(axisLabelGridIndex));
        result.setRasql(rasql);
        
        return result;
    }
}
