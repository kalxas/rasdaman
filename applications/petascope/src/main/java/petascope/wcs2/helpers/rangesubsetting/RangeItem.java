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
package petascope.wcs2.helpers.rangesubsetting;

import petascope.util.Pair;

/**
 * Implementation of RangeItem abstract class according to OGC-12-040 standard,
 * Requirement2.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */
public abstract class RangeItem {

    /**
     * Returns an interval of the range of components that should be selected
     * e.g. RANGESUBSET=red => (red,red); RANGESUBSET=red,blue => (red,blue)
     *
     * @return the interval of the range
     */
    public abstract Pair<String, String> getInterval();

    /**
     * Implementation of the toString method
     *
     * @return string representation of the object i.e. (startComponent,
     * endComponent)
     */
    @Override
    public String toString() {
        return this.getInterval().toString();
    }

    /**
     * Implementation of the hashCode method
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {
        int hash = this.getInterval().hashCode();
        return hash;
    }

    /**
     * Implementation of the equals method
     *
     * @param obj the object to compare with
     * @return true if the objects are equal false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        Boolean equal = false;
        if (obj.getClass() == this.getClass()) {
            equal = false;
        }
        if (this.hashCode() == obj.hashCode()) {
            equal = true;
        }
        return equal;
    }
}
