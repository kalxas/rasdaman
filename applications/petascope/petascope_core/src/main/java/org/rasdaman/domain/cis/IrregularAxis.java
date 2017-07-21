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

import java.math.BigDecimal;
import java.util.ArrayList;
import javax.persistence.*;
import java.util.List;
import petascope.util.BigDecimalUtil;
import petascope.core.Pair;
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
public class IrregularAxis extends GeoAxis {

    public static final String TABLE_NAME = "irregular_axis";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "irregular_axis_direct_positions")
    @OrderColumn
    // Ordered sequence of direct positions (coefficients) along this axis
    // NOTE: Postgresql can set precision and scale defined, it will use only use 2 numbers for scale (e.g: 19.3434534534534534533333333....33 -> 19.34)
    // However, SQLite does not support and it is stripped to only eights numbers after the "." and will have wrong calculations, so use String to store BigDecimal    
    private List<String> directPositions;

    public IrregularAxis() {

    }
    
    public List<String> getDirectPositions() {
        return this.directPositions;
    }

    /**
     * Ultility method to get direct positions in number
     * @return 
     */
    public List<BigDecimal> getDirectPositionsNumber() {
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
    public Pair<Long, Long> getGridIndices(BigDecimal minInput, BigDecimal maxInput) {

        Long minIndex = null;
        Long maxIndex = null;
        boolean foundMinIndex = false;

        Long i = Long.valueOf("0");

        // coefficient in numbers for legacy coverages
        for (BigDecimal coefficient : this.getDirectPositionsNumber()) {
            // find the min number which >= minInput
            if (!foundMinIndex && coefficient.compareTo(minInput) >= 0) {
                minIndex = i;
                foundMinIndex = true;
            }
            // find the max number which <= maxInput (as it is ascending list, so don't stop until coefficent > maxInput
            if (coefficient.compareTo(maxInput) <= 0) {
                maxIndex = i;
            }
            // stop as it should find the minIndex and maxIndex already
            if (coefficient.compareTo(maxInput) >= 0) {
                break;
            }

            i++;
        }

        return new Pair(minIndex, maxIndex);
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
     * @param coefficient
     * @return
     * @throws petascope.exceptions.WCSException
     */
    public boolean validateCoefficient(BigDecimal coefficient) throws WCSException {
        long index = BigDecimalUtil.listContainsCoefficient(this.getDirectPositionsNumber(), coefficient);
        if (index == -1) {
            // Check if coefficient > the upperBound of axis, if it is not then it is added between other coeffcients which is not valid
            int numberOfCoefficients = this.getDirectPositionsNumber().size();
            BigDecimal upperBoundCoefficient = this.getDirectPositionsNumber().get(numberOfCoefficients - 1);
            if (upperBoundCoefficient.compareTo(coefficient) > 0) {
                throw new WCSException("Can not add new slice in between existing slices on irregular axis. Only " +
            "adding slices on top is currently supported.");
            } else {
                // the coefficient does not exist in the list of direct positions and greater than upperBound
                return false;
            }
        } else {
            // the coefficient does exist in list of direct positions
            return true;
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
    public List<BigDecimal> getAllCoefficientsInInterval(BigDecimal minInput, BigDecimal maxInput) {
        // Find the min and max grid incides in the List of directPositions
        Pair<Long, Long> gridIndices = this.getGridIndices(minInput, maxInput);
        List<BigDecimal> coefficients = new ArrayList<>();
        for (Long i = gridIndices.fst; i <= gridIndices.snd; i++) {
            BigDecimal coefficient = this.getDirectPositionsNumber().get(i.intValue());
            coefficients.add(coefficient);
        }

        return coefficients;
    }

    // This is used for translating from geo domain to grid domain for irregular axis
    public static final BigDecimal RESOLUTION = BigDecimal.ONE;
}
