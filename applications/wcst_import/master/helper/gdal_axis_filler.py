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

from master.importer.axis_subset import AxisSubset
from master.provider.metadata.coverage_axis import CoverageAxis
from master.importer.interval import Interval
from master.provider.metadata.axis import Axis
from master.provider.metadata.grid_axis import GridAxis
from master.provider.metadata.regular_axis import RegularAxis
from util.crs_util import CRSAxis
from util.gdal_util import GDALGmlUtil


class GdalAxisFiller:

    GRID_AXIS_X_ORDER = 0
    GRID_AXIS_Y_ORDER = 1

    # In case import time series with time as grid order 0
    GRID_AXIS_X_ORDER_SHIFTED = 1
    GRID_AXIS_Y_ORDER_SHIFTED = 2

    def __init__(self, axes, gdal_dataset, overview=None):
        """
        Adds the necesary info for an axis derived from a gdal dataset
        :param list[CRSAxis]  axes: a list of potential axes to be filled
        :param GDALGmlUtil gdal_dataset: the gdal dataset that provides the info
        :return:
        """
        self.axes = axes
        self.gdal_dataset = gdal_dataset
        self.subsets = []
        """:type : list[AxisSubset]"""

        # data type: int to know this object is created for a specific overview from file
        self.overview = overview

    def fill(self, timeseries_recipe=False):
        """
        Returns the self.subsets for the given crs axes
        :param boolean timeseries_recipe: Determine if it should create grid axes for 3D coverage in time/x/y order.
        :rtype: list[AxisSubset]
        """
        self._fill_domain_axes()
        self._fill_grid_axis(timeseries_recipe)
        return self.subsets

    def _fill_domain_axes(self):
        for axis in self.axes:
            if axis.is_x_axis():
                east_axis = RegularAxis(axis.label, axis.uom, self.gdal_dataset.get_extents_x()[0],
                                        self.gdal_dataset.get_extents_x()[1],
                                        self.gdal_dataset.get_origin_x(), axis)
                self.subsets.append(
                    AxisSubset(CoverageAxis(east_axis, None, True), Interval(self.gdal_dataset.get_extents_x()[0],
                                                                             self.gdal_dataset.get_extents_x()[1])))
            elif axis.is_y_axis():
                north_axis = RegularAxis(axis.label, axis.uom, self.gdal_dataset.get_extents_y()[0],
                                         self.gdal_dataset.get_extents_y()[1],
                                         self.gdal_dataset.get_origin_y(), axis)

                self.subsets.append(AxisSubset(CoverageAxis(north_axis, None, True),
                                               Interval(self.gdal_dataset.get_extents_y()[0],
                                                        self.gdal_dataset.get_extents_y()[1])))
            else:
                unknown_axis = Axis(axis.label, axis.uom, 0, 0, 0, axis)
                self.subsets.append(AxisSubset(CoverageAxis(unknown_axis, None, False), Interval(0)))

    def _fill_grid_axis(self, timeseries_recipe=False):
        x_order = self.GRID_AXIS_X_ORDER
        y_order = self.GRID_AXIS_Y_ORDER

        if timeseries_recipe:
            x_order = self.GRID_AXIS_X_ORDER_SHIFTED
            y_order = self.GRID_AXIS_Y_ORDER_SHIFTED

        grid_axis_x = GridAxis(x_order, "", self.gdal_dataset.get_resolution_x(), 0,
                               self.gdal_dataset.get_raster_x_size() - 1)
        grid_axis_y = GridAxis(y_order, "", self.gdal_dataset.get_resolution_y(), 0,
                               self.gdal_dataset.get_raster_y_size() - 1)
        for i in range(0, len(self.subsets)):
            if self.subsets[i].coverage_axis.axis.crs_axis.is_x_axis():
                grid_axis_x.label = self.subsets[i].coverage_axis.axis.label
                self.subsets[i].coverage_axis.grid_axis = grid_axis_x
            elif self.subsets[i].coverage_axis.axis.crs_axis.is_y_axis():
                grid_axis_y.label = self.subsets[i].coverage_axis.axis.label
                self.subsets[i].coverage_axis.grid_axis = grid_axis_y
            else:
                self.subsets[i].coverage_axis.grid_axis = GridAxis(i, self.subsets[i].coverage_axis.axis.label, 1, 0, 0)

        return self.subsets
