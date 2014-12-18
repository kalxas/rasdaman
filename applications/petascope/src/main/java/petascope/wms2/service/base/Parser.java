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

package petascope.wms2.service.base;

import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.servlet.WMSGetRequest;

/**
 * Interface for WMS parsers. Each parser transforms a WMS raw http request into a specific wms request, e.g. a
 * GetCapabilities request.
 * Each parser has to implement two methods, one defining if this parser can parse the requests and the other
 * one containing the actual parser
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public abstract class Parser<T extends Request> {

    /**
     * Returns true if this parser can parse the raw request into a specific wms request
     *
     * @param rawRequest the raw wms http request
     * @return true if the parser can parse it, false otherwise
     */
    public abstract boolean canParse(WMSGetRequest rawRequest);

    /**
     * Parses the raw wms http request into a wms specific request
     *
     * @param rawRequest the raw wms http request
     * @return the specific wms request
     */
    public abstract T parse(WMSGetRequest rawRequest) throws WMSException;

    /**
     * Parses the raw request and returns a typed base request
     *
     * @param rawRequest the raw request to be parse
     * @return the typed request
     */
    protected static Request parseBaseRequest(WMSGetRequest rawRequest) {
        String version = rawRequest.getGetValueByKey(Request.getRequestParameterVersion());
        String service = rawRequest.getGetValueByKey(Request.getRequestParameterService());
        String request = rawRequest.getGetValueByKey(Request.getRequestParameterRequest());
        return new Request(service, version, request, rawRequest);
    }
}
