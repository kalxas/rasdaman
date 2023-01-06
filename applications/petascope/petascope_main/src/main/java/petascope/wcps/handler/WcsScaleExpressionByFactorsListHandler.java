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
 * Copyright 2003 - 2023 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import com.rasdaman.accesscontrol.service.AuthenticationService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.ras.RasUtil;
import petascope.wcps.exception.processing.ScaleValueLessThanZeroException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericSlicing;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.CollectionAliasRegistry;
import petascope.wcps.metadata.service.RasqlTranslationService;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.AbstractWcpsScaleDimension;
import petascope.wcps.subset_axis.model.WcpsScaleDimensionIntevalList;
import petascope.wcps.subset_axis.model.WcpsSliceScaleDimension;

/**
 * Class to translate a scale wcps expression into rasql  <code>
 *    scale($coverageExpression, {axis1($factor1), axis2($factor2), ...})
  * </code>
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WcsScaleExpressionByFactorsListHandler extends Handler {

    @Autowired
    private RasqlTranslationService rasqlTranslationService;
    @Autowired
    private ScaleExpressionByDimensionIntervalsHandler scaleExpressionByDimensionIntervalsHandler;
    @Autowired
    private CollectionAliasRegistry collectionAliasRegistry;
    
    public WcsScaleExpressionByFactorsListHandler() {
        
    }
    
    public WcsScaleExpressionByFactorsListHandler create(Handler coverageExpressionHandler, Handler scaleFactorHandler) {
        WcsScaleExpressionByFactorsListHandler result = new WcsScaleExpressionByFactorsListHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, scaleFactorHandler));
        
        result.rasqlTranslationService = this.rasqlTranslationService;
        result.scaleExpressionByDimensionIntervalsHandler = this.scaleExpressionByDimensionIntervalsHandler;
        result.collectionAliasRegistry = this.collectionAliasRegistry;
        
        return result;
        
    }
    
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpression = ((WcpsResult)this.getFirstChild().handle());
        
        WcpsScaleDimensionIntevalList scaleDimensionIntevalList = null;
        
        Object obj = this.getSecondChild().handle();
        if (obj instanceof WcpsResult) {
            // Scale by 1 factor
            List<AbstractWcpsScaleDimension> elementsList = new ArrayList<>();
            for (Axis axis : coverageExpression.getMetadata().getAxes()) {
                WcpsSliceScaleDimension element = new WcpsSliceScaleDimension(axis.getLabel(), ((WcpsResult)obj).getRasql());
                elementsList.add(element);
            }
            
            scaleDimensionIntevalList = new WcpsScaleDimensionIntevalList(elementsList);
        } else {
            // Scale by a list of axisLabel(factor)
            scaleDimensionIntevalList = (WcpsScaleDimensionIntevalList)this.getSecondChild().handle();
            
            for (Axis axis : coverageExpression.getMetadata().getAxes()) {
                boolean exist = false;
                for (AbstractWcpsScaleDimension element : scaleDimensionIntevalList.getIntervals()) {
                    if (CrsUtil.axisLabelsMatch(element.getAxisName(), axis.getLabel())) {
                        exist = true;
                        break;
                    }
                }
                
                if (!exist) {
                    // Other axes are not mentioned -> keep the same ration
                    scaleDimensionIntevalList.getIntervals().add(new WcpsSliceScaleDimension(axis.getLabel(), "1"));
                }
            }
        }
        
        Map<String, BigDecimal> axisScaleFactorsMap = new LinkedHashMap<>();
        
        // Store the processed scale factors, so no need to ask rasdaman multiple times if they are the same expression, e.g. scale(c, (3 + 5))
        Map<String, BigDecimal> resultsMapTmp = new LinkedHashMap<>();
        
        for (AbstractWcpsScaleDimension scaleDimension : scaleDimensionIntevalList.getIntervals()) {
            
            String scaleFactorStr = ((WcpsSliceScaleDimension)scaleDimension).getBound();
            BigDecimal scaleFactor = resultsMapTmp.get(scaleFactorStr);

            if (scaleFactor == null) {
                if (BigDecimalUtil.isNumber(scaleFactorStr)) {
                    scaleFactor = new BigDecimal(scaleFactorStr);
                } else {
                    // e.g. avg(c)
                    // NOTE: this is required, to get the correct scale factor by calculating it from rasdaman
                    // then to update the grid domains of the current coverage object c after scale(c, avg(c))
                    String query = "SELECT " + scaleFactorStr + " FROM " + this.collectionAliasRegistry.getFromClause();
                    String result = RasUtil.executeQueryToReturnString(query, ConfigManager.RASDAMAN_USER, ConfigManager.RASDAMAN_PASS);
                    scaleFactor = new BigDecimal(result);
                }
            }
            
            String axisLabel = scaleDimension.getAxisName();
            axisScaleFactorsMap.put(axisLabel, scaleFactor);
            
            resultsMapTmp.put(scaleFactorStr, scaleFactor);
        }
        
        WcpsResult result = this.handle(coverageExpression, axisScaleFactorsMap);
        return result;
    }

    private WcpsResult handle(WcpsResult coverageExpression, Map<String, BigDecimal> axisScaleFactorsMap) {
        // scale($coverageExpression, {axis1($factor1), axis2($factor2), ...})   
        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        List<Subset> subsets = new ArrayList<>();
        List<Pair> gridBoundAxes = new ArrayList();
        
        for (Map.Entry<String, BigDecimal> entry : axisScaleFactorsMap.entrySet()) {
            String axisLabel = entry.getKey();
            BigDecimal scaleFactor = entry.getValue();
            
            if (scaleFactor.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ScaleValueLessThanZeroException(axisLabel, scaleFactor.toPlainString());
            }

            Axis axis = metadata.getAxisByName(axisLabel);
            
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
