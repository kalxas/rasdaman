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
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.wms2.metadata.EXGeographicBoundingBox;
import petascope.wms2.service.exception.error.WMSInvalidCrsUriException;

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
     * Needed for the WGS84 coordinates
     */
    private static final String DEFAULT_CRS = "EPSG:4326";
    /**
     * For logging
     */
    private static Logger log = LoggerFactory.getLogger(CrsComputer.class);

    /**
     * Converts a bounding box into an ExGeographinBoundingBox (WGS84 crs).
     *
     * @param originalCrs the name of the original crs.
     * @param minX        the min value on the first axis.
     * @param minY        the min value on the second axis.
     * @param maxX        the max value on the first axis.
     * @param maxY        the max value on the second axis.
     * @return the ExGeographinBoundingBox object.
     */
    public static EXGeographicBoundingBox covertToWgs84(String originalCrs, double minX, double minY, double maxX, double maxY) {
        //check if the crs is not index2d
        if (originalCrs.startsWith("OGC")) {
            //not geo-referenced
            return new EXGeographicBoundingBox("0", "0", "0", "0");
        }

        double[] minCrsPoint = new double[] {minX, minY};
        double[] maxCrsPoint = new double[] {maxX, maxY};

        try {
            minCrsPoint = getDefaultCrsCoord(minX, minY, originalCrs);
            maxCrsPoint = getDefaultCrsCoord(maxX, maxY, originalCrs);
        } catch (FactoryException e) {
            handleExceptionFromOpenGIS(originalCrs, minX, minY, maxX, maxY);
        } catch (TransformException e) {
            handleExceptionFromOpenGIS(originalCrs, minX, minY, maxX, maxY);
        }

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
            return new double[] {easting, northing};
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

    /***
     * Handle exception when try to get the originalCRS from opengis
     *
     * @param originalCrs Name of CRS want to find
     * @param minX        min X in bounding box
     * @param minY        min Y in bounding box
     * @param maxX        max X in bounding box
     * @param maxY        max Y in bounding box
     * @return
     */
    private static void handleExceptionFromOpenGIS(String originalCrs, double minX, double minY, double maxX, double maxY) {
        log.debug("Could not find the given crs: {} in the list of CRS definitions.", originalCrs);
        log.debug("Returning the original coordinates of the layer's bounding box: [{}, {}, {}, {}]", minX, minY, maxX, maxY);
    }
}
