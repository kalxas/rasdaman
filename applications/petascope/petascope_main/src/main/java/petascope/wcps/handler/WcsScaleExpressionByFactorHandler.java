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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.util.BigDecimalUtil;
import petascope.wcps.exception.processing.ScaleValueLessThanZeroException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericSlicing;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.RasqlTranslationService;
import petascope.wcps.result.WcpsResult;

/**
 * Class to translate a scale wcps expression into rasql  <code>
 *    SCALE_FACTOR($coverageExpression, $factor)
 * NOTE: factor = 0.5 means every axis of coverage will multiple by 0.5 in grid pixels
 * </code>
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WcsScaleExpressionByFactorHandler extends Handler {

    @Autowired
    private RasqlTranslationService rasqlTranslationService;
    @Autowired
    private ScaleExpressionByDimensionIntervalsHandler scaleExpressionByDimensionIntervalsHandler;
    
    public WcsScaleExpressionByFactorHandler() {
        
    }
    
    public WcsScaleExpressionByFactorHandler create(Handler coverageExpressionHandler, Handler scaleFactorStringHandler) {
        WcsScaleExpressionByFactorHandler result = new WcsScaleExpressionByFactorHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, scaleFactorStringHandler));
        
        result.rasqlTranslationService = this.rasqlTranslationService;
        result.scaleExpressionByDimensionIntervalsHandler = this.scaleExpressionByDimensionIntervalsHandler;
        
        return result;
        
    }
    
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpression = ((WcpsResult)this.getFirstChild().handle());
        BigDecimal scaleFactor = new BigDecimal(((WcpsResult)this.getSecondChild().handle()).getRasql());
        
        WcpsResult result = this.handle(coverageExpression, scaleFactor);
        return result;
    }

    private WcpsResult handle(WcpsResult coverageExpression, BigDecimal scaleFactor) {
        // SCALE LEFT_PARENTHESIS
        //        coverageExpression COMMA number
        // RIGHT_PARENTHESIS
        // e.g: scale(c[t(0)], 2.5) with c is 3D coverage which means 2D output will be 
        // downscaled to 2.5 by each dimension (e.g: grid pixel is: 100 then the result is 100 / 2.5)        

        if (scaleFactor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ScaleValueLessThanZeroException("all axes", scaleFactor.toPlainString());
        }

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        List<Subset> subsets = new ArrayList<>();
        // first apply the scale factor on all the grid bounds
        // e.g: 100 * 2.5 or 100 * 0.5
        List<Pair> gridBoundAxes = new ArrayList();
        for (Axis axis : metadata.getAxes()) {
            
            NumericTrimming gridNumericTrimming = new NumericTrimming(new BigDecimal(axis.getGridBounds().getLowerLimit().toPlainString()),
                                                                      new BigDecimal(axis.getGridBounds().getUpperLimit().toPlainString()));
            Pair<String, NumericTrimming> gridPair = new Pair(axis.getLabel(), gridNumericTrimming);
            gridBoundAxes.add(gridPair);
            
            BigDecimal scaledLowerBound = BigDecimalUtil.multiple(axis.getGridBounds().getLowerLimit(), scaleFactor);
            BigDecimal scaledUpperBound = BigDecimalUtil.multiple(axis.getGridBounds().getUpperLimit(), scaleFactor);
            NumericSubset numericSubset = null;
            if (axis.getGridBounds() instanceof NumericSlicing) {
                numericSubset = new NumericSlicing(new BigDecimal(scaledLowerBound.intValue()));
            } else {
                numericSubset = new NumericTrimming(new BigDecimal(scaledLowerBound.intValue()),
                        new BigDecimal(scaledUpperBound.intValue()));
            }
            axis.setGridBounds(numericSubset);

            subsets.add(new Subset(numericSubset, null, axis.getLabel()));
        }

        // it will not get all the axis to build the intervals in case of (extend() and scale())
        String domainIntervals = rasqlTranslationService.constructSpecificRasqlDomain(metadata.getSortedAxesByGridOrder(), subsets);
        String rasql = TEMPLATE.replace("$coverage", coverageExpression.getRasql())
                               .replace("$intervalList", domainIntervals);
        
        this.scaleExpressionByDimensionIntervalsHandler.applyScaleOnIrregularAxes(metadata, gridBoundAxes);

        return new WcpsResult(metadata, rasql);
    }

    //in case we will need to handle scale with a factor, use a method such as below
    //public  WcpsResult handle(WcpsResult coverageExpression, BigDecimal scaleFactor)
    private final String TEMPLATE = "SCALE($coverage, [$intervalList])";
}
