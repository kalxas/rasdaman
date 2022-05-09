"""
 *
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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""

from master.provider.metadata.axis import Axis
from master.provider.metadata.grid_axis import GridAxis


class CoverageAxis:
    def __init__(self, geo_axis, grid_axis, dataBound=False):
        """
        A representation of a subset on one axis
        :param Axis geo_axis: the name of the axis
        :param GridAxis grid_axis: the grid axis
        :param bool dataBound: boolean flag to indicate if this axis is bound to the data provider. An axis is bound to
        the data provider if the axis is defined on the original container of the data. E.g. in a 3-D timeseries formed
        from 2D slices, the easting and northing axes are data bound while the time axis is not.
        """
        self.axis = geo_axis
        self.grid_axis = grid_axis
        self.dataBound = dataBound
