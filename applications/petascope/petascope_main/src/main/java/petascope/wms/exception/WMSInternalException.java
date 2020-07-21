/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wms.exception;

import org.jetbrains.annotations.NotNull;
import petascope.exceptions.WMSException;

/**
 * Exception to be thrown when an internal error from Petascope, WCS, SECORE
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class WMSInternalException extends WMSException {

    /**
     * Constructor for the class
     *
     * @param errorMessage the error from the catched exception
     * @param exception
     */
    public WMSInternalException(@NotNull String errorMessage, @NotNull Exception exception) {
        super(ERROR_MESSAGE.replace("$errorMessage", errorMessage), exception);
    }

    @NotNull
    @Override
    public String getExceptionCode() {
        return EXCEPTION_CODE;
    }

    private static final String EXCEPTION_CODE = "InternalError";
    private static final String ERROR_MESSAGE = "Failed when processing WMS request: $errorMessage";
}
