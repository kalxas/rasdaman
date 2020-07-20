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
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.rasdaman.domain.cis.Coverage;
import org.springframework.stereotype.Service;
import petascope.core.AxisTypes;
import petascope.core.CrsDefinition;
import petascope.core.GeoTransform;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsProjectionUtil;
import petascope.wcps.exception.processing.IdenticalAxisNameInCrsTransformException;
import petascope.wcps.exception.processing.InvalidOutputCrsProjectionInCrsTransformException;
import petascope.wcps.exception.processing.Not2DCoverageForCrsTransformException;
import petascope.wcps.exception.processing.Not2DXYGeoreferencedAxesCrsTransformException;
import petascope.wcps.exception.processing.NotGeoReferenceAxisNameInCrsTransformException;
import petascope.wcps.exception.processing.NotIdenticalCrsInCrsTransformException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.RegularAxis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.WcpsResult;

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
public class CrsTransformHandler extends AbstractOperatorHandler {
    
    public static final String OPERATOR = "crsTransform";

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
    public WcpsResult handle(WcpsResult coverageExpression, HashMap<String, String> axisCrss, 
                             String interpolationType) throws PetascopeException, SecoreException {
        
        checkOperandIsCoverage(coverageExpression, OPERATOR);
        int numberOfAxes = coverageExpression.getMetadata().getAxes().size();
        if (numberOfAxes != 2) {
            throw new Not2DCoverageForCrsTransformException(numberOfAxes);
        }
        
        checkValid(axisCrss);
        String rasql = getRasqlExpression(coverageExpression, axisCrss, interpolationType);
        String outputCrs = axisCrss.values().toArray()[0].toString();

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        //from this point onwards, the coverage has the new crs uri
        metadata.setCrsUri(outputCrs);
        
        if (!CrsUtil.isGridCrs(outputCrs) && !CrsUtil.isIndexCrs(outputCrs)) {
            // NOTE: after this crsTransform operator, the coverage's axes will need to updated with values from outputCRS also.
            // e.g: crsTransform(c, {Lat:"http://localhost:8080/def/crs/epsg/0/4326", Long:"http://localhost:8080/def/crs/epsg/0/4326"})
            // with c has X, Y axes (CRS:3857), then output of crsTransform is a 2D coverage with Lat, Long axes (CRS:4326).
            this.updateAxesByOutputCRS(metadata);
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
        
        BigDecimal geoLowerBoundX = new BigDecimal(projectedGeoTransform.getUpperLeftGeoX());
        BigDecimal geoUpperBoundX = new BigDecimal(projectedGeoTransform.getUpperLeftGeoX() 
                                                     + projectedGeoTransform.getGeoXResolution() * projectedGeoTransform.getGridWidth());
        NumericSubset geoBoundsX = new NumericTrimming(geoLowerBoundX, geoUpperBoundX);
        
        BigDecimal gridLowerBoundX = BigDecimal.ZERO;
        BigDecimal gridUpperBoundX = new BigDecimal(projectedGeoTransform.getGridWidth() - 1);
        NumericSubset gridBoundsX = new NumericTrimming(gridLowerBoundX, gridUpperBoundX);
        
        BigDecimal geoResolutionX = new BigDecimal(projectedGeoTransform.getGeoXResolution());
        
        Axis axisX = new RegularAxis(inputAxisX.getLabel(), geoBoundsX, gridBoundsX, gridBoundsX,
                                    inputAxisX.getNativeCrsUri(), inputAxisX.getCrsDefinition(), inputAxisX.getAxisType(),
                                    inputAxisX.getAxisUoM(), inputAxisX.getRasdamanOrder(), geoLowerBoundX, geoResolutionX, geoBoundsX);
        
        // negative Axis (resolution < 0)
        Axis inputAxisY = inputXYAxes.get(1);
        
        BigDecimal geoUpperBoundY = new BigDecimal(projectedGeoTransform.getUpperLeftGeoY());
        BigDecimal geoLowerBoundY = new BigDecimal(projectedGeoTransform.getUpperLeftGeoY() 
                                                     - Math.abs(projectedGeoTransform.getGeoYResolution()) * projectedGeoTransform.getGridHeight());
        NumericSubset geoBoundsY = new NumericTrimming(geoLowerBoundY, geoUpperBoundY);
        
        BigDecimal gridLowerBoundY = BigDecimal.ZERO;
        BigDecimal gridUpperBoundY = new BigDecimal(projectedGeoTransform.getGridHeight() - 1);
        NumericSubset gridBoundsY = new NumericTrimming(gridLowerBoundY, gridUpperBoundY);
        
        BigDecimal geoResolutionY = new BigDecimal(projectedGeoTransform.getGeoYResolution());
        
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
        
        int sourceEPSGCode = new Integer(CrsUtil.getCode(axisX.getNativeCrsUri()));
        geoTransform.setEPSGCode(sourceEPSGCode);
        
        geoTransform.setGeoXResolution(axisX.getResolution().doubleValue());
        geoTransform.setGeoYResolution(axisY.getResolution().doubleValue());
        geoTransform.setUpperLeftGeoX(new Double(axisX.getLowerGeoBoundRepresentation()));
        geoTransform.setUpperLeftGeoY(new Double(axisY.getUpperGeoBoundRepresentation()));
        
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
    private void updateAxesByOutputCRS(WcpsCoverageMetadata covMetadata) throws PetascopeException, SecoreException {
        List<Axis> xyAxes = covMetadata.getXYAxes();        
        GeoTransform sourceGeoTransform = this.createGeoTransform(xyAxes);

        // Do the geo transform for this 2D geo, grid domains from source CRS to output CRS by GDAL
        String outputCRS = covMetadata.getCrsUri();
        GeoTransform targetGeoTransform = CrsProjectionUtil.getGeoTransformInTargetCRS(sourceGeoTransform, outputCRS);
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
        
        BigDecimal geoLowerBoundX = new BigDecimal(String.valueOf(targetGeoTransform.getUpperLeftGeoX()));
        BigDecimal geoUpperBoundX = new BigDecimal(String.valueOf(targetGeoTransform.getUpperLeftGeoX() 
                                                   + targetGeoTransform.getGeoXResolution() * targetGeoTransform.getGridWidth()));
        
        NumericSubset geoBoundsX = new NumericTrimming(geoLowerBoundX, geoUpperBoundX);
        NumericSubset originalGridBoundX = new NumericTrimming(BigDecimal.ZERO, new BigDecimal(targetGeoTransform.getGridWidth() - 1));
        NumericSubset gridBoundX = new NumericTrimming(BigDecimal.ZERO, new BigDecimal(targetGeoTransform.getGridWidth() - 1));
        
        Axis axisX = new RegularAxis(firstCRSAxis.getAbbreviation(), geoBoundsX, originalGridBoundX, gridBoundX, 
                outputCRS, crsDefinition, 
                firstCRSAxis.getType(), firstCRSAxis.getUoM(), xyAxes.get(0).getRasdamanOrder(), 
                geoLowerBoundX, new BigDecimal(String.valueOf(targetGeoTransform.getGeoXResolution())), geoBoundsX);
        
        BigDecimal geoUpperBoundY = new BigDecimal(String.valueOf(targetGeoTransform.getUpperLeftGeoY()));
        BigDecimal geoLowerBoundY = new BigDecimal(String.valueOf(targetGeoTransform.getUpperLeftGeoY() 
                                                   + targetGeoTransform.getGeoYResolution() * targetGeoTransform.getGridHeight()));
        
        NumericSubset geoBoundsY = new NumericTrimming(geoLowerBoundY, geoUpperBoundY);
        NumericSubset originalGridBoundY = new NumericTrimming(BigDecimal.ZERO, new BigDecimal(targetGeoTransform.getGridHeight() - 1));
        NumericSubset gridBoundY = new NumericTrimming(BigDecimal.ZERO, new BigDecimal(targetGeoTransform.getGridHeight() - 1));

        Axis axisY = new RegularAxis(secondCRSAxis.getAbbreviation(), geoBoundsY, originalGridBoundY, gridBoundY, 
                outputCRS, crsDefinition, 
                secondCRSAxis.getType(), secondCRSAxis.getUoM(), xyAxes.get(1).getRasdamanOrder(), 
                geoLowerBoundY, new BigDecimal(String.valueOf(targetGeoTransform.getGeoYResolution())), geoBoundsY);
        
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
    private void checkValid(HashMap<String, String> axisCrss) {

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

    public String getRasqlExpression(WcpsResult coverageExpression, HashMap<String, String> axisCrss, String interpolationType) {
        String outputStr = "";

        // Get the calculated coverage in grid axis with Rasql
        String covRasql = coverageExpression.getRasql();

        // Get bounding box of calculated coverage
        WcpsCoverageMetadata covMetadata = coverageExpression.getMetadata();
        List<Axis> axisList = covMetadata.getXYAxes();

        // NOTE: only trimming subset is used to set bounding box and axisList need to have 2 axes (X,Y)
        // It can support 3D netCdf, so need to handle this in EncodedCoverageHandler as well
        if (axisList.size() < 2) {
            throw new Not2DXYGeoreferencedAxesCrsTransformException(axisList.size());
        }
        String xMin = String.valueOf(((NumericTrimming) axisList.get(0).getGeoBounds()).getLowerLimit().toPlainString());
        String xMax = String.valueOf(((NumericTrimming) axisList.get(0).getGeoBounds()).getUpperLimit().toPlainString());
        String yMin = String.valueOf(((NumericTrimming) axisList.get(1).getGeoBounds()).getLowerLimit().toPlainString());
        String yMax = String.valueOf(((NumericTrimming) axisList.get(1).getGeoBounds()).getUpperLimit().toPlainString());

        // Handle bounding_box to project
        String bbox = xMin + "," + yMin + "," + xMax + "," + yMax;

        // Handle source_crs, target_crs to project
        // (NOTE: sourceCrs can be compoundCrs, e.g: irr_cube_2) then need to get the crsUri from axis not from coverage metadata
        String axisName = axisCrss.keySet().iterator().next();
        Axis axis = covMetadata.getAxisByName(axisName);
        String covCRS = axis.getNativeCrsUri();
        String outputCrs = axisCrss.values().toArray()[0].toString();
        String sourceCRS = CrsUtil.CrsUri.getAuthorityCode(covCRS);
        String targetCRS = CrsUtil.CrsUri.getAuthorityCode(outputCrs);
        
        if (interpolationType == null) {
            interpolationType = DEFAULT_INTERPOLATION_TYPE;
        }
        
        outputStr = TEMPLATE.replace("$COVERAGE_EXPRESSION", covRasql).replace("$BOUNDING_BOX", bbox)
                            .replace("$SOURCE_CRS", sourceCRS).replace("$TARGET_CRS", targetCRS)
                            .replace("$INTERPOLATION_TYPE", interpolationType);

        return outputStr;
    }
    
    // Nearest neighbour (default, fastest algorithm, worst interpolation quality).
    private static final String DEFAULT_INTERPOLATION_TYPE = "near";

    // e.g Rasql query: select encode(project( c[0,-10:10,51:71], "20.0,40.0,30.0,50.0", "EPSG:4326", "EPSG:32633" ),
    // "GTiff", "xmin=20000.0;xmax=300000.0;ymin=400000.0;ymax=500000.0;crs=EPSG:32633") from eobstest AS c where oid(c)=1537
    private static final String TEMPLATE = "project( $COVERAGE_EXPRESSION, \"$BOUNDING_BOX\", \"$SOURCE_CRS\", \"$TARGET_CRS\", $INTERPOLATION_TYPE )";
}
