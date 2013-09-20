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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.parsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.TimeUtil;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.extensions.RangeSubsettingExtension;
import petascope.wcs2.helpers.rest.RESTUrl;

/**
 * Implementation of the RESTParser for the GetCapabilities operation in REST
 * syntax: /wcs/:version/coverage/:id/
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */
public class RESTGetCoverageParser extends RESTParser<GetCoverageRequest> {

    /**
     * Overrides the parent canParse method to better identify GetCoverage requests
     * @param request the http request
     * @return true if possible, false otherwise
     */
    @Override
    public boolean canParse(HTTPRequest request) {
        RESTUrl rUrl = new RESTUrl(request.getUrlPath());
        Boolean canParse = true;
        if (!(request.getQueryString() == null || request.getQueryString().isEmpty())) {
            canParse = false;
        } else if (!rUrl.existsKey(this.getOperationName())) {
            canParse = false;
        } else if (rUrl.existsKey(RESTDescribeCoverageParser.OPERATION_IDENTIFIER)) {
            canParse = false;
        }
        return canParse;
    }

    /**
     * Parses all the subsets and add them to the coverage request
     *
     * @param rUrl - A RESTUrl object from which subsets can be extracted
     * @param ret - the GetCoverageRequest
     * @throws WCSException
     */
    public void parseSubsets(RESTUrl rUrl, GetCoverageRequest ret) throws WCSException {
        ArrayList<String> subsets = rUrl.getByKey(REST_SUBSET_PARAM);
        for (String subsetValue : subsets) {
            Matcher matcher = SUBSET_REGEX.matcher(subsetValue);
            if (matcher.find()) {
                String dim = matcher.group(1);
                String crs = matcher.group(3);
                String low = matcher.group(4);
                String high = matcher.group(6);
                if (high == null) {
                    ret.getSubsets().add(new GetCoverageRequest.DimensionSlice(dim, crs, low));
                } else if (dim != null) {
                    ret.getSubsets().add(new GetCoverageRequest.DimensionTrim(dim, crs, low, high));
                } else {
                    throw new WCSException(ExceptionCode.InvalidEncodingSyntax);
                }

                // Check time-subset validity (YYYY-MM-DD)
                if (dim.equals(AxisTypes.T_AXIS)) {
                    if (low != null && !TimeUtil.isValidTimestamp(low)) {
                        throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + low + "\" is not valid (pattern is YYYY-MM-DD).");
                    }
                    if (high != null && !TimeUtil.isValidTimestamp(high)) {
                        throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + high + "\" is not valid (pattern is YYYY-MM-DD).");
                    }
                    // Check low<high
                    if (low != null && high != null && !TimeUtil.isOrderedTimeSubset(low, high)) {
                        throw new WCSException(ExceptionCode.InvalidParameterValue, "Temporal subset \"" + low + ":" + high + "\" is invalid: check order.");
                    }
                }
            } else {
                throw new WCSException(ExceptionCode.InvalidEncodingSyntax);
            }
        }
    }

    /**
     * Parses the HTTPRequest into a GetCoverage request
     *
     * @param request
     * @return the GetCoverage request
     * @throws WCSException
     */
    public GetCoverageRequest parse(HTTPRequest request) throws WCSException {
        RESTUrl rUrl = new RESTUrl(request.getUrlPath());
        List<String> coverageIds = new ArrayList<String>(Arrays.asList(
                rUrl.getByIndex(RESTGetCoverageParser.COVERAGE_ID_PLACE).fst.split(",")));
        if (coverageIds.size() != 1) {
            throw new WCSException(ExceptionCode.InvalidRequest,
                    "A GetCoverage request can specify only one CoverageId");
        }
        String mediaType = ListUtil.head(rUrl.getByKey("mediatype"));
        String format = ListUtil.head(rUrl.getByKey("format"));
        if (FormatExtension.MIME_MULTIPART.equals(mediaType) && FormatExtension.MIME_GML.equals(format)) {
            throw new WCSException(ExceptionCode.InvalidRequest,
                    "The 'MEDIATYPE=multipart/mixed & FORMAT=application/gml+xml' combination is not applicable");
        }

        GetCoverageRequest ret = new GetCoverageRequest(coverageIds.get(0), format,
                FormatExtension.MIME_MULTIPART.equals(mediaType));

        /**
         * Let the extensions do their job
         *
         * @todo We should not implement the extension parsing in the protocol
         * binding it should be the other way around, see
         * RangeSubsettingExtension as an example
         */
        this.parseSubsets(rUrl, ret);
        RangeSubsettingExtension.parseGetCoverageRESTRequest(rUrl, ret);

        return ret;
    }

    public String getOperationName() {
        return RESTGetCoverageParser.OPERATION_IDENTIFIER;
    }
    private static final String OPERATION_IDENTIFIER = "coverage";
    private static final int COVERAGE_ID_PLACE = 3;
    private static final Pattern SUBSET_REGEX = Pattern.compile("([^,\\(]+)(,([^\\(]+))?\\(([^"
            + RESTParser.RANGE_SEPARATOR + "\\)]+)(" + RESTParser.RANGE_SEPARATOR + "([^\\)]+))?\\)");
    private static final String REST_SUBSET_PARAM = "subset";
}
