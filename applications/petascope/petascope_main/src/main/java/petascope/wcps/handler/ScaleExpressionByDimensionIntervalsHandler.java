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
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.wcps.exception.processing.IncompatibleAxesNumberException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.RasqlTranslationService;
import petascope.wcps.metadata.service.SubsetParsingService;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.DimensionIntervalList;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/**
 * Class to translate a scale wcps expression into rasql  <code>
 *    SCALE($coverageExpression, [$dimensionIntervalList])
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class ScaleExpressionByDimensionIntervalsHandler extends AbstractOperatorHandler {

    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;
    @Autowired
    private SubsetParsingService subsetParsingService;
    @Autowired
    private RasqlTranslationService rasqlTranslationService;
    @Autowired
    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslatorService;
    
    public static final String OPERATOR = "scale";
    
    /**
     * Special case, only 1 X or Y axis specified, find the grid domain for another axis implicitly from the specified axis
     */
    private void handleScaleWithOnlyXorYAxis(WcpsResult coverageExpression, List<Subset> subsets) {
        // e.g: for c in (test_mean_summer_airtemp) return encode(scale( c, { Long:"CRS:1"(0:10)} ), "png")
        Subset subset1 = subsets.get(0);
        BigDecimal lowerLimit1 = subset1.getNumericSubset().getLowerLimit();
        BigDecimal upperLimit1 = subset1.getNumericSubset().getUpperLimit();            

        // e.g: Long axis has grid bounds: 30:50
        Axis axis1 = coverageExpression.getMetadata().getAxisByName(subset1.getAxisName());
        BigDecimal gridDistance1 = axis1.getGridBounds().getUpperLimit().subtract(axis1.getGridBounds().getLowerLimit());
        // scale ratio is: (10 - 0) / (50 - 30) = 10 / 20 = 0.5 (downscale)
        BigDecimal scaleRatio = BigDecimalUtil.divide(upperLimit1.subtract(lowerLimit1), gridDistance1);

        List<Axis> xyAxes = coverageExpression.getMetadata().getXYAxes();
        Axis axis2 = null;
        for (Axis axis : xyAxes) {
            if (!CrsUtil.axisLabelsMatch(axis.getLabel(), subset1.getAxisName())) {
                axis2 = axis;
                break;
            }
        }

        // Lat axis has grid bounds: 60:70
        // -> scale on Lat axis: 0:(70 - 60) * 0.5 = 0:5
        BigDecimal gridDistance2 = axis2.getGridBounds().getUpperLimit().subtract(axis2.getGridBounds().getLowerLimit());
        BigDecimal lowerLimit2 = BigDecimal.ZERO;
        BigDecimal upperLimit2 = gridDistance2.multiply(scaleRatio);
        NumericTrimming numericTrimming = new NumericTrimming(lowerLimit2, upperLimit2);

        Subset subset2 = new Subset(numericTrimming, subset1.getCrs(), axis2.getLabel());
        subsets.add(subset2);
    }
    
    public WcpsResult handle(WcpsResult coverageExpression, DimensionIntervalList dimensionIntervalList) throws PetascopeException, SecoreException {
        
        checkOperandIsCoverage(coverageExpression, OPERATOR); 

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        // scale(coverageExpression, {domainIntervals})
        List<WcpsSubsetDimension> subsetDimensions = dimensionIntervalList.getIntervals();
        List<Subset> numericSubsets = subsetParsingService.convertToNumericSubsets(subsetDimensions, metadata.getAxes());
        
        if (!metadata.containsOnlyXYAxes() && (metadata.getAxes().size() != numericSubsets.size())) {
            throw new IncompatibleAxesNumberException(metadata.getCoverageName(), metadata.getAxes().size(), numericSubsets.size());
        } else if (numericSubsets.size() == 1) {
            this.handleScaleWithOnlyXorYAxis(coverageExpression, numericSubsets);
        }
        
        for (Axis axis : metadata.getAxes()) {
            boolean exists = false;
            for (WcpsSubsetDimension subsetDimension : subsetDimensions) {
                if (CrsUtil.axisLabelsMatch(subsetDimension.getAxisName(), axis.getLabel())) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                String lowerBound = axis.getLowerGeoBoundRepresentation();
                String upperBound = axis.getUpperGeoBoundRepresentation();
                for (Subset subset : numericSubsets) {
                    if (subset.getAxisName().equals(axis.getLabel())) {
                        lowerBound = subset.getNumericSubset().getLowerLimit().toPlainString();
                        upperBound = subset.getNumericSubset().getUpperLimit().toPlainString();
                        break;
                    }                    
                }
                
                subsetDimensions.add(new WcpsTrimSubsetDimension(axis.getLabel(), axis.getNativeCrsUri(), lowerBound, upperBound));
            }
        }

        List<Pair> geoBoundAxes = new ArrayList();
        for (Axis axis : metadata.getAxes()) {
            NumericTrimming numericTrimming = new NumericTrimming(axis.getGeoBounds().getLowerLimit(), axis.getGeoBounds().getUpperLimit());
            Pair<String, NumericTrimming> pair = new Pair(axis.getLabel(), numericTrimming);
            geoBoundAxes.add(pair);
        }
        
        // Only for 2D XY coverage imported with downscaled collections
        this.wcpsCoverageMetadataTranslatorService.applyDownscaledLevelOnXYGridAxesForScale(coverageExpression, metadata, numericSubsets);
        
        // NOTE: from WCPS 1.0 standard, C2 = scale(C1, {x(lo1:hi1), y(lo2:hi2),...}        
        // for all a ∈ dimensionList(C2), c ∈ crsSet(C2, a):
        //          imageCrsDomain(C2 , a ) = (lo:hi) - it means: ***axis's grid domain will be set*** to corresponding lo:hi!
        //          domain(C2,a,c) = domain(C1,a,c) - it means: ***axis's geo domain will not change***!
        wcpsCoverageMetadataService.applySubsets(false, metadata, subsetDimensions, numericSubsets);
        
        // it will not get all the axis to build the intervals in case of (extend() and scale())
        String domainIntervals = rasqlTranslationService.constructSpecificRasqlDomain(metadata.getSortedAxesByGridOrder(), numericSubsets);
        String rasql = TEMPLATE.replace("$coverage", coverageExpression.getRasql())
                               .replace("$intervalList", domainIntervals);

        // Revert the changed axes' geo bounds as before applying scale subsets.
        // e.g: scale(c, {Lat:"CRS:1"(0:20), Long:"CRS:1"(0:20)} and before scale, 
        // coverage has geo domains: Lat(-40, 40), Long(-30, 30), grid domains: Lat":CRS:1"(0:300), Long:"CRS:1"(0:200)
        // After scale, the geo domains are kept and grid domain will be: Lat":CRS1:"(0:20), Long:"CRS:1"(0:20)
        for (Axis axis : metadata.getAxes()) {
            for (Pair<String, NumericTrimming> pair : geoBoundAxes) {
                if (CrsUtil.axisLabelsMatch(axis.getLabel(), pair.fst)) {
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
