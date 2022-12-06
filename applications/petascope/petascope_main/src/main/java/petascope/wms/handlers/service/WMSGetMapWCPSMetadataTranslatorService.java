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
package petascope.wms.handlers.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.BoundingBox;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wms.handlers.model.WMSLayer;

/**
 * Use WCPS handlers to handle WMS GetMap request
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class WMSGetMapWCPSMetadataTranslatorService {
   
    @Autowired
    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslator;
    
    /**
     * Create a WMSLayer from input parameters of GetMap request
     */
    public WMSLayer createWMSLayer(String layerName, BoundingBox originalXYBoundsBBox, BoundingBox requestBBox,
                                   BoundingBox extendedRequestBBox, int width, int height
                                   , List<WcpsSubsetDimension> subsetDimensions
                                   , String wmtsTileMatrixName) {
        WMSLayer wmsLayer = new WMSLayer(layerName, originalXYBoundsBBox, requestBBox, extendedRequestBBox, width, height,
                                        subsetDimensions
                                        , wmtsTileMatrixName
                                        ); 
        return wmsLayer;
    }
    
    /**
     * Translate a WMS layer name to a WCPS coverage metadata object
     */
    public WcpsCoverageMetadata translate(String layerName) throws PetascopeException {
        WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(layerName);
        return wcpsCoverageMetadata;
    }
        
    /**
     * Create a WCPS Coverage metadata object based on layerName (coverageId) and input extended BBOX on XY axes which fits on a rasdaman downscaled collection.
     */
    public WcpsCoverageMetadata createWcpsCoverageMetadataForDownscaledLevelByExtendedRequestBBox(WMSLayer wmsLayer) throws PetascopeException {
        WcpsCoverageMetadata wcpsCoverageMetadata = wmsLayer.getWcpsCoverageMetadata();
        
        String tileMatrixName = wmsLayer.getWMTSTileMatrixName();
        
        if (tileMatrixName == null) {
            if (wcpsCoverageMetadata == null) {
                wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(wmsLayer.getLayerName());  
            } 
             
            Pair<BigDecimal, BigDecimal> geoSubsetX = new Pair(wmsLayer.getExtendedRequestBBox().getXMin(), wmsLayer.getExtendedRequestBBox().getXMax());
            Pair<BigDecimal, BigDecimal> geoSubsetY = new Pair(wmsLayer.getExtendedRequestBBox().getYMin(), wmsLayer.getExtendedRequestBBox().getYMax());

            wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.createForDownscaledLevelByGeoXYSubsets(wcpsCoverageMetadata, 
                                                                                                         geoSubsetX, geoSubsetY, 
                                                                                                         wmsLayer.getWidth(), wmsLayer.getHeight(),
                                                                                                         wmsLayer.getNonXYSubsetDimensions());
        } else {
            wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(tileMatrixName);
        }
        
        wmsLayer.setWcpsCoverageMetadata(wcpsCoverageMetadata);        
        return wcpsCoverageMetadata;
    }
}
