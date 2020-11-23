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
 * Class to represent gridLimits element in JSON CIS 1.1. e.g:
 
"gridLimits": {
    "type": "GridLimitsType",
    "srsName": "http://www.opengis.net/def/crs/OGC/0/Index2D",
    "axisLabels": ["i", "j"],
    "axis": [{
        "type": "IndexAxisType",
        "axisLabel": "i",
        "lowerBound": 0,
        "upperBound": 2
    },{
        "type": "IndexAxisType",
        "axisLabel": "j",
        "lowerBound": 0,
        "upperBound": 2
    }]
}
  
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@JsonPropertyOrder({TYPE_NAME})
public class GridLimits {
    
    public static final String INDEX_AXES_NAME = "axis";
    
    private final String type = "GridLimitsType";
    private String srsName;
    private List<String> axisLabels;
    private List<IndexAxis> indexAxes;

    public GridLimits(String srsName, List<String> axisLabels, List<IndexAxis> indexAxes) {
        this.srsName = srsName;
        this.axisLabels = axisLabels;
        this.indexAxes = indexAxes;
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

    @JsonProperty(INDEX_AXES_NAME)
    public List<IndexAxis> getIndexAxes() {
        return indexAxes;
    }

    @JsonProperty(INDEX_AXES_NAME)
    public void setIndexAxes(List<IndexAxis> indexAxes) {
        this.indexAxes = indexAxes;
    }
}
