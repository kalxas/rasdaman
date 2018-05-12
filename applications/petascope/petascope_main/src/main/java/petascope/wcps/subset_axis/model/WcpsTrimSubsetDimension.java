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
package petascope.wcps.subset_axis.model;

/**
 *
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class WcpsTrimSubsetDimension extends WcpsSubsetDimension {
    private String lowerBound;
    private String upperBound;

    public WcpsTrimSubsetDimension(String axisName, String crs, String lowerBound, String upperBound) {
        super(axisName, crs);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public String getLowerBound() {
        return lowerBound;
    }

    public String getUpperBound() {
        return upperBound;
    }

    public void setLowerBound(String lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(String upperBound) {
        this.upperBound = upperBound;
    }

    @Override
    public String getStringBounds() {
        return lowerBound + ":" + upperBound;
    }

    @Override
    public String toString() {
        String result = getAxisName();
        if(getCrs() != null && !getCrs().isEmpty()){
            result += ":\"" + getCrs() + "\"";
        }
        result += "(" + lowerBound + ":" + upperBound + ")";
        return result;
    }
}
