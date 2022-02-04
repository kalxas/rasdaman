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

package petascope.exceptions;


/**
 * General WMS Exception class conformant to the standard. Any compliant exception should subclass
 * this exception class.
 * Each extending class should provide its own exception code according to the standard.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class WMSException extends PetascopeException {

    /**
     * Constructor for the class
     *
     * @param exceptionText a user friendly error message
     */
    public WMSException(String exceptionText) {
        super(ExceptionCode.WmsError, exceptionText);
    }
    
    public WMSException(String errorMessage, PetascopeException ex) {
        super(ex.getExceptionCode(), errorMessage, ex);
    }
    
    public WMSException(ExceptionCode exceptionCode, String exceptionText) {
        super(exceptionCode, exceptionText, null);
    }
    
    public WMSException(ExceptionCode exceptionCode, String exceptionText, Exception ex) {
        super(exceptionCode, exceptionText, ex);
    }
}
