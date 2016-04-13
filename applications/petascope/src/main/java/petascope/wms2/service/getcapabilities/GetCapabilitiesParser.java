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

package petascope.wms2.service.getcapabilities;

import petascope.wms2.service.base.Parser;
import petascope.wms2.servlet.WMSGetRequest;

/**
 * Parser for the GetCapabilities requests. It will consume a raw wms http request and transform it into
 * a typed GetCapabilities request.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class GetCapabilitiesParser extends Parser<GetCapabilitiesRequest> {

    /**
     * Returns true if the parser can parse this raw request, false otherwise
     *
     * @param rawRequest the raw wms http request
     * @return true if it can parse the request, false otherwise
     */
    @Override
    public boolean canParse(WMSGetRequest rawRequest) {
        String requestValue = rawRequest.getGetValueByKey(GetCapabilitiesRequest.getRequestParameterRequest());
        return requestValue != null &&
                requestValue.equalsIgnoreCase(GetCapabilitiesRequest.getRequestType());
    }

    /**
     * Parses the raw request into a typed request
     *
     * @param rawRequest the raw wms http request
     * @return the typed request
     */
    @Override
    public GetCapabilitiesRequest parse(WMSGetRequest rawRequest) {
        String updateSequence = rawRequest.getGetValueByKey(GetCapabilitiesRequest.getRequestParameterUpdateSequence());
        String format = rawRequest.getGetValueByKey(GetCapabilitiesRequest.getRequestParameterFormat());
        return new GetCapabilitiesRequest(parseBaseRequest(rawRequest), updateSequence, format, rawRequest.getBaseUrl());
    }
}
