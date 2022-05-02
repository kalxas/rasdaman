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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.cis.AxisExtent;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.domain.cis.Wgs84BoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.AxisTypes;
import petascope.core.BoundingBox;
import petascope.core.GeoTransform;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import static petascope.util.CrsUtil.COSMO_101_AUTHORITY_CODE;

/**
 * This class will provide utility method for projecting interval in
 * geo-referenced axis
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class CrsProjectionUtil {

    private static final Logger log = LoggerFactory.getLogger(CrsUtil.class);

    // caches SpatialReference objects, as computing them is relatively expensive
    // cf. https://gdal.org/java/org/gdal/osr/SpatialReference.html#ImportFromEPSG-int-
    private static Map<Integer, SpatialReference> srMap = new HashMap<>();
    
    // used by gdal vrt for gdalwarp
    private static final String DUMMY_PNG_FOR_GDAL_VRT_FILE_PATH = ConfigManager.DEFAULT_PETASCOPE_DIR_TMP + "/dummy_file_for_gdal.vrt.png";
    private static boolean DUMMY_PNG_FILE_FOR_GDAL_CREATED = false;
    
    // stored the projected GeoTransform to an output crs by specific geo XY resolutions
    private static Map<String, GeoTransform> projectedGeoTransformCacheMap = new HashMap<>();
    
    // 1 million pixels
    private static final int NUMBER_OF_GRID_PIXELS_FOR_VRT = 1000000;
    
    private static final String VRT_TEMPLATE = "<VRTDataset rasterXSize=\"RASTER_X_SIZE\" rasterYSize=\"RASTER_Y_SIZE\">\n" +
                                            "  <SRS>SRS_WKT</SRS>\n" +
                                            "  <GeoTransform> GEO_TRANSFORM </GeoTransform>\n" +
                                            "  <VRTRasterBand dataType=\"Byte\" band=\"1\">\n" +
                                            "    <ColorInterp>Red</ColorInterp>\n" +
                                            "    <SimpleSource>\n" +
                                            "      <SourceFilename relativeToVRT=\"1\">DUMMY_FILE_PATH</SourceFilename>\n" +
                                            "      <SourceProperties RasterXSize=\"RASTER_X_SIZE\" RasterYSize=\"RASTER_Y_SIZE\" DataType=\"Byte\" BlockXSize=\"1\" BlockYSize=\"1\" />\n" +
                                            "\n" +
                                            "    </SimpleSource>\n" +
                                            "  </VRTRasterBand>\n" +
                                            "</VRTDataset>";
    
    /**
     * Transform a XY values from sourceCRS to targetCRS (e.g: Long Lat (EPSG:4326) to ESPG:3857).
     * NOTE: Not every reprojection is possible, even if both sourceCR and targetCRS are EPSG CRS.
     * And only used for transforming **POINT** not **BOUND**
     *
     * @param sourceCRSWKT source CRS of axis in WKT format
     * @param targetCRSWKT target CRS to project in WKT format
     * @param sourceCoords Array of input coordinates: min values first,
     * max values next. E.g. [xMin,yMin,xMax,yMax].
     * @return List<BigDecimal> Locations transformed to targetCRS (defined at construction time).
     */
    public static List<BigDecimal> transformPoint(String sourceCRSWKT, String targetCRSWKT, double[] sourceCoords, String sourceCRSURL, String targetCRSURL) throws PetascopeException {
        
        // In case the input CRS is YX axes order with GDAL version >= 3
        double[] adjustedSourceCoords = adjustCoordinatesByGdalVersion(sourceCoords, sourceCRSURL);
        
        SpatialReference sourceSP = new SpatialReference();
        int sourceTmp = sourceSP.ImportFromWkt(sourceCRSWKT);
        
        String errorTemplate = "Cannot read WKT for GDAL SpatialReference object. Given '$WKT'. Hint: make sure GDAL version can understand the WKT.";
        
        if (sourceTmp != 0) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, errorTemplate.replace("$WKT", sourceCRSWKT));
        }
                
        SpatialReference targetSP = new SpatialReference();
        int targetTmp = targetSP.ImportFromWkt(targetCRSWKT);
        if (targetTmp != 0) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, errorTemplate.replace("$WKT", targetCRSWKT));
        }

        // Use gdal native library to transform the coordinates from source crs to target crs
        CoordinateTransformation coordTrans = CoordinateTransformation.CreateCoordinateTransformation(sourceSP, targetSP);
        if (coordTrans == null) {
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    "Failed creating coordinate transformation from source CRS '" + sourceCRSWKT + "' to target CRS '" + targetCRSWKT + "'.");
        }
        // This will returns 3 values, translated X, translated Y and another value which is not used
        double[] translatedCoords = coordTrans.TransformPoint(adjustedSourceCoords[0], adjustedSourceCoords[1]);
        
        // In case the output CRS is YX axes order with GDAL version >= 3
        double[] adjustedTranslatedCoords = adjustCoordinatesByGdalVersion(translatedCoords, targetCRSURL);

        List<BigDecimal> ret = new ArrayList<>(sourceCoords.length);
        // NOTE: projection can return NaN which means cannot reproject from sourceCRS to targetCRS
        // e.g: EPSG:3577 (75042.7273594 5094865.55794) to EPSG:4326
        for (int i = 0; i < adjustedTranslatedCoords.length; i++) {
            Double value = adjustedTranslatedCoords[i];
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new PetascopeException(ExceptionCode.InternalComponentError, 
                        "Failed reprojecting XY coordinates '" + Arrays.toString(sourceCoords) + 
                                "' from source CRS '" + sourceCRSWKT + "' to target CRS '" + targetCRSWKT + "'. Result is: " + value);
            }
            ret.add(new BigDecimal(value.toString()));
        }
        return ret;
    }
    
    /**
     * Transform a Gdal GeoTransform object to target CRS
     * in target CRS and get output's Bounding Box.
     */
    public static BoundingBox transform(GeoTransform sourceGT, String targetCRS) throws PetascopeException {
        GeoTransform targetGT = getGeoTransformInTargetCRS(sourceGT, targetCRS);
        
        if (Double.isNaN(targetGT.getGeoXResolution().doubleValue()) 
            || Double.isNaN(targetGT.getGeoYResolution().doubleValue())) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, 
                    "Cannot transform GeoTransform: " + sourceGT.toString() + " to target CRS '" + targetCRS + "'. Reason: axis X/Y resolution in target GeoTransform is NaN.");
        }
        
        BigDecimal xmin = new BigDecimal(String.valueOf(targetGT.getUpperLeftGeoX()));
        BigDecimal ymin = new BigDecimal(String.valueOf(targetGT.getLowerRightGeoY()));
        BigDecimal xmax = new BigDecimal(String.valueOf(targetGT.getLowerRightGeoX()));
        BigDecimal ymax = new BigDecimal(String.valueOf(targetGT.getUpperLeftGeoY()));
        
        BoundingBox bbox = new BoundingBox(xmin, ymin, xmax, ymax);
        return bbox;
    }
    
    /**
     * Transform a bbox from sourceCRS (e.g. http://localhost:8080/rasdaman/def/crs/EPSG/0/4326) to targetCRS (e.g. http://localhost:8080/rasdaman/def/crs/EPSG/0/32632)
     */
    public static BoundingBox transformBBox(BoundingBox sourceCRSBBox, String sourceCRS, String targetCRS) throws PetascopeException {
        
        String sourceCRSWKT = CrsUtil.getWKT(sourceCRS);
        String targetCRSWKT = CrsUtil.getWKT(targetCRS);
        
        if (CrsUtil.equalsWKT(sourceCRSWKT, targetCRSWKT)) {
            // Same CRS, no need to reproject
            return sourceCRSBBox;
        }
        
        boolean isPoint = false;
        
        if (sourceCRSBBox.getXMin().equals(sourceCRSBBox.getXMax()) || sourceCRSBBox.getYMin().equals(sourceCRSBBox.getYMax())) {
            isPoint = true;
        }
        
        BoundingBox result;
        
        if (!isPoint) {
            // transform bbox
            BigDecimal geoXResolution = BigDecimalUtil.divide(sourceCRSBBox.getXMax().subtract(sourceCRSBBox.getXMin()), new BigDecimal(NUMBER_OF_GRID_PIXELS_FOR_VRT));
            // negative resolution for lat axis
            BigDecimal geoYResolution = BigDecimalUtil.negative(BigDecimalUtil.divide(sourceCRSBBox.getYMax().subtract(sourceCRSBBox.getYMin()), new BigDecimal(NUMBER_OF_GRID_PIXELS_FOR_VRT)));

            GeoTransform sourceGeoTransform = new GeoTransform(sourceCRSWKT, sourceCRSBBox.getXMin(), sourceCRSBBox.getYMax(), 
                                                               NUMBER_OF_GRID_PIXELS_FOR_VRT, NUMBER_OF_GRID_PIXELS_FOR_VRT, geoXResolution, geoYResolution);
            GeoTransform targetGeoTransform = getGeoTransformInTargetCRS(sourceGeoTransform, targetCRS);
            result = targetGeoTransform.toBBox();
        } else {
            // transform point
            double[] xyMinArray = { sourceCRSBBox.getXMin().doubleValue(), sourceCRSBBox.getYMin().doubleValue() };
            double[] xyMaxArray = { sourceCRSBBox.getXMax().doubleValue(), sourceCRSBBox.getYMax().doubleValue() };
            
            List<BigDecimal> minLongLatList = null, maxLongLatList = null;
            
            minLongLatList = transformPoint(sourceCRSWKT, targetCRSWKT, xyMinArray, sourceCRS, targetCRS);
            maxLongLatList = transformPoint(sourceCRSWKT, targetCRSWKT, xyMaxArray, sourceCRS, targetCRS);
            
            BigDecimal lonMin = minLongLatList.get(0);
            BigDecimal latMin = minLongLatList.get(1);
            BigDecimal lonMax = maxLongLatList.get(0);
            BigDecimal latMax = maxLongLatList.get(1);
            
            result = new BoundingBox(lonMin, latMin, lonMax, latMax);
        }
        
        return result;
    }
    
    /**
     * Transform a Gdal GeoTransform object to target CRS with given geo X-Y axes' resolutions 
     * in target CRS and get output's Bounding Box.
    */
    public static BoundingBox transform(GeoTransform sourceGT, String targetCRS,
                                        BigDecimal targetCRSGeoXRes, BigDecimal targetCRSGeoYRes) throws PetascopeException {
        GeoTransform targetGT = getGeoTransformInTargetCRS(sourceGT, targetCRS, targetCRSGeoXRes, targetCRSGeoYRes);
        
        BigDecimal xmin = new BigDecimal(String.valueOf(targetGT.getUpperLeftGeoX()));
        BigDecimal ymin = new BigDecimal(String.valueOf(targetGT.getLowerRightGeoY()));
        BigDecimal xmax = new BigDecimal(String.valueOf(targetGT.getLowerRightGeoX()));
        BigDecimal ymax = new BigDecimal(String.valueOf(targetGT.getUpperLeftGeoY()));
        
        BoundingBox bbox = new BoundingBox(xmin, ymin, xmax, ymax);
        return bbox;
    }
    
    /**
     * Estimate the geo/grid and axes resolutions of 2D geo-referenced coverage from a source CRS (e.g: EPGS:4326) to a target CRS (e.g: EPSG:3857).
     * 
     * NOTE: CRS reprojection is not 1 - 1 relationship between the input coverage and estimated output coverage for grid domains because it depends on the 
     * CRS is used to reproject (e.g: source coverage with ESPG:4326 has grid domain [0:300, 0:200], reprojected coverage with EPSG:3857 has grid domain [0:310, 0:250],
     * reprojected coverage with EPSG:3521 has grid domain [0:230, 0:350]).
     * 
     * NOTE: The input is always XY axes order. (e.g: Long-Lat, not Lat-Long)
     * 
     * Therefore, GDAL library is used here to estimate the GeoTransform (a four double array containing the transformation coefficients):
     * + geo_min_x
     * + geo_min_y
     * of output reprojected coverage in target CRS. 
     * 
     * No file should be created and GDAL only needs to calculate this GeoTransform object quickly.
     */
    public static GeoTransform getGeoTransformInTargetCRS(GeoTransform sourceGT, String targetCRS) throws PetascopeException {
        String sourceCRSWKT = sourceGT.getWKT();
        String targetCRSWKT = CrsUtil.getWKT(targetCRS);
        
        if (CrsUtil.equalsWKT(sourceCRSWKT, targetCRSWKT)) {
            // Same CRS, no need to reproject
            return sourceGT;
        }
        
        // Source extents
        Dataset sourceDS = gdal.GetDriverByName("VRT").Create("", sourceGT.getGridWidth(), sourceGT.getGridHeight());
        // e.g: test_mean_summer_airtemp ([111.9750000, 0.05, 0, -8.9750000, 0, -0.05])
        double[] gdalGeoTransform = new double[] {
            BigDecimalUtil.toDouble(sourceGT.getUpperLeftGeoX()), BigDecimalUtil.toDouble(sourceGT.getGeoXResolution()), 0,
            BigDecimalUtil.toDouble(sourceGT.getUpperLeftGeoY()), 0, BigDecimalUtil.toDouble(sourceGT.getGeoYResolution())
        };
        sourceDS.SetGeoTransform(gdalGeoTransform);
        
        
        // error threshold for transformation approximation (in pixel units - defaults https://gdal.org/1.11/gdalwarp.html to 0.125)
        double errorThreshold = 0.125;
        int interpolation = gdalconstConstants.GRA_NearestNeighbour;
        // Expected target extents
        Dataset targetDS = gdal.AutoCreateWarpedVRT(sourceDS, sourceCRSWKT, targetCRSWKT, interpolation, errorThreshold);
        if (targetDS == null) {
            // NOTE: this happens when gdal cannot translate a 2D bounding box from source CRS (e.g: EPSG:4326) to a target CRS (e.g: EPSG:32652)
            throw new PetascopeException(ExceptionCode.RuntimeError, "Failed estimating the input geo domains "
                    + "from source CRS '" + sourceCRSWKT + "' to target CRS '" + targetCRSWKT + 
                    "'. Given source geoTransform values '" + sourceGT + "'");
        }
        
        GeoTransform targetGT = new GeoTransform();
        targetGT.setWKT(targetCRSWKT);
        
        // OutputCRS is also X-Y order, e.g: EPSG:3857
        double[] values = targetDS.GetGeoTransform();
        targetGT.setUpperLeftGeoX(values[0]);
        targetGT.setGeoXResolution(values[1]);
        targetGT.setUpperLeftGeoY(values[3]);
        targetGT.setGeoYResolution(values[5]);

        targetGT.setGridWidth(targetDS.GetRasterXSize());
        targetGT.setGridHeight(targetDS.GetRasterYSize());

        return targetGT;
    }
    
    /**
     * This file (1 x 1 pixel) is used to allow gdal binding to read VRT file without error
     */
    private static void createDummyImageForGdalVRT() throws PetascopeException {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        File tempFile = new File(DUMMY_PNG_FOR_GDAL_VRT_FILE_PATH);
        try {
            ImageIO.write(img, "PNG", tempFile);
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError,
                                        "Cannot create a dummy file for gdal VRT at '" + tempFile.getAbsolutePath() + "'. Reason: " + ex.getMessage(), ex);
        }
        
    }
    
    /**
     * From a GeoTransform object in source EPSG code (e.g: EPSG:32632), project it to a target EPSG code (e.g: EPSG:4326)
     * NOTE: with the given geo XY axes's resolutions in target CRS.
     * 
     * It works like with: gdalwarp a.tif -t_srs EPSG:4326 -tr 0.2 0.2 -tap warped.tif
     */
    public static GeoTransform getGeoTransformInTargetCRS(GeoTransform sourceGeoTransform, String targetCRS, 
                                                          BigDecimal targetCRSGeoXResolution, BigDecimal targetCRSGeoYResolution) throws PetascopeException {
        
        String keyMap = StringUtil.join(targetCRS, sourceGeoTransform.toString(), targetCRS, targetCRSGeoXResolution.toPlainString(), targetCRSGeoYResolution.toPlainString());
        GeoTransform result = projectedGeoTransformCacheMap.get(keyMap);
        if (result != null) {
            return result;
        }
        
        if (DUMMY_PNG_FILE_FOR_GDAL_CREATED == false) {
            createDummyImageForGdalVRT();
            DUMMY_PNG_FILE_FOR_GDAL_CREATED = true;
        }
        
        String sourceCRSWKT = sourceGeoTransform.getWKT();
        String targetCRSWKT = CrsUtil.getWKT(targetCRS);

        String vrtXML = VRT_TEMPLATE.replace("RASTER_X_SIZE", String.valueOf(sourceGeoTransform.getGridWidth()))
                                    .replace("RASTER_Y_SIZE", String.valueOf(sourceGeoTransform.getGridHeight()))
                                    .replace("SRS_WKT", sourceCRSWKT)
                                    .replace("GEO_TRANSFORM", sourceGeoTransform.toGdalString())
                                    .replace("DUMMY_FILE_PATH", DUMMY_PNG_FOR_GDAL_VRT_FILE_PATH);
        
        File tempVRTFile = new File(ConfigManager.DEFAULT_PETASCOPE_DIR_TMP + "/" + StringUtil.addDateTimeSuffix("vrt"));
        File tempWarpedVRTFile = new File(ConfigManager.DEFAULT_PETASCOPE_DIR_TMP + "/" + StringUtil.addDateTimeSuffix("vrt_warped"));
        
        File targetCRSWKTFile = new File(ConfigManager.DEFAULT_PETASCOPE_DIR_TMP + "/" + StringUtil.addDateTimeSuffix("target_crs_wkt"));
        
        try {
            FileUtils.writeStringToFile(tempVRTFile, vrtXML);
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.IOConnectionError, 
                                        "Cannot write temp VRT file at '" + tempVRTFile.getAbsolutePath() + "'. Reason: " + ex.getMessage(), ex);
        }
        
        try {
            FileUtils.writeStringToFile(targetCRSWKTFile, targetCRSWKT);
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.IOConnectionError, 
                                        "Cannot write target CRS WKT file at '" + tempVRTFile.getAbsolutePath() + "'. Reason: " + ex.getMessage(), ex);
        }
        
        // Run a quick command to warp VRT file to target CRS with given geo XY axes' resolutions
        String bashCommand = "gdalwarp -overwrite -t_srs TARGET_CRS_WKT_FILE -tr RES_X RES_Y -tap VRT_FILE -of VRT VRT_WARPED_FILE"
                            .replace("TARGET_CRS_WKT_FILE", targetCRSWKTFile.getAbsolutePath())
                            .replace("RES_X", targetCRSGeoXResolution.abs().toPlainString())
                            .replace("RES_Y", targetCRSGeoYResolution.abs().toPlainString())
                            .replace("VRT_FILE", tempVRTFile.getAbsolutePath())
                            .replace("VRT_WARPED_FILE", tempWarpedVRTFile.getAbsolutePath());
        
        result = getGeoTransformViaGdalBash(bashCommand, tempVRTFile, tempWarpedVRTFile);        
        
        projectedGeoTransformCacheMap.put(keyMap, result);
        
        return result;
    }
    
    
    /**
     * Project a VRT file in sourceCRS to a targetCRS, then create GeoTransform object from the projected VRT file
     */
    private static GeoTransform getGeoTransformViaGdalBash(String bashCommand, File tempVRTFile, File tempWarpedVRTFile) throws PetascopeException {
        GeoTransform result = null;
        // If gdalwarp cannot return valid VRT output, then retry 3 more times
        int MAX_RETRIES = 3;
        int i = 1;
        
        while (i <= MAX_RETRIES) {
        
            String stdout = "", stderr = "";
            try {
                Process process = Runtime.getRuntime().exec(bashCommand);
                process.waitFor();
                stderr = IOUtils.toString(process.getErrorStream());
                stdout = IOUtils.toString(process.getInputStream());

            } catch (Exception ex) {
                throw new PetascopeException(ExceptionCode.RuntimeError, "Cannot execute gdalwarp command: " + bashCommand + ". Reason: " + ex.getMessage(), ex);
            }

            // Then, read the warped VRT file to collect the calculated result
            String tmpVRTFile = tempWarpedVRTFile.getAbsolutePath();
            Dataset dataset = gdal.Open(tmpVRTFile);
            if (dataset == null) {
                // retry one more time as the output VRT has error for some reason
                i++;
                
                if (i > MAX_RETRIES) {
                    throw new PetascopeException(ExceptionCode.RuntimeError, 
                                                "Cannot project VRT file with gdal bash command: '" + bashCommand + "'. Stddout: "  + stdout + ". Stderr: "  + stderr);
                }
                log.warn("Failed projecting VRT file with gdal bash command: '" + bashCommand + "'. Stddout: "  + stdout + ". Stderr: "  + stderr + ". Retrying " + i + "/" + MAX_RETRIES);
            } else {
                result = new GeoTransform(dataset);
                break;
            } 
        }
        
        // Finally, remove temp files
        FileUtils.deleteQuietly(tempVRTFile);
        FileUtils.deleteQuietly(tempWarpedVRTFile);
        
        return result;
    }
    
    /**
     * Create a Wgs84BoundingBox, but the projected geo domains in EPSG:4326 are less precisely,
     * because it uses the pairs of coordinates to project from coverage's Envelope (domainset is not known)
     * 
     * NOTE: this should not be used in most cases if grid extents and axis resolutions are known
     */
    public static Wgs84BoundingBox createLessPreciseWgs84BBox(EnvelopeByAxis envelopeByAxis) throws PetascopeException {

        List<AxisExtent> axisExtents = envelopeByAxis.getAxisExtents();
        boolean foundX = false, foundY = false;
        String xyAxesCRSURL = null;
        String coverageCRS = envelopeByAxis.getSrsName();
        BigDecimal xMin = null, yMin = null, xMax = null, yMax = null;
        
        Wgs84BoundingBox wgs84BoundingBox = null;
        int i = 0;
        for (AxisExtent axisExtent : axisExtents) {
            String axisExtentCrs = axisExtent.getSrsName();
            // NOTE: the basic coverage metadata can have the abstract SECORE URL, so must replace it first
            axisExtentCrs = CrsUtil.CrsUri.fromDbRepresentation(axisExtentCrs);
            
            if (axisExtentCrs.contains(CrsUtil.EPSG_AUTH)) {
                // x, y
                String axisType = CrsUtil.getAxisTypeByIndex(coverageCRS, i);
                if (axisType.equals(AxisTypes.X_AXIS)) {
                    foundX = true;
                    xMin = new BigDecimal(axisExtent.getLowerBound());
                    xMax = new BigDecimal(axisExtent.getUpperBound());
                    xyAxesCRSURL = axisExtentCrs;
                } else if (axisType.equals(AxisTypes.Y_AXIS)) {
                    foundY = true;
                    yMin = new BigDecimal(axisExtent.getLowerBound());
                    yMax = new BigDecimal(axisExtent.getUpperBound());
                }
                if (foundX && foundY) {
                    break;
                }
            }
            
            i++;
        }
        
        // Don't transform the XY extents to EPSG:4326 if it is not EPSG code or it is not at least 2D
        if (foundX && foundY && CrsProjectionUtil.isValidTransform(xyAxesCRSURL)) {
            if (CrsUtil.getAuthorityCode(xyAxesCRSURL).equals(CrsUtil.EPSG_4326_AUTHORITY_CODE)) {
                // already in EPSG:4326
                wgs84BoundingBox = new Wgs84BoundingBox(xMin, yMin, xMax, yMax);
            } else {
                // not in EPSG:4326, needs to project
                wgs84BoundingBox = CrsProjectionUtil.createLessPreciseWgs84BBox(xMin, yMin, xMax, yMax, xyAxesCRSURL);
            }
        }
        
        return wgs84BoundingBox;
    }
    
    /**
     * Create a Wgs84BoundingBox, but the projected geo domains in EPSG:4326 are less precisely,
     * because it uses the pairs of coordinates to project.
     * 
     * NOTE: this should not be used in most cases if grid extents and axis resolutions are known
     */
    public static Wgs84BoundingBox createLessPreciseWgs84BBox(BigDecimal xMin, BigDecimal yMin, BigDecimal xMax, BigDecimal yMax, String xyAxesCRSURL) 
            throws PetascopeException {
        // NOTE: this one is used only for older peernode petascope which doesn't have Wgs84BoundingBox object in envelope
        // It returns less precisely bounding box, but it is used for backward compatibility
        double[] xyMinArray = { xMin.doubleValue(), yMin.doubleValue() };
        double[] xyMaxArray = { xMax.doubleValue(), yMax.doubleValue() };
        List<BigDecimal> minLongLatList = null, maxLongLatList = null;
        String sourceCRSWKT = CrsUtil.getWKT(xyAxesCRSURL);
        String targetCRSWKT = CrsUtil.getWKT(CrsUtil.getEPSG4326FullURL());
        
        minLongLatList = CrsProjectionUtil.transformPoint(sourceCRSWKT, targetCRSWKT, xyMinArray, xyAxesCRSURL, CrsUtil.getEPSG4326FullURL());
        maxLongLatList = CrsProjectionUtil.transformPoint(sourceCRSWKT, targetCRSWKT, xyMaxArray, xyAxesCRSURL, CrsUtil.getEPSG4326FullURL());

        BigDecimal lonMin = minLongLatList.get(0);
        BigDecimal latMin = minLongLatList.get(1);
        BigDecimal lonMax = maxLongLatList.get(0);
        BigDecimal latMax = maxLongLatList.get(1);

        if (lonMin.compareTo(new BigDecimal("-180")) < 0) {
            lonMin = new BigDecimal("-180");
        }
        if (latMin.compareTo(new BigDecimal("-90")) < 0) {
            latMin = new BigDecimal("-90");
        }                
        if (lonMax.compareTo(new BigDecimal("180")) > 0) {
            lonMax = new BigDecimal("180");
        }
        if (latMax.compareTo(new BigDecimal("90")) > 0) {
            latMax = new BigDecimal("90");
        }

        Wgs84BoundingBox wgs84BoundingBox = new Wgs84BoundingBox(lonMin, latMin, lonMax, latMax);
        return wgs84BoundingBox;
    }

    /**
     * Current only support transformation between EPSG crss and COSMO:101 rotated CRS
     * @param crs = AuthorityCode: EPSG:4326 or fullCRSURL: http://localhost:8080/def/crs/EPSG/0/4326
     */
    public static boolean isValidTransform(String crs) throws PetascopeException {
        crs = crs.trim();
        
        // e.g EPSG:4326
        String authorityCRSCode = crs;
        
        if (CrsUtil.isIndexCrs(crs) || CrsUtil.isGridCrs(crs)) {
            return false;
        }
        
        if (!CrsUtil.isAuthorityCode(crs)) {
            // crs is full URL, e.g. http://localhost:8080/def/crs/EPSG/0/4326
            // return EPSG:4326
            authorityCRSCode = CrsUtil.getAuthorityCode(crs);
        }
        return (authorityCRSCode.startsWith(CrsUtil.EPSG_AUTH) 
                || authorityCRSCode.equals(COSMO_101_AUTHORITY_CODE));
    }
    
    /**
     * NOTE: from GDAL 3, it regards the axes orders in the EPSG CRS definition (e.g: EPSG:4326 with YX axes order).
     * Previous versions are always with XY axes orders for any EPSG CRSs.
     */
    private static double[] adjustCoordinatesByGdalVersion(double[] coordinates, String crs) throws PetascopeException {        
        if (ConfigManager.GDAL_JAVA_VERSION < 3 || CrsUtil.isXYAxesOrder(crs)) {          
            return coordinates;
        } else {
           // gdal version 3 and input coordinates (YX axes order), then need to flip the coordinates
           double[] adjustedCoordinates = new double[] {coordinates[1], coordinates[0]};
           return adjustedCoordinates;
        }
    }
}
