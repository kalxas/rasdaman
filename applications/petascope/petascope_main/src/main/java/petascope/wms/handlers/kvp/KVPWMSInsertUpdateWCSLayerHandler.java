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
package petascope.wms.handlers.kvp;

import java.math.BigDecimal;
import petascope.core.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.rasdaman.domain.wms.BoundingBox;
import org.rasdaman.domain.wms.EXGeographicBoundingBox;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.domain.wms.LayerAttribute;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.KVPSymbols;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.MIMEUtil;
import petascope.exceptions.WMSException;
import petascope.util.CrsProjectionUtil;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;
import petascope.wms.exception.WMSDuplicateLayerException;
import petascope.wms.exception.WMSInvalidBoundingBoxInCrsTransformException;
import petascope.wms.exception.WMSInvalidCrsUriException;
import petascope.wms.exception.WMSInvalidDimensionalityException;
import petascope.wms.exception.WMSLayerNotExistException;
import petascope.wms.exception.WMSMissingRequestParameter;
import petascope.wms.handlers.service.WMSGetMapCachingService;

/**
 * Handle the InsertWCSLayer request to insert a WMS layer, e.g:
 * service=WMS&version=1.3.0&request=InsertWCSLayer&withPyramids=False&wcsCoverageId=test_wms_4326
 *
 * or UpdateWCSLayer request to update a WMS laye
 *
 * NOTE: This is a made up request to insert / update WMS layer. There is no
 * DeleteLayer for WMS as it is inconsistent with WCS coverage, only when
 * deleting WCS coverage, WMS layer will be deleted.
 *
 * @author
 * <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class KVPWMSInsertUpdateWCSLayerHandler extends KVPWMSAbstractHandler {

    private static Logger log = LoggerFactory.getLogger(KVPWMSInsertUpdateWCSLayerHandler.class);

    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslator;
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;

    // It is the same as WGS:84 except the order (WGS:84 is Long, Lat) and WCS only allows to add CRS in EPSG:4326 order (Lat, Long)
    private static final String DEFAULT_CRS_CODE = "4326";
    private static final String DEFAULT_CRS_URI = "http://www.opengis.net/def/crs/EPSG/0/4326";

    public KVPWMSInsertUpdateWCSLayerHandler() {

    }

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws WMSException {
        // Check if wcsCoverageId does exist from the request        
        String[] layerParam = kvpParameters.get(KVPSymbols.KEY_WMS_WCSCOVERAGEID);
        if (layerParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_WCSCOVERAGEID);
        }
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        // Validate before handling the request
        this.validate(kvpParameters);

        // Check if layer does exist in database
        // NOTE: a WCS coverageId is equivalent to a WMS layer
        String layerName = kvpParameters.get(KVPSymbols.KEY_WMS_WCSCOVERAGEID)[0];

        String request = kvpParameters.get(KVPSymbols.KEY_REQUEST)[0];
        Layer layer = null;
        // InsertWCSLayer
        if (request.equals(KVPSymbols.VALUE_WMS_INSERT_WCS_LAYER)) {
            // Check if layer does not exist            
            layer = this.wmsRepostioryService.readLayerByNameFromDatabase(layerName);
            // Cannot add same layer name
            if (layer != null) {
                throw new WMSDuplicateLayerException(layerName);
            } else {
                // create new layer
                layer = new Layer();
            }
        } else {
            // UpdateWCSLayer                        
            layer = this.wmsRepostioryService.readLayerByNameFromDatabase(layerName);
        }

        // Layer does not exist, prepare to insert a new layer
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

        // NOTE: not supports insert 2D+ WMS coverage now, it needs to add the dimension object from WCS coverage metadata
        // @TODO: add non XY axes as Dimensions to layer so WMS can supports more than 2D request.
        int coverageDimensions = wcpsCoverageMetadata.getAxes().size();
        if (coverageDimensions > 2) {
            throw new WMSInvalidDimensionalityException(coverageDimensions);
        }

        // No need to add a default style as before, WMS with Styles= will return the default style
        // Persist the layer
        wmsRepostioryService.saveLayer(layer);
        log.info("WMS layer: " + layerName + " is persisted in database.");

        if (!request.equals(KVPSymbols.VALUE_WMS_INSERT_WCS_LAYER)) {
            // Remove all the cached GetMap response from cache as layer is updated
            this.wmsGetMapCachingService.removeLayerGetMapInCache(layerName);
        }

        // Returns the layer name as a success
        return new Response(Arrays.asList(layerName.getBytes()), MIMEUtil.MIME_GML, null);
    }

    /**
     * Create a EXGeographicBoundingBox from XY axes. NOTE: it always is WGS:84
     * crs (i.e: Long, Lat order)
     *
     * @param xyAxes
     * @return
     */
    private EXGeographicBoundingBox createEXGeographicBoundingBox(List<Axis> xyAxes) throws WCSException, WMSInvalidBoundingBoxInCrsTransformException {
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
            // Need to transform from native CRS of XY geo axes (e.g: EPSG:3857) to EPSG:4326
            String sourceCrs = xyAxes.get(0).getNativeCrsUri();
            String targetCrs = DEFAULT_CRS_URI;
            List<BigDecimal> minValues = null, maxValues = null;
            try {
                // min geo bounds
                double[] minSourceCoordinates = new double[]{minGeoBoundX.doubleValue(), minGeoBoundY.doubleValue()};            
                minValues = CrsProjectionUtil.transform(sourceCrs, targetCrs, minSourceCoordinates);
                // max geo bounds
                double[] maxSourceCoordinates = new double[]{maxGeoBoundX.doubleValue(), maxGeoBoundY.doubleValue()};
                maxValues = CrsProjectionUtil.transform(sourceCrs, targetCrs, maxSourceCoordinates);
            } catch (PetascopeException ex) {
                String bbox = "xmin=" + minGeoBoundX.doubleValue() + ", ymin=" + minGeoBoundY.doubleValue()
                            + "xmax=" + maxGeoBoundX.doubleValue() + ", ymax=" + maxGeoBoundY.doubleValue();
                throw new WMSInvalidBoundingBoxInCrsTransformException(bbox, sourceCrs, targetCrs, ex.getExceptionText());
            }

            // Output is EPSG:4326 (but GDAL always show Long, Lat)
            minLong = minValues.get(0);
            minLat = minValues.get(1);
            maxLong = maxValues.get(0);
            maxLat = maxValues.get(1);
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
