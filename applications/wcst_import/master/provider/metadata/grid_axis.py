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

import functools


@functools.total_ordering
class GridAxis:
    def __init__(self, order, label, resolution, grid_low, grid_high):
        """
        Class to represent a grid axis in gml
        :param int order: the axis order in the original dataset
        :param str label: the label of this axis, usually taken from the domain axis
        :param float resolution: the resolution on this grid axis
        :param int grid_low: the low extent of the grid
        :param int grid_high: the high extent of the grid
        """
        self.order = order
        self.label = label
        self.resolution = resolution
        self.grid_low = grid_low
        self.grid_high = grid_high

    def __eq__(self, other):
        """
        Compares two tuples ==
        :param GridAxis other: the tuple to compare with
        :rtype bool
        """
        return self.order == other.order

    def __lt__(self, other):
        """
        Compares two tuples <
        :param GridAxis other: the tuple to compare with
        :rtype bool
        """
        return self.order < other.order