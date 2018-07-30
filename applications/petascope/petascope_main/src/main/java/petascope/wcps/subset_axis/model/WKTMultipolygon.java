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

import java.util.ArrayList;
import java.util.List;
import petascope.util.ListUtil;

/**
 * Class to represent WKT Polygon object
 * e.g: Multipolygon ( ((20 30, 30 40, 50 60)) --polygon 1, ((20 30, 30 40, 50 60), (20 30, 30 40, 50 60)) --polygon 2 )
 * 
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class WKTMultipolygon extends AbstractWKTShape {
    
    public WKTMultipolygon(List<WKTCompoundPoints> wktCompoundPointsList) {
        super(wktCompoundPointsList);        
        this.defaultNumberOfDimensions = 2;        
    }            

    @Override
    public String getStringRepresentation() {
        // e.g: MULTIPOLGON( ((20 30, 40 50, 60 70)), ((20 30, 40 50), (60 70, 80 90)) )
        List<String> polygonList = new ArrayList<>();
        
        // Iterate each Polygon coordinates to concatenate to Multipolygon coordinates
        for (WKTCompoundPoints tmp : this.wktCompoundPointsList) {
            String polygon = "(" + tmp.getStringRepresentation() + ")";
            polygonList.add(polygon);            
        }
        String coordinates = ListUtil.join(polygonList, ",");
        String result = TEMPLATE.replace(TEMPLATE_COORDINATES, coordinates);
        
        return result;
    }
    
    @Override
    public String getStringRepresentation(String translatedCoordinates) {
        String translatedWKT = TEMPLATE.replace(TEMPLATE_COORDINATES, translatedCoordinates);
        return translatedWKT;
    }
    
    private static final String TEMPLATE = "MULTIPOLYGON(" + TEMPLATE_COORDINATES + ")";
}
