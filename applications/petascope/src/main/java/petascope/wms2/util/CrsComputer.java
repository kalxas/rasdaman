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

package petascope.wms2.util;


import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import petascope.wms2.metadata.BoundingBox;
import petascope.wms2.metadata.EXGeographicBoundingBox;
import petascope.wms2.service.exception.error.WMSInvalidCrsUriException;
import petascope.wms2.service.getmap.access.RasdamanSubset;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents a crs computer that translates from geographic coordinates to pixel indices. It does this by translating
 * the given coordinates into a default coordinate crs system and then calculate it in pixels given the width and height
 * of the whole map.
 * The  default crs to pixel coordinates formula is taken from here.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class CrsComputer {

    /**
     * Translates from geographical coordinates to pixel indices.
     *
     * @param subsetBoundingBox the bounding box to be translated
     * @param mapBoundingBox    the bounding box of the whole map
     * @param mapWidth          the width of the map on which the bounding box exists
     * @param mapHeight         the height of the map on which the bounding box exists
     * @param xOrder            the order of the x axis in rasdaman
     * @param yOrder            the order of the y axis in rasdaman
     * @return the pixel coordinates
     */
    public static List<RasdamanSubset> convertToPixelIndices(BoundingBox subsetBoundingBox, BoundingBox mapBoundingBox,
                                                             long mapWidth, long mapHeight, int xOrder, int yOrder){
        long[] xAxis = crsSubsetToPixelIndices(subsetBoundingBox.getMinx(), subsetBoundingBox.getMaxx(), mapBoundingBox.getMinx(), mapBoundingBox.getMaxx(),
                0, mapWidth, false);
        long[] yAxis = crsSubsetToPixelIndices(subsetBoundingBox.getMiny(), subsetBoundingBox.getMaxy(), mapBoundingBox.getMiny(), mapBoundingBox.getMaxy(),
                0, mapHeight, true);
        RasdamanSubset subsetX = new RasdamanSubset(xOrder, xAxis[0], xAxis[1]);
        RasdamanSubset subsetY = new RasdamanSubset(yOrder, yAxis[0], yAxis[1]);
        return Arrays.asList(subsetX, subsetY);
    }

    /**
     * Converts a bounding box into an ExGeographinBoundingBox (WGS84 crs).
     *
     * @param originalCrs the name of the original crs.
     * @param minX        the min value on the first axis.
     * @param minY        the min value on the second axis.
     * @param maxX        the max value on the first axis.
     * @param maxY        the max value on the second axis.
     * @return the ExGeographinBoundingBox object.
     * @throws TransformException
     * @throws FactoryException
     */
    public static EXGeographicBoundingBox covertToWgs84(String originalCrs, double minX, double minY, double maxX, double maxY) throws TransformException, FactoryException {
        //check if the crs is not index2d
        if (originalCrs.startsWith("OGC")) {
            //not geo-referenced
            return new EXGeographicBoundingBox("0", "0", "0", "0");
        }
        double[] minCrsPoint = getDefaultCrsCoord(minX, minY, originalCrs);
        double[] maxCrsPoint = getDefaultCrsCoord(maxX, maxY, originalCrs);
        return new EXGeographicBoundingBox(String.valueOf(minCrsPoint[0]), String.valueOf(maxCrsPoint[0]),
                String.valueOf(minCrsPoint[1]), String.valueOf(maxCrsPoint[1]));
    }

    /**
     * Converst a crs uri into the format used by wms (institution:number). Example:
     * http://www.opengis.net/def/crs/EPSG/0/4326 => EPSG:4326
     *
     * @param crsUri the crs in uri format.
     * @return the crs in wms format.
     * @throws petascope.wms2.service.exception.error.WMSInvalidCrsUriException
     */
    public static String convertCrsUriToWmsCrs(String crsUri) throws WMSInvalidCrsUriException {
        String[] parts = crsUri.split("/0/");
        if (parts.length != 2) {
            throw new WMSInvalidCrsUriException(crsUri);
        }
        String[] institutionParts = parts[0].split("/");
        String institution = institutionParts[institutionParts.length - 1];
        String crsNo = parts[1];
        return institution + ":" + crsNo;
    }

    /**
     * Translates the coordinates from any 2D crs to our default CRS
     *
     * @param easting  the easting coordinate
     * @param northing the northing coordinate
     * @param crs      the original crs
     * @return the coordinates in the default crs
     * @throws TransformException
     * @throws FactoryException
     */
    private static double[] getDefaultCrsCoord(double easting, double northing, String crs) throws TransformException, FactoryException {
        if (crs.equalsIgnoreCase(DEFAULT_CRS)) {
            return new double[]{easting, northing};
        } else {
            CRSAuthorityFactory factory = CRS.getAuthorityFactory(true);
            CoordinateReferenceSystem srcCRS = factory.createCoordinateReferenceSystem(crs);
            CoordinateReferenceSystem dstCRS = factory.createCoordinateReferenceSystem(DEFAULT_CRS);
            MathTransform transform = CRS.findMathTransform(srcCRS, dstCRS, true);
            double[] srcProjec = {easting, northing};// easting, northing,
            double[] dstProjec = {0, 0};
            transform.transform(srcProjec, 0, dstProjec, 0, 1);
            return dstProjec;
        }
    }

    /**
     * Converts a crs geographic coordinates on one axis into pixel indices
     *
     * @param minCoord    the minimum coordinate
     * @param maxCoord    the maximum coordinate
     * @param minCoordMap the min coord of the whole map
     * @param maxCoordMap the max coord of the whole map
     * @param pxMin       the minimum pixel subset value on the axis
     * @param pxMax       the maximum pixel subset value on the axis
     * @param reversed    if the axis is reversed set this to true
     * @return the pixel indices
     */
    private static long[] crsSubsetToPixelIndices(double minCoord, double maxCoord,
                                                  double minCoordMap, double maxCoordMap,
                                                  long pxMin, long pxMax, boolean reversed) {
        BigDecimal domMin = new BigDecimal(minCoordMap);
        BigDecimal domMax = new BigDecimal(maxCoordMap);

        double cellWidth = (maxCoordMap - minCoordMap) / (pxMax - pxMin);
        long returnLowerLimit, returnUpperLimit;
        if (!reversed) {
            // Normal linear numerical axis
            returnLowerLimit = (long) Math.floor((minCoord - domMin.doubleValue()) / cellWidth) + pxMin;
            returnUpperLimit = (long) Math.floor((maxCoord - domMin.doubleValue()) / cellWidth) + pxMin;
            // NOTE: the if a slice equals the upper bound of a coverage, out[0]=pxHi+1 but still it is a valid subset.
            if (minCoord == maxCoord && maxCoord == domMax.doubleValue()) {
                returnLowerLimit = returnLowerLimit - 1;
            }
        } else {
            // Linear negative axis (eg northing of georeferenced images)
            // First coordHi, so that left-hand index is the lower one
            returnLowerLimit = (long) Math.ceil((domMax.doubleValue() - maxCoord) / cellWidth) + pxMin;
            returnUpperLimit = (long) Math.ceil((domMax.doubleValue() - minCoord) / cellWidth) + pxMin;
            // NOTE: the if a slice equals the lower bound of a coverage, out[0]=pxHi+1 but still it is a valid subset.
            if (minCoord == maxCoord && maxCoord == domMin.doubleValue()) {
                returnLowerLimit -= 1;
            }
        }
        return new long[]{returnLowerLimit, returnUpperLimit};
    }


    /**
     * We know how to compute pixel coords from this one so use it as a base
     */
    private static final String DEFAULT_CRS = "EPSG:4326";
}
