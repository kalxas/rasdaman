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
package org.rasdaman.repository.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.CoveragePyramid;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.IndexAxis;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.domain.wmts.TileMatrix;
import org.rasdaman.domain.wmts.TileMatrixSet;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.GeoTransform;
import petascope.core.Pair;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsProjectionUtil;
import petascope.util.CrsUtil;

/**
 * Service to handle WMTS repository
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class WMTSRepositoryService {
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(WMTSRepositoryService.class);
    
    // e.g. TileMatrixSet test_wms_4326:EPSG:4326 in local database
    private static final Map<String, TileMatrixSet> localTileMatrixSetsMapCache = new ConcurrentSkipListMap<>();
    
    public TileMatrixSet getTileMatrixSetFromCaches(String tileMatrixSetName) throws PetascopeException {
        TileMatrixSet tileMatrixSet = this.localTileMatrixSetsMapCache.get(tileMatrixSetName);
        
        return tileMatrixSet;
    }
    
    
    /**
     * Build TileMatrixSets caches correspondingly
     * - If cache is empty -> initialize everything
     * - If cache is not empty, then use the local updated layers to update cache
     */
    public void buildTileMatrixSetsMapCaches(Set<String> localUpdatedLayerNames) throws PetascopeException {
        // Reload new caches for WMTS GetCapabilities request
        if (this.localTileMatrixSetsMapCache.isEmpty()) {
            this.initializeLocalTileMatrixSetsMapCache();
        } else {
            this.updateLocalTileMatrixSetsMapCache(localUpdatedLayerNames);
        }
    }
    
    
    
    /**
     * Initialize the local TileMatrixSets cache from the list of local WMS layers
     */
    public void initializeLocalTileMatrixSetsMapCache() throws PetascopeException {
        this.localTileMatrixSetsMapCache.clear();
        
        for (Layer localLayer : wmsRepostioryService.readAllLocalLayers()) {
            String layerName = localLayer.getName();
            String epsgCode = CrsUtil.getAuthorityCode(localLayer.getGeoXYCRS());
            
            List<TileMatrixSet> tileMatrixSetsList = this.buildTileMatrixSetsList(layerName, epsgCode);
            for (TileMatrixSet tileMatrixSet : tileMatrixSetsList) {
                this.localTileMatrixSetsMapCache.put(tileMatrixSet.getName(), tileMatrixSet);
            }
        }
    }
    
    /**
     * Given a list of local WMS layers, then update the local tile matrix sets map
       only update TileMatrixSets for the local input updated layers
     */
    public void updateLocalTileMatrixSetsMapCache(Set<String> localUpdatedLayerNames) throws PetascopeException {
        for (String layerName : localUpdatedLayerNames) {
            if (this.wmsRepostioryService.isInLocalCache(layerName)) {
                Layer layer = this.wmsRepostioryService.readLayerByNameFromLocalCache(layerName);
                String epsgCode = CrsUtil.getAuthorityCode(layer.getGeoXYCRS());

                // e.g. test_wms:EPSG:4326 and test_wms:EPSG:32632 (layer native CRS is EPSG:32632)
                List<TileMatrixSet> tileMatrixSetsList = this.buildTileMatrixSetsList(layerName, epsgCode);
                for (TileMatrixSet tileMatrixSet : tileMatrixSetsList) {
                    this.localTileMatrixSetsMapCache.put(tileMatrixSet.getName(), tileMatrixSet);
                }
            }
        }
    }
    
    
    /**
     * Remove a TileMatrixSet from the map of TileMatrixSets cache
     * Used when deleting a base coverage (deactivate a WMS layer)
     */
    public void removeTileMatrixSetFromLocalCache(String layerName, String epsgCode) {
        // the default TileMatrixSet is EPSG:4326
        String tileMatrixSetName = this.getTileMatrixSetName(layerName, epsgCode);
        this.localTileMatrixSetsMapCache.remove(tileMatrixSetName);
        
        if (!epsgCode.equals(CrsUtil.EPSG_4326_AUTHORITY_CODE)) {
            // e.g. if layer has EPSG:32632
            tileMatrixSetName = this.getTileMatrixSetName(layerName, epsgCode);
            this.localTileMatrixSetsMapCache.remove(tileMatrixSetName);
        }
    }
    
    /**
     * Remove a TileMatrix from a TileMatrixSet from TileMatrixSets cache
     * Used when deleting or removing a pyramid member coverage
     */
    public void removeTileMatrixFromLocalCache(String baseLayerName, String pyramidMemberLayerName, String epsgCode) {
        String tileMatrixSetName = this.getTileMatrixSetName(baseLayerName, epsgCode);
        TileMatrixSet tileMatrixSet = this.localTileMatrixSetsMapCache.get(tileMatrixSetName);
        
        if (tileMatrixSet != null) {
            tileMatrixSet.getTileMatrixMap().remove(pyramidMemberLayerName);
        }
        
        if (!epsgCode.equals(CrsUtil.EPSG_4326_AUTHORITY_CODE)) {
            // e.g. if layer has EPSG:32632
            tileMatrixSet = this.localTileMatrixSetsMapCache.get(tileMatrixSetName);

            if (tileMatrixSet != null) {
                tileMatrixSet.getTileMatrixMap().remove(pyramidMemberLayerName);
            }
        }
    }
    
    
    /** 
     * e.g. test_layer_1:EPSG:4326, test_layer_2:EPSG:4326
     */
    public List<String> getListTileMatrixSetNames(String layerName) throws PetascopeException {
        Coverage coverage = this.coverageRepositoryService.readCoverageBasicMetadataByIdFromCache(layerName);
        // e.g. EPSG:4326
        String geoXYCRS = coverage.getEnvelope().getEnvelopeByAxis().getGeoXYCrs();
        String epsgCode = CrsUtil.getAuthorityCode(geoXYCRS);
        
        List<String> results = new ArrayList<>();
        String tileMatrixSetName = this.getTileMatrixSetName(layerName, epsgCode);
        results.add(tileMatrixSetName);
        
        if (!epsgCode.equals(CrsUtil.EPSG_4326_AUTHORITY_CODE)) {
            // NOTE: If a layer's native CRS is different
            // from EPSG:4326 -> it has another TileMatrixSet in this CRS, besides the default TileMatrixSet in EPSG:4326
            results.add(this.getTileMatrixSetName(layerName, CrsUtil.EPSG_4326_AUTHORITY_CODE));
        }
        
        return results;
    }
    
    /**
     * e.g. test_layer:EPSG:4326
     */
    public String getTileMatrixSetName(String layerName, String epsgCode) {
        return layerName + ":" + epsgCode;
    }
    
    /**
     *  e.g. test_wms:EPSG:4326 -> 4326
     */
    public String getCodeFromTileMatrixSetName(String tileMatrixSetName) {
        return tileMatrixSetName.substring(tileMatrixSetName.lastIndexOf(":") + 1);
    }
    
    /**
     * Given a base layer name and EPSG code (e.g. EPSG:4326) -> create a list of TileMatrixSet with a map of TileMatrix
     * NOTE: if epsgCode is different from EPSG:4326, e.g. EPSG:32632
     * -> this layer has two TileMatrixSet (one is in EPSG:4326 (default CRS)) and one is in EPSG:32632
     */
    private List<TileMatrixSet> buildTileMatrixSetsList(String baseLayerName, String inputEpgsCode) throws PetascopeException {
        Coverage baseCoverage = null;
        try {
            baseCoverage = this.coverageRepositoryService.readCoverageBasicMetadataByIdFromCache(baseLayerName);
        } catch(PetascopeException ex) {
            if (ex.getExceptionCode().getExceptionCodeName().equalsIgnoreCase(ExceptionCode.NoSuchCoverage.getExceptionCodeName())) {
                log.warn("Coverage: " + baseLayerName + " does not exist.");
            } else {
                throw ex;
            }
        }
        
        List<TileMatrixSet> results = new ArrayList<>();
        
        if (baseCoverage != null) {
            List<CoveragePyramid> coveragePyramids = new ArrayList<>();

            // base coverage
            coveragePyramids.add(new CoveragePyramid(baseLayerName, new ArrayList<String>(), false));

            // other pyramid member coverages
            for (CoveragePyramid coveragePyramid : baseCoverage.getPyramid()) {
                coveragePyramids.add(coveragePyramid);
            }

            List<String> epsgCodes = new ArrayList<>();
            epsgCodes.add(CrsUtil.EPSG_4326_AUTHORITY_CODE);
            if (!inputEpgsCode.equalsIgnoreCase(CrsUtil.EPSG_4326_AUTHORITY_CODE)) {
                // e.g. EPSG:32632
                epsgCodes.add(inputEpgsCode);
            }

            for (String epgsCode : epsgCodes) {
                Map<String, TileMatrix> tileMatrixMap = new LinkedHashMap<>();
                for (int i = coveragePyramids.size() - 1; i >= 0; i--) {
                    // NOTE: List of TileMatrices is reversed order (lower grid domains -> higher grid domains)
                    // e.g. 60m, 20m, 10m
                    CoveragePyramid coveragePyramid = coveragePyramids.get(i);
                    String pyramidMemberCoverageId = coveragePyramid.getPyramidMemberCoverageId();

                    // e.g test_wms_2 (pyramid member)
                    String tileMatrixName = coveragePyramid.getPyramidMemberCoverageId();

                    TileMatrix tileMatrix = this.buildTileMatrix(pyramidMemberCoverageId, epgsCode);
                    tileMatrixMap.put(tileMatrixName, tileMatrix);

                }

                String tileMatrixSetName = this.getTileMatrixSetName(baseLayerName, epgsCode);
                TileMatrixSet tileMatrixSet = new TileMatrixSet(tileMatrixSetName, tileMatrixMap);
                results.add(tileMatrixSet);
            }
        }
       
        return results;
    }
    
    
    /**
     * Build TileMatrix object (a TileMatrix contains details for a pyramid member of a base layer)
     */
    private TileMatrix buildTileMatrix(String pyramidMemberCoverageId, String tileMatrixSetEPSGCode) throws PetascopeException {
        GeneralGridCoverage pyramidMemberCoverage = (GeneralGridCoverage) this.coverageRepositoryService.readCoverageFullMetadataByIdFromCache(pyramidMemberCoverageId);
        
        Pair<GeoAxis, GeoAxis> xyAxesPair = pyramidMemberCoverage.getXYGeoAxes();
        GeoAxis geoAxisX = xyAxesPair.fst;
        GeoAxis geoAxisY = xyAxesPair.snd;
        String xyCRS = geoAxisX.getSrsName();
        
        BigDecimal geoLowerBoundX = geoAxisX.getLowerBoundNumber();
        BigDecimal geoUpperBoundX = geoAxisX.getUpperBoundNumber();
        BigDecimal geoLowerBoundY = geoAxisY.getLowerBoundNumber();
        BigDecimal geoUpperBoundY = geoAxisY.getUpperBoundNumber();
        
        BigDecimal geoResolutionX = geoAxisX.getResolution();
        BigDecimal geoResolutionY = geoAxisY.getResolution();
        
        IndexAxis indexAxisX = pyramidMemberCoverage.getIndexAxisByName(geoAxisX.getAxisLabel());
        IndexAxis indexAxisY = pyramidMemberCoverage.getIndexAxisByName(geoAxisY.getAxisLabel());
        
        long gridLowerBoundX = indexAxisX.getLowerBound();
        long gridUpperBoundX = indexAxisX.getUpperBound();
        
        long gridLowerBoundY = indexAxisY.getLowerBound();
        long gridUpperBoundY = indexAxisY.getUpperBound();
        
        long numberOfGridPixelsX = gridUpperBoundX - gridLowerBoundX + 1;
        long numberOfGridPixelsY = gridUpperBoundY - gridLowerBoundY + 1;
        
        GeoTransform geoTransform = null;
        
        // e.g. pyramid member coverage's native CRS is EPSG:32632
        String nativeEPSGCode = CrsUtil.getAuthorityCode(xyCRS);
        if (!nativeEPSGCode.equals(tileMatrixSetEPSGCode)) {
            // e.g. tileMatrixSet is in EPSG:4326 -> it needs to reproject X,Y axes from EPSG:32632 -> EPSG:4326
            geoTransform = CrsProjectionUtil.buildGeoTransform(xyAxesPair, new Pair<>(indexAxisX, indexAxisY));
            geoTransform = CrsProjectionUtil.getGeoTransformInTargetCRS(geoTransform, tileMatrixSetEPSGCode);
            
            geoLowerBoundX = geoTransform.getUpperLeftGeoX();
            geoUpperBoundX = geoTransform.getLowerRightGeoX();
            
            geoLowerBoundY = geoTransform.getLowerRightGeoY();
            geoUpperBoundY = geoTransform.getUpperLeftGeoY();
            
            geoResolutionX = geoTransform.getGeoXResolution();
            geoResolutionY = geoTransform.getGeoYResolution();
            
            gridLowerBoundX = 0;
            gridUpperBoundX = geoTransform.getGridWidth() - 1;
            
            gridLowerBoundY = 0;
            gridUpperBoundY = geoTransform.getGridHeight() - 1;
            
            numberOfGridPixelsX = geoTransform.getGridWidth();
            numberOfGridPixelsY = geoTransform.getGridHeight();
        }
        
        // scaleDenominator
        
        if (geoTransform == null) {
            geoTransform = CrsProjectionUtil.buildGeoTransform(xyAxesPair, new Pair<>(indexAxisX, indexAxisY));
            geoTransform = CrsProjectionUtil.getGeoTransformInTargetCRS(geoTransform, tileMatrixSetEPSGCode);
        }
        
        BigDecimal geoDomainX = geoTransform.getLowerRightGeoX().subtract(geoTransform.getUpperLeftGeoX());
        BigDecimal geoDomainY = geoTransform.getUpperLeftGeoY().subtract(geoTransform.getLowerRightGeoY());

        BigDecimal geoDomain = geoDomainX.compareTo(geoDomainY) < 0 ? geoDomainY : geoDomainX;
        int gridDomain = geoDomainX.compareTo(geoDomainY) < 0 ? geoTransform.getGridHeight() : geoTransform.getGridWidth();
        
        // NOTE: The formula below is from WMS 1.3.0 standard
        
        // 6378137m Earth ellipsoid (EPSG:4326) (meters)
        // degree * (6378137 * 2 * pi) : 360
        BigDecimal linearSizeInMeters = BigDecimalUtil.divide(geoDomain.multiply(new BigDecimal("6378137").multiply(new BigDecimal("2")).multiply(new BigDecimal("3.14"))), new BigDecimal("360"));
        // linearSizeInMeters : numberOfGridPixels : 0.00028m/pixel
        BigDecimal scaleDenominator = BigDecimalUtil.divide(BigDecimalUtil.divide(linearSizeInMeters, BigDecimal.valueOf(gridDomain)), new BigDecimal("0.00028"));
        
        // topLeftCorner
        
        String topLeftCorner = "";
        if (CrsUtil.isXYAxesOrder(tileMatrixSetEPSGCode)) {
            // e.g. EPSG:3857
            topLeftCorner += geoLowerBoundX.toPlainString() + " " + geoUpperBoundY.toPlainString();
        } else {
            // e.g. EPSG:4326
            topLeftCorner += geoUpperBoundY.toPlainString() + " " + geoLowerBoundX.toPlainString();
        }
        
        // matrixWidth (number of tiles per columns)
        
        long tileWidth = TileMatrix.GRID_SIZE;
        long matrixWidth = new BigDecimal(numberOfGridPixelsX).divide(new BigDecimal(TileMatrix.GRID_SIZE), RoundingMode.CEILING).longValue();
        if (numberOfGridPixelsX < TileMatrix.GRID_SIZE) {
            matrixWidth = new BigDecimal(numberOfGridPixelsX).divide(new BigDecimal(numberOfGridPixelsX), RoundingMode.CEILING).longValue();
            // e.g. 36 pixels
            tileWidth = numberOfGridPixelsX;
        }
        
        // matrixHeight (number of tiles per rows)
        
        long tileHeight = TileMatrix.GRID_SIZE;
        long matrixHeight = new BigDecimal(numberOfGridPixelsY).divide(new BigDecimal(TileMatrix.GRID_SIZE), RoundingMode.CEILING).longValue();
        if (numberOfGridPixelsY < TileMatrix.GRID_SIZE) {
            matrixHeight = new BigDecimal(numberOfGridPixelsY).divide(new BigDecimal(numberOfGridPixelsY), RoundingMode.CEILING).longValue();
            // e.g. 18 pixels
            tileHeight = numberOfGridPixelsY;
        }
        
        // it contains the details of a TileMatrix in a TileMatrixSet (with CRS) to build an equivalent WMS request later
        TileMatrix result = new TileMatrix(
                                pyramidMemberCoverageId,
                                scaleDenominator, topLeftCorner,
                                tileWidth, tileHeight,
                                matrixWidth, matrixHeight,
                                tileMatrixSetEPSGCode,
                                geoLowerBoundX, geoUpperBoundX, geoLowerBoundY, geoUpperBoundY,
                                geoResolutionX, geoResolutionY,
                                numberOfGridPixelsX, numberOfGridPixelsY
                                );
        return result;
    }
    
}
