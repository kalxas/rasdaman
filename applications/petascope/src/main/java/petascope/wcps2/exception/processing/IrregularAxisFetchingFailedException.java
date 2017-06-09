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
package petascope.wcps2.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;

/**
 * Exception that is thrown when irregular axis cannot be fetched
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class IrregularAxisFetchingFailedException extends WCPSException {

    /**
     * Constructor for the class
     *
     * @param coverageName the name of the coverage
     * @param axisName the name of the axis whose fetching failed
     * @param originalCause the original exception cause
     */
    public IrregularAxisFetchingFailedException(String coverageName, String axisName, Exception originalCause) {
        super(ERROR_TEMPLATE.replace("$axisName", axisName) + originalCause.getMessage(), ExceptionCode.WcpsError);
    }

    public IrregularAxisFetchingFailedException(Exception originalExceptionCause) {
        super(originalExceptionCause.getMessage(), ExceptionCode.WcpsError);
    }

    public static final String ERROR_TEMPLATE = "Irregular axis cannot be fetched in axis '$axisName'.";

}
