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
import javax.servlet.http.HttpServletRequest;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.Pair;
import petascope.util.TimeUtil;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.helpers.rest.RESTUrl;

/**
 * Implementation of the RESTParser for the GetCapabilities operation in REST
 * syntax: /wcs/:version/coverage/:id/
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */
public class RESTGetCoverageParser extends RESTParser<GetCoverageRequest> {

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

        /* CRS-extension parameters: */
        // subsettingCrs
        String subCrs = ListUtil.head(rUrl.getByKey("subsettingcrs"));
        if (!(subCrs == null) && !CrsUtil.CrsUri.isValid(subCrs)) {
            throw new WCSException(ExceptionCode.NotASubsettingCrs, "subsettingCrs " + subCrs + " is not valid.");
        }
        if (!(subCrs == null) && !CrsUtil.isSupportedCrsCode(subCrs)) {
            throw new WCSException(ExceptionCode.SubsettingCrsNotSupported, "subsettingCrs " + subCrs + " is not supported.");
        }
        // outputCrs

        String outCrs = ListUtil.head(rUrl.getByKey("outputcrs"));
        if (!(outCrs == null) && !CrsUtil.CrsUri.isValid(outCrs)) {
            throw new WCSException(ExceptionCode.NotAnOutputCrs, "outputCrs " + outCrs + " is not valid.");
        }
        if (!(outCrs == null) && !CrsUtil.isSupportedCrsCode(outCrs)) {
            throw new WCSException(ExceptionCode.SubsettingCrsNotSupported, "outputCrs " + outCrs + " is not supported.");
        }
        if (!(subCrs == null) || !(outCrs == null)) {
            ret.getCRS().add(new GetCoverageRequest.CRS(subCrs, outCrs));
        }


        ArrayList<String> subsets = rUrl.getByKey("subset");
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
        return ret;
    }

    public String getOperationName() {
        return RESTGetCoverageParser.OPERATION_IDENTIFIER;
    }
    private static final String OPERATION_IDENTIFIER = "coverage";
    private static final int COVERAGE_ID_PLACE = 3;
    private static final Pattern SUBSET_REGEX = Pattern.compile("([^,\\(]+)(,([^\\(]+))?\\(([^,\\)]+)(,([^\\)]+))?\\)");
}
