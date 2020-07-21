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
package petascope.wms.handlers.model;

import petascope.core.BoundingBox;

/**
 * Model class contains some basic properties for a WMS layer
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class WMSLayer {
    
   private String layerName;
   // original min and max XY geo bounds of a layer
   private BoundingBox originalXYBoundsBBox;
   // min and max XY geo bounds of a requesting BBOX from client
   private BoundingBox requestBBox;
   // extended min and max XY geo bounds from the requesting BBOX from client
   private BoundingBox extendedRequestBBox;
   private final Integer width;
   private final Integer height;

    public WMSLayer(String layerName, BoundingBox originalXYBoundsBBox, BoundingBox requestBBox, BoundingBox extendedRequestBBox, Integer width, Integer height) {
        this.layerName = layerName;
        this.originalXYBoundsBBox = originalXYBoundsBBox;
        this.requestBBox = requestBBox;
        this.extendedRequestBBox = extendedRequestBBox;
        this.width = width;
        this.height = height;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getLayerName() {
        return layerName;
    }

    public BoundingBox getRequestBBox() {
        return requestBBox;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setRequestBBbox(BoundingBox requestBBox) {
        this.requestBBox = requestBBox;
    }

    public void setExtendedRequestBBox(BoundingBox extendedRequestBBox) {
        this.extendedRequestBBox = extendedRequestBBox;
    }

    public BoundingBox getOriginalBoundsBBox() {
        return originalXYBoundsBBox;
    }

    public BoundingBox getExtendedRequestBBox() {
        return extendedRequestBBox;
    }
    
}
