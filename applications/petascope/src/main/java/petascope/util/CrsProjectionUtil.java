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
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import static petascope.util.CrsUtil.getEPSGCode;

/**
 * This class will provide utility method for projecting interval in geo-referenced axis
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class CrsProjectionUtil {

    private static final Logger log = LoggerFactory.getLogger(CrsUtil.class);

    /**
     * Transform a bounding box (e.g: xmin,xmax,ymin,ymax) from sourceCrs to targetCrs
     * @param sourceCrs
     * @param targetCrs
     * @param srcCoords
     * @return
     * @throws WCSException
     */
    public static List<BigDecimal> transformBoundingBox(String sourceCrs, String targetCrs, double[] srcCoords) throws WCSException {
        List<BigDecimal> out = new ArrayList<BigDecimal>(srcCoords.length);

        // xmin, ymin
        double[] xArray = new double[] { srcCoords[0], srcCoords[1] };
        out.addAll(transform(sourceCrs, targetCrs, xArray, false));

        // xmax, ymax
        double[] yArray = new double[] { srcCoords[2], srcCoords[3] };
        out.addAll(transform(sourceCrs, targetCrs, yArray, false));

        return out;
    }

    // Methods
    /** Overloaded transform methods:
     * @param sourceCrs source CRS of axis
     * @param targetCrs target CRS to project
     * @param   srcCoords       Array of input coordinates: min values first, max values next. E.g. [xMin,yMin,xMax,yMax].
     * @param isSwapCoords      Normally when source coordinate has correct x and y then isSwapCoords = true, otherwise it should not swap.
     * @return  List<BigDecimal>    Locations transformed to targetCRS (defined at construction time).
     * @throws  WCSException
     */
    public static List<BigDecimal> transform (String sourceCrs, String targetCrs, double[] srcCoords, boolean isSwapCoords) throws WCSException {

        try {
            double[] trasfCoords = new double[srcCoords.length];

            CoordinateReferenceSystem sCrsID = CRS.decode(getEPSGCode(sourceCrs));
            CoordinateReferenceSystem tCrsID = CRS.decode(getEPSGCode(targetCrs));
            MathTransform transform = CRS.findMathTransform(sCrsID, tCrsID);

            // Transform
            JTS.xform(transform, srcCoords, trasfCoords);

            /* Re-order transformed coordinates: warping between different projections
             * might change the ordering of the points. Eg with a small horizontal subsets
             * and a very wide vertical subset:
             *
             *   EPSG:32634 (UTM 34N) |  EPSG:4326 (pure WGS84)
             *   230000x   3800000y   =  18.07lon   34.31lat
             *   231000x   4500000y   =  17.82lon   40.61lat
             */
            /**
             * In case of we don't have x or y value and add the 0 for the missing value.
             * We don't need the swap function as it will return the wrong order.
             * As we have the same origin (0) then the assumption above is not valid..
             * e.g: x=0, y=15000 -> x=20, y=0. which is wrong, should be x=0, y=20
             */
            if(isSwapCoords) {
                double buf;
                for (int i = 0; i < trasfCoords.length/2; i++) {
                    if (trasfCoords[i] > trasfCoords[i+trasfCoords.length/2]) {
                        // Need to swap the coordinates
                        buf = trasfCoords[i];
                        trasfCoords[i] = trasfCoords[i+trasfCoords.length/2];
                        trasfCoords[i+trasfCoords.length/2] = buf;
                    }
                }
            }

            // Format output
            List<BigDecimal> out = new ArrayList<BigDecimal>(srcCoords.length);
            for (int i = 0; i < trasfCoords.length; i++)
                out.add(new BigDecimal(trasfCoords[i]));

            return out;

        } catch (TransformException e) {
            log.error("Error while transforming coordinates.\n" + e.getMessage());
            throw new WCSException(ExceptionCode.InternalComponentError, "Error while transforming point: " + e.getMessage());
        } catch (ClassCastException e) {
            log.error("Inappropriate key when accessing CrsUtil.loadedTransforms.\n" + e.getMessage());
            throw new WCSException(ExceptionCode.InternalComponentError, "Error while transforming point: " + e.getMessage());
        } catch (NullPointerException e) {
            log.error("Null key when accessing CrsUtil.loadedTransforms.\n" + e.getMessage());
            throw new WCSException(ExceptionCode.InternalComponentError, "Error while transforming point: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error while transforming point." + e.getMessage());
            throw new WCSException(ExceptionCode.InternalComponentError, "Error while transforming point: " + e.getMessage());
        }
    }
}
