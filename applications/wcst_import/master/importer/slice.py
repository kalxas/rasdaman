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

from master.importer.axis_subset import AxisSubset
from master.provider.data.data_provider import DataProvider


class Slice:
    def __init__(self, axis_subsets, data_provider):
        """
        Class to represent one slice of the coverage
        :param list[AxisSubset] axis_subsets: the position of this slice in the coverage represented through a list
        of axis subsets
        :param DataProvider data_provider: a data provider that can get the data corresponding to this slice
        """
        self.axis_subsets = axis_subsets
        self.data_provider = data_provider

    def __str__(self):
        ret = "{Axis Subset: "
        for subset in self.axis_subsets:
            ret += str(subset) + " "
        ret += "\nData Provider: " + str(self.data_provider) + "}\n"
        return ret