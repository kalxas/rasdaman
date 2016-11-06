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
import pygrib
import sys
from datetime import datetime, MINYEAR, MAXYEAR
from dateutil.parser import parse

from master.evaluator.evaluator_slice import GribMessageEvaluatorSlice
from master.evaluator.sentence_evaluator import SentenceEvaluator
from master.extra_metadata.extra_metadata_collector import ExtraMetadataCollector, ExtraMetadataEntry
from master.extra_metadata.extra_metadata_ingredient_information import ExtraMetadataIngredientInformation
from master.extra_metadata.extra_metadata_serializers import ExtraMetadataSerializerFactory
from master.extra_metadata.extra_metadata_slice import ExtraMetadataSliceSubset
from master.generator.model.range_type_field import RangeTypeField
from master.generator.model.range_type_nill_value import RangeTypeNilValue
from master.helper.irregular_user_axis import IrregularUserAxis
from master.helper.user_axis import UserAxis
from master.helper.user_band import UserBand
from master.importer.axis_subset import AxisSubset
from master.importer.coverage import Coverage
from master.importer.interval import Interval
from master.importer.slice import Slice
from master.provider.data.file_data_provider import FileDataProvider
from master.provider.metadata.coverage_axis import CoverageAxis
from master.provider.metadata.grid_axis import GridAxis
from master.provider.metadata.irregular_axis import IrregularAxis
from master.provider.metadata.regular_axis import RegularAxis
from recipes.general_coverage.abstract_to_coverage_converter import AbstractToCoverageConverter
from util.crs_util import CRSAxis, CRSUtil
from util.file_obj import File
from util.string_util import stringify


class MetadataType:
    JSON = "json"
    XML = "xml"

    @staticmethod
    def valid_type(type):
        if type != MetadataType.JSON and type != MetadataType.XML:
            return False
        return True


class GRIBMessage:
    def __init__(self, id, axes, message):
        """
        A representation of a grib message
        :param int id: the id of the message
        :param list[UserAxis] axes: the axes corresponding to this message
        :param pygrib.message message: the message as a pygrib data structure
        """
        self.id = id
        self.axes = axes
        self.message = message

    def to_json(self):
        """
        Translates the message to the petascope expected json dict
        :rtype: dict
        """
        axes = []
        for axis in self.axes:
            axes.append(axis.to_json())
        return {
            "messageId": self.id,
            "axes": axes
        }


class GRIBToCoverageConverter(AbstractToCoverageConverter):
    DEFAULT_DATA_TYPE = "Float64"
    MIMETYPE = "application/grib"

    def __init__(self, sentence_evaluator, coverage_id, band, grib_files, crs, user_axes, tiling,
                 global_metadata_fields, local_metadata_fields, metadata_type, grid_coverage):
        """
        Converts a grib list of files to a coverage
        :param SentenceEvaluator sentence_evaluator: the evaluator for wcst sentences
        :param str coverage_id: the id of the coverage
        :param UserBand band: the name of the coverage band
        :param list[File] grib_files: a list of grib files
        :param str crs: the crs of the coverage
        :param list[UserAxis] user_axes: a list with user axes
        :param str tiling: the tiling string to be passed to wcst
        :param dict global_metadata_fields: the global metadata fields
        :param dict local_metadata_fields: the local metadata fields
        :param str metadata_type: the metadata type
        :param boolean grid_coverage: check if user want to import grid coverage
        """
        AbstractToCoverageConverter.__init__(self, sentence_evaluator)
        self.sentence_evaluator = sentence_evaluator
        self.coverage_id = coverage_id
        self.band = band
        self.grib_files = grib_files
        self.crs = crs
        self.user_axes = user_axes
        self.tiling = tiling
        self.global_metadata_fields = global_metadata_fields
        self.local_metadata_fields = local_metadata_fields
        self.metadata_type = metadata_type
        self.grid_coverage = grid_coverage

    def _get_null_value(self):
        """
        Returns the null value for this file
        :rtype: list[RangeTypeNilValue]
        """
        if self.band.nilValues is not None:
            range_nils = []
            for nil_value in self.band.nilValues:
                range_nils.append(RangeTypeNilValue("", nil_value))
            return range_nils
        dataset = pygrib.open(self.grib_files[0].filepath)
        return [RangeTypeNilValue(self.band.nilReason, dataset.message(1)["missingValue"])]

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
                axes.append(self._user_axis(user_axis, GribMessageEvaluatorSlice(message, grib_file)))
            messages.append(GRIBMessage(i, axes, message))
        return messages

    def _low_high_origin_numeric(self, messages, axis_name):
        """
        Returns the low, high and the origin for an axis in the context of a grib file
        :param list[GRIBMessage] messages: a list of messages represented by a list of user axes
        :param str axis_name: the axis name
        :rtype: (float, float, float, int, int, float)
        """
        low, high, resolution = sys.maxint, -sys.maxint - 1, None
        for message in messages:
            for axis in message.axes:
                if axis.name == axis_name:
                    if axis.interval.low < low:
                        low = axis.interval.low
                    if axis.interval.low > high:
                        high = axis.interval.low
                    if axis.interval.high is not None and axis.interval.high > high:
                        high = axis.interval.high
                    resolution = axis.resolution
        grid_low = 0
        grid_high = math.fabs(math.floor((high - low) / resolution))

        # NOTE: Grid Coverage uses the direct intervals as in Rasdaman, modify the high bound will have error in petascope
        if not self.grid_coverage:
            if grid_high > grid_low:
                grid_high -= 1
        return low, high, low, int(grid_low), int(grid_high), resolution

    def _low_high_origin_date(self, messages, axis_name):
        """
        Returns the low, high and the origin for an axis in the context of a grib file
        :param list[GRIBMessage] messages: a list of messages represented by a list of user axes
        :param str axis_name: the axis name
        :rtype: (float, float, float, int, int, float)
        """
        low, high, resolution = datetime(MAXYEAR, 1, 1), datetime(MINYEAR, 1, 1), None
        grid_high = 0
        for message in messages:
            for axis in message.axes:
                if axis.name == axis_name:
                    date_low = parse(axis.interval.low)
                    if date_low < low:
                        low = date_low
                    if date_low > high:
                        high = date_low
                    if axis.interval.high:
                        date_high = parse(axis.interval.high)
                        if date_high > high:
                            high = date_high
                    resolution = axis.resolution
                    grid_high += 1
        grid_low = 0
        if grid_high > grid_low:
            grid_high -= 1
        return stringify(low.isoformat()), stringify(high.isoformat()), stringify(
            low.isoformat()), int(grid_low), int(grid_high), resolution

    def _low_high_origin(self, messages, axis_name, is_numeric):
        """
        Returns the low, high and origin for an axis name alongside the grid low and high
        :param messages: the messages for which to compute
        :param axis_name: the axis name
        :param is_numeric: if the axis expects numeric input or string input (ansidate)
        :rtype: (float | str, float | str, float | str, int, int, float)
        """
        if is_numeric:
            return self._low_high_origin_numeric(messages, axis_name)
        else:
            return self._low_high_origin_date(messages, axis_name)

    def _messages_to_dict(self, messages):
        """
        Converts a list of messages to json friendly data structure
        :param list[GRIBMessage] messages: the messages to convert
        :rtype: list[dict]
        """
        out_messages = []
        for message in messages:
            out_messages.append(message.to_json())
        return out_messages

    def _metadata(self):
        """
        Returns the metadata in the corresponding format indicated in the converter
        :rtype: str
        """
        if not self.local_metadata_fields and not self.global_metadata_fields:
            return ""
        serializer = ExtraMetadataSerializerFactory.get_serializer(self.metadata_type)
        metadata_entries = []
        for grib_file in self.grib_files:
            for message in self._messages(grib_file):
                slice = GribMessageEvaluatorSlice(message.message, grib_file)
                metadata_entry_subsets = []
                for axis in message.axes:
                    metadata_entry_subsets.append(ExtraMetadataSliceSubset(axis.name, axis.interval))
                metadata_entries.append(ExtraMetadataEntry(slice, metadata_entry_subsets))

        collector = ExtraMetadataCollector(self.sentence_evaluator,
                                           ExtraMetadataIngredientInformation(self.global_metadata_fields,
                                                                              self.local_metadata_fields),
                                           metadata_entries)
        return serializer.serialize(collector.collect())

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
                geo_axis = IrregularAxis(crs_axis.label, crs_axis.uom, low, high, origin, user_axis.directPositions,
                                         crs_axis)
            else:
                geo_axis = RegularAxis(crs_axis.label, crs_axis.uom, low, high, origin, crs_axis)
            grid_axis = GridAxis(user_axis.order, crs_axis.label, resolution, grid_low, grid_high)
            if crs_axis.is_easting():
                geo_axis.origin = geo_axis.low + resolution / 2
            elif crs_axis.is_northing():
                geo_axis.origin = geo_axis.high + resolution / 2
            axis_subsets.append(AxisSubset(CoverageAxis(geo_axis, grid_axis, user_axis.dataBound), Interval(low, high)))
        return Slice(axis_subsets, FileDataProvider(grib_file, self._messages_to_dict(messages), self.MIMETYPE))

    def _get_user_axis(self, message, axis_name):
        """
        Returns the user axis given the crs axis name
        :param GRIBMessage message: the grib message to extract the axis from
        :param str axis_name: the axis name
        :rtype: UserAxis
        """
        for axis in message.axes:
            if axis.name == axis_name:
                return axis

    def _slices(self, crs_axes):
        """
        Returns all the slices for this coverage
        :param crs_axes:
        :rtype: list[Slice]
        """
        slices = []
        for grib_file in self.grib_files:
            slices.append(self._slice(grib_file, crs_axes))
        return slices

    def to_coverage(self):
        """
        Returns the grib files as a coverage
        :rtype: Coverage
        """
        crs_axes = CRSUtil(self.crs).get_axes()
        range_field = RangeTypeField(self.band.name, self.band.definition, self.band.description,
                                     self._get_null_value())
        coverage = Coverage(self.coverage_id, self._slices(crs_axes), [range_field], self.crs, self.DEFAULT_DATA_TYPE,
                            self.tiling, self._metadata())
        return coverage
