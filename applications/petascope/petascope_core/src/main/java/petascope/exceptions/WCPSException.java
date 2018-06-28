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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.exceptions;

/**
 * General exception class for the wcps package.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class WCPSException extends RuntimeException {

    public WCPSException(ExceptionCode exceptionCode, String message) {
        super(message);
        this.exceptionCode = exceptionCode;
    }
    
    public WCPSException(String message, Exception cause) {
        super(message, cause);
    }
    
    public WCPSException(ExceptionCode exceptionCode, String message, Exception cause) {
        super(message, cause);
        this.exceptionCode = exceptionCode;
    }

    public WCPSException(String message) {
        super(message);
        this.exceptionCode = ExceptionCode.WcpsError;
    }

    public ExceptionCode getExceptionCode() {
        return this.exceptionCode;
    }

    private ExceptionCode exceptionCode;
}
