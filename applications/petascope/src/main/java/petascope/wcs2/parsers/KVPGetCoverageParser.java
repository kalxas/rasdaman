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
import static petascope.util.KVPSymbols.*;
import petascope.util.ListUtil;
import petascope.util.StringUtil;
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

    @Override
    public GetCoverageRequest parse(HTTPRequest request) throws WCSException {
        String input = request.getRequestString();
        Map<String, List<String>> p = StringUtil.parseQuery(input);
        checkEncodingSyntax(p,
                KEY_COVERAGEID, KEY_VERSION, KEY_MEDIATYPE, KEY_FORMAT,
                KEY_SCALEFACTOR, KEY_SCALEAXES, KEY_SCALESIZE, KEY_SCALEEXTENT,
                KEY_RANGESUBSET, KEY_INTERPOLATION);
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
