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
package org.rasdaman.migration.domain.legacy;

/**
* Representation of a subset in a WCS request.
*/
public class LegacyDimensionSubset {

    public static final String QUOTED_SUBSET = "^\".*\"$"; // switch from numeric to ISO8601 coordinates for time
    public static final String ASTERISK = "*";
    protected final String dimension;
    //protected final String crs;
    protected String crs;
    protected boolean isNumeric = true;

    public LegacyDimensionSubset(String dimension) {
        this(dimension, null);
    }

    public LegacyDimensionSubset(String dimension, String crs) {
        this.dimension = dimension;
        this.crs = crs;
    }

    public LegacyDimensionSubset(String dimension, String crs, boolean isNumeric) {
        this.dimension = dimension;
        this.crs = crs;
        this.isNumeric = isNumeric;
    }

    public String getDimension() {
        return dimension;
    }

    public String getCrs() {
        return crs;
    }

    // When transforming a subset, change crs accordingly
    public void setCrs(String value) {
        crs = value;
    }

    public boolean isNumeric() {
        return isNumeric;
    }

    @Override
    public String toString() {
        return dimension + ((crs != null) ? "," + crs : "");
    }
}
