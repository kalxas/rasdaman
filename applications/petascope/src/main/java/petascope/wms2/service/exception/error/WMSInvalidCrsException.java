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

package petascope.wms2.service.exception.error;

import org.jetbrains.annotations.NotNull;

/**
 * Exception that is thrown when an invalid crs is requested
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class WMSInvalidCrsException extends WMSException {

    /**
     * Constructor for the class if parameter is null
     */
    public WMSInvalidCrsException() {
        super("No crs parameter requested.");
    }

    /**
     * Constructor for the class
     *
     * @param invalidCrs the invalid crs requested
     */
    public WMSInvalidCrsException(@NotNull String invalidCrs) {
        super(ERROR_MESSAGE.replace("$CRS", invalidCrs));
    }

    @NotNull
    @Override
    public String getExceptionCode() {
        return EXCEPTION_CODE;
    }

    private static final String EXCEPTION_CODE = "InvalidCRS";
    private static final String ERROR_MESSAGE = "The request contains a CRS $CRS not offered by the server for one or more of the Layers in the request.";
}
