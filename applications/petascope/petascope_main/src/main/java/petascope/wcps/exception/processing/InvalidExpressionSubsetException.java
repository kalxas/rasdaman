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

import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;

public class InvalidExpressionSubsetException extends WCPSException {
    
    public InvalidExpressionSubsetException(WcpsSubsetDimension subset) {
        super(ExceptionCode.WcpsError, EXCEPTION_TEXT.replace("$subset", subset.toString())
                .replace("$hint", computeHint(subset)));
    }

    /**
     * Shows an example of the subset being applied at grid level, by changing its crs to CRS:1.
     * @param subset: the user input subset.
     * @return: the same subset with adjusted CRS.
     */
    private static String computeHint(WcpsSubsetDimension subset){
        subset.setCrs(CrsUtil.GRID_CRS);
        return subset.toString();
    }

    private static final String EXCEPTION_TEXT = "Invalid subset expression: $subset. Expressions inside subsets are only allowed on grid axes." +
            "\nHINT: Try a grid subset instead. E.g. $hint subsets directly on the grid.";
}

