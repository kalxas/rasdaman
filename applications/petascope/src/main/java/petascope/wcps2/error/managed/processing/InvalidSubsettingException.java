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

import petascope.wcps2.metadata.Interval;

/**
 * General error for invalid subsetting
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class InvalidSubsettingException extends WCPSProcessingError {

    /**
     * Constructor for the class
     *
     * @param axisName the axis on which the subset is being made
     * @param subset   the offending subset
     */
    public InvalidSubsettingException(String axisName, Interval<String> subset) {
        super(TEMPLATE.replace("$lowerBound", subset.getLowerLimit()).replace("$upperBound", subset.getUpperLimit()).replace("$axis", axisName));
        this.axisName = axisName;
        this.subset = subset;
    }

    /**
     * Getter for axis name
     *
     * @return
     */
    public String getAxisName() {
        return axisName;
    }

    /**
     * Returns the offendingSubset
     *
     * @return
     */
    public Interval<String> getSubset() {
        return subset;
    }

    private final String axisName;
    private final Interval<String> subset;

    private static final String TEMPLATE = "Invalid subsetting coordinates: $lowerBound:$upperBound for axis $axis.";


}
