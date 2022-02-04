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
 * Exception that is thrown when nodata parameters has more values than the coverage's range fields.
 * if nodata = array of values then each value is applied to each band separately
 * 
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class InvalidNumberOfNodataValuesException extends WCPSException {

    /**
     * Constructor for the class
     */
    public InvalidNumberOfNodataValuesException(int numberOfRange, int numberOfNodata) {
        super(ExceptionCode.InvalidRequest, ERROR_TEMPLATE.replace("$numberOfRange", String.valueOf(numberOfRange))
                            .replace("$numberOfNodata", String.valueOf(numberOfNodata)));
    }

    public static final String ERROR_TEMPLATE = "The number of coverage's range fields does not match the number of nodata values, "
                                              + "expected: $numberOfRange values, received: $numberOfNodata values.";

}
