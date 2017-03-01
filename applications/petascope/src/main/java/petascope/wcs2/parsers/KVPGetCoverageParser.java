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
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.CrsUtil;
import static petascope.util.KVPSymbols.*;
import petascope.util.ListUtil;
import petascope.util.StringUtil;
import petascope.util.XMLSymbols;
import static petascope.util.XMLSymbols.LABEL_FORMAT;
import static petascope.util.XMLSymbols.LABEL_MEDIATYPE;
import petascope.wcs2.extensions.ExtensionsRegistry;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.extensions.InterpolationExtension;
import petascope.wcs2.extensions.RangeSubsettingExtension;
import petascope.wcs2.extensions.ScalingExtension;
import petascope.wcs2.handlers.RequestHandler;
import petascope.wcs2.parsers.subsets.DimensionSubset;
import petascope.wcs2.parsers.subsets.SubsetParser;

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
        checkEncodingSyntax(p,
                            KEY_COVERAGEID, KEY_VERSION, KEY_MEDIATYPE, KEY_FORMAT,
                            KEY_SUBSETCRS, KEY_OUTPUTCRS, KEY_RANGESUBSET,
                            KEY_SCALEFACTOR, KEY_SCALEAXES, KEY_SCALESIZE, KEY_SCALEEXTENT,
                            KEY_RANGESUBSET, KEY_INTERPOLATION);
        List<String> coverageIds = p.get(KEY_COVERAGEID); // null if no key
        if (null == coverageIds || coverageIds.size() != 1) {
            throw new WCSException(ExceptionCode.InvalidRequest,
                                   "A GetCoverage request can specify only one " + XMLSymbols.LABEL_COVERAGE_ID + ".");
        }
        String mediaType = ListUtil.head(p.get(KEY_MEDIATYPE));
        // Test /conf/core/getCoverage-acceptable-mediaType
        if (mediaType != null && !mediaType.equals(FormatExtension.MIME_MULTIPART)) {
            throw new WCSException(ExceptionCode.InvalidMediatype);
        }
        String format = ListUtil.head(p.get(KEY_FORMAT));
        if (StringUtils.isEmpty(format)) {
            format = FormatExtension.MIME_GML;
        }

        String encodeType = ExtensionsRegistry.mimeToIdentifier.get(format);

        // e.g: format=application/xml is not valid request
        if (encodeType == null) {
            // e.g: format=image/NotSupport
            throw new WCSException(ExceptionCode.InvalidRequest, "WCS does not support this Mime type: " + format);
        }

        /*
        OGC CITE test it is valid
        // format=application/gml+xml&mediatype=multipart/related is not valid request
        if (FormatExtension.MIME_MULTIPART.equals(mediaType)
                && FormatExtension.MIME_GML.equals(format)) {
            throw new WCSException(ExceptionCode.InvalidRequest, "The '" +
                    LABEL_MEDIATYPE + "=" + FormatExtension.MIME_MULTIPART + "' & '" +
                    LABEL_FORMAT    + "=" + FormatExtension.MIME_GML +
                    "' combination is not applicable");
        }
        */

        // init GetCoverage request
        GetCoverageRequest ret = new GetCoverageRequest(
            coverageIds.get(0),
            format,
            FormatExtension.MIME_MULTIPART.equals(mediaType)
        );

        //Parse rangeSubset parameters if any for the RangeSubset Extension
        RangeSubsettingExtension.parseGetCoverageKVPRequest(p, ret);

        /* CrsExt-extension parameters: */
        // subsettingCrs
        String subCrs = null, outCrs = null;
        List<String> list = p.get(KEY_SUBSETCRS);
        if (list != null && list.size() > 1) {
            throw new WCSException(ExceptionCode.InvalidRequest,
                                   "Multiple \"" + KEY_SUBSETCRS + "\" parameters in the request: must be unique.");
        } else {
            subCrs = ListUtil.head(list);
            if (!(subCrs == null) && !CrsUtil.CrsUri.isValid(subCrs)) {
                throw new WCSException(ExceptionCode.SubsettingCrsNotSupported,
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
        } else {
            outCrs = ListUtil.head(list);
            if (!(outCrs == null) && !CrsUtil.CrsUri.isValid(outCrs)) {
                throw new WCSException(ExceptionCode.OutputCrsNotSupported,
                        KEY_OUTPUTCRS + " " + outCrs + " is not valid.");
            }
            if (!(outCrs == null) && !CrsUtil.isSupportedCrsCode(outCrs)) {
                throw new WCSException(ExceptionCode.SubsettingCrsNotSupported,
                                       KEY_OUTPUTCRS + " " + outCrs + " is not supported.");
            }
        }
        if (!(subCrs == null) || !(outCrs == null)) {
            ret.getCrsExt().setSubsettingCrs(subCrs);
            ret.getCrsExt().setOutputCrs(outCrs);
        }

        //Parse subsets
        List<DimensionSubset> subsets = SubsetParser.parseSubsets(input);
        ret.setSubsets(subsets);

        // Parse scaling parameters if any for the Scaling Extension
        ScalingExtension.parseGetCoverageKVPRequest(p, ret);

        // Parse interpolation parameter for the interpolation extension
        // (now mainly check that, if specified, it is Nearest-Neighbor)
        InterpolationExtension.parseGetCoverageKVPRequest(p, ret);

        return ret;
    }

    @Override
    public String getOperationName() {
        return RequestHandler.GET_COVERAGE;
    }
}
