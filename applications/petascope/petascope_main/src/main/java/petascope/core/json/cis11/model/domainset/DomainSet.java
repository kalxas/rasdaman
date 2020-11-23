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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import static petascope.core.json.cis11.JSONCIS11GetCoverage.TYPE_NAME;

/**
 * Class to represent domainSet element in JSON CIS 1.1. e.g:
 
"domainSet": {
    "type": "DomainSetType",
    "generalGrid":{
        "type": "GeneralGridCoverageType",
        "srsName": "http://www.opengis.net/def/crs/EPSG/0/4326",
        "axisLabels": ["Lat", "Long"],
        "axis": [{
            "type": "RegularAxisType",
            "axisLabel": "Lat",
            "lowerBound": -80,
            "upperBound": -70,
            "uomLabel": "deg",
            "resolution": 5
        },{
            "type": "RegularAxisType",
            "axisLabel": "Long",
            "lowerBound": 0,
            "upperBound": 10,
            "uomLabel": "deg",
            "resolution": 5
        }],
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
    }
}
    
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@JsonPropertyOrder({TYPE_NAME})
public class DomainSet {
    
    public static final String DOMAIN_SET_NAME = "domainSet";
    
    private final String type = "DomainSetType";
    
    private GeneralGrid generalGrid;

    public DomainSet(GeneralGrid generalGrid) {
        this.generalGrid = generalGrid;
    }
    
    public String getType() {
        return type;
    }

    public GeneralGrid getGeneralGrid() {
        return generalGrid;
    }

    public void setGeneralGrid(GeneralGrid generalGrid) {
        this.generalGrid = generalGrid;
    }
}
