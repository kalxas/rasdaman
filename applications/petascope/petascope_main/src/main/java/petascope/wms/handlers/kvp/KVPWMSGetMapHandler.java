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
import org.apache.commons.lang3.math.NumberUtils;
import org.rasdaman.domain.wms.BoundingBox;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.domain.wms.Style;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WMSException;
import petascope.util.CrsProjectionUtil;
import petascope.util.ListUtil;
import petascope.util.MIMEUtil;
import petascope.util.StringUtil;
import petascope.wms.exception.WMSInvalidBoundingBoxExcpetion;
import petascope.wms.exception.WMSInvalidCrsUriException;
import petascope.wms.exception.WMSInvalidHeight;
import petascope.wms.exception.WMSInvalidInterpolation;
import petascope.wms.exception.WMSInvalidWidth;
import petascope.wms.exception.WMSLayerNotExistException;
import petascope.wms.exception.WMSMissingRequestParameter;
import petascope.wms.exception.WMSStyleNotFoundException;
import petascope.wms.exception.WMSUnsupportedFormatException;
import petascope.wms.handlers.service.WMSGetMapCachingService;
import petascope.wms.handlers.service.WMSGetMapExceptionService;
import petascope.wms.handlers.service.WMSGetMapService;

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

    private static Logger log = LoggerFactory.getLogger(KVPWMSDeleteStyleHandler.class);

    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private WMSGetMapService wmsGetMapService;
    @Autowired
    private WMSGetMapExceptionService wmsGetMapExceptionService;
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;

    public KVPWMSGetMapHandler() {

    }

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws WMSException {
        // Table 8 — The Parameters of a GetMap request (WMS 1.3.0 document)        
        // Layers (manadatory)
        String[] layersParam = kvpParameters.get(KVPSymbols.KEY_WMS_LAYERS);
        List<Layer> layers = new ArrayList<>();
        if (layersParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_LAYERS);
        } else {
            for (String layerName : layersParam[0].split(",")) {
                Layer layer = wmsRepostioryService.readLayerByNameFromCache(layerName);
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
        } else {
            // NOTE: if Styles=&format=image/png, so styles is empty for all the requesting layers (use the first style of layers)        
            if (!stylesParam[0].isEmpty()) {
                String[] styleNames = stylesParam[0].split(",");
                for (int i = 0; i < styleNames.length; i++) {
                    String styleName = styleNames[i];
                    // Each map in the list of LAYERS is drawn using the corresponding style in the same position in the list of STYLES.
                    // Style must exist in one of layers' styles.
                    boolean styleExists = false;
                    for (int j = 0; j < layers.size(); j++) {
                        Layer layer = layers.get(j);
                        Style style = layer.getStyle(styleName);
                        if (style != null) {
                            styleExists = true;
                            break;
                        }
                    }
                    
                    if (!styleExists) {
                        throw new WMSStyleNotFoundException(styleName);
                    }
                }
            }
        }

        // CRS (mandatory)
        String[] crsParam = kvpParameters.get(KVPSymbols.KEY_WMS_CRS);
        if (crsParam == null) {
            throw new WMSMissingRequestParameter(KVPSymbols.KEY_WMS_CRS);
        } else {
            // Check if crs is EPSG code (only supports now)
            if (!CrsProjectionUtil.validTransformation(crsParam[0])) {
                throw new WMSInvalidCrsUriException(crsParam[0]);
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
                throw new WMSInvalidBoundingBoxExcpetion(bboxParam[0]);
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
        // NOTE: WMS supports multiple types of exception report (XML and also image)
        String exceptionsFormat = kvpParameters.get(KVPSymbols.KEY_WMS_EXCEPTIONS) == null
                ? KVPWMSGetCapabilitiesHandler.EXCEPTION_XML
                : kvpParameters.get(KVPSymbols.KEY_WMS_EXCEPTIONS)[0];
        // If request with exceptions parameter in image, then use these default values if the kvp map cannot provide.
        String format = MIMEUtil.MIME_PNG;
        int width = 256;
        int height = 256;
        try {
            // NOTE: If first query returns success, then just fetch it from cache
            String queryString = StringUtil.buildQueryString(kvpParameters);
            if (WMSGetMapCachingService.responseCachingMap.containsKey(queryString)) {
                return wmsGetMapCachingService.getResponseFromCache(queryString);
            }
            // Validate before handling the request
            this.validate(kvpParameters);

            // Collect all the parameters (mandatory)
            List<String> layerNames = ListUtil.valuesToList(kvpParameters.get(KVPSymbols.KEY_WMS_LAYERS)[0].split(","));
            List<String> styleNames = ListUtil.valuesToList(kvpParameters.get(KVPSymbols.KEY_WMS_STYLES)[0].split(","));
            // Do some other validations for the styles

            String outputCRS = kvpParameters.get(KVPSymbols.KEY_WMS_CRS)[0];
            String bboxParam = kvpParameters.get(KVPSymbols.KEY_WMS_BBOX)[0];
            BoundingBox bbox = this.createBoundingBox(bboxParam);
            
            String widthValue = kvpParameters.get(KVPSymbols.KEY_WMS_WIDTH)[0];
            try {
                width = Integer.parseInt(widthValue);
            } catch (NumberFormatException ex) {
                throw new WMSInvalidWidth(widthValue);
            }
            
            if (width <= 0) {
                throw new WMSInvalidWidth(widthValue);
            }
            
            String heightValue = kvpParameters.get(KVPSymbols.KEY_WMS_HEIGHT)[0];
            try {
                height = Integer.parseInt(heightValue);
            } catch (NumberFormatException ex) {
                throw new WMSInvalidHeight(heightValue);
            }
            
            if (height <= 0) {
                throw new WMSInvalidHeight(heightValue);
            }
            
            format = kvpParameters.get(KVPSymbols.KEY_WMS_FORMAT)[0];

            // Optional values
            boolean transparent = false;
            if (kvpParameters.get(KVPSymbols.KEY_WMS_TRANSPARENT) != null) {
                transparent = Boolean.parseBoolean(kvpParameters.get(KVPSymbols.KEY_WMS_TRANSPARENT)[0]);
            }
            
            // Optional non XY axes subsets (e.g: time=...,dim_pressure=...)
            Map<String, String> dimSubsetsMap = new HashMap<>();
            if (kvpParameters.get(KVPSymbols.KEY_WMS_TIME) != null) {
                String timeSubset = kvpParameters.get(KVPSymbols.KEY_WMS_TIME)[0].trim();
                dimSubsetsMap.put(KVPSymbols.KEY_WMS_TIME, timeSubset);
            } 
            
            if (kvpParameters.get(KVPSymbols.KEY_WMS_ELEVATION) != null) {
                String elevationSubset = kvpParameters.get(KVPSymbols.KEY_WMS_ELEVATION)[0].trim();
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
            
            String interpolation = "";
            
            // Optional value (used only when requesting different CRS from layer's native CRS)
            if (kvpParameters.get(KVPSymbols.KEY_WMS_INTERPOLATION) != null) {
                interpolation = kvpParameters.get(KVPSymbols.KEY_WMS_INTERPOLATION)[0].trim();
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
            wmsGetMapService.setBBoxes(bbox);
            wmsGetMapService.setFormat(format);
            wmsGetMapService.setTransparent(transparent);
            wmsGetMapService.setDimSubsetsMap(dimSubsetsMap);
            wmsGetMapService.setInterpolation(interpolation);

            response = wmsGetMapService.createGetMapResponse();
            // Add the successful result to the cache
            wmsGetMapCachingService.addResponseToCache(queryString, response);
        } catch (Exception ex) {
            if (exceptionsFormat.equalsIgnoreCase(KVPWMSGetCapabilitiesHandler.EXCEPTION_XML)) {
                throw ex;
            } else {
                log.error("Catched an exeception: ", ex);
                wmsGetMapExceptionService.setErrorMessage(ex.getMessage());
                wmsGetMapExceptionService.setExceptionFormat(exceptionsFormat);
                wmsGetMapExceptionService.setWidth(width);
                wmsGetMapExceptionService.setHeight(height);
                wmsGetMapExceptionService.setFormat(format);
                response = wmsGetMapExceptionService.createImageExceptionResponse();
            }
        }

        return response;
    }

    /**
     * Create a BoundingBox object from the bbox string (e.g: -180,-90,180,90)
     * NOTE: WMS 1.3.0, bbox xy depends on the crs order.
     *
     * @param input
     * @return
     */
    private BoundingBox createBoundingBox(String input) throws WMSInvalidBoundingBoxExcpetion {

        BoundingBox bbox = new BoundingBox();
        String[] values = input.split(",");
        
        try {
            bbox.setXMin(new BigDecimal(values[0]));
        } catch (NumberFormatException ex) {
            throw new WMSInvalidBoundingBoxExcpetion(input, "xMin is not a number. Given: '" + values[0] + "'");
        }
        
        try {
            bbox.setYMin(new BigDecimal(values[1]));
        } catch (NumberFormatException ex) {
            throw new WMSInvalidBoundingBoxExcpetion(input, "yMin is not a number. Given: '" + values[1] + "'");
        }
        
        try {
            bbox.setXMax(new BigDecimal(values[2]));
        } catch (NumberFormatException ex) {
            throw new WMSInvalidBoundingBoxExcpetion(input, "xMax is not a number. Given: '" + values[2] + "'");
        }
        
        try {
            bbox.setYMax(new BigDecimal(values[3]));
        } catch (NumberFormatException ex) {
            throw new WMSInvalidBoundingBoxExcpetion(input, "yMax is not a number. Given: '" + values[3] + "'");
        }

        return bbox;
    }
}
