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

import org.jetbrains.annotations.NotNull;

/**
 * General WMS Exception class conformant to the standard. Any compliant exception should subclass
 * this exception class.
 * Each extending class should provide its own exception code according to the standard.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public abstract class WMSException extends Exception {

    /**
     * Constructor for the class
     *
     * @param errorMessage a user friendly error message
     */
    public WMSException(String errorMessage) {
        super(errorMessage);
    }
    
    public WMSException(String errorMessage, Exception ex) {
        super(errorMessage, ex);
    }

    /**
     * Returns the exception code
     *
     * @return the exception code associated with this exception
     */
    @NotNull
    public abstract String getExceptionCode();
}
