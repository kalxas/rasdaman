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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.WCSException;
import petascope.core.GeoTransform;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;

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

    /**
     * Transform a XY values from sourceCRS to targetCRS (e.g: Long Lat (EPSG:4326) to ESPG:3857).
     * NOTE: Not every reprojection is possible, even if both sourceCR and targetCRS are EPSG CRS.
     *
     * @param sourceCrs source CRS of axis
     * @param targetCrs target CRS to project
     * @param sourceCoords Array of input coordinates: min values first,
     * max values next. E.g. [xMin,yMin,xMax,yMax].
     * @return List<BigDecimal> Locations transformed to targetCRS (defined at construction time).
     */
    public static List<BigDecimal> transform(String sourceCrs, String targetCrs, double[] sourceCoords) throws WCSException, PetascopeException {
        // e.g: 4326, 32633
        int sourceCode = CrsUtil.getEpsgCodeAsInt(sourceCrs);
        int targetCode = CrsUtil.getEpsgCodeAsInt(targetCrs);

        // Use gdal native library to transform the coordinates from source crs to target crs
        CoordinateTransformation coordTrans = CoordinateTransformation.CreateCoordinateTransformation(
                getSpatialReference(sourceCode), getSpatialReference(targetCode));
        if (coordTrans == null) {
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    "Failed creating coordinate transformation from " + sourceCrs + " to " + targetCrs);
        }
        // This will returns 3 values, translated X, translated Y and another value which is not used
        double[] transCoords = coordTrans.TransformPoint(sourceCoords[0], sourceCoords[1]);

        List<BigDecimal> ret = new ArrayList<>(sourceCoords.length);
        // NOTE: projection can return NaN which means cannot reproject from sourceCRS to targetCRS
        // e.g: EPSG:3577 (75042.7273594 5094865.55794) to EPSG:4326
        for (int i = 0; i < transCoords.length - 1; i++) {
            Double value = transCoords[i];
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new PetascopeException(ExceptionCode.InternalComponentError, 
                        "Failed reprojecting XY coordinates '" + Arrays.toString(sourceCoords) + 
                                "' from sourceCrs '" + sourceCrs + "' to targetCRS '" + targetCrs + ", result is " + value);
            }
            ret.add(new BigDecimal(value));
        }
        return ret;
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
    public static GeoTransform getGeoTransformInTargetCRS(GeoTransform sourceGT, String targetCRS) throws PetascopeException, SecoreException {
        
        // Source extents
        Dataset sourceDS = gdal.GetDriverByName("VRT").Create("", sourceGT.getGridWidth(), sourceGT.getGridHeight());
        // e.g: test_mean_summer_airtemp ([111.9750000, 0.05, 0, -8.9750000, 0, -0.05])
        double[] gdalGeoTransform = new double[] {
            sourceGT.getUpperLeftGeoX(), sourceGT.getGeoXResolution(), 0,
            sourceGT.getUpperLeftGeoY(), 0, sourceGT.getGeoYResolution()};
        sourceDS.SetGeoTransform(gdalGeoTransform);
        
        String sourceCRSWKT = getSpatialReference(sourceGT.getEPSGCode()).ExportToWkt();
        int targetEPSGCode = CrsUtil.getEpsgCodeAsInt(targetCRS);
        String targetCRSWKT = getSpatialReference(targetEPSGCode).ExportToWkt();
        
        // error threshold for transformation approximation (in pixel units - defaults https://gdal.org/1.11/gdalwarp.html to 0.125)
        double errorThreshold = 0.125;
        int interpolation = gdalconstConstants.GRA_NearestNeighbour;
        // Expected target extents
        Dataset targetDS = gdal.AutoCreateWarpedVRT(sourceDS, sourceCRSWKT, targetCRSWKT, interpolation, errorThreshold);
        if (targetDS == null) {
            // NOTE: this happens when gdal cannot translate a 2D bounding box from source CRS (e.g: EPSG:4326) to a target CRS (e.g: EPSG:32652)
            throw new PetascopeException(ExceptionCode.RuntimeError, "Failed estimating the input geo domains "
                    + "from source CRS 'EPSG:" + sourceGT.getEPSGCode() + "' to target CRS 'EPSG:" + targetEPSGCode + 
                    "', given source geoTransform values '" + sourceGT + "'.");
        }
        
        GeoTransform targetGT = new GeoTransform();
        targetGT.setEPSGCode(targetEPSGCode);
        
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
     * Manage caching of SpatialReference objects.
     */
    public static SpatialReference getSpatialReference(int code) throws PetascopeException {
        SpatialReference ret = srMap.get(code);
        if (ret == null) {
            ret = new SpatialReference();
            ret.ImportFromEPSG(code);
            srMap.put(code, ret);
        }
        return ret;
    }

    /**
     * Current only support transformation between EPSG crss
     *
     * @param crsCode (e.g: EPSG:4326)
     */
    public static boolean validTransformation(String crsCode) {
        if (!crsCode.startsWith(CrsUtil.EPSG_AUTH)) {
            return false;
        }
        return true;
    }
}
