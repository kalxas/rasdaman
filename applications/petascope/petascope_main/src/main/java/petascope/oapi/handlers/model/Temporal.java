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
package petascope.oapi.handlers.model;

import java.util.List;

/**
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class Temporal {
    
    public static final String DEFAULT_TEMPORAL_CRS = "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian";
    
    /**
     * https://github.com/opengeospatial/ogc_api_coverages/blob/master/standard/openapi/schemas/common/extent.yaml#L51
     */
    private List<List<String>> intervals;
    /**
     * https://github.com/opengeospatial/ogc_api_coverages/blob/master/standard/openapi/schemas/common/extent.yaml#L75
     */
    private String trs = DEFAULT_TEMPORAL_CRS;

    public Temporal(List<List<String>> intervals) {
        this.intervals = intervals;
    }

    public List<List<String>> getIntervals() {
        return intervals;
    }

    public String getTrs() {
        return trs;
    }
    
}
