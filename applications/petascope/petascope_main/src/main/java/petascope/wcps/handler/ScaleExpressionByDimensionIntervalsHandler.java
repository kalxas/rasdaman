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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.IrregularAxis;
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
     * Check if scaling axis exists in the input coverage
     */
    private void validateAxisLabelExist(WcpsCoverageMetadata metadata, String scaleAxisLabel) {
        List<Axis> coverageAxes = metadata.getAxes();
        
        boolean result = false;
        for (Axis axis : coverageAxes) {
            if (CrsUtil.axisLabelsMatch(axis.getLabel(), scaleAxisLabel)) {
                result = true;
                break;
            }
        }
        
        if (!result) {
            throw new WCPSException(ExceptionCode.InvalidAxisLabel, "Scaling axis label '" + scaleAxisLabel + "' does not exist in coverage '" + metadata.getCoverageName() + "'.");
        }
    }
    
    /**
     * Special case, only 1 X or Y axis specified, find the grid domain for another axis implicitly from the specified axis
     */
    private void handleScaleWithOnlyXorYAxis(WcpsResult coverageExpression, List<Subset> subsets, boolean implicitScaleByXorYAxis) {
        // e.g: for c in (test_mean_summer_airtemp) return encode(scale( c, { Long:"CRS:1"(0:10)} ), "png")
        Subset subset1 = subsets.get(0);
        BigDecimal lowerLimit1 = subset1.getNumericSubset().getLowerLimit();
        BigDecimal upperLimit1 = subset1.getNumericSubset().getUpperLimit();            

        // e.g: Long axis has grid bounds: 30:50
        Axis axis1 = coverageExpression.getMetadata().getAxisByName(subset1.getAxisName());
        List<Axis> xyAxes = coverageExpression.getMetadata().getXYAxes();
        Axis axis2 = null;
        for (Axis axis : xyAxes) {
            if (!CrsUtil.axisLabelsMatch(axis.getLabel(), subset1.getAxisName())) {
                axis2 = axis;
                break;
            }
        }
        
        if (!implicitScaleByXorYAxis) {
            // NOTE: for example scaleextent() of WCS scale extension, it doesn't have this auto implicitly scale ratio by X or Y axis
            NumericTrimming numericTrimming = new NumericTrimming(axis2.getGridBounds().getLowerLimit(), axis2.getGridBounds().getUpperLimit());
            Subset subset2 = new Subset(numericTrimming, axis2.getNativeCrsUri(), axis2.getLabel());
            subsets.add(subset2);
            return;
        }        
        
        BigDecimal gridDistance1 = axis1.getGridBounds().getUpperLimit().subtract(axis1.getGridBounds().getLowerLimit());
        // scale ratio is: (10 - 0) / (50 - 30) = 10 / 20 = 0.5 (downscale)
        BigDecimal scaleRatio = BigDecimalUtil.divide(upperLimit1.subtract(lowerLimit1), gridDistance1);

        // Lat axis has grid bounds: 60:70
        // -> scale on Lat axis: 0:(70 - 60) * 0.5 = 0:5
        BigDecimal gridDistance2 = axis2.getGridBounds().getUpperLimit().subtract(axis2.getGridBounds().getLowerLimit());
        BigDecimal lowerLimit2 = BigDecimal.ZERO;
        BigDecimal upperLimit2 = gridDistance2.multiply(scaleRatio);
        NumericTrimming numericTrimming = new NumericTrimming(lowerLimit2, upperLimit2);

        Subset subset2 = new Subset(numericTrimming, subset1.getCrs(), axis2.getLabel());
        subsets.add(subset2);
    }
    
    public WcpsResult handle(WcpsResult coverageExpression, DimensionIntervalList dimensionIntervalList, boolean implcitScaleByXorYAxis) throws PetascopeException {
        
        checkOperandIsCoverage(coverageExpression, OPERATOR); 

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        // scale(coverageExpression, {domainIntervals})
        List<WcpsSubsetDimension> subsetDimensions = dimensionIntervalList.getIntervals();
        for (WcpsSubsetDimension subset : subsetDimensions) {
            this.validateAxisLabelExist(metadata, subset.getAxisName());
        }
        List<Subset> numericSubsets = subsetParsingService.convertToNumericSubsets(subsetDimensions, metadata.getAxes());
        
        if (this.processXOrYAxisImplicitly(metadata, numericSubsets)) {
            this.handleScaleWithOnlyXorYAxis(coverageExpression, numericSubsets, implcitScaleByXorYAxis);
        }
        
        // Then, check if any non-XY axes from coverage which are not specified from the scale() interval
        // will be added implitcily to the scale domains interval
        List<Axis> nonXYAxes = metadata.getNonXYAxes();
        for (Axis axis : nonXYAxes) {
            boolean exists = false;
            for (Subset inputSubset : numericSubsets) {
                if (CrsUtil.axisLabelsMatch(axis.getLabel(), inputSubset.getAxisName())) {
                    exists = true;
                    break;
                }
            }
            
            // Axis is not specified in the domain intervals for scale()
            if (!exists) {
                BigDecimal geoLowerBound = axis.getGeoBounds().getLowerLimit();
                BigDecimal geoUpperBound = axis.getGeoBounds().getUpperLimit();
                
                numericSubsets.add(new Subset(new NumericTrimming(geoLowerBound, geoUpperBound), axis.getNativeCrsUri(), axis.getLabel()));
                subsetDimensions.add(new WcpsTrimSubsetDimension(axis.getLabel(), axis.getNativeCrsUri(), 
                                                                 geoLowerBound.toPlainString(),
                                                                 geoUpperBound.toPlainString()));
            }
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
        List<Pair> gridBoundAxes = new ArrayList();
        Map<String, List<BigDecimal>> directPositionsMap = new HashMap<>();
        for (Axis axis : metadata.getAxes()) {
            NumericTrimming numericTrimming = new NumericTrimming(axis.getGeoBounds().getLowerLimit(), axis.getGeoBounds().getUpperLimit());
            Pair<String, NumericTrimming> pair = new Pair(axis.getLabel(), numericTrimming);
            geoBoundAxes.add(pair);
            
            NumericTrimming gridNumericTrimming = new NumericTrimming(new BigDecimal(axis.getGridBounds().getLowerLimit().toPlainString()),
                                                                      new BigDecimal(axis.getGridBounds().getUpperLimit().toPlainString()));
            Pair<String, NumericTrimming> gridPair = new Pair(axis.getLabel(), gridNumericTrimming);
            gridBoundAxes.add(gridPair);
            
            if (axis instanceof IrregularAxis) {
                List<BigDecimal> currentDirectPositions = ((IrregularAxis) axis).getDirectPositions();
                List<BigDecimal> tmpDirectPositions = new ArrayList<>();
                for (BigDecimal value : currentDirectPositions) {
                    tmpDirectPositions.add(new BigDecimal(value.toPlainString()));
                }
                
                directPositionsMap.put(axis.getLabel(), tmpDirectPositions);
            }
        }
        
        // Only for 2D XY coverage imported with downscaled collections
        this.wcpsCoverageMetadataTranslatorService.applyDownscaledLevelOnXYGridAxesForScale(coverageExpression, metadata, numericSubsets);
        
        // NOTE: from WCPS 1.0 standard, C2 = scale(C1, {x(lo1:hi1), y(lo2:hi2),...}        
        // for all a ∈ dimensionList(C2), c ∈ crsSet(C2, a):
        //          imageCrsDomain(C2 , a ) = (lo:hi) - it means: ***axis's grid domain will be set*** to corresponding lo:hi!
        //          domain(C2,a,c) = domain(C1,a,c) - it means: ***axis's geo domain will not change***!
        wcpsCoverageMetadataService.applySubsets(false, false, metadata, subsetDimensions, numericSubsets);
        
        this.addImplicitScaleGridIntervals(metadata, numericSubsets);
        
        // it will not get all the axis to build the intervals in case of (extend() and scale())
        String domainIntervals = rasqlTranslationService.constructSpecificRasqlDomain(metadata.getSortedAxesByGridOrder(), numericSubsets);
        String rasql = TEMPLATE.replace("$coverage", coverageExpression.getRasql())
                               .replace("$intervalList", domainIntervals);
        
        this.revertAfterScale(metadata, geoBoundAxes, directPositionsMap);
        this.applyScaleOnIrregularAxes(metadata, gridBoundAxes);
        
        return new WcpsResult(metadata, rasql);
    }

    
    /**
     * Add each axis's grid domains which is not decleared in the scale's interval explicitly
     */
    private void addImplicitScaleGridIntervals(WcpsCoverageMetadata metadata, List<Subset> gridNumericSubsets) {
        for (Axis axis : metadata.getAxes()) {
            boolean exists = false;
            for (Subset subset : gridNumericSubsets) {
                if (CrsUtil.axisLabelsMatch(axis.getLabel(), subset.getAxisName())) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                NumericTrimming numericTrimming = new NumericTrimming(axis.getGridBounds().getLowerLimit(), axis.getGeoBounds().getUpperLimit());
                Subset subset = new Subset(numericTrimming, axis.getNativeCrsUri(), axis.getLabel());
                gridNumericSubsets.add(subset);
            }
        }
    }
    
    /**
     * Revert some values after applying subset from scale's intervals
     */
    private void revertAfterScale(WcpsCoverageMetadata metadata, List<Pair> geoBoundAxes, Map<String, List<BigDecimal>> directPositionsMap) {
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
        
        // Revert the direct positions for irregular axes to the ones before applying scaling intervals
        for (Axis axis : metadata.getAxes()) {
            if (axis instanceof IrregularAxis) {
                IrregularAxis irregularAxis = ((IrregularAxis)axis);
                List<BigDecimal> directPositions = directPositionsMap.get(axis.getLabel());
                irregularAxis.setDirectPositions(directPositions);
            }
        }
    }
    
    /**
     * For irregular axes, when scaling, the coefficients must be filtered (scale down, typical case) or added (scale up).
     * 
     */
    public void applyScaleOnIrregularAxes(WcpsCoverageMetadata metadata, List<Pair> gridBoundAxes) {
        for (Axis axis : metadata.getAxes()) {
            for (Pair<String, NumericTrimming> pair : gridBoundAxes) {
                String axisLabel = pair.fst;
                if (axis instanceof IrregularAxis && CrsUtil.axisLabelsMatch(axis.getLabel(), axisLabel)) {
                    // e.g: [0:10]
                    NumericTrimming scaleGridTrimming = pair.snd;
                    long sourceGridLowerBound = scaleGridTrimming.getLowerLimit().longValue();
                    long sourceGridUpperBound = scaleGridTrimming.getUpperLimit().longValue();
                    long sourceGridPoints = sourceGridUpperBound - sourceGridLowerBound + 1;
                    
                    // e.g: [0:4]
                    long destGridLowerBound = axis.getGridBounds().getLowerLimit().longValue();
                    long destGridUpperBound = axis.getGridBounds().getUpperLimit().longValue();
                    long destGridPoints = destGridUpperBound - destGridLowerBound + 1;                    
                    
                    if (sourceGridPoints >= destGridPoints) {
                        // scale down [0:11] -> [0:3]
                        this.applyScaleDownOnIrregularAxis((IrregularAxis)axis, scaleGridTrimming);
                    } else {
                        // scale up [0:11] -> [0:300]                        
                        // e.g: before scale time("2001":"2010") has 6 coefficients: 2001, 2002, 2005, 2007, 2008, 2009, 2010 with grid [0:5]
                        //      after scale  time("2001":"2010") has 301 coefficients: 2001, ... 2010 with grid [0:300]
                        // @TODO: how to calculate the newly added coefficients in the middle of irregular axis?                        
                        if (CrsUtil.isGridCrs(axis.getNativeCrsUri())) {
                            throw new WCPSException(ExceptionCode.NoApplicableCode, 
                                    "Cannot scale up on irregular axis '" + axisLabel + "', only scale down is supported.");
                        } else {
                            this.applyScaleUpOnIrregularAxisWithGridCRS((IrregularAxis) axis);
                        }
                    }
                                        
                }
            }            
        }
    }
    
    /**
     * e.g: irregular time axis has 11 coefficients (time slices) with grid bounds [0:10] and scaling's grid interval is [0:3]
     * then after scaling, only 4 coefficients are left on time axis
     */
    private void applyScaleDownOnIrregularAxis(IrregularAxis axis, NumericTrimming sourceGridTrimming) {
        // e.g: [0:11]
        long sourceLowerBound = sourceGridTrimming.getLowerLimit().longValue();
        long sourceUpperBound = sourceGridTrimming.getUpperLimit().longValue();
        long sourceGridPoints = sourceUpperBound - sourceLowerBound;
        sourceLowerBound = 0;
        sourceUpperBound = sourceGridPoints;
        
        // e.g: scale to [0:3]
        long destLowerBound = axis.getGridBounds().getLowerLimit().longValue();        
        long destUpperBound = axis.getGridBounds().getUpperLimit().longValue();
        long destGridPoints = destUpperBound - destLowerBound;
        destLowerBound = 0;
        destUpperBound = destGridPoints;
        
        BigDecimal scaleRatio = BigDecimalUtil.divide(new BigDecimal(sourceUpperBound - sourceLowerBound + 1), new BigDecimal(destUpperBound - destLowerBound + 1));
        BigDecimal realIndex = BigDecimal.ZERO;
        int intIndex = 0;
        List<BigDecimal> selectedCoefficients = new ArrayList<>();
        selectedCoefficients.add(axis.getDirectPositions().get(0));
        
        while (intIndex <= sourceUpperBound) {
            realIndex = realIndex.add(scaleRatio); 
           intIndex = realIndex.intValue();
            
            if (intIndex <= sourceUpperBound) {
                BigDecimal coefficient = axis.getDirectPositions().get(intIndex);
                selectedCoefficients.add(coefficient);
            }
        }
        
        axis.setDirectPositions(selectedCoefficients);        
    }
    
    /**
     * e.g: irregular time axis has 11 coefficients (time slices) with grid bounds [0:10] and scaling's grid interval is [0:3]
     * then after scaling, only 4 coefficients are left on time axis
     */
    private void applyScaleUpOnIrregularAxisWithGridCRS(IrregularAxis axis) {
        // e.g: scale grid domain to [0:10]
        long destLowerBound = axis.getGridBounds().getLowerLimit().longValue();        
        long destUpperBound = axis.getGridBounds().getUpperLimit().longValue();
        long destGridPoints = destUpperBound - destLowerBound;
        axis.setDirectPositions(new ArrayList<>());
        
        for (int i = 0; i <= destGridPoints; i++) {
            axis.getDirectPositions().add(new BigDecimal(i));
        }
    }
    
    /**
     * Check if the coverage contains X and Y axes, but one only specifies
     * X or Y axis for scale()
     */
    private boolean processXOrYAxisImplicitly(WcpsCoverageMetadata metadata, List<Subset> numericSubsets) {
        if (metadata.hasXYAxes()) {
            // NOTE: in case 
            Axis axisX = metadata.getXYAxes().get(0);
            Axis axisY = metadata.getXYAxes().get(1);
            List<Boolean> hasXYAxes = new ArrayList<>();
            
            for (Subset subset : numericSubsets) {
                if (CrsUtil.axisLabelsMatch(subset.getAxisName(), axisX.getLabel())
                    || CrsUtil.axisLabelsMatch(subset.getAxisName(), axisY.getLabel())) {
                    hasXYAxes.add(true);
                }
                
                if (hasXYAxes.size() == 2) {
                    break;
                }
            }
            
            // Coverage has X and Y axes, but user only specifies one of X or Y for the scale(), then the domain for the other axis
            // will be determined from the specified X/Y axis.
            if (hasXYAxes.size() == 1) {
                return true;
            }
        }
        
        return false;
    }

    //in case we will need to handle scale with a factor, use a method such as below
    //public  WcpsResult handle(WcpsResult coverageExpression, BigDecimal scaleFactor)
    private final String TEMPLATE = "SCALE($coverage, [$intervalList])";
}
