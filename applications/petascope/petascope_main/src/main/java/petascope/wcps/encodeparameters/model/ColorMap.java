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
 * Copyright 2003 - 2019 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.encodeparameters.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rasdaman ColorMap format
 * e.g: 
   "colorMap": {
        "type": "values",
        "colorTable": {
          "-1": [255, 255, 255, 0],
          "-0.5": [125, 125, 125, 255],
          "1": [0, 0, 0, 255]
        }
    }
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class ColorMap {
    
    private String type;
    
    private Map<String, List<Integer>> colorTable = new LinkedHashMap<>();

    public ColorMap() {
        
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, List<Integer>> getColorTable() {
        return colorTable;
    }

    public void setColorTable(Map<String, List<Integer>> colorTable) {
        this.colorTable = colorTable;
    }

}
