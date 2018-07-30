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
package petascope.wcps.encodeparameters.service;


import java.math.BigDecimal;
import java.util.List;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.core.BoundingBox;

/**
 *
 * Given coverage metadata, this utility class will extract the bounding box in xmin,ymin,xmax,ymax from metadata.
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class BoundingBoxExtractorService {
    
    /**
     * Create a bbox object from WCPS coverage metadata
     * @param metadata
     * @return 
     */
    public BoundingBox extract(WcpsCoverageMetadata metadata) {
        BigDecimal xMin = null, yMin = null, xMax = null, yMax = null;
        
        List<Axis> xyAxes = metadata.getXYAxes();
        xMin = xyAxes.get(0).getGeoBounds().getLowerLimit();
        yMin = xyAxes.get(1).getGeoBounds().getLowerLimit();
        xMax = xyAxes.get(0).getGeoBounds().getUpperLimit();
        yMax = xyAxes.get(1).getGeoBounds().getUpperLimit();

        return this.bbox = new BoundingBox(xMin, yMin, xMax, yMax);
    }
    
    public BoundingBox getBbox() {
        return this.bbox;
    }
    
    private BoundingBox bbox;
}
