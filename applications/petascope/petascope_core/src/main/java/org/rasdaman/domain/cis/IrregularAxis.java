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
package org.rasdaman.domain.cis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import javax.persistence.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.AxisTypes;
import petascope.util.BigDecimalUtil;
import petascope.core.Pair;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;

/**
 * CIS 1.1
 *
 * 8.2 Irregular independent grid axes
 *
 * The first extension over regular axes consists of irregular axes where
 * spacing along an axis can have any positive increment. Graphically, this can
 * be represented by straight lines (but that existence of values between direct
 * positions is possibly guided by interpolation restrictions). Such axes are
 * modelled by type CIS::IrregularAxis.
 *
 * Example This allows grid representations like swath data, but also mixes like
 * Lat/Long/t datacubes over orthorectified imagery where Lat and Long are
 * equidistant while acquisition time, hence t, is irregular.
 *
 * An irregular axis abandons the equidistant spacing of a regular axis.
 * Therefore, all direct positions along such an axis must be enumerated
 * explicitly which is achieved by replacing the lower bound / resolution /
 * upper bound scheme by an ordered list of direct positions.
 *
 * This is used as an independent irregular axis and combine to regular axis
 */
@Entity
@Table(name = IrregularAxis.TABLE_NAME)
@PrimaryKeyJoinColumn(name = IrregularAxis.COLUMN_ID, referencedColumnName = GeoAxis.COLUMN_ID)
public class IrregularAxis extends GeoAxis implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(IrregularAxis.class);

    public static final String TABLE_NAME = "irregular_axis";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    public static final int COEFFICIENT_ZERO = 0;
    
    public static final BigDecimal DEFAULT_RESOLUTION = BigDecimal.ONE;
    
    public enum CoefficientStatus {
        APPEND_TO_TOP,
        APPEND_TO_BOTTOM,
        UPDATE_EXISTING
    }



    // Ordered sequence of direct positions (coefficients) along this axis
    // NOTE: Postgresql can set precision and scale defined, it will use only use 2 numbers for scale 
    //       e.g: 19.3434534534534534533333333....33 -> 19.34
    //       However, SQLite does not support and it is stripped to only eights numbers after the "." 
    //       and will have wrong calculations, so we use String to store BigDecimal
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "irregular_axis_direct_positions")
    @OrderColumn
    private List<String> directPositions;

    public IrregularAxis() {

    }

    public IrregularAxis(List<String> directPositions, String axisLabel, String uomLabel, String srsName, String lowerBound, String upperBound, String resolution) {
        super(axisLabel, uomLabel, srsName, lowerBound, upperBound, resolution, AxisTypes.UNKNOWN);
        this.directPositions = directPositions;
    }
    
    public List<String> getDirectPositions() {
        return this.directPositions;
    }

    /**
     * Ultility method to get direct positions in number
     * @return 
     */
    @JsonIgnore
    public List<BigDecimal> getDirectPositionsAsNumbers() {
        List<BigDecimal> bigDecimalList = new ArrayList<>();
        for (String value : directPositions) {
            bigDecimalList.add(new BigDecimal(value));
        }

        return bigDecimalList;
    }

    public void setDirectPositions(List<BigDecimal> directPositions) {
        this.directPositions = new ArrayList<>();
        for (BigDecimal value : directPositions) {
            this.directPositions.add(value.toPlainString());
        }

    }
    
    /**
     * Compare 2 big decimal numbers with a small epsilon to make sure the difference between numbers are acceptable.
     * e.g: number1: 0.46666666666666666666666667, number2: 0.466666666666666666666666666666666666666666 are equal.
     * 
     * @param number1
     * @param number2
     * @return 
     */
    private boolean epsilonGreaterOrEqual(BigDecimal number1, BigDecimal number2) {
        // number1 really greater than number2
        if (number1.compareTo(number2.add(BigDecimalUtil.COEFFICIENT_DECIMAL_EPSILON)) >= 0) {
            return true;
        } else if (number1.subtract(number2).abs().compareTo(BigDecimalUtil.COEFFICIENT_DECIMAL_EPSILON) <= 0) {
            // number1 not really greater than number2 but approximately
            return true;
        }
        return false;
    }
    
    /**
     * From the index of grid bound in list of coefficients, find out the correspond
     * grid bound in rasdaman grid axis. e.g: a list of coefficients: 
     * -30 -20 -10 0 10 20
     * grid axis domain is: [-1:4], zero coefficient index is 3 in list of coefficients,
     * then grid bound of index -3 (normalized by zero coefficient) will return grid value -1.
     */
    public Pair<Long, Long> calculateGridBoundsByZeroCoefficientIndex(IndexAxis indexAxis, Long indexOfGridLowerBound, Long indexOfGridUpperBound) throws PetascopeException {
        
        if (indexOfGridLowerBound == null) {
            indexOfGridLowerBound = indexOfGridUpperBound;
        }
        
        long coefficientZeroIndex = this.getIndexOfCoefficientZero();
        Long normalizedCurrentGridLowerBound = -1L * coefficientZeroIndex;
                
        Long currentGridLowerBound = indexAxis.getLowerBound();

        Long distance = 0L;

        if (normalizedCurrentGridLowerBound.compareTo(indexOfGridLowerBound) == 0) {
            if (indexOfGridUpperBound == null) {
                distance = 1L;
            }                  
        } else {
            distance = normalizedCurrentGridLowerBound - indexOfGridLowerBound;
        }

        Long gridLowerBound = currentGridLowerBound - distance;
        Long gridUpperBound = gridLowerBound;
        if (indexOfGridUpperBound != null) {
            distance = -(indexOfGridUpperBound - indexOfGridLowerBound);
            gridUpperBound = gridLowerBound - distance;
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
     * @param minInput
     * @param maxInput
     * @return
     */
    @JsonIgnore
    public Pair<Long, Long> getGridIndices(BigDecimal minInput, BigDecimal maxInput) throws PetascopeException {

        Long minIndex = null;
        Long maxIndex = null;
        boolean foundMinIndex = false;

        Long i = Long.valueOf("0");
        List<BigDecimal> coefficients = this.getDirectPositionsAsNumbers();

        // coefficient in numbers for legacy coverages
        for (BigDecimal coefficient : coefficients) {
            
            // NOTE: from WCST_Import coefficient (especially DateTime from arrow) will return double and it can be larger than BigDecimal value calculated in Petascope.
            // Therefore, we need to check this comparison with a small epsilon to make sure it is actually same number more or less.
            // e.g: 
            // coefficient: 0.04166666666666666666666666667
            // input:       0.04166666666666666666666666666666666666666666666667
            // find the min number which >= minInput
            if (!foundMinIndex) {
                if (this.epsilonGreaterOrEqual(coefficient, minInput)) {
                    minIndex = i;
                    foundMinIndex = true;
                }
            }
            // find the max number which <= maxInput (as it is ascending list, so don't stop until coefficent > maxInput
            if (this.epsilonGreaterOrEqual(maxInput, coefficient)) {
                maxIndex = i;
            }
            // stop as it should find the minIndex and maxIndex already
            if (this.epsilonGreaterOrEqual(coefficient, maxInput)) {
                break;
            }

            i++;
        }
        
        // Find the position of coefficient zero "0" in the list of direct positions.
        i = this.getIndexOfCoefficientZero();
        
        // Then, the indices of input subset will need to rely on the index of fixed first coefficient (0).
        if (minIndex != null) {
            minIndex = minIndex - i;
        }
        if (maxIndex != null) {
            maxIndex = maxIndex - i;
        }
        
        if (minIndex != null && maxIndex != null) {
            if (minIndex > maxIndex) {
                // In this case coefficient doesn't exist in the list of coefficients
                minIndex = null;
                maxIndex = null;
            }
        }

        return new Pair<>(minIndex, maxIndex);
    }
    
    /**
     * Get the fixed first slice (0) imported coefficient's index from list of directPositions
     */
    @JsonIgnore
    public long getIndexOfCoefficientZero() throws PetascopeException {
        int ret = Collections.binarySearch(this.getDirectPositionsAsNumbers(), BigDecimal.ZERO);
        if (ret >= 0) {
            return ret;
        } else {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                for (BigDecimal pos: this.getDirectPositionsAsNumbers()) {
                    sb.append(pos.toString());
                    sb.append(", ");
                }
                log.debug("Current direct positions: " + sb.toString());
            }
            throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration, 
                    "Coefficient 0 (zero) not found for irregular axis '" + getAxisLabel() + "'.");
        }
    }
    
    /**
     * Return the bound number of first imported coverage slice (coefficient zero)
     * in direct positions list.
     * 
     * NOTE: This one is used as the anchor when needs to normalize other coefficients (greater than or lower than).
     */
    @JsonIgnore
    public BigDecimal getCoefficientZeroBoundNumber() throws PetascopeException {        
        Long coefficientZeroIndex = this.getIndexOfCoefficientZero();
        BigDecimal lowestCoefficient = this.getDirectPositionsAsNumbers().get(0);
        // Distance value between lowest coefficient and coeffcient zero
        BigDecimal distanceValue = this.getDirectPositionsAsNumbers().get(coefficientZeroIndex.intValue()).subtract(lowestCoefficient);
        BigDecimal coefficientZeroBoundNumber = this.getLowerBoundNumber().add(distanceValue.multiply(this.getResolution()));
        
        return coefficientZeroBoundNumber;
    }
    
    /**
     *
     * Check if a coefficient is valid (i.e: it should be equals to an existing
     * coefficient in case of updating coverage with a existing slice or,
     * greater than the highest coefficient of directPositions in case of
     * updating coverage with a new slice).
     *
     * NOTE: it does not support to insert a slice between 2 existing slices
     * e.g: existing coefficients: 0 3 5 8, cannot add 2, 4, 6, 7 but 8.01 is ok
     *
     *
     * @param isInsitu
     * @param coefficient
     * @return
     * @throws petascope.exceptions.WCSException
     */
    public CoefficientStatus validateCoefficient(boolean isInsitu, BigDecimal coefficient) throws WCSException {
        long index = BigDecimalUtil.listContainsCoefficient(this.getDirectPositionsAsNumbers(), coefficient);
        if (index == -1) {
            // Check if coefficient > the upperBound of axis, if it is not then it is added between other coeffcients which is not valid
            int numberOfCoefficients = this.getDirectPositionsAsNumbers().size();
            BigDecimal upperBoundCoefficient = this.getDirectPositionsAsNumbers().get(numberOfCoefficients - 1);
            BigDecimal lowerBoundCoefficient = this.getDirectPositionsAsNumbers().get(0);
            
            if (coefficient.compareTo(upperBoundCoefficient) > 0) {
                // insert on top
                return CoefficientStatus.APPEND_TO_TOP;
            } else if (coefficient.compareTo(lowerBoundCoefficient) < 0) {
                // insert on bottom
                return CoefficientStatus.APPEND_TO_BOTTOM;
            } else {
		// insert to middle of direct positions

                throw new WCSException(ExceptionCode.NoApplicableCode, "Adding slice in between existing slices on irregular axis is not supported.");
            }
        } else {
            // the coefficient does exist in list of direct positions
            return CoefficientStatus.UPDATE_EXISTING;
        }
    }

    /**
     * Get all the coefficients from the list of directPositions which greater
     * than minInput and less than maxInput
     *
     * @param minInput
     * @param maxInput
     * @return
     */
    @JsonIgnore
    public List<BigDecimal> getAllCoefficientsInInterval(BigDecimal minInput, BigDecimal maxInput) throws PetascopeException {
        // Find the min and max grid incides in the List of directPositions
        Pair<Long, Long> gridIndices = this.getGridIndices(minInput, maxInput);
        List<BigDecimal> coefficients = new ArrayList<>();
        for (Long i = gridIndices.fst; i <= gridIndices.snd; i++) {
            BigDecimal coefficient = this.getDirectPositionsAsNumbers().get(i.intValue());
            coefficients.add(coefficient);
        }

        return coefficients;
    }

    // This is used for translating from geo domain to grid domain for irregular axis
    public static final BigDecimal RESOLUTION = BigDecimal.ONE;
}
