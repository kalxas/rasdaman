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

package petascope.wms2.service.getmap;

import petascope.wms2.service.base.Response;
import petascope.wms2.service.exception.error.WMSException;

/**
 * A representation of a get map response
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class GetMapResponse extends Response {

    /**
     * Constructor for the class
     *
     * @param mimeType the mimetype of the result
     * @param result   the result encoded as a byte array
     */
    public GetMapResponse(String mimeType, byte[] result) {
        this.mimeType = mimeType;
        this.result = result;
    }

    /**
     * Returns the byte array that represents the result
     *
     * @return a byte array representing the result
     * @throws WMSException
     */
    @Override
    public byte[] toBytes() throws WMSException {
        return result;
    }

    /**
     * Returns the mime type of the result
     *
     * @return the mime type of the result
     */
    @Override
    public String getMimeType() {
        return mimeType;
    }

    private final String mimeType;
    private final byte[] result;
}
