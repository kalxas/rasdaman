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
package petascope.core.gml.metadata.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In case coverage is rotated grid CRS (e.g. COSMO 101), there is "grid_mapping" element in coverage's global metadata
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class GridMapping {
    
    private Map<String, String> metadataAttributesMap = new LinkedHashMap<>();
    // for example, grid_mapping variable in rotated netCDF file is called "rotated_pole"
    public static final String IDENTIFIER = "identifier";

    public GridMapping() {
    }    
    
    public GridMapping(Map<String, String> metadataAttributesMap) {
        this.metadataAttributesMap = metadataAttributesMap;
    }
    
    @JsonAnySetter
    // NOTE: To map an unknown list of properties, must use this annotation
    public void addKeyValue(String key, String value) {
        this.metadataAttributesMap.put(key, value);
    }
    
    @JsonAnyGetter
    // NOTE: to unwrap the "map" from { "map": { "key": "value" } }, only keep { "key": "value" }
    public Map<String, String> getGlobalAttributesMap() {
        return metadataAttributesMap;
    }
     
}
