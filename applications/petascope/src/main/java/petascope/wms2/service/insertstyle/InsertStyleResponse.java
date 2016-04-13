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

import petascope.wms2.service.base.Response;
import petascope.wms2.service.exception.error.WMSException;

/**
 * Response for the insert style request. It will just return a success message if everything went ok, otherwise
 * the exception handlers will take care of the errors.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class InsertStyleResponse extends Response {

    /**
     * Constructor for the class
     */
    public InsertStyleResponse() {
    }

    /**
     * Returns the success message as an array of bytes
     *
     * @return the message
     * @throws WMSException
     */
    @Override
    public byte[] toBytes() throws WMSException {
        return SUCCESS_MESSAGE.getBytes();
    }

    private static final String SUCCESS_MESSAGE = "The style was inserted successfully";
}
