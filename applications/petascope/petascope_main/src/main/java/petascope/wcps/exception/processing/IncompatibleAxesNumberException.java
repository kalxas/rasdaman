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
package petascope.wcps.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class IncompatibleAxesNumberException extends WCPSException {

    public IncompatibleAxesNumberException(String firstCovName, String secondCovName, int firstCovAxes, int secondCovAxes) {
        super(ExceptionCode.WcpsError, ERROR_TEMPLATE_BINARY_COVERAGE_EXPRESSION.replace("$firstCov", firstCovName).replace("$secondCov", secondCovName)
              .replace("$firstCovAxes", String.valueOf(firstCovAxes))
              .replace("$secondCovAxes", String.valueOf(secondCovAxes)));
    }
    
    public IncompatibleAxesNumberException(String coverageName, int coverageAxes, int dimensionsIntervalsAxes) {
        super(ExceptionCode.WcpsError, ERROR_TEMPLATE_SCALE_OR_EXTEND_EXPRESSION.replace("$coverageName", coverageName)
              .replace("$coverageAxes", String.valueOf(coverageAxes))
              .replace("$dimensionsIntervalsAxes", String.valueOf(dimensionsIntervalsAxes)));
    }

    // e.g: coverage_1 + coverage_2
    public static final String ERROR_TEMPLATE_BINARY_COVERAGE_EXPRESSION = "Coverages '$firstCov' ('$firstCovAxes' axes) and '$secondCov' ('$secondCovAxes' axes) don't have the same number of axes.";
    // e.g: scale(coverage_1, {imageCrsdomain(coverage_2))
    public static final String ERROR_TEMPLATE_SCALE_OR_EXTEND_EXPRESSION = "Coverage '$coverageName' ('$coverageAxes' axes) "
                                                                         + "does not have the same number of axes as input scale/extend dimensions intervals ('$dimensionsIntervalsAxes' axes).";
}
