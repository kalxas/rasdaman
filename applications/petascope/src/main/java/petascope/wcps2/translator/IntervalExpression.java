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

/**
 * Translation node from wcps interval  rasql
 * Example:
 * <code>
 * 0:100
 * </code>
 * translates to
 * <code>
 * [0:100]
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class IntervalExpression extends IParseTreeNode {

    /**
     * Constructor for the  class
     *
     * @param low  the lower bound of the interval
     * @param high the upper bound of the interval
     */
    public IntervalExpression(String low, String high) {
        this.low = low;
        this.high = high;
    }

    @Override
    public String toRasql() {
        String template = TEMPLATE.replace("$low", this.low).replace("$high", this.high);
        return template;
    }

    /**
     * Returns the lower bound of the interval
     *
     * @return
     */
    public String getLowerBound() {
        return this.low;
    }

    /**
     * Returns the upper bound of the interval
     *
     * @return
     */
    public String getUpperBound() {
        return this.high;
    }

    /**
     * Returns the number of cells in the interval
     *
     * @return
     */
    public Integer cellCount() {
        return Integer.valueOf(this.high) - Integer.valueOf(this.low) + 1;
    }

    private String low;
    private String high;
    private String TEMPLATE = "[$low:$high]";
}
