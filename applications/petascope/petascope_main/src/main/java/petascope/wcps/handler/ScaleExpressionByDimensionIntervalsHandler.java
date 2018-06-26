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

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.wcps.exception.processing.IncompatibleAxesNumberException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.RasqlTranslationService;
import petascope.wcps.metadata.service.SubsetParsingService;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.DimensionIntervalList;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;

/**
 * Class to translate a scale wcps expression into rasql  <code>
 *    SCALE($coverageExpression, [$dimensionIntervalList])
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class ScaleExpressionByDimensionIntervalsHandler {

    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;
    @Autowired
    private SubsetParsingService subsetParsingService;
    @Autowired
    private RasqlTranslationService rasqlTranslationService;

    public WcpsResult handle(WcpsResult coverageExpression, DimensionIntervalList dimensionIntervalList) throws PetascopeException {

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        // scale(coverageExpression, {domainIntervals})
        List<WcpsSubsetDimension> intervals = dimensionIntervalList.getIntervals();
        List<Subset> subsets = subsetParsingService.convertToNumericSubsets(intervals, metadata, true);
        
        if (metadata.getAxes().size() != subsets.size()) {
            throw new IncompatibleAxesNumberException(metadata.getCoverageName(), metadata.getAxes().size(), subsets.size());
        }

        List<Pair> geoBoundAxes = new ArrayList();
        for (Axis axis : metadata.getAxes()) {
            NumericTrimming numericTrimming = new NumericTrimming(axis.getGeoBounds().getLowerLimit(), axis.getGeoBounds().getUpperLimit());
            Pair<String, NumericTrimming> pair = new Pair(axis.getLabel(), numericTrimming);
            geoBoundAxes.add(pair);
        }

        // NOTE: from WCPS 1.0 standard, C2 = scale(C1, {x(lo1:hi1), y(lo2:hi2),...}        
        // for all a ∈ dimensionList(C2), c ∈ crsSet(C2, a):
        //          imageCrsDomain(C2 , a ) = (lo:hi) - it means: ***axis's grid domain will be set*** to corresponding lo:hi!
        //          domain(C2,a,c) = domain(C1,a,c) - it means: ***axis's geo domain will not change***!
        wcpsCoverageMetadataService.applySubsets(false, metadata, subsets);

        // it will not get all the axis to build the intervals in case of (extend() and scale())
        String domainIntervals = rasqlTranslationService.constructSpecificRasqlDomain(metadata.getSortedAxesByGridOrder(), subsets);
        String rasql = TEMPLATE.replace("$coverage", coverageExpression.getRasql())
                .replace("$intervalList", domainIntervals);

        // Revert the changed axes' geo bounds as before applying scale subsets.
        // e.g: scale(c, {Lat:"CRS:1"(0:20), Long:"CRS:1"(0:20)} and before scale, 
        // coverage has geo domains: Lat(-40, 40), Long(-30, 30), grid domains: Lat":CRS:1"(0:300), Long:"CRS:1"(0:200)
        // After scale, the geo domains are kept and grid domain will be: Lat":CRS1:"(0:20), Long:"CRS:1"(0:20)
        for (Axis axis : metadata.getAxes()) {
            for (Pair<String, NumericTrimming> pair : geoBoundAxes) {
                if (axis.getLabel().equals(pair.fst)) {
                    axis.getGeoBounds().setLowerLimit(pair.snd.getLowerLimit());
                    axis.getGeoBounds().setUpperLimit(pair.snd.getUpperLimit());
                    
                    this.wcpsCoverageMetadataService.updateGeoResolutionByGridBound(axis);
                }
            }
        }
        
        return new WcpsResult(metadata, rasql);
    }

    //in case we will need to handle scale with a factor, use a method such as below
    //public  WcpsResult handle(WcpsResult coverageExpression, BigDecimal scaleFactor)
    private final String TEMPLATE = "SCALE($coverage, [$intervalList])";
}
