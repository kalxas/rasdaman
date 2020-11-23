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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import static petascope.core.json.cis11.JSONCIS11GetCoverage.TYPE_NAME;

/**
 * Class to represent uom in JSON CIS 1.1, e.g:
 
    "uom": {
        "type": "UnitReference",
        "code": "10^0"
    }
    
* @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@JsonPropertyOrder({TYPE_NAME})
public class UoM {
    
    private final String type = "UnitReference";
    
    private String code = "10^0";

    public UoM(String code) {
        this.code = code;
    }
    
    public String getType() {
        return type;
    }    

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
}
