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
package petascope.wcps2.metadata.model;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class Interval<T> {

    T lowerLimit;
    T upperLimit;

    public Interval(T lowerLimit, T upperLimit) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    public T getLowerLimit() {
        return lowerLimit;
    }

    public T getUpperLimit() {
        return upperLimit;
    }

    public void setLowerLimit(T lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public void setUpperLimit(T upperLimit) {
        this.upperLimit = upperLimit;
    }
}
