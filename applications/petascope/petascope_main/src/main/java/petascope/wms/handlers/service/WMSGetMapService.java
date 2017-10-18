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
import java.util.List;
import org.hsqldb.lib.StringUtil;
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
import petascope.core.response.Response;
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
import petascope.wms.exception.WMSInternalException;
import petascope.wms.exception.WMSStyleNotExistException;

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
 * <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
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
    private CoverageRepostioryService coverageRepostioryService;

    // In case of nativeCrs of layer (coverage) is different from outputCrs of GetMap request, then it needs to reproject $coverageExpression from sourceCrs to targetCrs.
    private static final String PROJECTION_TEMPLATE = "project($coverageExpression, \"$xMin, $yMin, $xMax, $yMax\", \"$sourceCRS\", \"$targetCRS\")";
    private static final String SUBSET_COVERAGE_EXPRESSION_TEMPLATE = "( $coverageExpression )[$gridXMin:$gridXMax, $gridYMin:$gridYMax]";
    private static final String FINAL_TRANSLATED_RASQL_TEMPLATE = "SELECT ENCODE($coverageExpression, \"$formatType\", \"$nodata\") FROM $collections";
    private static final String ALIAS_NAME = "c";
    private static final String WCPS_COVERAGE_ALIAS = "$c";

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
    private BoundingBox bbox;

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

    public void setBBox(BoundingBox bbox) throws PetascopeException, SecoreException {
        // If request is in YX order for bounding box (e.g: EPSG:4326 Lat, Long, swap it to XY order Long, Lat)
        // NOTE: as all layers requested with same outputCRS so only do this one time
        this.bbox = this.swapYXBoundingBox(bbox, outputCRS);
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
     * Create the response for the GetMap request. NOTE: As WMS layer is a WCS
     * coverage so reuse the functionalities from WCS coverage metadata
     *
     * @return
     * @throws petascope.exceptions.WMSException
     */
    public Response createGetMapResponse() throws WMSException {
        byte[] bytes = null;
        try {
            List<String> coverageExpressions = new ArrayList<>();
            List<String> collectionAlias = new ArrayList<>();
            int i = 0;

            for (String layerName : layerNames) {
                String aliasName = ALIAS_NAME + "" + i;
                // Then generate the rasql query for this layer 
                // NOTE: styleName can be empty if GetMap request with default styles for all layers
                String styleName = "";
                if (!this.styleNames.isEmpty()) {
                    styleName = this.styleNames.get(i);
                }

                // Collect the translated rasql query for the current layer's style
                Style style = this.wmsRepostioryService.readLayerByNameFromCache(layerName).getStyle(styleName);
                // NOTE: style can be null when just insert a new layer without style, but if styleName is not empty, then it is invalid request.
                if (style == null && !styleName.isEmpty()) {
                    throw new WMSStyleNotExistException(styleName, layerName);
                }
                // CoverageExpression is the main part of a Rasql query builded from the current layer and style
                // e.g: c1 + 5, case c1 > 5 then {0, 1, 2}
                String coverageExpression;

                if (style == null) {
                    // NOTE: in case of Style is empty, it still need to create a Rasql for the scale(layer[bbox], [width, height])
                    coverageExpression = aliasName;
                } else if (!StringUtil.isEmpty(style.getWcpsQueryFragment())) {
                    // wcpsQueryFragment
                    coverageExpression = this.buildCoverageExpressionByWCPSQueryFragment(aliasName, layerName, styleName);
                } else {
                    // rasqlTransformFragment
                    coverageExpression = this.buildCoverageExpressionByRasqlTransformFragment(aliasName, layerName, styleName);
                }
                // Add the translated Rasql query for the style to combine later
                coverageExpressions.add("( " + coverageExpression + " )");

                // a layer is equivalent to a rasdaman collection
                String collectionName = this.coverageRepostioryService.readCoverageFullMetadataByIdFromCache(layerName).getRasdamanRangeSet().getCollectionName();
                collectionAlias.add(collectionName + " as " + aliasName);

                i++;
            }

            // All requesting layers (coverages) should have same axes's domains and nativeCrss.
            WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(layerNames.get(0));
            List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
            String nativeCRS = CrsUtil.getEPSGCode(xyAxes.get(0).getNativeCrsUri());
            if (!this.outputCRS.equalsIgnoreCase(nativeCRS)) {
                // First, transform from the request BBox in outputCrs to nativeCrs to calculate the grid bounds on the current coverages
                // e.g: coverage's nativeCRS is EPSG:4326 and request BBox which contains coordinates in EPSG:3857
                this.bbox = this.transformBoundingBox(this.bbox, this.outputCRS, nativeCRS);
            }
            ParsedSubset<BigDecimal> geoDomainSubsetX = new ParsedSubset<>(bbox.getXMin(), bbox.getXMax());
            ParsedSubset<BigDecimal> geoDomainSubsetY = new ParsedSubset<>(bbox.getYMin(), bbox.getYMax());

            Axis axisX = xyAxes.get(0);
            BigDecimal geoDomainMinX = axisX.getGeoBounds().getLowerLimit();
            BigDecimal geoDomainMaxX = axisX.getGeoBounds().getUpperLimit();

            Axis axisY = xyAxes.get(1);
            BigDecimal geoDomainMinY = axisY.getGeoBounds().getLowerLimit();
            BigDecimal geoDomainMaxY = axisY.getGeoBounds().getUpperLimit();

            BigDecimal resolutionX = axisX.getResolution();
            BigDecimal gridDomainMinX = axisX.getGridBounds().getLowerLimit();

            ParsedSubset<Long> gridDomainSubsetX = coordinateTranslationService.geoToGridForRegularAxis(geoDomainSubsetX, geoDomainMinX,
                    geoDomainMaxX, resolutionX, gridDomainMinX);

            BigDecimal resolutionY = axisY.getResolution();
            BigDecimal gridDomainMinY = axisY.getGridBounds().getLowerLimit();

            ParsedSubset<Long> gridDomainSubsetY = coordinateTranslationService.geoToGridForRegularAxis(geoDomainSubsetY, geoDomainMinY,
                    geoDomainMaxY, resolutionY, gridDomainMinY);

            // Now create a final coverageExpression which combines all the translated coverageExpression for styles with the OVERLAY operator            
            String combinedCoverageExpression = ListUtil.join(coverageExpressions, " OVERLAY ");
            // e.g: (c + 1)[0:20, 30:45]
            String subsetCoverageExpression = SUBSET_COVERAGE_EXPRESSION_TEMPLATE
                    .replace("$coverageExpression", combinedCoverageExpression)
                    .replace("$gridXMin", gridDomainSubsetX.getLowerLimit().toString())
                    .replace("$gridYMin", gridDomainSubsetY.getLowerLimit().toString())
                    .replace("$gridXMax", gridDomainSubsetX.getUpperLimit().toString())
                    .replace("$gridYMax", gridDomainSubsetY.getUpperLimit().toString());

            String scaleCoverageExpression = "Scale( " + subsetCoverageExpression + ", [0:" + (this.width - 1) + ", 0:" + (this.height - 1) + "] )";
            // Final Rasql from all the styles, layers
            if (!nativeCRS.equals(outputCRS)) {
                // It needs to be projected when the requesting CRS is different from the geo-referenced XY axes
                scaleCoverageExpression = PROJECTION_TEMPLATE.replace("$coverageExpression", scaleCoverageExpression)
                        .replace("$xMin", this.bbox.getXMin().toPlainString())
                        .replace("$yMin", this.bbox.getYMin().toPlainString())
                        .replace("$xMax", this.bbox.getXMax().toPlainString())
                        .replace("$yMax", this.bbox.getYMax().toPlainString())
                        .replace("$sourceCRS", nativeCRS)
                        .replace("$targetCRS", this.outputCRS);
            }

            String nodata = this.transparent ? "nodata=0" : "";
            String formatType = MIMEUtil.getFormatType(this.format);
            String collections = ListUtil.join(collectionAlias, ", ");

            // Create the final Rasql query for all layers's styles of this GetMap request.
            String finalRasqlQuery = FINAL_TRANSLATED_RASQL_TEMPLATE
                    .replace("$coverageExpression", scaleCoverageExpression)
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
        if (crsDefinition.getAxes().get(0).getType().equals(AxisTypes.Y_AXIS)) {
            // CRS axis is YX order so must swap the input bbox to XY order (e.g: EPSG:4326 (Lat, Long) to Long, Lat.
            BigDecimal minTemp = minX;
            BigDecimal maxTemp = maxX;
            minX = minY;
            minY = minTemp;
            maxX = maxY;
            maxY = maxTemp;

            inputBBox.setXMin(minX);
            inputBBox.setYMin(minY);
            inputBBox.setXMax(maxX);
            inputBBox.setYMax(maxY);
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
        coverageExpression = style.getRasqlQueryTransformFragment().replace("$Iterator", coverageAlias);

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
