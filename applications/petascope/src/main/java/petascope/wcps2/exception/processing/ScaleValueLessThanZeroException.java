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
package petascope.wcps2.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;

/**
 * Error message for not scaling factor is less than zero (eg.: scalesize(c, [Lat(0), Long(-1)]) or scalefactor(c, 0);
 *
 * @author <a href="mailto:bphamhuu@jacbos-university.de">Bang Pham Huu</a>
 */
public class ScaleValueLessThanZeroException extends WCPSException {
    /**
     * Constructor for the class
     *
     * @param scaleValue
     */
    public ScaleValueLessThanZeroException(String axisName, String scaleValue) {
        super(ERROR_TEMPLATE.replace("$axisName", axisName).replace("$scaleValue", scaleValue), ExceptionCode.WcpsError);
    }

    private static final String ERROR_TEMPLATE = "Scaling value for axis: $axisName must be > 0, given: $scaleValue.";
}