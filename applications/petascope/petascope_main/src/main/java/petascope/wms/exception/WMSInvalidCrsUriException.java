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
 * Exception class for invalid crs uris to transform in WMS (current it must be EPSG)
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class WMSInvalidCrsUriException extends WMSException {
    /**
     * Constructor for the class.
     *
     * @param crs the invalid crs.
     */
    public WMSInvalidCrsUriException(@NotNull String crs) {        
        super(ExceptionCode.NoApplicableCode, ERROR_MESSAGE.replace(CRS_TOKEN, crs));
    }

    private static final String CRS_TOKEN = "$crs$";
    private static final String ERROR_MESSAGE = "The supplied crs uri '" + CRS_TOKEN + "' is invalid, current only supports EPSG code.";
}
