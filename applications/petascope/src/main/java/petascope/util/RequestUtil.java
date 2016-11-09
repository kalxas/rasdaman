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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Utilities for parsing a request.
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class RequestUtil {

    /**
     * Parses the query string of a KVP request into a map of form key => value.
     * Keys are case insensitive, so they are converted to lowercase.
     * @param queryString The KVP query string (i.e. what is after "?" in the request url).
     * @return A map of the form param => value.
     */
    public static Map<String, String> parseKVPRequestParams(String queryString) {        
        //split query string into parameters
        Map<String, String> params = new HashMap<String, String>();
        // In case of XML POST request then queryString is null
        if (queryString == null) {
          return params;
        }
        String[] queryParts = queryString.split(KVPParamSeparator);
        for (String queryComponent : queryParts) {
            String[] componentParts = queryComponent.split(KVPKeyValueSeparator);
            if (componentParts.length > 1) {
                //some times the query is split in more than 2 parts, i.e. when the separator appears in the body
                //example: query=encode("nodata=0"). "=" is the separator. For this reason the parts need to be joined.
                String[] remainingParts = Arrays.copyOfRange(componentParts, 1, componentParts.length);
                String concatenatedParts = StringUtils.join(remainingParts, KVPKeyValueSeparator);
                params.put(componentParts[0].toLowerCase(), StringUtil.urldecode(concatenatedParts, null));
            }
        }
        return params;
    }
    

    /**
     * The separator between parameters in a KVP request, e.g.
     * firstParam=firstValue<b>&</b>secondParam=secondValue
     */
    private static final String KVPParamSeparator = "&";
    /**
     * The separator between the key and the value of a parameter in a KVP request, e.g.
     * firstParam<b>=</b>firstValue
     */
    private static final String KVPKeyValueSeparator = "=";
}
