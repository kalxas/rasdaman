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

import java.util.HashMap;
import java.util.Map;
import petascope.HTTPRequest;

/**
 * Utilities for parsing a request.
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class RequestUtil {

    /**
     * Parses the query string of a KVP request into a map of form param => value.
     * @param queryString The KVP query string (i.e. what is after "?" in the request url).
     * @return A map of the form param => value.
     */
    public static Map<String, String> parseKVPRequestParams(String queryString){
        //split query string into parameters
        Map<String, String> params = new HashMap<String, String>();
        String[] queryParts = queryString.split(KVPParamSeparator);
        for (String queryComponent : queryParts) {
            String[] componentParts = queryComponent.split(KVPKeyValueSeparator);
            if (componentParts.length > 1) {
                params.put(componentParts[0], StringUtil.urldecode(componentParts[1], null));
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
