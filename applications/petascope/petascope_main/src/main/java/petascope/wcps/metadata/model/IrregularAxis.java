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
package petascope.wcps.metadata.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import petascope.core.CrsDefinition;
import petascope.exceptions.PetascopeException;
import static petascope.core.AxisTypes.T_AXIS;
import petascope.util.ListUtil;
import petascope.core.Pair;
import petascope.exceptions.ExceptionCode;
import petascope.util.BigDecimalUtil;
import petascope.util.BigDecimalUtil.BigDecimalComparator;
import static petascope.util.BigDecimalUtil.MIN_SCALE_TO_CHECK_EPSILON;
import petascope.util.JSONUtil;
import petascope.util.StringUtil;
import petascope.util.TimeUtil;
import petascope.wcps.exception.processing.IrregularAxisTrimmingCoefficientNotFoundException;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class IrregularAxis extends Axis {

    // list of coefficients for irregular axis
    private List<BigDecimal> directPositions;
    
    private List<BigDecimal> originalDirectPositions;
    
    public IrregularAxis() {
        
    }
    
    public IrregularAxis(String label, NumericSubset geoBounds, NumericSubset originalGridBounds, NumericSubset gridBounds,
            String crsUri, CrsDefinition crsDefinition,
            String axisType, String axisUoM,
            int rasdamanOrder, BigDecimal origin, BigDecimal resolution, List<BigDecimal> directPositions) {
        this(label, geoBounds, originalGridBounds, gridBounds, crsUri, crsDefinition, axisType, axisUoM, rasdamanOrder, origin, resolution, directPositions, null);
    }

    public IrregularAxis(String label, NumericSubset geoBounds, NumericSubset originalGridBounds, NumericSubset gridBounds,
            String crsUri, CrsDefinition crsDefinition,
            String axisType, String axisUoM,
            int rasdamanOrder, BigDecimal origin, BigDecimal resolution, List<BigDecimal> directPositions, NumericSubset originalGeoBounds) {
        super(label, geoBounds, originalGridBounds, gridBounds, crsUri, crsDefinition, axisType, axisUoM, rasdamanOrder, origin, resolution, originalGeoBounds);
        this.directPositions = directPositions;
        this.setOriginalDirectPositions();
    }

    public List<BigDecimal> getDirectPositions() {
        return directPositions;
    }

    public void setDirectPositions(List<BigDecimal> directPositions) {
        this.directPositions = directPositions;
    }
    
    public List<BigDecimal> getOriginalDirectPositions() {
        return this.originalDirectPositions;
    }
    
    public List<String> getOriginalDirectPositionsAsString() {
        List<String> results = new ArrayList<>();
        for (BigDecimal value : this.originalDirectPositions) {
            results.add(value.toPlainString());
        }
        
        return results;
    }
    
    public void setOriginalDirectPositions() {
        this.originalDirectPositions = new ArrayList<>();
        for (BigDecimal value : this.directPositions) {
            this.originalDirectPositions.add(new BigDecimal(value.toPlainString()));
        }
    }

    /**
     * Get the fixed first slice (0) imported coefficient's index from list of directPositions
     */
    @JsonIgnore
    public int getIndexOfCoefficientZero() {
        int i = Collections.binarySearch(this.directPositions, BigDecimal.ZERO);        
        return i;
    }
    
    /**
     * Get the index of a given coefficient from the list of original coefficients
     */
    @JsonIgnore
    public int getIndexOfCoefficient(BigDecimal inputCoefficient) {
        int i = Collections.binarySearch(this.directPositions, inputCoefficient);
        
        if (i < 0) {
            for (int j = 0; j < this.directPositions.size(); j++) {
                BigDecimal coefficient = this.directPositions.get(j);
                
                if (BigDecimalUtil.approximateEquals(coefficient, inputCoefficient)) {
                    i = j;
                    break;
                }
            }
        }
        return i;
    }
    
    /**
     * Get the fixed first slice (0) imported coefficient's index from list of originalDirectPositions
     */
    @JsonIgnore
    public int getIndexOfCoefficientZeroFromOriginalDirectPositions() {
        int i = Collections.binarySearch(this.originalDirectPositions, BigDecimal.ZERO);        
        return i;
    }
    
    /**
     * Return the index of input coefficient in list of directions
     */
    @JsonIgnore
    public int getIndexOfCoefficientFromOriginalDirectPositions(BigDecimal coefficient) throws PetascopeException {
        int i = Collections.binarySearch(this.originalDirectPositions, coefficient, new BigDecimalComparator());
        
        return i;
    }
    
    /**
     * Get element in list of coefficients which has the lowest coefficient value
     */
    @JsonIgnore
    public BigDecimal getLowestCoefficientValue() {
        if (this.originalDirectPositions == null) {
            this.setOriginalDirectPositions();
        }
        
        BigDecimal firstCoefficient = this.originalDirectPositions.get(0);
        BigDecimal lastCoefficient = this.originalDirectPositions.get(this.originalDirectPositions.size() - 1);
        
        BigDecimal result = firstCoefficient.compareTo(lastCoefficient) < 0 ? firstCoefficient : lastCoefficient;
        return result;            
    }
    
    /**
     * Return the bound number of first imported coverage slice (coefficient zero)
     * in direct positions list.
     * 
     * NOTE: This one is used as the anchor when needs to normalize other coefficients (greater than or lower than).
     */
    @JsonIgnore
    public BigDecimal getCoefficientZeroBoundNumber() throws PetascopeException {        
        int coefficientZeroIndex = this.getIndexOfCoefficientZero();
        BigDecimal lowestCoefficient = this.directPositions.get(0);
        // Distance value between lowest coefficient and coeffcient zero
        BigDecimal distanceValue = this.directPositions.get(coefficientZeroIndex).subtract(lowestCoefficient);
        BigDecimal coefficientZeroBoundNumber = this.getGeoBounds().getLowerLimit().add(distanceValue.multiply(this.getResolution()));
        
        return coefficientZeroBoundNumber;
    }
    
    @JsonIgnore
    public BigDecimal getCoefficientZeroBoundNumberFromOriginalDirectPositions() throws PetascopeException {        
        int coefficientZeroIndex = this.getIndexOfCoefficientZeroFromOriginalDirectPositions();
        BigDecimal lowestCoefficient = this.originalDirectPositions.get(0);
        // Distance value between lowest coefficient and coeffcient zero
        BigDecimal distanceValue = this.originalDirectPositions.get(coefficientZeroIndex).subtract(lowestCoefficient);
        BigDecimal coefficientZeroBoundNumber = this.getOriginalGeoBounds().getLowerLimit().add(distanceValue.multiply(this.getResolution()));
        
        return coefficientZeroBoundNumber;
    }
    
    /**
     * From the index of grid bound in list of coefficients, find out the correspond
     * grid bound in rasdaman grid axis. e.g: a list of coefficients: 
     * -30 -20 -10 0 10 20
     * grid axis domain is: [-1:4], zero coefficient index is 3 in list of coefficients,
     * then grid bound of index -3 (normalized by zero coefficient) will return grid value -1.
     */
    public Pair<Long, Long> calculateGridBoundsByZeroCoefficientIndex(Long indexOfGridLowerBound, Long indexOfGridUpperBound) throws PetascopeException {
        int coefficientZeroIndex = this.getIndexOfCoefficientZero();

        BigDecimal firstCoefficient = this.directPositions.get(0);
        BigDecimal lastCoefficient = this.directPositions.get(this.directPositions.size() - 1);
        
        if (firstCoefficient.compareTo(lastCoefficient) > 0) {
            firstCoefficient = lastCoefficient;
        }
        
        int gridZeroCoefficientDistance = this.getIndexOfCoefficientFromOriginalDirectPositions(firstCoefficient) - this.getIndexOfCoefficientZeroFromOriginalDirectPositions();
        
        if (coefficientZeroIndex != -1) {
            indexOfGridLowerBound = indexOfGridLowerBound - coefficientZeroIndex; 
        } else {
            // e.g. $c[Lat(55.2:85), Lon(0.1:9.9), unix("2015-09-01":"2015-12-01")][unix("2015-12-01")]
            indexOfGridLowerBound = indexOfGridLowerBound + gridZeroCoefficientDistance;
        }
        if (indexOfGridUpperBound != null) {
            if (coefficientZeroIndex != -1) {
                indexOfGridUpperBound = indexOfGridUpperBound - coefficientZeroIndex;
            } else {
                indexOfGridUpperBound = indexOfGridUpperBound + gridZeroCoefficientDistance;
            }
        }

        long gridLowerBound = Math.abs(indexOfGridLowerBound);
        long gridUpperBound = Math.abs(indexOfGridUpperBound);
        
        if (this.getGridBounds().getLowerLimit().compareTo(BigDecimal.ZERO) >= 0) {
            // In case of irregular axis with reversed geo bounds from input file (e.g: 100 85 70 50)
            // coefficients are -50 -15 -30 0 and grid bounds [0:3]
            if (gridUpperBound < gridLowerBound) {
                long tmp = gridLowerBound;
                gridLowerBound = gridUpperBound;
                gridUpperBound = tmp;
            }
        } else {
            Long normalizedCurrentGridLowerBound = -1L * coefficientZeroIndex;
            Long currentGridLowerBound = this.getGridBounds().getLowerLimit().longValue();

            Long distance = 0L;

            if (normalizedCurrentGridLowerBound.compareTo(indexOfGridLowerBound) == 0) {
                if (indexOfGridUpperBound == null) {
                    distance = 1L;
                }                  
            } else {
                distance = normalizedCurrentGridLowerBound - indexOfGridLowerBound;
            }

            gridLowerBound = currentGridLowerBound - distance;
            gridUpperBound = gridLowerBound;
            if (indexOfGridUpperBound != null) {
                distance = -(indexOfGridUpperBound - indexOfGridLowerBound);
                gridUpperBound = gridLowerBound - distance;
            }
        }
        
        return new Pair<>(gridLowerBound, gridUpperBound);
    }
    

    /**
     *
     * Return the grid indices of input geo min and geo max values for irregular
     * axis e.g: 0 10 20 50 70 and input: min is 30, max is 60 then the first
     * value which is selected is 50 (and grid index is: 3) and the second value
     * which is selected is 70 (and grid index is: 4)
     *
     * return [3, 4]
     *
     * @return
     */
    @JsonIgnore
    public Pair<Long, Long> getGridIndices(BigDecimal minInput, BigDecimal maxInput) throws PetascopeException {
        
        BigDecimal coefficientLowerBound = this.directPositions.get(0);
        BigDecimal coefficientUpperBound = this.directPositions.get(this.directPositions.size() - 1);
        
        boolean needToSwapBounds = false;
        
        if (coefficientLowerBound.compareTo(coefficientUpperBound) > 0) {
            BigDecimal tmp = coefficientLowerBound;
            coefficientLowerBound = coefficientUpperBound;
            coefficientUpperBound = tmp;
            
            needToSwapBounds = true;
        }
        
        
        if (minInput.compareTo(coefficientLowerBound) < 0) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, "Input coefficient lower bound '" + minInput 
                                                    + "' is lower than the direct positions' lower bound '" + coefficientLowerBound.toPlainString() 
                                                    + "' of irregular axis '" + this.getLabel() + "'.");
        } else if (maxInput.compareTo(coefficientUpperBound.add(BigDecimalUtil.COEFFICIENT_DECIMAL_EPSILON)) > 0) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, "Input upper bound '" + maxInput
                                                    + "' is greater than the direct positions' upper bound '" + coefficientUpperBound 
                                                    + "' of irregular axis '" + this.getLabel() + "'.");
        }

        Long minIndex = null;
        Long maxIndex = null;
        boolean foundMinIndex = false;  
        
        if (!needToSwapBounds) {
            // normal list of coefficients
            for (long i = 0; i < directPositions.size(); i++) {
                BigDecimal coefficient = directPositions.get((int) i);
                // find the min number which >= minInput
                if (!foundMinIndex && BigDecimalUtil.greaterThanOrEqual(coefficient, minInput)) {
                    minIndex = i;
                    foundMinIndex = true;
                }
                // find the max number which <= maxInput (as it is ascending list, so don't stop until coefficent > maxInput
                if (BigDecimalUtil.smallerThanOrEqual(coefficient, maxInput)) {
                    maxIndex = i;
                }
                // stop as it should find the minIndex and maxIndex already
                if (coefficient.compareTo(maxInput) >= 0) {
                    break;
                }
            }
        } else {
            // flipped list of coefficients
            for (long i = directPositions.size() - 1; i >= 0; i--) {
                BigDecimal coefficient = directPositions.get((int) i);
                // find the min number which >= minInput
                if (!foundMinIndex && BigDecimalUtil.greaterThanOrEqual(coefficient, minInput)) {
                    minIndex = i;
                    foundMinIndex = true;
                }
                // find the max number which <= maxInput (as it is ascending list, so don't stop until coefficent > maxInput
                if (BigDecimalUtil.smallerThanOrEqual(coefficient, maxInput)) {
                    maxIndex = i;
                }
                // stop as it should find the minIndex and maxIndex already
                if (coefficient.compareTo(maxInput) >= 0) {
                    break;
                }
            }
        }

        Pair<Long, Long> gridBoundsPair = new Pair<>(minIndex, maxIndex);
        return gridBoundsPair;
    }

    /**
     * Get all the coefficients from the list of directPositions which greater
     * than minInput and less than maxInput
     *
     */
    @JsonIgnore
    public List<BigDecimal> getAllCoefficientsInInterval(BigDecimal minInput, BigDecimal maxInput) throws PetascopeException {
        // Find the min and max grid incides in the List of directPositions
        Pair<Long, Long> gridIndices = this.getGridIndices(minInput, maxInput);
        if (gridIndices.fst.compareTo(gridIndices.snd) > 0) {
            throw new IrregularAxisTrimmingCoefficientNotFoundException(this.getLabel(), minInput.toPlainString(), maxInput.toPlainString());
        }
        
        List<BigDecimal> coefficients = new ArrayList<>();
        
        for (Long i = gridIndices.fst; i <= gridIndices.snd; i++) {
            BigDecimal coefficient = this.directPositions.get((int)(i.intValue()));
            coefficients.add(coefficient);
        }

        return coefficients;
    }

    /**
     * Return the list of raw coefficients in a String, raw means dateTime no
     * transform to dateTime format e.g: 0 3 5 8
     *
     * @return
     */
    @JsonIgnore
    public String getRawCoefficients() {
        List<BigDecimal> adjustedDirectPositions = this.adjustCoefficientsForPresentation(directPositions);
        String result = ListUtil.join(adjustedDirectPositions, " ");
        return result;
    }
    
    /**
     * In case of irregular axis is imported with reversed values (e.g: 10000 7000 50000 0)
     * then, it the coefficient values should be shown by these values as well.
     */
    private List<BigDecimal> adjustCoefficientsForPresentation(List<BigDecimal> coefficients) {
        List<BigDecimal> adjustedDirectPositions = new ArrayList<>();
        
        if (BigDecimalUtil.stripDecimalZeros(coefficients.get(0)).compareTo(BigDecimal.ZERO) < 0) {
            for (int i = 0; i < coefficients.size(); i++) {
                adjustedDirectPositions.add(this.getOriginalGeoBounds().getUpperLimit().add(coefficients.get(i)));
            }
        } else {
            for (int i = 0; i < coefficients.size(); i++) {
                adjustedDirectPositions.add(this.getOriginalGeoBounds().getLowerLimit().add(coefficients.get(i)));
            }
        }
        
        return adjustedDirectPositions;
    }

    /**
     * Return the concatenated string of a list of translated coefficients (DateTime axis) or raw
     * coefficients (non-datetime axis)
     *
     * @return
     * @throws PetascopeException
     */
    public String getRepresentationCoefficients() throws PetascopeException {
        String coefficients = "";

        // date time axis, need to translate from raw coefficients to datetime format based on CRS origin
        if (this.getAxisType().equals(T_AXIS)) {
            List<String> translatedCoefficients = TimeUtil.listValuesToISODateTime(this.getGeoBounds().getLowerLimit(),
                    directPositions, this.getCrsDefinition());
            coefficients = ListUtil.join(translatedCoefficients, " ");
        } else {
            // non date time axis
            coefficients = this.getRawCoefficients();
        }

        return coefficients;
    }
    
    public List<String> getCoefficientValues() throws PetascopeException {
        List<String> coefficients = new ArrayList<>();

        // date time axis, need to translate from raw coefficients to datetime format based on CRS origin
        if (this.getAxisType().equals(T_AXIS)) {
            List<String> timeValues = TimeUtil.listValuesToISODateTime(this.getGeoBounds().getLowerLimit(),
                    directPositions, this.getCrsDefinition());
            
            for (String value : timeValues) {
                coefficients.add(StringUtil.stripQuotes(value));
            }
        } else {
            // non date time axis
            List<BigDecimal> adjustedDirectPositions = this.adjustCoefficientsForPresentation(directPositions);
            for (BigDecimal value : adjustedDirectPositions) {
                coefficients.add(BigDecimalUtil.stripDecimalZeros(value).toPlainString());
            }
        }

        return coefficients;
    }
    
    /**
     * Return the list of translated coefficients (DateTime axis) or raw
     * coefficients (non-datetime axis)
     */
    public List<String> getRepresentationCoefficientsList() throws PetascopeException  {
        
        List<String> coefficients = new ArrayList<>();
        
        // date time axis, need to translate from raw coefficients to datetime format based on CRS origin
        if (this.getAxisType().equals(T_AXIS)) {
            List<String> translatedCoefficients = TimeUtil.listValuesToISODateTime(this.getGeoBounds().getLowerLimit(),
                    directPositions, this.getCrsDefinition());
            
            coefficients.addAll(translatedCoefficients);
        } else {
            List<BigDecimal> adjustedDirectPositions = this.adjustCoefficientsForPresentation(this.directPositions);
            
            // non date time axis
            for (BigDecimal coefficient : adjustedDirectPositions) {
                coefficients.add(BigDecimalUtil.stripDecimalZeros(coefficient).toPlainString());
            }            
        }
        
        return coefficients;

    }
    
    /**
     * Return the list of all original coefficients (DateTime axis) or raw
     * coefficients (non-datetime axis)
     */
    public List<String> getRepresentationOriginalCoefficientsList() throws PetascopeException  {
        
        List<String> coefficients = new ArrayList<>();
        
        // date time axis, need to translate from raw coefficients to datetime format based on CRS origin
        if (this.getAxisType().equals(T_AXIS)) {
            List<String> translatedCoefficients = TimeUtil.listValuesToISODateTime(this.getGeoBounds().getLowerLimit(),
                    this.originalDirectPositions, this.getCrsDefinition());
            
            coefficients.addAll(translatedCoefficients);
        } else {
            List<BigDecimal> adjustedDirectPositions = this.adjustCoefficientsForPresentation(this.originalDirectPositions);
            
            // non date time axis
            for (BigDecimal coefficient : adjustedDirectPositions) {
                coefficients.add(BigDecimalUtil.stripDecimalZeros(coefficient).toPlainString());
            }            
        }
        
        return coefficients;

    }    

    @Override
    public IrregularAxis clone() {
        return new IrregularAxis(getLabel(), getGeoBounds(), getOriginalGridBounds(), getGridBounds(),
                getNativeCrsUri(), getCrsDefinition(), getAxisType(), getAxisUoM(),
                getRasdamanOrder(), getOriginalOrigin(), getResolution(), getDirectPositions(), getOriginalGeoBounds());
    }

}
