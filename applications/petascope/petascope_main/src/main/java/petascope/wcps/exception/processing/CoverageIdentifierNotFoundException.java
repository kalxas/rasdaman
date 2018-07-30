
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
 * Error exception for coverage identifier lookup failure
 * <code>
 *  for c in (mr) return identifier(d) then cannot find coverage named d.
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 *
 */
public class CoverageIdentifierNotFoundException extends WCPSException {
    /**
     * Constructor for the class
     *
     * @param coverageVariableName the coverage variable name that was provided
     */
    public CoverageIdentifierNotFoundException(String coverageVariableName) {
        super(ExceptionCode.WcpsError, ERROR_TEMPLATE.replace("$coverageVariableName", coverageVariableName));
        this.coverageVariableName = coverageVariableName;
    }

    /**
     * Getter for the axis name
     * @return
     */
    public String getCoverageVariableName() {
        return coverageVariableName;
    }

    private final String coverageVariableName;
    private static final String ERROR_TEMPLATE = "Coverage identifier not found from coverage variable name '$coverageVariableName'.";
}