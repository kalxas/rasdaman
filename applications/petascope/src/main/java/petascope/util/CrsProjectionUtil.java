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
import java.util.List;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.WCSException;
import petascope.wcps2.encodeparameters.model.BoundingBox;

/**
 * This class will provide utility method for projecting interval in
 * geo-referenced axis
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class CrsProjectionUtil {

    private static final Logger log = LoggerFactory.getLogger(CrsUtil.class);

    /**
     * Transform a bounding box (e.g: xmin,ymin,xmax,ymax) from sourceCrs to
     * targetCrs
     *
     * @param sourceCrs
     * @param targetCrs
     * @param srcCoords
     * @return List<BigDecimal> of xmin, ymin, xmax, ymax
     * @throws WCSException
     */
    public static List<BigDecimal> transformBoundingBox(String sourceCrs, String targetCrs, double[] srcCoords) throws WCSException {
        List<BigDecimal> out = new ArrayList<BigDecimal>(srcCoords.length);

        // xmin, ymin
        double[] xyMinArray = new double[]{srcCoords[0], srcCoords[1]};
        out.addAll(transform(sourceCrs, targetCrs, xyMinArray));

        // xmax, ymax
        double[] xyMaxArray = new double[]{srcCoords[2], srcCoords[3]};
        out.addAll(transform(sourceCrs, targetCrs, xyMaxArray));

        return out;
    }

    /**
     * Transform a BoundingBox object with xmin, ymin, xmax, ymax from soureCrs
     * to targetCrs
     *
     * @param sourceCrs
     * @param targetCrs
     * @param bbox BoundingBox
     * @return
     * @throws petascope.exceptions.WCSException
     */
    public static BoundingBox transformBoundingBox(String sourceCrs, String targetCrs, BoundingBox bbox) throws WCSException {
        List<BigDecimal> out = new ArrayList<>();

        // xmin, ymin
        double[] xyMinArray = new double[]{bbox.getXMin().doubleValue(), bbox.getYMin().doubleValue()};
        out.addAll(transform(sourceCrs, targetCrs, xyMinArray));

        // xmax, ymax
        double[] xyMaxArray = new double[]{bbox.getXMax().doubleValue(), bbox.getYMax().doubleValue()};
        out.addAll(transform(sourceCrs, targetCrs, xyMaxArray));

        return new BoundingBox(out.get(0), out.get(1), out.get(2), out.get(3));
    }

    // Methods
    /**
     * Overloaded transform methods:
     *
     * @param sourceCrs source CRS of axis
     * @param targetCrs target CRS to project
     * @param sourceCoordinates Array of input coordinates: min values first,
     * max values next. E.g. [xMin,yMin,xMax,yMax].
     * @return List<BigDecimal> Locations transformed to targetCRS (defined at
     * construction time).
     * @throws WCSException
     */
    public static List<BigDecimal> transform(String sourceCrs, String targetCrs, double[] sourceCoordinates) throws WCSException {

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
        for (int i = 0; i < transformedCoordinate.length - 1; i++) {
            out.add(new BigDecimal(transformedCoordinate[i]));
        }

        return out;
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
