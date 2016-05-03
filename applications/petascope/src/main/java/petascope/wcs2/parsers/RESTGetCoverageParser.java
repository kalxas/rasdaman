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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.wcs2.extensions.CRSExtension;
import petascope.util.ListUtil;
import petascope.util.TimeUtil;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.extensions.RangeSubsettingExtension;
import petascope.wcs2.helpers.rest.RESTUrl;
import petascope.wcs2.parsers.subsets.DimensionSlice;
import petascope.wcs2.parsers.subsets.DimensionTrim;
import static petascope.wcs2.parsers.subsets.DimensionSubset.QUOTED_SUBSET;

/**
 * Implementation of the RESTParser for the GetCapabilities operation in REST
 * syntax: /wcs/:version/coverage/:id/
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */
public class RESTGetCoverageParser extends RESTParser<GetCoverageRequest> {

    private static final String OPERATION_IDENTIFIER = "coverage";
    private static final int COVERAGE_ID_PLACE = 3;
    private static final Pattern SUBSET_REGEX = Pattern.compile("([^,\\(]+)(,([^\\(]+))?\\(([^"
            + RESTParser.RANGE_SEPARATOR + "\\)]+)(" + RESTParser.RANGE_SEPARATOR + "([^\\)]+))?\\)");
    private static final String REST_SUBSET_PARAM = "subset";
    private static Logger log = LoggerFactory.getLogger(RESTGetCoverageParser.class);

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
        log.trace("RESTGetCoverageParser {} parse the request", canParse ? "can" : "cannot");
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
        Set<String> subsetsDims = new HashSet<String>();
        for (String subsetValue : subsets) {
            Matcher matcher = SUBSET_REGEX.matcher(subsetValue);
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
                    ret.addSubset(new DimensionSlice(dim, crs, low));
                    if (null != low && low.matches(QUOTED_SUBSET)) {
                        ((DimensionSlice)ret.getSubset(dim)).timestampSubsetCheck();
                    }
                } else if (dim != null) {
                    ret.addSubset(new DimensionTrim(dim, crs, low, high));
                    if (null != low && (low.matches(QUOTED_SUBSET) || high.matches(QUOTED_SUBSET))) {
                        ((DimensionTrim)ret.getSubset(dim)).timestampSubsetCheck();
                    }
                } else {
                    throw new WCSException(ExceptionCode.InvalidEncodingSyntax);
                }

                // Check time-subset validity
                if (dim.equals(AxisTypes.T_AXIS)) {
                    if (low != null && low.matches(QUOTED_SUBSET) && !TimeUtil.isValidTimestamp(low)) {
                        throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + low + "\" is not valid.");
                    }
                    if (high != null && high.matches(QUOTED_SUBSET) && !TimeUtil.isValidTimestamp(high)) {
                        throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + high + "\" is not valid.");
                    }
                    // Check low<high
                    try {
                        if (low != null && high != null && low.matches(QUOTED_SUBSET) && high.matches(QUOTED_SUBSET)
                                && !TimeUtil.isOrderedTimeSubset(low, high)) {
                            throw new WCSException(ExceptionCode.InvalidParameterValue, "Temporal subset \"" + low + ":" + high + "\" is invalid: check order.");
                        }
                    } catch (PetascopeException ex) {
                        throw new WCSException(ex.getExceptionCode(), ex);
                    }
                }
            } else {
                throw new WCSException(ExceptionCode.InvalidEncodingSyntax);
            }
        }
    }

    /**
     * Parses the parameters needed for the CRS extension
     *
     * @param rUrl the REST url
     * @param ret the GetCoverage request
     * @throws WCSException
     */
    public void parseCRS(RESTUrl rUrl, GetCoverageRequest ret) throws WCSException {
        /* CRS-extension parameters: */
        // subsettingCrs
        String subCrs = ListUtil.head(rUrl.getByKey(CRSExtension.REST_SUBSETTING_PARAM));
        if (!(subCrs == null) && !CrsUtil.CrsUri.isValid(subCrs)) {
            throw new WCSException(ExceptionCode.NotASubsettingCrs, "subsettingCrs " + subCrs + " is not valid.");
        }
        if (!(subCrs == null) && !CrsUtil.isSupportedCrsCode(subCrs)) {
            throw new WCSException(ExceptionCode.SubsettingCrsNotSupported, "subsettingCrs " + subCrs + " is not supported.");
        }
        // outputCrs

        String outCrs = ListUtil.head(rUrl.getByKey(CRSExtension.REST_OUTPUT_PARAM));
        if (!(outCrs == null) && !CrsUtil.CrsUri.isValid(outCrs)) {
            throw new WCSException(ExceptionCode.NotAnOutputCrs, "outputCrs " + outCrs + " is not valid.");
        }
        if (!(outCrs == null) && !CrsUtil.isSupportedCrsCode(outCrs)) {
            throw new WCSException(ExceptionCode.SubsettingCrsNotSupported, "outputCrs " + outCrs + " is not supported.");
        }
        if (!(subCrs == null) || !(outCrs == null)) {
            ret.getCrsExt().setSubsettingCrs(subCrs);
            ret.getCrsExt().setOutputCrs(outCrs);
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
        // Test /conf/core/getCoverage-acceptable-mediaType
        if (mediaType != null && !mediaType.equals(FormatExtension.MIME_MULTIPART)) {
            throw new WCSException(ExceptionCode.InvalidMediatype);
        }
        String format = ListUtil.head(rUrl.getByKey("format"));
        if (FormatExtension.MIME_MULTIPART.equals(mediaType) && FormatExtension.MIME_GML.equals(format)) {
            throw new WCSException(ExceptionCode.InvalidRequest,
                    "The 'MEDIATYPE=multipart/related & FORMAT=application/gml+xml' combination is not applicable");
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

        this.parseCRS(rUrl, ret);

        this.parseSubsets(rUrl, ret);
        RangeSubsettingExtension.parseGetCoverageRESTRequest(rUrl, ret);

        return ret;
    }

    public String getOperationName() {
        return RESTGetCoverageParser.OPERATION_IDENTIFIER;
    }
}
