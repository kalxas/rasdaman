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
  *  Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.encodeparameters.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class to represent the axes's metadata (axes element) inside gmlcov:metadata element
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class AxesMetadata {
    
    public static final String AXES_METADATA_ROOT_TAG = "axes";
    
    private Map<String, Map<String, String>> axesAttributesMap;
    
    public AxesMetadata() {
        this.axesAttributesMap = new LinkedHashMap<>();
    }

    public Map<String, Map<String, String>> getAxesAttributesMap() {
        return axesAttributesMap;
    }

    public void setAxesAttributesMap(Map<String, Map<String, String>> axesAttributesMap) {
        this.axesAttributesMap = axesAttributesMap;
    }
    
    @JsonAnySetter
    public void addKeyValue(String key, Map<String, String> values) {
        this.axesAttributesMap.put(key, values);
    }
}
