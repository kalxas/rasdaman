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
import petascope.util.CrsUtil;
import petascope.util.StringUtil;
import petascope.wcps.exception.processing.IncompatibleAxesNumberException;
import static petascope.wcps.handler.AbstractOperatorHandler.checkOperandIsCoverage;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/**
 * Class to translate a scale wcps expression into rasql  <code>
 *    EXTEND($coverageExpression, [$dimensionIntervalList])
 *    e.g: extend(c[t(0)], { imageCrsdomain(Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)) })
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ExtendExpressionByImageCrsDomainHandler extends Handler {
    
    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;
    
    public static final String OPERATOR = "extend";
    
    public ExtendExpressionByImageCrsDomainHandler() {
        
    }
    
    public ExtendExpressionByImageCrsDomainHandler create(Handler coverageExpressionHandler, Handler domainExpressionHandler) {
        ExtendExpressionByImageCrsDomainHandler result = new ExtendExpressionByImageCrsDomainHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, domainExpressionHandler));
        result.wcpsCoverageMetadataService = wcpsCoverageMetadataService;
        
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpression = (WcpsResult) this.getFirstChild().handle();
        WcpsMetadataResult wcpsMetadataResult = ((WcpsMetadataResult)this.getSecondChild().handle());
        
        WcpsResult result = this.handle(coverageExpression, wcpsMetadataResult);
        return result;
    }

    private WcpsResult handle(WcpsResult coverageExpression, 
                              WcpsMetadataResult wcpsMetadataResult) throws PetascopeException {
        
        checkOperandIsCoverage(coverageExpression, OPERATOR);  

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        // scale(coverageExpression, {domainIntervals})

        List<WcpsSubsetDimension> subsetDimensions = new ArrayList<>();
        List<Subset> numericSubsets = new ArrayList<>();
        
        List<Axis> axes = metadata.getSortedAxesByGridOrder();
        String dimensionIntervalList = StringUtil.stripParentheses(wcpsMetadataResult.getResult());
        
        
        // e.g: imageCrsdomain(c) returns 0:30,0:40,0:60
        String[] values = dimensionIntervalList.split(",");
        
        if (axes.size() != values.length) {
            throw new IncompatibleAxesNumberException(metadata.getCoverageName(), axes.size(), values.length);
        }
        
        for (int i = 0; i < axes.size(); i++) {
            String axisLabel = axes.get(i).getLabel();
            String lowerValue = values[i].split(":")[0];
            String upperValue = values[i].split(":")[1];
            NumericTrimming numericTrimming = new NumericTrimming(new BigDecimal(lowerValue), new BigDecimal(upperValue));
            Subset subset = new Subset(numericTrimming, CrsUtil.GRID_CRS, axisLabel);
            numericSubsets.add(subset);
            
            subsetDimensions.add(new WcpsTrimSubsetDimension(axisLabel, CrsUtil.GRID_CRS, lowerValue, upperValue));
        }
        
        // NOTE: from WCPS 1.0 standard: In this sense the extendExpr is a generalization of the trimExpr; still the trimExpr should be
        // used whenever the application needs to be sure that a proper subsetting has to take place.
        wcpsCoverageMetadataService.applySubsets(false, false, metadata, subsetDimensions, numericSubsets);

        // it will not get all the axis to build the intervals in case of (extend() and scale())
        String rasql = TEMPLATE.replace("$coverage", coverageExpression.getRasql())
                               .replace("$intervalList", dimensionIntervalList);

        return new WcpsResult(metadata, rasql);
    }

    //in case we will need to handle scale with a factor, use a method such as below
    //public  WcpsResult handle(WcpsResult coverageExpression, BigDecimal scaleFactor)
    private final String TEMPLATE = "EXTEND($coverage, [$intervalList])";
}
