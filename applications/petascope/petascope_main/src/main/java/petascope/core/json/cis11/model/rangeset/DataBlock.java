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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.json.cis11.model.rangeset;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import static petascope.core.json.cis11.JSONCIS11GetCoverage.TYPE_NAME;

/**
 * Class to build DataBlock element of RangeSet in JSON CIS 1.1. e.g: 
 
"dataBlock": {
    "type": "VDataBlockType",
    "values": [1,2,3,4,5,6,7,8,9, 10,11,12,13,14,15,16,17,18, 19,20,21,22,23,24,25,26,27]
}

* @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@JsonPropertyOrder({TYPE_NAME})
public class DataBlock {
    
    public static final String PIXEL_VALUES_NAME = "values";
    
    private final String type ="VDataBlockType";
    
    private List<Object> pixelValues;

    public DataBlock(List<Object> pixelValues) {
        this.pixelValues = pixelValues;
    }
    
    public String getType() {
        return type;
    }    

    @JsonProperty(PIXEL_VALUES_NAME)
    public List<Object> getPixelValues() {
        return pixelValues;
    }

    @JsonProperty(PIXEL_VALUES_NAME)
    public void setPixelValues(List<Object> pixelValues) {
        this.pixelValues = pixelValues;
    }
    
}
