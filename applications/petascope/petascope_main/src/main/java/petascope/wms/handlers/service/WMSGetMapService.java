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
package petascope.wms.handlers.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.rasdaman.domain.cis.NilValue;
import org.rasdaman.domain.wms.BoundingBox;
import org.rasdaman.domain.wms.Style;
import org.rasdaman.repository.service.CoverageRepostioryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.core.AxisTypes;
import petascope.core.CrsDefinition;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import petascope.core.service.CrsComputerService;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.exceptions.WCSException;
import petascope.exceptions.WMSException;
import petascope.util.CrsProjectionUtil;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.MIMEUtil;
import petascope.util.ras.RasUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.ParsedSubset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.CoordinateTranslationService;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;
import petascope.wcs2.handlers.kvp.KVPWCSProcessCoverageHandler;
import petascope.wcs2.parsers.subsets.TrimmingSubsetDimension;
import petascope.wms.exception.WMSInternalException;
import petascope.wms.handlers.model.TranslatedGridDimensionSubset;
import static petascope.core.KVPSymbols.VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER;
import petascope.core.Pair;
import petascope.exceptions.ExceptionCode;
import petascope.util.ras.RasConstants;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;
import static petascope.util.ras.RasConstants.RASQL_OPEN_SUBSETS;
import static petascope.util.ras.RasConstants.RASQL_CLOSE_SUBSETS;

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
 * Example Rasql query with overlaying 3 layers's styles from nativeCRS (EPSG:4326) to
 * BBox's CRS (EPSG:3857).
 *
 * SELECT ENCODE(project( Scale( ( ( CASE WHEN ( c0 > 1000 ) THEN ( {107,17,68}
 * ) ELSE ( {150,103,14} ) END ) OVERLAY ( case c1 when (c1 + 2) > 20 then
 * {255,0,0} when (c1 + 5 + 25 - 25) > 10+5 then {0,255,0} when
 * (2+c1+0.5-0.25*(0.5+5)) < 10-5+2 then {0,0,255} else {0,0,0} end ) OVERLAY ( CASE WHEN ( c2
 * > 10 ) THEN ( {107,17,68} ) ELSE ( {150,103,14} ) END ) )[0:221, 0:177],
 * [0:599, 0:599] ), "111.975000003801568482231232337653636932373046875,
 * -44.53346040109713754873155266977846622467041015625,
 * 156.251617152124168796945014037191867828369140625,
 * -8.9749973782681014000672803376801311969757080078125", "EPSG:4326",
 * "EPSG:3857"), "png", "") FROM test_wms_4326_2017_08_04_14_59_03_233 as c0,
 * test_wms_4326_2017_08_04_14_59_03_233 as c1, test_wms_4326_new as c2
 *
 * @author
 * <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WMSGetMapService {

    @Autowired
    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslator;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private CoordinateTranslationService coordinateTranslationService;
    @Autowired
    private KVPWCSProcessCoverageHandler kvpWCSProcessCoverageHandler;
    
    // In case of nativeCrs of layer (coverage) is different from outputCrs of GetMap request, then it needs to reproject $collectionExpression from sourceCrs to targetCrs.
    private static final String COLLECTION_EXPRESSION_TEMPLATE = "$collectionExpression";
    private static final String FORMAT_TYPE_TEMPLATE = "$formatType";
    private static final String NODATA_TEMPLATE = "$nodata";
    private static final String COLLECTIONS_TEMPLATE = "$collections";
    
    private static final String SUBSET_COVERAGE_EXPRESSION_TEMPLATE = "( " + COLLECTION_EXPRESSION_TEMPLATE + " )";
    private static final String FINAL_TRANSLATED_RASQL_TEMPLATE = "SELECT ENCODE(" + COLLECTION_EXPRESSION_TEMPLATE + ", "
                                                                + "\"" + FORMAT_TYPE_TEMPLATE + "\", \"" + NODATA_TEMPLATE + "\") FROM " + COLLECTIONS_TEMPLATE;
    private static final String OVERLAY = " OVERLAY ";
    private static final String ALIAS_NAME = "c";
    private static final String WCPS_COVERAGE_ALIAS = "$c";
    private static final String RASQL_FRAGMENT_ITERATOR = "$Iterator";
    
    private static final String PROJECTION_TEMPLATE = "project(" + COLLECTION_EXPRESSION_TEMPLATE + ", \"$xMin, $yMin, $xMax, $yMax\", \"$sourceCRS\", \"$targetCRS\")";

    private static Logger log = LoggerFactory.getLogger(WMSGetMapService.class);

    private List<String> layerNames;
    private List<String> styleNames;
    private String outputCRS;
    private int width;
    private int height;
    // MIME type (e.g: image/png)
    private String format;
    private boolean transparent;
    // BBox already translated from requesting CRS to native CRS of XY geo-referenced axes
    // NOTE: it needs to keep the original BBox for Extend() to display result correctly in WMS client
    private BoundingBox originalBBox;
    // NOTE: this fittedBBox is used to fit input BBox to coverage's geo XY axes' domains 
    // to avoid server killed by subsetting collection (e.g: c[-20:30] instead of c[0:30])
    private BoundingBox fittedBBbox;
    
    private Map<String, String> dimSubsetsMap = new HashMap<>();

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

    public void setBBoxes(BoundingBox bbox) throws PetascopeException, SecoreException {
        // If request is in YX order for bounding box (e.g: EPSG:4326 Lat, Long, swap it to XY order Long, Lat)
        // NOTE: as all layers requested with same outputCRS so only do this one time
        this.originalBBox = this.swapYXBoundingBox(bbox, outputCRS);
        this.fittedBBbox = this.swapYXBoundingBox(bbox, outputCRS);
        
        WcpsCoverageMetadata wcpsCoverageMetadata = this.createWcpsCoverageMetadataForDownscaledLevel(this.layerNames.get(0));
        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
        String nativeCRS = CrsUtil.getEPSGCode(xyAxes.get(0).getNativeCrsUri());
        if (!this.outputCRS.equalsIgnoreCase(nativeCRS)) {
            // First, transform from the request BBox in outputCrs to nativeCrs to calculate the grid bounds on the current coverages
            // e.g: coverage's nativeCRS is EPSG:4326 and request BBox which contains coordinates in EPSG:3857
            this.originalBBox = this.transformBoundingBox(this.originalBBox, this.outputCRS, nativeCRS);
            this.fittedBBbox = this.transformBoundingBox(this.fittedBBbox, this.outputCRS, nativeCRS);
        }
        
        // NOTE: to avoid error in rasql when server is killed from WMS client which sends request out of coverage's grid domains, 
        // adjust the bounding box to fit with coverage's grid XY axes' domains
        this.fitBBoxToCoverageGeoXYBounds();
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
    
    /**
     * Parse the dimension subsets (if exist) from GetMap request, e.g: time=...&dim_pressure=...
     * NOTE: WMS subset can be:
     * + Single value: e.g: dim_pressure=20
     * + Interval: e.g: dim_pressure=20/100
     * + List of values: e.g: dim_pressure=20,30,50
     * + Multiple intervals: e.g: dim_pressure=20/100,150/300
     */
    private List<ParsedSubset<BigDecimal>> parseDimensionSubset(Axis axis, String dimensionSubset) throws PetascopeException, SecoreException {
        List<ParsedSubset<BigDecimal>> parsedSubsets = new ArrayList<>();
        String[] parts = dimensionSubset.split(KVPSymbols.VALUE_WMS_SUBSET_SEPARATE_CHARACTER);
        
        for (String part : parts) {
            ParsedSubset<BigDecimal> parsedSubset;
            if (part.contains(KVPSymbols.VALUE_TIME_PATTERN_CHARACTER)) {
                // Time axis with datetime format, e.g: "2015-04-01T20:00:20Z")
                
                if (part.contains(VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER)) {
                    // e.g: "2015-05-01"/"2015-06-01"
                    String lowerBound = part.split(VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER)[0];
                    String upperBound = part.split(VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER)[1];
                    
                    TrimmingSubsetDimension subsetDimension = new TrimmingSubsetDimension(axis.getLabel(), axis.getNativeCrsUri(), lowerBound, upperBound);
                    parsedSubset = CrsComputerService.parseSubsetDimensionToNumbers(axis.getNativeCrsUri(), axis.getAxisUoM(), subsetDimension);
                } else {
                    // e.g: "2015-05-01"
                    String lowerBound = part;
                    String upperBound = part;
                    TrimmingSubsetDimension subsetDimension = new TrimmingSubsetDimension(axis.getLabel(), axis.getNativeCrsUri(), lowerBound, upperBound);
                    parsedSubset = CrsComputerService.parseSubsetDimensionToNumbers(axis.getNativeCrsUri(), axis.getAxisUoM(), subsetDimension);
                }
            } else {
                // Numeric axes
                if (part.contains(VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER)) {
                    // e.g: 20/100
                    BigDecimal lowerBound = new BigDecimal(part.split(VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER)[0]);
                    BigDecimal upperBound = new BigDecimal(part.split(VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER)[1]);
                    parsedSubset = new ParsedSubset<>(lowerBound, upperBound);
                } else {
                    // e.g: 50
                    BigDecimal lowerBound = new BigDecimal(part);
                    BigDecimal upperBound = lowerBound;
                    parsedSubset = new ParsedSubset<>(lowerBound, upperBound);
                }
            }
            
            parsedSubsets.add(parsedSubset);
        }
        
        return parsedSubsets;
    }
    
    /**
     * From the input params (bbox (mandatory), time=..., dim_* (optional))
     * translate all these geo subsets to grid domains for all layers.
     * 
     * NOTE: if layer doesn't contain optional dimension subset, it is not an error from WMS 1.3 standard.
     * e.g: GetMap request with 2 layers, layers=layer_3D,layer_2D&time=... then the time subset only be translated on layer_3D.
     */
    private Map<String, List<TranslatedGridDimensionSubset>> translateGridDimensionsSubsetsLayers() throws PetascopeException, SecoreException {
        // First, parse all the dimension subsets (e.g: time=...,dim_pressure=....) as one parsed dimension subset is one of layer's overlay operator's operand.
        Map<String, List<TranslatedGridDimensionSubset>> translatedSubsetsAllLayersMap = new HashMap<>();

        // NOTE: a GetMap requests can contain multiple layers (e.g: layers=Layer_1,Layer_2)
        // If a subset dimension (e.g: time=...) does not exist in one of the layer, it is not a problem.
        for (String layerName : layerNames) {

            WcpsCoverageMetadata wcpsCoverageMetadata = this.createWcpsCoverageMetadataForDownscaledLevel(layerName);
            List<TranslatedGridDimensionSubset> translatedGridDimensionSubsets = new ArrayList<>();

            // First, convert all the input dimensions subsets to BigDecimal to be translated to grid subsets
            // e.g: time="2015-02-05"
            for (Axis axis : wcpsCoverageMetadata.getSortedAxesByGridOrder()) {
                List<ParsedSubset<Long>> translatedGridSubsets = new ArrayList<>();
                int gridAxisOrder = axis.getRasdamanOrder();

                // Parse the requested dimension subset values from GetMap request
                List<ParsedSubset<BigDecimal>> parsedGeoSubsets = new ArrayList<>();

                if (axis.isNonXYAxis()) {
                    // e.g: time=...&dim_pressure=...
                    String dimSubset = dimSubsetsMap.get(axis.getLabel());
                    if (axis.isTimeAxis()) {
                        // In case axis is time axis, it has a specific key.
                        dimSubset = dimSubsetsMap.get(KVPSymbols.KEY_WMS_TIME);
                    } else if (axis.isElevationAxis()) {
                        // In case axis is elevation axis, it has a specific key.
                        dimSubset = dimSubsetsMap.get(KVPSymbols.KEY_WMS_ELEVATION);
                    }
                    
                    if (dimSubset != null) {
                        // Coverage contains a non XY, time dimension axis and there is a dim_axisLabel in GetMap request.
                        parsedGeoSubsets = this.parseDimensionSubset(axis, dimSubset);
                    } else {
                        // NOTE: if coverage contains a non XY, time dimension (e.g: temperature) axis but there is no dim_temperature parameter from GetMap request
                        // it will be the upper Bound grid coordinate in this axis (the latest slice of this dimension according to WMS 1.3 document).
                        Long gridUpperBound = axis.getGridBounds().getUpperLimit().longValue();
                        translatedGridSubsets.add(new ParsedSubset<>(gridUpperBound, gridUpperBound));
                    }
                } else {
                    // X, Y axes (bbox=...)
                    if (axis.isXAxis()) {
                        parsedGeoSubsets.add(new ParsedSubset<>(fittedBBbox.getXMin(), fittedBBbox.getXMax()));
                    } else {
                        parsedGeoSubsets.add(new ParsedSubset<>(fittedBBbox.getYMin(), fittedBBbox.getYMax()));
                    }
                }

                // Then, translate all these parsed subsets to grid domains
                for (ParsedSubset<BigDecimal> parsedGeoSubset : parsedGeoSubsets) {
                    ParsedSubset<Long> parsedGridSubset = coordinateTranslationService.geoToGridSpatialDomain(axis, parsedGeoSubset);
                    translatedGridSubsets.add(parsedGridSubset);
                }

                TranslatedGridDimensionSubset translatedGridDimensionSubset = new TranslatedGridDimensionSubset(gridAxisOrder, axis.isNonXYAxis(), translatedGridSubsets);
                translatedGridDimensionSubsets.add(translatedGridDimensionSubset);
            }

            translatedSubsetsAllLayersMap.put(layerName, translatedGridDimensionSubsets);
        }
        
        return translatedSubsetsAllLayersMap;
    }
    
    /**
     * From the list of translated grid subsets of a layer, build the list of grid spatial domains.
     * NOTE: It needs to use Cartesian product on non XY axes as the result of each element will be an overlay()'s operand.
     * e.g: time=0:1,dim_pressure=3:4 result would be:
     * [*:*,*:*,0,3],[*:*,*:*,1,3],[*:*,*:*,0,4],[*:*,*:*,1,4]
     */
    private List<String> createGridSpatialDomains(List<TranslatedGridDimensionSubset> translatedGridDimensionSubsets) throws PetascopeException, SecoreException {
        List<String> gridSpatialDomains = new ArrayList<>();
        List<List<String>> gridBoundAxes = new ArrayList<>();
        
        // Iterate all the translated grid domains for axes
        for (TranslatedGridDimensionSubset translatedGridDimensionSubset : translatedGridDimensionSubsets) {
            gridBoundAxes.add(translatedGridDimensionSubset.getGridBounds());
        }
        
        List<List<String>> listTmp = ListUtil.cartesianProduct(gridBoundAxes);
        for (List<String> list : listTmp) {
            gridSpatialDomains.add(list.toString());
        }
     
        return gridSpatialDomains;
    }
    
    /**
     * Create a list of Rasql collection expressions' string representations for a specific layer.
     * 
     */
    private List<String> createCollectionExpressionsLayer(String layerName, String aliasName, int index,
                                                          List<TranslatedGridDimensionSubset> translatedGridDimensionSubsets) throws PetascopeException, SecoreException {
        List<String> coverageExpressionsLayer = new ArrayList<>();
        
        // e.g: time="2015-05-12"&dim_pressure=20/30
        // translated to grid domains: time=5&dim_pressure=6:9
        // result would be c[*:*,*:*:,5,6] overlay c[*:*,*:*,5,7] overlay c[*:*,*:*,5,8] overlay c[*:*,*:*,5,9]
        List<String> gridSpatialDomains = this.createGridSpatialDomains(translatedGridDimensionSubsets);
        for (String gridSpatialDomain : gridSpatialDomains) {

            // CoverageExpression is the main part of a Rasql query builded from the current layer and style
            // e.g: c1 + 5, case c1 > 5 then {0, 1, 2}
            String coverageExpression;
            // NOTE: This one is used temporarily for generating Rasql query from fragments, it will not be showed in final Rasql query
            String aliasNameTemp = "GENERATED_COLLECTION_ALIAS_" + index;

            for (String styleName : this.styleNames) {

                Style style = this.wmsRepostioryService.readLayerByNameFromCache(layerName).getStyle(styleName);
                String styleExpression = "";
                if (style == null) {
                    // no complex coverage expression (e.g: c[0:50,0:20] - 5) just simple subsetted coverage expression c[0:50,0:20]
                    coverageExpression = aliasNameTemp;
                } else if (!StringUtils.isEmpty(style.getWcpsQueryFragment())) {
                    // wcpsQueryFragment
                    coverageExpression = this.buildCoverageExpressionByWCPSQueryFragment(aliasNameTemp, layerName, styleName);
                    styleExpression = style.getWcpsQueryFragment();
                } else {
                    // rasqlTransformFragment
                    coverageExpression = this.buildCoverageExpressionByRasqlTransformFragment(aliasNameTemp, layerName, styleName);
                    styleExpression = style.getRasqlQueryTransformFragment();
                }

                // Normally, apply the trimming subset for each coverage iterator
                // e.g: style is $c + 5 then rasql will be: c[0:20,0:30] + 5
                // NOTE: select c0 + udf.c0test() only should replace c0 with c0[0:20, 30:40] not udf.c0test to udf.c0[0:20, 30:40]test
                String valueToReplace = aliasName + gridSpatialDomain;
                // NOTE: if WMS style already contains "[" (e.g: condense + over $ts t( imageCrsDomain($c[ansi:"CRS:1"(0:3)], ansi) ) using $c[ansi($ts)])
                // It means user already picked the grid domains to be translated in WCPS query, don't add the additional subsets from bbox anymore.
                if (styleExpression.contains(RASQL_OPEN_SUBSETS)) {
                    valueToReplace = aliasName;
                }
                coverageExpression = coverageExpression.replace(aliasNameTemp, valueToReplace);

                // Add the translated Rasql query for the style to combine later
                coverageExpressionsLayer.add("( " + coverageExpression + " )");
            }
        }
        
        return coverageExpressionsLayer;
    }
    
    /**
     * Create the response for the GetMap request. NOTE: As WMS layer is a WCS
     * coverage so reuse the functionalities from WCS coverage metadata
     *
     * @return
     * @throws petascope.exceptions.WMSException
     */
    public Response createGetMapResponse() throws WMSException, PetascopeException {
        byte[] bytes = null;
        try {
            List<String> collectionAlias = new ArrayList<>();
            int i = 0;
            
            // First, parse all the dimension subsets (e.g: time=...,dim_pressure=....) as one parsed dimension subset is one of layer's overlay operator's operand.
            Map<String, List<TranslatedGridDimensionSubset>> translatedSubsetsAllLayersMap = this.translateGridDimensionsSubsetsLayers();            
            List<String> finalCollectionExpressions = new ArrayList<>();            
            // If GetMap requests with transparent=true then extract the nodata values from layer to be added to final rasql query
            List<String> nodataValues = new ArrayList<>();
            
            for (Map.Entry<String, List<TranslatedGridDimensionSubset>> entry : translatedSubsetsAllLayersMap.entrySet()) {
                String layerName = entry.getKey();
                
                WcpsCoverageMetadata wcpsCoverageMetadata = this.createWcpsCoverageMetadataForDownscaledLevel(layerName);
                
                // NOTE: the nodata values are fetched from coverage which has largest number of bands because the output also have the same bands
                // e.g: coverage 1: 3 bands, coverage 2: 1 band, then output coverage has 3 bands.
                if (wcpsCoverageMetadata.getNodata().size() > nodataValues.size()) {
                    nodataValues.clear();
                    for (NilValue nilValue : wcpsCoverageMetadata.getNodata()) {
                        nodataValues.add(nilValue.getValue());
                    }
                }
                
                List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
                String nativeCRS = CrsUtil.getEPSGCode(xyAxes.get(0).getNativeCrsUri());
                String gridBBoxSpatialDomains = this.createBBoxGridSpatialDomainsForExtend(layerName);
                
                String aliasName = ALIAS_NAME + "" + i;
                // a layer is equivalent to a rasdaman collection
                String collectionName = wcpsCoverageMetadata.getRasdamanCollectionName();
                collectionAlias.add(collectionName + " as " + aliasName);
                
                List<TranslatedGridDimensionSubset> translatedGridDimensionSubsets = entry.getValue();
                // Apply style if necessary on the geo subsetted coverage expressions and translate to Rasql collection expressions
                List<String> collectionExpressionsLayer = this.createCollectionExpressionsLayer(layerName, aliasName, i, translatedGridDimensionSubsets);
                
                // Now create a final coverageExpression which combines all the translated coverageExpression for styles with the OVERLAY operator            
                String combinedCollectionExpression = ListUtil.join(collectionExpressionsLayer, OVERLAY);
                // e.g: (c + 1)[0:20, 30:45]
                String subsetCollectionExpression = SUBSET_COVERAGE_EXPRESSION_TEMPLATE.replace(COLLECTION_EXPRESSION_TEMPLATE, combinedCollectionExpression);
                
                // NOTE: We need to ***extend*** before ***scaling*** for GetMap result.
                // Reason: Subsetting that goes _beyond_ the edge of a coverage will return the intersection with the coverage sdom.
                // We want to get the full subset sdom, however, and not just the intersection, otherwise the result will be scaled wrongly.
                // Therefore we use the _extend_ operation here to extend the intersection to the full subset domain.

                // e.g: sdom(coverage) is (0:860, 0:710) and GetMap request with grid bounds (700:1000, 500:800), only the intersection will be return (700:860, 500:710)
                // and if we bring this output to scale (e.g: scale(c[700:860, 500:710], [0:255,0:255])), this output will be stretched wrongly.
                // Therefore, it needs to use ***extend**** for this out of the bbox case and after that, scale will keep the correct ratio.
                subsetCollectionExpression = "Extend( " + subsetCollectionExpression + ", " + gridBBoxSpatialDomains + ")";

                String finalCollectionExpressionLayer = "Scale( " + subsetCollectionExpression + ", [0:" + (this.width - 1) + ", 0:" + (this.height - 1) + "] )";
                if (!nativeCRS.equals(outputCRS)) {
                    // It needs to be projected when the requesting CRS is different from the geo-referenced XY axes
                    finalCollectionExpressionLayer = PROJECTION_TEMPLATE.replace(COLLECTION_EXPRESSION_TEMPLATE, finalCollectionExpressionLayer)
                                                                        .replace("$xMin", this.fittedBBbox.getXMin().toPlainString())
                                                                        .replace("$yMin", this.fittedBBbox.getYMin().toPlainString())
                                                                        .replace("$xMax", this.fittedBBbox.getXMax().toPlainString())
                                                                        .replace("$yMax", this.fittedBBbox.getYMax().toPlainString())
                                                                        .replace("$sourceCRS", nativeCRS)
                                                                        .replace("$targetCRS", this.outputCRS);
                }
                
                finalCollectionExpressions.add( " ( " + finalCollectionExpressionLayer + " ) ");
                i++;
            }
            
            // Now, create the final Rasql query from all WMS layers
            String finalCollectionExpressionLayers = ListUtil.join(finalCollectionExpressions, OVERLAY);

            String formatType = MIMEUtil.getFormatType(this.format);
            String collections = ListUtil.join(collectionAlias, ", ");
            String nodata = this.createNodataValues(nodataValues);
            
            // Create the final Rasql query for all layers's styles of this GetMap request.
            String finalRasqlQuery = FINAL_TRANSLATED_RASQL_TEMPLATE
                    .replace(COLLECTION_EXPRESSION_TEMPLATE, finalCollectionExpressionLayers)
                    .replace("$nodata", nodata)
                    .replace("$formatType", formatType)
                    .replace("$collections", collections);
            
            bytes = RasUtil.getRasqlResultAsBytes(finalRasqlQuery);
        } catch (PetascopeException | SecoreException ex) {
            throw new WMSInternalException(ex.getMessage(), ex);
        }

        return new Response(Arrays.asList(bytes), this.format);
    }
    
    /**
     * Create a WCPS Coverage metadata object based on layerName (coverageId) and input bounding box (geoXY subsets) which fits on a rasdaman downscaled collection.
     */
    private WcpsCoverageMetadata createWcpsCoverageMetadataForDownscaledLevel(String layerName) throws PetascopeException, SecoreException {
        WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(layerName);
        Pair<BigDecimal, BigDecimal> geoSubsetX = new Pair(fittedBBbox.getXMin(), fittedBBbox.getXMax());
        Pair<BigDecimal, BigDecimal> geoSubsetY = new Pair(fittedBBbox.getYMin(), fittedBBbox.getYMax());
        wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.createForDownscaledLevelByGeoXYSubsets(wcpsCoverageMetadata, geoSubsetX, geoSubsetY, this.width, this.height);
        
        return wcpsCoverageMetadata;
    }
    
    /**
     * From the list of nodata values from all combined layers, create a nodata string for SELECT encode() query.
     * 
     */
    private String createNodataValues(List<String> nodataValues) {
        String nodata = "";
        if (this.transparent) {
            // If coverages don't have nodata, use this default value.
            String values = "0";
            if (nodataValues.size() > 0) {
                values = ListUtil.join(nodataValues, ",");
            }
            // e.g: {"nodata": [10, 20, 40]}
            nodata = "{\\\"nodata\\\": " + RASQL_OPEN_SUBSETS + values + RASQL_CLOSE_SUBSETS + "}";
        }
        
        return nodata;
    }

    /**
     * NOTE: GDAL always transform with XY order (i.e: Rasdaman grid order), not
     * by CRS order. So request from WMS must be swapped from YX order to XY
     * order for bounding box.
     *
     * @param inputBBox
     * @param sourceCrs
     * @return
     * @throws PetascopeException
     * @throws SecoreException
     */
    private BoundingBox swapYXBoundingBox(BoundingBox inputBBox, String sourceCrs) throws PetascopeException, SecoreException {
        String crsUri = CrsUtil.getEPSGFullUri(sourceCrs);
        CrsDefinition crsDefinition = CrsUtil.getCrsDefinition(crsUri);        // x, y, t,... 
        BigDecimal minX = inputBBox.getXMin();
        BigDecimal minY = inputBBox.getYMin();
        BigDecimal maxX = inputBBox.getXMax();
        BigDecimal maxY = inputBBox.getYMax();
        
        BoundingBox bbox = new BoundingBox();
        bbox.setXMin(new BigDecimal(inputBBox.getXMin().toPlainString()));
        bbox.setYMin(new BigDecimal(inputBBox.getYMin().toPlainString()));
        bbox.setXMax(new BigDecimal(inputBBox.getXMax().toPlainString()));
        bbox.setYMax(new BigDecimal(inputBBox.getYMax().toPlainString()));
        
        if (crsDefinition.getAxes().get(0).getType().equals(AxisTypes.Y_AXIS)) {
            // CRS axis is YX order so must swap the input bbox to XY order (e.g: EPSG:4326 (Lat, Long) to Long, Lat.
            BigDecimal minTemp = new BigDecimal(minX.toPlainString());
            BigDecimal maxTemp = new BigDecimal(maxX.toPlainString());
            minX = new BigDecimal(minY.toPlainString());
            minY = new BigDecimal(minTemp.toPlainString());
            maxX = new BigDecimal(maxY.toPlainString());
            maxY = new BigDecimal(maxTemp.toPlainString());

            bbox.setXMin(minX);
            bbox.setYMin(minY);
            bbox.setXMax(maxX);
            bbox.setYMax(maxY);
        }

        return bbox;
    }

    /**
     * Transform the input BBox from sourceCrs to targetCrs
     *
     * @param inputBBox
     * @param sourceCrs
     * @param targetCrs
     * @return
     */
    private BoundingBox transformBoundingBox(BoundingBox inputBBox, String sourceCrs, String targetCrs)
            throws WCSException, PetascopeException, SecoreException {

        BoundingBox bboxTmp = new BoundingBox();
        // Beware! Inserted values pairs needs to be in order X-coordinate and then Y-coordinate.
        // If you are inserting latitude/longitude values in decimal format, then the longitude should be first value of the pair (X-coordinate) and latitude the second value (Y-coordinate).        
        double minX = inputBBox.getXMin().doubleValue();
        double minY = inputBBox.getYMin().doubleValue();
        double maxX = inputBBox.getXMax().doubleValue();
        double maxY = inputBBox.getYMax().doubleValue();

        // NOTE: GDAL transform returns to XY order (e.g: EPSG:3857 (XY) -> EPSG:4326 (also XY))        
        double[] minXY = new double[]{minX, minY};
        List<BigDecimal> minValues = CrsProjectionUtil.transform(sourceCrs, targetCrs, minXY);
        double[] maxXY = new double[]{maxX, maxY};
        List<BigDecimal> maxValues = CrsProjectionUtil.transform(sourceCrs, targetCrs, maxXY);

        bboxTmp.setXMin(minValues.get(0));
        bboxTmp.setYMin(minValues.get(1));
        bboxTmp.setXMax(maxValues.get(0));
        bboxTmp.setYMax(maxValues.get(1));

        return bboxTmp;
    }
    
    /**
     * If request bbox is outside of first layer's geo XY axes bounds, adjust it to fit with coverage's geo XY axes bounds
     * to avoid server killed by Rasql query.
     * 
     */
    private void fitBBoxToCoverageGeoXYBounds() throws PetascopeException, SecoreException {
        WcpsCoverageMetadata wcpsCoverageMetadata = this.createWcpsCoverageMetadataForDownscaledLevel(this.layerNames.get(0));
        
        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
        Axis axisX = xyAxes.get(0);
        Axis axisY = xyAxes.get(1);
        
        if (this.fittedBBbox.getXMin().compareTo(axisX.getGeoBounds().getLowerLimit()) < 0) {
            this.fittedBBbox.setXMin(axisX.getGeoBounds().getLowerLimit());
        }
        if (this.fittedBBbox.getXMax().compareTo(axisX.getGeoBounds().getUpperLimit()) > 0) {
            this.fittedBBbox.setXMax(axisX.getGeoBounds().getUpperLimit());
        }
        
        if (this.fittedBBbox.getYMin().compareTo(axisY.getGeoBounds().getLowerLimit()) < 0) {
            this.fittedBBbox.setYMin(axisY.getGeoBounds().getLowerLimit());
        }
        if (this.fittedBBbox.getYMax().compareTo(axisY.getGeoBounds().getUpperLimit()) > 0) {
            this.fittedBBbox.setYMax(axisY.getGeoBounds().getUpperLimit());
        }
        
        if (this.fittedBBbox.getXMin().compareTo(this.fittedBBbox.getXMax()) > 0) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                         "Geo lower bound cannot be greater than geo upper bound on X axis named '" + axisX.getLabel() 
                                         + "', given '" + this.fittedBBbox.getXMin().toPlainString() + ":" + this.fittedBBbox.getXMax().toPlainString() + "'.");
        } else if (this.fittedBBbox.getYMin().compareTo(this.fittedBBbox.getYMax()) > 0) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                         "Geo lower bound cannot be greater than geo upper bound on Y axis named '" + axisY.getLabel() 
                                         + "', given '" + this.fittedBBbox.getYMin().toPlainString() + ":" + this.fittedBBbox.getYMax().toPlainString() + "'.");
        }
    }
    
    /**
     * Translate the geo bounding box from the input parameter to grid spatial domains by ****grid axes order****.
     * e.g: if coverage imported as YX (Lat, Long) grid axes order, it should keep this order.
     */
    private String createBBoxGridSpatialDomainsForExtend(String layerName) throws PetascopeException, SecoreException {
        WcpsCoverageMetadata wcpsCoverageMetadata = this.createWcpsCoverageMetadataForDownscaledLevel(layerName);
        
        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
        Axis axisX = xyAxes.get(0);
        Axis axisY = xyAxes.get(1);
        String gridBBoxSpatialDomains = "";
        
        // as BBox always in XY CRS order (e.g: Long, Lat) not Lat, Long
        ParsedSubset<Long> parsedGridSubsetX = coordinateTranslationService.geoToGridSpatialDomain(axisX, 
                                                                         new ParsedSubset<>(originalBBox.getXMin(), originalBBox.getXMax()));
        ParsedSubset<Long> parsedGridSubsetY = coordinateTranslationService.geoToGridSpatialDomain(axisY, 
                                                                         new ParsedSubset<>(originalBBox.getYMin(), originalBBox.getYMax()));

        if (xyAxes.get(0).getRasdamanOrder() < xyAxes.get(1).getRasdamanOrder()) {
            // Coverage is imported with grid XY order (e.g: Long, Lat) - e.g: test_mean_summer_airtemp
            gridBBoxSpatialDomains = RASQL_OPEN_SUBSETS + parsedGridSubsetX.getLowerLimit() + RASQL_BOUND_SEPARATION + parsedGridSubsetX.getUpperLimit() + ","
                                   + parsedGridSubsetY.getLowerLimit() + RASQL_BOUND_SEPARATION + parsedGridSubsetY.getUpperLimit() + RASQL_CLOSE_SUBSETS;
        } else {
            // Coverage is imported with grid YX order (e.g: Lat, Long) - e.g: test_eobstest
            gridBBoxSpatialDomains = RASQL_OPEN_SUBSETS + parsedGridSubsetY.getLowerLimit() + RASQL_BOUND_SEPARATION + parsedGridSubsetY.getUpperLimit() + ","
                                   + parsedGridSubsetX.getLowerLimit() + RASQL_BOUND_SEPARATION + parsedGridSubsetX.getUpperLimit() + RASQL_CLOSE_SUBSETS;
        }
        
        return gridBBoxSpatialDomains;
    }

    /**
     *
     * Return the translated coverageExpression for a layer's style by
     * rasqlTransformFragment e.g: persistent rasqlStyleTemplate is: case
     * $Iterator when ($Iterator + 2) > 20 and coverageAlias is c1 then the
     * translatedCoverageExpression is case c1 when (c1 + 2) > 20
     *
     * @return
     */
    private String buildCoverageExpressionByRasqlTransformFragment(String coverageAlias, String layerName, String styleName) {
        String coverageExpression = null;
        Style style = this.wmsRepostioryService.readLayerByNameFromCache(layerName).getStyle(styleName);
        coverageExpression = style.getRasqlQueryTransformFragment().replace(RASQL_FRAGMENT_ITERATOR, coverageAlias);

        return coverageExpression;
    }

    /**
     * Build a dummy WCPS query for a style by wcpsQueryFragment, then translate
     * this WCPS query to Rasql query and substract the coverageExpression
     * (select encode(coverageExpression, "png")) from this rasql.
     *
     */
    private String buildCoverageExpressionByWCPSQueryFragment(String coverageAlias, String layerName, String styleName) throws WCPSException, PetascopeException {
        // Create a dummy WCPS query, just to extract the translated coverageExpression in Rasql (select encode(coverageExpression, "png") from layerName.
        final String WCPS_QUERY_TEMPLATE = "for " + WCPS_COVERAGE_ALIAS + " in (" + layerName + ") return encode($coverageExpression, \"png\")";
        String wcpsQueryFragment = this.wmsRepostioryService.readLayerByNameFromCache(layerName).getStyle(styleName).getWcpsQueryFragment();
        String wcpsQuery = WCPS_QUERY_TEMPLATE
                .replace("$coverageExpression", wcpsQueryFragment);

        // NOTE: wcpsQueryFragment uses "$c" as coverage alias (rasqlTransformFragment uses "$Iterator")
        wcpsQuery = wcpsQuery.replace(WCPS_COVERAGE_ALIAS, coverageAlias);
        log.debug("Generated a WCPS query for wcpsQueryFragment '" + wcpsQuery + "'.");

        // Generate a Rasql query from the WCPS
        String rasqlTmp = this.kvpWCSProcessCoverageHandler.buildRasqlQuery(wcpsQuery);
        // Then extract the coverageExpression inside of output Rasql
        String coverageExpression = rasqlTmp.substring(rasqlTmp.indexOf("encode(") + 7, rasqlTmp.indexOf(", \"png\""));

        return coverageExpression;
    }
}
