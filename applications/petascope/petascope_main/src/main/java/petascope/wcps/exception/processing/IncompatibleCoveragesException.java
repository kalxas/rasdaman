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
package petascope.wcps.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;

/**
 * Exception thrown when 2 coverages cannot be combined by binary coverage expression (e.g: coverage_1 + coverage_2)
 * 
 * @author <a href="b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class IncompatibleCoveragesException extends WCPSException {

    public IncompatibleCoveragesException(String firstCovName, String secondCovName, String errorMessage) {
        super(ExceptionCode.WcpsError, ERROR_TEMPLATE.replace("$firstCov", firstCovName).replace("$secondCov", secondCovName)
              .replace("$errorMessage", errorMessage));
    }

    public static final String ERROR_TEMPLATE = "Axes of coverages '$firstCov' and '$secondCov' are not compatible. Reason: $errorMessage.";
}
