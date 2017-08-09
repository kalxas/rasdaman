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
package petascope.wcps.subset_axis.model;

import petascope.wcps.result.ParameterResult;

/**
 * Translation node from wcps axisSpec to rasql
 * Example:
 * <code>
 * x(0:100)
 * </code>
 * translates to
 * <code>
 * x in [0:100]
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class AxisSpec extends ParameterResult {

    /**
     * Constructor for the class
     *
     * @param subsetDimension the interval in which the iteration is done
     */
    public AxisSpec(WcpsSubsetDimension subsetDimension) {
        this.subsetDimension = subsetDimension;
    }

    public WcpsSubsetDimension getSubsetDimension() {
        return subsetDimension;
    }

    public void setAxisName(String axisName) {
        this.subsetDimension.setAxisName(axisName);
    }

    protected WcpsSubsetDimension subsetDimension;
}
