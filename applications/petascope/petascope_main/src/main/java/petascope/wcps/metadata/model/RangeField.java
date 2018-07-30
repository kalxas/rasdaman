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
package petascope.wcps.metadata.model;

import java.util.List;
import org.rasdaman.domain.cis.AllowedValue;
import org.rasdaman.domain.cis.NilValue;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class RangeField {

    private String dataType;

    private String name;

    private String description;

    private List<NilValue> nodata;

    private String uomCode;

    private String definition;

    private List<AllowedValue> allowedValues;
    
    public RangeField() {
        
    }

    public RangeField(String dataType, String name, String description, List<NilValue> nodata, 
                      String uomCode, String definition, List<AllowedValue> allowedValues) {
        this.dataType = dataType;
        this.name = name;
        this.description = description;
        this.nodata = nodata;
        this.uomCode = uomCode;
        this.definition = definition;
        this.allowedValues = allowedValues;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    
    public void setNodata(List<NilValue> nodata) {
        this.nodata = nodata;
    }

    public List<NilValue> getNodata() {
        return nodata;
    }

    public String getUomCode() {   
        if (uomCode == null) {
            return RangeField.UOM_CODE;
        } else if (uomCode.trim().isEmpty()) {
            return RangeField.UOM_CODE;
        } else {
            return uomCode;
        }
    }

    public String getDefinition() {
        return definition;
    }

    public List<AllowedValue> getAllowedValues() {
        return allowedValues;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUomCode(String uomCode) {
        this.uomCode = uomCode;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setAllowedValues(List<AllowedValue> allowedValues) {
        this.allowedValues = allowedValues;
    }
    
    public static final String DATA_TYPE = "double";
    public static final String UOM_CODE = "10^0";
    // used in case of creating coverage constructor
    public static final String DEFAULT_NAME = "band1";
}
