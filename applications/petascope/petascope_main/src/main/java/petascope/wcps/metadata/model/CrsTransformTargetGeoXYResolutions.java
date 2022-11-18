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
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.metadata.model;

/**
 *
 * Model object to host crsTransform target geo XY resolutions 
 * e.g. {Lat:0.5, Lon:0.3}
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class CrsTransformTargetGeoXYResolutions {
    
    private String geoResolutionAxisLabelX, geoResolutionX,
            geoResolutionAxisLabelY, geoResolutionY;

    public CrsTransformTargetGeoXYResolutions(String geoResolutionAxisLabelX, String geoResolutionX, 
                                              String geoResolutionAxisLabelY, String geoResolutionY) {
        this.geoResolutionAxisLabelX = geoResolutionAxisLabelX.trim();
        this.geoResolutionX = geoResolutionX.trim();
        this.geoResolutionAxisLabelY = geoResolutionAxisLabelY.trim();
        this.geoResolutionY = geoResolutionY.trim();
    }

    public String getGeoResolutionAxisLabelX() {
        return geoResolutionAxisLabelX;
    }

    public String getGeoResolutionX() {
        return geoResolutionX;
    }

    public String getGeoResolutionAxisLabelY() {
        return geoResolutionAxisLabelY;
    }

    public String getGeoResolutionY() {
        return geoResolutionY;
    }
    
    
    
    
}
