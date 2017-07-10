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
package petascope.wcps2.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.util.BigDecimalUtil;
import petascope.wcps2.exception.processing.ScaleValueLessThanZeroException;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.NumericSlicing;
import petascope.wcps2.metadata.model.NumericSubset;
import petascope.wcps2.metadata.model.NumericTrimming;
import petascope.wcps2.metadata.model.Subset;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.metadata.service.RasqlTranslationService;
import petascope.wcps2.result.WcpsResult;

/**
 * Class to translate a scale wcps expression into rasql  <code>
 *    SCALE_FACTOR($coverageExpression, $factor)
 * NOTE: factor = 0.5 means every axis of coverage will divide by 0.5 in grid pixels
 * </code>
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class WcsScaleExpressionByFactorHandler extends AbstractWcsScaleHandler {

    @Autowired
    private RasqlTranslationService rasqlTranslationService;

    public WcpsResult handle(WcpsResult coverageExpression, BigDecimal scaleFactor) {

        if (scaleFactor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ScaleValueLessThanZeroException("all axes", scaleFactor.toPlainString());
        }

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        List<Subset> subsets = new ArrayList<>();
        // first apply the scale factor on all the grid bounds
        // e.g: 100 / 2.5 or 100 / 0.5
        for (Axis axis : metadata.getAxes()) {
            BigDecimal scaledLowerBound = BigDecimalUtil.divide(axis.getGridBounds().getLowerLimit(), scaleFactor);
            BigDecimal scaledUpperBound = BigDecimalUtil.divide(axis.getGridBounds().getUpperLimit(), scaleFactor);
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
