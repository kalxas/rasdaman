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
package petascope.wmts.handlers.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.KVPSymbols;
import petascope.core.Pair;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WMSException;
import petascope.util.CrsUtil;
import petascope.util.StringUtil;
import petascope.wms.handlers.kvp.KVPWMSGetMapHandler;
import petascope.wmts.handlers.model.TileMatrix;

/**
 * Service to handle WMTS GetTile request
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class WMTSGetTileService {
    
    @Autowired
    private KVPWMSGetMapHandler kvpWMSGetMapHandler;
    
    public Response handle(String layerName, String styleName,
                           TileMatrix tileMatrix,
                           int tileRowIndex, int tileColIndex, List<Pair<String, String>> nonXYAxisPairs, String format) throws PetascopeException, WMSException, Exception {
        
        // NOTE: convert WMTS request to WMS request to handle
        Map<String, String[]> kvpParameters = new LinkedHashMap<>();
        
        // layers
        kvpParameters.put(KVPSymbols.KEY_WMS_LAYERS.toLowerCase(), new String[] { layerName });
        
        // NOTE: TileMatrix (pyramid member coverage Id)
        String tileMatrixName = tileMatrix.getName();
        // This internal key is used for GetMap request handler to handle the request on a specific pyramid member in GetTile request properly
        kvpParameters.put(KVPSymbols.KEY_WMTS_RASDAMAN_INTERNAL_FOR_GETMAP_REQUEST_PYRAMID_COVERAGE_ID.toLowerCase(), new String[] { tileMatrixName } );
        
        // styles
        if (styleName != null) {
            kvpParameters.put(KVPSymbols.KEY_WMS_STYLES.toLowerCase(), new String[] { styleName });
        } else {
            kvpParameters.put(KVPSymbols.KEY_WMS_STYLES.toLowerCase(), new String[] { "" });
        }
        
        // crs
        kvpParameters.put(KVPSymbols.KEY_WMS_CRS.toLowerCase(), new String[] { tileMatrix.getEpsgCode() });
        
        // bbox
        String bbox = this.buildBBoxValue(tileMatrix, tileRowIndex, tileColIndex);
        kvpParameters.put(KVPSymbols.KEY_WMS_BBOX.toLowerCase(), new String[] { bbox });
        
        // width
        kvpParameters.put(KVPSymbols.KEY_WMS_WIDTH.toLowerCase(), new String[] { String.valueOf(tileMatrix.getTileWidth()) });
        
        // height
        kvpParameters.put(KVPSymbols.KEY_WMS_HEIGHT.toLowerCase(), new String[] { String.valueOf(tileMatrix.getTileHeight()) });
        
        // format
        kvpParameters.put(KVPSymbols.KEY_WMS_FORMAT.toLowerCase(), new String[] { String.valueOf(format) });
        
        // optional non-XY axes
        for (Pair<String, String> nonXYAxisPair : nonXYAxisPairs) {
            // e.g. Time, Elevation or e.g. isobaric
            String key = nonXYAxisPair.fst;
            String value = nonXYAxisPair.snd;
            
            if (value != null) {
                kvpParameters.put(key, new String[] { value });
            }
        }
        
        // create transparent tile with null pixel values
        kvpParameters.put(KVPSymbols.KEY_WMS_TRANSPARENT.toLowerCase(), new String[] { Boolean.TRUE.toString() } );
        
        Response result = this.kvpWMSGetMapHandler.handle(kvpParameters);
        return result;
    }
    
    /**
     * e.g. A pyramid member (TileMatrix) is test_wms_2, and the request with i,j indices are 10,20
     * then, return a corresponding WMS GetMap bbox in the CRS of the TileMatrix for these indices.
     */
    private String buildBBoxValue(TileMatrix tileMatrix, int tileRowIndex, int tileColIndex) throws PetascopeException {
        // e.g. -180
        BigDecimal geoOriginalLowerBoundX = tileMatrix.getGeoLowerBoundX();
        // e.g. 180
        BigDecimal geoOriginalUpperBoundX = tileMatrix.getGeoUpperBoundX();

        // e.g. -90
        BigDecimal geoOriginalLowerBoundY = tileMatrix.getGeoLowerBoundY();        
        // e.g. 90
        BigDecimal geoOriginalUpperBoundY = tileMatrix.getGeoUpperBoundY();
        
        // e.g. 10
        BigDecimal geoResolutionX = tileMatrix.getGeoResolutionX();
        // e.g. -10
        BigDecimal geoResolutionY = tileMatrix.getGeoResolutionY();
        
        // e.g. EPSG:4326
        String epsgCode = tileMatrix.getEpsgCode();
        
        // X axis with positive geo resolution (tileCol)
        
        BigDecimal geoDistanceX = (BigDecimal.valueOf(tileColIndex).multiply(BigDecimal.valueOf(tileMatrix.getTileWidth()))).multiply(geoResolutionX);
        // leftX = tileCol * tileSpanX + tileMatrixMinX
        BigDecimal geoSubsetLowerBoundX = geoOriginalLowerBoundX.add(geoDistanceX);
        // rightX = (tileCol+1) * tileSpanX + tileMatrixMinX
        BigDecimal geoSubsetUpperBoundX = geoSubsetLowerBoundX.add(BigDecimal.valueOf(tileMatrix.getTileWidth()).multiply(geoResolutionX));
        
        // Y axis with negative geo resolution (tileRow)
        
        BigDecimal geoDistanceY = (BigDecimal.valueOf(tileRowIndex).multiply(BigDecimal.valueOf(tileMatrix.getTileHeight()))).multiply(geoResolutionY);
        // upperY = tileMatrixMaxY - tileRow * tileSpanY
        BigDecimal geoSubsetUpperBoundY = geoOriginalUpperBoundY.add(geoDistanceY);
        // lowerY = tileMatrixMaxY – (tileRow+1) * tileSpanY
        BigDecimal geoSubsetLowerBoundY = geoSubsetUpperBoundY.add(BigDecimal.valueOf(tileMatrix.getTileHeight()).multiply(geoResolutionY));
        
        // NOTE: Normally this value is 256, in case a TileMatrix has small grid domains, then this value is smaller
        long tileWidth = tileMatrix.getTileWidth();
        long tileHeight = tileMatrix.getTileHeight();
        
        if (tileWidth < TileMatrix.GRID_SIZE) {
            // e.g. 36 pixels
            geoSubsetLowerBoundX = geoOriginalLowerBoundX;
            geoSubsetUpperBoundX = geoOriginalUpperBoundX;
        }
        
        if (tileHeight < tileMatrix.GRID_SIZE) {
            // e.g. 18 pixels
            geoSubsetLowerBoundY = geoOriginalLowerBoundY;
            geoSubsetUpperBoundY = geoOriginalUpperBoundY;
        }        
        
        boolean isXYAxisOrder = CrsUtil.isXYAxesOrder(epsgCode);
        String result;
        if (isXYAxisOrder) {
            // e.g. EPSG:32632
            result = geoSubsetLowerBoundX.toPlainString() + "," + geoSubsetLowerBoundY.toPlainString() + "," 
                   + geoSubsetUpperBoundX.toPlainString() + "," + geoSubsetUpperBoundY.toPlainString();
        } else {
            // e.g. EPSG:4326
            result = geoSubsetLowerBoundY.toPlainString() + "," + geoSubsetLowerBoundX.toPlainString() + "," 
                   + geoSubsetUpperBoundY.toPlainString() + "," + geoSubsetUpperBoundX.toPlainString();
        }
        

        return result;
    }
}
