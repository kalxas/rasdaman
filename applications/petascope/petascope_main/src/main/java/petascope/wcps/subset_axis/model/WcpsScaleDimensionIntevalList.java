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
package petascope.wcps.subset_axis.model;

import petascope.wcps.result.ParameterResult;

import java.util.List;

/**
 * Translates a list of scaleAxes intervals from WCS to WCPS
 * NOTE: if axis is not mentioned, so the scaleFactor for this axis is 1,
 * otherwise the total pixel of the mentioned axis is total pixel / scaleFactor
 * <code>
 * [x(0.5), y(0.5)]
 * </code>
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class WcpsScaleDimensionIntevalList extends ParameterResult {

    /**
     * Constructor for the class
     *
     * @param intervals a list of trim intervals
     */
    public WcpsScaleDimensionIntevalList(List<AbstractWcpsScaleDimension> intervals) {
        this.intervals = intervals;
    }

    /**
     * Returns a mutable list of the trim intervals
     *
     * @return the trim intervals
     */
    public List<AbstractWcpsScaleDimension> getIntervals() {
        return intervals;
    }

    private final List<AbstractWcpsScaleDimension> intervals;
}
