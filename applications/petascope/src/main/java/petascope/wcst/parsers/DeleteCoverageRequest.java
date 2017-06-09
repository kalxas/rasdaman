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

package petascope.wcst.parsers;

import java.util.Arrays;
import java.util.List;
import petascope.util.StringUtil;

/**
 * Request modeling the deletion of a coverage.
 * @autor <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class DeleteCoverageRequest extends AbstractWCSTRequest{

    /**
     * Parse the combination string of deleting coverageIds "e.g: COVERAGEID=a,b,c" to list
     * @param coverageId 
     */
    public DeleteCoverageRequest(String coverageId) {
        coverageIds = Arrays.asList(StringUtil.trim(coverageId).split(","));
    }
    
    public List<String> getCoverageIds() {
        return coverageIds;
    }

    // Coverage Id here is a list of deleting ids
    private final List<String> coverageIds;
}
