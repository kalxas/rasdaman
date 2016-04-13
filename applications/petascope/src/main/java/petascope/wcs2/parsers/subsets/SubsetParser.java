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

import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static petascope.util.KVPSymbols.KEY_SUBSET;
import static petascope.wcs2.parsers.subsets.DimensionSubset.QUOTED_SUBSET;

/**
 * Parsing functionality for subsets in WCS requests.
 * The functionality has been refactored from GetCoverageParser, as new request types that use subset parameters
 * have been added.
 */
public class SubsetParser {

    private static final Pattern PATTERN = Pattern.compile("([^,\\(]+)(,([^\\(]+))?\\(([^,\\)]+)(,([^\\)]+))?\\)");

    /**
     * Parses and checks the list of subsets.
     *
     * @param request the string representing the request.
     * @return
     * @throws petascope.exceptions.WCSException
     */
    public static List<DimensionSubset> parseSubsets(String request) throws WCSException {
        List<DimensionSubset> ret = new ArrayList<DimensionSubset>();
        HashMap<String, String> subsets = parseSubsetParams(request);
        Set<String> subsetsDims = new HashSet<String>();
        for (Map.Entry<String, String> subset : subsets.entrySet()) {
            String subsetKey = (String) subset.getKey();
            String subsetValue = (String) subset.getValue();
            Matcher matcher = PATTERN.matcher(subsetValue);
            if (matcher.find()) {
                String dim = matcher.group(1);
                String crs = matcher.group(3);
                String low = matcher.group(4);
                String high = matcher.group(6);
                if (!subsetsDims.add(dim)) {
                    // /conf/core/getCoverage-request-no-duplicate-dimension
                    throw new WCSException(ExceptionCode.InvalidAxisLabel, "Dimension " + dim + " is duplicated in the request subsets.");
                }
                if (high == null) {
                    DimensionSlice currentSubset = new DimensionSlice(dim, crs, low.trim());
                    ret.add(currentSubset);
                    if (null != low && low.matches(QUOTED_SUBSET)) {
                        currentSubset.timestampSubsetCheck();
                    }
                } else if (dim != null) {
                    DimensionTrim currentSubset = new DimensionTrim(dim, crs, low.trim(), high.trim());
                    ret.add(currentSubset);
                    if (null != low && (low.matches(QUOTED_SUBSET) || high.matches(QUOTED_SUBSET))) {
                        currentSubset.timestampSubsetCheck();
                    }
                } else {
                    throw new WCSException(ExceptionCode.InvalidEncodingSyntax.locator(subsetKey));
                }
            } else {
                throw new WCSException(ExceptionCode.InvalidEncodingSyntax.locator(subsetKey));
            }
        }

        return ret;
    }

    /**
     * Parses any subset parameters defined as in OGC 09-147r1 standard(e.g.
     * ...&subset=x(200,300)&subset=y(300,200)) and for backwards compatibility
     * subsets defined as subsetD(where D is any distinct string)(e.g.
     * &subsetA=x(200,300))
     *
     * @param request - the request parameters as a string
     * @return ret - a hashmap containing the subsets
     */
    public static HashMap<String, String> parseSubsetParams(String request) {
        HashMap<String, String> ret = new HashMap<String, String>();
        StringTokenizer st = new StringTokenizer(request, "&");
        while (st.hasMoreTokens()) {
            String kvPair = (String) st.nextToken();
            int splitPos = kvPair.indexOf("=");
            if (splitPos != -1) {
                String key = kvPair.substring(0, splitPos);
                String value = kvPair.substring(splitPos + 1);
                if (key.equalsIgnoreCase(KEY_SUBSET)) {
                    ret.put(key + value, value);
                }
                //Backward compatibility
                else if (key.toLowerCase().startsWith(KEY_SUBSET)) {
                    ret.put(key + value, value);
                }
            }
        }

        return ret;
    }
}
