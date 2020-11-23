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
package petascope.core.json.cis11.model.rangetype;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import static petascope.core.json.cis11.JSONCIS11GetCoverage.TYPE_NAME;

/**
 * Class to represent rangeType element in JSON CIS 1.1. e.g:
 * 
"rangeType": {
    "type": "DataRecordType",
    "field":[{
        "type": "QuantityType",
        name="singleBand",
        "definition": "ogcType:unsignedInt",
        "uom": {
            "type": "UnitReference",
            "code": "10^0"
        }
    }]
}
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@JsonPropertyOrder({TYPE_NAME})
public class RangeType {
    
    public static final String RANGE_TYPE_NAME = "rangeType";
    
    public static final String FIELD_NAME = "field";
    
    private static final String type = "DataRecordType";
    
    private List<Quantity> quantities;

    public RangeType() {
        
    }

    public String getType() {
        return type;
    }
    
    public RangeType(List<Quantity> quantities) {
        this.quantities = quantities;
    }

    @JsonProperty(FIELD_NAME)
    public List<Quantity> getQuantities() {
        return quantities;
    }

    @JsonProperty(FIELD_NAME)
    public void setQuantities(List<Quantity> quantities) {
        this.quantities = quantities;
    }
    
}
