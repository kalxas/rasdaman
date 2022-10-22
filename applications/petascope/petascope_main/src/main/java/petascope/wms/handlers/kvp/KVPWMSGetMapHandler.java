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
  *  Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wms.handlers.kvp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.math.NumberUtils;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.cis.Wgs84BoundingBox;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.AbstractController;
import petascope.controller.PetascopeController;
import petascope.core.BoundingBox;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WMSException;
import petascope.util.CrsProjectionUtil;
import petascope.util.ListUtil;
import petascope.util.MIMEUtil;
import petascope.util.StringUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wms.exception.WMSInvalidBoundingBoxException;
import petascope.wms.exception.WMSInvalidCrsUriException;
import petascope.wms.exception.WMSInvalidHeight;
import petascope.wms.exception.WMSInvalidInterpolation;
import petascope.wms.exception.WMSInvalidWidth;
import petascope.wms.exception.WMSLayerNotExistException;
import petascope.wms.exception.WMSMissingRequestParameter;
import petascope.wms.exception.WMSStyleNotMatchLayerNumbersException;
import petascope.wms.exception.WMSUnsupportedFormatException;
import petascope.wms.handlers.service.WMSGetMapCachingService;
import petascope.wms.handlers.service.WMSGetMapExceptionService;
import petascope.wms.handlers.service.WMSGetMapService;
import petascope.wms.handlers.service.WMSGetMapWCPSMetadataTranslatorService;

/**
 * Class to handle the KVP WMS GetMap request, e.g:
 * service=WMS&version=1.3.0&request=GetMap&layers=test_wms_3857&bbox=-44.525,111.976,-8.978,156.274&crs=EPSG:4326&
 * width=600&height=600&format=image/png NOTE: if crs is different from native
 * CRS, so must transform from requesting bbox to native CRS, e.g:
 * service=WMS&version=1.3.0&request=GetMap&layers=test_wms_4326&bbox=12464999.982,-5548370.985,17393850.457,-1003203.187&
 * crs=EPSG:3857&width=600&height=600&format=image/png It works as same as
 * subsettingCrs in WCS and the output should be reprojected to the requesting
 * CRS (EPSG:3857)
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWMSGetMapHandler extends KVPWMSAbstractHandler {

    private static Logger log = LoggerFactory.getLogger(KVPWMSGetMapHandler.class);

    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private WMSGetMapService wmsGetMapService;
    @Autowired
    private WMSGetMapExceptionService wmsGetMapExceptionService;
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private PetascopeController petascopeController;    
    
    @Autowired
    private WMSGetMapWCPSMetadataTranslatorService wmsGetMapWCPSMetadataTranslatorService;    

    public KVPWMSGetMapHandler() {

    }

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws WMSException, PetascopeException {
        // Table 8 — The Parameters of a GetMap request (WMS 1.3.0 document)        
        // Layers (manadatory)
        String[] layersParam = kvpParameters.get(KVPSymbols.KEY_WMS_LAYERS);
        List<Layer> layers = new ArrayList<>();
        if (layersParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_LAYERS);
        } else {
            for (String layerName : layersParam[0].split(",")) {
                Layer layer = wmsRepostioryService.readLayerByName(layerName);
                if (layer == null) {
                    throw new WMSLayerNotExistException(layerName);
                }
                layers.add(layer);
            }
        }

        // Styles (mandatory), can be empty (e.g: a coverage with no style)
        String[] stylesParam = kvpParameters.get(KVPSymbols.KEY_WMS_STYLES);
        if (stylesParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_STYLES);
        }

        // CRS (mandatory)
        String[] crsParam = kvpParameters.get(KVPSymbols.KEY_WMS_CRS);
        if (crsParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_CRS);
        } else {
            String crs = crsParam[0];
            // Check if crs is supported for projection
            try {
                if (!CrsProjectionUtil.isValidTransform(crs)) {
                    throw new WMSInvalidCrsUriException(crs);
                }
            } catch (Exception ex) {
                throw new WMSInvalidCrsUriException(crs);
            }
        }

        // BBOX (mandatory)
        String[] bboxParam = kvpParameters.get(KVPSymbols.KEY_WMS_BBOX);
        if (bboxParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_BBOX);
        } else {
            // BBOX must follow this pattern: minX,minY,maxX,maxY
            int countCommas = 0;
            for (int i = 0; i < bboxParam[0].length(); i++) {
                if (bboxParam[0].charAt(i) == ',') {
                    countCommas++;
                }
            }    
            
            if (countCommas != 3) {
                throw new WMSInvalidBoundingBoxException(bboxParam[0]);
            }
        }

        // WIDTH (mandatory)
        String[] widthParam = kvpParameters.get(KVPSymbols.KEY_WMS_WIDTH);
        if (widthParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_WIDTH);
        } else {
            if (!NumberUtils.isNumber(widthParam[0])) {
                throw new WMSInvalidWidth(widthParam[0]);
            }
        }

        // HEIGHT (mandatory)
        String[] heightParam = kvpParameters.get(KVPSymbols.KEY_WMS_HEIGHT);
        if (heightParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_HEIGHT);
        } else {
            if (!NumberUtils.isNumber(heightParam[0])) {
                throw new WMSInvalidHeight(heightParam[0]);
            }
        }

        // FORMAT (mandatory)
        String[] formatParam = kvpParameters.get(KVPSymbols.KEY_WMS_FORMAT);
        if (formatParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_FORMAT);
        } else {
            if (KVPWMSGetCapabilitiesHandler.supportedFormats.indexOf(formatParam[0]) == -1) {
                throw new WMSUnsupportedFormatException(formatParam[0]);
            }
        }
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        Response response = null;
        int width = 256;
        int height = 256;
        // if the request contains a random=random_number then just ignore it as it is used to bypass Web Browser's cache only
        kvpParameters.remove(KVPSymbols.KEY_WMS_RASDAMAN_RANDOM);

        // NOTE: If first query returns success, then just fetch it from cache
        String queryString = StringUtil.buildQueryString(kvpParameters);
        if (ConfigManager.MAX_WMS_CACHE_SIZE > 0 && WMSGetMapCachingService.responseCachingMap.containsKey(queryString)) {
            return wmsGetMapCachingService.getResponseFromCache(queryString);
        }
        // Validate before handling the request
        this.validate(kvpParameters);

        // Collect all the parameters (mandatory)
        List<String> layerNames = ListUtil.valuesToList(AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMS_LAYERS).split(","));

        List<String> styleNames = new ArrayList<>();
        String styleValue = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMS_STYLES);
        if (!styleValue.isEmpty()) {
            styleNames = ListUtil.valuesToList(styleValue.split(","));
        }

        // All layers can use one default style (styles=) or each layer will need its own style (layers=L1,L2,L3&styles=s1,s2,s3)
        if (!styleNames.isEmpty() && (layerNames.size() != styleNames.size())) {
            throw new WMSStyleNotMatchLayerNumbersException(layerNames.size(), styleNames.size());
        }

        String outputCRS = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMS_CRS);
        String bboxParam = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMS_BBOX);
        BoundingBox bbox = this.createBoundingBox(bboxParam);

        String widthValue = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMS_WIDTH);
        try {
            width = Integer.parseInt(widthValue);
        } catch (NumberFormatException ex) {
            throw new WMSInvalidWidth(widthValue);
        }

        if (width <= 0) {
            throw new WMSInvalidWidth(widthValue);
        }

        String heightValue = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMS_HEIGHT);
        try {
            height = Integer.parseInt(heightValue);
        } catch (NumberFormatException ex) {
            throw new WMSInvalidHeight(heightValue);
        }

        if (height <= 0) {
            throw new WMSInvalidHeight(heightValue);
        }

        String format = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMS_FORMAT);

        // Optional values
        boolean transparent = false;
        if (kvpParameters.get(KVPSymbols.KEY_WMS_TRANSPARENT) != null) {
            transparent = Boolean.parseBoolean(AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMS_TRANSPARENT));
        }

        // Optional non XY axes subsets (e.g: time=...,dim_pressure=...)
        Map<String, String> dimSubsetsMap = new HashMap<>();
        if (kvpParameters.get(KVPSymbols.KEY_WMS_TIME) != null) {
            String timeSubset = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMS_TIME);
            dimSubsetsMap.put(KVPSymbols.KEY_WMS_TIME, timeSubset);
        } 

        if (kvpParameters.get(KVPSymbols.KEY_WMS_ELEVATION) != null) {
            String elevationSubset = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMS_ELEVATION);
            dimSubsetsMap.put(KVPSymbols.KEY_WMS_ELEVATION, elevationSubset);
        } 

        // Check if request contains other dimensions (e.g: dim_pressure)
        for (Map.Entry<String, String[]> entry : kvpParameters.entrySet()) {
            if (entry.getKey().contains(KVPSymbols.KEY_WMS_DIM_PREFIX)) {
                String axisName = entry.getKey().split("_")[1];
                String dimSubset = entry.getValue()[0];
                dimSubsetsMap.put(axisName, dimSubset.trim());
            }
        }

        // Support for non-standard dim_ prefix for non-XY axes
        WcpsCoverageMetadata wcpsCoverageMetadataTmp = this.wmsGetMapWCPSMetadataTranslatorService.translate(layerNames.get(0));
        for (Axis axis : wcpsCoverageMetadataTmp.getNonXYAxes()) {
            String axisName = axis.getLabel();
            String[] valueTmps = kvpParameters.get(axisName);
            if (valueTmps != null) {
                dimSubsetsMap.put(axisName, valueTmps[0]);
            }
        }

        String interpolation = "";

        // Optional value (used only when requesting different CRS from layer's native CRS)
        if (kvpParameters.get(KVPSymbols.KEY_WMS_INTERPOLATION) != null) {
            interpolation = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_WMS_INTERPOLATION);
            if (!WMSGetMapService.validInterpolations.contains(interpolation)) {
                throw new WMSInvalidInterpolation(interpolation);
            }
        } else {
            interpolation = WMSGetMapService.DEFAULT_INTERPOLATION;
        }

        wmsGetMapService.setLayerNames(layerNames);
        wmsGetMapService.setStyleNames(styleNames);
        wmsGetMapService.setOutputCRS(outputCRS);
        wmsGetMapService.setWidth(width);
        wmsGetMapService.setHeight(height);
        wmsGetMapService.setDimSubsetsMap(dimSubsetsMap);
        wmsGetMapService.setBBoxes(bbox, layerNames);
        wmsGetMapService.setFormat(format);
        wmsGetMapService.setTransparent(transparent);            
        wmsGetMapService.setInterpolation(interpolation);

        // In case, GetMap request is generated from a WMTS GetTile service
        String tileMatrixName = AbstractController.getValueByKeyAllowNull(kvpParameters, 
                                                            KVPSymbols.KEY_WMTS_RASDAMAN_INTERNAL_FOR_GETMAP_REQUEST_PYRAMID_COVERAGE_ID);
        if (tileMatrixName != null) {
            wmsGetMapService.setWMTSTileMatrixName(tileMatrixName);
        }

        wmsGetMapService.setBBoxes(bbox, layerNames);

        response = wmsGetMapService.createGetMapResponse();

        // Store the request's bbox in EPSG:4326 in cache
        Wgs84BoundingBox wgs84BBox = wmsGetMapService.getWgs84BBox();

        // Add the successful result to the cache
        wmsGetMapCachingService.addResponseToCache(queryString, wgs84BBox, response);

        return response;
    }

    /**
     * Create a BoundingBox object from the bbox string (e.g: -180,-90,180,90)
     * NOTE: WMS 1.3.0, bbox xy depends on the crs order.
     *
     * @param input
     * @return
     */
    private BoundingBox createBoundingBox(String input) throws WMSInvalidBoundingBoxException {

        BoundingBox bbox = new BoundingBox();
        String[] values = input.split(",");
        BigDecimal xMin = null;
        BigDecimal yMin = null;
        BigDecimal xMax = null;
        BigDecimal yMax = null;
        
        if (values.length != 4) {
            throw new WMSInvalidBoundingBoxException(input, "bbox paramter value must be this pattern: xmin,ymin,xmax,ymax.");
        }
        
        try {
            xMin = new BigDecimal(values[0].trim());
            bbox.setXMin(xMin);
        } catch (NumberFormatException ex) {
            throw new WMSInvalidBoundingBoxException(input, "xMin is not a number. Given: '" + values[0].trim() + "'");
        }
        
        try {
            yMin = new BigDecimal(values[1].trim());
            bbox.setYMin(yMin);
        } catch (NumberFormatException ex) {
            throw new WMSInvalidBoundingBoxException(input, "yMin is not a number. Given: '" + values[1].trim() + "'");
        }
        
        try {
            xMax = new BigDecimal(values[2].trim());
            bbox.setXMax(xMax);
        } catch (NumberFormatException ex) {
            throw new WMSInvalidBoundingBoxException(input, "xMax is not a number. Given: '" + values[2].trim() + "'");
        }
        
        try {
            yMax = new BigDecimal(values[3].trim());
            bbox.setYMax(yMax);
        } catch (NumberFormatException ex) {
            throw new WMSInvalidBoundingBoxException(input, "yMax is not a number. Given: '" + values[3].trim() + "'");
        }
        
        if (xMin.compareTo(xMax) >= 0) {
            throw new WMSInvalidBoundingBoxException(input, "xMin must be smaller than xMax. Given: xMin=" + xMin.toPlainString() + " and xMax=" + xMax.toPlainString());
        }
        if (yMin.compareTo(yMax) >= 0) {
            throw new WMSInvalidBoundingBoxException(input, "yMin must be smaller than yMax. Given: yMin=" + xMin.toPlainString() + " and yMax=" + xMax.toPlainString());
        }

        return bbox;
    }
}
