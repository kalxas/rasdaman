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
package petascope.wcps.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import petascope.util.CrsUtil;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.rasdaman.config.ConfigManager;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.core.AxisTypes;
import petascope.core.BoundingBox;
import petascope.core.CrsDefinition;
import petascope.core.GeoTransform;
import petascope.core.Pair;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsProjectionUtil;
import petascope.util.StringUtil;
import petascope.util.ras.RasUtil;
import petascope.wcps.exception.processing.IdenticalAxisNameInCrsTransformException;
import petascope.wcps.exception.processing.InvalidOutputCrsProjectionInCrsTransformException;
import petascope.wcps.exception.processing.Not2DCoverageForCrsTransformException;
import petascope.wcps.exception.processing.Not2DXYGeoreferencedAxesCrsTransformException;
import petascope.wcps.exception.processing.NotGeoReferenceAxisNameInCrsTransformException;
import petascope.wcps.exception.processing.NotIdenticalCrsInCrsTransformException;
import static petascope.wcps.handler.AbstractOperatorHandler.checkOperandIsCoverage;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.CrsTransformTargetGeoXYBoundingBox;
import petascope.wcps.metadata.model.CrsTransformTargetGeoXYResolutions;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.RegularAxis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/**
 * Class to handle an crsTransform coverage expression  <code>
 * encode(
 *      crsTransform($c, {Lat:"http://localhost:8080/def/crs/epsg/0/4326", Long:"http://localhost:8080/def/crs/epsg/0/4326"),
 *                       {"near"},
 * "tiff", "NODATA=0")
 * </code> returns a Rasql query  <code>
 * encode(project(c, {20,30,40,50}, "EPSG:3857, EPSG:4326"),
 *        "xmin=1000,ymin=15000,xmax=2000,ymax=25000", "tiff", "NODATA=0")
 * </code>
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CrsTransformHandler extends Handler {
    
    public static final String OPERATOR = "crsTransform";
    
    public CrsTransformHandler() {
        
    }
    
    public CrsTransformHandler create(Handler coverageExpressionHandler,
                                    StringScalarHandler axisLabelXHandler, StringScalarHandler crsXHandler,
                                    StringScalarHandler axisLabelYHandler, StringScalarHandler crsYHandler,
                                    StringScalarHandler interpolationTypeHandler,
                                    CrsTransformTargetGeoXYResolutionsHandler targetGeoXYResolutionsHandler,
                                    CrsTransformTargetGeoXYBoundingBoxHandler targetGeoXYBoundingBoxHandler) {
        
        CrsTransformHandler result = new CrsTransformHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, 
                            axisLabelXHandler, crsXHandler,
                            axisLabelYHandler, crsYHandler,
                            interpolationTypeHandler,
                            targetGeoXYResolutionsHandler,
                            targetGeoXYBoundingBoxHandler));
        
        return result;
    }
    
    @Override
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpression = ((WcpsResult)this.getFirstChild().handle());
        String axisLabelX = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        String crsX = ((WcpsResult)this.getThirdChild().handle()).getRasql();
        
        String axisLabelY = ((WcpsResult)this.getFourthChild().handle()).getRasql();
        String crsY = ((WcpsResult)this.getFifthChild().handle()).getRasql();
        
        String interpolationType = ((WcpsResult)this.getSixthChild().handle()).getRasql();
        
        
        CrsTransformTargetGeoXYResolutions targetGeoXYResolutions = null;
        if (this.getSeventhChild() != null) {
            WcpsMetadataResult wcpsMetadataResult = (WcpsMetadataResult) this.getSeventhChild().handle();
            if (wcpsMetadataResult.getTmpObject() instanceof CrsTransformTargetGeoXYResolutions) {
                targetGeoXYResolutions = (CrsTransformTargetGeoXYResolutions) wcpsMetadataResult.getTmpObject();
            }
        }
        
        
        CrsTransformTargetGeoXYBoundingBox targetGeoXYBBox = null;
        if (this.getEighthChild()!= null) {
            WcpsMetadataResult wcpsMetadataResult = (WcpsMetadataResult) this.getEighthChild().handle();
            if (wcpsMetadataResult.getTmpObject() instanceof CrsTransformTargetGeoXYBoundingBox) {
                targetGeoXYBBox = (CrsTransformTargetGeoXYBoundingBox) wcpsMetadataResult.getTmpObject();
            }
        }        
        
        
        WcpsResult result = this.handle(coverageExpression, axisLabelX, crsX, axisLabelY, crsY, interpolationType,
                                        targetGeoXYResolutions,
                                        targetGeoXYBBox);
        return result;
    }

    /**
     * Constructor for the class
     *
     * @param coverageExpression the coverage expression that is encoded
     * @param axisCrss List of 2 coverage's axes and their CRS (e.g:
     * http://opengis.net/def/crs/epsg/0/4326)
     * @param interpolationType resample algorithm (e.g: "near", "bilinear",...). 
     * If interpolation type is null, use default type = near.
     * @return
     */
    public WcpsResult handle(WcpsResult coverageExpression, 
                            String axisLabelX, String crsX,
                            String axisLabelY, String crsY, 
                            String interpolationType,
                            CrsTransformTargetGeoXYResolutions targetGeoXYResolutions,
                            CrsTransformTargetGeoXYBoundingBox targetGeoXYBBox
                            ) throws PetascopeException {
        
        checkOperandIsCoverage(coverageExpression, OPERATOR);
        
        int numberOfAxes = coverageExpression.getMetadata().getAxes().size();
        if (numberOfAxes != 2) {
            throw new Not2DCoverageForCrsTransformException(numberOfAxes);
        }
        
        String username = ConfigManager.RASDAMAN_ADMIN_USER;
        String password = ConfigManager.RASDAMAN_ADMIN_PASS;
        
        String geoResolutionAxisLabelX = null, geoResolutionX = null, geoResolutionAxisLabelY = null, geoResolutionY = null;

        
        if (targetGeoXYResolutions != null) {
        
            geoResolutionX = targetGeoXYResolutions.getGeoResolutionX();
            if (!BigDecimalUtil.isNumber(geoResolutionX)) {
                // e.g. 3 + 2.5
                geoResolutionX = RasUtil.executeQueryToReturnString("SELECT " + geoResolutionX, username, password);
            }

            geoResolutionY = targetGeoXYResolutions.getGeoResolutionY();
            if (!BigDecimalUtil.isNumber(geoResolutionY)) {
                // e.g. 3 + 2.5
                geoResolutionY = RasUtil.executeQueryToReturnString("SELECT " + geoResolutionY, username, password);
            }
            
            geoResolutionAxisLabelX = targetGeoXYResolutions.getGeoResolutionAxisLabelX();
            geoResolutionAxisLabelY = targetGeoXYResolutions.getGeoResolutionAxisLabelY();
            
            if (new BigDecimal(geoResolutionX).equals(BigDecimal.ZERO)) {
                throw new WCPSException(ExceptionCode.InvalidRequest, "Target resolution for axis " + geoResolutionAxisLabelX + " must be a non-zero number.");
            }
            if (new BigDecimal(geoResolutionY).equals(BigDecimal.ZERO)) {
                throw new WCPSException(ExceptionCode.InvalidRequest, "Target resolution for axis " + geoResolutionAxisLabelY + " must be a non-zero number.");
            }
        }
        
        
        
        Map<String, String> axisCrss = new LinkedHashMap<>();
        axisCrss.put(axisLabelX, crsX);
        axisCrss.put(axisLabelY, crsY);
        
        checkValid(axisCrss);

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        
        // Target geoXY resolutions, e.g. {Lat:0.5, Lon:0.3}
        Pair<String, String> inputTargetGeoXYResolutionPair = null;
        if (targetGeoXYResolutions != null) {
            if (metadata.getAxisByName(targetGeoXYResolutions.getGeoResolutionAxisLabelX()).isXAxis()) {
                // e.g. input from user is: {Lon:30.5, Lat:50.6}
                inputTargetGeoXYResolutionPair = new Pair<>(geoResolutionX, geoResolutionY);
            } else {
                // e.g. input from user is: {Lat:30.5, Lon:60.7}
                inputTargetGeoXYResolutionPair = new Pair<>(geoResolutionY, geoResolutionX);
            }
        }
        
        
        // Target geoXY bbox, e.g. {Lat(0:30), Lon(50:60)}
        BoundingBox inputTargetGeoXYBoundingBox = null;
        
        if (targetGeoXYBBox != null && !targetGeoXYBBox.getGeoXYSubsets().isEmpty()) {
            WcpsTrimSubsetDimension geoXYSubset1 = targetGeoXYBBox.getGeoXYSubsets().get(0);
            WcpsTrimSubsetDimension geoXYSubset2 = targetGeoXYBBox.getGeoXYSubsets().get(1);
            
            if (metadata.getAxisByName(geoXYSubset1.getAxisName()).isXAxis()) {
                // e.g. input bbox from user is: {Lon(30.5:60.5), Lat(50.6:60.6)}
                inputTargetGeoXYBoundingBox = new BoundingBox(new BigDecimal(geoXYSubset1.getLowerBound()), new BigDecimal(geoXYSubset2.getLowerBound()),
                                                        new BigDecimal(geoXYSubset1.getUpperBound()), new BigDecimal(geoXYSubset2.getUpperBound()));
            } else {
                // e.g. input bbox from user is: {Lat(50.6:60.6), Lon(30.5:60.5)}
                inputTargetGeoXYBoundingBox = new BoundingBox(new BigDecimal(geoXYSubset2.getLowerBound()), new BigDecimal(geoXYSubset1.getLowerBound()),
                                                        new BigDecimal(geoXYSubset2.getUpperBound()), new BigDecimal(geoXYSubset1.getUpperBound()));
            }
        }        
        
        String rasql = getRasqlExpression(coverageExpression, axisCrss, interpolationType, inputTargetGeoXYResolutionPair, inputTargetGeoXYBoundingBox);
        String outputCrs = axisCrss.values().toArray()[0].toString();

        //from this point onwards, the coverage has the new crs uri
        metadata.setCrsUri(outputCrs);
        
        if (!CrsUtil.isGridCrs(outputCrs) && !CrsUtil.isIndexCrs(outputCrs)) {
            // NOTE: after this crsTransform operator, the coverage's axes will need to updated with values from outputCRS also.
            // e.g: crsTransform(c, {Lat:"http://localhost:8080/def/crs/epsg/0/4326", Long:"http://localhost:8080/def/crs/epsg/0/4326"})
            // with c has X, Y axes (CRS:3857), then output of crsTransform is a 2D coverage with Lat, Long axes (CRS:4326).
            this.updateAxesByOutputCRS(metadata, inputTargetGeoXYResolutionPair, inputTargetGeoXYBoundingBox);
        }
        
        WcpsResult result = new WcpsResult(metadata, rasql);
        return result;
    }
    
    /**
     * Return new List of XY axes from projected GeoTransform (from nativeCRS to output CRS)
     */
    public static List<Axis> createGeoXYAxes(List<Axis> inputXYAxes, GeoTransform projectedGeoTransform) {
        
        // postive Axis (resolution > 0)
        Axis inputAxisX = inputXYAxes.get(0);
        
        BigDecimal geoLowerBoundX = projectedGeoTransform.getUpperLeftGeoX();
        BigDecimal geoUpperBoundX = projectedGeoTransform.getLowerRightGeoX();
        NumericSubset geoBoundsX = new NumericTrimming(geoLowerBoundX, geoUpperBoundX);
        
        BigDecimal gridLowerBoundX = BigDecimal.ZERO;
        BigDecimal gridUpperBoundX = new BigDecimal(projectedGeoTransform.getGridWidth() - 1);
        NumericSubset gridBoundsX = new NumericTrimming(gridLowerBoundX, gridUpperBoundX);
        
        BigDecimal geoResolutionX = projectedGeoTransform.getGeoXResolution();
        
        Axis axisX = new RegularAxis(inputAxisX.getLabel(), geoBoundsX, gridBoundsX, gridBoundsX,
                                    inputAxisX.getNativeCrsUri(), inputAxisX.getCrsDefinition(), inputAxisX.getAxisType(),
                                    inputAxisX.getAxisUoM(), inputAxisX.getRasdamanOrder(), geoLowerBoundX, geoResolutionX, geoBoundsX);
        
        // negative Axis (resolution < 0)
        Axis inputAxisY = inputXYAxes.get(1);
        
        BigDecimal geoUpperBoundY = projectedGeoTransform.getUpperLeftGeoY();
        BigDecimal geoLowerBoundY = projectedGeoTransform.getLowerRightGeoY();
        NumericSubset geoBoundsY = new NumericTrimming(geoLowerBoundY, geoUpperBoundY);
        
        BigDecimal gridLowerBoundY = BigDecimal.ZERO;
        BigDecimal gridUpperBoundY = new BigDecimal(projectedGeoTransform.getGridHeight() - 1);
        NumericSubset gridBoundsY = new NumericTrimming(gridLowerBoundY, gridUpperBoundY);
        
        BigDecimal geoResolutionY = projectedGeoTransform.getGeoYResolution();
        
        Axis axisY = new RegularAxis(inputAxisY.getLabel(), geoBoundsY, gridBoundsY, gridBoundsY, 
                                    inputAxisY.getNativeCrsUri(), inputAxisY.getCrsDefinition(), inputAxisY.getAxisType(),
                                    inputAxisY.getAxisUoM(), inputAxisY.getRasdamanOrder(), geoLowerBoundY, geoResolutionY, geoBoundsY);
        
        List<Axis> xyAxes = new ArrayList<>();
        xyAxes.add(axisX);
        xyAxes.add(axisY);
        
        return xyAxes;
    }
    
    /**
     * Create GDAL GeoTransform object based on XY geo axes
     */
    public static GeoTransform createGeoTransform(List<Axis> xyAxes) throws PetascopeException {
        Axis axisX = xyAxes.get(0);
        Axis axisY = xyAxes.get(1);

        GeoTransform geoTransform = new GeoTransform();
        
        String sourceCRS = axisX.getNativeCrsUri();
        String sourceCRSWKT = CrsUtil.getWKT(sourceCRS);
        
        geoTransform.setWKT(sourceCRSWKT);
        
        geoTransform.setGeoXResolution(axisX.getResolution().doubleValue());
        geoTransform.setGeoYResolution(axisY.getResolution().doubleValue());
        geoTransform.setUpperLeftGeoX(axisX.getGeoBounds().getLowerLimit());
        geoTransform.setUpperLeftGeoY(axisY.getGeoBounds().getUpperLimit());
        
        int width = axisX.getGridBounds().getUpperLimit().subtract(axisX.getGridBounds().getLowerLimit()).toBigInteger().intValue() + 1;
        int height = axisY.getGridBounds().getUpperLimit().subtract(axisY.getGridBounds().getLowerLimit()).toBigInteger().intValue() + 1;
        geoTransform.setGridWidth(width);
        geoTransform.setGridHeight(height);
        
        return geoTransform;
    }
    
    /**
     * Update the values of 2D geo, grid axes of current coverage to the corresponding values in OutputCRS.
     * e.g: coverage with 2 axes in EPSG:4326 Lat, Long order and outputCRS is EPSG:3857 X, Y order.
     */
    private void updateAxesByOutputCRS(WcpsCoverageMetadata covMetadata, 
                                    Pair<String, String> inputTargetGeoXYResolutionsPair,
                                    BoundingBox inputTargetGeoXYBoundingBox) throws PetascopeException {
        List<Axis> xyAxes = covMetadata.getXYAxes();        
        GeoTransform sourceGeoTransform = this.createGeoTransform(xyAxes);

        // Do the geo transform for this 2D geo, grid domains from source CRS to output CRS by GDAL
        String outputCRS = covMetadata.getCrsUri();
        GeoTransform targetGeoTransform = null;
        if (inputTargetGeoXYResolutionsPair == null && inputTargetGeoXYBoundingBox == null) {
            targetGeoTransform = CrsProjectionUtil.getGeoTransformInTargetCRS(sourceGeoTransform, outputCRS);
        } else {
            // if gdalwarp -tr -te provided or only one of them provided
            BigDecimal targetGeoResolutionX = null, targetGeoResolutionY = null;
            if (inputTargetGeoXYResolutionsPair != null) {
                targetGeoResolutionX = new BigDecimal(inputTargetGeoXYResolutionsPair.fst);
                targetGeoResolutionY = new BigDecimal(inputTargetGeoXYResolutionsPair.snd);
            }
            targetGeoTransform = CrsProjectionUtil.getGeoTransformInTargetCRS(sourceGeoTransform, outputCRS,
                                                    targetGeoResolutionX,
                                                    targetGeoResolutionY,
                                                    inputTargetGeoXYBoundingBox);
        }
        
        CrsDefinition crsDefinition = CrsUtil.getCrsDefinition(outputCRS);
        
        CrsDefinition.Axis firstCRSAxis, secondCRSAxis;
        
        if (CrsUtil.isXYAxesOrder(outputCRS)) {
            // e.g: X, Y EPSG:3857
            firstCRSAxis = crsDefinition.getAxes().get(0);
            secondCRSAxis = crsDefinition.getAxes().get(1);
        } else {
            // e.g: Lat, Long EPSG:4326
            firstCRSAxis = crsDefinition.getAxes().get(1);
            secondCRSAxis = crsDefinition.getAxes().get(0);
        }
        
        BigDecimal geoLowerBoundX = targetGeoTransform.getUpperLeftGeoX();
        BigDecimal geoUpperBoundX = targetGeoTransform.getLowerRightGeoX();
        
        NumericSubset geoBoundsX = new NumericTrimming(geoLowerBoundX, geoUpperBoundX);
        NumericSubset originalGridBoundX = new NumericTrimming(BigDecimal.ZERO, new BigDecimal(targetGeoTransform.getGridWidth() - 1));
        NumericSubset gridBoundX = new NumericTrimming(BigDecimal.ZERO, new BigDecimal(targetGeoTransform.getGridWidth() - 1));
        
        Axis axisX = new RegularAxis(firstCRSAxis.getAbbreviation(), geoBoundsX, originalGridBoundX, gridBoundX, 
                outputCRS, crsDefinition, 
                firstCRSAxis.getType(), firstCRSAxis.getUoM(), xyAxes.get(0).getRasdamanOrder(), 
                geoLowerBoundX, targetGeoTransform.getGeoXResolution(), geoBoundsX);
        
        BigDecimal geoUpperBoundY = targetGeoTransform.getUpperLeftGeoY();
        BigDecimal geoLowerBoundY = targetGeoTransform.getLowerRightGeoY();
        
        NumericSubset geoBoundsY = new NumericTrimming(geoLowerBoundY, geoUpperBoundY);
        NumericSubset originalGridBoundY = new NumericTrimming(BigDecimal.ZERO, new BigDecimal(targetGeoTransform.getGridHeight() - 1));
        NumericSubset gridBoundY = new NumericTrimming(BigDecimal.ZERO, new BigDecimal(targetGeoTransform.getGridHeight() - 1));

        Axis axisY = new RegularAxis(secondCRSAxis.getAbbreviation(), geoBoundsY, originalGridBoundY, gridBoundY, 
                outputCRS, crsDefinition, 
                secondCRSAxis.getType(), secondCRSAxis.getUoM(), xyAxes.get(1).getRasdamanOrder(), 
                geoLowerBoundY, targetGeoTransform.getGeoYResolution(), geoBoundsY);
        
        List<Axis> targetAxes;
        if (CrsUtil.isXYAxesOrder(outputCRS)) {
            targetAxes = new ArrayList<>(Arrays.asList(axisX, axisY));
        } else {
            targetAxes = new ArrayList<>(Arrays.asList(axisY, axisX));
        }
        covMetadata.setAxes(targetAxes);
    }

    /**
     * Check if crsTrasnformExpression is valid
     */
    private void checkValid(Map<String, String> axisCrss) {

        Set<String> keys = axisCrss.keySet();
        String[] axisNameArray = keys.toArray(new String[keys.size()]);
        String[] crsArray = Arrays.copyOf(axisCrss.values().toArray(), axisCrss.values().toArray().length, String[].class);

        // 1. axisName should be different (not as {Long:CRS_A, Long:CRS_B})
        if (axisCrss.size() != 2) {
            throw new IdenticalAxisNameInCrsTransformException(axisNameArray[0], axisNameArray[0]);
        }

        // 2. it should have same axis CRS (e.g: epsg:4326)
        String crsX = CrsUtil.CrsUri.getAuthorityCode(crsArray[0]);
        String crsY = CrsUtil.CrsUri.getAuthorityCode(crsArray[1]);
        if (!crsX.equals(crsY)) {
            throw new NotIdenticalCrsInCrsTransformException(crsX, crsY);
        }

        // 3. it can only subset 2D and input coverage with geo-referenced axis (native CRS)
        // i.e: don't support to project between a geo-referenced axis (e.g: Lat:"4326")
        // and time/pressure axis (e.g:t:"ansidate")
        String axisType1 = CrsDefinition.getAxisTypeByName(axisNameArray[0]);
        String axisType2 = CrsDefinition.getAxisTypeByName(axisNameArray[1]);
        
        if (!(axisType1.equals(AxisTypes.X_AXIS) || axisType1.equals(AxisTypes.Y_AXIS))) {
            throw new NotGeoReferenceAxisNameInCrsTransformException(axisNameArray[0]);
        } else if (!(axisType2.equals(AxisTypes.X_AXIS) || axisType2.equals(AxisTypes.Y_AXIS))) {
            throw new NotGeoReferenceAxisNameInCrsTransformException(axisNameArray[1]);
        }

        // 4. if outputCrs is GridCRS (Index%dD or CRS:1) is not valid geo-referenced CRS to transform
        String axisCrss1 = axisCrss.values().toArray()[0].toString();
        String axisCrss2 = axisCrss.values().toArray()[1].toString();

        if (axisCrss1.contains(CrsUtil.INDEX_CRS_PREFIX) || CrsUtil.isGridCrs(axisCrss1)) {
            throw new InvalidOutputCrsProjectionInCrsTransformException(axisCrss1, axisNameArray[0]);
        } else if (axisCrss2.contains(CrsUtil.INDEX_CRS_PREFIX) || CrsUtil.isGridCrs(axisCrss2)) {
            throw new InvalidOutputCrsProjectionInCrsTransformException(axisCrss2, axisNameArray[1]);
        }
    }
    
    /**
     * If EPSG:code just returns normally, if WKT then the wkt is quoted and breaklines removed
     */
    public static String getEscapedAuthorityEPSGCodeOrWKT(String crs) throws PetascopeException {
        String result = StringUtils.normalizeSpace(StringUtil.escapeQuotes(CrsUtil.getAuthorityEPSGCodeOrWKT(crs)));
        return result;
    }

    public String getRasqlExpression(WcpsResult coverageExpression, Map<String, String> axisCrss, String interpolationType, 
                                    Pair<String, String> geoXYResolutionPair,
                                    BoundingBox inputTargetGeoXYBoundingBox) throws PetascopeException {
        String outputStr = "";

        // Get the calculated coverage in grid axis with Rasql
        String covRasql = coverageExpression.getRasql();

        // Get bounding box of calculated coverage
        WcpsCoverageMetadata covMetadata = coverageExpression.getMetadata();
        List<Axis> xyAxes = covMetadata.getXYAxes();

        // NOTE: only trimming subset is used to set bounding box and axisList need to have 2 axes (X,Y)
        // It can support 3D netCdf, so need to handle this in EncodedCoverageHandler as well
        if (xyAxes.size() < 2) {
            throw new Not2DXYGeoreferencedAxesCrsTransformException(xyAxes.size());
        }
        String xMin = String.valueOf(((NumericTrimming) xyAxes.get(0).getGeoBounds()).getLowerLimit().toPlainString());
        String xMax = String.valueOf(((NumericTrimming) xyAxes.get(0).getGeoBounds()).getUpperLimit().toPlainString());
        String yMin = String.valueOf(((NumericTrimming) xyAxes.get(1).getGeoBounds()).getLowerLimit().toPlainString());
        String yMax = String.valueOf(((NumericTrimming) xyAxes.get(1).getGeoBounds()).getUpperLimit().toPlainString());
        
        // Handle bounding_box to project
        String sourceBBoxRepresentation = xMin + "," + yMin + "," + xMax + "," + yMax;

        // Handle source_crs, target_crs to project
        // (NOTE: sourceCrs can be compoundCrs, e.g: irr_cube_2) then need to get the crsUri from axis not from coverage metadata
        String axisName = axisCrss.keySet().iterator().next();
        Axis axis = covMetadata.getAxisByName(axisName);
        String sourceCRS = axis.getNativeCrsUri();
        String outputCRS = axisCrss.values().toArray()[0].toString();
        
        // NOTE: 
        // 1. If CRS is EPSG then return e.g. EPSG:4326 or the WKT of the CRS (rotated CRS COSMO 101) if possible which gdal can parse
        // 2. If the WKT is returned, then it needs to be enquoted e.g. "CRS[\"GeodeticCRS\": ...]"
        String sourceCRSParam = getEscapedAuthorityEPSGCodeOrWKT(sourceCRS);
        String targetCRSParam = getEscapedAuthorityEPSGCodeOrWKT(outputCRS);
        
        if (interpolationType == null) {
            interpolationType = DEFAULT_INTERPOLATION_TYPE;
        }
        
        if (geoXYResolutionPair == null) {        
            outputStr = TEMPLATE.replace("$COVERAGE_EXPRESSION", covRasql).replace("$BOUNDING_BOX", sourceBBoxRepresentation)
                                .replace("$SOURCE_CRS", sourceCRSParam).replace("$TARGET_CRS", targetCRSParam)
                                .replace("$INTERPOLATION_TYPE", interpolationType);
        } else {
            GeoTransform sourceGeoTransform = this.createGeoTransform(xyAxes);
            // In case there are target geo resolutions XY
            String targetGeoResolutionX = new BigDecimal(geoXYResolutionPair.fst).abs().toPlainString();
            String targetGeoResolutionY = new BigDecimal(geoXYResolutionPair.snd).abs().toPlainString();

            // Convert sourceBBox from sourceCRS to targetBBox in targetCRS            
            BoundingBox targetBBox = inputTargetGeoXYBoundingBox;
            if (inputTargetGeoXYBoundingBox == null) {
                targetBBox = CrsProjectionUtil.transform(sourceGeoTransform, targetCRSParam, 
                                                                new BigDecimal(geoXYResolutionPair.fst), new BigDecimal(geoXYResolutionPair.snd));
            }
            // Always in XY CRS order
            String targetBBoxRepresentation = StringUtil.stripQuotes(targetBBox.getRepresentation());
            
            outputStr = TEMPLATE_FULL
                                .replace("$COVERAGE_EXPRESSION", covRasql)
                                .replace("$SOURCE_BOUNDING_BOX", sourceBBoxRepresentation).replace("$SOURCE_CRS", sourceCRSParam)
                                .replace("$TARGET_BOUNDING_BOX", targetBBoxRepresentation).replace("$TARGET_CRS", targetCRSParam)
                                .replace("$INTERPOLATION_TYPE", interpolationType)
                                .replace("$TARGET_GEO_RESOLUTION_X", targetGeoResolutionX).replace("$TARGET_GEO_RESOLUTION_Y", targetGeoResolutionY);
        }

        return outputStr;
    }
    
    // Nearest neighbour (default, fastest algorithm, worst interpolation quality).
    private static final String DEFAULT_INTERPOLATION_TYPE = "near";

    // e.g Rasql query: select encode(project( c[0,-10:10,51:71], "20.0,40.0,30.0,50.0", "EPSG:4326", "EPSG:32633" ),
    // "GTiff", "xmin=20000.0;xmax=300000.0;ymin=400000.0;ymax=500000.0;crs=EPSG:32633") from eobstest AS c where oid(c)=1537
    private static final String TEMPLATE = "project( $COVERAGE_EXPRESSION, \"$BOUNDING_BOX\", \"$SOURCE_CRS\", \"$TARGET_CRS\", $INTERPOLATION_TYPE )";
    
    // e.g. rasql query: project(c, "20.0,40.0,30.0,50.0", "EPSG:4326", "111120.0,222240.0,333330.0,55550.0", "EPSG:32633", bilinear, 25.6, 456.7, 0.125
    private static final String TEMPLATE_FULL = "project( $COVERAGE_EXPRESSION, "
                                                + "\"$SOURCE_BOUNDING_BOX\", \"$SOURCE_CRS\", "
                                                + "\"$TARGET_BOUNDING_BOX\", \"$TARGET_CRS\", "
                                                + "$TARGET_GEO_RESOLUTION_X, $TARGET_GEO_RESOLUTION_Y, "
                                                + "$INTERPOLATION_TYPE, 0.125 )";
    
}
