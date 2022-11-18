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
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.metadata.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import petascope.core.BoundingBox;
import petascope.util.StringUtil;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/**
 *
 * Model object to host crsTransform target geo XY bounding box
 * e.g. {Lat (30:50), Lon (40:50) } or {domain($c)} with $c is 2D
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class CrsTransformTargetGeoXYBoundingBox {
    
    List<WcpsTrimSubsetDimension> geoXYSubsets = new ArrayList<>();

    public CrsTransformTargetGeoXYBoundingBox(String axisLabelX, String trimSubsetX,
                                            String axisLabelY, String trimSubsetY) {
        
        if (axisLabelX != null) {
        
            // e.g. (30:50)
            String[] geoBoundsX = StringUtil.stripParentheses(trimSubsetX.trim()).split(":");
            String geoLowerBoundX = geoBoundsX[0];
            String geoUpperBoundX = geoBoundsX[1];

            // e.g. (40:50)
            String[] geoBoundsY = StringUtil.stripParentheses(trimSubsetY.trim()).split(":");
            String geoLowerBoundY = geoBoundsY[0];
            String geoUpperBoundY = geoBoundsY[1];

            geoXYSubsets.add(new WcpsTrimSubsetDimension(axisLabelX, null, geoLowerBoundX, geoUpperBoundX));
            geoXYSubsets.add(new WcpsTrimSubsetDimension(axisLabelY, null, geoLowerBoundY, geoUpperBoundY));
            
        }
    }

    public List<WcpsTrimSubsetDimension> getGeoXYSubsets() {
        return geoXYSubsets;
    }
    
}
