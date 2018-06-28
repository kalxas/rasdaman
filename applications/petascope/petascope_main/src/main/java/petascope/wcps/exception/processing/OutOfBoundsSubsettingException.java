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
 * Error for invalid out of bounds subset parameters
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class OutOfBoundsSubsettingException extends InvalidSubsettingException {

    /**
     * Constructor for the class with domMin:domMax in Double (e.g i, j, Lat,
     * Long)
     *
     * @param axisName the axis on which the subset is being made
     * @param subset the offending subset
     * @param domMin lower limit of coverage's axis domain
     * @param domMax upper limit of coverage's axis domain
     */
    public OutOfBoundsSubsettingException(String axisName, ParsedSubset<String> subset, Double domMin, Double domMax, Exception cause) {
        super(axisName, subset, ERROR_TEMPLATE.replace("$domMin", domMin.toString()).replace("$domMax", domMax.toString()), cause);
    }

    /**
     * Constructor for the class with domMin:domMax in string (e.g time axis)
     *
     * @param axisName the axis on which the subset is being made
     * @param subset the offending subset
     * @param domMin ower limit of coverage's axis domain
     * @param domMax upper limit of coverage's axis domain
     */
    public OutOfBoundsSubsettingException(String axisName, ParsedSubset<String> subset, String domMin, String domMax, Exception cause) {
        super(axisName, subset, ERROR_TEMPLATE.replace("$domMin", domMin).replace("$domMax", domMax), cause);
    }
    
        /**
     * Constructor for the class with domMin:domMax in Double (e.g i, j, Lat,
     * Long)
     *
     * @param axisName the axis on which the subset is being made
     * @param subset the offending subset
     * @param domMin lower limit of coverage's axis domain
     * @param domMax upper limit of coverage's axis domain
     */
    public OutOfBoundsSubsettingException(String axisName, ParsedSubset<String> subset, Double domMin, Double domMax) {
        super(axisName, subset, ERROR_TEMPLATE.replace("$domMin", domMin.toString()).replace("$domMax", domMax.toString()));
    }

    /**
     * Constructor for the class with domMin:domMax in string (e.g time axis)
     *
     * @param axisName the axis on which the subset is being made
     * @param subset the offending subset
     * @param domMin ower limit of coverage's axis domain
     * @param domMax upper limit of coverage's axis domain
     */
    public OutOfBoundsSubsettingException(String axisName, ParsedSubset<String> subset, String domMin, String domMax) {
        super(axisName, subset, ERROR_TEMPLATE.replace("$domMin", domMin).replace("$domMax", domMax));
    }

    private static final String ERROR_TEMPLATE = "Invalid $subsetDomainType '$subsetBound' is not within coverage's domain '$domMin:$domMax' for axis '$axis'.";
}
