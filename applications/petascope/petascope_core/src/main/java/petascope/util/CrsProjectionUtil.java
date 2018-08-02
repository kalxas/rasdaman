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
import java.util.List;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.WCSException;
import petascope.core.BoundingBox;
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

    /**
     * Transform a XY values from sourceCRS to targetCRS (e.g: Long Lat (EPSG:4326) to ESPG:3857).
     * NOTE: Not every reprojection is possible, even both sourceCRS, targetCRS are from EPSG codes.
     *
     * @param sourceCrs source CRS of axis
     * @param targetCrs target CRS to project
     * @param sourceCoordinates Array of input coordinates: min values first,
     * max values next. E.g. [xMin,yMin,xMax,yMax].
     * @return List<BigDecimal> Locations transformed to targetCRS (defined at
     * construction time).
     * @throws WCSException
     */
    public static List<BigDecimal> transform(String sourceCrs, String targetCrs, double[] sourceCoordinates) throws WCSException, PetascopeException {

        gdal.AllRegister();
        
        String sourceCrsEPSGCode = sourceCrs;
        String targetCrsEPSGCode = targetCrs;
        // NOTE: sourceCrs and targetCrs can be CRS Uri (e.g: http://.../4326) or already EPSG:4326
        if (!sourceCrsEPSGCode.contains(CrsUtil.EPSG_AUTH + ":")) {
            sourceCrsEPSGCode = CrsUtil.getEPSGCode(sourceCrsEPSGCode);
        }
        if (!targetCrsEPSGCode.contains(CrsUtil.EPSG_AUTH + ":")) {
            targetCrsEPSGCode = CrsUtil.getEPSGCode(targetCrsEPSGCode);
        }

        // e.g: 4326, 32633
        int sourceCode = Integer.valueOf(sourceCrsEPSGCode.split(":")[1]);
        int targetCode = Integer.valueOf(targetCrsEPSGCode.split(":")[1]);

        // Using gdal native library to transform the coordinates from source crs to target crs
        SpatialReference sourceSpatialReference = new SpatialReference();
        sourceSpatialReference.ImportFromEPSG(sourceCode);

        SpatialReference targetSpatialReference = new SpatialReference();
        targetSpatialReference.ImportFromEPSG(targetCode);

        CoordinateTransformation coordinateTransformation = CoordinateTransformation.CreateCoordinateTransformation(sourceSpatialReference, targetSpatialReference);
        // This will returns 3 values, translated X, translated Y and another value which is not used
        double[] transformedCoordinate = coordinateTransformation.TransformPoint(sourceCoordinates[0], sourceCoordinates[1]);

        List<BigDecimal> out = new ArrayList<>(sourceCoordinates.length);
        // NOTE: projection can return NaN which means cannot reproject from sourceCRS to targetCRS
        // e.g: EPSG:3577 (75042.7273594 5094865.55794) to EPSG:4326
        for (int i = 0; i < transformedCoordinate.length - 1; i++) {
            Double value = transformedCoordinate[i];
            if (Double.isNaN(value)) {
                throw new PetascopeException(ExceptionCode.InternalComponentError, 
                        "Cannot reproject from sourceCrs '" + sourceCrs + "' to targetCRS '" + targetCrs + "' with XY values '" + Arrays.toString(sourceCoordinates) + " as result is NaN.");
            }
            out.add(new BigDecimal(value));
        }

        return out;
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
    public static GeoTransform getGeoTransformInTargetCRS(GeoTransform sourceGeoTransform, String targetCRS) throws PetascopeException, SecoreException {
        
        // Source extents
        Dataset sourceDataSet = gdal.GetDriverByName("VRT").Create("", sourceGeoTransform.getGridWidth(), sourceGeoTransform.getGridHeight());
        // e.g: test_mean_summer_airtemp ([111.9750000, 0.05, 0, -8.9750000, 0, -0.05])
        double[] gdalGeoTransform = new double[] {sourceGeoTransform.getUpperLeftGeoX(), 
                                    sourceGeoTransform.getGeoXResolution(), 0, sourceGeoTransform.getUpperLeftGeoY(), 0, sourceGeoTransform.getGeoYResolution()};
        sourceDataSet.SetGeoTransform(gdalGeoTransform);
        
        SpatialReference sourceSpatialReference = new SpatialReference();
        sourceSpatialReference.ImportFromEPSG(sourceGeoTransform.getEPSGCode());
        String sourceCRSWKT = sourceSpatialReference.ExportToWkt();
        
        SpatialReference targetSpatialReference = new SpatialReference();
        int targetEPSGCode = new Integer(CrsUtil.getCode(targetCRS));
        targetSpatialReference.ImportFromEPSG(targetEPSGCode);
        String targetCRSWKT = targetSpatialReference.ExportToWkt();
        
        
        // error threshold for transformation approximation (in pixel units - defaults to 0.125)
        double error_threshold = 0.125;
        // Expected target extents
        Dataset targetDataSet = gdal.AutoCreateWarpedVRT(sourceDataSet, sourceCRSWKT, targetCRSWKT, gdalconstConstants.GRA_NearestNeighbour, error_threshold);
        if (targetDataSet == null) {
            // NOTE: this happens when gdal cannot translate a 2D bounding box from source CRS (e.g: EPSG:4326) to a target CRS (e.g: EPSG:32652)
            throw new PetascopeException(ExceptionCode.RuntimeError, "Cannot estimte the input geo domains "
                    + "from source CRS 'EPSG:" + sourceGeoTransform.getEPSGCode() + "' to target CRS 'EPSG:" + targetEPSGCode + "', given source geoTransform values '" + sourceGeoTransform + "'.");
        }
        double[] values = targetDataSet.GetGeoTransform();
        
        GeoTransform targetGeoTransform = new GeoTransform();
        targetGeoTransform.setEPSGCode(targetEPSGCode);
        
        // OutputCRS is also X-Y order, e.g: EPSG:3857
        targetGeoTransform.setUpperLeftGeoX(values[0]);
        targetGeoTransform.setGeoXResolution(values[1]);

        targetGeoTransform.setUpperLeftGeoY(values[3]);
        targetGeoTransform.setGeoYResolution(values[5]);

        targetGeoTransform.setGridWidth(targetDataSet.GetRasterXSize());
        targetGeoTransform.setGridHeight(targetDataSet.GetRasterYSize());
        
        return targetGeoTransform;
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
