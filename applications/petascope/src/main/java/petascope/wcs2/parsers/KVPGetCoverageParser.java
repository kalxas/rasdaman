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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import static petascope.util.KVPSymbols.*;
import petascope.util.ListUtil;
import petascope.util.StringUtil;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.extensions.RangeSubsettingExtension;
import petascope.wcs2.extensions.ScalingExtension;
import petascope.wcs2.handlers.RequestHandler;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionSlice;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionTrim;
import static petascope.wcs2.parsers.GetCoverageRequest.QUOTED_SUBSET;

/**
 * Parse a GetCapabilities KVP request.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class KVPGetCoverageParser extends KVPParser<GetCoverageRequest> {

    private static final Pattern PATTERN = Pattern.compile("([^,\\(]+)(,([^\\(]+))?\\(([^,\\)]+)(,([^\\)]+))?\\)");

    /**
     * Parses any subset parameters defined as in OGC 09-147r1 standard(e.g.
     * ...&subset=x(200,300)&subset=y(300,200)) and for backwards compatibility
     * subsets defined as subsetD(where D is any distinct string)(e.g.
     * &subsetA=x(200,300))
     *
     * @param request - the request parameters as a string
     * @return ret - a hashmap containing the subsets
     */
    public HashMap<String, String> parseSubsetParams(String request) {
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

    @Override
    public GetCoverageRequest parse(HTTPRequest request) throws WCSException {
        String input = request.getRequestString();
        Map<String, List<String>> p = StringUtil.parseQuery(input);
        checkEncodingSyntax(p,
                KEY_COVERAGEID, KEY_VERSION, KEY_MEDIATYPE, KEY_FORMAT,
                KEY_SCALEFACTOR, KEY_SCALEAXES, KEY_SCALESIZE, KEY_SCALEEXTENT,
                KEY_RANGESUBSET);
        List<String> coverageIds = p.get(KEY_COVERAGEID); // null if no key
        if (null == coverageIds || coverageIds.size() != 1) {
            throw new WCSException(ExceptionCode.InvalidRequest,
                    "A GetCoverage request can specify only one " + KEY_COVERAGEID + ".");
        }
        String mediaType = ListUtil.head(p.get(KEY_MEDIATYPE));
        // Test /conf/core/getCoverage-acceptable-mediaType
        if (mediaType != null && !mediaType.equals(FormatExtension.MIME_MULTIPART)) {
            throw new WCSException(ExceptionCode.InvalidMediatype);
        }
        String format = ListUtil.head(p.get(KEY_FORMAT));

        if (FormatExtension.MIME_MULTIPART.equals(mediaType)
                && FormatExtension.MIME_GML.equals(format)) {
            throw new WCSException(ExceptionCode.InvalidRequest, "The '" +
                    KEY_MEDIATYPE + "=" + FormatExtension.MIME_MULTIPART + "' & '" +
                    KEY_FORMAT    + "=" + FormatExtension.MIME_GML +
                    "' combination is not applicable");
        }

        // init GetCoverage request
        GetCoverageRequest ret = new GetCoverageRequest(
                coverageIds.get(0),
                format,
                FormatExtension.MIME_MULTIPART.equals(mediaType)
        );

        //Parse rangeSubset parameters if any for the RangeSubset Extension
        RangeSubsettingExtension.parseGetCoverageKVPRequest(p, ret);

        HashMap<String, String> subsets = parseSubsetParams(input);
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
                    ret.addSubset(ret.new DimensionSlice(dim, crs, low));
                    if (null != low && low.matches(QUOTED_SUBSET)) {
                        ((DimensionSlice)ret.getSubset(dim)).timestampSubsetCheck();
                    }
                } else if (dim != null) {
                    ret.addSubset(ret.new DimensionTrim(dim, crs, low, high));
                    if (null != low && (low.matches(QUOTED_SUBSET) || high.matches(QUOTED_SUBSET))) {
                        ((DimensionTrim)ret.getSubset(dim)).timestampSubsetCheck();
                    }
                } else {
                    throw new WCSException(ExceptionCode.InvalidEncodingSyntax.locator(subsetKey));
                }
            } else {
                throw new WCSException(ExceptionCode.InvalidEncodingSyntax.locator(subsetKey));
            }
        }

        // Parse scaling parameters if any for the Scaling Extension
        ScalingExtension.parseGetCoverageKVPRequest(p, ret);

        return ret;
    }

    @Override
    public String getOperationName() {
        return RequestHandler.GET_COVERAGE;
    }
}
