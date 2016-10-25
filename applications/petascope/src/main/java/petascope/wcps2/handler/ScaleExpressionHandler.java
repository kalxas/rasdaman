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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.handler;

import java.util.ArrayList;
import java.util.List;
import petascope.exceptions.PetascopeException;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.Subset;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.metadata.service.RasqlTranslationService;
import petascope.wcps2.metadata.service.SubsetParsingService;
import petascope.wcps2.metadata.service.WcpsCoverageMetadataService;
import petascope.wcps2.result.WcpsResult;
import petascope.wcps2.result.parameters.DimensionIntervalList;
import petascope.wcps2.result.parameters.SubsetDimension;

/**
 * Class to translate a scale wcps expression into rasql
 * <code>
 *    SCALE($coverageExpression, [$dimensionIntervalList])
 * </code>
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ScaleExpressionHandler {


    public static WcpsResult handle(WcpsResult coverageExpression, DimensionIntervalList dimensionIntervalList,
                                    WcpsCoverageMetadataService wcpsCoverageMetadataService,
                                    RasqlTranslationService rasqlTranslationService, SubsetParsingService subsetParsingService) throws PetascopeException {

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        // scale(coverageExpression, {domainIntervals})
        List<SubsetDimension> intervals = dimensionIntervalList.getIntervals();
        List<Subset> subsets = subsetParsingService.convertToNumericSubsets(intervals, metadata, true);
        // NOTE: this method will apply subsets on coverage's axes (e.g: scale(c, {Lat:"CRS:1"(0:100), Long:"CRS:1"(20:70)})
        // Only gridbound of the translated axis is needed to add in the intervalList below.
        // The coverage must keep the original axes in the coverage metadata as it does not mean coverage is translated to CRS:1.
        List<Axis> originalAxes = new ArrayList();
        for (Axis axis:metadata.getAxes()) {
            originalAxes.add( axis.clone() );
        }

        metadata = wcpsCoverageMetadataService.applySubsets(false, metadata, subsets);

        // it will not get all the axis to build the intervals in case of (extend() and scale())
        String domainIntervals = rasqlTranslationService.constructSpecificRasqlDomain(metadata.getAxes(), subsets);
        String rasql = TEMPLATE.replace("$coverage", coverageExpression.getRasql())
                               .replace("$intervalList", domainIntervals);

        // Revert translatedAxes by originaAxes
        metadata.setAxes(originalAxes);
        return new WcpsResult(metadata, rasql);
    }

    //in case we will need to handle scale with a factor, use a method such as below
    //public static WcpsResult handle(WcpsResult coverageExpression, BigDecimal scaleFactor)

    private static final String TEMPLATE = "SCALE($coverage, [$intervalList])";
}
