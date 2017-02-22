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
from lib import arrow
from master.evaluator.evaluator_slice import NetcdfEvaluatorSlice
from master.helper.point_pixel_adjuster import PointPixelAdjuster
from master.helper.regular_user_axis import RegularUserAxis
from master.helper.user_axis import UserAxisType
from master.importer.axis_subset import AxisSubset
from master.importer.interval import Interval
from master.provider.metadata.coverage_axis import CoverageAxis
from master.provider.metadata.grid_axis import GridAxis
from master.provider.metadata.irregular_axis import IrregularAxis
from master.provider.metadata.regular_axis import RegularAxis
from recipes.general_coverage.netcdf_to_coverage_converter import NetcdfToCoverageConverter
from util.string_util import stringify
from util.time_util import DateTimeUtil

class PointPixelNetcdfToCoverageConverter(NetcdfToCoverageConverter):
    """
    Converts a netcdf list of files to a coverage
    :param SentenceEvaluator sentence_evaluator: the evaluator for wcst sentences
    :param str coverage_id: the id of the coverage
    :param list[UserBand] bands: the name of the coverage band
    :param list[File] nc_files: a list of grib files
    :param str crs: the crs of the coverage
    :param list[UserAxis] user_axes: a list with user axes
    :param str tiling: the tiling string to be passed to wcst
    :param dict global_metadata_fields: the global metadata fields
    :param dict local_metadata_fields: the local metadata fields
    :param str metadata_type: the metadata type
    :param boolean grid_coverage: check if user want to import as grid coverage
    """

    def __init__(self, sentence_evaluator, coverage_id, bands, nc_files, crs, user_axes, tiling, global_metadata_fields,
                 local_metadata_fields, metadata_type, grid_coverage):
        NetcdfToCoverageConverter.__init__(self, sentence_evaluator, coverage_id, bands, nc_files, crs, user_axes,
                                           tiling, global_metadata_fields, local_metadata_fields, metadata_type,
                                           grid_coverage)

    def _axis_subset(self, crs_axis, nc_file):
        """
        Returns an axis subset using the given crs axis in the context of the nc file
        :param CRSAxis crs_axis: the crs definition of the axis
        :param File nc_file: the netcdf file
        :rtype AxisSubset
        """
        user_axis = self._user_axis(self._get_user_axis_by_crs_axis_name(crs_axis.label), NetcdfEvaluatorSlice(nc_file))
        PointPixelAdjuster.adjust_axis_bounds_to_continuous_space(user_axis)

        high = user_axis.interval.high if user_axis.interval.high else user_axis.interval.low

        if isinstance(user_axis, RegularUserAxis):
            geo_axis = RegularAxis(crs_axis.label, crs_axis.uom, user_axis.interval.low, high,
                                   PointPixelAdjuster.get_origin(user_axis), crs_axis)
        else:
            if user_axis.type == UserAxisType.DATE:
                if crs_axis.is_uom_day():
                    coefficients = self._translate_day_date_direct_position_to_coefficients(user_axis.interval.low,
                                                                                            user_axis.directPositions)
                else:
                    coefficients = self._translate_seconds_date_direct_position_to_coefficients(user_axis.interval.low,
                                                                                                user_axis.directPositions)
            else:
                coefficients = self._translate_number_direct_position_to_coefficients(user_axis.interval.low,
                                                                                      user_axis.directPositions)
            geo_axis = IrregularAxis(crs_axis.label, crs_axis.uom, user_axis.interval.low, high,
                                     PointPixelAdjuster.get_origin(user_axis), coefficients, crs_axis)

        grid_low = 0
        grid_high = PointPixelAdjuster.get_grid_points(user_axis, crs_axis)
        # NOTE: Grid Coverage uses the direct intervals as in Rasdaman, modify the high bound will have error in petascope
        if not self.grid_coverage and grid_high > grid_low:
            grid_high -= 1

        grid_axis = GridAxis(user_axis.order, crs_axis.label, user_axis.resolution, grid_low, grid_high)

        if user_axis.type == UserAxisType.DATE:
            geo_axis.origin = DateTimeUtil.get_datetime_iso(geo_axis.origin)
            geo_axis.low = DateTimeUtil.get_datetime_iso(geo_axis.low)
            if geo_axis.high is not None:
                geo_axis.high = DateTimeUtil.get_datetime_iso(geo_axis.high)
            user_axis.interval.low = DateTimeUtil.get_datetime_iso(user_axis.interval.low)
            if user_axis.interval.high is not None:
                user_axis.interval.high = DateTimeUtil.get_datetime_iso(user_axis.interval.high)

        return AxisSubset(CoverageAxis(geo_axis, grid_axis, user_axis.dataBound),
                          Interval(user_axis.interval.low, user_axis.interval.high))
