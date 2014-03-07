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
 * This class represents a Range Interval type as described in OGC-12-040
 * standard, Requirement2.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */
public class RangeInterval extends RangeItem {

    /**
     * Constructor for the class
     * @param lowComponent - the low component of the range (e.g. in red:blue = red)
     * @param highComponent - the high component of the range (e.g. in red:blue = blue)
     */
    public RangeInterval(String lowComponent, String highComponent) {
        this.lowComponent = lowComponent;
        this.highComponent = highComponent;
    }

    /**
     * Returns the low component of the range 
     * @return 
     */
    public String getLowComponent() {
        return lowComponent;
    }

    /**
     * Returns the high component of the range
     * @return 
     */
    public String getHighComponent() {
        return highComponent;
    }

    /**
     * String representation of the object
     * @return 
     */
    @Override
    public String toString() {
        return "RangeInterval{" + "lowComponent=" + lowComponent + ", highComponent=" + highComponent + '}';
    }
    private String lowComponent;
    private String highComponent;

    /**
     * Implementation of the RangeItem interface
     * @return a pair of type {low, high}
     */
    @Override
    public Pair<String, String> getInterval() {
        return new Pair<String, String>(this.lowComponent, this.highComponent);
    }
}
