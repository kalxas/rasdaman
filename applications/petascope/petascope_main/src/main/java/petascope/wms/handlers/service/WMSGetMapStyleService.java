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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.BoundingBox;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsUtil;
import petascope.util.JSONUtil;
import petascope.util.ListUtil;
import static petascope.wcps.handler.ForClauseHandler.AS;
import static petascope.wcps.handler.ForClauseListHandler.FROM;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.CollectionAliasRegistry;
import petascope.wcps.metadata.service.CoverageAliasRegistry;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.WcpsSliceSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;
import petascope.wcs2.handlers.kvp.KVPWCSProcessCoverageHandler;
import petascope.wms.handlers.model.WMSLayer;
import static petascope.wms.handlers.service.WMSGetMapService.OVERLAY;

/**
 * Utility for handling WMS styles in WMS GetMap request
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class WMSGetMapStyleService {
    
    @Autowired
    private WMSGetMapSubsetTranslatingService wmsGetMapSubsetTranslatingService;
    @Autowired
    private WMSGetMapBBoxService wmsGetMapBBoxService;
    @Autowired
    private KVPWCSProcessCoverageHandler kvpWCSProcessCoverageHandler;
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    
    @Autowired
    private CollectionAliasRegistry collectionAliasRegistry;
    @Autowired
    private CoverageAliasRegistry coverageAliasRegistry;
    
    // -- rasdaman enteprise begin
    
    public static final String WMS_VIRTUAL_LAYER_EXPECTED_BBOX = "BBOX";
    public static final String WMS_VIRTUAL_LAYER_EXPECTED_WIDTH = "WIDTH";
    public static final String WMS_VIRTUAL_LAYER_EXPECTED_HEIGHT = "HEIGHT";
    
    // -- rasdaman enterprise end
    
    public static final String FRAGMENT_ITERATOR_PREFIX = "$";
    private static final String COLLECTION_ITERATOR = "c";
    public static final String WCPS_FRAGMENT_ITERATOR = FRAGMENT_ITERATOR_PREFIX + COLLECTION_ITERATOR;
    public static final String RASQL_FRAGMENT_ITERATOR = FRAGMENT_ITERATOR_PREFIX + "Iterator";
    
    public static final Pattern LAYER_ITERATOR_PATTERN = Pattern.compile("\\" + FRAGMENT_ITERATOR_PREFIX + "([a-zA-Z0-9_][a-zA-Z0-9_\\.-]*:([0-9]+:)?)?[a-zA-Z_][a-zA-Z0-9_]*");
    public static final String USING = " using ";
    public static final String IN = " IN ";
    public static final String SELECT = " SELECT ";
    public static final String FOR = " FOR ";
    public static final String RETURN = " RETURN ";
    public static final String ENCODE = " ENCODE ";
    public static final String ENCODE_PNG = "\"png\"";
    public static final String SCALE = " SCALE ";
    public static final String EXTEND = " EXTEND ";
    public static final String AS = " AS ";
    
    @Autowired
    private WMSGetMapWCPSMetadataTranslatorService wmsGetMapWCPSMetadataTranslatorService;
    
    private static Logger log = LoggerFactory.getLogger(WMSGetMapStyleService.class);
    
    
    /**
     * From a rasql style expression with layer name iterator, return a rasql expression for it
     * e.g: $covA + $covB -> collectionA[0:10,0:20] + collectionB[0:20,0:40]
     */
    public String buildRasqlStyleExpressionForRasqFragment(String styleQuery, String layerName, WMSLayer wmsLayer,
                                                WcpsCoverageMetadata wcpsCoverageMetadata,
                                                List<List<WcpsSliceSubsetDimension>> nonXYGridSliceSubsetDimensions, 
                                                BoundingBox extendedFittedRequestGeoBBox) 
                                                throws PetascopeException {
        
        // e.g: $covA -> collectionA[0:20,30:50]
        Map<String, String> layerNameIteratorsCollectionExpressionsMap = new LinkedHashMap<>();
        Set<String> layerNameIterators = this.parseLayerNameIteratorsFromStyleExpression(styleQuery);
        
        for (String layerNameIterator : layerNameIterators) {
            
            List<String> coverageExpressions = new ArrayList<>();
            String coverageExpression = null;
            
            if (nonXYGridSliceSubsetDimensions == null) {
                // e.g: layer is 2D with XY axes only
                coverageExpression = this.getCoverageExpressionForRasqlFragment(layerNameIterator, wmsLayer, extendedFittedRequestGeoBBox, 
                                                           wcpsCoverageMetadata, null);
                coverageExpressions.add(coverageExpression);
            } else {
                for (List<WcpsSliceSubsetDimension> wcpsSliceSubsetDimensions : nonXYGridSliceSubsetDimensions) {
                    coverageExpression = this.getCoverageExpressionForRasqlFragment(layerNameIterator, wmsLayer, extendedFittedRequestGeoBBox, 
                                                           wcpsCoverageMetadata, wcpsSliceSubsetDimensions);
                    coverageExpressions.add(coverageExpression);
                }
            }
            
            layerNameIteratorsCollectionExpressionsMap.put(layerNameIterator, ListUtil.join(coverageExpressions, OVERLAY));
        }
        
        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
        String result = this.replaceLayerNameIteratorsByLayerExpressions(styleQuery, layerNameIteratorsCollectionExpressionsMap,
                                                                         extendedFittedRequestGeoBBox, xyAxes.get(0), xyAxes.get(1));
        return result;        
    }
    
    /**
     * From a wcps style expression with layer name iterator, return a rasql expression for it
     * e.g: $covA + $covB -> collectionA[0:10,0:20] + collectionB[0:20,0:40]
     */
    public String buildRasqlStyleExpressionForWCPSFragment(String styleQuery, String layerName, WMSLayer wmsLayer,
                                                           WcpsCoverageMetadata wcpsCoverageMetadata,
                                                           List<List<WcpsSliceSubsetDimension>> nonXYGridSliceSubsetDimensions,                                                           
                                                           BoundingBox extendedFittedRequestGeoBBox) 
                                                           throws PetascopeException {
        
        this.wmsGetMapBBoxService.fitBBoxToCoverageGeoXYBounds(wmsLayer.getRequestBBox(), wmsLayer.getLayerName());
        this.wmsGetMapBBoxService.fitBBoxToCoverageGeoXYBounds(wmsLayer.getExtendedRequestBBox(), wmsLayer.getLayerName());
        
        WcpsCoverageMetadata wcpsCoverageMetadataTmp = (WcpsCoverageMetadata) JSONUtil.clone(wcpsCoverageMetadata);
        List<WcpsSubsetDimension> wcpsSubsetDimensions = this.wmsGetMapSubsetTranslatingService.parseWcpsSubsetDimensions(wcpsCoverageMetadataTmp, 
                                                                                                               wmsLayer.getExtendedRequestBBox());
        // First, apply the XY subsets from request BBOX which will return proper geo/grid domains on XY axes
        WcpsResult wcpsResult = this.wmsGetMapSubsetTranslatingService.applyWCPSGeoSubsets(wcpsCoverageMetadata, 
                                                                                wcpsSubsetDimensions);

        List<Axis> xyAxes = wcpsResult.getMetadata().getXYAxes();
        Axis axisX = xyAxes.get(0);
        Axis axisY = xyAxes.get(1);

        // Then, it needs to update the request geo XY BBOX in native CRS with the translated results by WCPS 
        this.updateFittedBBoxByXYAxes(extendedFittedRequestGeoBBox, axisX, axisY);
        
        Map<String, String> layerNameIteratorsCoverageExpressionsMap = new LinkedHashMap<>();
        List<String> coverageAliasList = new ArrayList<>();
        
        Set<String> layerNameIterators = this.parseLayerNameIteratorsFromStyleExpression(styleQuery);
        int numberOfCollectionVariables = this.collectionAliasRegistry.getAliasMap().size();
        int i = numberOfCollectionVariables - 1;
        for (String layerNameIterator : layerNameIterators) {
            
            List<String> coverageExpressions = new ArrayList<>();
            String coverageExpression = null;
            
            String coverageId = this.stripDollarSign(layerNameIterator);
            // e.g: c0 in (covA)
            String coverageAlias = this.coverageAliasRegistry.getCoverageAliasMap().get(coverageId);
            
            if (coverageAlias == null) {
                coverageAlias = COLLECTION_ITERATOR + i;
            }
            
            coverageAliasList.add(coverageAlias + IN + "(" + coverageId + ")");
            String coverageSubset = coverageAlias + "[ ";
            
            if (nonXYGridSliceSubsetDimensions == null) {
                coverageExpression = this.getCoverageExpressionForWCPSFragment(wmsLayer, coverageSubset, wcpsCoverageMetadataTmp, null);
                coverageExpressions.add(coverageExpression);
            } else {
                for (List<WcpsSliceSubsetDimension> wcpsSliceSubsetDimensions : nonXYGridSliceSubsetDimensions) {
                    coverageExpression = this.getCoverageExpressionForWCPSFragment(wmsLayer, coverageSubset, wcpsCoverageMetadataTmp, wcpsSliceSubsetDimensions);
                    coverageExpressions.add(coverageExpression);
                }
            }
            
            layerNameIteratorsCoverageExpressionsMap.put(layerNameIterator,  ListUtil.join(coverageExpressions, OVERLAY));
            i++;
        }
        
        String forClauseWCPSQuery = ListUtil.join(coverageAliasList, ", ");
        String mainWCPSQuery = this.replaceLayerNameIteratorsByLayerExpressions(styleQuery, layerNameIteratorsCoverageExpressionsMap, extendedFittedRequestGeoBBox, axisX, axisY);
        
        // This WCPS query is created temporarily to extract the main rasql content to be used later
        String wcpsQuery = FOR + forClauseWCPSQuery + RETURN + ENCODE + "(" + mainWCPSQuery + ", " + ENCODE_PNG + ")";
        
        log.debug("Generated WCPS query for WCPS fragment style: " + wcpsQuery);
        String rasqlTmp = this.kvpWCSProcessCoverageHandler.buildRasqlQuery(wcpsQuery);
        String mainRasqlQuery = rasqlTmp.substring(rasqlTmp.indexOf("encode(") + 7, rasqlTmp.indexOf(", " + ENCODE_PNG));
        
        mainRasqlQuery = this.parseTranslatedRasqlForWCPSFragmentToCollectionsRegistry(mainRasqlQuery, rasqlTmp, wcpsCoverageMetadataTmp, 
                                                                                       extendedFittedRequestGeoBBox);
                    
        return mainRasqlQuery;
    }
    
    /**
     * Return a translated coverage expression as rasdaman subquery for a WMS style by Rasql fragment
     */
    private String getCoverageExpressionForRasqlFragment(String layerNameIterator, 
                                                         WMSLayer wmsLayer, 
                                                         BoundingBox extendedFittedRequestGeoBBox,
                                                         WcpsCoverageMetadata wcpsCoverageMetadata,
                                                         List<WcpsSliceSubsetDimension> nonXYGridSliceSubsetDimensions) throws PetascopeException {
        
        this.wmsGetMapBBoxService.fitBBoxToCoverageGeoXYBounds(wmsLayer.getRequestBBox(), wmsLayer.getLayerName());
        this.wmsGetMapBBoxService.fitBBoxToCoverageGeoXYBounds(wmsLayer.getExtendedRequestBBox(), wmsLayer.getLayerName());
        
        WcpsCoverageMetadata wcpsCoverageMetadataTmp = (WcpsCoverageMetadata) JSONUtil.clone(wcpsCoverageMetadata);
        List<WcpsSubsetDimension> wcpsSubsetDimensions = this.wmsGetMapSubsetTranslatingService.parseWcpsSubsetDimensions(wcpsCoverageMetadata, 
                                                                                                           wmsLayer.getExtendedRequestBBox());
        if (nonXYGridSliceSubsetDimensions != null) {
            // Only for 3rd+ layers
            wcpsSubsetDimensions.addAll(nonXYGridSliceSubsetDimensions);
        }

        wmsLayer.setLayerName(this.stripDollarSign(layerNameIterator));        


        WcpsResult wcpsResult = this.wmsGetMapSubsetTranslatingService.applyWCPSGeoSubsets(wcpsCoverageMetadataTmp, 
                                                                                wcpsSubsetDimensions);
 
        // Then, it needs to update the request geo XY BBOX in native CRS with the translated results by WCPS 
        List<Axis> xyAxes = wcpsResult.getMetadata().getXYAxes();
        Axis axisX = xyAxes.get(0);
        Axis axisY = xyAxes.get(1);
        
        this.updateFittedBBoxByXYAxes(extendedFittedRequestGeoBBox, axisX, axisY);
        
        String collectionExpression = wcpsResult.getRasql();

        if (this.needExtendedGridXYBBox(wcpsCoverageMetadataTmp, extendedFittedRequestGeoBBox, wmsLayer.getRequestBBox())) {
            Pair<String, String> extendedXYGridDomainsPair = this.createExtendedXYGridDomains(wcpsCoverageMetadataTmp, extendedFittedRequestGeoBBox);
            collectionExpression += "[" + extendedXYGridDomainsPair.fst + "," + extendedXYGridDomainsPair.snd + "]";
        }
        
        return collectionExpression;
    }
    
    /**
     * Return a translated coverage expression as rasdaman subquery for a WMS style by WCPS fragment
     */
    private String getCoverageExpressionForWCPSFragment(WMSLayer wmsLayer, 
                                                        String coverageSubset,
                                                        WcpsCoverageMetadata wcpsCoverageMetadata,
                                                        List<WcpsSliceSubsetDimension> nonXYGridSliceSubsetDimensions) throws PetascopeException {
        
        this.wmsGetMapBBoxService.fitBBoxToCoverageGeoXYBounds(wmsLayer.getRequestBBox(), wmsLayer.getLayerName());
        this.wmsGetMapBBoxService.fitBBoxToCoverageGeoXYBounds(wmsLayer.getExtendedRequestBBox(), wmsLayer.getLayerName());
        
        List<WcpsSubsetDimension> wcpsSubsetDimensions = this.wmsGetMapSubsetTranslatingService.parseWcpsSubsetDimensions(wcpsCoverageMetadata, 
                                                                                                               wmsLayer.getExtendedRequestBBox());
        
        if (nonXYGridSliceSubsetDimensions != null) {
            // Only for 3rd+ layers
            wcpsSubsetDimensions.addAll(nonXYGridSliceSubsetDimensions);
        }        
        
        List<String> subsets = new ArrayList<>();
        for (WcpsSubsetDimension wcpsSubsetDimension : wcpsSubsetDimensions) {
            if (wcpsSubsetDimension instanceof WcpsTrimSubsetDimension) {
                String subset = wcpsSubsetDimension.getAxisName() + "(" 
                               + ((WcpsTrimSubsetDimension) wcpsSubsetDimension).getLowerBound() + ":" 
                               + ((WcpsTrimSubsetDimension) wcpsSubsetDimension).getUpperBound() + ")";

                subsets.add(subset);
            } else {
                // NOTE: only slicing on non-XY axes and they are already translated to grid coordinate
                String subset = wcpsSubsetDimension.getAxisName() 
                              + ":\"" + CrsUtil.GRID_CRS + "\"(" + ((WcpsSliceSubsetDimension)wcpsSubsetDimension).getBound() + ")";
                subsets.add(subset);
            }
        }

        // e.g: $covA -> $covA[Lat(0:30), Long(0:40)]
        coverageSubset += ListUtil.join(subsets, ", ") + " ]";
        
        return coverageSubset;
    }
    
    /**
     * Parse the translated rasql for WCPS fragment and add it to the registry for alias and collections
     */
    private String parseTranslatedRasqlForWCPSFragmentToCollectionsRegistry(String mainRasqlQuery, String rasqlTmp,
                                                     WcpsCoverageMetadata wcpsCoverageMetadata,                                                     
                                                     BoundingBox extendedFittedRequestGeoBBox) throws PetascopeException {
        // NOTE: in case a style containing clip(), the result of clip() needs to be extended() to the selected grid domains as it is smaller
        if (mainRasqlQuery.toLowerCase().contains("clip")) {
            Pair<String, String> extendedXYGridDomainsPair = this.createExtendedXYGridDomains(wcpsCoverageMetadata, extendedFittedRequestGeoBBox);
            String extendDomain = "[" + extendedXYGridDomainsPair.fst + ", " + extendedXYGridDomainsPair.snd + "]";
            mainRasqlQuery = EXTEND + "( " + mainRasqlQuery + ", " + extendDomain + ")";
        }
        
        // Parse rasql from FROM clause for aliases and collections
        if (rasqlTmp.indexOf(FROM) > 0) {
            String fromClauseRasqlQuery = rasqlTmp.substring(rasqlTmp.indexOf(FROM) + 4, rasqlTmp.length()).trim();
            String[] tmps = fromClauseRasqlQuery.split(",");
            for (String tmp : tmps) {
                // e.g: collectionA AS c0
                String[] tmp1 = tmp.split(AS);
                String collectionName = tmp1[0];
                String alias = tmp1[1];

                String layerName = wcpsCoverageMetadata.getCoverageName();
                this.collectionAliasRegistry.add(alias, collectionName, layerName);
            }
        }
        
        return mainRasqlQuery;
    }
    
    /**
     *  From the Set of coverage (layer) names, return 
     * e.g: Sentinel2_B4 AS c0, Sentienl2_B8 as c1
     */
    public String builRasqlFromExpression(int width, int height) 
                 throws PetascopeException {
        
        List<String> collectionAlias = new ArrayList<>();
        for (Map.Entry<String, Pair<String, String>> entryTmp : this.collectionAliasRegistry.getAliasMap().entrySet()) {
            String alias = entryTmp.getKey();
            String sourceCollectionName = entryTmp.getValue().fst;

            collectionAlias.add(sourceCollectionName + AS + alias);
        }
        
        String result = ListUtil.join(collectionAlias, ", ");
        return result;
    }
    
    /**
     * Return all layer names from a style expression
     * e.g: $covA + $covB - $coVA - $covC
     * returns: $covA, $covB, $covC
     */
    private Set<String> parseLayerNameIteratorsFromStyleExpression(String styleExpression) throws PetascopeException {
        Set<String> layerNameIterators = new LinkedHashSet<>();
        Matcher matcher = LAYER_ITERATOR_PATTERN.matcher(styleExpression);
        while (matcher.find()) {
            // e.g: $c or $Iterator or $COVERAGE_ID (e.g: $EU_DEM)
            String layerNameIterator = matcher.group();
            if (layerNameIterator.equals(WCPS_FRAGMENT_ITERATOR) 
                    || layerNameIterator.equals(RASQL_FRAGMENT_ITERATOR)
                    || this.coverageRepositoryService.isInCache(layerNameIterator.replace(FRAGMENT_ITERATOR_PREFIX, ""))) {
                layerNameIterators.add(layerNameIterator);                
            }
        }
        
        return layerNameIterators;
    }
    
    /**
     * e.g: $covA -> covA
     */
    private String stripDollarSign(String input) {
        return input.replace(FRAGMENT_ITERATOR_PREFIX, "");
    }
    
    /**
     * Check if bounding was extended
     */
    private boolean extendedBBox(BoundingBox fittedBBox, BoundingBox extendedFittedBBox) {
        // e.g: 10 > 8
        return fittedBBox.getXMin().compareTo(extendedFittedBBox.getXMin()) > 0; 
    }
    
    /**
     * Check if the result should need an extended grid XY BBox
     * (especially in case of requesting in different CRS to layer's native geo XY CRS)
     */
    private boolean needExtendedGridXYBBox(WcpsCoverageMetadata wcpsCoverageMetadata,
                                           BoundingBox fittedBBox, BoundingBox extendedFittedBBox) throws PetascopeException {
        return this.extendedBBox(fittedBBox, extendedFittedBBox);
    }
    
    /**
     * Replace the layer names in the style expression with correspondent collection expressions
     * e.g: $covA + 5 -> collectionA[0:10,0:20] + 5
     */
    private String replaceLayerNameIteratorsByLayerExpressions(String styleExpression, Map<String, String> map, 
                                                               BoundingBox fittedBBox,
                                                               Axis axisX,
                                                               Axis axisY) {
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = LAYER_ITERATOR_PATTERN.matcher(styleExpression);
        while (matcher.find()) {
            // e.g: $COV
            String layerNameIterator = matcher.group();
            String expression = map.get(layerNameIterator);
            
            if (expression != null) {
                // In case of WMS style contains condenser
                if (styleExpression.contains("[")) {
                    expression = expression.substring(0, expression.indexOf("["));
                }
                matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(expression));
            }
        }
        
        matcher.appendTail(stringBuffer);
        String result = stringBuffer.toString();
        
        // In case of WMS style contains condenser or fixed subsets
        if (styleExpression.contains("[")) {
            String geoXYSubsets = axisX.getLabel() + "(" + fittedBBox.getXMin() + ":" + fittedBBox.getXMax() + "), "
                                + axisY.getLabel() + "(" + fittedBBox.getYMin() + ":" + fittedBBox.getYMax() + ")";
            
            int indexOfUsing = result.toLowerCase().indexOf(USING);
            if (indexOfUsing != -1) {
                indexOfUsing = indexOfUsing + 5;
                String firstPart = result.substring(0, indexOfUsing);
                String secondPart = result.substring(indexOfUsing, result.length());
                // e.g: $CoV[ansi($ts)] -> $COV[Lat(0:20), Long(20:30), ansi($ts)]
                result = firstPart + secondPart.replace("[", "[" + geoXYSubsets + ", ");
            } else {
                // e.g. style = $c[ansi("2020-12-30T23:54:58.500Z")] -  $c[ansi("2021-01-04T00:15:04.500Z")] + 30
                result = result.replace("[", "[" + geoXYSubsets + ", ");
            }
        }

        return result;
        
    }
    
    /**
     * Create a pair of extended grid domains for XY geo axes
     */
    private Pair<String, String> createExtendedXYGridDomains(WcpsCoverageMetadata wcpsCoverageMetadata,
                                                            BoundingBox fittedBBox) throws PetascopeException {
        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
        Axis axisX = xyAxes.get(0);
        Axis axisY = xyAxes.get(1);

        BoundingBox extendedFittedGridBBox = this.wmsGetMapBBoxService.createExtendedGridBBox(axisX, axisY, fittedBBox);
        String gridDomainX = extendedFittedGridBBox.getXMin() + ":" + extendedFittedGridBBox.getXMax();
        String gridDomainY = extendedFittedGridBBox.getYMin() + ":" + extendedFittedGridBBox.getYMax();
        
        return new Pair<>(gridDomainX, gridDomainY);
    }
    
    /**
     * Update fitted bbox by input X and Y axes
     */
    private void updateFittedBBoxByXYAxes(BoundingBox fittedBBox, Axis axisX, Axis axisY) {
        fittedBBox.setXMin(axisX.getGeoBounds().getLowerLimit());
        fittedBBox.setYMin(axisY.getGeoBounds().getLowerLimit());
        fittedBBox.setXMax(axisX.getGeoBounds().getUpperLimit());
        fittedBBox.setYMax(axisY.getGeoBounds().getUpperLimit());
    }
    
}
