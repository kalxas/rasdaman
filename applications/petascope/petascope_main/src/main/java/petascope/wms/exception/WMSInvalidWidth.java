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

import petascope.exceptions.WMSException;
import org.jetbrains.annotations.NotNull;
import petascope.exceptions.ExceptionCode;

/**
 * Exception to be thrown when an invalid width
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class WMSInvalidWidth extends WMSException {

    /**
     * Constructor for the class if parameter is null
     */
    public WMSInvalidWidth() {
        super(ExceptionCode.InvalidRequest, "No width parameter requested.");
    }

    /**
     * Constructor for the class
     *
     * @param width the invalid width
     */
    public WMSInvalidWidth(@NotNull String width) {
        super(ExceptionCode.InvalidRequest, ERROR_MESSAGE.replace("$width", width));
    }

    private static final String ERROR_MESSAGE = "Invalid value for 'width' parameter, must be positive integer. Given '$width'.";
}
