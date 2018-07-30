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
package petascope.wcst.exceptions;

import petascope.exceptions.WCSTException;
import petascope.exceptions.ExceptionCode;

/**
 * Scale level from InsertScaleLevel request is not valid.
 * 
 @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class WCSTScaleLevelNotValid extends WCSTException {

    public WCSTScaleLevelNotValid(String level) {
        super(ExceptionCode.InvalidRequest, EXCEPTION_TEXT.replace("$level", level));
    }

    private static final String EXCEPTION_TEXT = "Scale level must be positive number and greater than 1, given '$level'.";
}
