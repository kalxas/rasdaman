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
package petascope.wcs2.parsers;

import java.util.*;

import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.ListUtil;
import petascope.util.StringUtil;
import petascope.wcs2.handlers.RequestHandler;

/**
 * KVP Parser for the Process Coverage extension. Parses the HTTP request into a ProcessCoverageRequest that
 * can be further consumed by handlers.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class KVPProcessCoverageParser extends KVPParser<ProcessCoverageRequest> {
    /**
     * Parses the HTTPRequest into a ProcessCoverageRequest.
     *
     * @param request the http request
     * @return the parsed process coverage request
     * @throws WCSException
     */
    @Override
    public ProcessCoverageRequest parse(HTTPRequest request) throws WCSException {
        Map<String, String> params = new HashMap<String, String>(3);
        String[] queryParts = request.getQueryString().split("&");
        for (String queryComponent : queryParts) {
            String[] componentParts = queryComponent.split("=");
            if (componentParts.length > 1) {
                params.put(componentParts[0], StringUtil.urldecode(componentParts[1], null));
            }
        }
        validateRequest(params);
        ProcessCoverageRequest ret = new ProcessCoverageRequest(
            params.get(QUERY_KEY),
            params.get(VERSION_KEY),
            parseExtraParameters(params)
        );
        return ret;
    }

    /**
     * Returns the operation name that this parser is targeting
     *
     * @return the operation name
     */
    @Override
    public String getOperationName() {
        return RequestHandler.PROCESS_COVERAGE;
    }

    /**
     * Validates the http request, checking if some mandatory parameters exist
     *
     * @param params a key-value pair collection of the url query parameters.
     * @throws WCSException
     */
    private void validateRequest(Map<String, String> params) throws WCSException {
        if (!params.containsKey("query")) {
            throw new WCSException(ExceptionCode.WCSPMissingQueryParameter);
        }
    }

    /**
     * Parse any extra parameters. The standard specifies that each extra parameter is a numeric string corresponding
     * to its equivalent in the query string.
     *
     * @param queryParts the query parts of the url
     * @return
     */
    private Map<Integer, String> parseExtraParameters(Map<String, String> queryParts) {
        Map<Integer, String> extraParams = new HashMap<Integer, String>();
        Set<String> keys = queryParts.keySet();
        for (String key : keys) {
            if (key.matches("\\d")) {
                extraParams.put(Integer.valueOf(key), queryParts.get(key));
            }
        }
        return extraParams;
    }

    public static final String QUERY_KEY = "query";
    public static final String VERSION_KEY = "wcpsVersion";
}
