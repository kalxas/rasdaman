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
import pygrib

from lib import arrow
from master.helper.user_axis import UserAxis
from master.helper.point_pixel_adjuster import PointPixelAdjuster
from master.helper.irregular_user_axis import IrregularUserAxis
from master.helper.regular_user_axis import RegularUserAxis
from master.evaluator.evaluator_slice import GribMessageEvaluatorSlice
from master.importer.axis_subset import AxisSubset
from master.importer.interval import Interval
from master.provider.metadata.coverage_axis import CoverageAxis
from master.helper.user_axis import UserAxisType
from master.provider.metadata.grid_axis import GridAxis
from master.provider.metadata.irregular_axis import IrregularAxis
from master.provider.metadata.regular_axis import RegularAxis
from recipes.general_coverage.grib_to_coverage_converter import GRIBToCoverageConverter
from recipes.general_coverage.grib_to_coverage_converter import GRIBMessage
from master.importer.slice import Slice
from master.provider.data.file_data_provider import FileDataProvider
from util.string_util import stringify

class PointPixelGRIBToCoverageConverter(GRIBToCoverageConverter):
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
        GRIBToCoverageConverter.__init__(self, sentence_evaluator, coverage_id, bands, nc_files, crs, user_axes,
                                         tiling, global_metadata_fields, local_metadata_fields, metadata_type,
                                         grid_coverage)

    def _messages(self, grib_file):
        """
        Returns the message information already evaluated
        :param File grib_file: the grib file for which to return the messages
        :rtype: list[GRIBMessage]
        """
        dataset = pygrib.open(grib_file.get_filepath())
        messages = []
        for i in range(1, dataset.messages + 1):
            message = dataset.message(i)
            axes = []
            for user_axis in self.user_axes:
                evaluated_user_axis = self._user_axis(user_axis, GribMessageEvaluatorSlice(message, grib_file))
                # Shift by half pixel for min/max of the axis
                PointPixelAdjuster.adjust_axis_bounds_to_continuous_space(evaluated_user_axis)
                axes.append(evaluated_user_axis)
            messages.append(GRIBMessage(i, axes, message))
        return messages

    def _slice(self, grib_file, crs_axes):
        """
        Returns a slice for a grib file
        :param File grib_file: the path to the grib file
        :param list[CRSAxis] crs_axes: the crs axes for the coverage
        :rtype: Slice
        """
        messages = self._messages(grib_file)
        axis_subsets = []
        for i in range(0, len(crs_axes)):
            crs_axis = crs_axes[i]
            user_axis = self._get_user_axis(messages[0], crs_axis.label)
            low, high, origin, grid_low, grid_high, resolution = self._low_high_origin(messages, crs_axis.label,
                                                                                       not crs_axis.is_future())
            if isinstance(user_axis, IrregularUserAxis):
                user_axis_tmp = IrregularUserAxis(user_axis.name, user_axis.resolution, user_axis.order, low, high,
                                                  user_axis.type, user_axis.dataBound)

                geo_axis = IrregularAxis(crs_axis.label, crs_axis.uom, low, high,
                                         PointPixelAdjuster.get_origin(user_axis_tmp),
                                         user_axis.directPositions, crs_axis)
            else:
                user_axis_tmp = RegularUserAxis(user_axis.name, resolution, user_axis.order, low, high,
                                                user_axis.type, user_axis.dataBound)
                geo_axis = RegularAxis(crs_axis.label, crs_axis.uom, low, high,
                                       PointPixelAdjuster.get_origin(user_axis_tmp), crs_axis)

            if user_axis_tmp.type == UserAxisType.DATE:
                geo_axis.origin = stringify(arrow.get(geo_axis.origin))
                geo_axis.low = stringify(arrow.get(geo_axis.low))
                if geo_axis.high is not None:
                    geo_axis.high = stringify(arrow.get(geo_axis.high))
                    user_axis_tmp.interval.low = stringify(arrow.get(user_axis_tmp.interval.low))
                if user_axis.interval.high is not None:
                    user_axis_tmp.interval.high = stringify(arrow.get(user_axis_tmp.interval.high))

            grid_axis = GridAxis(user_axis.order, crs_axis.label, resolution, grid_low, grid_high)
            axis_subsets.append(AxisSubset(CoverageAxis(geo_axis, grid_axis, user_axis.dataBound), Interval(low, high)))
        return Slice(axis_subsets, FileDataProvider(grib_file, self._messages_to_dict(messages), self.MIMETYPE))
