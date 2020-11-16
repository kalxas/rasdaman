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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.oapi.handlers.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Class to parse subset for Oapi GetCoverage request
 * 
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class OapiSubsetParsingService {
    
    private static org.slf4j.Logger log = LoggerFactory.getLogger(OapiSubsetParsingService.class);
    
    private static final String COMMA = ",";
    private static final String COLON = ":";
    private static final String RIGHT_PARENTHESIS = ")";
    private static final String TIME_QUOTE = "\"";

    /**
     * Parse the GetCoverage request subsets as list of strings
     * e.g: subset=Lat(0,10) -- WCS style
     *      subset=Lat(0:10) -- WCPS style
     *      subset=ansi("2020-01-01:00"),Lat(0:1),Long(0:2) -- Combination of multiple subsets
     */
    public String[] parseGetCoverageSubsets(String[] inputSubsets) {
        List<String> results = new ArrayList<>();
        
        if (inputSubsets != null) {        
            for (String inputSubset : inputSubsets) {
                inputSubset = inputSubset.trim();
                int indexOfComma = inputSubset.indexOf(COMMA);
                if (indexOfComma < 0) {
                    // subset contains single value, e.g: subset=Lat(0:20) or subset=Lat(0,10) or subset=ansi("2015-01-01")
                    String value = replaceColonByComma(inputSubset);
                    results.add(value);
                } else {
                    // subset contains multiple values, e.g: subset=Lat(0:20),Lat(0,10),ansi("2015-01-01T20:10:40":"2015-02-03T10:10:05")
                    String[] subsets = inputSubset.split(Pattern.quote(RIGHT_PARENTHESIS + COMMA));
                    for (String subset : subsets) {
                        String value = replaceColonByComma(subset + RIGHT_PARENTHESIS);
                        results.add(value);
                    }
                }
            }
        }
        
        return results.toArray(new String[results.size()]);
    }
    
    /**
     * Change interval with WCPS style, separated by ":" to WCS style, separated by ","
     */
    private String replaceColonByComma(String subset) {
        String result;
        
        if (!subset.contains(TIME_QUOTE)) {
            // Lat(0:20) -> Lat(0,10)
            result = subset.replace(COLON, COMMA);
        } else {
            // ansi("2015-01-01T10:00:02":"2015-03-02T10:30:20") -> ansi("2015-01-01T10:00:02","2015-03-02T10:30:20")
            result = subset.replace(COLON + TIME_QUOTE, COMMA + TIME_QUOTE);
        }
        
        return result;
    }

}
