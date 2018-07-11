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
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsUtil;
import petascope.wcps.exception.processing.IncompatibleAxesNumberException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;

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
public class ExtendExpressionByImageCrsDomainHandler extends AbstractOperatorHandler {
    
    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;
    
    public static final String OPERATOR = "extend";

    public WcpsResult handle(WcpsResult coverageExpression, WcpsMetadataResult wcpsMetadataResult, String dimensionIntervalList) throws PetascopeException {
        
        checkOperandIsCoverage(coverageExpression, OPERATOR); 

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        // scale(coverageExpression, {domainIntervals})

        List<Subset> subsets = new ArrayList<>();
        
        List<Axis> axes = metadata.getSortedAxesByGridOrder();
        // e.g: imageCrsdomain(c) returns 0:30,0:40,0:60
        String[] values = dimensionIntervalList.split(",");
        
        if (axes.size() != values.length) {
            throw new IncompatibleAxesNumberException(metadata.getCoverageName(), axes.size(), values.length);
        }
        
        for (int i = 0; i < axes.size(); i++) {
            String lowerValue = values[i].split(":")[0];
            String upperValue = values[i].split(":")[1];
            NumericTrimming numericTrimming = new NumericTrimming(new BigDecimal(lowerValue), new BigDecimal(upperValue));
            Subset subset = new Subset(numericTrimming, CrsUtil.GRID_CRS, axes.get(i).getLabel());
            subsets.add(subset);
        }
        
        // NOTE: from WCPS 1.0 standard: In this sense the extendExpr is a generalization of the trimExpr; still the trimExpr should be
        // used whenever the application needs to be sure that a proper subsetting has to take place.
        wcpsCoverageMetadataService.applySubsets(false, metadata, subsets);

        // it will not get all the axis to build the intervals in case of (extend() and scale())
        String rasql = TEMPLATE.replace("$coverage", coverageExpression.getRasql())
                               .replace("$intervalList", dimensionIntervalList);

        return new WcpsResult(metadata, rasql);
    }

    //in case we will need to handle scale with a factor, use a method such as below
    //public  WcpsResult handle(WcpsResult coverageExpression, BigDecimal scaleFactor)
    private final String TEMPLATE = "EXTEND($coverage, [$intervalList])";
}
