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

import petascope.wcps.metadata.model.ParsedSubset;

/**
 * Error to be thrown if bounds that are calculated are not correct.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class InvalidCalculatedBoundsSubsettingException extends InvalidSubsettingException {

    /**
     * Constructor for the class
     *
     * @param axisName the axis on which the subset is being made
     * @param subset   the offending subset
     */
    public InvalidCalculatedBoundsSubsettingException(String axisName, ParsedSubset<String> subset, Exception cause) {
        super(axisName, subset, ERROR_TEMPLATE, cause);
    }

    public static String ERROR_TEMPLATE = "The bound(s) were not correctly calculated '$subsetDomainType' coordinate(s) '$subsetBound' for axis '$axis'.";
}
