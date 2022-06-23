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
package petascope.wmts.handlers.service;

import com.rasdaman.accesscontrol.service.AuthenticationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.servlet.http.HttpServletRequest;
import nu.xom.Attribute;
import nu.xom.Element;
import org.rasdaman.ApplicationMain;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.config.VersionManager;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.CoveragePyramid;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.IndexAxis;
import org.rasdaman.domain.cis.IrregularAxis;
import org.rasdaman.domain.cis.RegularAxis;
import org.rasdaman.domain.cis.Wgs84BoundingBox;
import org.rasdaman.domain.owsmetadata.OwsServiceMetadata;
import org.rasdaman.domain.owsmetadata.ServiceIdentification;
import org.rasdaman.domain.wms.BoundingBox;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.domain.wms.Style;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.CrsDefinition;
import petascope.core.GeoTransform;
import petascope.core.KVPSymbols;
import petascope.core.Pair;
import petascope.core.XMLSymbols;
import static petascope.core.XMLSymbols.ATT_HREF;
import static petascope.core.XMLSymbols.ATT_NAME;
import static petascope.core.XMLSymbols.LABEL_ALLOWED_VALUES;
import static petascope.core.XMLSymbols.LABEL_DCP;
import static petascope.core.XMLSymbols.LABEL_GET;
import static petascope.core.XMLSymbols.LABEL_HTTP;
import static petascope.core.XMLSymbols.LABEL_KVP;
import static petascope.core.XMLSymbols.LABEL_OPERATION;
import static petascope.core.XMLSymbols.LABEL_POST;
import static petascope.core.XMLSymbols.LABEL_WMS_NAME;
import static petascope.core.XMLSymbols.LABEL_WMTS_CONTENTS;
import static petascope.core.XMLSymbols.NAMESPACE_OWS;
import static petascope.core.XMLSymbols.NAMESPACE_WMTS;
import static petascope.core.XMLSymbols.NAMESPACE_XLINK;
import static petascope.core.XMLSymbols.PREFIX_OWS;
import static petascope.core.XMLSymbols.PREFIX_XLINK;
import petascope.core.gml.GMLGetCapabilitiesBuilder;
import petascope.exceptions.PetascopeException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsProjectionUtil;
import petascope.util.CrsUtil;
import petascope.util.MIMEUtil;
import petascope.util.TimeUtil;
import petascope.util.XMLUtil;
import petascope.wms.handlers.kvp.KVPWMSGetCapabilitiesHandler;
import org.rasdaman.domain.wmts.TileMatrix;
import org.rasdaman.domain.wmts.TileMatrixSet;
import org.rasdaman.repository.service.WMTSRepositoryService;
import petascope.wms.handlers.service.WMSGetMapCachingService;

/**
 * Handler for WMTS GetCapabilities service
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class WMTSGetCapabilitiesService {
    
    private static Logger log = LoggerFactory.getLogger(WMTSGetCapabilitiesService.class);
    
    @Autowired
    private WMTSRepositoryService wmtsRepositoryService;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;    
    @Autowired
    private GMLGetCapabilitiesBuilder wcsGMLGetCapabilitiesBuild;
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private KVPWMSGetCapabilitiesHandler kvpWMSGetCapabilitiesHandler;
    @Autowired
    private HttpServletRequest httpServletRequest; 
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;
    
    public static final List<String> SUPPORTED_ENCODE_FORMATS = Arrays.asList(MIMEUtil.MIME_PNG, MIMEUtil.MIME_JPEG);
    
    public static final String STYLE_DEFAULT = "default";
    
    // Any local layer has been updated by WCS-T UpdateCoverage are added here 
    // and WMTS GetCapabilities needs to create new TileMatrixSets cached objects for them
    public static final Set<String> localUpdatedLayerNames = new ConcurrentSkipListSet<>();
    
    public Element buildServiceIdentificationElement(OwsServiceMetadata owsServiceMetadata) {
        
        ServiceIdentification serviceIdentification = owsServiceMetadata.getServiceIdentification();
        
        // Service
        Element serviceElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_SERVICE_IDENTIFICATION), NAMESPACE_OWS);

        // Title
        Element titleElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMS_TITLE), NAMESPACE_OWS);
        titleElement.appendChild(serviceIdentification.getServiceTitle());
        serviceElement.appendChild(titleElement);

        // Abstract
        Element abstractElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMS_ABSTRACT), NAMESPACE_OWS);
        abstractElement.appendChild(serviceIdentification.getServiceAbstract());
        serviceElement.appendChild(abstractElement);

        // KeywordList
        if (!serviceIdentification.getKeywords().isEmpty()) {
            Element keywordsElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_KEYWORDS), NAMESPACE_OWS);
            // keyWords element can contain multiple KeyWord elements
            for (String keyWord : serviceIdentification.getKeywords()) {
                Element keyWordElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_KEYWORD), NAMESPACE_OWS);
                keyWordElement.appendChild(keyWord);

                keywordsElement.appendChild(keyWordElement);
            }

            serviceElement.appendChild(keywordsElement);
        }
        
        // ServiceType
        Element serviceTypeElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_SERVICE_TYPE), NAMESPACE_OWS);
        serviceTypeElement.appendChild("OGC WMTS");
        serviceElement.appendChild(serviceTypeElement);
        
        // ServiceTypeVersion
        Element serviceTypeVersionElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_SERVICE_TYPE_VERSION), NAMESPACE_OWS);
        serviceTypeVersionElement.appendChild(VersionManager.WMTS_VERSION_10);
        serviceElement.appendChild(serviceTypeVersionElement);

        // Fees
        if (serviceIdentification.getFees() != null) {
            Element feesElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_FEES), NAMESPACE_OWS);
            feesElement.appendChild(serviceIdentification.getFees());
            serviceElement.appendChild(feesElement);
        }

        // AccessConstraints
        if (serviceIdentification.getAccessConstraints().size() > 0) {
            Element accessContraintsElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_ACCESS_CONSTRAINTS), NAMESPACE_OWS);
            accessContraintsElement.appendChild(serviceIdentification.getAccessConstraints().get(0));
        } 

        return serviceElement;
    }
    
    public Element buildContentsElement() throws PetascopeException {
        Element contentsElement = new Element(LABEL_WMTS_CONTENTS, NAMESPACE_WMTS);
        
        // All WMS layers
        List<Layer> layers = this.wmsRepostioryService.readAllLayersFromCaches();
        
        for (Layer layer : layers) {
            Element layerElement = this.buildLayerElement(layer);
            
            if (layerElement != null) {
                contentsElement.appendChild(layerElement);
            }
        }
        
        // Create the caches for WMTS TileMatrixSet if needed first
        this.wmtsRepositoryService.buildTileMatrixSetsMapCaches(this.localUpdatedLayerNames);
        // then, remove the local layer names which has been newly recreated TileMatrixSet in cache
        for (String layerName : this.localUpdatedLayerNames) {
            this.localUpdatedLayerNames.remove(layerName);
            this.wmsGetMapCachingService.removeLayerGetMapInCache(layerName);
        }
        
        // build list of TileMatrixSet elements for list of layers
        List<Element> tileMatrixSetElements = this.buildTileMatrixSetElements(layers);
        for (Element element : tileMatrixSetElements) {
            contentsElement.appendChild(element);
        }
        
        
        return contentsElement;
    }
    
    /**
    Create a list of TileMatrixSet elements for list of layers under <Contents> element
    
    <TileMatrixSet>
        <ows:Identifier>{LAYERNAME}:EPSG:4326</ows:Identifier>
        <ows:SupportedCRS>urn:ogc:def:crs:EPSG::4326</ows:SupportedCRS>
        <TileMatrix>
            <!-- pyramid member with smaller grid domains -->
            <ows:Identifier>PYRAMID_MEMBER_1</ows:Identifier>
            <ScaleDenominator>5.590822640285016E8</ScaleDenominator>
            <TopLeftCorner>-2.0037508342787E7 2.0037508342787E7</TopLeftCorner>
            <TileWidth>256</TileWidth>
            <TileHeight>256</TileHeight>
            <MatrixWidth>1</MatrixWidth>
            <MatrixHeight>1</MatrixHeight>
        </TileMatrix>
        <TileMatrix>
            <!-- pyramid member with larger grid domains -->
            <ows:Identifier>PYRAMID_MEMBER_0 (base layer)</ows:Identifier>
            <ScaleDenominator>2.7954113201425034E8</ScaleDenominator>
            <TopLeftCorner>-2.0037508342787E7 2.0037508342787E7</TopLeftCorner>
            <TileWidth>256</TileWidth>
            <TileHeight>256</TileHeight>
            <MatrixWidth>1</MatrixWidth>
            <MatrixHeight>1</MatrixHeight>
        </TileMatrix>
    </TileMatrixSet>     
     */
    public List<Element> buildTileMatrixSetElements(List<Layer> layers) throws PetascopeException {
        List<Element> results = new ArrayList<>();
        
        for (Layer layer : layers) {
            String layerName = layer.getName();
            for (String tileMatrixSetName : this.wmtsRepositoryService.getListTileMatrixSetNames(layerName)) {
                
                TileMatrixSet tileMatrixSet = this.wmtsRepositoryService.getTileMatrixSetFromCaches(tileMatrixSetName);
                if (tileMatrixSet == null) {
                    log.warn("TileMatrixSet: " + tileMatrixSetName + " does not exist.");
                    continue;
                }
                
                // NOTE: if a WMTS layer's geoXY CRS (e.g. EPSG:32633) is not EPSG:4326, then, here it has an extra TileMatrixSet for EPSG:32633
                // e.g. test_wms:EPSG:4326
                Element tileMatrixSetElement = new Element(XMLSymbols.LABEL_WMTS_TILE_MATRIX_SET, NAMESPACE_WMTS);
                results.add(tileMatrixSetElement);

                Element tileMatrisSetIdentifierElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_IDENTIFIER), NAMESPACE_OWS);
                tileMatrixSetElement.appendChild(tileMatrisSetIdentifierElement);
                tileMatrisSetIdentifierElement.appendChild(tileMatrixSetName);

                Element supportedCrsElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_SUPPORTED_CRS), NAMESPACE_OWS);
                String code = this.wmtsRepositoryService.getCodeFromTileMatrixSetName(tileMatrixSetName);
                String supportedCrs = CrsUtil.URN_EPSG_PREFIX + code;
                
                supportedCrsElement.appendChild(supportedCrs);
                tileMatrixSetElement.appendChild(supportedCrsElement);
                
                for (TileMatrix tileMatrix : tileMatrixSet.getTileMatrixMap().values()) {
                    // NOTE: List of TileMatrices is reversed order (lower grid domains -> higher grid domains)
                    
                    // e.g test_wms_2 (pyramid member)
                    String tileMatrixName = tileMatrix.getName();
                    Element tileMatrixElement = new Element(XMLSymbols.LABEL_WMTS_TILE_MATRIX, NAMESPACE_WMTS);
                    
                    // Identifier

                    Element tileMatrixIdentifierElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_IDENTIFIER), NAMESPACE_OWS);
                    tileMatrixIdentifierElement.appendChild(tileMatrixName);
                    tileMatrixElement.appendChild(tileMatrixIdentifierElement);
                    
                    // ScaleDenominator
                    
                    Element scaleDenominatorElement = new Element(XMLSymbols.LABEL_WMTS_SCALE_DENOMINATOR, NAMESPACE_WMTS);
                    tileMatrixElement.appendChild(scaleDenominatorElement);
                    String scaleDenomitor = tileMatrix.getScaleDenominator().toPlainString();
                    scaleDenominatorElement.appendChild(scaleDenomitor);
                    
                    // TopLeftCorner
                    
                    Element topLeftCornerElement = new Element(XMLSymbols.LABEL_WMTS_TOP_LEFT_CORNER, NAMESPACE_WMTS);
                    tileMatrixElement.appendChild(topLeftCornerElement);
                    String topLeftCorner = tileMatrix.getTopLeftCorner();
                    topLeftCornerElement.appendChild(topLeftCorner);
                    
                    // TileWidth
                    
                    Element tileWidthElement = new Element(XMLSymbols.LABEL_WMTS_TILE_WIDTH, NAMESPACE_WMTS);
                    tileMatrixElement.appendChild(tileWidthElement);
                    String tileWidth = String.valueOf(tileMatrix.getTileWidth());
                    tileWidthElement.appendChild(tileWidth);
                    
                    // TileHeight
                    
                    Element tileHeightElement = new Element(XMLSymbols.LABEL_WMTS_TILE_HEIGHT, NAMESPACE_WMTS);
                    tileMatrixElement.appendChild(tileHeightElement);
                    String tileHeight = String.valueOf(tileMatrix.getTileHeight());
                    tileHeightElement.appendChild(tileHeight);
                    
                    // MatrixWidth
                    
                    Element matrixWidthElement = new Element(XMLSymbols.LABEL_WMTS_MATRIX_WIDTH, NAMESPACE_WMTS);
                    tileMatrixElement.appendChild(matrixWidthElement);
                    String matrixWidth = String.valueOf(tileMatrix.getMatrixWidth());
                    matrixWidthElement.appendChild(matrixWidth);
                    
                    // MatrixHeight
                    
                    Element matrixHeightElement = new Element(XMLSymbols.LABEL_WMTS_MATRIX_HEIGHT, NAMESPACE_WMTS);
                    tileMatrixElement.appendChild(matrixHeightElement);
                    String matrixHeight = String.valueOf(tileMatrix.getMatrixHeight());
                    matrixHeightElement.appendChild(matrixHeight);                    

                    tileMatrixSetElement.appendChild(tileMatrixElement);
                }
                
            }
        }
        
        return results;
    }
    
    private Element buildLayerElement(Layer layer) throws PetascopeException {
        String layerName = layer.getName();
        
        Element layerElement = new Element(XMLSymbols.LABEL_WMS_LAYER, NAMESPACE_WMTS);
        
        Element identifierElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_IDENTIFIER), NAMESPACE_OWS);
        identifierElement.appendChild(layerName);
        layerElement.appendChild(identifierElement);
        
        Element titleElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_TITLE), NAMESPACE_OWS);
        titleElement.appendChild(layer.getTitle());
        layerElement.appendChild(titleElement);
        
        Element abstractElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_ABSTRACT), NAMESPACE_OWS);
        abstractElement.appendChild(layer.getLayerAbstract());
        layerElement.appendChild(abstractElement);
        
        // KeywordList
        if (layer.getKeywordList().size() > 0) {
            Element keywordListElement = this.kvpWMSGetCapabilitiesHandler.buildKeywordListElement(layer.getKeywordList());
            layerElement.appendChild(keywordListElement);
        }
        
        this.buildBBoxElements(layer, layerElement);
        
        this.buildStyleElements(layer, layerElement);
        
        this.buildFormatElements(layerElement);
        
        this.buildTileMatrixSetLinkElements(layer, layerElement);
        
        this.buildDimensionElements(layer, layerElement);
        
        this.buildLayerElementMetadata(layer, layerElement);
        
        
        return layerElement;
    }
    

    /**
    Each layer should have a reference to MatrixSetLink EPSG:4326
    and a reference to MatrixSetLink in layer's native CRS (if the nativeCRS is different from EPSG:4326)
    
    <TileMatrixSetLink>
        <TileMatrixSet>{LAYER_NAME}:EPSG:4326</TileMatrixSet>
    </TileMatrixSetLink>
    <TileMatrixSetLink>
        <TileMatrixSet>{LAYER_NAME}:EPSG:32632</TileMatrixSet>
    </TileMatrixSetLink>
    
    */
    private void buildTileMatrixSetLinkElements(Layer layer, Element layerElement) throws PetascopeException {
        String layerName = layer.getName();
        List<String> tileMatrixSetNames = this.wmtsRepositoryService.getListTileMatrixSetNames(layerName);
        
        for (String tileMatrixSetName : tileMatrixSetNames) {
            // e.g: test_layer:EPSG:4326
            Element tileMatrixSetLinkElement = new Element(XMLSymbols.LABEL_WMTS_TILE_MATRIX_SET_LINK, NAMESPACE_WMTS);
            Element tileMatrixSetElement = new Element(XMLSymbols.LABEL_WMTS_TILE_MATRIX_SET, NAMESPACE_WMTS);
            tileMatrixSetLinkElement.appendChild(tileMatrixSetElement);
            
            tileMatrixSetElement.appendChild(tileMatrixSetName);
            
            layerElement.appendChild(tileMatrixSetLinkElement);
        }
    }
    
    /**
     * Build ows:WGS84BoundingBox and the BoundingBox element in layer's CRS     
     * 
     <ows:WGS84BoundingBox>
        <ows:LowerCorner>12.999446822650462 46.722110379286</ows:LowerCorner>
        <ows:UpperCorner>13.308182612644663 46.91359611878293</ows:UpperCorner>
    </ows:WGS84BoundingBox>
     */
    private void buildBBoxElements(Layer layer, Element layerElement) throws PetascopeException {
        String layerName = layer.getName();
        GeneralGridCoverage coverage = (GeneralGridCoverage) this.coverageRepositoryService.readCoverageFullMetadataByIdFromCache(layerName);
        
        // build wgs84BBox (Long Lat order)
        
        Wgs84BoundingBox wgs84BBox = coverage.getEnvelope().getEnvelopeByAxis().getWgs84BBox();
        
        Element wgs84BoundingBoxElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_WGS84_BOUNDING_BOX), NAMESPACE_OWS);
        Element lowerCornerElement1 = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_LOWER_CORNER), NAMESPACE_OWS);
        String lowerBounds1 = wgs84BBox.getMinLong() + " " + wgs84BBox.getMinLat();
        lowerCornerElement1.appendChild(lowerBounds1);
        wgs84BoundingBoxElement.appendChild(lowerCornerElement1);
        
        Element upperCornerElement1 = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_UPPER_CORNER), NAMESPACE_OWS);
        String upperBounds1 = wgs84BBox.getMaxLong()+ " " + wgs84BBox.getMaxLat();
        upperCornerElement1.appendChild(upperBounds1);
        wgs84BoundingBoxElement.appendChild(upperCornerElement1);
        
        layerElement.appendChild(wgs84BoundingBoxElement);
        
        // build layer's native CRS bbox (e.g EPSG:4326 Lat Long order)
        BoundingBox bbox = layer.getBoundingBoxes().get(0);
        String code = CrsUtil.getCode(bbox.getCrs());
        
        Element bboxElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_BOUNDING_BOX), NAMESPACE_OWS);
        Attribute crsAttribute = new Attribute(XMLSymbols.ATT_CRS, CrsUtil.URN_EPSG_PREFIX + code);
        bboxElement.addAttribute(crsAttribute);
        
        Element lowerCornerElement2 = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_LOWER_CORNER), NAMESPACE_OWS);
        String lowerBounds2 = bbox.getXMin() + " " + bbox.getYMin();
        lowerCornerElement2.appendChild(lowerBounds2);
        bboxElement.appendChild(lowerCornerElement2);
        
        Element upperCornerElement2 = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_UPPER_CORNER), NAMESPACE_OWS);
        String upperBounds2 = bbox.getXMax() + " " + bbox.getYMax();
        
        upperCornerElement2.appendChild(upperBounds2);
        bboxElement.appendChild(upperCornerElement2);
        
        layerElement.appendChild(bboxElement);
        
    }
    
    /**
     * Build additional metadata elements for LayerElement
     */
    private void buildLayerElementMetadata(Layer layer, Element layerElement) throws PetascopeException {
        String layerName = layer.getName();
        Coverage coverage = this.coverageRepositoryService.readCoverageFromLocalCache(layerName);
        
        if (coverage == null) {
            // Instead of throwing exception, just log warning to return the result of WMS GetCapabilities
            if (ApplicationMain.COVERAGES_CACHES_LOADED) {
                // NOTE: this warn shows only when all coverages and layers loaded to caches after petascope starts
                log.warn("Coverage associated with the layer: " + layer.getName() + " does not exist.");
            }
            return;
        } else {
            Element customizedMetadataElement = this.wcsGMLGetCapabilitiesBuild.createCustomizedCoverageMetadataElement(coverage);
           
            if (customizedMetadataElement != null) {
                layerElement.appendChild(customizedMetadataElement);
            }
        }
    }
    
    /**
     * Build a list of styles for this element
     */
    private void buildStyleElements(Layer layer, Element layerElement) throws PetascopeException {
        if (layer.getStyles().size() > 0) {
            for (Style style : layer.getStyles()) {
                Element styleElement = this.kvpWMSGetCapabilitiesHandler.getStyleElement(layer, style);
                Element nameElement = styleElement.getFirstChildElement(LABEL_WMS_NAME, XMLSymbols.NAMESPACE_WMS);
                styleElement.removeChild(nameElement);

                Element identifierElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_IDENTIFIER), NAMESPACE_OWS);
                identifierElement.appendChild(style.getName());
                styleElement.appendChild(identifierElement);

                layerElement.appendChild(styleElement);
            }
        } else {
            // NOTE: In WMTs, in case layer has no style, then it still contains a Style element with the default text
            Element styleElement = new Element(XMLSymbols.LABEL_WMS_STYLE, NAMESPACE_WMTS);
            Element identifierElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_WMTS_IDENTIFIER), NAMESPACE_OWS);
            identifierElement.appendChild(this.STYLE_DEFAULT);
            styleElement.appendChild(identifierElement);

            layerElement.appendChild(styleElement);
        }
    }
    
    /**
     * Output for GetTile request
     */
    private void buildFormatElements(Element layerElement) {
        for (String format : this.SUPPORTED_ENCODE_FORMATS) {
            Element formatElement = new Element(XMLSymbols.LABEL_WMTS_FORMAT, NAMESPACE_WMTS);
            formatElement.appendChild(format);
            layerElement.appendChild(formatElement);
        }
    }
    
    /**
     * Build non-XY axes elements
     */
    private void buildDimensionElements(Layer layer, Element layerElement) throws PetascopeException {
        String layerName = layer.getName();
        GeneralGridCoverage coverage = (GeneralGridCoverage) this.coverageRepositoryService.readCoverageFullMetadataByIdFromCache(layerName);
        List<GeoAxis> nonXYAxes = coverage.getNonXYGeoAxes();
        
        for (GeoAxis axis : nonXYAxes) {
            String axisLabel = axis.getAxisLabel();
            if (axis.isTimeAxis()) {
                axisLabel = KVPSymbols.KEY_WMS_TIME;
            } else if (axis.isElevationAxis()) {
                axisLabel = KVPSymbols.KEY_WMS_ELEVATION;
            }
            
            Element dimensionElement = new Element(XMLSymbols.LABEL_WMTS_DIMENSION, NAMESPACE_WMTS);
            layerElement.appendChild(dimensionElement);
            
            Element identifierElement = new Element(XMLSymbols.LABEL_WMTS_IDENTIFIER);
            identifierElement.appendChild(axisLabel);
            dimensionElement.appendChild(identifierElement);
            
            Element defaultElement = new Element(XMLSymbols.LABEL_WMTS_DEFAULT);
            String upperBound = axis.getUpperBound();
            defaultElement.appendChild(upperBound);
            dimensionElement.appendChild(defaultElement);
            
            // See Table 9 — Parts of Dimension data structure (WMTS standard)
            if (axis instanceof RegularAxis) {
                Element lowerBoundValueElement = new Element(XMLSymbols.LABEL_WMTS_VALUE);
                lowerBoundValueElement.appendChild(axis.getLowerBound());
                dimensionElement.appendChild(lowerBoundValueElement);
                
                Element upperBoundValueElement = new Element(XMLSymbols.LABEL_WMTS_VALUE);
                upperBoundValueElement.appendChild(axis.getUpperBound());
                dimensionElement.appendChild(upperBoundValueElement);
            } else {
                // irregular axis, list all coefficients as values
                // WMTS standard (doc: c Repeat this parameter for each available value for this dimension)
                
                IrregularAxis irregularAxis = (IrregularAxis)axis;
                CrsDefinition crsDefinition = CrsUtil.getCrsDefinition(irregularAxis.getSrsName());
                
                for (String coefficient : irregularAxis.getDirectPositions()) {
                    Element valueElement = new Element(XMLSymbols.LABEL_WMTS_VALUE);
                    
                    if (!irregularAxis.isTimeAxis()) {
                        valueElement.appendChild(coefficient);
                    } else {
                        BigDecimal adjustedCoefficient = irregularAxis.getLowerBoundNumber().add(new BigDecimal(coefficient));
                        String dateTimeValue = TimeUtil.valueToISODateTime(BigDecimal.ZERO, adjustedCoefficient, crsDefinition);
                        valueElement.appendChild(dateTimeValue);
                    }
                    
                    dimensionElement.appendChild(valueElement);
                }                
            }
           
        }
    }
    
    public Element buildOperationsMetadataElement() {
        Element operationsMetadataElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, XMLSymbols.LABEL_OPERATIONS_METADATA), NAMESPACE_OWS);
        
        Element getCapabilitiesOperationElement = this.buildOperationElement(KVPSymbols.VALUE_WMTS_GET_CAPABILITIES);
        Element getTileElement = this.buildOperationElement(KVPSymbols.VALUE_WMTS_GET_TILE);
        
        operationsMetadataElement.appendChild(getCapabilitiesOperationElement);
        operationsMetadataElement.appendChild(getTileElement);
        
        return operationsMetadataElement;
    }
    
    /**
     * Build ows:Operation element for OperationsMetadata element. e.g:
     * 
     <ows:Operation name="GetCapabilities">
        <ows:DCP>
            <ows:HTTP>

                <ows:Get xlink:href="http://localhost:8080/rasdaman/ows">
                    <ows:Constraint name="GetEncoding">
                        <ows:AllowedValues>
                            <ows:Value>KVP</ows:Value>
                        </ows:AllowedValues>
                    </ows:Constraint>
                </ows:Get>

                <ows:Post xlink:href="http://localhost:8080/rasdaman/ows">
                    <ows:Constraint name="PostEncoding">
                        <ows:AllowedValues>
                            <ows:Value>KVP</ows:Value>
                        </ows:AllowedValues>
                    </ows:Constraint>
                </ows:Post>
                
            </ows:HTTP>
        </ows:DCP>
    </ows:Operation
     */
    private Element buildOperationElement(String operationName) {
        
        Element operationElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_OPERATION), NAMESPACE_OWS);
        
        Attribute nameAttribute = new Attribute(ATT_NAME, NAMESPACE_OWS);
        // e.g. GetCapabilities
        nameAttribute.setValue(operationName);
        operationElement.addAttribute(nameAttribute);
        
        Element dcpElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_DCP), NAMESPACE_OWS);
        Element httpElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_HTTP), NAMESPACE_OWS);
        
        // GET request
        Element getElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_GET), NAMESPACE_OWS);
        Attribute getHrefAttribute = XMLUtil.createXMLAttribute(NAMESPACE_XLINK, PREFIX_XLINK, ATT_HREF, ConfigManager.PETASCOPE_ENDPOINT_URL);
        getElement.addAttribute(getHrefAttribute);
        
        Element allowedValuesElement1 = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_ALLOWED_VALUES), NAMESPACE_OWS);
        Element valueElement1 = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_KVP), NAMESPACE_OWS);
        allowedValuesElement1.appendChild(valueElement1);
        
        getElement.appendChild(allowedValuesElement1);
        
        httpElement.appendChild(getElement);
        
        // POST request
        Element postElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_POST), NAMESPACE_OWS);
        Attribute postHrefAttribute = XMLUtil.createXMLAttribute(NAMESPACE_XLINK, PREFIX_XLINK, ATT_HREF, ConfigManager.PETASCOPE_ENDPOINT_URL);
        postElement.addAttribute(postHrefAttribute);
        
        Element allowedValuesElement2 = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_ALLOWED_VALUES), NAMESPACE_OWS);
        Element valueElement2 = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_KVP), NAMESPACE_OWS);
        allowedValuesElement2.appendChild(valueElement2);
        
        postElement.appendChild(allowedValuesElement2);
        
        httpElement.appendChild(postElement);
        
        dcpElement.appendChild(httpElement);
        
        operationElement.appendChild(dcpElement);
        
        return operationElement;
    }
    
    // -------- Utility methods
}
