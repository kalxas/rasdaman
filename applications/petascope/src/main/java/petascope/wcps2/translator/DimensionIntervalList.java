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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.translator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
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
public class DimensionIntervalList extends IParseTreeNode {

    /**
     * Constructor for the class
     *
     * @param intervals a list of trim intervals
     */
    public DimensionIntervalList(List<TrimDimensionInterval> intervals) {
        this.intervals = intervals;
        for(TrimDimensionInterval interval: intervals){
            addChild(interval);
        }
    }

    @Override
    public String toRasql() {
        List<String> dimensionIntervalsString = new ArrayList<String>(intervals.size());
        Collections.sort(intervals);
        for (TrimDimensionInterval interval : intervals) {
            dimensionIntervalsString.add(interval.toRasql());
        }
        return StringUtils.join(dimensionIntervalsString, ",");
    }

    /**
     * Returns a mutable list of the trim intervals
     *
     * @return the trim intervals
     */
    public List<TrimDimensionInterval> getIntervals() {
        return intervals;
    }


    @Override
    protected String nodeInformation() {
        return new StringBuilder("(").append(intervals.toString()).append(")").toString();
    }

    private final List<TrimDimensionInterval> intervals;
}
