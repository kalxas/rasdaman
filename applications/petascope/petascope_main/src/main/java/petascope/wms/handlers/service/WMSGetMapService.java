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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.rasdaman.domain.cis.NilValue;
import org.rasdaman.domain.wms.BoundingBox;
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
import petascope.core.AxisTypes;
import petascope.core.CrsDefinition;
import petascope.core.GeoTransform;
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
import petascope.core.gml.metadata.model.CoverageMetadata;
import petascope.exceptions.ExceptionCode;
import petascope.util.BigDecimalUtil;
import petascope.util.JSONUtil;
import petascope.wcps.encodeparameters.model.JsonExtraParams;
import petascope.wcps.encodeparameters.model.NoData;
import petascope.wcps.encodeparameters.service.SerializationEncodingService;
import petascope.wcps.encodeparameters.service.TranslateColorTableService;
import petascope.wcps.handler.CrsTransformHandler;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.service.SubsetParsingService;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;
import petascope.wms.exception.WMSStyleNotFoundException;

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
    @Autowired
    private SubsetParsingService subsetParsingService;
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    
    // In case of nativeCrs of layer (coverage) is different from outputCrs of GetMap request, then it needs to reproject $collectionExpression from sourceCrs to targetCrs.
    private static final String COLLECTION_EXPRESSION_TEMPLATE = "$collectionExpression";
    private static final String FORMAT_TYPE_TEMPLATE = "$formatType";
    private static final String ENCODE_FORMAT_PARAMETERS_TEMPLATE = "$encodeFormatParameters";
    private static final String COLLECTIONS_TEMPLATE = "$collections";
    
    private static final String SUBSET_COVERAGE_EXPRESSION_TEMPLATE = "( " + COLLECTION_EXPRESSION_TEMPLATE + " )";
    private static final String FINAL_TRANSLATED_RASQL_TEMPLATE = "SELECT ENCODE(" + COLLECTION_EXPRESSION_TEMPLATE + ", "
                                                                + "\"" + FORMAT_TYPE_TEMPLATE + "\", \"" + ENCODE_FORMAT_PARAMETERS_TEMPLATE + "\") FROM " + COLLECTIONS_TEMPLATE;
    private static final String OVERLAY = " OVERLAY ";
    private static final String FRAGMENT_ITERATOR_PREFIX = "$";
    private static final String COLLECTION_ITERATOR = "c";
    private static final String WCPS_FRAGMENT_ITERATOR = FRAGMENT_ITERATOR_PREFIX + "c";
    private static final String RASQL_FRAGMENT_ITERATOR = FRAGMENT_ITERATOR_PREFIX + "Iterator";
    
    private static final Pattern LAYER_ITERATOR_PATTERN = Pattern.compile("\\" + FRAGMENT_ITERATOR_PREFIX + "[a-zA-Z0-9_]+");
    private static final String WCPS_FRAGMENT_TYPE = "WCPS";
    private static final String RASQL_FRAGMENT_TYPE = "RASQL";
    
    private static final String NATIVE_CRS = "$nativeCRS";
    
    private static final String XMIN_NATIVCE_CRS = "$xMinNativeCRS";
    private static final String YMIN_NATIVCE_CRS = "$yMinNativeCRS";
    private static final String XMAX_NATIVCE_CRS = "$xMaxNativeCRS";
    private static final String YMAX_NATIVCE_CRS = "$yMaxNativeCRS";
    
    private static final String OUTPUT_CRS = "$outputCRS";
    
    private static final String XMIN_OUTPUT_CRS = "$xMinOutputCRS";
    private static final String YMIN_OUTPUT_CRS = "$yMinOutputCRS";
    private static final String XMAX_OUTPUT_CRS = "$xMaxOutputCRS";
    private static final String YMAX_OUTPUT_CRS = "$yMaxOutputCRS";
    
    private static final String WIDTH = "$width";
    private static final String HEIGHT = "$height";
    
    private static final String RESAMPLE_ALG = "$resampleAlg";
    private static final String ERR_THRESHOLD = "$errThreshold";
    
    private static final String PROJECT_TEMPLATE = "project( " + COLLECTION_EXPRESSION_TEMPLATE                                                                      
                                                    + ", \"" + XMIN_NATIVCE_CRS + "," + YMIN_NATIVCE_CRS + ", " + XMAX_NATIVCE_CRS + "," + YMAX_NATIVCE_CRS + "\" "
                                                    + ", \"" + NATIVE_CRS +"\" "
                                                    + ", \"" + XMIN_OUTPUT_CRS + "," + YMIN_OUTPUT_CRS + ", " + XMAX_OUTPUT_CRS + "," + YMAX_OUTPUT_CRS + "\" "
                                                    + ", \"" + OUTPUT_CRS + "\" "
                                                    + ", " + WIDTH + ", " + HEIGHT + ", " + RESAMPLE_ALG + ", " + ERR_THRESHOLD +  " )";
    
    // Increase width / height size of original bounding box by 15% percent when projection() needed to avoid gaps in tile's corners
    private static final BigDecimal EXTEND_RATIO = new BigDecimal("0.15");

    private static Logger log = LoggerFactory.getLogger(WMSGetMapService.class);

    private List<String> layerNames;
    private List<String> styleNames;
    private String outputCRS;
    private Integer width;
    private Integer height;
    // MIME type (e.g: image/png)
    private String format;
    // Optional parameter, used only incase with project()
    private String interpolation;
    public static final String DEFAULT_INTERPOLATION = "near";
    // Default value for project()
    private static final String DEFAULT_ERR_THRESHOLD = "0.125";
    private boolean transparent;
    // BBox already translated from requesting CRS to native CRS of XY geo-referenced axes
    // NOTE: it needs to keep the original BBox for Extend() to display result correctly in WMS client
    private BoundingBox originalBBox;
    
    // NOTE: this fittedBBox is used to fit input BBox to coverage's geo XY axes' domains 
    // to avoid server killed by subsetting collection (e.g: c[-20:30] instead of c[0:30])
    private BoundingBox fittedBBbox;

    // Used when requesting a BBox which is not layer's nativeCRS
    // because of projection() the result does not fill the bounding box and shows gaps (null values) in the result
    // then, it needs to select a bigger subsets (e.g: [lower_width -10% : upper_width + 10%, lower_height - 10% : upper_height + 10%]
    // in rasdaman to cover the gaps then apply the original subset [lower_width:upper_width, lower_height:upper_height]
    // to show the result without gaps in the borders.
    private BoundingBox extendedFittedGeoBBbox = new BoundingBox();
    private BoundingBox extendedFittedGridBBbox = new BoundingBox();
    
    // If request BoundingBox in different CRS from layer's CRS, it is true
    private boolean isProjection = false;
    
    private Map<String, String> dimSubsetsMap = new HashMap<>();
    
    private static final Map<String, Response> blankTileMap = new ConcurrentHashMap<>();
    
    public static final Set<String> validInterpolations = new LinkedHashSet<>();
    
    // Only 1 color table definition coming from the first style defines it (!)
    // e.g: layerA has colorTable A, layerB has colorTable B, then request with layers=layerA,layerB will end up with colorTable A
    // to be used as extra parameter in the final rasql encode().
    private String colorTableDefinition = null;
    private byte colorTableTypeCode = -1;
    
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

    public void setBBoxes(BoundingBox bbox) throws PetascopeException, SecoreException {
        // If request is in YX order for bounding box (e.g: EPSG:4326 Lat, Long, swap it to XY order Long, Lat)
        // NOTE: as all layers requested with same outputCRS so only do this one time
        this.originalBBox = this.swapYXBoundingBox(bbox, outputCRS);
        this.fittedBBbox = this.swapYXBoundingBox(bbox, outputCRS);
        
        WcpsCoverageMetadata wcpsCoverageMetadata = this.createWcpsCoverageMetadataForDownscaledLevel(this.layerNames.get(0));
        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();

        String nativeCRS = CrsUtil.getEPSGCode(xyAxes.get(0).getNativeCrsUri());
        this.isProjection = !this.outputCRS.equalsIgnoreCase(nativeCRS);
        if (isProjection) {
            // e.g: coverage's nativeCRS is EPSG:4326 and request BBox which contains coordinates in EPSG:3857
            this.fittedBBbox = this.transformBoundingBox(this.fittedBBbox, this.outputCRS, nativeCRS);
        }
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
        Map<String, List<TranslatedGridDimensionSubset>> translatedSubsetsAllLayersMap = new LinkedHashMap<>();

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
                    BoundingBox bbox = this.fittedBBbox;
                    if (isProjection) {
                        bbox = this.extendedFittedGeoBBbox;
                    }
                    
                    if (axis.isXAxis()) { 
                        parsedGeoSubsets.add(new ParsedSubset<>(bbox.getXMin(), bbox.getXMax()));
                    } else {
                        parsedGeoSubsets.add(new ParsedSubset<>(bbox.getYMin(), bbox.getYMax()));
                    }
                }

                // Then, translate all these parsed subsets to grid domains
                for (ParsedSubset<BigDecimal> parsedGeoSubset : parsedGeoSubsets) {
                    WcpsSubsetDimension subsetDimension = new WcpsTrimSubsetDimension(axis.getLabel(), axis.getNativeCrsUri(),
                                                                                      parsedGeoSubset.getLowerLimit().toPlainString(), parsedGeoSubset.getUpperLimit().toPlainString());
                    ParsedSubset<Long> parsedGridSubset = coordinateTranslationService.geoToGridSpatialDomain(axis, subsetDimension, parsedGeoSubset);
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
    private Pair<String, Map<String, String>> createCollectionExpressionsLayer(Map<String, String> iteratorsMap, int layerIndex, String layerName, String styleName, 
                                                                               List<TranslatedGridDimensionSubset> translatedGridDimensionSubsets) throws PetascopeException, SecoreException, WMSStyleNotFoundException, WCPSException {
        List<String> coverageExpressionsLayer = new ArrayList<>();
        
        // e.g: time="2015-05-12"&dim_pressure=20/30
        // translated to grid domains: time=5&dim_pressure=6:9
        // result would be c[*:*,*:*:,5,6] overlay c[*:*,*:*,5,7] overlay c[*:*,*:*,5,8] overlay c[*:*,*:*,5,9]
        List<String> gridSpatialDomains = this.createGridSpatialDomains(translatedGridDimensionSubsets);
        for (String gridSpatialDomain : gridSpatialDomains) {

            // CoverageExpression is the main part of a Rasql query builded from the current layer and style
            // e.g: c1 + 5, case c1 > 5 then {0, 1, 2}
            String collectionExpression = null;
            Layer layer = this.wmsRepostioryService.readLayerByNameFromCache(layerName);
            
            if (!StringUtils.isEmpty(styleName) && layer.getStyle(styleName) == null) {
                throw new WMSStyleNotFoundException(styleName, layerName);
            }

            Style style = layer.getStyle(styleName);
            String collectionIterator = COLLECTION_ITERATOR + layerIndex;
            collectionExpression = collectionIterator + gridSpatialDomain;
            iteratorsMap.put(layerName, collectionIterator);
            
            if (style != null) {
                if (!StringUtils.isEmpty(style.getWcpsQueryFragment())) {
                    // wcpsQueryFragment
                    collectionExpression = this.buildCoverageExpressionByWCPSQueryFragment(iteratorsMap, layerName, styleName, gridSpatialDomain);
                } else if (!StringUtils.isEmpty(style.getRasqlQueryTransformFragment())) {
                    // rasqlTransformFragment                
                    collectionExpression = this.buildCoverageExpressionByRasqlTransformFragment(iteratorsMap, layerName, styleName, gridSpatialDomain);
                }

                if (!StringUtils.isEmpty(style.getColorTableDefinition())) {
                    if (colorTableDefinition == null) {
                        colorTableTypeCode = style.getColorTableType();
                        colorTableDefinition = style.getColorTableDefinition();
                    }
                }
            }
            
            // Add the translated Rasql query for the style to combine later
            coverageExpressionsLayer.add("( " + collectionExpression + " )");
        }
        
        Pair<String, Map<String, String>> resultPair = new Pair<>(ListUtil.join(coverageExpressionsLayer, OVERLAY), iteratorsMap);
        return resultPair;
    }
    
    /**
     * Check if request BBox in native CRS intersects with first layer's BBox.
     */
    private boolean intersectLayerXYBBox() throws PetascopeException, SecoreException {
        WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(this.layerNames.get(0));
        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
        
        if ((this.fittedBBbox.getXMax().compareTo(xyAxes.get(0).getGeoBounds().getLowerLimit()) < 0)
          || (this.fittedBBbox.getXMin().compareTo(xyAxes.get(0).getGeoBounds().getUpperLimit()) > 0)
          || (this.fittedBBbox.getYMax().compareTo(xyAxes.get(1).getGeoBounds().getLowerLimit()) < 0)
          || (this.fittedBBbox.getYMin().compareTo(xyAxes.get(1).getGeoBounds().getUpperLimit()) > 0)) {
            return false;
        }
        
        return true;
    }
    
    /**
     *  From the Set of coverage (layer) names, return 
     * e.g: Sentinel2_B4 AS c0, Sentienl2_B8 as c1
     */
    private String builRasqlFromExpression(Map<String, String> iteratatorsMap) throws PetascopeException, SecoreException {
        List<String> collectionAlias = new ArrayList<>();
        for (Map.Entry<String, String> entry : iteratatorsMap.entrySet()) {
            String coverageId = entry.getKey().replace(FRAGMENT_ITERATOR_PREFIX, "");
            String collectionname = createWcpsCoverageMetadataForDownscaledLevel(coverageId).getRasdamanCollectionName();
            // e.g: Sentinel2_B4 as c0
            collectionAlias.add(collectionname + " AS " + entry.getValue().replace(FRAGMENT_ITERATOR_PREFIX, ""));
        }
        
        String result = ListUtil.join(collectionAlias, ", ");
        return result;
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
            if (!this.intersectLayerXYBBox()) {
                Response response = this.createBlankImage();
                return response;
            }

            // NOTE: to avoid error in rasql when server is killed from WMS client which sends request out of coverage's grid domains, 
            // adjust the bounding box to fit with coverage's grid XY axes' domains
            this.fitBBoxToCoverageGeoXYBounds(this.fittedBBbox);

            if (isProjection) {
                this.createExtendedBBox(this.layerNames.get(0));
                this.fitBBoxToCoverageGeoXYBounds(this.extendedFittedGeoBBbox);
            }
            
            // First, parse all the dimension subsets (e.g: time=...,dim_pressure=....) as one parsed dimension subset is one of layer's overlay operator's operand.
            Map<String, List<TranslatedGridDimensionSubset>> translatedSubsetsAllLayersMap = this.translateGridDimensionsSubsetsLayers();            
            List<String> finalCollectionExpressions = new ArrayList<>();            
            // If GetMap requests with transparent=true then extract the nodata values from layer to be added to final rasql query
            List<BigDecimal> nodataValues = new ArrayList<>();
            
            WcpsCoverageMetadata wcpsCoverageMetadata = null;
            
            Map<String, String> iteratorsMap = new LinkedHashMap<>();
            
            int styleIndex = 0;
            int layerIndex = 0;
                        
            for (Map.Entry<String, List<TranslatedGridDimensionSubset>> entry : translatedSubsetsAllLayersMap.entrySet()) {
                String layerName = entry.getKey();
                String styleName = null;
                if (this.styleNames.size() > 0) {
                    styleName = this.styleNames.get(styleIndex);
                }
                wcpsCoverageMetadata = this.createWcpsCoverageMetadataForDownscaledLevel(layerName);
                
                if (nodataValues.isEmpty()) {
                    if (wcpsCoverageMetadata.getNilValues().size() > 0) {
                        nodataValues.add(new BigDecimal(wcpsCoverageMetadata.getNilValues().get(0).getValue()));
                    }
                }
                
                List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
                String nativeCRS = CrsUtil.getEPSGCode(xyAxes.get(0).getNativeCrsUri());
                List<TranslatedGridDimensionSubset> translatedGridDimensionSubsets = entry.getValue();
                
                // Apply style if necessary on the geo subsetted coverage expressions and translate to Rasql collection expressions
                Pair<String, Map<String, String>> collectionExpressionsLayerPair = this.createCollectionExpressionsLayer(iteratorsMap, layerIndex, layerName, styleName, translatedGridDimensionSubsets);
                
                // e.g: (c + 1)[0:20, 30:45]
                String subsetCollectionExpression = SUBSET_COVERAGE_EXPRESSION_TEMPLATE.replace(COLLECTION_EXPRESSION_TEMPLATE, collectionExpressionsLayerPair.fst);
                iteratorsMap.putAll(collectionExpressionsLayerPair.snd);
                String finalCollectionExpressionLayer;
                
                if (!isProjection) {
                    finalCollectionExpressionLayer = this.createGridScalingOutputNonProjection(layerName, subsetCollectionExpression);
                } else {
                    finalCollectionExpressionLayer = this.createGridScalingOutputProjection(nativeCRS, subsetCollectionExpression);
                }
                
                finalCollectionExpressions.add( " ( " + finalCollectionExpressionLayer + " ) ");
                styleIndex++;
                layerIndex++;
            }
            
            
            // Now, create the final Rasql query from all WMS layers
            // NOTE: WMS request first layer is always on top, rasdaman is reversed (a overlay b, then b is ontop of a)
            String finalCollectionExpressionLayers = "";
            for (int i = finalCollectionExpressions.size() - 1; i >= 0; i--) {
                finalCollectionExpressionLayers += finalCollectionExpressions.get(i);
                if (i > 0) {
                    finalCollectionExpressionLayers += " " + OVERLAY + " ";
                }
            }

            String formatType = MIMEUtil.getFormatType(this.format);
            String collections = this.builRasqlFromExpression(iteratorsMap);
            String encodeFormatParameters = this.createEncodeFormatParameters(nodataValues, wcpsCoverageMetadata);
            
            // Create the final Rasql query for all layers's styles of this GetMap request.
            String finalRasqlQuery = FINAL_TRANSLATED_RASQL_TEMPLATE
                                .replace(COLLECTION_EXPRESSION_TEMPLATE, finalCollectionExpressionLayers)
                                .replace(ENCODE_FORMAT_PARAMETERS_TEMPLATE, encodeFormatParameters)
                                .replace(FORMAT_TYPE_TEMPLATE, formatType)
                                .replace(COLLECTIONS_TEMPLATE, collections);
            
            bytes = RasUtil.getRasqlResultAsBytes(finalRasqlQuery);
        } catch (PetascopeException | SecoreException ex) {
            throw new WMSInternalException(ex.getMessage(), ex);
        }

        return new Response(Arrays.asList(bytes), this.format, this.layerNames.get(0));
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
        
        String encodeFormatParameters = JSONUtil.serializeObjectToJSONString(jsonExtraParams);
        encodeFormatParameters = encodeFormatParameters.replace("\"", "\\\"");
        
        return encodeFormatParameters;
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
    private void fitBBoxToCoverageGeoXYBounds(BoundingBox bbox) throws PetascopeException, SecoreException {
        WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(this.layerNames.get(0));
        
        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
        Axis axisX = xyAxes.get(0);
        Axis axisY = xyAxes.get(1);
        
        BigDecimal originalGeoLowerBoundX = axisX.getGeoBounds().getLowerLimit();
        BigDecimal originalGeoUpperBoundX = axisX.getGeoBounds().getUpperLimit();
        
        BigDecimal originalGeoLowerBoundY = axisY.getGeoBounds().getLowerLimit();
        BigDecimal originalGeoUpperBoundY = axisY.getGeoBounds().getUpperLimit();
        
        NumericSubset geoSubsetX = new NumericTrimming(bbox.getXMin(), bbox.getXMax());
        NumericSubset geoSubsetY = new NumericTrimming(bbox.getYMin(), bbox.getYMax());
        
        axisX.setGeoBounds(geoSubsetX);
        axisY.setGeoBounds(geoSubsetY);
        
        List<Subset> subsets = new ArrayList<>();
        subsets.add(new Subset(geoSubsetX, axisX.getNativeCrsUri(), axisX.getLabel()));
        subsets.add(new Subset(geoSubsetY, axisY.getNativeCrsUri(), axisY.getLabel()));
        
        subsetParsingService.fitToSampleSpaceRegularAxes(subsets, wcpsCoverageMetadata);

        if (bbox.getXMin().compareTo(originalGeoLowerBoundX) < 0) {
            bbox.setXMin(originalGeoLowerBoundX);
        }
        if (bbox.getXMax().compareTo(originalGeoUpperBoundX) > 0) {
            bbox.setXMax(originalGeoUpperBoundX);
        }
        
        if (bbox.getYMin().compareTo(originalGeoLowerBoundY) < 0) {
            bbox.setYMin(originalGeoLowerBoundY);
        }
        if (bbox.getYMax().compareTo(originalGeoUpperBoundY) > 0) {
            bbox.setYMax(originalGeoUpperBoundY);
        }
        
        if (bbox.getXMin().compareTo(bbox.getXMax()) > 0) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                         "Geo lower bound cannot be greater than geo upper bound on X axis named '" + axisX.getLabel() 
                                         + "', given '" + bbox.getXMin().toPlainString() + ":" + bbox.getXMax().toPlainString() + "'.");
        } else if (bbox.getYMin().compareTo(bbox.getYMax()) > 0) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                         "Geo lower bound cannot be greater than geo upper bound on Y axis named '" + axisY.getLabel() 
                                         + "', given '" + bbox.getYMin().toPlainString() + ":" + bbox.getYMax().toPlainString() + "'.");
        }
    }
    
    /**
     * When projection is needed, create an extended Geo BBox from original BBox.
     */
    private void createExtendedGeoBBox(Axis axisX, Axis axisY) {
        BigDecimal geoDistanceX = fittedBBbox.getXMax().subtract(fittedBBbox.getXMin());
        BigDecimal gridDistanceX = BigDecimalUtil.divide(geoDistanceX, axisX.getResolution());
        int gridOffsetX = BigDecimalUtil.multiple(gridDistanceX, EXTEND_RATIO).toBigInteger().intValue();
        BigDecimal geoOffsetX = new BigDecimal(gridOffsetX).multiply(axisX.getResolution()).abs();
        
        BigDecimal newGeoLowerBoundX = fittedBBbox.getXMin().subtract(geoOffsetX);
        BigDecimal newGeoUpperBoundX = fittedBBbox.getXMax().add(geoOffsetX);        

        BigDecimal geoDistanceY = fittedBBbox.getYMax().subtract(fittedBBbox.getYMin());
        BigDecimal gridDistanceY = BigDecimalUtil.divide(geoDistanceY, axisY.getResolution());
        int gridOffsetY = BigDecimalUtil.multiple(gridDistanceY, EXTEND_RATIO).toBigInteger().intValue();
        BigDecimal geoOffsetY = new BigDecimal(gridOffsetY).multiply(axisY.getResolution()).abs();

        BigDecimal newGeoLowerBoundY = fittedBBbox.getYMin().subtract(geoOffsetY);
        BigDecimal newGeoUpperBoundY = fittedBBbox.getYMax().add(geoOffsetY);        
        
        this.extendedFittedGeoBBbox = new BoundingBox(newGeoLowerBoundX, newGeoLowerBoundY, newGeoUpperBoundX, newGeoUpperBoundY);
    }
    
    /**
     * When projection is needed, from ExtendedGeoBBox, calculate grid bounds for it.
     */
    private void createExtendedGridBBox(Axis axisX, Axis axisY) throws PetascopeException {
        
        ParsedSubset<BigDecimal> parsedGeoSubsetX = new ParsedSubset<>(extendedFittedGeoBBbox.getXMin(), extendedFittedGeoBBbox.getXMax());
        WcpsSubsetDimension subsetDimensionX = new WcpsTrimSubsetDimension(axisX.getLabel(), axisX.getNativeCrsUri(), 
                                                                           parsedGeoSubsetX.getLowerLimit().toPlainString(), parsedGeoSubsetX.getUpperLimit().toPlainString());
        ParsedSubset<Long> parsedGridSubsetX = coordinateTranslationService.geoToGridSpatialDomain(axisX, subsetDimensionX, parsedGeoSubsetX);
        extendedFittedGridBBbox.setXMin(new BigDecimal(parsedGridSubsetX.getLowerLimit()));
        extendedFittedGridBBbox.setXMax(new BigDecimal(parsedGridSubsetX.getUpperLimit()));

        ParsedSubset<BigDecimal> parsedGeoSubsetY = new ParsedSubset<>(extendedFittedGeoBBbox.getYMin(), extendedFittedGeoBBbox.getYMax());
        WcpsSubsetDimension subsetDimensionY = new WcpsTrimSubsetDimension(axisY.getLabel(), axisY.getNativeCrsUri(), 
                                                                           parsedGeoSubsetY.getLowerLimit().toPlainString(), parsedGeoSubsetY.getUpperLimit().toPlainString());
        ParsedSubset<Long> parsedGridSubsetY = coordinateTranslationService.geoToGridSpatialDomain(axisY, subsetDimensionY, parsedGeoSubsetY);
        extendedFittedGridBBbox.setYMin(new BigDecimal(parsedGridSubsetY.getLowerLimit()));
        extendedFittedGridBBbox.setYMax(new BigDecimal(parsedGridSubsetY.getUpperLimit()));
    }
    
    /**
     * In case of requesting bounding box in a CRS which is different from layer's native CRS, it translates
     * request BBOX from source CRS to native CRS then extend this transformed BBox by a constant in both width and height sizes.
     * 
     * This allows rasdaman to query result without missing values in the corners because of projection() will return a rotated result.
     */
    private void createExtendedBBox(String layerName) throws PetascopeException, SecoreException {
        
        WcpsCoverageMetadata wcpsCoverageMetadata = this.createWcpsCoverageMetadataForDownscaledLevel(layerName);
        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
        
        Axis axisX = xyAxes.get(0);
        Axis axisY = xyAxes.get(1);
        
        this.createExtendedGeoBBox(axisX, axisY);
        this.createExtendedGridBBox(axisX, axisY);
    }
    
    /**
     * Apply subset() and extend() on top of collection subsetting expression if needed in case of no-projection.
     */
    private String createGridScalingOutputNonProjection(String layerName, String subsetCollectionExpression)
                   throws PetascopeException, SecoreException {
        WcpsCoverageMetadata wcpsCoverageMetadata = this.createWcpsCoverageMetadataForDownscaledLevel(layerName);
        
        List<Axis> originalXYAxes = wcpsCoverageMetadata.getXYAxes();
                    
//      Transform ***layer BBox*** from layer's native CRS to output CRS (default EPSG:4326) as original request BBox is also in EPSG:4326
        GeoTransform sourceGeoTransformLayerBBox = CrsTransformHandler.createGeoTransform(originalXYAxes);
        GeoTransform targetGeoTransformLayerBBox = CrsProjectionUtil.getGeoTransformInTargetCRS(sourceGeoTransformLayerBBox, outputCRS);
            
        List<Axis> transformedXYAxesLayerBBox = CrsTransformHandler.createGeoXYAxes(originalXYAxes, targetGeoTransformLayerBBox);
        
        Axis axisX = transformedXYAxesLayerBBox.get(0);
        Axis axisY = transformedXYAxesLayerBBox.get(1);
        
        // Default grid domains for scale, extend if request BBox is within layer's geo bounds.
        String scaleX = "0:" + (this.width - 1);
        String extendX = scaleX;
        
        String scaleY = "0:" + (this.height - 1);
        String extendY = scaleY;
        
        // If request BBox is not within the layer's geo bounds
        if (! ( (originalBBox.getXMin().compareTo(axisX.getGeoBounds().getLowerLimit()) >= 0)
            && (originalBBox.getXMax().compareTo(axisX.getGeoBounds().getUpperLimit()) <= 0)     
            && (originalBBox.getYMin().compareTo(axisY.getGeoBounds().getLowerLimit()) >= 0)
            && (originalBBox.getYMax().compareTo(axisY.getGeoBounds().getUpperLimit()) <= 0)
            ) ) {
            
            // ********* X axis: e.g: Long
            
            BigDecimal geoOriginX = BigDecimal.ONE;
            BigDecimal lengthGeoIntersectionX = BigDecimal.ONE;
            
            BigDecimal lengthBBoxX = originalBBox.getXMax().subtract(originalBBox.getXMin());
            
            if (!(originalBBox.getXMin().compareTo(axisX.getGeoBounds().getLowerLimit()) >= 0 && originalBBox.getXMax().compareTo(axisX.getGeoBounds().getUpperLimit()) <= 0)) {
                if (originalBBox.getXMin().compareTo(axisX.getGeoBounds().getLowerLimit()) < 0 && originalBBox.getXMax().compareTo(axisX.getGeoBounds().getUpperLimit()) <= 0) {
                    // e.g: BBox of Long is [10:30] intersects with axis Long [20:40] from [20:30], originX: 20
                    geoOriginX = axisX.getGeoBounds().getLowerLimit();
                    lengthGeoIntersectionX = originalBBox.getXMax().subtract(geoOriginX);
                } else if (originalBBox.getXMin().compareTo(axisX.getGeoBounds().getLowerLimit()) >= 0 && originalBBox.getXMax().compareTo(axisX.getGeoBounds().getUpperLimit()) > 0) {
                    // e.g: BBox of Long is [30:50] intersects with axis Long [20:40] from [30:40], originX: 30
                    geoOriginX = originalBBox.getXMin();
                    lengthGeoIntersectionX = axisX.getGeoBounds().getUpperLimit().subtract(geoOriginX);
                } else if (originalBBox.getXMin().compareTo(axisX.getGeoBounds().getLowerLimit()) < 0 && originalBBox.getXMax().compareTo(axisX.getGeoBounds().getUpperLimit()) > 0) {
                    // e.g: BBox of Long is [10:50] intersects with axis Long [20:40] from [20:40], originX: 20
                    geoOriginX = axisX.getGeoBounds().getLowerLimit();
                    lengthGeoIntersectionX = axisX.getGeoBounds().getUpperLimit().subtract(geoOriginX);
                }

                // Calculate the portion of intersection's length and bbox's length on X axis for the domains of scale and extend
                BigDecimal portionIntersectionX = BigDecimalUtil.divide(lengthGeoIntersectionX, lengthBBoxX);
                Long scaleUpperBoundX = portionIntersectionX.multiply(new BigDecimal(this.width)).longValue();
                scaleX = "0:" + scaleUpperBoundX;

                // Calculate the portion of originX in the geo bboxX
                BigDecimal portionGeoOriginX = BigDecimalUtil.divide((geoOriginX.subtract(originalBBox.getXMin())), lengthBBoxX); 
                Long gridOriginX = portionGeoOriginX.multiply(new BigDecimal(this.width)).longValue();
                Long extendLowerBoundX = 0L - Math.abs(gridOriginX);
                Long extendUpperBoundX = this.width + extendLowerBoundX - 1;
                extendX = extendLowerBoundX + ":" + extendUpperBoundX ;
            }
            
            // ********* Y axis: e.g: Lat
            
            if (!(originalBBox.getYMin().compareTo(axisY.getGeoBounds().getLowerLimit()) >= 0 && originalBBox.getYMax().compareTo(axisY.getGeoBounds().getUpperLimit()) <= 0)) {
                BigDecimal geoOriginY = BigDecimal.ONE;
                BigDecimal lengthGeoIntersectionY = BigDecimal.ONE;

                BigDecimal lengthBBoxY = originalBBox.getYMax().subtract(originalBBox.getYMin());

                if (originalBBox.getYMin().compareTo(axisY.getGeoBounds().getLowerLimit()) < 0 && originalBBox.getYMax().compareTo(axisY.getGeoBounds().getUpperLimit()) <= 0) {
                    // e.g: BBox of Lat is [-50:-30] intersects with axis Lat [-40:-20] from [-40:-30], originY: -30
                    geoOriginY = originalBBox.getYMax();
                    lengthGeoIntersectionY = originalBBox.getYMax().subtract(axisY.getGeoBounds().getLowerLimit());
                } else if (originalBBox.getYMin().compareTo(axisY.getGeoBounds().getLowerLimit()) >= 0 && originalBBox.getYMax().compareTo(axisY.getGeoBounds().getUpperLimit()) > 0) {
                    // e.g: BBox of Lat is [-30:-10] intersects with axis Lat [-40:-20] from [-30:-20], originY: -20
                    geoOriginY = axisY.getGeoBounds().getUpperLimit();
                    lengthGeoIntersectionY = axisY.getGeoBounds().getUpperLimit().subtract(originalBBox.getYMin());
                } else if (originalBBox.getYMin().compareTo(axisY.getGeoBounds().getLowerLimit()) < 0 && originalBBox.getYMax().compareTo(axisY.getGeoBounds().getUpperLimit()) > 0) {
                    // e.g: BBox of Lat is [-50:-10] intersects with axis Lat [-40:-20] from [-40:-20], originY: -20
                    geoOriginY = axisY.getGeoBounds().getUpperLimit();
                    lengthGeoIntersectionY = geoOriginY.subtract(axisY.getGeoBounds().getLowerLimit());
                }

                // Calculate the portion of intersection's length and bbox's length on Y axis for the domains of scale and extend
                BigDecimal portionIntersectionY = BigDecimalUtil.divide(lengthGeoIntersectionY, lengthBBoxY);
                Long scaleUpperBoundY = portionIntersectionY.multiply(new BigDecimal(this.height)).longValue();
                scaleY = "0:" + scaleUpperBoundY;

                // Calculate the portion of originX in the geo bboxY
                BigDecimal portionGeoOriginY = BigDecimalUtil.divide(originalBBox.getYMax().subtract(geoOriginY), lengthBBoxY); 
                Long gridOriginY = portionGeoOriginY.multiply(new BigDecimal(this.height)).longValue();
                Long extendLowerBoundY = 0L - Math.abs(gridOriginY);
                Long extendUpperBoundY = this.height + extendLowerBoundY - 1;
                extendY = extendLowerBoundY + ":" + extendUpperBoundY;
            }
        }
        
        if (originalXYAxes.get(0).getRasdamanOrder() > originalXYAxes.get(1).getRasdamanOrder()) {
            // NOTE: This case is layer imported with YX grid order (e.g: netCDF lat, long) not GeoTiff (long, lat).
            // Hence, it must need swap scale, extend and add transpose in encode to return correct result
            String temp = scaleX;
            scaleX = scaleY;
            scaleY = temp;

            temp = extendX;
            extendX = extendY;
            extendY = temp;                
        }
    
        subsetCollectionExpression = "Scale( " + subsetCollectionExpression + ", [" + scaleX + ", " + scaleY + "] )";
        String finalCollectionExpressionLayer = subsetCollectionExpression;
        
        // No need to add extend if XY grid domains are as same as Scale() in the final generated rasql query
        if (!(extendX.equals(scaleX) && extendY.equals(scaleY))) {
            finalCollectionExpressionLayer = "Extend( " + subsetCollectionExpression + ", [" + extendX + ", " + extendY + "] )"; 
        }
        
        return finalCollectionExpressionLayer;
    }
    
    /**
     * Using feature of project() to scale subsetting expression and then transform result from nativeCRS to outputCRS.
     */
    private String createGridScalingOutputProjection(String nativeCRS, String subsetCollectionExpression) {
        
        String finalCollectionExpressionLayer = PROJECT_TEMPLATE.replace(COLLECTION_EXPRESSION_TEMPLATE, subsetCollectionExpression)

                                                                .replace(XMIN_NATIVCE_CRS, this.extendedFittedGeoBBbox.getXMin().toPlainString())
                                                                .replace(YMIN_NATIVCE_CRS, this.extendedFittedGeoBBbox.getYMin().toPlainString())
                                                                .replace(XMAX_NATIVCE_CRS, this.extendedFittedGeoBBbox.getXMax().toPlainString())
                                                                .replace(YMAX_NATIVCE_CRS, this.extendedFittedGeoBBbox.getYMax().toPlainString())

                                                                .replace(NATIVE_CRS, nativeCRS)

                                                                .replace(XMIN_OUTPUT_CRS, this.originalBBox.getXMin().toPlainString())
                                                                .replace(YMIN_OUTPUT_CRS, this.originalBBox.getYMin().toPlainString())
                                                                .replace(XMAX_OUTPUT_CRS, this.originalBBox.getXMax().toPlainString())
                                                                .replace(YMAX_OUTPUT_CRS, this.originalBBox.getYMax().toPlainString())

                                                                .replace(OUTPUT_CRS, outputCRS)

                                                                .replace(WIDTH, width.toString())
                                                                .replace(HEIGHT, height.toString())
                
                                                                .replace(RESAMPLE_ALG, interpolation)
                                                                .replace(ERR_THRESHOLD, DEFAULT_ERR_THRESHOLD);
        
        return finalCollectionExpressionLayer;
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
    private String buildCoverageExpressionByRasqlTransformFragment(Map<String, String> iteratorsMap, String layerName,
                                                                   String styleName, String gridSpatialDomain) throws PetascopeException {
        Style style = this.wmsRepostioryService.readLayerByNameFromCache(layerName).getStyle(styleName);
        String rasqlQueryFragment = style.getRasqlQueryTransformFragment();
        String rasqlQueryFragmentUpdated = this.updateStyleQueryByLayerIterators(RASQL_FRAGMENT_TYPE, iteratorsMap, layerName, rasqlQueryFragment);
        String collectionExpression = rasqlQueryFragmentUpdated;
        
        for (String collectionAlias : iteratorsMap.values()) {
            // e.g: c0 -> c0[0:30, 30:50]
            collectionExpression = collectionExpression.replace(collectionAlias, collectionAlias + gridSpatialDomain);
        }
        
        return collectionExpression;
    }
    
        
    /**
     * Extract all iterators (starting with $) from query fragment (e.g: $c, $Iterator, $LAYER_NAME)
     */
    private String updateStyleQueryByLayerIterators(String fragmentType, Map<String, String> iteratorsMap, String layerName, String queryFragment) throws PetascopeException {        
        
        StringBuffer stringBuffer = new StringBuffer();        
        Matcher matcher = LAYER_ITERATOR_PATTERN.matcher(queryFragment);
        int i = 0;
        while (matcher.find()) {
            // e.g: $Iterator, $Sentinel2_B4
            String iterator = matcher.group(0);
            
            if (iterator.equals(WCPS_FRAGMENT_ITERATOR) || iterator.equals(RASQL_FRAGMENT_ITERATOR)) {
                iterator = FRAGMENT_ITERATOR_PREFIX + layerName;
            }
            
            boolean newValue = false;
            if (iteratorsMap.isEmpty()) {
                newValue = true;
            } else if (!iteratorsMap.containsKey(iterator)) {
                if (!iteratorsMap.containsKey(iterator.replace(FRAGMENT_ITERATOR_PREFIX, ""))) {
                    newValue = true;
                    i++;
                }
            }
            
            String replacement = WCPS_FRAGMENT_ITERATOR + i;
            
            if (!newValue) {
                replacement = iteratorsMap.get(iterator);
                if (replacement == null) {
                    replacement = iteratorsMap.get(iterator.replace(FRAGMENT_ITERATOR_PREFIX, ""));
                }
            }
            if (fragmentType.equals(RASQL_FRAGMENT_TYPE)) {
                replacement = replacement.replace(FRAGMENT_ITERATOR_PREFIX, "");
            }
            
            // e.g: axis iterator $px, $py only in WCPS query fragment, they are not coverage iterators
            if (this.coverageRepositoryService.readCoverageBasicMetadataByIdFromCache(iterator.replace(FRAGMENT_ITERATOR_PREFIX, "")) != null) {
                matcher.appendReplacement(stringBuffer, iterator.replace(iterator,  replacement).replace(FRAGMENT_ITERATOR_PREFIX, "\\" + FRAGMENT_ITERATOR_PREFIX));
            
                // e.g: $Sentinel2_B4 -> $co, $Sentinel2_B8 -> $c1
                if (newValue) {
                    iteratorsMap.put(iterator, replacement);
                }
            }
        }
        
        matcher.appendTail(stringBuffer);           
        return stringBuffer.toString();
    }

    /**
     * Build a dummy WCPS query for a style by wcpsQueryFragment, then translate
     * this WCPS query to Rasql query and substract the coverageExpression
     * (select encode(coverageExpression, "png")) from this rasql.
     *
     */
    private String buildCoverageExpressionByWCPSQueryFragment(Map<String, String> iteratorsMap, String layerName, String styleName, String gridDomain) throws WCPSException, PetascopeException {
        String wcpsQueryFragment = this.wmsRepostioryService.readLayerByNameFromCache(layerName).getStyle(styleName).getWcpsQueryFragment();
        String wcpsQueryFragmentUpdated = this.updateStyleQueryByLayerIterators(WCPS_FRAGMENT_TYPE, iteratorsMap, layerName, wcpsQueryFragment);
        
        List<String> forExpressions = new ArrayList<>();
        List<String> coverageAliasNames = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : iteratorsMap.entrySet()) {
            String coverageId = entry.getKey().replace(FRAGMENT_ITERATOR_PREFIX, "");
            String coverageAliasTmp = entry.getValue();                       
            String forExpression = coverageAliasTmp + " IN (" + coverageId + ")";
            forExpressions.add(forExpression);

            coverageAliasNames.add(coverageAliasTmp);              
        }
        
        // Create a dummy WCPS query, just to extract the translated coverageExpression in Rasql (select encode(coverageExpression, "png") from layerName.
        String WCPS_QUERY_TEMPLATE = "FOR " + ListUtil.join(forExpressions, ", ") + " RETURN ENCODE( $coverageExpression, \"png\" )";
        String wcpsQuery = WCPS_QUERY_TEMPLATE.replace("$coverageExpression", wcpsQueryFragmentUpdated);
        log.debug("Generated a WCPS query for wcpsQueryFragment '" + wcpsQuery + "'.");

        // Generate a Rasql query from the WCPS
        String rasqlTmp = this.kvpWCSProcessCoverageHandler.buildRasqlQuery(wcpsQuery);
        // Then extract the collectionExpression from encode() of rasql query
        String collectionExpression = rasqlTmp.substring(rasqlTmp.indexOf("encode(") + 7, rasqlTmp.indexOf(", \"png\""));
        
        // NOTE: if WMS style already contains "[" (e.g: condense + over $ts t( imageCrsDomain($c[ansi:"CRS:1"(0:3)], ansi) ) using $c[ansi($ts)])
        // It means user already picked the grid domains to be translated in WCPS query, don't add the additional subsets from bbox anymore.
        if (!collectionExpression.contains("[")) {
            for (String coverageAliasNameTmp : coverageAliasNames) {
                coverageAliasNameTmp = coverageAliasNameTmp.replace(FRAGMENT_ITERATOR_PREFIX, "");
                // e.g: replace $c0 + 5 by c0[0:20,0:30] + 5
                collectionExpression = collectionExpression.replace(coverageAliasNameTmp, coverageAliasNameTmp + gridDomain);
            }
        }

        return collectionExpression;
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
            String query = " select encode( extend(<[0:0,0:0] 0c>, [0:" + (this.width - 1) + ",0:" + (this.height - 1) + "]) , \"" + this.format + "\", \"nodata=0\") ";
            byte[] bytes = RasUtil.getRasqlResultAsBytes(query);
            response = new Response(Arrays.asList(bytes), this.format, this.layerNames.get(0));
            this.blankTileMap.put(key, response);
        }
        
        return response;       
    }
}
