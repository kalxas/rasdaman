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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package com.rasdaman.admin.layer.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.wms.BoundingBox;
import org.rasdaman.domain.wms.Dimension;
import org.rasdaman.domain.wms.EXGeographicBoundingBox;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.domain.wms.LayerAttribute;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.GeoTransform;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import petascope.util.CrsProjectionUtil;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.IrregularAxis;
import petascope.wcps.metadata.model.RegularAxis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;
import petascope.wms.exception.WMSInvalidBoundingBoxInCrsTransformException;
import petascope.wms.exception.WMSInvalidCrsUriException;
import petascope.wms.handlers.service.WMSGetMapCachingService;

/**
 * Service to create a new WMS layer or update an existing WMS layer of a
 * coverage
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class AdminCreateOrUpdateLayerService {

    private static Logger log = LoggerFactory.getLogger(AdminCreateOrUpdateLayerService.class);
    
    // It is the same as WGS:84 except the order (WGS:84 is Long, Lat) and WCS only allows to add CRS in EPSG:4326 order (Lat, Long)
    private static final String DEFAULT_CRS_CODE = "4326";
    private static final String DEFAULT_EPSG_CRS = "EPSG:4326";

    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslator;
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;

    /**
     * If a layer name associated with a coverageID doesn't exist, then create a
     * new layer object If this layer exists, then update this layer from the
     * associated coverage then persist to database
     */
    public void save(String layerName, Boolean isBlacklisted) throws Exception {
        
        if (!this.coverageRepositoryService.isInLocalCache(layerName)) {
            throw new PetascopeException(ExceptionCode.NoSuchCoverage, "Coverage '" + layerName + "' does not exist in local database.");
        }

        boolean layerExist = this.wmsRepostioryService.isInLocalCache(layerName);
        Layer layer = null;
        
        if (!layerExist) {
            layer = new Layer();
        } else {
            layer = this.wmsRepostioryService.readLayerByNameFromDatabase(layerName);
        }

        // NOTE: As WMS layer is a WCS coverage so reuse the functionalities from WCS coverage metadata
        WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(layerName);
        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
        // Check the crs of xyAxes, should only geo-referenced CRS
        String crs = xyAxes.get(0).getNativeCrsUri();
        if (CrsUtil.isGridCrs(crs) || CrsUtil.isIndexCrs(crs)) {
            throw new WMSInvalidCrsUriException(crs);
        }

        layer.setName(layerName);
        layer.setTitle(layerName);

        // Only set 1 crs for 1 layer now
        layer.setCrss(ListUtil.valuesToList(CrsUtil.getEPSGCode(crs)));

        // These attributes are fixed by default (cascaded, fixedHeight,...)
        LayerAttribute layerAttribute = new LayerAttribute();
        layer.setLayerAttribute(layerAttribute);

        // EX_geographicBoundingBox
        EXGeographicBoundingBox exBBox = this.createEXGeographicBoundingBox(xyAxes);
        layer.setExGeographicBoundingBox(exBBox);

        // Only set 1 bounding box for 1 native CRS now
        BoundingBox bbox = this.createBoundingBox(wcpsCoverageMetadata.isXYOrder(), xyAxes);
        layer.setBoundingBoxes(ListUtil.valuesToList(bbox));

        List<Dimension> dimensions = new ArrayList<>();
        // Create dimensions for 3D+ coverage
        for (Axis axis : wcpsCoverageMetadata.getAxes()) {
            // Non XY axes only
            if (axis.isNonXYAxis()) {
                Dimension dimension = new Dimension();

                if (axis.isTimeAxis()) {
                    // NOTE: TIME and ELEVATION are special axes names in WMS 1.3
                    dimension.setName(KVPSymbols.KEY_WMS_TIME);
                } else if (axis.isElevationAxis()) {
                    dimension.setName(KVPSymbols.KEY_WMS_ELEVATION);
                } else {
                    dimension.setName(axis.getLabel());
                }

                String axisExtent;
                // According to Table C.2, WMS 1.3 document
                // if axis is regular, the extent will be: minGeoBound/maxGeoBound/resolution_with_axisUom (e.g: "1949-12-31T12:00:00.000Z"/"1950-01-06T12:00:00.000Z"/1d)
                if (axis instanceof RegularAxis) {
                    String minGeoBound = axis.getLowerGeoBoundRepresentation();
                    String maxGeoBound = axis.getUpperGeoBoundRepresentation();
                    String resolution = axis.getResolution().toPlainString();
                    String axisUoM = axis.getAxisUoM();
                    axisExtent = minGeoBound + VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER + maxGeoBound + VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER + resolution + axisUoM;
                } else {
                    // if it is irregular, the extent will be the list of seperate values: value1,value2,...valueN
                    axisExtent = ((IrregularAxis) axis).getRepresentationCoefficients();
                    axisExtent = axisExtent.replace(" ", ",");
                }
                dimension.setExtent(axisExtent);
                dimensions.add(dimension);
            }
        }

        layer.setDimensions(dimensions);

        // No need to add a default style as before, WMS with Styles= will return the default style
        // Persist the layer
        wmsRepostioryService.saveLayer(layer);
        log.info("Layer '" + layerName + "' is persisted to local database.");

        if (layerExist) {
            // Remove all the cached GetMap response from cache as layer is updated
            this.wmsGetMapCachingService.removeLayerGetMapInCache(layerName);
        }
    }

    /**
     * Create a EXGeographicBoundingBox from XY axes. NOTE: it always is WGS:84
     * crs (i.e: Long, Lat order)
     */
    public EXGeographicBoundingBox createEXGeographicBoundingBox(List<Axis> xyAxes) throws WCSException, WMSInvalidBoundingBoxInCrsTransformException, PetascopeException {
        EXGeographicBoundingBox exBBox = new EXGeographicBoundingBox();
        String crs = CrsUtil.getCode(xyAxes.get(0).getNativeCrsUri());

        // No need to transform as EPSG:4326 is same coordinates as WGS:84
        // NOTE: xyAxes in CRS:4326 is Lat (X), Long (Y) order              
        BigDecimal minGeoBoundX = xyAxes.get(0).getGeoBounds().getLowerLimit();
        BigDecimal minGeoBoundY = xyAxes.get(1).getGeoBounds().getLowerLimit();
        BigDecimal maxGeoBoundX = xyAxes.get(0).getGeoBounds().getUpperLimit();
        BigDecimal maxGeoBoundY = xyAxes.get(1).getGeoBounds().getUpperLimit();

        BigDecimal minLong = minGeoBoundX;
        BigDecimal minLat = minGeoBoundY;
        BigDecimal maxLong = maxGeoBoundX;
        BigDecimal maxLat = maxGeoBoundY;

        if (!crs.equals(DEFAULT_CRS_CODE)) {
            String sourceCrs = xyAxes.get(0).getNativeCrsUri();
            int epsgCode = CrsUtil.getEpsgCodeAsInt(sourceCrs);

            Axis axisX = xyAxes.get(0);
            Axis axisY = xyAxes.get(1);
            int gridWidth = axisX.getGridBounds().getUpperLimit().subtract(axisX.getGridBounds().getLowerLimit()).intValue() + 1;
            int gridHeight = axisY.getGridBounds().getUpperLimit().subtract(axisY.getGridBounds().getLowerLimit()).intValue() + 1;

            GeoTransform geoTransform = new GeoTransform(epsgCode, minGeoBoundX, maxGeoBoundY,
                                                        gridWidth, gridHeight, axisX.getResolution(), axisY.getResolution());

            // Need to transform from native CRS of XY geo axes (e.g: EPSG:3857) to EPSG:4326
            String targetCrs = CrsUtil.getEPSGFullUri(DEFAULT_EPSG_CRS);
            petascope.core.BoundingBox wgs84bbox;
            try {
                wgs84bbox = CrsProjectionUtil.transform(geoTransform, DEFAULT_EPSG_CRS);
            } catch (PetascopeException ex) {
                String bbox = "xmin=" + minGeoBoundX.doubleValue() + ", ymin=" + minGeoBoundY.doubleValue()
                        + "xmax=" + maxGeoBoundX.doubleValue() + ", ymax=" + maxGeoBoundY.doubleValue();
                throw new WMSInvalidBoundingBoxInCrsTransformException(bbox, sourceCrs, targetCrs, ex.getExceptionText());
            }

            // Output is EPSG:4326 (but GDAL always show Long, Lat)
            minLong = wgs84bbox.getXMin();
            minLat = wgs84bbox.getYMin();
            maxLong = wgs84bbox.getXMax();
            maxLat = wgs84bbox.getYMax();
        }

        exBBox.setWestBoundLongitude(minLong);
        exBBox.setEastBoundLongitude(maxLong);
        exBBox.setSouthBoundLatitude(minLat);
        exBBox.setNorthBoundLatitude(maxLat);

        return exBBox;
    }

    /**
     * Create a bounding box for the XY axes by the CRS order
     */
    private BoundingBox createBoundingBox(boolean isXYOrder, List<Axis> xyAxes) {

        BoundingBox bbox = new BoundingBox();
        bbox.setCrs(CrsUtil.getEPSGCode(xyAxes.get(0).getNativeCrsUri()));

        if (isXYOrder) {
            // EPSG:3857 is XY order
            bbox.setXMin(xyAxes.get(0).getGeoBounds().getLowerLimit());
            bbox.setYMin(xyAxes.get(1).getGeoBounds().getLowerLimit());
            bbox.setXMax(xyAxes.get(0).getGeoBounds().getUpperLimit());
            bbox.setYMax(xyAxes.get(1).getGeoBounds().getUpperLimit());
        } else {
            // EPSG:4326 is YX order
            bbox.setXMin(xyAxes.get(1).getGeoBounds().getLowerLimit());
            bbox.setYMin(xyAxes.get(0).getGeoBounds().getLowerLimit());
            bbox.setXMax(xyAxes.get(1).getGeoBounds().getUpperLimit());
            bbox.setYMax(xyAxes.get(0).getGeoBounds().getUpperLimit());
        }

        return bbox;
    }

}
