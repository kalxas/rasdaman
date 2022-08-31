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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;

/**
 * General error for invalid trimming on irregular axis
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class IrregularAxisTrimmingCoefficientNotFoundException extends WCPSException {

    public IrregularAxisTrimmingCoefficientNotFoundException(String axisName, String lowerBound, String upperBound) {
        super(ExceptionCode.InvalidSubsetting, SLICING_ERROR_TEMPLATE.replace("$lowerBound", lowerBound)
                                                                     .replace("$upperBound", upperBound)
                                                                     .replace("$axis", axisName));
    }

    private static final String SLICING_ERROR_TEMPLATE = "Subsetting: $lowerBound:$upperBound does not contain any coefficients of irregular axis: $axis.";

}
