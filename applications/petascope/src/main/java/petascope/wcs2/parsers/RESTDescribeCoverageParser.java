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

import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import static petascope.util.KVPSymbols.KEY_COVERAGEID;
import petascope.wcs2.helpers.rest.RESTUrl;

/**
 * Implementation of the RESTParser for the GetCapabilities operation in REST
 * syntax: /wcs/:version/coverage/:id/description
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */
public class RESTDescribeCoverageParser extends RESTParser<DescribeCoverageRequest> {

    /**
     * Parses an HTTPRequest into a DescribeCoverage request
     *
     * @param request the http request
     * @return the DescribeCoverage request
     * @throws WCSException
     */
    public DescribeCoverageRequest parse(HTTPRequest request) throws WCSException {
        RESTUrl rUrl = new RESTUrl(request.getUrlPath());
        DescribeCoverageRequest ret = new DescribeCoverageRequest();
        ret.getCoverageIds().add(rUrl.getByIndex(RESTDescribeCoverageParser.COVERAGE_ID_PLACE).fst);
        if (null == ret.getCoverageIds() || ret.getCoverageIds().isEmpty()) {
            throw new WCSException(ExceptionCode.InvalidRequest,
                    "A DescribeCoverage request must specify at least one " + KEY_COVERAGEID + ".");
        }
        return ret;
    }

    public String getOperationName() {
        return RESTDescribeCoverageParser.OPERATION_IDENTIFIER;
    }
    public static final String OPERATION_IDENTIFIER = "description";
    private static final int COVERAGE_ID_PLACE = 3;
}
