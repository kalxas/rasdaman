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
package petascope.wcps.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;

/**
 * Exception for errors regarding coverage metadata
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageMetadataException extends WCPSException {

    /**
     * Constructor for the class
     *
     * @param originalCause the exception that caused the error
     */
    public CoverageMetadataException(Exception originalCause) {
        super(ExceptionCode.InvalidRequest, ERROR_TEMPLATE.replace("$metadataError", originalCause.getMessage()), originalCause);
    }

    /**
     * Constructor for the class when subclass send the appropriate exception
     * message
     *
     * @param originalCause the exception that caused the error
     * @param errorMessage the error message from subclass
     */
    public CoverageMetadataException(Exception originalCause, String errorMessage) {
        super(ExceptionCode.InvalidRequest, errorMessage);
    }

    private static final String ERROR_TEMPLATE = "Error in processing coverage metadata '$metadataError'.";
}
