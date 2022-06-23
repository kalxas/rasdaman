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
package petascope.wmts.handlers.kvp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.AbstractController;
import petascope.controller.PetascopeController;
import petascope.core.KVPSymbols;
import petascope.core.Pair;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WMSException;
import org.rasdaman.domain.wmts.TileMatrix;
import org.rasdaman.domain.wmts.TileMatrixSet;
import org.rasdaman.repository.service.WMTSRepositoryService;
import petascope.wms.handlers.service.WMSGetMapCachingService;
import petascope.wmts.handlers.service.WMTSGetCapabilitiesService;
import petascope.wmts.handlers.service.WMTSGetTileService;

/**
 * Handler for WMTS GetTile service
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class KVPWMTSGetTileHandler extends KVPWMTSAbstractHandler {
    
    private static Logger log = LoggerFactory.getLogger(KVPWMTSGetTileHandler.class);
    
    @Autowired
    private PetascopeController petascopeController;    
    @Autowired
    private HttpServletRequest httpServletRequest;
    
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;    

    @Autowired
    private WMTSRepositoryService wmtsRepositoryService;
    
    @Autowired
    private WMTSGetCapabilitiesService wmtsGetCapabilitiesService;
    @Autowired
    private WMTSGetTileService wmtsGetTileService;

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException, Exception {
        
        this.validate(kvpParameters);
        
        Map<String, String[]> kvpMap = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : kvpParameters.entrySet()) {
            if (!(entry.getKey().equalsIgnoreCase(KVPSymbols.KEY_SERVICE)
                || entry.getKey().equalsIgnoreCase(KVPSymbols.KEY_REQUEST)
                || entry.getKey().equalsIgnoreCase(KVPSymbols.KEY_VERSION))) {
                kvpMap.put(entry.getKey(), entry.getValue());
            }
        }
        
        String layerName = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMTS_LAYER);
        kvpMap.remove(KVPSymbols.KEY_WMTS_LAYER.toLowerCase());
        
        String styleName = AbstractController.getValueByKeyAllowNull(kvpParameters, KVPSymbols.KEY_WMTS_STYLE);
        if (styleName != null && styleName.equals(WMTSGetCapabilitiesService.STYLE_DEFAULT)) {
            // WMS GetMap allow style=null
            styleName = null;
        }
        kvpMap.remove(KVPSymbols.KEY_WMTS_STYLE.toLowerCase());
        
        String format = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMTS_FORMAT).toLowerCase();
        kvpMap.remove(KVPSymbols.KEY_WMTS_FORMAT.toLowerCase());
        
        // e.g. test_wms:EPSG:4326 (base layer in EPSG:4326)
        String tileMatrixSetName = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMTS_TILE_MATRIX_SET);
        kvpMap.remove(KVPSymbols.KEY_WMTS_TILE_MATRIX_SET.toLowerCase());
        
        // e.g. test_wms_2 (pyramid member)
        String tileMatrixName = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMTS_TILE_MATRIX);
        kvpMap.remove(KVPSymbols.KEY_WMTS_TILE_MATRIX.toLowerCase());
        
        int tileRowIndex = Integer.parseInt(AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMTS_TILE_ROW));
        kvpMap.remove(KVPSymbols.KEY_WMTS_TILE_ROW.toLowerCase());
        
        int tileColIndex = Integer.parseInt(AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMTS_TILE_COL));
        kvpMap.remove(KVPSymbols.KEY_WMTS_TILE_COL.toLowerCase());
        
        // optinal dimension (non-XY axes)
        // e.g. (Time,2015-01-01) or (isobaric,200)
        List<Pair<String, String>> nonXYAxisPairs = new ArrayList<>();
        
        String timeValue = AbstractController.getValueByKeyAllowNull(kvpParameters, KVPSymbols.KEY_WMS_TIME);
        nonXYAxisPairs.add(new Pair<>(KVPSymbols.KEY_WMS_TIME, timeValue));
        kvpMap.remove(KVPSymbols.KEY_WMS_TIME);
        
        String elevationValue = AbstractController.getValueByKeyAllowNull(kvpParameters, KVPSymbols.KEY_WMS_ELEVATION);
        nonXYAxisPairs.add(new Pair<>(KVPSymbols.KEY_WMS_ELEVATION, elevationValue));
        kvpMap.remove(KVPSymbols.KEY_WMS_ELEVATION);
        
        for (Map.Entry<String, String[]> entry : kvpMap.entrySet()) {
            // e.g isobaric axis (NOTE: in WMS GetMap it is called dim_isobaric with dim_ prefix)
            String key = KVPSymbols.KEY_WMS_DIM_PREFIX + entry.getKey();
            String value = AbstractController.getValueByKey(kvpMap, key);
            nonXYAxisPairs.add(new Pair<>(key, value));
        }
        
        TileMatrixSet tileMatrixSet = this.wmtsRepositoryService.getTileMatrixSetFromCaches(tileMatrixSetName);
        
        TileMatrix tileMatrix = tileMatrixSet.getTileMatrixMap().get(tileMatrixName);
        
        if (tileMatrix == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "TileMatrix: " + tileMatrixName + " does not exist.");
        }
        
        Response result = this.wmtsGetTileService.handle(layerName, styleName,
                                                        tileMatrix,
                                                        tileRowIndex, tileColIndex, nonXYAxisPairs, format);
        
        return result;
    }

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        String layerName = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMTS_LAYER);
        if (!this.wmsRepostioryService.isInCache(layerName)) {
            throw new PetascopeException(ExceptionCode.NoSuchLayer, KVPSymbols.KEY_WMTS_LAYER + ": " + layerName + " does not exist.");
        }
        
        Layer layer = this.wmsRepostioryService.readLayerByName(layerName);
        
        String styleName = AbstractController.getValueByKeyAllowNull(kvpParameters, KVPSymbols.KEY_WMTS_STYLE);
        if (styleName != null && !styleName.isEmpty() && !styleName.equals(WMTSGetCapabilitiesService.STYLE_DEFAULT) && !layer.hasStyle(styleName)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                         KVPSymbols.KEY_WMTS_STYLE + ": " + styleName + " does not exist in " + KVPSymbols.KEY_WMTS_LAYER + ": " + layerName);
        }
        
        String format = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMTS_FORMAT).toLowerCase();
        if (!WMTSGetCapabilitiesService.SUPPORTED_ENCODE_FORMATS.contains(format)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, KVPSymbols.KEY_WMTS_FORMAT+ ": " + format + " is not supported.");
        }
        
        String tileMatrixSetName = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMTS_TILE_MATRIX_SET);
        String tileMatrixName = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMTS_TILE_MATRIX);
        
        Set<String> localUpdateLayerNames = new LinkedHashSet<>();
        if (this.wmtsGetCapabilitiesService.localUpdatedLayerNames.contains(layerName)) {
            localUpdateLayerNames.add(layerName);
        }
        
        // rebuild cache of TileMatrixSet for the requesting layer if needed
        this.wmtsRepositoryService.updateLocalTileMatrixSetsMapCache(localUpdateLayerNames);
        // remove this updated layer from the list
        this.wmtsGetCapabilitiesService.localUpdatedLayerNames.remove(layerName);
        this.wmsGetMapCachingService.removeLayerGetMapInCache(layerName);
        
        TileMatrixSet tileMatrixSet = this.wmtsRepositoryService.getTileMatrixSetFromCaches(tileMatrixSetName);
        if (tileMatrixSet == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                        KVPSymbols.KEY_WMTS_TILE_MATRIX_SET + ": " + tileMatrixSetName + " does not exist.");
        }
        
        TileMatrix tileMatrix = tileMatrixSet.getTileMatrixMap().get(tileMatrixName);
        if (tileMatrix == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest,
                                        KVPSymbols.KEY_WMTS_TILE_MATRIX + ": " + tileMatrixName + " does not exist in "
                                      + KVPSymbols.KEY_WMTS_TILE_MATRIX_SET + ": "  + tileMatrixSetName);
        }
        
        // i index
        String tileRowTmp = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMTS_TILE_ROW);
        int tileRow = 0;
        try {
            tileRow = Integer.parseInt(tileRowTmp);
            if (tileRow < 0) {
                throw new RuntimeException();
            }
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, KVPSymbols.KEY_WMTS_TILE_ROW + ": " + tileRowTmp + " must be non negative integer.");
        }
        
        if (tileRow > tileMatrix.getMatrixWidth() - 1) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, KVPSymbols.KEY_WMTS_TILE_ROW + ": " + tileRow + " must be non negative integer and less or equal than " + (tileMatrix.getMatrixWidth() - 1));
        }
        
        // j index
        String tileColTmp = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMTS_TILE_COL);
        int tileCol = 0;
        try {
            tileCol = Integer.parseInt(tileRowTmp);
            if (tileCol < 0) {
                throw new RuntimeException();
            }
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, KVPSymbols.KEY_WMTS_TILE_COL + ": " + tileColTmp + " must be non negative integer.");
        }
        
        if (tileCol > tileMatrix.getMatrixHeight() - 1) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, KVPSymbols.KEY_WMTS_TILE_COL + ": " + tileCol + " must be non negative integer and less or equal than " + (tileMatrix.getMatrixHeight() - 1));
        }
    }
    
}
