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

import petascope.wcps2.metadata.legacy.Coverage;

/**
 * Error that is thrown when an operation between two incompatible coverages is performed
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class IncompatibleCoverageExpressionException extends WCPSProcessingError {

    /**
     * Constructor for the class
     *
     * @param firstCov  the first incompatible coverage
     * @param secondCov the second incompatible coverage
     */
    public IncompatibleCoverageExpressionException(Coverage firstCov, Coverage secondCov) {
        super(ERROR_TEMPLATE.replace("$firstCov", firstCov.getCoverageName()).replace("$secondCov", secondCov.getCoverageName()));
    }

    public static final String ERROR_TEMPLATE = "Incompatible operation between coverages '$firstCov' and '$secondCov'.";
}
