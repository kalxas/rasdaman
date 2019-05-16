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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;
import petascope.wcps.metadata.model.ParsedSubset;

/**
 * General error for invalid subsetting
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class InvalidSubsettingException extends WCPSException {

    /**
     * Constructor for the class default
     *
     * @param axisName the axis on which the subset is being made
     * @param subset the offending subset
     */
    public InvalidSubsettingException(String axisName, ParsedSubset<String> subset, Exception cause) {
        super(ExceptionCode.InvalidSubsetting, subset.isTrimming() ? ERROR_TEMPLATE.replace("$subsetDomainType", "subsetting").replace("$subsetBound", subset.getLowerLimit() + ":" + subset.getUpperLimit()).replace("$axis", axisName)
              : ERROR_TEMPLATE.replace("$subsetDomainType", "slicing").replace("$subsetBound", subset.getSlicingCoordinate()).replace("$axis", axisName), cause);
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
    public InvalidSubsettingException(String axisName, ParsedSubset<String> subset, String exceptionMessage, Exception cause) {
        super(ExceptionCode.InvalidSubsetting, subset.isTrimming() ? exceptionMessage.replace("$subsetDomainType", "subsetting").replace("$subsetBound", subset.getLowerLimit() + ":" + subset.getUpperLimit()).replace("$axis", axisName)
              : exceptionMessage.replace("$subsetDomainType", "slicing").replace("$subsetBound", subset.getSlicingCoordinate()).replace("$axis", axisName), cause);
    }
    
    /**
     * Constructor for the class default
     *
     * @param axisName the axis on which the subset is being made
     * @param subset the offending subset
     */
    public InvalidSubsettingException(String axisName, ParsedSubset<String> subset) {
        super(ExceptionCode.InvalidSubsetting, subset.isTrimming() ? ERROR_TEMPLATE.replace("$subsetDomainType", "subsetting").replace("$subsetBound", subset.getLowerLimit() + ":" + subset.getUpperLimit()).replace("$axis", axisName)
              : ERROR_TEMPLATE.replace("$subsetDomainType", "slicing").replace("$subsetBound", subset.getSlicingCoordinate()).replace("$axis", axisName));
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
        super(ExceptionCode.InvalidSubsetting, subset.isTrimming() ? exceptionMessage.replace("$subsetDomainType", "subsetting").replace("$subsetBound", subset.getLowerLimit() + ":" + subset.getUpperLimit()).replace("$axis", axisName)
              : exceptionMessage.replace("$subsetDomainType", "slicing").replace("$subsetBound", subset.getSlicingCoordinate()).replace("$axis", axisName));
    }
    
    public InvalidSubsettingException(String axisName, String lowerBound, String upperBound) {
        super(ExceptionCode.InvalidSubsetting, 
                            "lower bound '$lowerBound' must be less than or equal to upper bound '$upperBound' for axis '$axis'."
                            .replace("$lowerBound", lowerBound)
                            .replace("$upperBound", upperBound)
                            .replace("$axis", axisName));
    }
    
    private static final String ERROR_TEMPLATE = "Invalid '$subsetDomainType' with '$subsetBound' for axis '$axis'.";

}
