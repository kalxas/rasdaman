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

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class Spatial {
    
    public static final String DEFAULT_SPATIAL_CRS = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
    
    /**
     * https://github.com/opengeospatial/ogc_api_coverages/blob/master/standard/openapi/schemas/common/extent.yaml#L14
     */
    private List<List<BigDecimal>> bbox;
    /**
     * default is WGS84 Long Lat order
     * https://github.com/opengeospatial/ogc_api_coverages/blob/master/standard/openapi/schemas/common/extent.yaml#L22
     */
    private String crs = DEFAULT_SPATIAL_CRS;

    public Spatial(List<List<BigDecimal>> bboxWGS84Values) {
        this.bbox = bboxWGS84Values;
    }

    public List<List<BigDecimal>> getBbox() {
        return bbox;
    }

    public String getCrs() {
        return crs;
    }
    
}
