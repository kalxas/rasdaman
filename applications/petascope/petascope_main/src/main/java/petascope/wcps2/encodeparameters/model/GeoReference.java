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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/
package petascope.wcps2.encodeparameters.model;

import petascope.core.BoundingBox;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class to store geoReference parameter
 * e.g: Rasql: \"geoReference\": { \"bbox\": { \"xmin\": 0.5, \"xmax\": 30, \"ymin\": -15, \"ymax\": 50.3}, \"crs\": \"EPSG:4326\" }
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class GeoReference {
    
    public GeoReference() {
        
    }

    public GeoReference(BoundingBox bbox, String crs) {
        this.bbox = bbox;
        this.crs = crs;
    }
    
    public void setBoundingBox(BoundingBox bbox) {
        this.bbox = bbox;
    }

    @JsonProperty("bbox")
    // Deserialize to bbox key in JSON
    public BoundingBox getBoundingBox() {
        return this.bbox;
    }
    
    public void setCrs(String crs) {
        this.crs = crs;
    }

    public String getCrs() {
        return this.crs;
    }

    // As a coverage can only be transformed by a geo-referenced CRS
    private String crs = null;
    private BoundingBox bbox = null;
}
