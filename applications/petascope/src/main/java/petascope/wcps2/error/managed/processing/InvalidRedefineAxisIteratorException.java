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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.error.managed.processing;

import petascope.exceptions.ExceptionCode;
import petascope.wcps2.result.parameters.SubsetDimension;

/**
 * Error when redefine axis iterator alias
 * (e.g: for c in (mr) return encode
 *       (coverage cov $px x(0:20), $px x(0:30) values c[i($px), j($px)], "png"))
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class InvalidRedefineAxisIteratorException extends WCPSProcessingError {

    /**
     * Constructor for the class default
     *
     * @param axisIteratorAlias the redefined axis iterator alias
     * @param subsetDimension the offending subsetDimension
     */
    public InvalidRedefineAxisIteratorException(String axisIteratorAlias, SubsetDimension subsetDimension) {
        super(ERROR_TEMPLATE.replace("$axisIteratorAlias", axisIteratorAlias)
                            .replace("$subsetDimensionString", subsetDimension.getStringRepresentation()), ExceptionCode.WcpsError);
    }

    private static final String ERROR_TEMPLATE = "Cannot redefine axis iterator '$axisIteratorAlias' in '$subsetDimensionString'.";
}
