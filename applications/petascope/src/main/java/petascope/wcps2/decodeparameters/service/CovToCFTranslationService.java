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
package petascope.wcps2.decodeparameters.service;

import petascope.wcps2.metadata.model.Axis;

import java.util.HashMap;
import java.util.Map;

/**
 * This class acts as a translator from Coverage metadata (e.g. axes labels) to NetCDF CF-compliant metadata.
 * The used list of CF-compliant terms is available at: http://cfconventions.org/Data/cf-standard-names/34/build/cf-standard-name-table.html
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class CovToCFTranslationService {

    /**
     * Translates the axis label given by the CRS into the CF-compliant name. In case the mapping is unknown, the crs
     * name is returned.
     * @param axisLabel: the crs axis label to be translate.
     * @return the corresponding CF-compliant name, if a mapping exists, the crs axis label otherwise.
     */
    public String getStandardName(String axisLabel){
        if(STANDARD_NAMES.containsKey(axisLabel)){
            return STANDARD_NAMES.get(axisLabel);
        }
        else {
            return axisLabel;
        }
    }

    /**
     * Translates the unit of measure for the given axisLabel into the CF-compliant name. In case no mapping is known for the
     * given axisLabel, the second parameter is returned.
     * @param axisLabel: the crs axis label for which the CF-compliant unit of measure is to be returned.
     * @param defaultUnitOfMeasure: the unit of measure to be returned in case no mapping is known for the given axisLabel.
     * @return the CF-compliant unit of measure, if a mapping exists, the defaultUnitOfMeasure otherwise.
     */
    public String getUnitOfMeasure(String axisLabel, String defaultUnitOfMeasure){
        if(UNITS_OF_MEASURE.containsKey(axisLabel)){
            return UNITS_OF_MEASURE.get(axisLabel);
        }
        else {
            return defaultUnitOfMeasure;
        }
    }

    /**
     * Translates the axis type into a CF-compliant one.
     * @param defaultAxisType: the crs axis type.
     * @return the CF-compliant axis type.
     */
    public String getAxisType(String axisLabel, String defaultAxisType){
        if(AXIS_TYPES.containsKey(axisLabel)){
            return AXIS_TYPES.get(axisLabel);
        }
        else {
            return defaultAxisType.toUpperCase();
        }
    }

    public final static Map<String, String> STANDARD_NAMES = new HashMap<String, String>();
    public final static Map<String, String> UNITS_OF_MEASURE = new HashMap<String, String>();
    public final static Map<String, String> AXIS_TYPES = new HashMap<String, String>();
    static {
        //EPSG 4326
        STANDARD_NAMES.put("Lat", "latitude");
        STANDARD_NAMES.put("Long", "longitude");

        UNITS_OF_MEASURE.put("Lat", "degree_north");
        UNITS_OF_MEASURE.put("Long", "degree_east");

        AXIS_TYPES.put("Lat", "Y");
        AXIS_TYPES.put("Long", "X");

        //WSG 84
        STANDARD_NAMES.put("N", "latitude");
        STANDARD_NAMES.put("E", "longitude");

        UNITS_OF_MEASURE.put("N", "m");
        UNITS_OF_MEASURE.put("E", "m");

        AXIS_TYPES.put("N", "Y");
        AXIS_TYPES.put("E", "X");

        //OGC ANSI
        STANDARD_NAMES.put("ansi", "time");
        UNITS_OF_MEASURE.put("ansi", "days since 1600-12-31 00:00:00");
        AXIS_TYPES.put("ansi", "T");
    }
}
