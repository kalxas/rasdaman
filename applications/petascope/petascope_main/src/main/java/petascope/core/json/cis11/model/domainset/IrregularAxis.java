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
package petascope.core.json.cis11.model.domainset;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import static petascope.core.json.cis11.JSONCIS11GetCoverage.TYPE_NAME;

/**
 *
 * Class to represent irregularAxis in JSON CIS 1.1. e.g:
     
{
    "type": "IrregularAxisType",
    "axisLabel": "h",
    "uomLabel": "m",
    "coordinate": [0, 100, 1000]
}

 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@JsonPropertyOrder({TYPE_NAME})
public class IrregularAxis extends AbstractAxis {
    
    public static final String COEFFICIENTS_NAME = "coordinate";
    
    private final String type = "IrregularAxisType";
    private String uomLabel;
    private List<Object> coefficients;

    public IrregularAxis(String axisLabel, String uomLabel, List<Object> coefficients) {
        super(axisLabel);
        this.uomLabel = uomLabel;
        this.coefficients = coefficients;
    }
    
    public String getType() {
        return type;
    }    

    public String getAxisLabel() {
        return axisLabel;
    }

    public void setAxisLabel(String axisLabel) {
        this.axisLabel = axisLabel;
    }

    public String getUomLabel() {
        return uomLabel;
    }

    public void setUomLabel(String uomLabel) {
        this.uomLabel = uomLabel;
    }

    @JsonProperty(COEFFICIENTS_NAME)
    public List<Object> getCoefficients() {
        return coefficients;
    }

    @JsonProperty(COEFFICIENTS_NAME)
    public void setCoefficients(List<Object> coefficients) {
        this.coefficients = coefficients;
    }
}
