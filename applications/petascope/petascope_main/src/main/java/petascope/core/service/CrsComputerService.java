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
package petascope.core.service;

import org.rasdaman.domain.cis.*;
import petascope.wcps.exception.processing.UnorderedSubsetException;
import petascope.wcps.exception.processing.IrregularAxisFetchingFailedException;
import petascope.wcps.exception.processing.InvalidCalculatedBoundsSubsettingException;
import petascope.wcps.exception.processing.InvalidDateTimeSubsetException;
import petascope.exceptions.PetascopeException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.TimeUtil;
import petascope.core.XMLSymbols;
import petascope.wcps.metadata.model.ParsedSubset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.rasdaman.domain.cis.Coverage;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.Pair;
import petascope.wcs2.parsers.subsets.AbstractSubsetDimension;
import petascope.wcs2.parsers.subsets.SlicingSubsetDimension;
import petascope.wcs2.parsers.subsets.TrimmingSubsetDimension;

/**
 * Translates a CRS subset interval to array indices
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CrsComputerService {

    /**
     * Constructor for the class in case trimming
     *
     * @param axisName the name of the axis on which the subset is being made
     * @param crsName the name of the crs that should be applied @unused
     * @param parsedSubset the subset that has to be translated
     * @param coverage the coverage on which the subsetting is applied
     */
    public CrsComputerService(String axisName, String crsName, ParsedSubset<String> parsedSubset, Coverage coverage) {
        this.axisName = axisName;
        this.crsName = crsName;
        this.subset = parsedSubset;
        this.coverage = coverage;
        this.subset.setSubsetTrim(true);
        // Only support generalGridCoverage now
        this.geoAxis = ((GeneralGridCoverage) coverage).getGeoAxisByName(axisName);
        this.indexAxis = ((GeneralGridCoverage) coverage).getIndexAxisByName(axisName);
    }
    
    /**
     * From the input subset (E.g: subset=Lat(0,20) or subset=ansi("2012-02-03")) of input slice, parse the domain(lowerBound,uppperBound)
     * from String to big decimal numbers.
     */
    public static ParsedSubset<BigDecimal> parseSubsetDimensionToNumbers(String axisCRS, String axisUoM, AbstractSubsetDimension subset) throws PetascopeException, SecoreException {
        String lowerBound, upperBound;
        
        if (subset instanceof TrimmingSubsetDimension) {
            TrimmingSubsetDimension trimSubset = (TrimmingSubsetDimension) subset;
            lowerBound = trimSubset.getLowerBound();
            upperBound = trimSubset.getUpperBound();
        } else {
            lowerBound = ((SlicingSubsetDimension) subset).getBound();
            upperBound = lowerBound;
        }
        
        if (subset.isNumeric()) {
            // e.g: Lat=(20, 30)
            return new ParsedSubset<>(new BigDecimal(lowerBound), new BigDecimal(upperBound));
        } else {
            // e.g: t="2012-02-03"
            String datumOrigin = CrsUtil.getDatumOrigin(axisCRS);

            BigDecimal lowerBoundNumber = TimeUtil.countOffsets(datumOrigin, lowerBound, axisUoM, BigDecimal.ONE);
            BigDecimal upperBoundNumber = TimeUtil.countOffsets(datumOrigin, upperBound, axisUoM, BigDecimal.ONE);
            
            return new ParsedSubset<>(lowerBoundNumber, upperBoundNumber);
        }
    }

    /**
     * Returns the translated interval from the original subset
     *
     * @return
     */
    public ParsedSubset<Long> getPixelIndices() throws WCSException, PetascopeException, SecoreException {
        BigDecimal lowerNumericLimit, upperNumericLimit;

        try {
            lowerNumericLimit = new BigDecimal(subset.getLowerLimit().toString());
            upperNumericLimit = new BigDecimal(subset.getUpperLimit().toString());

            // GridCoverage does not need to calculate subset
            if (coverage.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
                return returnGridPixelIndices(new ParsedSubset<>(lowerNumericLimit, upperNumericLimit));
            } else {
                // RectifiedGridCoverage, ReferenceableGridCoverage will need to calculate subset
                return getNumericPixelIndices(new ParsedSubset<>(lowerNumericLimit, upperNumericLimit));
            }
        } catch (NumberFormatException e) {
            // NOTE: in case time axis is String, e.g "1950-01-01" then it will use this method

            return getTimePixelIndices();
        }
    }

    /**
     * Converts grid coordinates to pixel indices (nothing needs to be done
     * except casting into the correct datatype)
     *
     * Used for GridCoverage
     *
     * @param numericSubset the subset interval to be translated
     * @return the translated interval
     */
    private ParsedSubset<Long> returnGridPixelIndices(ParsedSubset<BigDecimal> numericSubset) {
        return new ParsedSubset<>(numericSubset.getLowerLimit().longValue(),
                numericSubset.getUpperLimit().longValue());
    }

    /**
     * Returns the translated pixel indices for numeric subsets
     *
     * @param numericSubset the numeric subset to be translated
     * @return
     */
    private ParsedSubset<Long> getNumericPixelIndices(ParsedSubset<BigDecimal> numericSubset) throws PetascopeException, SecoreException {
        this.checkNumericSubsetValidity(numericSubset);

        final ParsedSubset<Long> result;
        if (this.geoAxis.isIrregular()) {
            result = getNumericPixelIndicesForIrregularAxes(numericSubset);
        } else {
            result = getNumericPixelIndicesForRegularAxis(numericSubset);
        }
        return result;
    }

    /**
     * Checks if the subset is valid and can be translated
     *
     * @param numericSubset the subset to be translated
     */
    private void checkNumericSubsetValidity(ParsedSubset<BigDecimal> numericSubset) throws PetascopeException, SecoreException {

        // Check order
        if (numericSubset.getUpperLimit().compareTo(numericSubset.getLowerLimit()) < 0) {
            throw new UnorderedSubsetException(axisName, subset);
        }
    }

    /**
     * Returns the translated subset if the coverage has an irregular axis
     *
     * @param numericSubset the subset to be translated
     * @return
     */
    private ParsedSubset<Long> getNumericPixelIndicesForIrregularAxes(ParsedSubset<BigDecimal> numericSubset) {

        try {
            // these values are the translated from datetime string with datumOrigin to number            
            BigDecimal minInput = numericSubset.getLowerLimit();
            BigDecimal maxInput = numericSubset.getUpperLimit();

            // then, they must be translated to coefficients as the directPositions is list of coefficient.
            // Normalize the input with the lowerBound of current geo axis first.
            BigDecimal lowerBoundNumber = this.geoAxis.getLowerBoundNumber();
            BigDecimal normalizedMinInput = minInput.subtract(lowerBoundNumber);
            BigDecimal normalizedMaxInput = maxInput.subtract(lowerBoundNumber);

            // Get the grid indices which were mapped to the coefficients in irregular axis
            Pair<Long, Long> gridIndices = ((IrregularAxis) geoAxis).getGridIndices(normalizedMinInput, normalizedMaxInput);

            return new ParsedSubset<>(gridIndices.fst, gridIndices.snd);
        } catch (Exception e) {
            throw new IrregularAxisFetchingFailedException(coverage.getCoverageId(), axisName, e);
        }
    }

    /**
     * Returns the translated subset for regular axis
     *
     * @param numericSubset the numeric subset to be translated
     * @return
     */
    private ParsedSubset<Long> getNumericPixelIndicesForRegularAxis(ParsedSubset<BigDecimal> numericSubset) throws PetascopeException, SecoreException {

        BigDecimal resolution = this.geoAxis.getResolution();

        // check the direction of axis by resoution (e.g: lat with direction < 0, long with direction > 0)
        boolean positiveAxis = resolution.doubleValue() > 0;

        BigDecimal domMin = this.geoAxis.getLowerBoundNumber();
        BigDecimal domMax = this.geoAxis.getUpperBoundNumber();

        long pxMin = indexAxis.getLowerBound();

        // Indexed CRSs do not require conversion
        if (crsName.contains(CrsUtil.GRID_CRS)) {
            return new ParsedSubset<>((long) numericSubset.getLowerLimit().doubleValue(), (long) numericSubset.getUpperLimit().doubleValue());
        }

        // Open interval on the right: take away epsilon from upper bound:
        long returnLowerLimit, returnUpperLimit;
        if (positiveAxis) {
            // Normal linear numerical axis
            BigDecimal lowerLimit = BigDecimalUtil.divide(numericSubset.getLowerLimit().subtract(domMin), resolution);
            lowerLimit = this.shiftToNearestGridPointWCST(lowerLimit);
            returnLowerLimit = lowerLimit.setScale(0, RoundingMode.FLOOR).add(new BigDecimal(pxMin)).longValue();
            
            BigDecimal upperLimit = BigDecimalUtil.divide(numericSubset.getUpperLimit().subtract(domMin), resolution);
            upperLimit = this.shiftToNearestGridPointWCST(upperLimit);
            returnUpperLimit = upperLimit.setScale(0, RoundingMode.CEILING).subtract(BigDecimal.ONE).add(new BigDecimal(pxMin)).longValue();
        } else {
            // Linear negative axis (eg northing of georeferenced images)
            // First coordHi, so that left-hand index is the lower one
            BigDecimal lowerLimit = BigDecimalUtil.divide(numericSubset.getUpperLimit().subtract(domMax), resolution);
            lowerLimit = this.shiftToNearestGridPointWCST(lowerLimit);
            returnLowerLimit = lowerLimit.setScale(0, RoundingMode.FLOOR).add(new BigDecimal(pxMin)).longValue();
            
            BigDecimal upperLimit = BigDecimalUtil.divide(numericSubset.getLowerLimit().subtract(domMax), resolution);
            upperLimit = this.shiftToNearestGridPointWCST(upperLimit);
            returnUpperLimit = upperLimit.setScale(0, RoundingMode.CEILING).subtract(BigDecimal.ONE).add(new BigDecimal(pxMin)).longValue();
        }
        
        //because we use ceil - 1, when values are close (less than 1 resolution dif), the upper will be pushed below the lower            
        if (returnUpperLimit + 1 == returnLowerLimit) {
            if (returnUpperLimit < pxMin) {
                returnUpperLimit = pxMin;
            }
            returnLowerLimit = returnUpperLimit;
            
        }
        
        return new ParsedSubset<>(returnLowerLimit, returnUpperLimit);
    }
    
    /**
     *
     * We shift the BigDecimal pixel to nearest grid pixel in integer e.g:
     * 4.9998 -> 5 and 5.00001 -> 5
     *
     * @param gridPoint
     * @return 
     */
    public static BigDecimal shiftToNearestGridPointWCST(BigDecimal gridPoint) {
        // e.g: 4.999 + 0.001 > 5 then return 5
        if ((gridPoint.add(GRID_POINT_EPSILON_WCST)).compareTo(gridPoint.setScale(0, RoundingMode.CEILING)) >= 0) {
            return gridPoint.setScale(0, RoundingMode.CEILING);
        } else if ((gridPoint.subtract(GRID_POINT_EPSILON_WCST)).compareTo(gridPoint.setScale(0, RoundingMode.FLOOR)) <= 0) {
            // e.g: 5.0001 -0.0001 = 5
            return gridPoint.setScale(0, RoundingMode.FLOOR);
        } else {
            return gridPoint;
        }

    }

    /**
     *
     * We shift the BigDecimal pixel to nearest grid pixel in integer e.g:
     * 4.9998 -> 5 and 5.00001 -> 5
     *
     * @param gridPoint
     * @return 
     */
    public static BigDecimal shiftToNearestGridPointWCPS(BigDecimal gridPoint) {
        // e.g: 4.999 + 0.001 > 5 then return 5
        if ((gridPoint.add(GRID_POINT_EPSILON_WCPS)).compareTo(gridPoint.setScale(0, RoundingMode.CEILING)) >= 0) {
            return gridPoint.setScale(0, RoundingMode.CEILING);
        } else if ((gridPoint.subtract(GRID_POINT_EPSILON_WCPS)).compareTo(gridPoint.setScale(0, RoundingMode.FLOOR)) <= 0) {
            // e.g: 5.0001 -0.0001 = 5
            return gridPoint.setScale(0, RoundingMode.FLOOR);
        } else {
            return gridPoint;
        }

    }

    /**
     * Checks if the subset is valid and can be translated
     */
    private void checkTimeSubsetValidity() throws WCSException {
        if (!TimeUtil.isValidTimestamp(subset.getLowerLimit())) {
            throw new InvalidDateTimeSubsetException(axisName, subset);
        }
        if (!TimeUtil.isValidTimestamp(subset.getUpperLimit())) {
            throw new InvalidDateTimeSubsetException(axisName, subset);
        }

        if (!TimeUtil.isOrderedTimeSubset(subset.getLowerLimit(), subset.getUpperLimit())) {
            throw new UnorderedSubsetException(axisName, subset);
        }
    }

    /**
     * Returns the translated interval for time subsets
     *
     * @return
     */
    private ParsedSubset<Long> getTimePixelIndices() throws WCSException, PetascopeException, SecoreException {
        checkTimeSubsetValidity();
        final ParsedSubset<Long> result;
        if (this.geoAxis.isIrregular()) {
            result = getTimePixelIndicesForIrregularAxis();
        } else {
            result = getTimePixelIndicesForRegularAxis();
        }
        return result;
    }

    /**
     * Returns the translated interval for coverages with a date time irregular
     * axis
     *
     * @return
     */
    private ParsedSubset<Long> getTimePixelIndicesForIrregularAxis() throws PetascopeException, SecoreException {
        String axisUoM = this.geoAxis.getUomLabel();
        String datumOrigin = CrsUtil.getDatumOrigin(this.geoAxis.getSrsName());
        BigDecimal numLo;
        BigDecimal numHi;
        try {
            numLo = TimeUtil.countOffsets(datumOrigin, subset.getLowerLimit(), axisUoM, IrregularAxis.RESOLUTION);
            numHi = TimeUtil.countOffsets(datumOrigin, subset.getUpperLimit(), axisUoM, IrregularAxis.RESOLUTION);
        } catch (PetascopeException e) {
            throw new InvalidCalculatedBoundsSubsettingException(axisName, subset);
        }
        return getNumericPixelIndicesForIrregularAxes(new ParsedSubset<>(numLo, numHi));
    }

    /**
     * Returns the translated interval for coverages with a date time regular
     * axis
     *
     * @return
     */
    private ParsedSubset<Long> getTimePixelIndicesForRegularAxis() throws PetascopeException, SecoreException {
        String axisUoM = this.geoAxis.getUomLabel();
        String datumOrigin = CrsUtil.getDatumOrigin(this.geoAxis.getSrsName());

        BigDecimal numLo;
        BigDecimal numHi;        

        try {
            // Need to convert timestamps to TemporalCRS numeric coordinates
            numLo = TimeUtil.countOffsets(datumOrigin, subset.getLowerLimit(), axisUoM, BigDecimal.ONE); // do not normalize by vector here:
            numHi = TimeUtil.countOffsets(datumOrigin, subset.getUpperLimit(), axisUoM, BigDecimal.ONE); // absolute time coordinates needed.
        } catch (PetascopeException e) {
            throw new InvalidCalculatedBoundsSubsettingException(axisName, subset);
        }
        
        return getNumericPixelIndicesForRegularAxis(new ParsedSubset<>(numLo, numHi));
    }

    // To support calculation from wcst_import before using float to petascope (BigDecimal)
    // we have to using an acceptable espilon to determine which grid point should be the result of calculation
    // as math.ceil(), math.floor() will easily +/- by 1 grid pixel in unwanted cases, such as: 4.00001 -> 5, 3.999 -> 3
    // so the epsilon is added to support these cases to shift to nearest integer value: 4.000001 -> 4, 3.999 -> 4
    // NOTE: for WCST, no need to use subset_correction:true when it could shift the grid bounds to nearest pixels properly with this epsilon 
    // (the axis's resolution is the most important to support this adjustment and the resolution should be almost correct, e.g: 4.1566667777 instead of 4.15666677777777 and not 4.1566).
    public static final BigDecimal GRID_POINT_EPSILON_WCST = new BigDecimal("0.01");
    // NOTE: for WCPS, the most important value is a correct axis's resolution, then the formular to translate geo bounds to grid bounds will be correct.
    // The epsilon to adjust grid bounds for WCPS is really small as this is not used in almost cases, 
    // only in rare cases when the result should be shifted (e.g: 1.0000000000000000000011111 -> 1)
    public static final BigDecimal GRID_POINT_EPSILON_WCPS = new BigDecimal("0.000000001");
    
    private final String axisName;
    private final String crsName;
    private ParsedSubset<String> subset;
    private final Coverage coverage;

    // current axis of coverage which is calculated with the input parsedSubset
    private final GeoAxis geoAxis;
    private final IndexAxis indexAxis;
    
}
