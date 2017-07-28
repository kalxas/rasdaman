/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core;

import java.math.BigDecimal;

/**
 * A coverage's extent with coverageId and BoundingBox
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class CoverageExtent implements Comparable<CoverageExtent> {
    private String coverageId;
    private BoundingBox bbox;
    
    public CoverageExtent() {

    }

    public CoverageExtent(String coverageId, BoundingBox bbox) {
        this.coverageId = coverageId;
        this.bbox = bbox;
    }
    
    /**
     * NOTE: Always return the coverage with bigger bounding box first and smaller bounding box after.
     * Because, Openlayers will load the extents in this order and allow to hover on smaller vector polygon and get the extent metadata.
     * If not, Openlayers will always return the bigger coverage's extent metadata despite of smaller coverages inside the big one.
     * @return 
     */    
    private BigDecimal getBoundingBoxArea() {
        BigDecimal xLength = bbox.getXMax().subtract(bbox.getXMin());
        BigDecimal yLength = bbox.getYMax().subtract(bbox.getYMin());
        BigDecimal area = xLength.multiply(yLength);
        
        return area;
    }

    public String getCoverageId() {
        return coverageId;
    }

    public void setCoverageId(String coverageId) {
        this.coverageId = coverageId;
    }

    public BoundingBox getBbox() {
        return bbox;
    }

    public void setBbox(BoundingBox bbox) {
        this.bbox = bbox;
    }
        
    @Override
    public int compareTo(CoverageExtent compare) {
        // Descending order
        return compare.getBoundingBoxArea().subtract(this.getBoundingBoxArea()).toBigInteger().intValue();
    }
}
