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
import petascope.exceptions.WCSException;
import petascope.util.ListUtil;
import petascope.wcs2.helpers.rest.RESTUrl;

/**
 * Implementation of the RESTParser for the GetCapabilities operation in REST
 * syntax: /wcs/:version/capabilities
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */
public class RESTGetCapabilitiesParser extends RESTParser<GetCapabilitiesRequest> {

    /**
     * Parses an HTTPRequest into a GetCapabilities request
     * @param request the http request
     * @return the get coverage request
     * @throws WCSException 
     */
    public GetCapabilitiesRequest parse(HTTPRequest request) throws WCSException {
        RESTUrl rUrl = new RESTUrl(request.getUrlPath());
        return new GetCapabilitiesRequest(
                ListUtil.head(rUrl.getByKey("acceptversions")),
                ListUtil.head(rUrl.getByKey("acceptformats")),
                ListUtil.head(rUrl.getByKey("acceptlanguages")));
    }

    public String getOperationName() {
        return RESTGetCapabilitiesParser.REST_IDENTIFIER;
    }
    private static final String REST_IDENTIFIER = "capabilities";
}
