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
package petascope.wcps2.subset_axis.model;

import petascope.wcps2.result.ParameterResult;

import java.util.List;

/**
 * Translates a list of dimension intervals
 * <code>
 * [x(0:10), y(0:100)]
 * </code>
 * translates to
 * <code>
 * 0:10,0:100
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class DimensionIntervalList extends ParameterResult {

    /**
     * Constructor for the class
     *
     * @param intervals a list of trim intervals
     */
    public DimensionIntervalList(List<WcpsSubsetDimension> intervals) {
        this.intervals = intervals;
    }

    /**
     * Returns a mutable list of the trim intervals
     *
     * @return the trim intervals
     */
    public List<WcpsSubsetDimension> getIntervals() {
        return intervals;
    }

    private final List<WcpsSubsetDimension> intervals;
}
