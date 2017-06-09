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
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wms.handlers.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.rasdaman.domain.wms.BoundingBox;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.core.AxisTypes;
import petascope.core.CrsDefinition;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.CrsProjectionUtil;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.MIMEUtil;
import petascope.util.ras.RasUtil;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.ParsedSubset;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.metadata.service.CoordinateTranslationService;
import petascope.wcps2.metadata.service.WcpsCoverageMetadataTranslator;
import petascope.wms.exception.WMSLayerNotExistException;
import petascope.wms.exception.WMSStyleNotExistException;

/**
 * Class to generate Rasql queries from all the requesting layers, styles
 * (RasqlFragments), e.g: case $Iterator when ($Iterator + 2) > 20 then
 * {255,0,0} when ($Iterator + 5 + 25 - 25) > 10+5 then {0,255,0} when
 * (2+$Iterator+0.5-0.25*(0.5+5)) < 10-5+2 then {0,0,255} else {0,0,0} end
 *
 * then $Iterator is replaced by an expression:
 * SCALE(test_wms_4326[0:221,1:176], [0:599, 0:599]) @author <a
 * href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WMSRasqlGeneratorService {

    @Autowired
    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslator;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private CoordinateTranslationService coordinateTranslationService;

    private static Logger log = LoggerFactory.getLogger(WMSRasqlGeneratorService.class);

    // NOTE: Dimensions can be more than 2D
    private static final String DEFAULT_RASQL_STYLE = "SCALE($layerName[$domains], [0:$width,0:$height])";
    private static final String PROJECTION_TEMPLATE = "project($coverageExpression, \"$xmin, $ymin, $xmax, $ymax\", \"$sourceCRS\", \"$targetCRS\")";
    private static final String RASQL_TEMPLATE = "SELECT ENCODE($coverageExpression, \"$formatType\", \"$nodata\") FROM $coverages";
    private static final String ITERATOR = "$Iterator";
    private static final String OVERLAY_LAYER = " OVERLAY ";

    private List<String> layerNames;
    private List<String> styleNames;
    private String outputCRS;
    private int width;
    private int height;
    // MIME type (e.g: image/png)
    private String format;
    private boolean transparent;
    // BBox already translated from requesting CRS to native CRS of XY geo-referenced axes
    private BoundingBox bbox;

    public WMSRasqlGeneratorService() {

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

    public void setBBox(BoundingBox bbox) {
        this.bbox = bbox;
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

    /**
     * Create the response for the GetMap request
     *
     * @return
     * @throws petascope.exceptions.PetascopeException
     * @throws petascope.exceptions.SecoreException
     * @throws petascope.wms.exception.WMSLayerNotExistException
     * @throws petascope.wms.exception.WMSStyleNotExistException
     */
    public Response createGetMapResponse() throws PetascopeException, SecoreException, WMSLayerNotExistException, WMSStyleNotExistException {
        // Each layer has 1 requesting style in select Rasql
        Map<String, String> layerStyleRasqlMap = new LinkedHashMap<>();
        // NOTE: As WMS layer is a WCS coverage so reuse the functionalities from WCS coverage metadata
        int i = 0;
        String nativeCRS = null;
        for (String layerName : layerNames) {
            WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(layerName);
            List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
            nativeCRS = CrsUtil.getEPSGCode(xyAxes.get(0).getNativeCrsUri());
            // If request is in YX order for bounding box (e.g: EPSG:4326 Lat, Long, swap it to XY order Long, Lat)
            bbox = this.swapYXBoundingBox(bbox, outputCRS);
            // Transform the bounding box from requesting CRS (e.g: EPSG:3857) to native CRS (e.g: EPSG:4326)
            if (!outputCRS.equals(nativeCRS)) {
                // Transform the bounding box and set the nativeCRS to requesting CRS (i.e: reprojection in Rasql)
                bbox = this.transformBoundingBox(bbox, outputCRS, nativeCRS);
            }

            // Then generate the rasql query for this layer 
            // NOTE: styleName can be empty if GetMap request with default styles for all layers
            String styleName = "";
            if (!this.styleNames.isEmpty()) {
                styleName = this.styleNames.get(i);
                i++;
            }

            // Collect all the rasql styles for all layers
            String styleRasql = this.getLayerStyleRasql(wcpsCoverageMetadata, layerName, styleName, bbox);
            layerStyleRasqlMap.put(layerName, styleRasql);
        }

        // Now create a RASQL query which combines all the layers with the OVERLAY operator
        // For SELECT
        List<String> layerStyleRasqls = new ArrayList<>(layerStyleRasqlMap.values());
        String coverageExpression = ListUtil.join(layerStyleRasqls, OVERLAY_LAYER);
        String nodata = this.transparent ? "nodata=0" : "";
        // For FROM        
        String coverages = ListUtil.join(this.layerNames, ",");

        // Final Rasql from all the styles, layers
        if (!nativeCRS.equals(outputCRS)) {
            // It needs to be projected when the requesting CRS is different from the geo-referenced XY axes
            coverageExpression = PROJECTION_TEMPLATE.replace("$coverageExpression", coverageExpression)
                    .replace("$xmin", this.bbox.getMinx().toPlainString())
                    .replace("$ymin", this.bbox.getMiny().toPlainString())
                    .replace("$xmax", this.bbox.getMaxx().toPlainString())
                    .replace("$ymax", this.bbox.getMaxy().toPlainString())
                    .replace("$sourceCRS", nativeCRS)
                    .replace("$targetCRS", this.outputCRS);
        }

        String rasqlQuery = RASQL_TEMPLATE.replace("$coverageExpression", coverageExpression)
                .replace("$formatType", MIMEUtil.getEncodingType(this.format).get(0))
                .replace("$nodata", nodata)
                .replace("$coverages", coverages);
        byte[] bytes = RasUtil.getRasqlResultAsBytes(rasqlQuery);

        return new Response(Arrays.asList(bytes), this.format);
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
        BigDecimal minX = inputBBox.getMinx();
        BigDecimal minY = inputBBox.getMiny();
        BigDecimal maxX = inputBBox.getMaxx();
        BigDecimal maxY = inputBBox.getMaxy();
        if (crsDefinition.getAxes().get(0).getType().equals(AxisTypes.Y_AXIS)) {
            // CRS axis is YX order so must swap the input bbox to XY order (e.g: EPSG:4326 (Lat, Long) to Long, Lat.
            BigDecimal minTemp = minX;
            BigDecimal maxTemp = maxX;
            minX = minY;
            minY = minTemp;
            maxX = maxY;
            maxY = maxTemp;

            inputBBox.setMinx(minX);
            inputBBox.setMiny(minY);
            inputBBox.setMaxx(maxX);
            inputBBox.setMaxy(maxY);
        }

        return inputBBox;

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

        BoundingBox bbox = new BoundingBox();
        // Beware! Inserted values pairs needs to be in order X-coordinate and then Y-coordinate.
        // If you are inserting latitude/longitude values in decimal format, then the longitude should be first value of the pair (X-coordinate) and latitude the second value (Y-coordinate).        
        double minX = inputBBox.getMinx().doubleValue();
        double minY = inputBBox.getMiny().doubleValue();
        double maxX = inputBBox.getMaxx().doubleValue();
        double maxY = inputBBox.getMaxy().doubleValue();

        // NOTE: GDAL transform returns to XY order (e.g: EPSG:3857 (XY) -> EPSG:4326 (also XY))        
        double[] minXY = new double[]{minX, minY};
        List<BigDecimal> minValues = CrsProjectionUtil.transform(sourceCrs, targetCrs, minXY);
        double[] maxXY = new double[]{maxX, maxY};
        List<BigDecimal> maxValues = CrsProjectionUtil.transform(sourceCrs, targetCrs, maxXY);

        bbox.setMinx(minValues.get(0));
        bbox.setMiny(minValues.get(1));
        bbox.setMaxx(maxValues.get(0));
        bbox.setMaxy(maxValues.get(1));

        return bbox;
    }

    /**
     * Return the Rasql query for the layer by the Rasql fragment query style
     * for a layer
     *
     * @return
     */
    private String getLayerStyleRasql(WcpsCoverageMetadata wcpsCoverageMetadata, String layerName,
            String styleName, BoundingBox bbox) throws WMSLayerNotExistException, WMSStyleNotExistException {
        String styleRasql = DEFAULT_RASQL_STYLE;

        // @TODO: Only supports 2D coverage now
        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
        ParsedSubset<BigDecimal> geoDomainSubsetX = new ParsedSubset<>(bbox.getMinx(), bbox.getMaxx());
        ParsedSubset<BigDecimal> geoDomainSubsetY = new ParsedSubset<>(bbox.getMiny(), bbox.getMaxy());

        Axis axisX = xyAxes.get(0);
        BigDecimal geoDomainMinX = axisX.getGeoBounds().getLowerLimit();
        BigDecimal geoDomainMaxX = axisX.getGeoBounds().getUpperLimit();

        Axis axisY = xyAxes.get(1);
        BigDecimal geoDomainMinY = axisY.getGeoBounds().getLowerLimit();
        BigDecimal geoDomainMaxY = axisY.getGeoBounds().getUpperLimit();

        // ---------- X AXIS ---------------
        // NOTE: if extent from bbox is larger than the domain of geo axis, then set the extent to min and max of geo bound
        if (geoDomainSubsetX.getLowerLimit().compareTo(geoDomainMinX) < 0) {
            geoDomainSubsetX.setLowerLimit(geoDomainMinX);
        }
        if (geoDomainSubsetX.getUpperLimit().compareTo(geoDomainMaxX) > 0) {
            geoDomainSubsetX.setUpperLimit(geoDomainMaxX);
        }

        BigDecimal resolutionX = axisX.getResolution();
        BigDecimal gridDomainMinX = axisX.getGridBounds().getLowerLimit();

        ParsedSubset<Long> gridDomainSubsetX = coordinateTranslationService.geoToGridForRegularAxis(geoDomainSubsetX, geoDomainMinX,
                geoDomainMaxX, resolutionX, gridDomainMinX);

        // ---------- Y AXIS ---------------        
        // NOTE: if extent from bbox is larger than the domain of geo axis, then set the extent to min and max of geo bound        
        if (geoDomainSubsetY.getLowerLimit().compareTo(geoDomainMinY) < 0) {
            geoDomainSubsetY.setLowerLimit(geoDomainMinY);
        }
        if (geoDomainSubsetY.getUpperLimit().compareTo(geoDomainMaxY) > 0) {
            geoDomainSubsetY.setUpperLimit(geoDomainMaxY);
        }
        BigDecimal resolutionY = axisY.getResolution();
        BigDecimal gridDomainMinY = axisY.getGridBounds().getLowerLimit();

        ParsedSubset<Long> gridDomainSubsetY = coordinateTranslationService.geoToGridForRegularAxis(geoDomainSubsetY, geoDomainMinY,
                geoDomainMaxY, resolutionY, gridDomainMinY);

        // NOTE: coverage uses CRS order (e.g: EPSG:4326 is Lat (X), Long (Y)) but the grid order is Long (X), Lat (Y)
        // So need to list them by rasdaman order.
        Map<Integer, ParsedSubset<Long>> gridDomainMap = new TreeMap<>();
        gridDomainMap.put(axisX.getRasdamanOrder(), gridDomainSubsetX);
        gridDomainMap.put(axisY.getRasdamanOrder(), gridDomainSubsetY);

        // Create the rasql style from the list of grid domains
        List<String> sortedGridDomains = new ArrayList<>();
        for (Integer key : gridDomainMap.keySet()) {
            ParsedSubset<Long> subset = gridDomainMap.get(key);
            sortedGridDomains.add(subset.getLowerLimit() + ":" + subset.getUpperLimit());
        }

        String domains = ListUtil.join(sortedGridDomains, ",");
        styleRasql = styleRasql.replace("$layerName", layerName);
        styleRasql = styleRasql.replace("$domains", domains);
        styleRasql = styleRasql.replace("$width", String.valueOf(this.width - 1));
        styleRasql = styleRasql.replace("$height", String.valueOf(this.height - 1));

        if (!styleName.isEmpty()) {
            // StyleName is inserted with a Rasql fragment
            String styleRasqlTemplate = this.wmsRepostioryService.readLayerByNameFromCache(layerName).getStyle(styleName).getRasqlQueryTransformer();
            if (!styleRasqlTemplate.isEmpty()) {
                // It is a customized style with $iterator
                styleRasql = styleRasqlTemplate.replace(ITERATOR, styleRasql);
            }
        }

        return styleRasql;
    }
}
