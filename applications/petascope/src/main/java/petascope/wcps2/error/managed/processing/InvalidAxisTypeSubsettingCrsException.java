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
package petascope.wcps2.error.managed.processing;

/**
 * Error message for using subsettingCrs in not geo-referenced/grid axis
 *
 * @author <a href="mailto:bphamhuu@jacbos-university.de">Bang Pham Huu</a> 
 */
public class InvalidAxisTypeSubsettingCrsException extends WCPSProcessingError {
    /**
     * Constructor for the class
     *
     * @param axisName axis name of subsettingCrs
     * @param subsettingCrs
     */
    public InvalidAxisTypeSubsettingCrsException (String axisName, String subsettingCrs) {
        super(ERROR_TEMPLATE.replace("$axisName", axisName).replace("$subsettingCrs", subsettingCrs));
    }
    
    private static final String ERROR_TEMPLATE = "subsettingCrs '$subsettingCrs' is not supported on axis '$axisName'.";
}