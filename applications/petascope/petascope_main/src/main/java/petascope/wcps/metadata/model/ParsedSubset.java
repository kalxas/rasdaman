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
package petascope.wcps.metadata.model;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Class to represent a subset interval, e.g. [0:100]
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 * @param <CoordinateType> (e.g String, Long,...)
 */
public class ParsedSubset<CoordinateType> {

    /**
     * Constructor for the class (case trimming)
     *
     * @param lowerLimit the lower limit of the interval
     * @param upperLimit the upper limit of the interval
     */
    public ParsedSubset(CoordinateType lowerLimit, CoordinateType upperLimit) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.subsetTrim = true;
    }

    /**
     * Constructor for the class (case slicing)
     *
     * @param slicingCoordinate the slicing coordinate
     */
    public ParsedSubset(CoordinateType slicingCoordinate) {
        this.slicingCoordinate = slicingCoordinate;
        this.subsetTrim = false;
    }

    /**
     * Getter for the lower limit of the interval
     *
     * @return
     */
    public CoordinateType getLowerLimit() {
        return lowerLimit;
    }

    /**
     * Setter for the lower limit of interval
     * @param lowerLimit
     */
    public void setLowerLimit(CoordinateType lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    /**
     * Getter for the upper limit of the interval
     *
     * @return
     */
    public CoordinateType getUpperLimit() {
        return upperLimit;
    }

    /**
     * Setter for the upper limit of interval
     * @param upperLimit
     */
    public void setUpperLimit(CoordinateType upperLimit) {
        this.upperLimit = upperLimit;
    }

    /**
     * Getter for the slicing coordinate
     *
     * @return slicingCoordinate
     */
    public CoordinateType getSlicingCoordinate() {
        return slicingCoordinate;
    }

    /**
     * Setter for subsetTrim for trimming
     * @param subsetTrim
     * @return
     */
    public void setTrimming(boolean subsetTrim) {
        this.subsetTrim = subsetTrim;
    }
    /**
     * Getter for subsetTrim for trimming
     *
     * @return subsetTrim
     */
    public boolean isTrimming() {
        return subsetTrim;
    }

    /**
     * Getter of subsetTrim for slicing
     *
     * @return !subsetTrim;
     */
    public boolean isSlicing() {
        return !subsetTrim;
    }

    /**
     * Setter of subsetTrim
     *
     * @param subsetTrim boolean
     */
    public void setSubsetTrim(boolean subsetTrim) {
        this.subsetTrim = subsetTrim;
    }

    /**
     * Setter of subsetScaleExtend, use to check if subset dimension is used in scale or extend operation
     * e.g: scale(c, {i(0:500), j(0:500)}) then subset is used.
     *
     * @param subsetScaleExtend boolean
     */
    public void setSubsetScaleExtend(boolean subsetScaleExtend) {
        this.subsetScaleExtend = subsetScaleExtend;
    }

    /**
     * Getter of subsetScaleExtend,use to check if subset dimension is used in scale or extend operation
     * @return
     */
    public boolean isSubsetScaleExtend() {
        return this.subsetScaleExtend;
    }

    /**
     * Indicates whether the interval bounds are numeric or not. If one of the
     * limits is *, the interval is still considered numeric. They can be
     * non-numeric in cases such as a[ i($x:$y)].
     *
     * @return
     */
    public boolean isCrsComputable() {
        if (isTrimming()) {
            if (isFullInterval()) {
                return false;
            }
            boolean isFirstDimensionNumeric = (NumberUtils.isNumber(lowerLimit.toString())
                                               || lowerLimit.toString().equals(WHOLE_DIMENSION_SYMBOL)
                                               || lowerLimit.toString().contains("-"));
            boolean isSecondDimensionNumeric = (NumberUtils.isNumber(upperLimit.toString())
                                                || upperLimit.toString().equals(WHOLE_DIMENSION_SYMBOL)
                                                || upperLimit.toString().contains("-"));
            return isFirstDimensionNumeric && isSecondDimensionNumeric;
        } else {
            boolean isNumeric = (NumberUtils.isNumber(slicingCoordinate.toString())
                                 || slicingCoordinate.toString().equals(WHOLE_DIMENSION_SYMBOL)
                                 || slicingCoordinate.toString().contains("-"));
            return isNumeric;
        }
    }

    /**
     * Returns true if the interval is of form *:*
     *
     * @return true if full interval false otherwise
     */
    private boolean isFullInterval() {
        return lowerLimit.toString().equals(WHOLE_DIMENSION_SYMBOL)
               && upperLimit.toString().equals(WHOLE_DIMENSION_SYMBOL);
    }

    private CoordinateType lowerLimit;
    private CoordinateType upperLimit;
    private CoordinateType slicingCoordinate;
    private boolean subsetTrim;
    private boolean subsetScaleExtend;
    public final static String WHOLE_DIMENSION_SYMBOL = "*";

}
