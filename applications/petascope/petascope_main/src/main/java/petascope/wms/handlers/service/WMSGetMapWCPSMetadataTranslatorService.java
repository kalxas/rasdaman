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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.BoundingBox;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;
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
    public WMSLayer createWMSLayer(String layerName, BoundingBox originalBoundsBBox, BoundingBox requestBBox,
                                   BoundingBox extendedRequestBBox, int width, int height) {
        WMSLayer wmsLayer = new WMSLayer(layerName, originalBoundsBBox, requestBBox, extendedRequestBBox, width, height);
        return wmsLayer;
    }
    
    /**
     * Translate a WMS layer name to a WCPS coverage metadata object
     */
    public WcpsCoverageMetadata translate(String layerName) throws PetascopeException, SecoreException {
        WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(layerName);
        return wcpsCoverageMetadata;
    }
    
    /**
     * Create a WCPS Coverage metadata object based on layerName (coverageId) and original XY bounding box of coverage's XY axes which fits on a rasdaman downscaled collection.
     */
    public WcpsCoverageMetadata createWcpsCoverageMetadataForDownscaledLevelByOriginalXYBBox(WMSLayer wmsLayer) throws PetascopeException {
        WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(wmsLayer.getLayerName());
        Pair<BigDecimal, BigDecimal> geoSubsetX = new Pair(wmsLayer.getOriginalBoundsBBox().getXMin(), wmsLayer.getOriginalBoundsBBox().getXMax());
        Pair<BigDecimal, BigDecimal> geoSubsetY = new Pair(wmsLayer.getOriginalBoundsBBox().getYMin(), wmsLayer.getOriginalBoundsBBox().getYMax());
        wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.createForDownscaledLevelByGeoXYSubsets(wcpsCoverageMetadata, 
                                                                                                     geoSubsetX, geoSubsetY, 
                                                                                                     wmsLayer.getWidth(), wmsLayer.getHeight());
        
        return wcpsCoverageMetadata;
    }
    
        
    /**
     * Create a WCPS Coverage metadata object based on layerName (coverageId) and input extended BBOX on XY axes which fits on a rasdaman downscaled collection.
     */
    public WcpsCoverageMetadata createWcpsCoverageMetadataForDownscaledLevelByExtendedRequestBBox(WMSLayer wmsLayer) throws PetascopeException, SecoreException {
        WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(wmsLayer.getLayerName());
        Pair<BigDecimal, BigDecimal> geoSubsetX = new Pair(wmsLayer.getExtendedRequestBBox().getXMin(), wmsLayer.getExtendedRequestBBox().getXMax());
        Pair<BigDecimal, BigDecimal> geoSubsetY = new Pair(wmsLayer.getExtendedRequestBBox().getYMin(), wmsLayer.getExtendedRequestBBox().getYMax());
        wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.createForDownscaledLevelByGeoXYSubsets(wcpsCoverageMetadata, 
                                                                                                     geoSubsetX, geoSubsetY, 
                                                                                                     wmsLayer.getWidth(), wmsLayer.getHeight());
        
        return wcpsCoverageMetadata;
    }
}
