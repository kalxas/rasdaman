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

import java.util.List;

/**
 * Class to represent WKT LineString object
 * e.g:  LINESTRING(20 30, 40 50)
 * 
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WKTLineString extends AbstractWKTShape {
    
    public WKTLineString(List<WKTCompoundPoints> wktCompoundPointsList) {
        super(wktCompoundPointsList);
        this.defaultNumberOfDimensions = 1;
    }            

    @Override
    public String getStringRepresentation() {
        // e.g: LINESTRING(20 30, 40 50)
        String coordinates = this.wktCompoundPointsList.get(0).getStringRepresentation();
        String result = TEMPLATE.replace(TEMPLATE_COORDINATES, coordinates);        
        return result;
    }

    @Override
    public String getStringRepresentation(String translatedCoordinates) {
        // Replace any (,) in input string as it is not used
        translatedCoordinates = translatedCoordinates.replace("(", "").replace(")", "");
        String translatedWKT = TEMPLATE.replace(TEMPLATE_COORDINATES, translatedCoordinates);
        return translatedWKT;
    }
    
    private static final String TEMPLATE = "LINESTRING(" + TEMPLATE_COORDINATES + ")";
}
