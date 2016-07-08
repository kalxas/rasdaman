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

from master.importer.interval import Interval


class ExtraMetadataSliceSubset:
    def __init__(self, axis_name, interval):
        """
        Represents a subset associated to a slice of extra metadata
        :param str axis_name: the name of the axis
        :param Interval interval:
        """
        self.axis_name = axis_name
        self.interval = interval


class ExtraMetadataSlice:
    def __init__(self, subsets, metadata_dictionary):
        """
        Represents a slice of extra metadata
        :param list[ExtraMetadataSliceSubset] subsets: the subsets on each axis defining the position in spacetime of the extra metadata
        """
        self.subsets = subsets
        self.metadata_dictionary = metadata_dictionary
