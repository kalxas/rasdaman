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

import java.util.List;
import java.util.Map;
import petascope.core.BoundingBox;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;

/**
 * Model class contains some basic properties for a WMS layer
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class WMSLayer {
    
   private String layerName;
   // NOTE: used only when WMS GetMap request is generated from WMTS GetTile request
   private String wmtsTileMatrixName;
   
   // original min and max XY geo bounds of a layer (e.g: in UTM 32)
   private BoundingBox originalXYBoundsBBox;
   // min and max XY geo bounds of a requesting BBOX from client (e.g: EPSG:4326)
   private BoundingBox requestBBox;
   // extended min and max XY geo bounds from the requesting BBOX from client
   private BoundingBox extendedRequestBBox;
   private final Integer width;
   private final Integer height;

   // List of WCPS subsets on nonXY axes (e.g. time / elevation,...)
   private List<WcpsSubsetDimension> nonXYSubsetDimensions;
   private WcpsCoverageMetadata wcpsCoverageMetadata;
   

    public WMSLayer(String layerName, BoundingBox originalXYBoundsBBox, BoundingBox requestBBox, BoundingBox extendedRequestBBox, Integer width, Integer height,
                    List<WcpsSubsetDimension> nonXYSubsetDimensions
                    , String wmtsTileMatrixName
                    ) {
        this.layerName = layerName;
        this.originalXYBoundsBBox = originalXYBoundsBBox;
        this.requestBBox = requestBBox;
        this.extendedRequestBBox = extendedRequestBBox;
        this.width = width;
        this.height = height;
        this.nonXYSubsetDimensions = nonXYSubsetDimensions;

        this.wmtsTileMatrixName = wmtsTileMatrixName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getLayerName() {
        return layerName;
    }

    public String getWMTSTileMatrixName() {
        return wmtsTileMatrixName;
    }

    public void setWMTSTileMatrixName(String wmtsTileMatrixName) {
        this.wmtsTileMatrixName = wmtsTileMatrixName;
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

    public List<WcpsSubsetDimension> getNonXYSubsetDimensions() {
        return nonXYSubsetDimensions;
    }


    public WcpsCoverageMetadata getWcpsCoverageMetadata() {
        return wcpsCoverageMetadata;
    }

    public void setWcpsCoverageMetadata(WcpsCoverageMetadata wcpsCoverageMetadata) {
        this.wcpsCoverageMetadata = wcpsCoverageMetadata;
    }

    @Override
    public String toString() {
        return this.layerName;
    }
}
