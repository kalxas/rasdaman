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

from collections import OrderedDict

from master.generator.model.range_type_field import RangeTypeField
from master.importer.slice import Slice
from master.provider.metadata.axis import Axis
from master.provider.metadata.grid_axis import GridAxis


class Coverage:
    def __init__(self, coverage_id, slices, range_fields, crs, pixel_data_type, tiling=None):
        """
        Class to represent a coverage that is created in a recipe containing the minimum amount of information
        form which we can extrapolate the whole gmlcov
        :param str coverage_id: the id of the coverage
        :param list[Slice] slices: a list of slices defining the coverage
        :param list[RangeTypeField] range_fields: the range fields for the coverage
        :param str crs: the crs of the coverage
        :param str pixel_data_type: the type of the pixel in gdal format
        :param str tiling: the tiling string to be passed to rasdaman if one is chosen
        """
        self.coverage_id = coverage_id
        self.slices = slices
        self.range_fields = range_fields
        self.crs = crs
        self.pixel_data_type = pixel_data_type
        self.tiling = tiling

    def get_insert_axes(self):
        """
        Returns all the axes for this subset
        :rtype: dict[Axis, GridAxis]
        """
        axes = OrderedDict()
        for axis_subset in self.slices[0].axis_subsets:
            axes[axis_subset.coverage_axis.axis] = axis_subset.coverage_axis.grid_axis
        return axes

    def get_update_axes(self):
        """
        Returns the axes for the slices that are bound to the data (e.g. Lat and Long for a 2-D raster)
        :rtype: dict[Axis, GridAxis]
        """
        axes = OrderedDict()
        for axis_subset in self.slices[0].axis_subsets:
            if axis_subset.coverage_axis.data_bound:
                axes[axis_subset.coverage_axis.axis] = axis_subset.coverage_axis.grid_axis
        return axes

    def __str__(self):
        ret = "{Coverage Id: " + self.coverage_id + "\n Pixel Type: " + str(self.pixel_data_type) + "\nSlices: {"
        for slice in self.slices:
            ret += str(slice) + "\n"
        ret += "}\n"
        return ret