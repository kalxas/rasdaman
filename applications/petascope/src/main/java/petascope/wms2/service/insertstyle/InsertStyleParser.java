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

package petascope.wms2.service.insertstyle;

import petascope.wms2.service.base.Parser;
import petascope.wms2.service.base.Request;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.service.exception.error.WMSInvalidLayerException;
import petascope.wms2.servlet.WMSGetRequest;

/**
 * Parser for the insert style request
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class InsertStyleParser extends Parser<InsertStyleRequest> {
    /**
     * Returns true if the request is of type insert style
     *
     * @param rawRequest the raw wms http request
     * @return true if so, false otherwise
     */
    @Override
    public boolean canParse(WMSGetRequest rawRequest) {
        String requestValue = rawRequest.getGetValueByKey(Request.getRequestParameterRequest());
        if (requestValue != null && requestValue.equals(InsertStyleRequest.getRequestParameterValue())) {
            return true;
        }
        return false;
    }

    /**
     * Parses the raw request into a InsertStyleRequest
     *
     * @param rawRequest the raw wms http request
     * @return the InsertStyleRequest
     * @throws WMSException
     */
    @Override
    public InsertStyleRequest parse(WMSGetRequest rawRequest) throws WMSException {
        String rasql = rawRequest.getGetValueByKey(InsertStyleRequest.getRequestParameterRasqlQuery());
        String name = rawRequest.getGetValueByKey(InsertStyleRequest.getRequestParameterStyleName());
        String styleAbstract = rawRequest.getGetValueByKey(InsertStyleRequest.getRequestParameterStyleAbstract());
        String layerName = rawRequest.getGetValueByKey(InsertStyleRequest.getRequestParameterStyleLayerId());
        return new InsertStyleRequest(parseBaseRequest(rawRequest), rasql, name, styleAbstract, layerName);
    }
}
