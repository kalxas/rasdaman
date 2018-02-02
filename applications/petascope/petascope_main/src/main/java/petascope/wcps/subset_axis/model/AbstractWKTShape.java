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
import petascope.wcps.result.ParameterResult;

/**
 * Abstract class of all WKT shapes (point, linestring, polygon,...)
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public abstract class AbstractWKTShape extends ParameterResult {
    
    protected List<WKTCompoundPoints> wktCompoundPointsList;

    public AbstractWKTShape(List<WKTCompoundPoints> wktCompoundPointsList) {
        this.wktCompoundPointsList = wktCompoundPointsList;
    }   

    public List<WKTCompoundPoints> getWktCompoundPointsList() {
        return wktCompoundPointsList;
    }

    public void setWktCompoundPointsList(List<WKTCompoundPoints> wktCompoundPointsList) {
        this.wktCompoundPointsList = wktCompoundPointsList;
    }
     
    public int getDefaultNumberOfDimensions() {
        return this.defaultNumberOfDimensions;
    }
    
    /**
     * From the coordinates (original or translated to grid coordinates),
     * return the string representing this shape.
     * 
     * @return String representing this shape.
     */
    public abstract String getStringRepresentation();
    
    
    /**
     * Return a WKT with translated coordinates from geo coordinates to grid coordinates
     * @param translatedCoordinates translated coordinates in grid pixel.
     * @return String representing this shape.
     */
    public abstract String getStringRepresentation(String translatedCoordinates);
    
    public static final String TEMPLATE_COORDINATES = "$coordinates";
    /**
     * A WKT shape only has a specific number of dimensions (e.g: polygon is 2, linestring is 1)
     */
    protected int defaultNumberOfDimensions = 0;
}
