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

import math
import decimal
from lib import arrow
from master.error.runtime_exception import RuntimeException
from master.evaluator.evaluator_slice import GDALEvaluatorSlice
from master.evaluator.sentence_evaluator import SentenceEvaluator
from master.helper.regular_user_axis import RegularUserAxis
from master.helper.user_axis import UserAxis, UserAxisType
from master.helper.user_band import UserBand
from master.importer.axis_subset import AxisSubset
from master.importer.interval import Interval
from master.provider.metadata.coverage_axis import CoverageAxis
from master.provider.metadata.grid_axis import GridAxis
from master.provider.metadata.irregular_axis import IrregularAxis
from master.provider.metadata.regular_axis import RegularAxis
from recipes.general_coverage.abstract_to_coverage_converter import AbstractToCoverageConverter
from util.crs_util import CRSAxis
from util.file_obj import File
from master.helper.high_pixel_adjuster import HighPixelAjuster
from master.helper.point_pixel_adjuster import PointPixelAdjuster
from util.gdal_util import GDALGmlUtil


class GdalToCoverageConverter(AbstractToCoverageConverter):

    RECIPE_TYPE = "gdal"

    def __init__(self, resumer, default_null_values, recipe_type, sentence_evaluator, coverage_id, bands, files, crs, user_axes, tiling,
                 global_metadata_fields, local_metadata_fields, bands_metadata_fields,
                 axes_metadata_fields, metadata_type, grid_coverage, import_order):
        """
        Converts a gdal list of files to a coverage
        :param resumer: resumer object
        :param default_null_values: list of null values from ingredient files if specified
        :param recipe_type: the type of recipe
        :param SentenceEvaluator sentence_evaluator: the evaluator for wcst sentences
        :param str coverage_id: the id of the coverage
        :param list[UserBand] bands: the name of the coverage band
        :param list[File] files: a list of gdal files
        :param str crs: the crs of the coverage
        :param list[UserAxis] user_axes: a list with user axes
        :param str tiling: the tiling string to be passed to wcst
        :param dict global_metadata_fields: the global metadata fields
        :param dict local_metadata_fields: the local metadata fields
        :param dict bands_metadata_fields: the bands metadata fields
        :param dict axes_metadata_fields: the axes metadata fields
        :param str metadata_type: the metadata type
        :param boolean grid_coverage: check if user want to import grid coverage
        :param import_order: ascending(default), descending if specified in ingredient file
        """
        AbstractToCoverageConverter.__init__(self, resumer, recipe_type, sentence_evaluator, import_order)
        self.default_null_values = default_null_values
        self.resumer = resumer
        self.sentence_evaluator = sentence_evaluator
        self.coverage_id = coverage_id
        self.bands = bands
        self.files = files
        self.crs = crs
        self.user_axes = user_axes
        self.tiling = tiling
        self.global_metadata_fields = global_metadata_fields
        self.local_metadata_fields = local_metadata_fields
        self.bands_metadata_fields = bands_metadata_fields
        self.axes_metadata_fields = axes_metadata_fields
        self.metadata_type = metadata_type
        self.grid_coverage = grid_coverage
        self.data_type = None

    def _data_type(self):
        return self.data_type

    @staticmethod
    def parse_gdal_global_metadata(file_path):
        """
        Parse the first file of importing gdal files to extract the global metadata for the coverage
        str file_path: path to first gdal input file
        :return: dict: global_metadata
        """
        # NOTE: all files should have same global metadata for each file
        gdal_dataset = GDALGmlUtil(file_path)
        global_metadata = gdal_dataset.get_metadata()

        return global_metadata

    def _file_band_nil_values(self, index):
        """
        This is used to get the null values (Only 1) from the given band index if one exists when nilValue was not defined
        in ingredient file
        :param integer index: the current band index to get the nilValues
        :rtype: List[RangeTypeNilValue] with only 1 element
        """
        if len(self.files) < 1:
            raise RuntimeException("No gdal files given for import!")

        if len(self.default_null_values) > 0:
            return self.default_null_values

        # NOTE: all files should have same bands's metadata, so 1 file is ok
        gdal_dataset = GDALGmlUtil.open_gdal_dataset_from_any_file(self.files)
        # band in gdal starts with 1
        gdal_band = gdal_dataset.get_raster_band(index + 1)
        nil_value = gdal_band.GetNoDataValue()

        if nil_value is None:
            return None
        else:
            return [nil_value]

    def _axis_subset(self, crs_axis, evaluator_slice, resolution=None):
        """
        Returns an axis subset using the given crs axis in the context of the gdal file
        :param CRSAxis crs_axis: the crs definition of the axis
        :param GDALEvaluatorSlice evaluator_slice: the evaluator for GDAL file
        :param resolution: Known axis resolution, no need to evaluate sentence expression from ingredient file (e.g: Sentinel2 recipe)
        :rtype AxisSubset
        """
        user_axis = self._user_axis(self._get_user_axis_by_crs_axis_name(crs_axis.label),
                                    evaluator_slice)
        if resolution is not None:
            user_axis.resolution = resolution

        high = user_axis.interval.high if user_axis.interval.high is not None else user_axis.interval.low

        if user_axis.type == UserAxisType.DATE:
            # it must translate datetime string to float by arrow for calculating later
            user_axis.interval.low = arrow.get(user_axis.interval.low).float_timestamp
            if user_axis.interval.high is not None:
                user_axis.interval.high = arrow.get(user_axis.interval.high).float_timestamp

        if isinstance(user_axis, RegularUserAxis):
            geo_axis = RegularAxis(crs_axis.label, crs_axis.uom, user_axis.interval.low, high,
                                   user_axis.interval.low, crs_axis)
        else:
            # Irregular axis (coefficients must be number, not datetime string)
            if user_axis.type == UserAxisType.DATE:
                if crs_axis.is_time_day_axis():
                    coefficients = self._translate_day_date_direct_position_to_coefficients(user_axis.interval.low,
                                                                                            user_axis.directPositions)
                else:
                    coefficients = self._translate_seconds_date_direct_position_to_coefficients(user_axis.interval.low,
                                                                                                user_axis.directPositions)
            else:
                coefficients = self._translate_number_direct_position_to_coefficients(user_axis.interval.low,
                                                                                      user_axis.directPositions)

            self._update_for_slice_group_size(self.coverage_id, user_axis, crs_axis, coefficients)

            geo_axis = IrregularAxis(crs_axis.label, crs_axis.uom, user_axis.interval.low, high, user_axis.interval.low, coefficients,
                                     crs_axis)

        if not crs_axis.is_x_axis() and not crs_axis.is_y_axis():
            # GDAL model is 2D so on any axis except x/y we expect to have only one value
            grid_low = 0
            grid_high = None
            if user_axis.interval.high is not None:
                grid_high = 0
        else:
            grid_low = 0
            number_of_grid_points = decimal.Decimal(str(user_axis.interval.high)) \
                                  - decimal.Decimal(str(user_axis.interval.low))
            # number_of_grid_points = (geo_max - geo_min) / resolution
            grid_high = grid_low + number_of_grid_points / decimal.Decimal(user_axis.resolution)
            grid_high = HighPixelAjuster.adjust_high(grid_high)

            # Negative axis, e.g: Latitude (min <--- max)
            if user_axis.resolution < 0:
                grid_high = int(abs(math.floor(grid_high)))
            else:
                # Positive axis, e.g: Longitude (min --> max)
                grid_high = int(abs(math.ceil(grid_high)))

        # NOTE: Grid Coverage uses the direct intervals as in Rasdaman
        if self.grid_coverage is False and grid_high is not None:
            if grid_high > grid_low:
                grid_high -= 1

        grid_axis = GridAxis(user_axis.order, crs_axis.label, user_axis.resolution, grid_low, grid_high)
        geo_axis.origin = PointPixelAdjuster.get_origin(user_axis, crs_axis)
        if user_axis.type == UserAxisType.DATE:
            self._translate_decimal_to_datetime(user_axis, geo_axis)
        # NOTE: current, gdal recipe supports only has 2 axes which are "bounded" (i.e: they exist as 2D axes in file)
        # and 1 or more another axes gotten (i.e: from fileName) which are not "bounded" to create 3D+ coverage.
        data_bound = crs_axis.is_y_axis() or crs_axis.is_x_axis()

        return AxisSubset(CoverageAxis(geo_axis, grid_axis, data_bound),
                          Interval(user_axis.interval.low, user_axis.interval.high))

