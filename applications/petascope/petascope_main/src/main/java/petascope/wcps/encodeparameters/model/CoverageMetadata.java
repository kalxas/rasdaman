/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.encodeparameters.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.util.LinkedHashMap;

/**
 * A class to represent the coverage's metadata from gmlcov:metadata element
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class CoverageMetadata {

    private Map<String, String> globalAttributesMap;

    @JsonProperty(value = "bands", access = Access.WRITE_ONLY)
    // Deserialize the JSON bands object but not serialize it as coverage's metadata (i.e: it belongs to band's metadata only).
    private BandsMetadata bandsMetadata;
    
    @JsonProperty(value = "axes", access = Access.WRITE_ONLY)
    // Deserialize the JSON bands object but not serialize it as coverage's metadata (i.e: it belongs to band's metadata only).
    private AxesMetadata axesMetadata;

    public CoverageMetadata() {
        this.globalAttributesMap = new LinkedHashMap<>();
        this.bandsMetadata = new BandsMetadata();
        this.axesMetadata = new AxesMetadata();
    }

    public Map<String, String> getGlobalAttributesMap() {
        return globalAttributesMap;
    }

    public void setGlobalAttributesMap(Map<String, String> globalAttributesMap) {
        this.globalAttributesMap = globalAttributesMap;
    }
    
    public BandsMetadata getBandsMetadata() {
        return bandsMetadata;
    }

    public void setBandsMetadata(BandsMetadata bandsMetadata) {
        this.bandsMetadata = bandsMetadata;
    }

    public AxesMetadata getAxesMetadata() {
        return axesMetadata;
    }

    public void setAxesMetadata(AxesMetadata axesMetadata) {
        this.axesMetadata = axesMetadata;
    }
    
    @JsonAnySetter
    // To map an unknown list of properties, must use this annotation
    public void addKeyValue(String key, String value) {
        this.globalAttributesMap.put(key, value);
    }
}
