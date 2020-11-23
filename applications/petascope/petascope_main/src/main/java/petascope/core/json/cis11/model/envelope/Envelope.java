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
package petascope.core.json.cis11.model.envelope;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Class to represent envelope element in JSON CIS 1.1. e.g:
 
"envelope": {
    "type": "EnvelopeByAxisType",
    "srsName": "http://www.opengis.net/def/crs/OGC/0/Index2D",
    "axisLabels": ["Lat", "Long"],
    "axis": [{
        "type": "AxisExtentType",
        "axisLabel": "Lat",
        "lowerBound": -80,
        "upperBound": -70,
        "uomLabel": "deg"
    },{
        "type": "AxisExtentType",
        "axisLabel": "Long",
        "lowerBound": 0,
        "upperBound": 10,
        "uomLabel": "deg"
    }]
}

* @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class Envelope {
    
    public static final String ENVELOPE_NAME = "envelope";
    
    private final String type = "EnvelopeByAxisType";
    
    private String srsName;
    private List<String> axisLabels;
    List<AxisExtent> axisExtents;

    public Envelope(String srsName, List<String> axisLabels, List<AxisExtent> axisExtents) {
        this.srsName = srsName;
        this.axisLabels = axisLabels;
        this.axisExtents = axisExtents;
    }
    
    public String getType() {
        return type;
    }

    public String getSrsName() {
        return srsName;
    }

    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

    public List<String> getAxisLabels() {
        return axisLabels;
    }

    public void setAxisLabels(List<String> axisLabels) {
        this.axisLabels = axisLabels;
    }

    @JsonProperty("axis")
    public List<AxisExtent> getAxisExtents() {
        return axisExtents;
    }

    public void setAxisExtents(List<AxisExtent> axisExtents) {
        this.axisExtents = axisExtents;
    }
    
}
