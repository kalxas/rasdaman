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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.metadata.service;

import petascope.swe.datamodel.NilValue;
import petascope.util.XMLSymbols;
import petascope.wcps2.error.managed.processing.CoverageAxisNotFoundExeption;
import petascope.wcps2.error.managed.processing.IncompatibleAxesNumberException;
import petascope.wcps2.metadata.model.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import petascope.core.CrsDefinition;
import petascope.exceptions.PetascopeException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps2.error.managed.processing.InvalidSubsettingException;
import petascope.wcps2.error.managed.processing.OutOfBoundsSubsettingException;
import petascope.wcps2.error.managed.processing.RangeFieldNotFound;
import petascope.wcps2.result.parameters.SubsetDimension;

/**
 * Class responsible with offering functionality for doing operations on
 * WcpsCoverageMetadataObjects.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WcpsCoverageMetadataService {

    private final CoordinateTranslationService coordinateTranslationService;

    public WcpsCoverageMetadataService(CoordinateTranslationService coordinateTranslationService) {
        this.coordinateTranslationService = coordinateTranslationService;
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
    public WcpsCoverageMetadata getResultingMetadata(WcpsCoverageMetadata firstMeta, WcpsCoverageMetadata secondMeta) {
        checkCompatibility(firstMeta, secondMeta);
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
     * Applies subsetting to a metadata object. e.g: eobstest(t(0:5),
     * Lat(-40.5:75), Long(25.5:75)) and with the trimming expression
     * (c[Lat(20:30), Long(40:50)]) then need to apply the subsets [(20:30),
     * (40:50)] in the coverage metadata expression
     *
     * @param checkBoundary should the subset needed to check the boundary (e.g: with scale(..., {subset})) will not need to check.
     * @param metadata
     * @param subsetList
     * @param updateAxisCrs in some cases like scale(c, {Lat:"CRS:1", Long:"CRS:1"}) it should not consider CRS:1 is CRS of axis Lat, Long of coverage.
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public WcpsCoverageMetadata applySubsets(Boolean checkBoundary, WcpsCoverageMetadata metadata, List<Subset> subsetList) throws PetascopeException {
        checkSubsetConsistency(metadata, subsetList);
        // iterate through the subsets
        // Normally, the query will need to calculate the grid bound from geo bound
        for (Subset subset : subsetList) {
            //identify the corresponding axis in the coverage metadata
            for (Axis axis : metadata.getAxes()) {
                // Only apply to correspondent axis with same name
                if (axis.getLabel().equals(subset.getAxisName())) {
                    // If subset has a given CRS, e.g: Lat:"http://../3857" then change the CRS in axis as well
                    if (subset.getCrs() != null && !subset.getCrs().equals(axis.getCrsUri())) {
                        axis.setCrsUri(subset.getCrs());
                    }

                    // NOTE: There are 2 types of subset:
                    // + update the geo-bound according to the subsets and translate updated geo-bound to new grid-bound
                    //   e.g: Lat(0:20) -> c[0:50] (calculate the grid coordinates from geo coordinates)
                    // + update the grid-bound according to the subsets and translate update grid-bound to new geo-bound
                    //   e.g: Lat:"CRS:1"(0:50) -> Lat(0:20) (calculate the geo coordinates from grid coordinates)
                    // Trimming
                    if (subset.getNumericSubset() instanceof NumericTrimming) {
                        applyTrimmingSubset(checkBoundary, metadata, subset, axis);
                    } else {
                        // slicing
                        applySlicing(checkBoundary, metadata, subset, axis);
                    }

                    // Continue with another subset
                    break;
                }
            }
        }

        return metadata;
    }

    /**
     * Strip slicing axes from coverage's metadata then they are not included
     * when re (slice/trim) the coverage. e.g: slice ( slice(c[t(0:5),
     * Lat(25:70), Long(30:60)], {t(0)}), {Lat(30)} ) the output is 1D in the
     * Long axis
     *
     * @param metadata
     * @param axisIteratorSubsetDimensions
     */
    public void stripSlicingAxes(WcpsCoverageMetadata metadata, List<SubsetDimension> axisIteratorSubsetDimensions) {
        List<Integer> removedIndexs = new ArrayList<Integer>();
        int i = 0;
        // If coverage has slicing axis (e.g: c[Lat(0), Long(20), t(0:5)]) then will strip Lat, Long from coverage c.
        for (Axis axis : metadata.getAxes()) {
            // If coverage has slicing axis from axisIterator (e.g: c[Lat($px), Long($py), t(0:5)])
            // As $px and $py cannot be used to applySubset to translate to number, then it need to be removed by using the List<SubsetDimension>.
            if (axis.getGeoBounds() instanceof NumericSlicing || this.containsAxisName(axis, axisIteratorSubsetDimensions)) {
                removedIndexs.add(i);
            }
            i++;
        }

        // Remove the slicing axes from the coverage
        int removedIndex = 0;
        for (int index : removedIndexs) {
            metadata.getAxes().remove(index - removedIndex);
            removedIndex++;
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
     * Remove all the un-unsed range fields from coverageExpression's metadata, if at least 1 range field is used.
     * e.g: coverage has 3 bands, but only 1 band is used (e.g: c.b1) then b2, b3 need to be removed from expression (c.b1)
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
     * Creates a coverage for the coverage constructor. Right now, this is not
     * geo-referenced.
     *
     * @param coverageName
     * @param numericSubsets
     * @return
     */
    public WcpsCoverageMetadata createCoverage(String coverageName, List<Subset> numericSubsets) {
        //create a new axis for each subset
        List<Axis> axes = new ArrayList();
        int axesCounter = 0;
        for (Subset numericSubset : numericSubsets) {
            String label = numericSubset.getAxisName();

            NumericSubset geoBounds = null;
            NumericSubset gridBounds = null;

            BigDecimal origin = null;

            if (numericSubset.getNumericSubset() instanceof NumericTrimming) {
                BigDecimal lowerLimit = ((NumericTrimming) numericSubset.getNumericSubset()).getLowerLimit();
                BigDecimal upperLimit = ((NumericTrimming) numericSubset.getNumericSubset()).getUpperLimit();

                // trimming
                geoBounds = new NumericTrimming(lowerLimit, upperLimit);
                //for now, the geoDomain is the same as the gridDomain, as we do no conversion in the coverage constructor / condenser
                gridBounds = new NumericTrimming(lowerLimit, upperLimit);
                origin = lowerLimit;
            } else {
                BigDecimal bound = ((NumericSlicing) numericSubset.getNumericSubset()).getBound();

                // slicing
                geoBounds = new NumericSlicing(bound);
                gridBounds = new NumericSlicing(bound);
                origin = bound;
            }

            // the crs of axis
            String crsUri = numericSubset.getCrs();

            //the axis direction should be deduced from the crs, when we'll support geo referencing in the coverage constructor
            //probably with a service. crsService.getAxisDirection(crs, axisLabel)
            AxisDirection axisDirection = CrsDefinition.getAxisDirection(label);

            // the created coverage now is only RectifiedGrid then it will use GridSpacing UoM
            String axisUoM = CrsUtil.INDEX_UOM;

            // Create a crsDefintion by crsUri
            CrsDefinition crsDefinition = null;
            if (crsUri != null && ! crsUri.equals("")) {
                CrsUtility.getCrsDefinitionByCrsUri(crsUri);
            }

            // the axis type (x, y, t,...) should be set to axis correctly, now just set to x
            String axisType = AxisTypes.X_AXIS;

            // Scalar resolution is set to 1
            BigDecimal scalarResolution = CrsUtil.INDEX_SCALAR_RESOLUTION;

            Axis axis = new RegularAxis(label, geoBounds, gridBounds, axisDirection, crsUri,
                                        crsDefinition, axisType, axisUoM, scalarResolution, axesCounter, origin, scalarResolution);
            axesCounter++;
            axes.add(axis);
        }
        //the current crs is IndexND CRS. When the coverage constructor will support geo referencing, the CrsService should
        //deduce the crs from the crses of the axes
        // NOTE: now, just use IndexND CRS (e.g: http://.../IndexND) to set as crs for creating coverage first
        String indexNDCrsUri = CrsUtility.createIndexNDCrsUri(axes);
        List<RangeField> rangeFields = new ArrayList<RangeField>();
        RangeField rangeField = new RangeField(RangeField.TYPE, RangeField.DEFAULT_NAME, null, new ArrayList<NilValue>(), RangeField.UOM, null, null);
        rangeFields.add(rangeField);
        
        WcpsCoverageMetadata result = new WcpsCoverageMetadata(coverageName, XMLSymbols.LABEL_GRID_COVERAGE, axes, indexNDCrsUri, rangeFields, null, null);
        return result;
    }

    /**
     * Apply trimming subset to regular/irregular axis and change geo bounds and
     * grid bounds of coverage metadata e.g: subset: Lat(0:20) with coverage
     * (Lat(0:70)) then need to update coverage metadata with geo bound(0:20)
     * and correspondent translated grid bound from the new geo bound.
     *
     * @param
     * @param checkBoundary
     * @param metadata
     * @param subset
     * @param axis
     * @throws PetascopeException
     */
    private void applyTrimmingSubset(Boolean checkBoundary, WcpsCoverageMetadata metadata, Subset subset, Axis axis) throws PetascopeException {

        boolean geoToGrid = true;
        if (CrsUtil.isGridCrs(axis.getCrsUri()) || metadata.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
            // it will need to calculate from grid bound to geo bound (e.g: Lat:"http://.../Index2D"(0:50) -> Lat(0:20))
            geoToGrid = false;
        }

        //set the lower, upper bounds and crs
        BigDecimal lowerLimit = ((NumericTrimming) subset.getNumericSubset()).getLowerLimit();
        BigDecimal upperLimit = ((NumericTrimming) subset.getNumericSubset()).getUpperLimit();

        ParsedSubset<BigDecimal> parsedSubset = new ParsedSubset<BigDecimal>(lowerLimit, upperLimit);

        // Check if trim (lo > high)
        if (lowerLimit.compareTo(upperLimit) > 0) {
            throw new InvalidSubsettingException(axis.getLabel(),
                                                 new ParsedSubset<String>(lowerLimit.toPlainString(), upperLimit.toPlainString()));
        }

        if (checkBoundary) {
            // NOTE: if crs is not CRS:1 then need to check valid geo boundary otherwise check valid grid boundary
            if (geoToGrid) {
                validParsedSubsetGeoBounds(parsedSubset, axis);
            } else {
                validParsedSubsetGridBounds(parsedSubset, axis);
            }
        }

        // Translate geo subset -> grid subset or grid subset -> geo subset
        NumericTrimming unAppliedNumericSubset = null;
        NumericTrimming unTranslatedNumericSubset = null;
        if (geoToGrid) {
            // Lat(0:20) -> c[0:50]
            // Apply subset from geo domain to grid domain
            unAppliedNumericSubset = (NumericTrimming)axis.getGeoBounds();
            unTranslatedNumericSubset = (NumericTrimming)axis.getGridBounds();
            this.translateTrimmingGeoToGridSubset(axis, subset, unAppliedNumericSubset, unTranslatedNumericSubset, metadata);
        } else {
            // Lat:"CRS:1"[0:50] -> Lat(0:20)
            // Apply subset from grid domain to geo domain
            unAppliedNumericSubset = (NumericTrimming)axis.getGridBounds();
            unTranslatedNumericSubset = (NumericTrimming)axis.getGeoBounds();
            this.translateTrimmingGridToGeoSubset(axis, subset, unAppliedNumericSubset, unTranslatedNumericSubset);
        }
    }

    /**
     * Apply the trimming subset on the unAppliedNumericSubset (geo bound) and
     * calculate this bound to unTranslatedNumericSubset (grid bound)
     * @param axis
     * @param subset
     * @param unAppliedNumericSubset
     * @param unTranslatedNumericSubset
     * @param metadata
     * @throws PetascopeException
     */
    private void translateTrimmingGeoToGridSubset(Axis axis, Subset subset,
            NumericTrimming unAppliedNumericSubset, NumericTrimming unTranslatedNumericSubset,
            WcpsCoverageMetadata metadata) throws PetascopeException {
        BigDecimal geoDomainMin = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit();
        BigDecimal geoDomainMax = ((NumericTrimming) axis.getGeoBounds()).getUpperLimit();
        BigDecimal gridDomainMin = ((NumericTrimming) axis.getGridBounds()).getLowerLimit();
        BigDecimal gridDomainMax = ((NumericTrimming) axis.getGridBounds()).getUpperLimit();

        BigDecimal lowerLimit = ((NumericTrimming) subset.getNumericSubset()).getLowerLimit();
        BigDecimal upperLimit = ((NumericTrimming) subset.getNumericSubset()).getUpperLimit();

        // Apply the subset on the unAppliedNumericSubset
        unAppliedNumericSubset.setLowerLimit(lowerLimit);
        unAppliedNumericSubset.setUpperLimit(upperLimit);

        ParsedSubset<BigDecimal> parsedSubset = new ParsedSubset<BigDecimal>(lowerLimit, upperLimit);
        // store the translated grid bounds from the subsets
        ParsedSubset<BigInteger> translatedSubset;
        translatedSubset = translateGeoToGridCoordinates(parsedSubset, axis,
                           metadata, geoDomainMin, geoDomainMax, gridDomainMin, gridDomainMax);

        // Set the correct translated grid parsed subset to axis
        unTranslatedNumericSubset.setLowerLimit(new BigDecimal(translatedSubset.getLowerLimit().toString()));
        unTranslatedNumericSubset.setUpperLimit(new BigDecimal(translatedSubset.getUpperLimit().toString()));
    }

    /**
     * Apply the trimming subset on the unAppliedNumericSubset (grid bound) and
     * calculate this bound to unTranslatedNumericSubset (geo bound)
     * @param axis
     * @param subset
     * @param unAppliedNumericSubset
     * @param unTranslatedNumericSubset
     * @throws PetascopeException
     */
    private void translateTrimmingGridToGeoSubset(Axis axis, Subset subset,
            NumericTrimming unAppliedNumericSubset,
            NumericTrimming unTranslatedNumericSubset) throws PetascopeException {
        BigDecimal geoDomainMin = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit();
        BigDecimal gridDomainMin = ((NumericTrimming) axis.getGridBounds()).getLowerLimit();
        BigDecimal gridDomainMax = ((NumericTrimming) axis.getGridBounds()).getUpperLimit();

        BigDecimal lowerLimit = ((NumericTrimming) subset.getNumericSubset()).getLowerLimit();
        BigDecimal upperLimit = ((NumericTrimming) subset.getNumericSubset()).getUpperLimit();

        // Apply the subset on the unAppliedNumericSubset
        unAppliedNumericSubset.setLowerLimit(lowerLimit);
        unAppliedNumericSubset.setUpperLimit(upperLimit);

        ParsedSubset<BigDecimal> parsedSubset = new ParsedSubset<BigDecimal>(lowerLimit, upperLimit);
        // store the translated grid bounds from the subsets
        ParsedSubset<BigInteger> translatedSubset;
        translatedSubset = translateGridToGeoCoordinates(parsedSubset, axis,
                           geoDomainMin, gridDomainMin, gridDomainMax);

        // Set the correct translated grid parsed subset to axis
        unTranslatedNumericSubset.setLowerLimit(new BigDecimal(translatedSubset.getLowerLimit().toString()));
        unTranslatedNumericSubset.setUpperLimit(new BigDecimal(translatedSubset.getUpperLimit().toString()));
    }

    /**
     * Apply slicing subset to regular/irregular axis and change geo bounds and
     * grid bounds of coverage metadata e.g: subset: Lat(20) with coverage
     * (Lat(0:70)) then need to update coverage metadata with geo bound(20) and
     * correspondent translated grid bound from the new geo bound.
     * @param calculateGridBound
     * @param checkBoundary should subset needed to be check within boundary (e.g: scale(..., {subset}) does not need to check)
     * @param metadata
     * @param subset
     * @param axis
     * @throws PetascopeException
     */
    private void applySlicing(Boolean checkBoundary, WcpsCoverageMetadata metadata,
                              Subset subset, Axis axis) throws PetascopeException {
        boolean geoToGrid = true;
        if (CrsUtil.isGridCrs(axis.getCrsUri()) || metadata.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
            // it will need to calculate from grid bound to geo bound (e.g: Lat:"http://.../Index2D"(0:50) -> Lat(0:20))
            geoToGrid = false;
        }

        BigDecimal bound = ((NumericSlicing) subset.getNumericSubset()).getBound();
        ParsedSubset<BigDecimal> geoParsedSubset = new ParsedSubset<BigDecimal>(bound, bound);
        // check if parsed subset is valid
        if (checkBoundary) {
            // NOTE: if crs is not CRS:1 then need to check valid geo boundary otherwise check valid grid boundary.
            if (!CrsUtil.isGridCrs(subset.getCrs())) {
                validParsedSubsetGeoBounds(geoParsedSubset, axis);
            } else {
                validParsedSubsetGridBounds(geoParsedSubset, axis);
            }
        }

        // Translate geo subset -> grid subset or grid subset -> geo subset
        if (geoToGrid) {
            this.translateSlicingGeoToGridSubset(axis, subset, metadata);
        } else {
            this.translateSlicingGridToGeoSubset(axis, subset);
        }
    }

    /**
     * Apply the slicing subset on the (geo bound) and
     * calculate this bound to (grid bound)
     * @param axis
     * @param subset
     * @param metadata
     * @throws PetascopeException
     */
    private void translateSlicingGeoToGridSubset(Axis axis, Subset subset,
            WcpsCoverageMetadata metadata) throws PetascopeException {

        BigDecimal bound = ((NumericSlicing) subset.getNumericSubset()).getBound();
        ParsedSubset<BigDecimal> parsedSubset = new ParsedSubset<BigDecimal>(bound, bound);

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
        ParsedSubset<BigInteger> translatedSubset = this.translateGeoToGridCoordinates(parsedSubset, axis,
                metadata, geoDomainMin, geoDomainMax, gridDomainMin, gridDomainMax);

        // Set the correct translated grid parsed subset to axis
        numericSlicingBound = new NumericSlicing(new BigDecimal(translatedSubset.getLowerLimit().toString()));
        axis.setGridBounds(numericSlicingBound);
    }

    /**
     * Apply the slicing subset on the (grid bound) and
     * calculate this bound to (geo bound)
     * @param axis
     * @param subset
     * @param metadata
     * @throws PetascopeException
     */
    private void translateSlicingGridToGeoSubset(Axis axis, Subset subset) throws PetascopeException {

        BigDecimal bound = ((NumericSlicing) subset.getNumericSubset()).getBound();
        ParsedSubset<BigDecimal> parsedSubset = new ParsedSubset<BigDecimal>(bound, bound);

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
        ParsedSubset<BigInteger> translatedSubset = this.translateGridToGeoCoordinates(parsedSubset, axis,
                geoDomainMin, gridDomainMin, gridDomainMax);
        // Set the correct translated grid parsed subset to axis
        numericSlicingBound = new NumericSlicing(new BigDecimal(translatedSubset.getLowerLimit().toString()));
        axis.setGeoBounds(numericSlicingBound);
    }


    /**
     * Translate a trimming/slicing from geo-referenced coordinates to grid coordinates
     * e.g: Lat(25.5:35.5) -> Lat:"CRS:1"(0:20)
     * @param calculateGridBound
     * @param parsedSubset
     * @param axis
     * @param metadata
     * @param geoDomainMin
     * @param geoDomainMax
     * @param gridDomainMin
     * @param gridDomainMax
     * @return
     * @throws PetascopeException
     */
    private ParsedSubset<BigInteger> translateGeoToGridCoordinates(ParsedSubset<BigDecimal> parsedSubset,
            Axis axis, WcpsCoverageMetadata metadata,  BigDecimal geoDomainMin,
            BigDecimal geoDomainMax, BigDecimal gridDomainMin,
            BigDecimal gridDomainMax) throws PetascopeException {
        ParsedSubset<BigInteger> translatedSubset;
        // Regular axis (no need to query database)
        if (axis instanceof RegularAxis) {
            BigDecimal resolution = ((RegularAxis) axis).getScalarResolution();
            // Lat(0:20) -> c[0:50]
            translatedSubset = coordinateTranslationService.geoToGridForRegularAxis(parsedSubset, geoDomainMin,
                               geoDomainMax, resolution, gridDomainMin);
        } else {
            // Irregular axis (query database for coefficients)
            int iOrder = axis.getRasdamanOrder();
            BigDecimal scalarResolution = axis.getScalarResolution();
            // e.g: ansi(148654) in irr_cube_2 -> c[0]
            translatedSubset = coordinateTranslationService.geoToGridForIrregularAxes(
                                   parsedSubset, scalarResolution, metadata.getCoverageName(),
                                   iOrder, gridDomainMin, gridDomainMax, geoDomainMin);
        }
        return translatedSubset;
    }

    /**
     * Translate from trimming/slicing grid bound to geo bounds
     * e.g: Lat:"CRS:1"(0:20) -> Lat(25.5:35.5)
     * @param parsedSubset
     * @param axis
     * @param metadata
     * @param geoDomainMin
     * @param geoDomainMax
     * @param gridDomainMin
     * @param gridDomainMax
     * @return
     * @throws PetascopeException
     */
    private ParsedSubset<BigInteger> translateGridToGeoCoordinates(ParsedSubset<BigDecimal> parsedSubset,
            Axis axis, BigDecimal geoDomainMin, BigDecimal gridDomainMin,
            BigDecimal gridDomainMax) throws PetascopeException {
        ParsedSubset<BigInteger> translatedSubset;
        // Regular axis (no need to query database)
        if (axis instanceof RegularAxis) {
            BigDecimal resolution = ((RegularAxis) axis).getScalarResolution();
            // Lat:"CRS:1"(0:50) -> [0:50]
            translatedSubset = coordinateTranslationService.gridToGeoForRegularAxis(parsedSubset, gridDomainMin, gridDomainMax, resolution, geoDomainMin);
        } else {
            // Irregular axis (query database for coefficients)
            // NOTE: if subsettingCrs is CRS:1, ( e.g: ansi:"CRS:1"(0) ) in irr_cube_2
            // it queries directly in grid coordinate which is regular not irregular anymore
            // Problem: it cannot resolve from grid coordinate (e.g: c[3]) to "geo" coordinate (e.g: 148661) with irregular axis.
            // then consider geo bound is equal to grid bound in this case.
            translatedSubset = new ParsedSubset<BigInteger>(parsedSubset.getLowerLimit().toBigInteger(), parsedSubset.getUpperLimit().toBigInteger());
        }
        return translatedSubset;
    }

    /**
     * Check if parsed subset is inside the geo domain of the current of the axis
     * e.g: axis's geo domain: Lat(0:50), and parsed subset: Lat(15:70) is out of upper bound
     * @param geoParsedSubset
     * @param axis
     */
    private void validParsedSubsetGeoBounds(ParsedSubset<BigDecimal> geoParsedSubset, Axis axis) {
        String axisName = axis.getLabel();

        // Check if subset is valid with trimming geo bound
        if (axis.getGeoBounds() instanceof NumericTrimming) {
            BigDecimal lowerLimit = ((NumericTrimming)axis.getGeoBounds()).getLowerLimit();
            BigDecimal upperLimit = ((NumericTrimming)axis.getGeoBounds()).getUpperLimit();
            ParsedSubset<String> subset;

            // Check if subset is inside the domain of geo bound
            if (geoParsedSubset.isSlicing()) {
                // slicing geo parsed subset
                if ((geoParsedSubset.getSlicingCoordinate().compareTo(lowerLimit) < 0)
                        || (geoParsedSubset.getSlicingCoordinate().compareTo(upperLimit) > 0)) {

                    // throw slicing error
                    subset = new ParsedSubset<String>(geoParsedSubset.getSlicingCoordinate().toPlainString());
                    throw new OutOfBoundsSubsettingException(axisName, subset, lowerLimit.toPlainString(), upperLimit.toPlainString());
                }
            } else {
                // trimming geo parsed subset
                if ((geoParsedSubset.getLowerLimit().compareTo(lowerLimit) < 0)
                        || (geoParsedSubset.getLowerLimit().compareTo(upperLimit) > 0)
                        || (geoParsedSubset.getUpperLimit().compareTo(lowerLimit) < 0)
                        || (geoParsedSubset.getUpperLimit().compareTo(upperLimit) > 0)) {

                    // throw trimming error
                    subset = new ParsedSubset<String>(geoParsedSubset.getLowerLimit().toPlainString(),
                                                      geoParsedSubset.getUpperLimit().toPlainString());
                    throw new OutOfBoundsSubsettingException(axisName, subset, lowerLimit.toPlainString(), upperLimit.toPlainString());
                }
            }
        } else {
            // Check if subset is valid with slicing geo bound
            BigDecimal bound = ((NumericSlicing)axis.getGeoBounds()).getBound();
            ParsedSubset<String> subset = new ParsedSubset<String>(geoParsedSubset.getLowerLimit().toPlainString());

            // Cannot pass a trimming subset in a slicing subset
            if (geoParsedSubset.isTrimming()) {
                throw new InvalidSubsettingException(axisName, subset);
            }

            // Check if subset is equal with slicing bound of geo bound
            if (!geoParsedSubset.getLowerLimit().equals(bound)) {
                throw new OutOfBoundsSubsettingException(axisName, subset,
                        bound.toPlainString(), bound.toPlainString());
            }
        }
    }

    /**
     * Check if parsed subset is inside the grid domain of the current of the axis
     * e.g: axis's grid domain: Lat(0:50), and parsed subset: Lat:"CRS:1"(65:80) is out of upper bound
     * @param gridParsedSubset
     * @param axis
     */
    private void validParsedSubsetGridBounds(ParsedSubset<BigDecimal> gridParsedSubset, Axis axis) {
        String axisName = axis.getLabel();

        // Check if subset is valid with trimming grid bound
        if (axis.getGridBounds() instanceof NumericTrimming) {
            BigDecimal lowerLimit = ((NumericTrimming)axis.getGridBounds()).getLowerLimit();
            BigDecimal upperLimit = ((NumericTrimming)axis.getGridBounds()).getUpperLimit();
            ParsedSubset<String> subset;

            // Check if subset is inside the domain of grid bound
            if (gridParsedSubset.isSlicing()) {
                // slicing grid parsed subset
                if ((gridParsedSubset.getSlicingCoordinate().compareTo(lowerLimit) < 0)
                        || (gridParsedSubset.getSlicingCoordinate().compareTo(upperLimit) > 0)) {

                    // throw slicing error
                    subset = new ParsedSubset<String>(gridParsedSubset.getSlicingCoordinate().toPlainString());
                    throw new OutOfBoundsSubsettingException(axisName, subset, lowerLimit.toPlainString(), upperLimit.toPlainString());
                }
            } else {
                // trimming grid parsed subset
                if ((gridParsedSubset.getLowerLimit().compareTo(lowerLimit) < 0)
                        || (gridParsedSubset.getLowerLimit().compareTo(upperLimit) > 0)
                        || (gridParsedSubset.getUpperLimit().compareTo(lowerLimit) < 0)
                        || (gridParsedSubset.getUpperLimit().compareTo(upperLimit) > 0)) {

                    // throw trimming error
                    subset = new ParsedSubset<String>(gridParsedSubset.getLowerLimit().toPlainString(),
                                                      gridParsedSubset.getUpperLimit().toPlainString());
                    throw new OutOfBoundsSubsettingException(axisName, subset, lowerLimit.toPlainString(), upperLimit.toPlainString());
                }
            }
        } else {
            // Check if subset is valid with slicing grid bound
            BigDecimal bound = ((NumericSlicing)axis.getGridBounds()).getBound();
            ParsedSubset<String> subset = new ParsedSubset<String>(gridParsedSubset.getLowerLimit().toPlainString());

            // Cannot pass a trimming subset in a slicing subset
            if (gridParsedSubset.isTrimming()) {
                throw new InvalidSubsettingException(axisName, subset);
            }

            // Check if subset is equal with slicing bound of grid bound
            if (!gridParsedSubset.getLowerLimit().equals(bound)) {
                throw new OutOfBoundsSubsettingException(axisName, subset,
                        bound.toPlainString(), bound.toPlainString());
            }
        }
    }

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
            if (axis.getLabel().equals(axisName)) {
                found = true;
                break;
            }
        }
        return found;
    }

    private void checkCompatibility(WcpsCoverageMetadata firstMeta, WcpsCoverageMetadata secondMeta) {
        //we want to detect only the cases where an error should be thrown
        if (firstMeta != null && secondMeta != null) {
            //check number of axes to be the same
            if (firstMeta.getAxes().size() != secondMeta.getAxes().size()) {
                throw new IncompatibleAxesNumberException(firstMeta.getCoverageName(), secondMeta.getCoverageName(),
                        firstMeta.getAxes().size(), secondMeta.getAxes().size());
            }
            //we don't check right now if the axes labels are different. If needed, add here.
        }
    }

    /**
     * Check if axis name is inside the subset dimensions list
     * @param axis
     * @param subsetDimensions
     */
    private boolean containsAxisName(Axis axis, List<SubsetDimension> subsetDimensions) {
        for (SubsetDimension subsetDimension : subsetDimensions) {
            if (subsetDimension.getAxisName().equals(axis.getLabel())) {
                return true;
            }
        }
        return false;
    }

}
