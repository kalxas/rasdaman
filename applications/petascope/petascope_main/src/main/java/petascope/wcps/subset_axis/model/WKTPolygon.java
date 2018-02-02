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
 * Class to represent WKT Polygon object
 * e.g: POLYGON((20 30, 40 50, 60 70), (70 70, 60 80, 50 60))
 * 
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WKTPolygon extends AbstractWKTShape {
    
    public WKTPolygon(List<WKTCompoundPoints> wktCompoundPointsList) {
        super(wktCompoundPointsList);        
        this.defaultNumberOfDimensions = 2;
    }            

    @Override
    public String getStringRepresentation() {
        // e.g: POLYGON((20 30, 40 50), (60 70, 80 90))
        String coordinates = this.wktCompoundPointsList.get(0).getStringRepresentation();
        String result = TEMPLATE.replace(TEMPLATE_COORDINATES, coordinates);        
        return result;
    }
    
    @Override
    public String getStringRepresentation(String translatedCoordinates) {
        String translatedWKT = TEMPLATE.replace(TEMPLATE_COORDINATES, translatedCoordinates);
        return translatedWKT;
    }
    
    private static final String TEMPLATE = "POLYGON" + TEMPLATE_COORDINATES;
}
