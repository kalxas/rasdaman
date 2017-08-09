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
import petascope.wcps.exception.processing.InvalidScaleExtentException;
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
import petascope.wcps.subset_axis.model.WcpsTrimScaleDimension;

/**
 * Class to translate a scale wcps expression by scaleSize into rasql  <code>
 *    SCALE_EXTENT($coverageExpression, [$scaleDimensionInteverlList]) *
 * </code>
 *
 * e.g: scale_extent(c, [i(25:50), j(25:50)]) then number of grid points for i
 * is 26 and j is 26 in the output
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class WcsScaleExpressionByScaleExtentHandler extends AbstractWcsScaleHandler {

    @Autowired
    private RasqlTranslationService rasqlTranslationService;

    public WcpsResult handle(WcpsResult coverageExpression, WcpsScaleDimensionIntevalList scaleAxesDimensionList) {
        // Validate the scale dimension intervals first
        this.validateScalingDimensionInterval(coverageExpression, scaleAxesDimensionList);

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        List<Subset> subsets = new ArrayList<>();

        for (Axis axis : metadata.getAxes()) {
            // Check if axis is mentioned in scaleAxesDimensionList, then divide by the scaleFactor or just keep the same pixels for unmentioned axis
            BigDecimal scaledLowerBound = axis.getGridBounds().getLowerLimit();
            BigDecimal scaledUpperBound = axis.getGridBounds().getUpperLimit();
            for (AbstractWcpsScaleDimension dimension : scaleAxesDimensionList.getIntervals()) {
                // NOTE: scaleextent must be low:hi
                if (dimension instanceof WcpsSliceScaleDimension) {
                    throw new InvalidScaleExtentException(axis.getLabel(), ((WcpsSliceScaleDimension) dimension).getBound());
                }
                if (axis.getLabel().equals(dimension.getAxisName())) {
                    // here scaleFactor is the number of pixels for the dimension
                    scaledLowerBound = new BigDecimal(((WcpsTrimScaleDimension) dimension).getLowerBound());
                    scaledUpperBound = new BigDecimal(((WcpsTrimScaleDimension) dimension).getUpperBound());
                    break;
                }
            }

            NumericSubset numericSubset = null;
            // NOTE: scaleextent=Lat(20:30) means grid lower bound is 20 - 20 = 0, grid upper bound is 30 - 20 = 10
            scaledUpperBound = scaledUpperBound.subtract(scaledLowerBound);
            scaledLowerBound = BigDecimal.ZERO;
            
            numericSubset = new NumericTrimming(new BigDecimal(scaledLowerBound.intValue()),
                    new BigDecimal(scaledUpperBound.intValue()));

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
