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
 * Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
from master.helper.user_axis import UserAxis, UserAxisType


class IrregularUserAxis(UserAxis):
    def __init__(self, name, resolution, order, min, directPositions, max=None, type=UserAxisType.NUMBER,
                 dataBound=True, crs_origin=None, statements=[]):
        """
        An IrregularUserAxis is a UserAxis with direct positions and assumed resolution of 1.
        :param str name: The name of the axis
        :param str | float resolution:  the resolution of the axis
        :param int order: the order of this geo axis in the grid. For example EPSG:4326 has geo axes Lat Long
        which correspond to grid axes y, x; However, inside the data container (file) the grid axes are actually x, y.
        In this case the order of the Lat axis is 1 and the order of the Long axis is 0
        :param str | float min: the minimum on this axis
        :param str | float | None max: the maximum on this axis
        :param str type: the type of the values on this axis
        :param str directPositions: the list of direct positions of the slices on this axis
        :param array statements: an array of statements to be executed before evaluation, e.g. import
        """
        UserAxis.__init__(self, name, resolution, order, min, max, type, dataBound, statements)
        self.directPositions = directPositions
