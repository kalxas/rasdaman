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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

package petascope.wms.exception;

import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WMSException;

/**
 * Exception to be thrown when the client update sequence larger than the server update sequence
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class WMSInvalidUpdateSequenceException extends WMSException {

    /**
     * Constructor for the class
     *
     * @param clientUpdateSequence the client update sequence
     * @param serverUpdateSequence the server update sequence
     */
    public WMSInvalidUpdateSequenceException(long clientUpdateSequence, long serverUpdateSequence) {
        super(ExceptionCode.InvalidRequest, ERROR_MESSAGE.replace("$ClientUpdateSequence", String.valueOf(clientUpdateSequence))
              .replace("$ServerUpdateSequence", String.valueOf(serverUpdateSequence)));
    }

    private static final String ERROR_MESSAGE = "The update sequence on the server is higher than the one provided '$ClientUpdateSequence'. The current update sequence is '$ServerUpdateSequence'";
}
