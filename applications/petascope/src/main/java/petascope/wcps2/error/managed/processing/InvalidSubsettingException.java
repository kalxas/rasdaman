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
import petascope.wcps2.metadata.model.ParsedSubset;

/**
 * General error for invalid subsetting
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class InvalidSubsettingException extends WCPSProcessingError {

    /**
     * Constructor for the class default
     *
     * @param axisName the axis on which the subset is being made
     * @param subset the offending subset
     */
    public InvalidSubsettingException(String axisName, ParsedSubset<String> subset) {
        super(ERROR_TEMPLATE.replace("$lowerBound", subset.getLowerLimit()).replace("$upperBound", subset.getUpperLimit()).replace("$axis", axisName), ExceptionCode.InvalidSubsetting);
    }

    /**
     * Constructor for the class when subclass send its appropriate
     * exceptionMessage for trimming/slicing
     *
     * @param axisName the axis on which the subset is being made
     * @param subset the offending subset
     * @param exceptionMessage the appropriate exception message (e.g: unordered
     * interval, time error,..)
     */
    public InvalidSubsettingException(String axisName, ParsedSubset<String> subset, String exceptionMessage) {
        super(subset.isTrimming() ? exceptionMessage.replace("$subsetDomainType", "subsetting").replace("$subsetBound", subset.getLowerLimit() + ":" + subset.getUpperLimit()).replace("$axis", axisName)
                : exceptionMessage.replace("$subsetDomainType", "slicing").replace("$subsetBound", subset.getSlicingCoordinate()).replace("$axis", axisName), ExceptionCode.InvalidSubsetting);
    }

    private static final String ERROR_TEMPLATE = "Invalid subsetting coordinates '$lowerBound:$upperBound' for axis '$axis'.";

}
