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
package petascope.util.ras;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.wcst.WCSTNoReadPermissionException;

/**
 * Utility for getting gdalinfo output
 */
public class Gdalinfo {

    /**
     * Gets the number of dimensions and the type of each band from a file, as
     * given by gdalinfo.
     *
     * @param filePath the path to the file
     * @return
     * @throws IOException
     */
    public static Pair<Integer, ArrayList<String>> getDimensionAndTypes(String filePath) throws IOException, WCSTNoReadPermissionException {
        log.trace("Reading gdal info output for " + filePath);
        Integer dimensions = 0;
        ArrayList<String> bandTypes = new ArrayList<String>();
        File f = new File(filePath);
        if (!f.canRead()) {
            log.error("Failed retrieving gdalinfo for file: " + filePath);
            throw new WCSTNoReadPermissionException(filePath);
        }
        try {
            Process process = Runtime.getRuntime().exec(GDAL_INFO + filePath);
            BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String currentLine;
            while ((currentLine = stdOutReader.readLine()) != null) {
                //parse number of dimensions
                if (currentLine.startsWith(GDAL_SIZE)) {
                    dimensions = parseNumberOfDimensions(currentLine);
                }
                //parse band type information
                if (currentLine.startsWith(GDAL_BAND)) {
                    bandTypes.add(parseBandType(currentLine));
                }
            }
        } catch (IOException e) {
            log.error("Failed retrieving gdalinfo for file: " + e.getMessage());
            throw e;
        }
        log.trace("Got " + dimensions + " dimensions and " + bandTypes.toString() + " band types.");
        return Pair.of(dimensions, bandTypes);
    }

    /**
     * Parses the number of dimensions from a gdalinfo output line.
     *
     * @param sizeLine the line containing information about the size of the
     * file.
     * @return
     */
    private static Integer parseNumberOfDimensions(String sizeLine) {
        return sizeLine.split(GDAL_DIMENSION_SEPARATOR).length;
    }

    /**
     * Parses the type of a band from gdalinfo output.
     *
     * @param bandLine the line containing information about a band.
     * @return
     */
    private static String parseBandType(String bandLine) {
        String[] typeInfo = bandLine.split(GDAL_TYPE_IDENTIFIER);
        return typeInfo[1].split(GDAL_TYPE_SEPARATOR)[0];
    }

    private final static Logger log = LoggerFactory.getLogger(Gdalinfo.class);
    private final static String GDAL_INFO = "gdalinfo ";
    private final static String GDAL_SIZE = "Size is ";
    private final static String GDAL_DIMENSION_SEPARATOR = ",";
    private final static String GDAL_BAND = "Band ";
    private final static String GDAL_TYPE_IDENTIFIER = "Type=";
    private final static String GDAL_TYPE_SEPARATOR = ",";
}
