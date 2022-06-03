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
  *  Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wms.handlers.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rasdaman.accesscontrol.service.AuthenticationService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.domain.wms.Style;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.core.BoundingBox;
import petascope.core.Pair;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.exceptions.WMSException;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.MIMEUtil;
import petascope.util.ras.RasUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wms.exception.WMSInternalException;
import petascope.core.gml.metadata.model.CoverageMetadata;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.util.CrsProjectionUtil;
import petascope.util.JSONUtil;
import petascope.wcps.encodeparameters.model.JsonExtraParams;
import petascope.wcps.encodeparameters.model.NoData;
import petascope.wcps.encodeparameters.service.SerializationEncodingService;
import petascope.wcps.encodeparameters.service.TranslateColorTableService;
import petascope.wcps.handler.SubsetExpressionHandler;
import petascope.wcps.subset_axis.model.WcpsSliceSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wms.exception.WMSStyleNotFoundException;
import petascope.wms.handlers.model.WMSLayer;
import static petascope.wms.handlers.service.WMSGetMapStyleService.ENCODE;
import static petascope.wms.handlers.service.WMSGetMapStyleService.EXTEND;
import static petascope.wms.handlers.service.WMSGetMapStyleService.FRAGMENT_ITERATOR_PREFIX;
import static petascope.wms.handlers.service.WMSGetMapStyleService.RASQL_FRAGMENT_ITERATOR;
import static petascope.wms.handlers.service.WMSGetMapStyleService.SELECT;
import static petascope.wms.handlers.service.WMSGetMapStyleService.WCPS_FRAGMENT_ITERATOR;

/**
 * Service class to build the response from a WMS GetMap request. In case of a
 * GetMap request which contains styles using wcpsQueryFragment or
 * rasqlTransformFragment, it will need to generate a rasql query for each style
 * accordingly.
 *
 * Example: a GetMap request with
 * layers=test_wms_4326&bbox=-44.525,111.976,-8.978,156.274&crs=EPSG:4326&width=600&height=600&Styles=aStyle
 *
 * Then, the rasql query for bbox and width, height is:
 * SCALE(test_wms_4326[0:221,1:176], [0:599, 0:599]) (*) + If aStyle is a
 * wcpsQueryFragment, e.g: $c + 5 (then $c is replaced by (*)) + If aStyle is a
 * rasqlTransformFragment, e.g: case $Iterator when ($Iterator + 2) > 20 then
 * $Iterator is replaced by (*)
 * 
 * NOTE: in case of multiple layers are requested, e.g: LAYERS=lay1,lay2 then
 * according to WMS 1.3.0:
 * 
 * A WMS shall render the requested layers by drawing the leftmost
 * in the list bottommost, the next one over that,
 * and so on.
 * 
 * e.g: translated rasql query: lay1 (bottom) OVERLAY lay2 (top)
 *
 * @author
 * <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WMSGetMapService {

    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private WMSGetMapStyleService wmsGetMapStyleService;
    @Autowired
    private WMSGetMapBBoxService wmsGetMapBBoxService;
    @Autowired
    private WMSGetMapWCPSMetadataTranslatorService wmsGetMapWCPSMetadataTranslatorService;
    @Autowired
    private WMSGetMapSubsetParsingService wmsGetMapSubsetParsingService;
    @Autowired
    private WMSGetMapSubsetTranslatingService wmsGetMapSubsetTranslatingService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    
    // In case of nativeCrs of layer (coverage) is different from outputCrs of GetMap request, then it needs to reproject $collectionExpression from sourceCrs to targetCrs.
    public static final String COLLECTION_EXPRESSION_TEMPLATE = "$collectionExpression";
    private static final String FORMAT_TYPE_TEMPLATE = "$formatType";
    private static final String ENCODE_FORMAT_PARAMETERS_TEMPLATE = "$encodeFormatParameters";
    private static final String COLLECTIONS_TEMPLATE = "$collections";
    private static final String SUBSET_COVERAGE_EXPRESSION_TEMPLATE = "( " + COLLECTION_EXPRESSION_TEMPLATE + " )";
    private static final String FINAL_TRANSLATED_RASQL_TEMPLATE = "SELECT ENCODE(" + COLLECTION_EXPRESSION_TEMPLATE + ", "
            + "\"" + FORMAT_TYPE_TEMPLATE + "\", \"" + ENCODE_FORMAT_PARAMETERS_TEMPLATE + "\") FROM " + COLLECTIONS_TEMPLATE;
    public static final String OVERLAY = " OVERLAY ";
    public static final String TRANSPARENT_DOMAIN = "<[0:0,0:0] 0c>";
    public static final String DEFAULT_NULL_VALUE = "0";
    public static final String DEFAULT_INTERPOLATION = "near";

    private List<String> layerNames;
    private List<String> styleNames;
    private String outputCRS;
    private Integer width;
    private Integer height;
    // MIME type (e.g: image/png)
    private String format;
    // Optional parameter, used only incase with project()
    private String interpolation;    
    private boolean transparent;
    
    // The bbox parameter from WMS clients (e.g: in EPSG:4326), the layer's native CRS can be different
    private BoundingBox originalRequestBBox;
    
    // If layer has different CRS (e.g: UTM 32 CRS) and request BBOX from client is EPSG:4326, then transform the CRS from UTM32 -> EPSG:4326
    private Map<String, BoundingBox> layerRequestCRSBBoxesMap = new LinkedHashMap<>();
    
    // NOTE: this fittedBBox is used to fit input BBox to coverage's geo XY axes' domains 
    // to avoid server killed by subsetting collection (e.g: c[-20:30] instead of c[0:30])
    private Map<String, BoundingBox> fittedRequestBBoxesMap = new LinkedHashMap<>();

    // Used when requesting a BBox which is not layer's nativeCRS
    // because of projection() the result does not fill the bounding box and shows gaps (null values) in the result
    // then, it needs to select a bigger subsets (e.g: [lower_width -10% : upper_width + 10%, lower_height - 10% : upper_height + 10%]
    // in rasdaman to cover the gaps then apply the original subset [lower_width:upper_width, lower_height:upper_height]
    // to show the result without gaps in the borders.
    private Map<String, BoundingBox> extendedFittedRequestGeoBBoxesMap = new LinkedHashMap<>();
    
    // If request BoundingBox in different CRS from layer's CRS, it is true
    private boolean isProjection = false;
    
    private Map<String, String> dimSubsetsMap = new HashMap<>();
    
    private List<WcpsSubsetDimension> nonXYSubsetDimensions = new ArrayList<>();
    
    private static final Map<String, Response> blankTileMap = new ConcurrentHashMap<>();
    
    public static final Set<String> validInterpolations = new LinkedHashSet<>();
    
    // Only 1 color table definition coming from the first style defines it (!)
    // e.g: layerA has colorTable A, layerB has colorTable B, then request with layers=layerA,layerB will end up with colorTable A
    // to be used as extra parameter in the final rasql encode().
    private String colorTableDefinition = null;
    private Byte colorTableTypeCode = -1;
    
    // e.g: test_wms_4326 layer: {c0 -> test_wms_4326_collection, c1 -> test_wms_4326_new_collection}
    private Map<String, String> aliasCollectionNameRegistry = new LinkedHashMap<>();
    
    private static Logger log = LoggerFactory.getLogger(WMSGetMapService.class);
    
    static {
        // check valid values at http://doc.rasdaman.org/04_ql-guide.html#the-project-function
        // default is nearest neighbor
        validInterpolations.add(DEFAULT_INTERPOLATION);
        validInterpolations.add("bilinear");
        validInterpolations.add("cubic");
        validInterpolations.add("cubicspline");
        validInterpolations.add("lanczos");
        validInterpolations.add("average");
        validInterpolations.add("mode");
        validInterpolations.add("max");
        validInterpolations.add("min");
        validInterpolations.add("med");
        validInterpolations.add("q1");
        validInterpolations.add("q3");
    }

    public WMSGetMapService() {

    }

    public void setLayerNames(List<String> layerNames) {
        this.layerNames = layerNames;
    }

    public void setStyleNames(List<String> styleNames) {
        this.styleNames = styleNames;
    }

    public void setOutputCRS(String outputCRS) {
        this.outputCRS = outputCRS;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public Map<String, String> getDimSubsetsMap() {
        return dimSubsetsMap;
    }

    public void setDimSubsetsMap(Map<String, String> dimSubsetsMap) {
        this.dimSubsetsMap = dimSubsetsMap;
    }

    public void setInterpolation(String interpolation) {
        this.interpolation = interpolation;
    }
    
    public void setBBoxes(BoundingBox bbox, List<String> layerNames) throws PetascopeException, SecoreException {
        // If request is in YX order for bounding box (e.g: EPSG:4326 Lat, Long, swap it to XY order Long, Lat)
        // NOTE: as all layers requested with same outputCRS so only do this one time
        this.originalRequestBBox = this.wmsGetMapBBoxService.swapYXBoundingBox(bbox, outputCRS);
        
        for (String layerName : layerNames) {
            BoundingBox fittedRequestBBox = this.wmsGetMapBBoxService.swapYXBoundingBox(bbox, outputCRS);
            this.fittedRequestBBoxesMap.put(layerName, fittedRequestBBox);

            WcpsCoverageMetadata wcpsCoverageMetadataTmp = this.wmsGetMapWCPSMetadataTranslatorService.translate(layerName);
            this.nonXYSubsetDimensions = this.wmsGetMapSubsetParsingService.translateDimensionsMap(wcpsCoverageMetadataTmp, this.dimSubsetsMap);
            BoundingBox layerBBoxNativeCRS = wcpsCoverageMetadataTmp.getOrginalGeoXYBoundingBox();
            this.layerRequestCRSBBoxesMap.put(layerName, layerBBoxNativeCRS);


            WMSLayer wmsLayer = this.wmsGetMapWCPSMetadataTranslatorService.createWMSLayer(layerName, layerBBoxNativeCRS,
                                                                                           fittedRequestBBox, fittedRequestBBox, 
                                                                                           this.width, this.height,
                                                                                           this.nonXYSubsetDimensions);

            WcpsCoverageMetadata wcpsCoverageMetadata = this.wmsGetMapWCPSMetadataTranslatorService.createWcpsCoverageMetadataForDownscaledLevelByExtendedRequestBBox(wmsLayer);
            List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();

            String nativeCRS = xyAxes.get(0).getNativeCrsUri();
            this.isProjection = !CrsUtil.equalsWKT(CrsUtil.getWKT(this.outputCRS), CrsUtil.getWKT(nativeCRS));
            if (isProjection) {
                // e.g: layer's nativeCRS is EPSG:32632 and request BBox is EPSG:4326            
                this.transformNativeCRSBBoxToRequestCRS(layerName, xyAxes);
                
                BoundingBox layerBBoxRequestCRS = this.layerRequestCRSBBoxesMap.get(layerName);

                // Get the intersected geo bbox in layer's native CRS (e.g: UTM 32) by the original request bbox (e.g: in EPSG:4326) with the layer's bbox (e.g: in EPSG:4326)
                fittedRequestBBox = this.wmsGetMapBBoxService.getIntersectedBBoxInNativeCRS(originalRequestBBox, layerBBoxRequestCRS, layerBBoxNativeCRS, outputCRS, nativeCRS);
                this.fittedRequestBBoxesMap.put(layerName, fittedRequestBBox);

            }
        }
        
        this.extendedFittedRequestGeoBBoxesMap = this.fittedRequestBBoxesMap;
    }
    
    /**
     * Create the response for the GetMap request. NOTE: As WMS layer is a WCS
     * coverage so reuse the functionalities from WCS coverage metadata
     */
    public Response createGetMapResponse() throws WMSException, PetascopeException {
        byte[] bytes = null;
        
        String finalRasqlQuery = "";
        
        try {
            if (!this.intersectLayerXYBBox()) {
                Response response = this.createBlankImage();
                return response;
            }
            
            List<WcpsCoverageMetadata> wcpsCoverageMetadatas = this.getWcpsCoverageMetadataByLayernames();
            List<WMSLayer> wmsLayers = this.createWMSLayers(wcpsCoverageMetadatas);

            List<String> finalCollectionExpressions = new ArrayList<>();            
            // If GetMap requests with transparent=true then extract the nodata values from layer to be added to final rasql query
            List<BigDecimal> nodataValues = new ArrayList<>();
            
            int styleIndex = 0;
                       
            // e.g layers=layer1,layer2,layer3 then according to WMS standard (leftmost = bottommost)
            // layer1 at the bottom, layer2 at the middle, layer3 on top (same with OVERLAY() operator)
            for (WMSLayer wmsLayer : wmsLayers) {
                
                String styleName = null;
                if (this.styleNames.size() > 0) {
                    styleName = this.styleNames.get(styleIndex);
                }

                WcpsCoverageMetadata wcpsCoverageMetadata = this.wmsGetMapWCPSMetadataTranslatorService.createWcpsCoverageMetadataForDownscaledLevelByExtendedRequestBBox(wmsLayer);
                
                if (nodataValues.isEmpty()) {
                    if (wcpsCoverageMetadata.getNilValues().size() > 0) {
                        if (wcpsCoverageMetadata.getNilValues().get(0).size() > 0) {
                            nodataValues.add(new BigDecimal(wcpsCoverageMetadata.getNilValues().get(0).get(0).getValue()));
                        }
                    }
                }
                
                List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
                String nativeCRS = xyAxes.get(0).getNativeCrsUri();
                
                // Apply style if necessary on the geo subsetted coverage expressions and translate to Rasql collection expressions
                String collectionExpressionLayer = this.createCollectionExpressionsLayer(styleName, wcpsCoverageMetadata, wmsLayer);
                
                // e.g: (c + 1)[0:20, 30:45]
                String subsetCollectionExpression = SUBSET_COVERAGE_EXPRESSION_TEMPLATE.replace(COLLECTION_EXPRESSION_TEMPLATE, collectionExpressionLayer);
                String finalCollectionExpressionLayer;
                
                if (!isProjection) {
                    finalCollectionExpressionLayer = this.wmsGetMapSubsetTranslatingService.createGridScalingOutputNonProjection(subsetCollectionExpression,
                                                                                            wmsLayer, this.originalRequestBBox, outputCRS);
                } else {
                    finalCollectionExpressionLayer = this.wmsGetMapSubsetTranslatingService.createGridScalingOutputProjection(nativeCRS, subsetCollectionExpression,
                                                                                            wmsLayer, this.originalRequestBBox, outputCRS, 
                                                                                            this.interpolation);
                }
                
                finalCollectionExpressions.add( " ( " + finalCollectionExpressionLayer + " ) ");
                styleIndex++;
            }
            
            // Now, create the final Rasql query from all WMS layers
            // NOTE: WMS 1.3.0 - A WMS shall render the requested layers by drawing the leftmost in the list bottommost, the next one over that, and so on. 
            String finalCollectionExpressionLayers = "";
            for (int i = 0; i < finalCollectionExpressions.size(); i++) {
                finalCollectionExpressionLayers += "( " + finalCollectionExpressions.get(i) + " )";
                if (i != finalCollectionExpressions.size() - 1) {
                    finalCollectionExpressionLayers += " " + OVERLAY + " ";
                }
            }

            String formatType = MIMEUtil.getFormatType(this.format);
            String collections = this.wmsGetMapStyleService.builRasqlFromExpression(this.width, this.height);
            
            WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadatas.get(0);
            
            if (finalCollectionExpressionLayers.contains(TRANSPARENT_DOMAIN)) {
                // NOTE: if the layer returns a transparent image, then it needs to set the null value to 0
                this.transparent = true;
            }
            String encodeFormatParameters = this.createEncodeFormatParameters(nodataValues, wcpsCoverageMetadata);
            
            // Create the final Rasql query for all layers's styles of this GetMap request.
            finalRasqlQuery = FINAL_TRANSLATED_RASQL_TEMPLATE
                                .replace(COLLECTION_EXPRESSION_TEMPLATE, finalCollectionExpressionLayers)
                                .replace(ENCODE_FORMAT_PARAMETERS_TEMPLATE, encodeFormatParameters)
                                .replace(FORMAT_TYPE_TEMPLATE, formatType)
                                .replace(COLLECTIONS_TEMPLATE, collections);
            
            if (collections.isEmpty()) {
                finalRasqlQuery = finalRasqlQuery.replace("FROM ", "");
            }
            
            Pair<String, String> userPair = AuthenticationService.getBasicAuthCredentialsOrRasguest(httpServletRequest);
            bytes = RasUtil.getRasqlResultAsBytes(finalRasqlQuery, userPair.fst, userPair.snd);
        } catch (PetascopeException ex) {
            if (ex instanceof RasdamanException) {
                throw new PetascopeException(ex.getExceptionCode(), "Failed to run query: " + finalRasqlQuery + ". Reason: " + ex.getMessage(), ex);
            } else {
                throw new WMSInternalException(ex.getMessage(), ex);
            }
        }

        return new Response(Arrays.asList(bytes), this.format, this.layerNames.get(0));
    }
    
    /**
     * Return a list of WcpsCoverageMetadata objects from the request layer names
     */
    private List<WcpsCoverageMetadata> getWcpsCoverageMetadataByLayernames() throws PetascopeException {
        List<WcpsCoverageMetadata> wcpsCoverageMetadatas = new ArrayList<>();
        
        for (String layerName : this.layerNames) {
            WcpsCoverageMetadata wcpsCoverageMetadata = this.wmsGetMapWCPSMetadataTranslatorService.translate(layerName);
            wcpsCoverageMetadatas.add(wcpsCoverageMetadata);
        }
        
        return wcpsCoverageMetadatas;
    }
    
    /**
     * Return a list of WMSLayer objects from the requested layers
     */
    private List<WMSLayer> createWMSLayers(List<WcpsCoverageMetadata> wcpsCoverageMetadatas) throws PetascopeException {
        
        List<WMSLayer> wmsLayers = new ArrayList<>();
        
        for (WcpsCoverageMetadata wcpsCoverageMetadata : wcpsCoverageMetadatas) {

            String layerName = wcpsCoverageMetadata.getCoverageName();
            // e.g. here layer is S2_L2A_TCI_10m
            WMSLayer wmsLayer = this.wmsGetMapWCPSMetadataTranslatorService.createWMSLayer(
                                                                                layerName, this.layerRequestCRSBBoxesMap.get(layerName),
                                                                                this.fittedRequestBBoxesMap.get(layerName),
                                                                                this.extendedFittedRequestGeoBBoxesMap.get(layerName),
                                                                                this.width, this.height,
                                                                                this.nonXYSubsetDimensions
                                                                            );
            wmsLayers.add(wmsLayer);
        }
        
        return wmsLayers;
    }
    
    /**
     * In case of requesting different CRS (e.g: EPSG:4326) than layer's native CRS (e.g: UTM 32) for XY axes, then
     * transform the layer's geo XY bounds from UTM 32 to ESPG:4326
     */
    private void transformNativeCRSBBoxToRequestCRS(String layerName, List<Axis> xyAxesNativeCRS) throws PetascopeException {
        Axis axisX = xyAxesNativeCRS.get(0);
        Axis axisY = xyAxesNativeCRS.get(1);
        String nativeCRS = axisX.getNativeCrsUri();
        
        BigDecimal xMin = axisX.getGeoBounds().getLowerLimit();
        BigDecimal yMin = axisY.getGeoBounds().getLowerLimit();
        BigDecimal xMax = axisX.getGeoBounds().getUpperLimit();
        BigDecimal yMax = axisY.getGeoBounds().getUpperLimit();

        // e.g: native layer's CRS: UTM 32 to request CRS: EPSG:4326        
        BoundingBox sourceCRSBBOX = new BoundingBox(xMin, yMin, xMax, yMax);
        BoundingBox targetCRSBBox = CrsProjectionUtil.transformBBox(sourceCRSBBOX, nativeCRS, this.outputCRS);
        
        this.layerRequestCRSBBoxesMap.put(layerName, targetCRSBBox);
    }
    
    /**
     * Check if the request should need an extended geo XY BBox
     * (especially in case of requesting in different CRS to layer's native geo XY CRS)
     */
    private boolean needExtendedGeoXYBBox(WMSLayer wmsLayer) throws PetascopeException {
        BoundingBox bbox = this.layerRequestCRSBBoxesMap.get(wmsLayer.getLayerName());
        // If request BBox contains the layer (layer is inside the request BBox)
        // then no point to create extended request geo BBox as there are no more pixels to fill gaps
        
        boolean result = ((isProjection 
                ) 
                && wmsLayer.getOriginalBoundsBBox().intersectsXorYAxis(bbox));
        
        return result;
    }
    
    /**
     * Create a list of Rasql collection expressions' string representations for a specific layer.
     * 
     */
    private String createCollectionExpressionsLayer(String styleName,          
                                                    WcpsCoverageMetadata wcpsCoverageMetadata,
                                                    WMSLayer wmsLayer) 
            throws PetascopeException, WMSStyleNotFoundException, WCPSException {
        
        String layerName = wmsLayer.getLayerName();
        List<String> coverageExpressionsLayer = new ArrayList<>();
            
        // CoverageExpression is the main part of a Rasql query builded from the current layer and style
        // e.g: c1 + 5, case c1 > 5 then {0, 1, 2}
        String collectionExpression = null;
        Layer layer = this.wmsRepostioryService.readLayerByName(layerName);
        
        if (!StringUtils.isEmpty(styleName) && layer.getStyle(styleName) == null) {
            throw new WMSStyleNotFoundException(styleName, layerName);
        } 

        if (this.needExtendedGeoXYBBox(wmsLayer) 
            ) {
            BoundingBox bbox = this.wmsGetMapBBoxService.createExtendedGeoBBox(wmsLayer);
            this.extendedFittedRequestGeoBBoxesMap.put(layerName, bbox);
            wmsLayer.setExtendedRequestBBox(bbox);
        }
        
        List<List<WcpsSliceSubsetDimension>> nonXYGridSliceSubsetDimensions = this.wmsGetMapSubsetParsingService.translateGridDimensionsSubsetsLayers(wcpsCoverageMetadata, dimSubsetsMap);

        Style style = null;
        if (styleName == null) {
            // GetMap request without style specified, then if layer has at least one style, the default style is the first, else null
            style = layer.getDefaultStyle();
        } else {
            // GetMap request with style specified
            style = layer.getStyle(styleName);            
        }
        
        if (style == null) {
            // Layer has no style
            String styleQuery = FRAGMENT_ITERATOR_PREFIX + layerName;
            collectionExpression = this.wmsGetMapStyleService.buildRasqlStyleExpressionForRasqFragment(styleQuery, layerName,                                                                        
                                                                        wmsLayer, wcpsCoverageMetadata,
                                                                        nonXYGridSliceSubsetDimensions,
                                                                        extendedFittedRequestGeoBBoxesMap.get(layerName));
        } else {
            if (!StringUtils.isEmpty(style.getRasqlQueryFragment())) {
                // rasqlTransformFragment
                // e.g: $Iterator -> $covA
                String styleQuery = style.getRasqlQueryFragment().replace(RASQL_FRAGMENT_ITERATOR, FRAGMENT_ITERATOR_PREFIX + layerName);
                collectionExpression = this.wmsGetMapStyleService.buildRasqlStyleExpressionForRasqFragment(styleQuery, layerName,
                                                                        wmsLayer, wcpsCoverageMetadata,
                                                                        nonXYGridSliceSubsetDimensions,
                                                                        extendedFittedRequestGeoBBoxesMap.get(layerName));
            } else if (!StringUtils.isEmpty(style.getWcpsQueryFragment())) {
                // wcpsQueryFragment
                // e.g: $c -> $covA
                String styleQuery = style.getWcpsQueryFragment().replace(WCPS_FRAGMENT_ITERATOR, FRAGMENT_ITERATOR_PREFIX + layerName);
                collectionExpression = this.wmsGetMapStyleService.buildRasqlStyleExpressionForWCPSFragment(
                                                                        styleQuery, layerName,
                                                                        wmsLayer, wcpsCoverageMetadata,
                                                                        nonXYGridSliceSubsetDimensions,
                                                                        extendedFittedRequestGeoBBoxesMap.get(layerName));
            } else {
                // Style is not null, but no query fragment was defined, e.g: only contains colorTable value
                String styleQuery = FRAGMENT_ITERATOR_PREFIX + layerName;
                collectionExpression = this.wmsGetMapStyleService.buildRasqlStyleExpressionForRasqFragment(styleQuery, layerName,
                                                                        wmsLayer, wcpsCoverageMetadata,
                                                                        nonXYGridSliceSubsetDimensions,
                                                                        extendedFittedRequestGeoBBoxesMap.get(layerName));                
            }

            if (!StringUtils.isEmpty(style.getColorTableDefinition())) {
                if (colorTableDefinition == null) {
                    if (style.getColorTableType() != null) {
                        colorTableTypeCode = style.getColorTableType();
                    }
                    if (style.getColorTableDefinition() != null) {
                        colorTableDefinition = style.getColorTableDefinition();
                    }
                }
            }
        }

        // Add the translated Rasql query for the style to combine later
        coverageExpressionsLayer.add("( " + collectionExpression + " )");
        
        String result = ListUtil.join(coverageExpressionsLayer, OVERLAY);
        return result;
    }
    
    /**
     * Check if request BBox in native CRS intersects with first layer's BBox.
     */
    private boolean intersectLayerXYBBox() throws PetascopeException {
        // Check if the request BBox (e.g: in EPSG:4326) intersects with layer's BBox (e.g: in UTM 32)
        for (String layerName : this.layerNames) {
            BoundingBox bbox = this.layerRequestCRSBBoxesMap.get(layerName);
            if (bbox.intersectsXorYAxis(this.originalRequestBBox)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * From the list of nodata values from all combined layers, create a nodata string for SELECT encode() query.
     * NOTE: if layer was imported with lat, long grid axes order (e.g: via netCDF) then it must need tranpose to make the output correctly.
     * 
     */
    private String createEncodeFormatParameters(List<BigDecimal> nodataValues, WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        
        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
        
        ObjectMapper objectMapper = new ObjectMapper();        
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        
        JsonExtraParams jsonExtraParams = new JsonExtraParams();

        if (this.transparent) {
            if (nodataValues.isEmpty()) {
                // 0 is default null value if layer doesn't have other values
                nodataValues.add(BigDecimal.ZERO);
            }
            // e.g: {"nodata": [10, 20, 40]}
            NoData nodata = new NoData();
            nodata.setNoDataValues(nodataValues);
            
            jsonExtraParams.setNoData(nodata);
        }
        
        if (xyAxes.get(0).getRasdamanOrder() > xyAxes.get(1).getRasdamanOrder()) {
            // NOTE: if layer imported with Lat, Long grid order (via netCDF) so it needs to use transpose
            List<Integer> transposeList = new ArrayList<>();
            transposeList.add(0);
            transposeList.add(1);
            jsonExtraParams.setTranspose(transposeList);
        }
        
        CoverageMetadata coverageMetadata = wcpsCoverageMetadata.getCoverageMetadata();
        
        TranslateColorTableService.translate(colorTableTypeCode, colorTableDefinition, jsonExtraParams);
        
        SerializationEncodingService.addColorPalleteToJSONExtraParamIfPossible(this.format, coverageMetadata, jsonExtraParams);
        
        String encodeFormatParameters = JSONUtil.serializeObjectToJSONStringNoIndentation(jsonExtraParams);
        encodeFormatParameters = encodeFormatParameters.replace("\"", "\\\"");
        
        return encodeFormatParameters;
    }

    /**
     * According to WMS standard, if a request with BBox does not intersect with layer's BBox,
     * instead of throwing exception (which will show error requests in WebBrowser's consoles),
     * petascope returns an blank result instead.
     */
    private Response createBlankImage() throws PetascopeException {
        String key = this.width + "_" + this.height;
        Response response = this.blankTileMap.get(key);
        
        if (response == null) {
            // Create a transparent image by input width and height parameters
            String query = SELECT + ENCODE + "(" + EXTEND + "(" + TRANSPARENT_DOMAIN 
                         + ", [0:" + (this.width - 1) + ",0:" + (this.height - 1) + "]) , \"" 
                         + this.format + "\", \"{\\\"nodata\\\": [" + DEFAULT_NULL_VALUE + "]}\") ";
            
            Pair<String, String> userPair = AuthenticationService.getBasicAuthCredentialsOrRasguest(httpServletRequest);
            byte[] bytes = RasUtil.getRasqlResultAsBytes(query, userPair.fst, userPair.snd);
            response = new Response(Arrays.asList(bytes), this.format, this.layerNames.get(0));
            this.blankTileMap.put(key, response);
        }
        
        return response;       
    }
}
