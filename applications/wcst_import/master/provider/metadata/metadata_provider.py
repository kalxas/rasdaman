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

from master.error.runtime_exception import RuntimeException
from master.generator.model.offset_vector_irregular import OffsetVectorIrregular
from master.generator.model.offset_vector_regular import OffsetVectorRegular
from master.provider.metadata.axis import Axis
from master.provider.metadata.grid_axis import GridAxis
from master.provider.metadata.irregular_axis import IrregularAxis
from util.xml_util import XMLUtil


class MetadataProvider:
    def __init__(self, coverage_id, axes_map, range_fields, crs, extra_metadata, grid_coverage):
        """
        Class to provide the basis for metadata providers
        :param coverage_id: the id of the coverage
        :param dict[Axis, GridAxis] axes_map: a mapping from axis to grid axis
        :param list[RangeTypeField] range_fields: the range fields for this coverage
        :param str crs: the crs of the coverage
        :param str | None extra_metadata: any extra metadata that should go in the coverage
        :param boolean: check if want to import a grid coverage
        """
        self.coverage_id = coverage_id
        self.axes = axes_map.keys()
        self.grid_axes = axes_map.values()
        self.grid_axes.sort()
        self.range_fields = range_fields
        self.axes_map = axes_map
        self.crs = crs
        self.extra_metadata = extra_metadata
        self.grid_coverage = grid_coverage

    def get_crs(self):
        """
        Returns the crs of the coverage
        :rtype: str
        """
        # Escape the crs as it can contain ampersands for compound crses
        return XMLUtil.escape(self.crs)

    def get_axis_labels(self):
        """
        Returns the labels of the axis
        :rtype: list[str]
        """
        ret = []
        for axis in self.axes:
            ret.append(str(axis.label))
        return ret

    def get_axis_labels_grid(self):
        """
        Returns the labels of the grid axis
        :rtype: list[str]
        """
        ret = []
        for grid_axis in self.grid_axes:
            ret.append(grid_axis.label)
        return ret

    def get_axis_uom_labels(self):
        """
        Returns the labels of the uom axis
        :rtype: list[str]
        """
        ret = []
        for axis in self.axes:
            ret.append(str(axis.uomLabel))
        return ret

    def get_no_of_dimensions(self):
        """
        Returns the number of dimensions
        :rtype: int
        """
        return len(self.axes)

    def get_lower_corner(self):
        """
        Returns the lower corner of the coverage
        :rtype: list[float]
        """
        low = []
        for axis in self.axes:
            low.append(axis.low)
        return low

    def get_upper_corner(self):
        """
        Returns the upper corner of the coverage
        :rtype: list[float]
        """
        high = []
        for axis in self.axes:
            high.append(axis.high)
        return high

    def get_grid_low(self):
        """
        Returns the lower grid corner of the coverage
        :rtype: list[int]
        """
        low = []
        for grid_axis in self.grid_axes:
            low.append(grid_axis.grid_low)
        return low

    def get_grid_high(self):
        """
        Returns the upper grid corner of the coverage
        :rtype: list[int]
        """
        high = []
        for grid_axis in self.grid_axes:
            high.append(grid_axis.grid_high)
        return high

    def get_offset_vectors(self):
        """
        Returns the offset vectors of the coverage
        :rtype: list[OffsetVector]
        """
        offsets = []
        for grid_axis in self.grid_axes:
            axis = self._get_axis_for_grid_axis(grid_axis)
            offset_vector = [0] * len(self.grid_axes)
            offset_vector[self._get_axis_position_by_grid(grid_axis)] = grid_axis.resolution
            if self.is_coverage_irregular():
                coefficient = None if axis.coefficient is None else " ".join(map(lambda x: str(x), axis.coefficient))
                offsets.append(OffsetVectorIrregular(self.get_crs(), self.get_axis_labels(), self.get_axis_uom_labels(),
                                                     self.get_no_of_dimensions(), offset_vector, coefficient, grid_axis.label))
            else:
                offsets.append(OffsetVectorRegular(self.get_crs(), self.get_axis_labels(), self.get_axis_uom_labels(),
                                                   self.get_no_of_dimensions(), offset_vector, grid_axis.label))
        return offsets

    def _get_axis_for_grid_axis(self, grid_axis):
        """
        Return the domain axis for a grid axis
        :param GridAxis grid_axis: the grid axis to lookup
        :rtype: Axis
        """
        for axis, grid in self.axes_map.iteritems():
            if grid_axis == grid:
                return axis

    def _get_axis_position_by_grid(self, grid_axis):
        index = 0
        for axis in self.axes:
            if self.axes_map[axis] == grid_axis:
                return index
            index += 1
        raise RuntimeException(
            "Internal error: no corresponding domain axis found for the given grid axis with order " + grid_axis.order +
            " and label: " + grid_axis.label)

    def _get_offset_vector_for_axis(self, axis):
        """
        Returns the offset vector for one axis
        :param Axis axis: the axis to get the offset vector for
        :rtype: list[float]
        """
        grid_axis = self.axes_map[axis]
        offset_vector = [0] * len(self.axes)
        offset_vector[grid_axis.order] = grid_axis.resolution
        return offset_vector

    def get_origin(self):
        """
        Returns the origin of the coverage
        :rtype: list[float]
        """
        origin = []
        for axis in self.axes:
            origin.append(axis.origin)
        return origin

    def get_range_fields(self):
        """
        Returns the range fields of the coverage
        :rtype: list[RangeTypeField]
        """
        return self.range_fields

    def get_coverage_id(self):
        """
        Returns the id of the coverage
        :rtype: str
        """
        return self.coverage_id

    def is_coverage_irregular(self):
        """
        Returns true if the coverage is irregular, false otherwise
        :rtype: bool
        """
        for axis in self.axes:
            if isinstance(axis, IrregularAxis):
                return True
        return False

    def is_grid_coverage(self):
        """
        Returns true if user want to import coverage as a grid coverage
        :rtype: bool
        """
        return bool(self.grid_coverage)
