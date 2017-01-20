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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.WCSException;
import petascope.util.CrsProjectionUtil;
import petascope.util.CrsUtil;
import petascope.wms2.metadata.EXGeographicBoundingBox;
import petascope.wms2.service.exception.error.WMSUnsupportedCrsToTransformException;

import java.math.BigDecimal;
import java.util.List;

import static petascope.util.CrsProjectionUtil.validTransformation;

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
    private static final String DEFAULT_CRS_CODE = "EPSG:4326";
    private static Logger log = LoggerFactory.getLogger(CrsComputer.class);

    /**
     * Converts a bounding box into an ExGeographicBoundingBox (WGS84 crs).
     *
     * @param originalCrs the name of the original crs.
     * @param minX        the min value on the first axis.
     * @param minY        the min value on the second axis.
     * @param maxX        the max value on the first axis.
     * @param maxY        the max value on the second axis.
     * @return the ExGeographicBoundingBox object.
     */
    public static EXGeographicBoundingBox covertToWgs84(String originalCrs, double minX, double minY, double maxX, double maxY) throws WMSUnsupportedCrsToTransformException, WCSException {
        // current only support coverage imported with EPSG crs (e.g: EPSG:4326, EPSG:3857,... not OGC:IndexND)
        if (!validTransformation(CrsUtil.EPSG_AUTH)) {
            throw new WMSUnsupportedCrsToTransformException(originalCrs);
        }

        String originalCrsCode = CrsUtil.getEPSGCode(originalCrs);
        if (originalCrsCode.equals(DEFAULT_CRS_CODE)) {
            return new EXGeographicBoundingBox(String.valueOf(minX), String.valueOf(maxX),
                                               String.valueOf(minY), String.valueOf(maxY));
        }

        double[] srcCoords = new double[] { minX, minY,maxX, maxY };
        // transform from the native CRS of coverage (e.g: EPSG:3857 to default crs: EPSG:4326)
        List<BigDecimal> transformedBBox = CrsProjectionUtil.transformBoundingBox(originalCrsCode, DEFAULT_CRS_CODE, srcCoords);

        // the EX_GeographicBoundingBox (westLong, eastLong, southLat, northLat)
        return new EXGeographicBoundingBox(transformedBBox.get(0).toPlainString(), transformedBBox.get(2).toPlainString(),
                                           transformedBBox.get(1).toPlainString(), transformedBBox.get(3).toPlainString());
    }
}
