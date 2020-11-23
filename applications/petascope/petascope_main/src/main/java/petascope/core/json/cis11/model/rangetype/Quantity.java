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
package petascope.core.json.cis11.model.rangetype;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import static petascope.core.json.cis11.JSONCIS11GetCoverage.TYPE_NAME;

/**
 * Class to represent Quantity element in CIS 1.1. e.g:
 
{
    "type": "QuantityType",
    "name": "band1",
    "description": "Radiation dose measured by Gamma detector",
    "definition": "ogcType:unsignedInt",
    "uom": {
        "type": "UnitReference",
        "code": "10^0"
    }
}

* @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@JsonPropertyOrder({TYPE_NAME})
public class Quantity {
    
    private final String type = "QuantityType";
    
    private String name;
    private String description;
    // Data type (e.g: unsignedInt)
    private String defintion;
    private NilValues nilValues;
    private UoM uom;

    public Quantity() {
        
    }

    public Quantity(String name, String description, String defintion, NilValues nilValues, UoM uom) {
        this.name = name;
        this.description = description;
        this.defintion = defintion;
        this.nilValues = nilValues;
        this.uom = uom;
    }
    
    public String getType() {
        return type;
    }    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefintion() {
        return defintion;
    }

    public void setDefintion(String defintion) {
        this.defintion = defintion;
    }

    public NilValues getNilValues() {
        return nilValues;
    }

    public void setNilValues(NilValues nilValues) {
        this.nilValues = nilValues;
    }

    public UoM getUom() {
        return uom;
    }

    public void setUom(UoM uom) {
        this.uom = uom;
    }
}
