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
import petascope.exceptions.PetascopeException;
import petascope.util.StringUtil;
import petascope.wcps.exception.processing.IncompatibleAxesNumberException;
import static petascope.wcps.handler.AbstractOperatorHandler.checkOperandIsCoverage;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;

/**
 * Class to translate a scale wcps expression into rasql  <code>
 *    SCALE($coverageExpression, [$dimensionIntervalList])
 *    e.g: scale(c[t(0)], { imageCrsdomain(Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)) })
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ScaleExpressionByImageCrsDomainHandler extends Handler {
    
    public static final String OPERATOR = "scale";
    
    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;
    @Autowired
    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslatorService;
    
    public ScaleExpressionByImageCrsDomainHandler() {
        
    }
    
    public ScaleExpressionByImageCrsDomainHandler create(Handler coverageExpressionHandler, Handler domainIntervalsHandler) {
        ScaleExpressionByImageCrsDomainHandler result = new ScaleExpressionByImageCrsDomainHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, domainIntervalsHandler));
        
        result.wcpsCoverageMetadataService = this.wcpsCoverageMetadataService;
        result.wcpsCoverageMetadataTranslatorService = this.wcpsCoverageMetadataTranslatorService;
        
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpresisonResult = (WcpsResult)this.getFirstChild().handle();
        WcpsMetadataResult domainIntervalsResult =  (WcpsMetadataResult)this.getSecondChild().handle();
        
        WcpsResult result = this.handle(coverageExpresisonResult, domainIntervalsResult);
        return result;
    }

    private WcpsResult handle(WcpsResult coverageExpression, WcpsMetadataResult domainIntervalsResult) throws PetascopeException {
        checkOperandIsCoverage(coverageExpression, OPERATOR);
        
        String dimensionIntervalList = StringUtil.stripParentheses(domainIntervalsResult.getResult());

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();

        List<NumericTrimming> gridBounds = new ArrayList<>();
        // e.g: imageCrsdomain(c) returns 0:30,0:40,0:60
        String[] values = dimensionIntervalList.split(",");
        List<Axis> axes = metadata.getSortedAxesByGridOrder();

        if (axes.size() != values.length) {
            throw new IncompatibleAxesNumberException(metadata.getCoverageName(), axes.size(), values.length);
        }
        
        for (String value : values) {
            String lowerValue = value.split(":")[0];
            String upperValue = value.split(":")[1];
            gridBounds.add(new NumericTrimming(new BigDecimal(lowerValue), new BigDecimal(upperValue)));
        }
        
        List<Subset> numericSubsets = new ArrayList<>();        
        for (int i = 0; i < axes.size(); i++) {
            Axis axis = axes.get(i);
            axis.setGridBounds(gridBounds.get(i));
            
            // Also recalculate for axis's geo resolution as grid domain has changed
            this.wcpsCoverageMetadataService.updateGeoResolutionByGridBound(axis);
            
            NumericTrimming numericTrimming = new NumericTrimming(axis.getGridBounds().getLowerLimit(), axis.getGridBounds().getUpperLimit());
            Subset numericSubset = new Subset(numericTrimming, axis.getNativeCrsUri(), axis.getLabel());
            numericSubsets.add(numericSubset);
        }
        
        // scale(coverageExpression, {domainIntervals})
        // it will not get all the axis to build the intervals in case of (extend() and scale())
        String rasql = TEMPLATE.replace("$coverage", coverageExpression.getRasql());
        coverageExpression.setRasql(rasql);
        
        // Only for 2D XY coverage imported with downscaled collections
        this.wcpsCoverageMetadataTranslatorService.applyDownscaledLevelOnXYGridAxesForScale(coverageExpression, metadata, numericSubsets);
        
        rasql = coverageExpression.getRasql().replace("$intervalList", dimensionIntervalList);
        coverageExpression.setRasql(rasql);

        return new WcpsResult(metadata, coverageExpression.getRasql());
    }

    //in case we will need to handle scale with a factor, use a method such as below
    //public  WcpsResult handle(WcpsResult coverageExpression, BigDecimal scaleFactor)
    private final String TEMPLATE = "SCALE($coverage, [$intervalList])";
}
