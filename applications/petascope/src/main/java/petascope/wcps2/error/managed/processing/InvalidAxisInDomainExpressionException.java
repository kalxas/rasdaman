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
package petascope.wcps2.error.managed.processing;

import petascope.exceptions.ExceptionCode;

/**
 * Error exception when get the domain($coverageExpression, axisName, crs)
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 *
 */
public class InvalidAxisInDomainExpressionException extends WCPSProcessingError {
    /**
     * Constructor for the class
     *
     * @param axisName
     * @param crsUri
     */
    public InvalidAxisInDomainExpressionException(String axisName, String crsUri) {
        super(ERROR_TEMPLATE.replace("$axisName", axisName).replace("$crsUri", crsUri), ExceptionCode.WcpsError);
    }

    private static final String ERROR_TEMPLATE = "CRS: '$crsUri' does not belong to axis '$axisName'.";
}