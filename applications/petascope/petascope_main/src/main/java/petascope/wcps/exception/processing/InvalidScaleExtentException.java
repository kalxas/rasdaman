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
 * Error exception when scaling extent is not trimming interval (e.g:
 * scaleextent=Lat(0))
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 *
 */
public class InvalidScaleExtentException extends WCPSException {

    /**
     * Constructor for the class
     *
     * @param axisName
     * @param bound
     */
    public InvalidScaleExtentException(String axisName, String bound) {
        super(ExceptionCode.WcpsError, ERROR_TEMPLATE.replace("$axisName", axisName).replace("$bound", bound));
    }

    private static final String ERROR_TEMPLATE = "Scale extent domain must be interval (lowerBound:upperBound) for axis: $axisName, given: ($bound).";
}
