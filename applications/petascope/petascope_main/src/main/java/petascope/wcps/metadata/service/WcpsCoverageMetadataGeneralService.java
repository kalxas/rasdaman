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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.metadata.service;

import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.IrregularAxis;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.model.ParsedSubset;
import petascope.wcps.metadata.model.NumericSlicing;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.RegularAxis;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.core.XMLSymbols;
import petascope.wcps.exception.processing.CoverageAxisNotFoundExeption;
import petascope.wcps.exception.processing.IncompatibleAxesNumberException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import org.rasdaman.domain.cis.NilValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.CrsDefinition;
import petascope.exceptions.PetascopeException;
import petascope.core.AxisTypes;
import petascope.core.BoundingBox;
import petascope.core.GeoTransform;
import petascope.core.Pair;
import petascope.core.gml.metadata.model.Envelope;
import petascope.core.gml.metadata.model.LocalMetadataChild;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsProjectionUtil;
import petascope.util.CrsUtil;
import petascope.wcps.exception.processing.IncompatibleCoveragesException;
import petascope.wcps.exception.processing.InvalidBoundingBoxInCrsTransformException;
import petascope.wcps.exception.processing.InvalidSubsettingException;
import petascope.wcps.exception.processing.OutOfBoundsSubsettingException;
import static petascope.wcps.exception.processing.OutOfBoundsSubsettingException.GEO_TYPE;
import static petascope.wcps.exception.processing.OutOfBoundsSubsettingException.GRID_TYPE;
import petascope.wcps.exception.processing.RangeFieldNotFound;
import petascope.wcps.subset_axis.model.WcpsSliceSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/**
 * Class responsible with offering functionality for doing operations on
 * WcpsCoverageMetadataObjects.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class WcpsCoverageMetadataGeneralService {

    @Autowired
    private CoordinateTranslationService coordinateTranslationService;

    public WcpsCoverageMetadataGeneralService() {

    }
    
    /**
     * When the coverage's grid domain is changed from subsets but the geo bound is kept 
     * (e.g: SCALE(coverage, {dimension interval lists} with geo bounds are kept but grid bounds changed).
     * So need to recalculate the geo resolution (offset vector) for this axis.
     */
    public void updateGeoResolutionByGridBound(Axis axis) {
        // Also, recalculate the offset vector as the axis's grid domain has been changed by scale subset
        if (axis instanceof RegularAxis) {
            BigDecimal geoDomain = axis.getGeoBounds().getUpperLimit().subtract(axis.getGeoBounds().getLowerLimit());
            // e.g: grid domain (0:1) then number of pixels is: (1 - 0) + 1 = 2;
            BigDecimal gridPixels = axis.getGridBounds().getUpperLimit().subtract(axis.getGridBounds().getLowerLimit()).abs().add(BigDecimal.ONE);
            BigDecimal newOffsetVector = BigDecimalUtil.divide(geoDomain, gridPixels);
            if (axis.isYAxis()) {
                // e.g: Lat axis
                axis.setResolution(newOffsetVector.multiply(new BigDecimal("-1")));
            } else {
                // e.g: Long axis
                axis.setResolution(newOffsetVector);
            }
        } else if (axis instanceof IrregularAxis) {
            // @TODO: use this directPositions from the grid value temporarily until the coefficients of irregular axis
            // are calculated properly in http://rasdaman.org/ticket/1842
            int lowerBound = axis.getGridBounds().getLowerLimit().intValue();
            int upperBound = axis.getGridBounds().getUpperLimit().intValue();
            
            List<BigDecimal> directPositions = new ArrayList<>();
            for (int i = lowerBound; i <= upperBound; i++) {
                directPositions.add(new BigDecimal(i));
            }
            
            ((IrregularAxis)axis).setDirectPositions(directPositions);
        }
    }

    /**
     * Creates a resulting coverage metadata object when a binary operation is
     * performed. The current convention is the following: if only 1 object is
     * non null, the that is the passed meta. If both are non null, the first
     * one is passed. If both are null, null is passed.
     *
     * @param firstMeta
     * @param secondMeta
     * @return
     */
    public WcpsCoverageMetadata getResultingMetadata(WcpsCoverageMetadata firstMeta, WcpsCoverageMetadata secondMeta,
                                                     String firstRasql, String secondRasql) {
        if (firstMeta != null && secondMeta != null && (firstMeta.getAxes().size() > 0 && secondMeta.getAxes().size() > 0)) {
            validateCoveragesCompatibility(firstMeta, secondMeta);
        }
        
        if (firstMeta != null) {
            return firstMeta;
        }
        if (firstMeta == null && secondMeta != null) {
            return secondMeta;
        }
        
        //default both are null
        return null;
    }

    /**
     * Transform the subsettingCRS for XY-georeferenced axes correctly e.g:
     * Lat:http://.../EPSG:3857(35000:60000)&Long:http://..../EPSG:3857(30000:376000)
     * then translates these values from EPSG:3857 to EPSG:4326 of the list
     * subsets
     *
     * @param metadata
     * @param subsets
     * @return
     */
    public void transformSubsettingCrsXYSubsets(WcpsCoverageMetadata metadata, List<Subset> subsets) throws PetascopeException {
        String xyAxisCRSURL = metadata.getXYCrs();
        if (!CrsProjectionUtil.isValidTransform(xyAxisCRSURL)) {
            // No need to transform if XY axis is not geo-referenced or Authority is not EPSG
            return;
        }
        
        List<Axis> xyAxes = metadata.getXYAxes();
        Axis xAxis = xyAxes.get(0);
        String subsettingCrsX = xAxis.getNativeCrsUri();
        String xAxisName = xAxis.getLabel();

        Axis yAxis = xyAxes.get(1);
        String subsettingCrsY = yAxis.getNativeCrsUri();
        String yAxisName = yAxis.getLabel();

        BigDecimal xMin = null, yMin = null, xMax = null, yMax = null;

        // Target CRS is the nativeCrs
        String nativeCRS = subsettingCrsX;

        for (Subset subset : subsets) {
            if (CrsUtil.axisLabelsMatch(xAxisName, subset.getAxisName())) {
                // subset can contain CRS or not (e.g: Long(0:20) is not, but Long:"http://.../4326" is)
                String crsURL = subset.getCrs();
                if (crsURL != null) {
                    if (CrsProjectionUtil.isValidTransform(crsURL)) {
                        subsettingCrsX = crsURL;
                        xMin = subset.getNumericSubset().getLowerLimit();
                        xMax = subset.getNumericSubset().getUpperLimit();
                    }
                }
            }
            if (CrsUtil.axisLabelsMatch(yAxisName, subset.getAxisName())) {
                // subset can contain CRS or not (e.g: Long(0:20) is not, but Long:"http://.../4326" is)
                String crsURL = subset.getCrs();
                if (crsURL != null) {
                    if (CrsProjectionUtil.isValidTransform(crsURL)) {
                        subsettingCrsY = crsURL;
                        yMin = subset.getNumericSubset().getLowerLimit();
                        yMax = subset.getNumericSubset().getUpperLimit();
                    }
                }
            }
        }

        if (!subsettingCrsX.equals(subsettingCrsY)) {
            // In this case, only 1 X/Y axis is provided, the other one is not specified and is derived implicility
            String subsettingCRS = null;
            boolean isXAxisNotSpecified = false;
            boolean isYAxisNotSpecified = false;
            
            if (subsettingCrsX.equals(nativeCRS)) {
                // Axis X is not specified in subsets, only axis Y is specified
                subsettingCRS = subsettingCrsY;
                isXAxisNotSpecified = true;
            } else if (subsettingCrsY.equals(nativeCRS)) {
                // Axis Y is not specified in subsets, only axis X is specified
                subsettingCRS = subsettingCrsX;
                isYAxisNotSpecified = true;
            }
            
            
            // Transform the geo XY bounding box of current coverage before subsetting in original CRS (e.g: EPSG:4326) to subsettingCRS (e.g: EPSG:3857)
            BoundingBox sourceCRSBBOX = new BoundingBox(xAxis.getGeoBounds().getLowerLimit(), yAxis.getGeoBounds().getLowerLimit(),
                                                        xAxis.getGeoBounds().getUpperLimit(), yAxis.getGeoBounds().getUpperLimit());
            BoundingBox targetCRSBBOX = CrsProjectionUtil.transformBBox(sourceCRSBBOX, nativeCRS, subsettingCRS); 
            
            List<BigDecimal> xyMin = Arrays.asList(targetCRSBBOX.getXMin(), targetCRSBBOX.getYMin());
            List<BigDecimal> xyMax = Arrays.asList(targetCRSBBOX.getXMax(), targetCRSBBOX.getYMax());
            
            if (isXAxisNotSpecified) {
                xMin = xyMin.get(0);
                xMax = xyMax.get(0);
            }
            if (isYAxisNotSpecified) {
                yMin = xyMin.get(1);
                yMax = xyMax.get(1);
            }
        }

        // Only consider about transform when subsettingCrsX = subsetSettingCrsY but it is not the same as native CRS XY
        // e.g: subsetss are Lat:"http://.../3857", Long:"http://..../3857"
        if (!subsettingCrsX.equals(nativeCRS)) {
            // sourceCrs
            String subsettingCRS = subsettingCrsX;
            // Transform from sourceCrs to targetCrs and change the values of List subsets (only when targetCrs is different from soureCrs)
            List<BigDecimal> xyMin = null, xyMax  = null;
            try {
                BoundingBox sourceCRSBBOX = new BoundingBox(xMin, yMin, xMax, yMax);
                BoundingBox targetCRSBBox = CrsProjectionUtil.transformBBox(sourceCRSBBOX, subsettingCRS, nativeCRS);
                
                xyMin = Arrays.asList(targetCRSBBox.getXMin(), targetCRSBBox.getYMin());
                xyMax = Arrays.asList(targetCRSBBox.getXMax(), targetCRSBBox.getYMax());
            } catch (Exception ex) {
                String bboxStr = "xmin=" + xMin + "," + "ymin=" + yMin + ","
                               + "xmax=" + xMax + "," + "ymax=" + yMax;
                throw new InvalidBoundingBoxInCrsTransformException(bboxStr, subsettingCRS, ex.getMessage(), ex);
            }

            // NOTE: when using subsettingCRS, both of XY-georefenced axis must exist in coverage expression
            // e.g: c[Lat:"http://.../3857"(3000,60000), Long:"http://.../3857"(4000,60000")]
            // and without CrsTransform then, the outputCrs is set to subsettingCRS (i.e: coverage is transformed from native CRS: EPSG:4326 to EPSG:3857)
            for (Subset subset : subsets) {
                if (CrsUtil.axisLabelsMatch(subset.getAxisName(), xAxisName)) {
                    BigDecimal geoXMin = xyMin.get(0);
                    BigDecimal geoXMax = xyMax.get(0);
                    
                    // NOTE: with CRS transform, geoXMax can be less then geoXMin (e.g: for CRS EPSG:3857 on Long axis)
                    if (geoXMin.compareTo(geoXMax) > 0) {
                        Pair<BigDecimal, BigDecimal> pair = BigDecimalUtil.swap(geoXMin, geoXMax);
                        geoXMin = pair.fst;
                        geoXMax = pair.snd;
                    }
                    
                    subset.getNumericSubset().setLowerLimit(geoXMin);
                    subset.getNumericSubset().setUpperLimit(geoXMax);
                } else if (subset.getAxisName().equals(yAxisName)) {
                    
                    BigDecimal geoYMin = xyMin.get(1);
                    BigDecimal geoYMax = xyMax.get(1);
                    
                    // NOTE: with CRS transform, geoXMax can be less then geoXMin (e.g: for CRS EPSG:3857 on Long axis)
                    if (geoYMin.compareTo(geoYMax) > 0) {
                        Pair<BigDecimal, BigDecimal> pair = BigDecimalUtil.swap(geoYMin, geoYMax);
                        geoYMin = pair.fst;
                        geoYMax = pair.snd;
                    }
                    
                    subset.getNumericSubset().setLowerLimit(geoYMin);
                    subset.getNumericSubset().setUpperLimit(geoYMax);
                }
            }
        }
    }

    /**
     * Applies subsetting to a metadata object. e.g: eobstest(t(0:5),
     * Lat(-40.5:75), Long(25.5:75)) and with the trimming expression
     * (c[Lat(20:30), Long(40:50)]) then need to apply the subsets [(20:30),
     * (40:50)] in the coverage metadata expression
     *
     * @param checkGeoBound should the subset needed to check the boundary (e.g:
     * with scale(..., {subset})) will not need to check.
     * @param metadata
     * @param numericSubsets
     * @throws petascope.exceptions.PetascopeException
     */
    public void applySubsets(Boolean checkGeoBound, boolean checkGridBound, WcpsCoverageMetadata metadata, List<WcpsSubsetDimension> subsetDimensions, List<Subset> numericSubsets) throws PetascopeException {
        checkSubsetConsistency(metadata, numericSubsets);

        // If the subsets contain subsettingCrs which is different from XY-georeferenced axis's nativeCRS, then do the transform for both XY axes
        transformSubsettingCrsXYSubsets(metadata, numericSubsets);

        // NOTE: we need to transform the values from the subsetList for the XY-georeferenced axes before applying to the WcpsCoverageMetadata
        // for crsTransform (e.g: Lat:http://.../3857(3000,50000)&Lon://http://.../3857(30000,56000))
        // it will need Lat min, Long min and Lat max, Long max to transform from EPSG:3857 to EPSG:4326 correctly
        // Cannot use Lat min, Lat max to transform as it will be wrong result in any cases.
        // iterate through the subsets
        // Normally, the query will need to calculate the grid bound from geo bound
        for (Subset numericSubset : numericSubsets) {
            WcpsSubsetDimension subsetDimension = null;
            for (WcpsSubsetDimension tmp : subsetDimensions) {
                if (CrsUtil.axisLabelsMatch(numericSubset.getAxisName(), tmp.getAxisName())) {
                    subsetDimension = tmp;
                    break;
                }
            }
            
            //identify the corresponding axis in the coverage metadata
            for (Axis axis : metadata.getAxes()) {
                // Only apply to correspondent axis with same name
                if (CrsUtil.axisLabelsMatch(axis.getLabel(), numericSubset.getAxisName())) {
                    // NOTE: There are 2 types of subset:
                    // + update the geo-bound according to the subsets and translate updated geo-bound to new grid-bound
                    //   e.g: Lat(0:20) -> c[0:50] (calculate the grid coordinates from geo coordinates)
                    // + update the grid-bound according to the subsets and translate update grid-bound to new geo-bound
                    //   e.g: Lat:"CRS:1"(0:50) -> Lat(0:20) (calculate the geo coordinates from grid coordinates)
                    
                    // Trimming
                    if (numericSubset.getNumericSubset() instanceof NumericTrimming) {
                        applyTrimmingSubset(checkGeoBound, checkGridBound, metadata, subsetDimension, numericSubset, axis);
                    } else {
                        // slicing
                        applySlicing(checkGeoBound, checkGridBound, metadata, subsetDimension, numericSubset, axis);
                        axis.setSlicing();
                    }

                    // If axis is irregular so update the new axisDirections (i.e: coefficients between the new applied lowerBound, upperBound)
                    if (axis instanceof IrregularAxis) {
                        IrregularAxis irregularAxis = (IrregularAxis) axis;
                        // NOTE: must normalize the inputs with origin first (e.g: for AnsiDate: origin is: 1601-01-01 and input is: 2008-01-01T01:00:02Z)
                        BigDecimal geoLowerBound = irregularAxis.getGeoBounds().getLowerLimit();
                        BigDecimal geoUpperBound = irregularAxis.getGeoBounds().getUpperLimit();
                        
                        BigDecimal normalizedLowerBound = geoLowerBound.subtract(axis.getOriginalOrigin());
                        BigDecimal normalizedUpperBound = geoUpperBound.subtract(axis.getOriginalOrigin());
                        
                        normalizedLowerBound = normalizedLowerBound.add(irregularAxis.getLowestCoefficientValue());
                        normalizedUpperBound = normalizedUpperBound.add(irregularAxis.getLowestCoefficientValue());
                        
                        List<BigDecimal> newCoefficients = ((IrregularAxis) axis).getAllCoefficientsInInterval(normalizedLowerBound, normalizedUpperBound);
                        irregularAxis.setDirectPositions(newCoefficients);
                    }

                    // Continue with another subset
                    break;
                }
            }
        }
    }

    /**
     * Apply the subset type (slicing/trimming) on the processing coverage's
     * metadata e.g: Lat(0), Long(20:25) then coverage which has 2 original axes
     * (Lat(0:50), Long(20:60)) will has new metadata slicing: Lat(0), trimming:
     * Long(20:25)
     *
     * @param metadata
     * @param subsetDimensions
     */
    public void stripSlicingAxes(WcpsCoverageMetadata metadata, List<WcpsSubsetDimension> subsetDimensions) {
        List<Integer> removeIndexes = new ArrayList<>();
        int i = 0
                ;
        for (Axis axis : metadata.getAxes()) {
            for (WcpsSubsetDimension subset : subsetDimensions) {
                
                if (CrsUtil.axisLabelsMatch(axis.getLabel(), subset.getAxisName())) {
                    
                    // Subset is slice then the axis should be removed from coverage's metadata
                    if (subset instanceof WcpsSliceSubsetDimension) {
                        removeIndexes.add(i);
                    }
                }
            }
            i++;
        }

        // Remove the slicing axes from the coverage
        int removeIndex = 0;
        for (int index : removeIndexes) {
            metadata.getAxes().remove(index - removeIndex);
            removeIndex++;
        }
    }
    
    /**
     * Based on input subsets, filter elements from the list of coverage's local metadata
     * whose envelopes are not intersected/within.
     * 
     * e.g: [element1(Lat(-30:30), element3(Lat(-30:40), element3(Lat(60:80))], input subset(Lat(-2:10)),
     * then element3 is removed from list.
     */
    public void filterCoverageLocalMetadata(WcpsCoverageMetadata wcpsCoverageMetadata, List<Subset> inputSubsets) {
        
        if (wcpsCoverageMetadata.getCoverageMetadata().getLocalMetadata() == null) {
            return;
        }
        
        List<LocalMetadataChild> localMetadataChildList = wcpsCoverageMetadata.getCoverageMetadata().getLocalMetadata().getLocalMetadataChildList();
        ListIterator<LocalMetadataChild> iter = localMetadataChildList.listIterator();

        while (iter.hasNext()) {
            LocalMetadataChild localMetadataChild = iter.next();
            Envelope envelope = localMetadataChild.getBoundedBy().getEnvelope();
            List<Subset> envelopeSubsets = envelope.getEnvelopeSubsets();
            
            boolean foundToRemove = false;
            
            for (Subset inputSubset : inputSubsets) {
                for (Subset envelopeSubset : envelopeSubsets) {
                    if (inputSubset.getAxisName().equals(envelopeSubset.getAxisName())) {
                        // Check if trimming or slicing on coverage should also remove local metadata child element
                        BigDecimal inputSubsetLowerBound = inputSubset.getNumericSubset().getLowerLimit();
                        BigDecimal inputSubsetUpperBound = inputSubset.getNumericSubset().getUpperLimit();
                        
                        BigDecimal currentEnvelopeSubsetLowerBound = envelopeSubset.getNumericSubset().getLowerLimit();
                        BigDecimal currentEnvelopeSubsetUpperBound = envelopeSubset.getNumericSubset().getUpperLimit();
                        
                        // envelope: [20:30] and input subset is: [10:25] or [10:40]
                        boolean leftIntersect = (inputSubsetLowerBound.compareTo(currentEnvelopeSubsetLowerBound) <= 0 
                              && inputSubsetUpperBound.compareTo(currentEnvelopeSubsetLowerBound) >= 0);
                        // envelope: [20:30] and input subset is: [25:35] or [10:35]
                        boolean rightIntersect = (inputSubsetLowerBound.compareTo(currentEnvelopeSubsetUpperBound) <= 0
                              && inputSubsetUpperBound.compareTo(currentEnvelopeSubsetUpperBound) >= 0);
                        // envelope: [20:30] an input subset is: [21:28]
                        boolean within = (inputSubsetLowerBound.compareTo(currentEnvelopeSubsetLowerBound) >= 0 
                              && inputSubsetUpperBound.compareTo(currentEnvelopeSubsetUpperBound) <= 0);
                        
                        if (!(leftIntersect || rightIntersect || within)) {
                            // Local metadata child's envelope does not intersection/within input subset on the same axis then remove it
                            iter.remove();
                            foundToRemove = true;
                            break;
                        }
                    }
                }
                
                if (foundToRemove) {
                    break;
                }
            }
        }
    }

    /**
     * Get the index of field name in the coverage
     *
     * @param metadata
     * @param fieldName
     * @return
     */
    public int getRangeFieldIndex(WcpsCoverageMetadata metadata, String fieldName) {
        int index = 0;
        for (RangeField rangeField : metadata.getRangeFields()) {
            if (rangeField.getName().equals(fieldName)) {
                // e.g: c.red
                return index;
            } else if (fieldName.equals(String.valueOf(index))) {
                // e.g: c.1
                return index;
            }
            index++;
        }
        // Cannot found range field in coverage then throws error
        throw new RangeFieldNotFound(fieldName);
    }

    /**
     * Checks if the range field is present in the coverage.
     *
     * @param metadata
     * @param fieldName
     * @return
     */
    public boolean checkIfRangeFieldExists(WcpsCoverageMetadata metadata, String fieldName) {
        boolean found = false;
        for (RangeField rangeField : metadata.getRangeFields()) {
            if (rangeField.getName().equals(fieldName)) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Checks if the selected range is at a valid index for the current
     * coverage.
     *
     * @param metadata
     * @param rangeField
     * @return
     */
    public boolean checkRangeFieldNumber(WcpsCoverageMetadata metadata, int rangeField) {
        if (rangeField > metadata.getRangeFields().size() - 1 || rangeField < 0) {
            return false;
        }
        return true;
    }

    /**
     * Remove all the un-unsed range fields from coverageExpression's metadata,
     * if at least 1 range field is used. e.g: coverage has 3 bands, but only 1
     * band is used (e.g: c.b1) then b2, b3 need to be removed from expression
     * (c.b1)
     *
     * @param metadata
     * @param rangeFieldIndex
     */
    public void removeUnusedRangeFields(WcpsCoverageMetadata metadata, int rangeFieldIndex) {
        RangeField rangeField = metadata.getRangeFields().get(rangeFieldIndex);
        // clear the range field list
        metadata.getRangeFields().clear();
        metadata.getRangeFields().add(rangeField);
    }
    
    /**
     * 
     * From the grid domain (e.g: output of sdom(Rasql query)), create a WCPS Axis
     * which is used in WCPS coverage metadata by the index of this axis in sdom().
     * 
     */
    public Axis createAxisByGridBounds(Integer index, NumericSubset gridBounds) {
        String axisLabel = Axis.createAxisLabelByIndex(index);
        BigDecimal scalarResolution = CrsUtil.INDEX_SCALAR_RESOLUTION;
        String axisType = AxisTypes.X_AXIS;
        String axisUoM = CrsUtil.INDEX_UOM;
        String crsURI = CrsUtil.OPENGIS_INDEX_ND_PATTERN.replace(CrsUtil.INDEX_CRS_PATTERN_NUMBER, String.valueOf(index + 1));
        CrsDefinition crsDefinition = null;
        Axis axis = new RegularAxis(axisLabel, gridBounds, gridBounds, gridBounds, crsURI, 
                                    crsDefinition, axisType, axisUoM, index, gridBounds.getLowerLimit(), scalarResolution, gridBounds);
        return axis;
    }
    
    /**
     * Create a new WcpsCoverageMetadata object from a source WcpsCoverageMetadata object and a number of output coverage's axes (N).
     * The coverage's metadata output will contain IndexND axis instead of geo-CRS or CRS-compound as source object.
     * NOTE: the bounding boxes (geo/grid axes) of output coverage are unknown. e.g: clip a 2D oblique polygon on a 3D coverages (Lat, Long, Time).
     * In WCPS level, there is no understanding about the bounding box of this polygon, so set it with a constant value.
     * 
     * @param sourceMetadata a WcpsCoverageMetadata object
     * @param domains sdom() of clipped output which only can be determined by sending a clipping rasql query to rasserver.
     * @return WcpsCoverageMetadata object
     */
    public WcpsCoverageMetadata createCoverageByIndexAxes(WcpsCoverageMetadata sourceMetadata, List<Pair<String, String>> domains) throws PetascopeException {
        Integer numberOfAxes = domains.size();
        String coverageName = sourceMetadata.getCoverageName();
        String coverageType = sourceMetadata.getCoverageType();
        String crsURI = CrsUtil.OPENGIS_INDEX_ND_PATTERN.replace(CrsUtil.INDEX_CRS_PATTERN_NUMBER, numberOfAxes.toString());
        List<RangeField> rangeFields = sourceMetadata.getRangeFields();
        List<List<NilValue>> nilValues = sourceMetadata.getNilValues();
        String metadata = sourceMetadata.getMetadata();
        
        // Create index axes
        List<Axis> axes = new ArrayList<>();
        
        // Create index axes for output coverage
        for (int i = 0; i < numberOfAxes; i++) {
            // NOTE: output coverage is unknown about the bounding box, so set with a constant value for geo and grid domains.
            BigDecimal lowerBound = new BigDecimal(domains.get(i).fst);
            BigDecimal upperBound = new BigDecimal(domains.get(i).snd);
            NumericSubset gridBounds = new NumericTrimming(lowerBound, upperBound);
            Axis axis = this.createAxisByGridBounds(i, gridBounds);
            axes.add(axis);
        }
        
        WcpsCoverageMetadata outputMetadata = new WcpsCoverageMetadata(coverageName, coverageName, coverageType, axes, crsURI, rangeFields, nilValues, metadata, sourceMetadata.getAxes());
        return outputMetadata;
    }

    /**
     * Creates a coverage for the coverage constructor. Right now, this is not
     * geo-referenced.
     *
     * @param coverageName
     * @param numericSubsets
     * @return
     */
    public WcpsCoverageMetadata createCoverage(String coverageName, List<Subset> numericSubsets) throws PetascopeException {
        //create a new axis for each subset
        List<Axis> axes = new ArrayList();
        int axesCounter = 0;
        for (Subset numericSubset : numericSubsets) {
            String label = numericSubset.getAxisName();

            NumericSubset geoBounds = null;
            NumericSubset originalGridBounds = null;
            NumericSubset gridBounds = null;

            BigDecimal origin = null;

            if (numericSubset.getNumericSubset() instanceof NumericTrimming) {
                BigDecimal lowerLimit = ((NumericTrimming) numericSubset.getNumericSubset()).getLowerLimit();
                BigDecimal upperLimit = ((NumericTrimming) numericSubset.getNumericSubset()).getUpperLimit();

                // trimming
                geoBounds = new NumericTrimming(lowerLimit, upperLimit);
                originalGridBounds = new NumericTrimming(lowerLimit, upperLimit);
                //for now, the geoDomain is the same as the gridDomain, as we do no conversion in the coverage constructor / condenser
                gridBounds = new NumericTrimming(lowerLimit, upperLimit);
                origin = lowerLimit;
            } else {
                BigDecimal bound = ((NumericSlicing) numericSubset.getNumericSubset()).getBound();

                // slicing
                geoBounds = new NumericSlicing(bound);
                originalGridBounds = new NumericSlicing(bound);
                gridBounds = new NumericSlicing(bound);
                origin = bound;
            }

            // the crs of axis
            String crsUri = CrsUtil.INDEX_CRS_PATTERN;

            // the created coverage now is only RectifiedGrid then it will use GridSpacing UoM
            String axisUoM = CrsUtil.INDEX_UOM;

            // Create a crsDefintion by crsUri
            CrsDefinition crsDefinition = null;
            if (crsUri != null && !crsUri.equals("")) {
                CrsUtility.getCrsDefinitionByCrsUri(crsUri);
            }

            // the axis type (x, y, t,...) should be set to axis correctly, now just set to x
            String axisType = AxisTypes.X_AXIS;

            // Scalar resolution is set to 1
            BigDecimal scalarResolution = CrsUtil.INDEX_SCALAR_RESOLUTION;

            Axis axis = new RegularAxis(label, geoBounds, originalGridBounds, gridBounds, crsUri,
                    crsDefinition, axisType, axisUoM, axesCounter, origin, scalarResolution, geoBounds);
            axesCounter++;
            axes.add(axis);
        }
        //the current crs is IndexND CRS. When the coverage constructor will support geo referencing, the CrsService should
        //deduce the crs from the crses of the axes
        // NOTE: now, just use IndexND CRS (e.g: http://.../IndexND) to set as crs for creating coverage first
        String indexNDCrsUri = CrsUtility.createIndexNDCrsUri(axes);
        List<RangeField> rangeFields = new ArrayList<>();
        RangeField rangeField = new RangeField(RangeField.DATA_TYPE, RangeField.DEFAULT_NAME, null, new ArrayList<NilValue>(), RangeField.UOM_CODE, null, null);
        rangeFields.add(rangeField);

        List<List<NilValue>> nilValues = new ArrayList<>();

        WcpsCoverageMetadata result = new WcpsCoverageMetadata(coverageName, null, XMLSymbols.LABEL_GRID_COVERAGE, axes, indexNDCrsUri, rangeFields, nilValues, "", axes);
        return result;
    }

    /**
     * Apply trimming subset to regular/irregular axis and change geo bounds and
     * grid bounds of coverage metadata e.g: subset: Lat(0:20) with coverage
     * (Lat(0:70)) then need to update coverage metadata with geo bound(0:20)
     * and correspondent translated grid bound from the new geo bound.
     *
     */
    private void applyTrimmingSubset(Boolean checkGeoBound, boolean checkGridBound, WcpsCoverageMetadata metadata, WcpsSubsetDimension subsetDimension, Subset numericSubset, Axis axis) throws PetascopeException {

        boolean geoToGrid = true;
        //set the lower, upper bounds and crs
        BigDecimal lowerLimit = ((NumericTrimming) numericSubset.getNumericSubset()).getLowerLimit();
        BigDecimal upperLimit = ((NumericTrimming) numericSubset.getNumericSubset()).getUpperLimit();
        if (CrsUtil.isGridCrs(numericSubset.getCrs())) {
            // it will need to calculate from grid bound to geo bound (e.g: Lat:"CRS:1"(0:50) -> Lat(0:20))
            geoToGrid = false;
        }

        ParsedSubset<BigDecimal> parsedSubset = new ParsedSubset<>(lowerLimit, upperLimit);
        
        // Check if trim (lo > high)
        if (lowerLimit.compareTo(upperLimit) > 0) {
            String lowerBound = ((WcpsTrimSubsetDimension)subsetDimension).getLowerBound();
            String upperBound = ((WcpsTrimSubsetDimension)subsetDimension).getUpperBound();
            throw new InvalidSubsettingException(axis.getLabel(), lowerBound, upperBound);
        }

        if (checkGeoBound) {
            // NOTE: if crs is not CRS:1 then need to check valid geo boundary otherwise check valid grid boundary
            if (geoToGrid) {
                validParsedSubsetGeoBounds(subsetDimension, parsedSubset, axis);
            } else {
                validParsedSubsetGridBounds(subsetDimension, parsedSubset, axis);
            }
        }

        // Translate geo subset -> grid subset or grid subset -> geo subset
        NumericTrimming unAppliedNumericSubset = null;
        NumericTrimming unTranslatedNumericSubset = null;

        // NOTE: with grid coverage, geo bound is same as grid bound, no need to translate from grid bound to geo bound as in case of Lat:"CRS:1"(0:300) and geo bound is Lat(20.5:30.7)
        if (metadata.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
            NumericSubset tmpNumericSubset = new NumericTrimming(lowerLimit, upperLimit);
            axis.setGeoBounds(tmpNumericSubset);
            axis.setGridBounds(tmpNumericSubset);

            return;
        }

        if (geoToGrid) {
            // Lat(0:20) -> c[0:50]
            // Apply subset from geo domain to grid domain
            unAppliedNumericSubset = (NumericTrimming) axis.getGeoBounds();
            unTranslatedNumericSubset = (NumericTrimming) axis.getGridBounds();
            this.translateTrimmingGeoToGridSubset(checkGeoBound, checkGridBound, axis, subsetDimension, numericSubset, unAppliedNumericSubset, unTranslatedNumericSubset);
        } else {
            // Lat:"CRS:1"[0:50] -> Lat(0:20)
            // Apply subset from grid domain to geo domain
            unAppliedNumericSubset = (NumericTrimming) axis.getGridBounds();
            unTranslatedNumericSubset = (NumericTrimming) axis.getGeoBounds();
            this.translateTrimmingGridToGeoSubset(axis, subsetDimension, numericSubset, unAppliedNumericSubset, unTranslatedNumericSubset);
            
            axis.setTransatedGridToGeoBounds(true);
        }
    }

    /**
     * Apply the trimming subset on the unAppliedNumericSubset (geo bound) and
     * calculate this bound to unTranslatedNumericSubset (grid bound)
     */
    private void translateTrimmingGeoToGridSubset(boolean checkBoundary, boolean checkGridBound, Axis axis, WcpsSubsetDimension subsetDimension, Subset numericSubset,
            NumericTrimming unAppliedNumericSubset, NumericTrimming unTranslatedNumericSubset) throws PetascopeException {
        
        BigDecimal geoDomainMin = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit();
        BigDecimal geoDomainMax = ((NumericTrimming) axis.getGeoBounds()).getUpperLimit();
        BigDecimal gridDomainMin = ((NumericTrimming) axis.getGridBounds()).getLowerLimit();
        BigDecimal gridDomainMax = ((NumericTrimming) axis.getGridBounds()).getUpperLimit();
            
        BigDecimal lowerLimit = ((NumericTrimming) numericSubset.getNumericSubset()).getLowerLimit();
        BigDecimal upperLimit = ((NumericTrimming) numericSubset.getNumericSubset()).getUpperLimit();

        // Apply the subset on the unAppliedNumericSubset
        unAppliedNumericSubset.setLowerLimit(lowerLimit);
        unAppliedNumericSubset.setUpperLimit(upperLimit);
        
        ParsedSubset<BigDecimal> parsedSubset = null;
        ParsedSubset<Long> translatedSubset = null;
        
        BigDecimal geoDistance = upperLimit.subtract(lowerLimit);
        BigDecimal halfGeoResolution = BigDecimalUtil.divide(axis.getResolution(), new BigDecimal("2")).abs();
        // NOTE: gdal_translate doesn't allow to subset by projwin with less than half grid pixel
        boolean lessThanHalfPixel = geoDistance.compareTo(halfGeoResolution) < 0;
        
        if (axis instanceof RegularAxis && !lessThanHalfPixel && !axis.isNonXYAxis()) {
            String sourceCRS = axis.getNativeCrsUri();
            String sourceCRSWKT = CrsUtil.getWKT(sourceCRS);
            
            // e.g: [0:4] = 4 - 0 + 1 = 5 pixels
            int numberOfGridPixels = gridDomainMax.subtract(gridDomainMin).intValue() + 1;

            if (axis.getResolution().compareTo(BigDecimal.ZERO) > 0) {
                // axis X
                
                GeoTransform adfGeoTransform = new GeoTransform(sourceCRSWKT, geoDomainMin, BigDecimal.ZERO, numberOfGridPixels, 0, axis.getResolution(), BigDecimal.ZERO);
                Pair<ParsedSubset<BigDecimal>, ParsedSubset<Long>> pairX = coordinateTranslationService.calculateGeoGridXBounds(checkBoundary, checkGridBound, axis, adfGeoTransform, lowerLimit, upperLimit);
                parsedSubset = pairX.fst;
                translatedSubset = pairX.snd;
                
                axis.setGeoBounds(new NumericTrimming(parsedSubset.getLowerLimit(), parsedSubset.getUpperLimit()));
            } else if (axis.getResolution().compareTo(BigDecimal.ZERO) < 0) {
                // axis Y
            
                GeoTransform adfGeoTransform = new GeoTransform(sourceCRSWKT, BigDecimal.ZERO, geoDomainMax, 0, numberOfGridPixels, BigDecimal.ZERO, axis.getResolution());
                Pair<ParsedSubset<BigDecimal>, ParsedSubset<Long>> pairY = coordinateTranslationService.calculateGeoGridYBounds(checkBoundary, checkGridBound, axis, adfGeoTransform, lowerLimit, upperLimit);
                parsedSubset = pairY.fst;
                translatedSubset = pairY.snd;
                
                axis.setGeoBounds(new NumericTrimming(parsedSubset.getLowerLimit(), parsedSubset.getUpperLimit()));
            }
        } else {
            // other axis type
            
            parsedSubset = new ParsedSubset<>(lowerLimit, upperLimit);
            // store the translated grid bounds from the subsets
            translatedSubset = translateGeoToGridCoordinates(subsetDimension, parsedSubset, axis,
                    geoDomainMin, geoDomainMax, gridDomainMin, gridDomainMax);
            
            if (lessThanHalfPixel) {
                // e.g: Axis Long with geo resolution: 10, geo min bound = 0
                // Long(9,11) returns grid intervals[0,1], but the distance 11-9 = 2 < half grid resolution: 10 / 2
                // Then, grid lower bound = grid upper bound                
                translatedSubset.setUpperLimit(translatedSubset.getLowerLimit());
                parsedSubset.setUpperLimit(lowerLimit);
            }
        }

        // Set the correct translated grid parsed subset to axis
        unTranslatedNumericSubset.setLowerLimit(new BigDecimal(translatedSubset.getLowerLimit().toString()));
        unTranslatedNumericSubset.setUpperLimit(new BigDecimal(translatedSubset.getUpperLimit().toString()));
    }

    /**
     * Apply the trimming subset on the unAppliedNumericSubset (grid bound) and
     * calculate this bound to unTranslatedNumericSubset (geo bound)
     */
    private void translateTrimmingGridToGeoSubset(Axis axis, WcpsSubsetDimension subsetDimension, Subset numericSubset,
            NumericTrimming unAppliedNumericSubset,
            NumericTrimming unTranslatedNumericSubset) {
        BigDecimal geoDomainMin = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit();
        BigDecimal gridDomainMin = ((NumericTrimming) axis.getGridBounds()).getLowerLimit();
        BigDecimal gridDomainMax = ((NumericTrimming) axis.getGridBounds()).getUpperLimit();

        BigDecimal lowerLimit = ((NumericTrimming) numericSubset.getNumericSubset()).getLowerLimit();
        BigDecimal upperLimit = ((NumericTrimming) numericSubset.getNumericSubset()).getUpperLimit();

        // Apply the subset on the unAppliedNumericSubset
        unAppliedNumericSubset.setLowerLimit(lowerLimit);
        unAppliedNumericSubset.setUpperLimit(upperLimit);

        ParsedSubset<BigDecimal> parsedSubset = new ParsedSubset<>(lowerLimit, upperLimit);
        // store the translated grid bounds from the subsets
        ParsedSubset<BigDecimal> translatedSubset = translateGridToGeoCoordinates(parsedSubset, axis,
                geoDomainMin, gridDomainMin, gridDomainMax);

        // Set the correct translated grid parsed subset to axis
        unTranslatedNumericSubset.setLowerLimit(translatedSubset.getLowerLimit());
        unTranslatedNumericSubset.setUpperLimit(translatedSubset.getUpperLimit());
    }

    /**
     * Apply slicing subset to regular/irregular axis and change geo bounds and
     * grid bounds of coverage metadata e.g: subset: Lat(20) with coverage
     * (Lat(0:70)) then need to update coverage metadata with geo bound(20) and
     * correspondent translated grid bound from the new geo bound.
     *
     * @param checkGeoBound should subset needed to be check within boundary
     * (e.g: scale(..., {subset}) does not need to check)
     */
    private void applySlicing(Boolean checkGeoBound, boolean checkGridBound, WcpsCoverageMetadata metadata, WcpsSubsetDimension subsetDimension,
            Subset numericSubset, Axis axis) throws PetascopeException {
        boolean geoToGrid = true;
        BigDecimal bound = ((NumericSlicing) numericSubset.getNumericSubset()).getBound();

        if (CrsUtil.isGridCrs(numericSubset.getCrs())) {
            // it will need to calculate from grid bound to geo bound (e.g: Lat:"http://.../Index2D"(0:50) -> Lat(0:20))
            geoToGrid = false;
        }

        ParsedSubset<BigDecimal> geoParsedSubset = new ParsedSubset<>(bound);
        // check if parsed subset is valid
        if (checkGeoBound) {
            // NOTE: if crs is not CRS:1 then need to check valid geo boundary otherwise check valid grid boundary.
            if (!CrsUtil.isGridCrs(numericSubset.getCrs())) {
                validParsedSubsetGeoBounds(subsetDimension, geoParsedSubset, axis);
            } else {
                validParsedSubsetGridBounds(subsetDimension, geoParsedSubset, axis);
            }
        }

        // Translate geo subset -> grid subset or grid subset -> geo subset
        // NOTE: with grid coverage, geo bound is same as grid bound, no need to translate from grid bound to geo bound as in case of Lat:"CRS:1"(300) and geo bound is Lat(30.7)        
        if (metadata.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
            NumericSubset tmpNumericSubset = new NumericSlicing(bound);
            axis.setGeoBounds(tmpNumericSubset);
            axis.setGridBounds(tmpNumericSubset);

            return;
        }

        if (geoToGrid) {
            this.translateSlicingGeoToGridSubset(axis, subsetDimension, numericSubset);
        } else {
            this.translateSlicingGridToGeoSubset(axis, numericSubset);
        }
    }

    /**
     * Apply the slicing subset on the (geo bound) and calculate this bound to
     * (grid bound)
     */
    private void translateSlicingGeoToGridSubset(Axis axis, WcpsSubsetDimension subsetDimension, Subset numericSubset) throws PetascopeException {

        BigDecimal bound = ((NumericSlicing) numericSubset.getNumericSubset()).getBound();
        ParsedSubset<BigDecimal> parsedSubset = new ParsedSubset<>(bound);

        // Translate the coordinate in georeferenced to grid.
        BigDecimal geoDomainMin;
        BigDecimal geoDomainMax;
        BigDecimal gridDomainMin;
        BigDecimal gridDomainMax;

        // NOTE: before applying slicing subset on axis, it can be trimming ( e.g: slice(c[Lat(0:20)]), {Lat(5)}) )
        if (axis.getGridBounds() instanceof NumericSlicing) {
            // slicing axis
            geoDomainMin = ((NumericSlicing) axis.getGeoBounds()).getBound();
            geoDomainMax = geoDomainMin;
            gridDomainMin = ((NumericSlicing) axis.getGridBounds()).getBound();
            gridDomainMax = gridDomainMin;
        } else {
            // trimming axis
            geoDomainMin = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit();
            geoDomainMax = ((NumericTrimming) axis.getGeoBounds()).getUpperLimit();
            gridDomainMin = ((NumericTrimming) axis.getGridBounds()).getLowerLimit();
            gridDomainMax = ((NumericTrimming) axis.getGridBounds()).getUpperLimit();
        }

        // NOTE: numeric type of axis here can be trimming when building axes for the coverage, it need to be change to slicing
        NumericSlicing numericSlicingBound = new NumericSlicing(bound);
        // Lat(20) -> c(50)
        // Apply geo slicing to grid slicing
        axis.setGeoBounds(numericSlicingBound);

        // store the translated grid bounds from the subsets
        ParsedSubset<Long> translatedSubset = this.translateGeoToGridCoordinates(subsetDimension, parsedSubset, axis,
                geoDomainMin, geoDomainMax, gridDomainMin, gridDomainMax);

        // Set the correct translated grid parsed subset to axis
        numericSlicingBound = new NumericSlicing(new BigDecimal(translatedSubset.getLowerLimit().toString()));
        axis.setGridBounds(numericSlicingBound);
    }

    /**
     * Apply the slicing subset on the (grid bound) and calculate this bound to
     * (geo bound)
     *
     * @param axis
     * @param subset
     */
    private void translateSlicingGridToGeoSubset(Axis axis, Subset subset) {

        BigDecimal bound = ((NumericSlicing) subset.getNumericSubset()).getBound();
        ParsedSubset<BigDecimal> parsedSubset = new ParsedSubset<>(bound, bound);

        // Translate the coordinate in georeferenced to grid.
        BigDecimal geoDomainMin;
        BigDecimal gridDomainMin;
        BigDecimal gridDomainMax;

        // NOTE: before applying slicing subset on axis, it can be trimming ( e.g: slice(c[Lat(0:20)]), {Lat(5)}) )
        if (axis.getGridBounds() instanceof NumericSlicing) {
            // slicing axis
            geoDomainMin = ((NumericSlicing) axis.getGeoBounds()).getBound();
            gridDomainMin = ((NumericSlicing) axis.getGridBounds()).getBound();
            gridDomainMax = gridDomainMin;
        } else {
            // trimming axis
            geoDomainMin = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit();
            gridDomainMin = ((NumericTrimming) axis.getGridBounds()).getLowerLimit();
            gridDomainMax = ((NumericTrimming) axis.getGridBounds()).getUpperLimit();
        }

        // NOTE: numeric type of axis here can be trimming when building axes for the coverage, it need to be change to slicing
        NumericSlicing numericSlicingBound = new NumericSlicing(bound);
        // Lat:"CRS:1"(50) -> Lat(20)
        // Apply grid slicing to geo slicing
        axis.setGridBounds(numericSlicingBound);

        // store the translated grid bounds from the subsets
        ParsedSubset<BigDecimal> translatedSubset = this.translateGridToGeoCoordinates(parsedSubset, axis,
                geoDomainMin, gridDomainMin, gridDomainMax);
        // Set the correct translated grid parsed subset to axis
        numericSlicingBound = new NumericSlicing(translatedSubset.getLowerLimit());
        axis.setGeoBounds(numericSlicingBound);
    }

    /**
     * Translate a trimming/slicing from geo-referenced coordinates to grid
     * coordinates e.g: Lat(25.5:35.5) -> Lat:"CRS:1"(0:20)
     */
    public ParsedSubset<Long> translateGeoToGridCoordinates(WcpsSubsetDimension subsetDimension, ParsedSubset<BigDecimal> parsedSubset,
                                                            Axis axis, BigDecimal geoDomainMin,
                                                            BigDecimal geoDomainMax, BigDecimal gridDomainMin,
                                                            BigDecimal gridDomainMax) throws PetascopeException {
        ParsedSubset<Long> translatedSubset;
        // Regular axis (no need to query database)
        if (axis instanceof RegularAxis) {
            BigDecimal resolution = ((RegularAxis) axis).getResolution();
            // Lat(0:20) -> c[0:50]
            translatedSubset = coordinateTranslationService.geoToGridForRegularAxis(parsedSubset, geoDomainMin,
                    geoDomainMax, resolution, gridDomainMin);
        } else {
            IrregularAxis irregularAxis = (IrregularAxis)axis;
            
            // Irregular axis (query database for coefficients)
            BigDecimal scalarResolution = axis.getResolution();
            // e.g: ansi(148654) in irr_cube_2 -> c[0]
            translatedSubset = coordinateTranslationService.geoToGridForIrregularAxes(subsetDimension, parsedSubset, scalarResolution,
                                                                                      gridDomainMin, gridDomainMax, geoDomainMin, irregularAxis);
        }
        return translatedSubset;
    }
    
    /**
     * Make sure the input WCPS metadata object doesn't have larger geo/grid bounds for XY axes from original geo/grid bboxes
     */
    public void adjustXYGeoGridBounds(WcpsCoverageMetadata wcpsCoverageMetadata) {
        
        if (wcpsCoverageMetadata.hasXYAxes()) {
        
            List<Axis> inputXYAxes = wcpsCoverageMetadata.getXYAxes();
            Axis axisX = inputXYAxes.get(0);
            Axis axisY = inputXYAxes.get(1);

            NumericTrimming subsetGeoX = new NumericTrimming(axisX.getGeoBounds().getLowerLimit(), axisX.getGeoBounds().getUpperLimit());
            NumericTrimming subsetGeoY = new NumericTrimming(axisY.getGeoBounds().getLowerLimit(), axisY.getGeoBounds().getUpperLimit());

            NumericTrimming subsetGridX = new NumericTrimming(axisX.getGridBounds().getLowerLimit(), axisX.getGridBounds().getUpperLimit());
            NumericTrimming subsetGridY = new NumericTrimming(axisY.getGridBounds().getLowerLimit(), axisY.getGridBounds().getUpperLimit());

            // -- geo lower bounds
            if (axisX.getGeoBounds().getLowerLimit().compareTo(axisX.getOriginalGeoBounds().getLowerLimit()) < 0) {
                subsetGeoX.setLowerLimit(axisX.getOriginalGeoBounds().getLowerLimit());
            }
            if (axisY.getGeoBounds().getLowerLimit().compareTo(axisY.getOriginalGeoBounds().getLowerLimit()) < 0) {
                subsetGeoY.setLowerLimit(axisY.getOriginalGeoBounds().getLowerLimit());
            }

            // geo upper bounds
            if (axisX.getGeoBounds().getUpperLimit().compareTo(axisX.getOriginalGeoBounds().getUpperLimit()) > 0) {
                subsetGeoX.setUpperLimit(axisX.getOriginalGeoBounds().getUpperLimit());
            }
            if (axisY.getGeoBounds().getUpperLimit().compareTo(axisY.getOriginalGeoBounds().getUpperLimit()) > 0) {
                subsetGeoY.setUpperLimit(axisY.getOriginalGeoBounds().getUpperLimit());         
            }
                       
            // -- grid lower bounds
            if (axisX.getGridBounds().getLowerLimit().compareTo(axisX.getOriginalGridBounds().getLowerLimit()) < 0) {
                subsetGridX.setLowerLimit(axisX.getOriginalGridBounds().getLowerLimit());
            }
            if (axisX.getGridBounds().getLowerLimit().compareTo(axisX.getOriginalGridBounds().getUpperLimit()) > 0) {
                subsetGridX.setLowerLimit(axisX.getOriginalGridBounds().getUpperLimit());
            }
            if (axisY.getGridBounds().getLowerLimit().compareTo(axisY.getOriginalGridBounds().getLowerLimit()) < 0) {
                subsetGridY.setLowerLimit(axisY.getOriginalGridBounds().getLowerLimit());
            }
            if (axisY.getGridBounds().getLowerLimit().compareTo(axisY.getOriginalGridBounds().getUpperLimit()) > 0) {
                subsetGridY.setLowerLimit(axisY.getOriginalGridBounds().getUpperLimit());
            }

            // grid upper bounds
            if (axisX.getGridBounds().getUpperLimit().compareTo(axisX.getOriginalGridBounds().getUpperLimit()) > 0) {
                subsetGridX.setUpperLimit(axisX.getOriginalGridBounds().getUpperLimit());
            }
            if (axisX.getGridBounds().getUpperLimit().compareTo(axisX.getOriginalGridBounds().getLowerLimit()) < 0) {
                subsetGridX.setUpperLimit(axisX.getOriginalGridBounds().getLowerLimit());
            }
            if (axisY.getGridBounds().getUpperLimit().compareTo(axisY.getOriginalGridBounds().getUpperLimit()) > 0) {
                subsetGridY.setUpperLimit(axisY.getOriginalGridBounds().getUpperLimit());
            }
            if (axisY.getGridBounds().getUpperLimit().compareTo(axisY.getOriginalGridBounds().getLowerLimit()) < 0) {
                subsetGridY.setUpperLimit(axisY.getOriginalGridBounds().getLowerLimit());
            }
            
            if (subsetGridX.getLowerLimit().compareTo(subsetGridX.getUpperLimit()) > 0) {
                subsetGridX.setLowerLimit(subsetGridX.getUpperLimit());
            }
            if (subsetGridY.getLowerLimit().compareTo(subsetGridY.getUpperLimit()) > 0) {
                subsetGridY.setLowerLimit(subsetGridY.getUpperLimit());
            }
            
            // Adjust the geo bounds
            // axis X
            if (subsetGridX.getLowerLimit().compareTo(subsetGridX.getUpperLimit()) == 0) {
                if (subsetGeoX.getLowerLimit().compareTo(axisX.getOriginalGeoBounds().getLowerLimit()) == 0) {
                    // min geo bound corner
                    BigDecimal upperBound = subsetGeoX.getLowerLimit().add(axisX.getResolution().abs());
                    subsetGeoX.setUpperLimit(upperBound);
                } else if (subsetGeoX.getLowerLimit().compareTo(axisX.getOriginalGeoBounds().getUpperLimit()) == 0) {
                    // max geo bound corner
                    BigDecimal lowerBound = subsetGeoX.getLowerLimit().subtract(axisX.getResolution().abs());
                    subsetGeoX.setLowerLimit(lowerBound);
                }
            }
            
            // axis Y
            if (subsetGridY.getLowerLimit().compareTo(subsetGridY.getUpperLimit()) == 0) {
                if (subsetGeoY.getLowerLimit().compareTo(axisY.getOriginalGeoBounds().getLowerLimit()) == 0) {
                    // min geo bound corner
                    BigDecimal upperBound = subsetGeoY.getLowerLimit().add(axisY.getResolution().abs());
                    subsetGeoY.setUpperLimit(upperBound);
                } else if (subsetGeoY.getLowerLimit().compareTo(axisY.getOriginalGeoBounds().getUpperLimit()) == 0) {
                    // max geo bound corner
                    BigDecimal lowerBound = subsetGeoY.getLowerLimit().subtract(axisY.getResolution().abs());
                    subsetGeoY.setLowerLimit(lowerBound);
                }
            }
            
            axisX.setGeoBounds(subsetGeoX);
            axisX.setGridBounds(subsetGridX);

            axisY.setGeoBounds(subsetGeoY);
            axisY.setGridBounds(subsetGridY);
            
        }
    }
    
    /**
     * Given one input axis, create a WCPS metadata object with one axis as grid domains and CRS:1 CRS
     * used for imageCrsdomain() handler result
     * NOTE: this is used to determine in the case of axis iterator in condenser over $pt t (imageCrsdomain(c[time("2015":"2015")], t))
     */
    public WcpsCoverageMetadata generateWcpsMetadataWithOneGridAxis(String coverageId, Axis axis) throws PetascopeException {
        List<Axis> axesTmp = new ArrayList<>();
        axesTmp.add(axis);
        
        String crs = CrsUtil.GRID_CRS;
        if (!axis.getNativeCrsUri().equals(CrsUtil.GRID_CRS)) {
            crs = axis.getNativeCrsUri();
        }
        
        // NOTE: this is used to determine in the case of axis iterator in condenser over $pt t (imageCrsdomain(c[time("2015":"2015")], t))
        WcpsCoverageMetadata tmpMetadata = new WcpsCoverageMetadata(coverageId, null, null, axesTmp, crs, null, null, null, null);
        return tmpMetadata;
    }

    /**
     * Translate from trimming/slicing grid bound to geo bounds e.g:
     * Lat:"CRS:1"(0:20) -> Lat(25.5:35.5)
     *
     * @param parsedSubset
     * @param axis
     * @param geoDomainMin
     * @param gridDomainMin
     * @param gridDomainMax
     * @return
     * @throws PetascopeException
     */
    private ParsedSubset<BigDecimal> translateGridToGeoCoordinates(ParsedSubset<BigDecimal> parsedSubset,
            Axis axis, BigDecimal geoDomainMin, BigDecimal gridDomainMin,
            BigDecimal gridDomainMax) {
        ParsedSubset<BigDecimal> translatedSubset;
        // Regular axis (no need to query database)
        if (axis instanceof RegularAxis) {
            BigDecimal resolution = ((RegularAxis) axis).getResolution();
            // Lat:"CRS:1"(0:50) -> [0:50]
            translatedSubset = coordinateTranslationService.gridToGeoForRegularAxis(parsedSubset, gridDomainMin, gridDomainMax, resolution, geoDomainMin);
        } else {
            // Irregular axis (query database for coefficients)
            // NOTE: if subsettingCrs is CRS:1, ( e.g: ansi:"CRS:1"(0) ) in irr_cube_2
            // Then the geo bound is the coefficient + axis original origin
            // e.g: test_irr_cube_2, ansi:"CRS:1"(0) then coefficient is 0, the original origin is the dateTime in number from the first date (note the AnsiDate's origin)
            
            // e.g: coverage has list of coefficents (-30, -20, -10, -2, 0, 5, 7) and request ansi:"CRS:1"(-1), then the index of ":CRS:1"(-1) in the list is: -3.
            IrregularAxis irregularAxis = ((IrregularAxis) axis);
            
            int lowerCoefficientIndex = parsedSubset.getLowerLimit().intValue() - irregularAxis.getOriginalGridBounds().getLowerLimit().intValue();
            int upperCoefficientIndex = parsedSubset.getUpperLimit().intValue() - irregularAxis.getOriginalGridBounds().getLowerLimit().intValue();
            BigDecimal lowerCoefficient = irregularAxis.getDirectPositions().get(lowerCoefficientIndex);
            BigDecimal upperCoefficient = irregularAxis.getDirectPositions().get(upperCoefficientIndex);
            
            // Calculate the distance from this coefficient for CRS:1(GRID_INDEX) to coefficient zero.
            // (NOTE: coefficient zero can be in random position, not only the first element in list of directPositions)
            lowerCoefficient = ((IrregularAxis) axis).getLowestCoefficientValue().abs().add(lowerCoefficient);
            upperCoefficient = ((IrregularAxis) axis).getLowestCoefficientValue().abs().add(upperCoefficient);
            
            BigDecimal geoLowerBound = lowerCoefficient.add(axis.getOriginalOrigin());
            BigDecimal geoUpperBound = upperCoefficient.add(axis.getOriginalOrigin());

            translatedSubset = new ParsedSubset<>(geoLowerBound, geoUpperBound);
        }
        return translatedSubset;
    }

    /**
     * Check if parsed subset is inside the geo domain of the current of the
     * axis e.g: axis's geo domain: Lat(0:50), and parsed subset: Lat(15:70) is
     * out of upper bound
     */
    private void validParsedSubsetGeoBounds(WcpsSubsetDimension subsetDimension, ParsedSubset<BigDecimal> geoParsedSubset, Axis axis) throws PetascopeException {
        String axisName = axis.getLabel();
        
        BigDecimal lowerLimit = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit();
        BigDecimal upperLimit = ((NumericTrimming) axis.getGeoBounds()).getUpperLimit();
        
        // NOTE: in case the axis is flipped by flip operator
        Pair<BigDecimal, BigDecimal> pair = BigDecimalUtil.swapIfFirstLarger(lowerLimit, upperLimit);
        lowerLimit = pair.fst;
        upperLimit = pair.snd;
        
        ParsedSubset<String> subset = null;
        
        // Check if subset is inside the domain of geo bound
        if (geoParsedSubset.isSlicing()) {
            // slicing geo parsed subset (add/substract epsilon as the coordinate from crs transform can be approximately)
            if ((geoParsedSubset.getSlicingCoordinate().add(BigDecimalUtil.COEFFICIENT_DECIMAL_EPSILON).compareTo(lowerLimit) < 0)
                || (geoParsedSubset.getSlicingCoordinate().subtract(BigDecimalUtil.COEFFICIENT_DECIMAL_EPSILON).compareTo(upperLimit) > 0)) {
                
                // If the subset is datetime format
                if (axis.getLowerGeoBoundRepresentation().contains("\"")) {
                    subset = new ParsedSubset<>(((WcpsSliceSubsetDimension)subsetDimension).getBound());
                } else {
                    subset = new ParsedSubset<>(geoParsedSubset.getSlicingCoordinate().toPlainString());
                }
            }
        } else {
            String originalLowerBound = ((WcpsTrimSubsetDimension)subsetDimension).getLowerBound();
            String originalUpperBound = ((WcpsTrimSubsetDimension)subsetDimension).getUpperBound();
            
            // trimming geo parsed subset (add/substract epsilon as the coordinate from crs transform can be approximately)
            if ((geoParsedSubset.getLowerLimit().add(BigDecimalUtil.COEFFICIENT_DECIMAL_EPSILON).compareTo(lowerLimit) < 0)                    
                || (geoParsedSubset.getUpperLimit().subtract(BigDecimalUtil.COEFFICIENT_DECIMAL_EPSILON).compareTo(upperLimit) > 0)) {
                
                // If the subset is datetime format
                if (axis.getLowerGeoBoundRepresentation().contains("\"")) {
                    subset = new ParsedSubset<>(originalLowerBound, originalUpperBound);
                } else {
                    subset = new ParsedSubset<>(geoParsedSubset.getLowerLimit().toPlainString(), geoParsedSubset.getUpperLimit().toPlainString());
                }
            } else if ((geoParsedSubset.getLowerLimit().compareTo(upperLimit) > 0)
                    || (geoParsedSubset.getUpperLimit().compareTo(lowerLimit) < 0)) {
                throw new InvalidSubsettingException(axisName, originalLowerBound, originalUpperBound);
            }
        }
        
        if (subset != null) {
            String axisLowerGeoBound = axis.getLowerGeoBoundRepresentation();
            String axisUpperGeoBound = axis.getUpperGeoBoundRepresentation();
            throw new OutOfBoundsSubsettingException(GEO_TYPE, axisName, subset, axisLowerGeoBound, axisUpperGeoBound);
        }
    }

    /**
     * Check if parsed subset is inside the grid domain of the current of the
     * axis e.g: axis's grid domain: Lat(0:50), and parsed subset:
     * Lat:"CRS:1"(65:80) is out of upper bound
     *
     */
    private void validParsedSubsetGridBounds(WcpsSubsetDimension subsetDimension, ParsedSubset<BigDecimal> gridParsedSubset, Axis axis) {
        String axisName = axis.getLabel();
        
        BigDecimal lowerLimit = ((NumericTrimming) axis.getGridBounds()).getLowerLimit();
        BigDecimal upperLimit = ((NumericTrimming) axis.getGridBounds()).getUpperLimit();
        ParsedSubset<String> subset = null;

        // Check if subset is inside the domain of grid bound
        if (gridParsedSubset.isSlicing()) {
            // slicing grid parsed subset
            if ((gridParsedSubset.getSlicingCoordinate().compareTo(lowerLimit) < 0)
                    || (gridParsedSubset.getSlicingCoordinate().compareTo(upperLimit) > 0)) {

                // throw slicing error
                subset = new ParsedSubset<>(((WcpsSliceSubsetDimension)subsetDimension).getBound());                
            }
        } else {
            // trimming grid parsed subset
            BigDecimal gridSubsetLowerLimit = gridParsedSubset.getLowerLimit();
            BigDecimal gridSubsetUpperLimit = gridParsedSubset.getUpperLimit();
            
            BigDecimal gridSubsetDistance = gridSubsetUpperLimit.subtract(gridSubsetLowerLimit);
            BigDecimal axisGridDistance = upperLimit.subtract(lowerLimit);
            
            if (axisGridDistance.compareTo(gridSubsetDistance) < 0) {
            
                if ((gridSubsetLowerLimit.compareTo(lowerLimit) < 0)
                        || (gridSubsetLowerLimit.compareTo(upperLimit) > 0)
                        || (gridSubsetUpperLimit.compareTo(lowerLimit) < 0)
                        || (gridSubsetUpperLimit.compareTo(upperLimit) > 0)) {

                    if (subsetDimension instanceof WcpsTrimSubsetDimension) {
                        // throw trimming error
                        subset = new ParsedSubset<>(((WcpsTrimSubsetDimension)subsetDimension).getLowerBound(), ((WcpsTrimSubsetDimension)subsetDimension).getUpperBound());
                    } else {
                        // throw slicing error
                        subset = new ParsedSubset<>(((WcpsSliceSubsetDimension)subsetDimension).getBound());       
                    }
                }
                
            }
        }
        
        if (subset != null) {
            throw new OutOfBoundsSubsettingException(GRID_TYPE, axisName, subset, lowerLimit.toPlainString(), upperLimit.toPlainString());
        }
    }

    /**
     * Check if the subsets'names exist in the coverage's axes
     *
     * @param metadata
     * @param subsetList
     */
    private void checkSubsetConsistency(WcpsCoverageMetadata metadata, List<Subset> subsetList) {
        //check if all the subset axis exist in the coverage
        for (Subset dimension : subsetList) {
            if (!checkAxisExists(dimension.getAxisName(), metadata)) {
                throw new CoverageAxisNotFoundExeption(dimension.getAxisName());
            }
        }
        //check if the subset is withing the bounds of the axis is not done. @TODO: check if this is needed
    }

    private boolean checkAxisExists(String axisName, WcpsCoverageMetadata metadata) {
        boolean found = false;
        for (Axis axis : metadata.getAxes()) {
            if (CrsUtil.axisLabelsMatch(axis.getLabel(), axisName)) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Check if 2 coverages could be combined to 1 coverage via binary coverage expression (e.g: coverage_1 + coverage_2)
     */
    private void validateCoveragesCompatibility(WcpsCoverageMetadata firstMeta, WcpsCoverageMetadata secondMeta) {
        //we want to detect only the cases where an error should be thrown
        if (firstMeta != null && secondMeta != null) {
            //check number of axes to be the same
            if (firstMeta.getAxes().size() != secondMeta.getAxes().size()) {
                throw new IncompatibleAxesNumberException(firstMeta.getCoverageName(), secondMeta.getCoverageName(),
                        firstMeta.getAxes().size(), secondMeta.getAxes().size());
            }
            
            // Check axes' s types must be the same
            for (int i = 0; i < firstMeta.getAxes().size(); i++) {
                Axis firstAxis = firstMeta.getAxes().get(i);
                Axis secondAxis = secondMeta.getAxes().get(i);
                
                String firstAxisName = firstAxis.getLabel();
                String firstAxisType = firstAxis.getAxisType();
                String secondAxisName = secondAxis.getLabel();
                String secondAxisType = secondAxis.getAxisType();
                
                if (!(firstAxis.getNativeCrsUri().contains(CrsUtil.INDEX_CRS_PATTERN) 
                    || secondAxis.getNativeCrsUri().contains(CrsUtil.INDEX_CRS_PATTERN))) {
                    if (!firstAxisType.equals(secondAxisType)) {
                        String errorMessage = "Axis type is different, given first coverage's axis with name '" + firstAxisName + "', type '" + firstAxisType 
                                            + "' and second coverage's axis with name '" + secondAxisName + "', type '" + secondAxisType + "'.";
                        throw new IncompatibleCoveragesException(firstMeta.getCoverageName(), secondMeta.getCoverageName(), errorMessage);
                    }
                }
  
                // NOTE: dont' check if 2 coverages have same geo axes intervals or geo axes resolutions.
                // It is not correct in case of scaling 2 coverages to same grid intervals but they have different geo intervals.
                // e.g: scale(c[Lat(0:40), Long(30:50), { Lat:"CRS:1"(0:435), Long:"CRS:1"(0:1000) })
                //    + scale(d[Lat(120:140), Long(130:135), { Lat:"CRS:1"(0:435), Long:"CRS:1"(0:1000) })
                
                // Check also number of grid pixels for each axis must be the same size
                Long firstGridPixels = firstMeta.getAxes().get(i).getGeoBounds().getUpperLimit().subtract(firstMeta.getAxes().get(i).getGeoBounds().getUpperLimit()).abs().add(BigDecimal.ONE).longValue();
                Long secondGridPixels = secondMeta.getAxes().get(i).getGeoBounds().getUpperLimit().subtract(secondMeta.getAxes().get(i).getGeoBounds().getUpperLimit()).abs().add(BigDecimal.ONE).longValue();
                
                if (!firstGridPixels.equals(secondGridPixels)) {
                    String errorMessage = "Number of grid pixels is different, given first coverage's axis with name '" + firstAxisName + "', grid pixels '" + firstGridPixels
                                        + "' and second coverage's axis with name '" + secondAxisName + "', grid pixels '" + secondGridPixels + "'.";
                    throw new IncompatibleCoveragesException(firstMeta.getCoverageName(), secondMeta.getCoverageName(), errorMessage);
                }
                
                if (firstAxis instanceof IrregularAxis && secondAxis instanceof IrregularAxis) {
                    List<String> firstCoefficients;
                    List<String> secondCoefficients;
                    
                    try {
                        firstCoefficients = ((IrregularAxis)firstAxis).getCoefficientValues();
                        secondCoefficients = ((IrregularAxis)secondAxis).getCoefficientValues();
                    } catch (PetascopeException ex) {
                        throw new WCPSException(ExceptionCode.InternalComponentError, 
                                                "Failed to get coefficients from axis '" + firstAxisName + "'. Reason: " + ex.getExceptionText(), ex);
                    }
                    
                    if (firstCoefficients.size() != secondCoefficients.size()) {
                        String errorMessage = "Different number of coefficients in axis '" + firstAxisName
                                            + "'. Given '" + firstCoefficients.size() + "' and '" + secondCoefficients.size() + "' coefficients";
                        throw new IncompatibleCoveragesException(firstMeta.getCoverageName(), secondMeta.getCoverageName(), errorMessage);
                    }
                    
                    for (int j = 0; j < firstCoefficients.size(); j++) {
                        String firstCoefficient = firstCoefficients.get(j);
                        String secondCoefficient = secondCoefficients.get(j);
                        
                        if (!firstCoefficient.equals(secondCoefficient)) {
                            String errorMessage = "Coefficient '" + firstCoefficient + "' of axis '" + firstAxisName + "' "
                                                + "exists in the first coverage, but not in the second coverage";
                            throw new IncompatibleCoveragesException(firstMeta.getCoverageName(), secondMeta.getCoverageName(), errorMessage);
                        }
                    }
                }
            }
        }
    }
}
