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

package petascope.wms2.service.getmap.access;

/**
 * Class that represent a subset in rasdaman
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class RasdamanSubset implements Comparable {

    /**
     * Constructor for the class
     *
     * @param order the order of this axis in rasdaman
     * @param min   the min value of the subset
     * @param max   the max value of the subset
     */
    public RasdamanSubset(int order, long min, long max) {
        this.order = order;
        this.min = min;
        this.max = max;
    }

    /**
     * Returns the min value of the subset
     *
     * @return the min value of the subset
     */
    public long getMin() {
        return min;
    }

    /**
     * Returns the max value of the subset
     *
     * @return the max value of the subset
     */
    public long getMax() {
        return max;
    }

    /**
     * Returns the order of the rasdaman axis
     *
     * @return the order of the rasdaman axis
     */
    public int getOrder() {
        return order;
    }

    private final int order;
    private final long min;
    private final long max;

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!(o instanceof RasdamanSubset)) {
            throw new ClassCastException();
        }
        RasdamanSubset sub = (RasdamanSubset) o;
        return Integer.valueOf(order).compareTo(sub.getOrder());
    }
}
