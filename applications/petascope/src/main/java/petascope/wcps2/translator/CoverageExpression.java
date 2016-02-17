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
package petascope.wcps2.translator;

import org.jetbrains.annotations.NotNull;
import petascope.wcps2.metadata.Coverage;

/**
 * Class to represent a node in the tree that is a coverage expression
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public abstract class CoverageExpression extends IParseTreeNode {

    /**
     * Returns the coverage resulting from this operation
     *
     * @return
     */
    @NotNull
    public Coverage getCoverage() {
        return coverage;
    }

    /**
     * Sets the coverage resulting from this operation
     *
     * @param coverage
     * @return
     */
    public void setCoverage(@NotNull Coverage coverage) {
        this.coverage = coverage;
    }

    private Coverage coverage = null;
}
