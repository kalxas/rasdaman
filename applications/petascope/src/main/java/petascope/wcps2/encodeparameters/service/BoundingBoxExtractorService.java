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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/
package petascope.wcps2.encodeparameters.service;


import java.math.BigDecimal;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.AxisDirection;
import petascope.wcps2.metadata.model.NumericTrimming;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.encodeparameters.model.BoundingBox;

/**
 *
 * Given coverage metadata, this utility class will extract the bounding box in xmin,ymin,xmax,ymax from metadata.
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class BoundingBoxExtractorService {
    
    /**
     * Create a bbox object from WCPS coverage metadata
     * @param metadata
     * @return 
     */
    public BoundingBox extract(WcpsCoverageMetadata metadata) {
        BigDecimal xMin = null, yMin = null, xMax = null, yMax = null;

        int i = 0;

        // Get the calculated bounding box from the coverage
        for (Axis axis : metadata.getAxes()) {
            if (axis.getGeoBounds() instanceof NumericTrimming) {
                if (axis.getDirection().equals(AxisDirection.EASTING)) {
                    xMin = ((NumericTrimming)axis.getGeoBounds()).getLowerLimit();
                    xMax = ((NumericTrimming)axis.getGeoBounds()).getUpperLimit();
                }
                if (axis.getDirection().equals(AxisDirection.NORTHING)) {
                    yMin = ((NumericTrimming)axis.getGeoBounds()).getLowerLimit();
                    yMax = ((NumericTrimming)axis.getGeoBounds()).getUpperLimit();
                }
                i++;
                if (i == 2) {
                    break;
                }
            }
        }       

        return this.bbox = new BoundingBox(xMin, yMin, xMax, yMax);
    }
    
    public BoundingBox getBbox() {
        return this.bbox;
    }
    
    private BoundingBox bbox;
}
