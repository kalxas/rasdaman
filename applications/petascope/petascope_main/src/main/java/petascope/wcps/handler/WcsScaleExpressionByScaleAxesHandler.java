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
import petascope.util.BigDecimalUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericSlicing;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.RasqlTranslationService;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.WcpsScaleDimensionIntevalList;
import petascope.wcps.subset_axis.model.AbstractWcpsScaleDimension;
import petascope.wcps.subset_axis.model.WcpsSliceScaleDimension;

/**
 * Class to translate a scale wcps expression by scaleAxes into rasql  <code>
 *    SCALE_AXES($coverageExpression, [$scaleDimensionInteverlList]) *
 * </code>
 *
 * e.g: scale_axes(c, [i(0.5)]) then number of grid points for i * 0.5, while j
 * is kept with c is 2D coverage
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class WcsScaleExpressionByScaleAxesHandler extends AbstractWcsScaleHandler {

    @Autowired
    private RasqlTranslationService rasqlTranslationService;

    public WcpsResult handle(WcpsResult coverageExpression, WcpsScaleDimensionIntevalList scaleAxesDimensionList) {
        // Validate the scale dimension intervals first
        this.validateScalingDimensionInterval(coverageExpression, scaleAxesDimensionList);

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        List<Subset> subsets = new ArrayList<>();

        for (Axis axis : metadata.getAxes()) {
            // Check if axis is mentioned in scaleAxesDimensionList, then divide by the scaleFactor or just keep the same pixels for unmentioned axis
            BigDecimal scaleFactor = BigDecimal.ONE;
            for (AbstractWcpsScaleDimension dimension : scaleAxesDimensionList.getIntervals()) {
                if (axis.getLabel().equals(dimension.getAxisName())) {
                    scaleFactor = new BigDecimal(((WcpsSliceScaleDimension) dimension).getBound());
                    break;
                }
            }

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

        return new WcpsResult(metadata, rasql);
    }

    //in case we will need to handle scale with a factor, use a method such as below
    //public  WcpsResult handle(WcpsResult coverageExpression, BigDecimal scaleFactor)
    private final String TEMPLATE = "SCALE($coverage, [$intervalList])";
}
