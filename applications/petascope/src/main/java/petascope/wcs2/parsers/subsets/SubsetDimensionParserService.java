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
package petascope.wcs2.parsers.subsets;

import java.util.*;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;

/**
 * Parsing functionality for subsets in WCS requests. The functionality has been
 * refactored from GetCoverageParser, as new request types that use subset
 * parameters have been added.
 */
public class SubsetDimensionParserService {

    /**
     * Parses the list of subsets from WCS request e.g: for WCST:
     * subset=Long(-180.0,180.0) or WCS with subsettingCrs:
     * subset=E,http://www.opengis.net/def/crs/EPSG/0/3857(-1.3637472939075228E7,-1.3636585328807762E7)
     *
     * @param subsets
     * @return
     */
    public static List<AbstractSubsetDimension> parseSubsets(String[] subsets) throws WCSException {
        List<AbstractSubsetDimension> subsetDimensions = new ArrayList<>();
        for (String subset : subsets) {
            // e.g: E,http://...../4326 or E
            String dimensionTmp = subset.trim().split("\\(")[0];
            String dimensionName = null;
            String dimensionCrs = null;
            
            // Trimming subset
            if (dimensionTmp.contains(",")) {
                String[] tmp = dimensionTmp.trim().split(",");
                dimensionName = tmp[0];
                // optimal parameter
                dimensionCrs = tmp[1];
            } else {
                // Slicing subset
                dimensionName = dimensionTmp.trim();
            }
            
            // e.g: (0,20) or (0, 30) -> 0,20
            String intervalTmp = subset.trim().split("\\(")[1].replace(")", "");
            // NOTE: + is escaped in AbstractController as space, so if client does not encode + to %2B, it will be replaced with space
            // so datetime format, e.g: 2008-01-05+03:05:03Z will be replaced as 2008-01-05 03:05:03Z, so we want to keep it as same as from client.
            if (intervalTmp.contains("\"")) {
                intervalTmp = intervalTmp.replaceAll(" ", "+");
            }
            String lowerBound = null;
            String upperBound = null;

            if (intervalTmp.contains(",")) {
                // Trimming subset
                lowerBound = intervalTmp.trim().split(",")[0].trim();
                upperBound = intervalTmp.trim().split(",")[1].trim();
            } else {
                // Slicing subset
                lowerBound = intervalTmp.trim();
            }
            
            
            AbstractSubsetDimension subsetDimension;

            if (upperBound == null) {
                // Slicing subset
                subsetDimension = new SlicingSubsetDimension(dimensionName, dimensionCrs, lowerBound);
            } else {
                // Trimming subset
                subsetDimension = new TrimmingSubsetDimension(dimensionName, dimensionCrs, lowerBound, upperBound);
            }

            subsetDimensions.add(subsetDimension);
        }
        
        // NOTE: WCS does not support duplicate subsets on a request, e.g: subset=i(0,300)&subset=i(0) is not valid
        validateSubsetName(subsetDimensions);        

        return subsetDimensions;
    }
    
    /**
     * Validate the current subset name
     * @param subsetName
     * @param subsets 
     */
    private static void validateSubsetName(List<AbstractSubsetDimension> subsetDimensions) throws WCSException {
        for (int i = 0; i < subsetDimensions.size(); i++) {
            String subsetName = subsetDimensions.get(i).getDimensionName();
            for (int j = i + 1; j < subsetDimensions.size(); j++) {
                String nextSubsetName = subsetDimensions.get(j).getDimensionName();
                if (subsetName.equals(nextSubsetName)) {
                    // There are duplicated names for one subset and is not valid
                    throw new WCSException(ExceptionCode.WCSBadRequest, "Duplicated subsets with given name: " + subsetName + ".");
                }
            }
        }
    }
}
