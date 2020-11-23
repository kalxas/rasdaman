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
package petascope.core.json.cis11.model.metadata;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import petascope.core.gml.metadata.model.CoverageMetadata;

/**
 * Class represent metadata element in JSON CIS 1.1 ,e.g:
 * 
"metadata": {
    "bands": {
        "dc": {
            "units": "%",
            "grid_type": "gaussian",
            "_FillValue": "-9999.0",
            "missing_value": "-9999.0",
            "title": "Drought Code"
        }
    },
    "axes": {
        "Lon": {
            "standard_name": "longitude",
            "long_name": "longitude",
            "units": "degrees_east",
            "axis": "X"
        },
        "Lat": {
            "standard_name": "latitude",
            "long_name": "latitude",
            "units": "degrees_north",
            "axis": "Y"
        }
    },
    "slices": {
        "slice": [{
            "boundedBy": {
                "Envelope": {
                    "axisLabels": "Lat Lon ansi forecast",
                    "srsDimension": 4,
                    "lowerCorner": "34.4396675 29.6015625 \"2017-01-10T00:00:00+00:00\" 0",
                    "upperCorner": "34.7208095 29.8828125 \"2017-01-10T00:00:00+00:00\" 0"
                }
            },
                "local_metadata_key": "FROM FILE 1"
        }, {
            "boundedBy": {
                "Envelope": {
                    "axisLabels": "Lat Lon ansi forecast",
                    "srsDimension": 4,
                    "lowerCorner": "34.4396675 29.6015625 \"2017-02-10T00:00:00+00:00\" 3",
                    "upperCorner": "34.7208095 29.8828125 \"2017-02-10T00:00:00+00:00\" 3"
                }
            },
            "local_metadata_key": "FROM FILE 2"
        }]
    },
    "Title": "Drought code"
}

 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class Metadata {
    
    public static final String METADATA_NAME = "metadata";
   
    // Unwrapp this property to get all contents in JSON output
    @JsonUnwrapped
    
    private CoverageMetadata coverageMetadata;

    public Metadata(CoverageMetadata coverageMetadata) {        
        this.coverageMetadata = coverageMetadata;
    }

    public CoverageMetadata getCoverageMetadata() {
        return coverageMetadata;
    }

    public void setCoverageMetadata(CoverageMetadata coverageMetadata) {
        this.coverageMetadata = coverageMetadata;
    }
    
}
