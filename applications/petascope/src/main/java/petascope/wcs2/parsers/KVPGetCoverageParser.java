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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import static petascope.util.KVPSymbols.*;
import petascope.util.ListUtil;
import petascope.util.Pair;
import petascope.util.StringUtil;
import petascope.util.TimeUtil;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.extensions.RangeSubsettingExtension;
import petascope.wcs2.handlers.RequestHandler;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionSlice;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionTrim;

/**
 * Parse a GetCapabilities KVP request.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class KVPGetCoverageParser extends KVPParser<GetCoverageRequest> {

    private static final Pattern PATTERN = Pattern.compile("([^,\\(]+)(,([^\\(]+))?\\(([^,\\)]+)(,([^\\)]+))?\\)");
    private static final String QUOTED_SUBSET = "^\".*\"$"; // switch from numeric to ISO8601 coordinates for time

    /**
     * Parses any subset parameters defined as in OGC 09-147r1 standard(e.g.
     * ...&subset=x(200,300)&subset=y(300,200)) and for backwards compatibility
     * subsets defined as subsetD(where D is any distinct string)(e.g.
     * &subsetA=x(200,300))
     *
     * @param requestParams - the request parameters as a string
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
                else if (key.toLowerCase().startsWith(KEY_SUBSET)
                        && !key.equalsIgnoreCase(KEY_SUBSETCRS)) {
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
        checkEncodingSyntax(p, KEY_COVERAGEID, KEY_VERSION, KEY_MEDIATYPE, KEY_FORMAT, KEY_SUBSETCRS, KEY_OUTPUTCRS,
                KEY_SCALEFACTOR, KEY_SCALEAXES, KEY_SCALESIZE, KEY_SCALEEXTENT, KEY_RANGESUBSET);
        List<String> coverageIds = p.get(KEY_COVERAGEID);
        if (coverageIds.size() != 1) {
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

        GetCoverageRequest ret = new GetCoverageRequest(coverageIds.get(0), format,
                FormatExtension.MIME_MULTIPART.equals(mediaType));

        //Parse rangeSubset parameters if any for the RangeSubset Extension
        RangeSubsettingExtension.parseGetCoverageKVPRequest(p, ret);

        /* CrsExt-extension parameters: */
        // subsettingCrs
        String subCrs=null, outCrs=null;
        List<String> list = p.get(KEY_SUBSETCRS);
        if (list != null && list.size() > 1) {
            throw new WCSException(ExceptionCode.InvalidRequest,
                    "Multiple \"" + KEY_SUBSETCRS + "\" parameters in the request: must be unique.");
        }
        else {
            subCrs = ListUtil.head(list);
            if (!(subCrs == null) && !CrsUtil.CrsUri.isValid(subCrs)) {
                throw new WCSException(ExceptionCode.NotASubsettingCrs,
                        KEY_SUBSETCRS + " " + subCrs + " is not valid.");
            }
            if (!(subCrs == null) && !CrsUtil.isSupportedCrsCode(subCrs)) {
                throw new WCSException(ExceptionCode.SubsettingCrsNotSupported,
                        KEY_SUBSETCRS + " " + subCrs + " is not supported.");
            }
        }
        // outputCrs
        list = p.get(KEY_OUTPUTCRS);
        if (list != null && list.size() > 1) {
            throw new WCSException(ExceptionCode.InvalidRequest,
                    "Multiple \"" + KEY_OUTPUTCRS + "\" parameters in the request: must be unique.");
        }
        else {
            outCrs = ListUtil.head(list);
            if (!(outCrs == null) && !CrsUtil.CrsUri.isValid(outCrs)) {
                throw new WCSException(ExceptionCode.NotAnOutputCrs,
                        KEY_OUTPUTCRS + " " + outCrs + " is not valid.");
            }
            if (!(outCrs == null) && !CrsUtil.isSupportedCrsCode(outCrs)) {
                throw new WCSException(ExceptionCode.SubsettingCrsNotSupported,
                        KEY_OUTPUTCRS + " " + outCrs + " is not supported.");
            }
        }
        if (!(subCrs==null) || !(outCrs==null)) {
            ret.getCrsExt().setSubsettingCrs(subCrs);
            ret.getCrsExt().setOutputCrs(outCrs);
        }

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
                    ret.getSubsets().add(new DimensionSlice(dim, crs, low));
                } else if (dim != null) {
                    ret.getSubsets().add(new DimensionTrim(dim, crs, low, high));
                } else {
                    throw new WCSException(ExceptionCode.InvalidEncodingSyntax.locator(subsetKey));
                }

                // Check time-subset validity (currently, what Date4J can accept)
                if (null != low && low.matches(QUOTED_SUBSET)) {
                    if (low != null && !TimeUtil.isValidTimestamp(low)) {
                        throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + low + "\" is not valid or supported.");
                    }
                }
                if (null != high && high.matches(QUOTED_SUBSET)) {
                    if (high != null && !TimeUtil.isValidTimestamp(high)) {
                        throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + high + "\" is not valid or supported.");
                    }
                }
                if (null != low && null != high && low.matches(QUOTED_SUBSET) && high.matches(QUOTED_SUBSET)) {
                    // Check low<high
                    if (low != null && high != null && !TimeUtil.isOrderedTimeSubset(low, high)) {
                        throw new WCSException(ExceptionCode.InvalidParameterValue, "Temporal subset \"" + low + ":" + high + "\" is invalid: check order.");
                    }
                }
            } else {
                throw new WCSException(ExceptionCode.InvalidEncodingSyntax.locator(subsetKey));
            }
        }

        // get scaling options
        list = p.get(KEY_SCALEFACTOR);
        if (list != null && list.size() != 1 || ret.isScaled()) {
            throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
        } else if (list != null) {
            float scaleFactor;
            try {
                scaleFactor = Float.parseFloat(ListUtil.head(list));
            } catch (NumberFormatException e) {
                throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(ListUtil.head(list)));
            }
            if (scaleFactor <= 0) {
                throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(ListUtil.head(list)));
            }
            ret.getScaling().setFactor(scaleFactor);
            ret.getScaling().setType(1);
        }

        list = p.get(KEY_SCALEAXES);
        if (list != null && ret.isScaled()) {
            throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
        } else if (list != null) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String keyvalue = it.next();
                String fact = "", axis = "";
                int splitPos = keyvalue.indexOf("(");
                if (splitPos != -1) {
                    axis = keyvalue.substring(0, splitPos);
                    fact = keyvalue.substring(splitPos + 1, keyvalue.length()-1);
                } else
                    throw new WCSException(ExceptionCode.InvalidRequest, "Wrong scaling parameter format: must be axis(factor).");
                if (ret.getScaling().isPresentFactor(axis))
                    throw new WCSException(ExceptionCode.InvalidRequest, "Axis name repeated in the scaling request: must be unique.");
                float scaleFactor;
                try {
                    scaleFactor = Float.parseFloat(fact);
                } catch (NumberFormatException e) {
                    throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(fact));
                }
                if (scaleFactor <= 0) {
                    throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(fact));
                }
                ret.getScaling().addFactor(axis, scaleFactor);
            }
            ret.getScaling().setType(2);
        }

        list = p.get(KEY_SCALESIZE);
        if (list != null && ret.isScaled()) {
            throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
        } else if (list != null) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String keyvalue = it.next();
                String fact = "", axis = "";
                int splitPos = keyvalue.indexOf("(");
                if (splitPos != -1) {
                    axis = keyvalue.substring(0, splitPos);
                    fact = keyvalue.substring(splitPos + 1, keyvalue.length()-1);
                } else
                    throw new WCSException(ExceptionCode.InvalidRequest, "Wrong scaling parameter format: must be axis(size).");
                if (ret.getScaling().isPresentSize(axis))
                    throw new WCSException(ExceptionCode.InvalidRequest, "Axis name repeated in the scaling request: must be unique.");
                long scaleSize;
                try {
                    scaleSize = Long.parseLong(fact);
                } catch (NumberFormatException e) {
                    throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(fact));
                }
                if (scaleSize < 0) {
                    throw new WCSException(ExceptionCode.InvalidRequest, "Scaling size is not positive.");
                }

                ret.getScaling().addSize(axis, scaleSize);
            }
            ret.getScaling().setType(3);
        }
        list = p.get(KEY_SCALEEXTENT);
        if (list != null && ret.isScaled()) {
            throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
        } else if (list != null) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String keyvalue = it.next();
                String fact = "", axis = "";
                int splitPos = keyvalue.indexOf("(");
                if (splitPos != -1) {
                    axis = keyvalue.substring(0, splitPos);
                    fact = keyvalue.substring(splitPos + 1, keyvalue.length()-1);
                } else
                    throw new WCSException(ExceptionCode.InvalidRequest, "Wrong scaling parameter format: must be axis(lo:hi).");
                if (ret.getScaling().isPresentExtent(axis))
                    throw new WCSException(ExceptionCode.InvalidRequest, "Axis name repeated in the scaling request: must be unique.");
                String shi = "", slo = "";
                StringTokenizer st = new StringTokenizer(fact, ":");
                if (st.countTokens() != 2)
                    throw new WCSException(ExceptionCode.InvalidRequest, "Wrong format for scaling parameters: must be 'lo:hi'.");
                slo = st.nextToken();
                shi = st.nextToken();
                long hi, lo;
                try {
                    lo = Long.parseLong(slo);
                } catch (NumberFormatException e) {
                    throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(slo));
                }
                try {
                    hi = Long.parseLong(shi);
                } catch (NumberFormatException e) {
                    throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(shi));
                }
                if (ret.getScaling().isPresentExtent(axis)) {
                    throw new WCSException(ExceptionCode.InvalidRequest, "Axis name repeated in the scaling request: must be unique.");
                }
                if (hi < lo) {
                    throw new WCSException(ExceptionCode.InvalidExtent.locator(shi));
                }

                ret.getScaling().addExtent(axis, new Pair(lo, hi));
            }
            ret.getScaling().setType(4);
        }

        return ret;
    }

    @Override
    public String getOperationName() {
        return RequestHandler.GET_COVERAGE;
    }
}
