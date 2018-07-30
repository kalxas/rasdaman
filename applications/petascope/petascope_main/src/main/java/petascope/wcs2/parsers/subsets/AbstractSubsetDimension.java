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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.parsers.subsets;

/**
 * Representation of a subset in a WCS request.
 */
public abstract class AbstractSubsetDimension {

    public static final String QUOTED_SUBSET = "^\".*\"$"; // switch from numeric to ISO8601 coordinates for time
    public static final String ASTERISK = "*";
    protected final String dimensionName;
    //protected final String crs;
    protected String crs;
    protected boolean isNumeric = true;

    public AbstractSubsetDimension(String dimension) {
        this(dimension, null);
    }

    public AbstractSubsetDimension(String dimension, String crs) {
        this.dimensionName = dimension;
        this.crs = crs;
    }

    public AbstractSubsetDimension(String dimension, String crs, boolean isNumeric) {
        this.dimensionName = dimension;
        this.crs = crs;
        this.isNumeric = isNumeric;
    }

    public String getDimensionName() {
        return dimensionName;
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
        // e.g: E,http://.../EPSG/0/3857(1023423,2352356) or i(10,20)
        return dimensionName + ((crs != null) ? "," + crs : "") + this.getSubsetBoundsRepresentationWCS();
    }
    
    // trim WCS: (10,20)
    public abstract String getSubsetBoundsRepresentationWCS();
    
    // trim WCPS: (10:20)
    public abstract String getSubsetBoundsRepresentationWCPS();
}
