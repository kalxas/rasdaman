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
package petascope.wcps.subset_axis.model;

import petascope.wcps.result.ParameterResult;

/**
 * Class to represent a part of WKT coordinates (e.g: (20 30, 30 40), (50 60, 70 80) )
 * then, it stores 20 30, 30 40 is a WKTCompoundPoint with numberOfDimensions is 2.
 * 
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class WKTCompoundPoint extends ParameterResult {
    
    private String point;  
    private int numberOfDimensions;

    public WKTCompoundPoint(String point, int numberOfDimensions) {
        this.point = point;
        this.numberOfDimensions = numberOfDimensions;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public int getNumberOfDimensions() {
        return numberOfDimensions;
    }

    public void setNumberOfDimensions(int numberOfDimensions) {
        this.numberOfDimensions = numberOfDimensions;
    }
        
    public String getStringRepresentation() {        
        return this.point;
    }
}
