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
package petascope.wcps.encodeparameters.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.rasdaman.domain.cis.NilValue;

/**
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class NoData {
    private static final String NO_DATA_NAN = "NaN";
    
    public NoData() {

    }
    
    // NOTE: rasql support only encoding nodata values in json such as "nodata": [ 12, 23, 45 ] then must subtract the values only from NilValues
    public NoData(List<NilValue> nilValues) {
        // store the nil values of metadata
        this.nilValues = nilValues;
        for (NilValue nilValue:nilValues) {
            if (nilValue.getValue() != null) {
                // NOTE: rasql does not support parsing NaN as float number or null values as interval (e.g: 9.96921e+35:*) yet.
                if (!(nilValue.getValue().equalsIgnoreCase(NO_DATA_NAN) 
                    || nilValue.getValue().contains(":"))) {
                    this.nodataValues.add(new BigDecimal(nilValue.getValue()));
                }
            }
        }        
    }   
    
    @JsonProperty("nodata")
    public void setNoDataValues(List<BigDecimal> nodataValues) {
        this.nodataValues = nodataValues;
    }

    @JsonProperty("nodata")
    public List<BigDecimal> getNoDataValues() {
        return this.nodataValues;
    }
    
    public List<NilValue> getNilValues() {
        return this.nilValues;
    }

    // this list is used to build rasql query
    private List<BigDecimal> nodataValues = new ArrayList<>();
    // this list is used to store the nilValues of metadata
    @JsonIgnore
    private List<NilValue> nilValues = new ArrayList<>();
}
