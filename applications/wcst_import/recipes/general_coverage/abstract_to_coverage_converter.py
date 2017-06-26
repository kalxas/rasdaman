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

import decimal
import math
from lib import arrow
from util.list_util import sort_slices_by_datetime

from master.evaluator.evaluator_slice_factory import EvaluatorSliceFactory
from master.evaluator.grib_expression_evaluator import GribExpressionEvaluator
from util.time_util import DateTimeUtil
from master.helper.user_axis import UserAxisType
from master.helper.irregular_user_axis import IrregularUserAxis
from master.helper.regular_user_axis import RegularUserAxis
from master.helper.user_axis import UserAxis
from master.extra_metadata.extra_metadata_collector import ExtraMetadataCollector, ExtraMetadataEntry
from master.extra_metadata.extra_metadata_ingredient_information import ExtraMetadataIngredientInformation
from master.extra_metadata.extra_metadata_serializers import ExtraMetadataSerializerFactory
from master.extra_metadata.extra_metadata_slice import ExtraMetadataSliceSubset
from master.generator.model.range_type_nill_value import RangeTypeNilValue
from master.generator.model.range_type_field import RangeTypeField
from master.error.runtime_exception import RuntimeException
from master.evaluator.sentence_evaluator import SentenceEvaluator
from util.log import log
from util.crs_util import CRSUtil
from master.importer.coverage import Coverage
from master.importer.slice import Slice
from master.provider.data.file_data_provider import FileDataProvider
from util.string_util import is_number
from util.file_util import FileUtil


class AbstractToCoverageConverter:

    # if irregular axis for netcdf, grib is from fileName (e.g: datetime) so the file is a slicing on this axis
    DIRECT_POSITIONS_SLICING = [None]
    COEFFICIENT_SLICING = [0]

    def __init__(self, recipe_type, sentence_evaluator):
        """
        Abstract class capturing common functionality between coverage converters.
        :param String recipe_type: the type of recipe (gdal|grib|netcdf)
        :param SentenceEvaluator sentence_evaluator: the evaluator for wcst sentences
        """
        self.recipe_type = recipe_type
        self.sentence_evaluator = sentence_evaluator

    def _user_axis(self, user_axis, evaluator_slice):
        """
        Returns an evaluated user axis from a user supplied axis
        The user supplied axis contains for each attribute an expression that can be evaluated.
         We need to return a user axis that contains the actual values derived from the expression evaluation
        :param UserAxis | IrregularUserAxis user_axis: the user axis to evaluate
        :param evaluator_slice: the sentence evaluator for the slice
        :rtype: UserAxis | IrregularUserAxis
        """
        min = self.sentence_evaluator.evaluate(user_axis.interval.low, evaluator_slice, user_axis.statements)
        max = None
        if user_axis.interval.high:
            max = self.sentence_evaluator.evaluate(user_axis.interval.high, evaluator_slice, user_axis.statements)
        resolution = self.sentence_evaluator.evaluate(user_axis.resolution, evaluator_slice, user_axis.statements)
        if isinstance(user_axis, RegularUserAxis):
            return RegularUserAxis(user_axis.name, resolution, user_axis.order, min, max, user_axis.type,
                                   user_axis.dataBound)
        else:
            if GribExpressionEvaluator.FORMAT_TYPE in user_axis.directPositions:
                # grib irregular axis will be calculated later when all the messages is evaluated
                direct_positions = user_axis.directPositions
            else:
                direct_positions = self.sentence_evaluator.evaluate(user_axis.directPositions, evaluator_slice, user_axis.statements)

            return IrregularUserAxis(user_axis.name, resolution, user_axis.order, min, direct_positions, max,
                                     user_axis.type, user_axis.dataBound)

    def _translate_number_direct_position_to_coefficients(self, origin, direct_positions):
        # just translate 1 -> 1 as origin is 0 (e.g: irregular Index1D)
        if direct_positions == self.DIRECT_POSITIONS_SLICING:
            return self.COEFFICIENT_SLICING
        else:
            return map(lambda x: decimal.Decimal(str(x)) - decimal.Decimal(str(origin)), direct_positions)

    def _translate_seconds_date_direct_position_to_coefficients(self, origin, direct_positions):
        # just translate 1 -> 1 as origin is 0 (e.g: irregular UnixTime)
        if direct_positions == self.DIRECT_POSITIONS_SLICING:
            return self.COEFFICIENT_SLICING
        else:
            return map(lambda x: (decimal.Decimal(str(arrow.get(x).float_timestamp)) - decimal.Decimal(str(origin))), direct_positions)

    def _translate_day_date_direct_position_to_coefficients(self, origin, direct_positions):
        # coefficients in AnsiDate (day) -> coefficients in UnixTime (seconds)
        coeff_list = []
        # as irregular axis gotten from fileName so it is slicing
        if direct_positions == self.DIRECT_POSITIONS_SLICING:
            return self.COEFFICIENT_SLICING
        else:
            for coeff in direct_positions:
                # (current_datetime in seconds - origin in seconds) / 86400
                coeff_seconds = ((decimal.Decimal(str(arrow.get(coeff).float_timestamp)) - decimal.Decimal(str(origin)))
                                 / decimal.Decimal(DateTimeUtil.DAY_IN_SECONDS))
                coeff_list.append(coeff_seconds)

            return coeff_list

    def _translate_decimal_to_datetime(self, user_axis, geo_axis):
        """
        DateTime axis must be translated from seconds to ISO format
        :param User_Axis user_axis: the dateTime user axis which needs to be translated
        :param Regular/Irregular geo_axis: the dateTime axis which needs to be translated
        """
        if user_axis.type == UserAxisType.DATE:
            geo_axis.origin = DateTimeUtil.get_datetime_iso(geo_axis.origin)
            geo_axis.low = DateTimeUtil.get_datetime_iso(geo_axis.low)

            if geo_axis.high is not None:
                geo_axis.high = DateTimeUtil.get_datetime_iso(geo_axis.high)

            user_axis.interval.low = DateTimeUtil.get_datetime_iso(user_axis.interval.low)
            if user_axis.interval.high is not None:
                user_axis.interval.high = DateTimeUtil.get_datetime_iso(user_axis.interval.high)

    def _get_user_axis_by_crs_axis_name(self, crs_axis_name):
        """
        Returns the user axis from the list by crs_axis_name
        :param string crs_axis_name: the name of the crs axis to retrieve
        :rtype: UserAxis
        """
        for user_axis in self.user_axes:
            if user_axis.name == crs_axis_name:
                return user_axis

    def _get_crs_axis_by_user_axis_name(self, user_axis_name):
        """
        Returns the crs axis from the list by user_axis_name
        :param user_axis_name:
        :return: crs_axis
        """
        crs_axes = CRSUtil(self.crs).get_axes()

        for i in range(0, len(crs_axes)):
            crs_axis = crs_axes[i]
            if crs_axis.label == user_axis_name:

                return crs_axis

    def _file_structure(self):
        """
        Returns the file structure (i.e: the list of all defined bands in ingredient files)
        :rtype: dict
        """
        file_structure = {}
        variables = []
        for band in self.bands:
            variables.append(str(band.identifier))
        if len(variables) > 0:
            file_structure["variables"] = variables
        return file_structure

    def _metadata(self, slices):
        """
        Factory method to evaluate the metadata defined in ingredient file to xml/json and ingest as extra metadata
        in DescribeCoverage WCS request
        :param list[Slice] slices: the slices of the coverage which needs the metadata from ingredient files
        :return: str
        """
        if not self.global_metadata_fields and not self.local_metadata_fields:
            return ""
        serializer = ExtraMetadataSerializerFactory.get_serializer(self.metadata_type)
        metadata_entries = []
        for coverage_slice in slices:
            # get the evaluator for the current recipe_type (each recipe has different evaluator)
            evaluator_slice = EvaluatorSliceFactory.get_evaluator_slice(self.recipe_type, coverage_slice.data_provider.file)
            metadata_entry_subsets = []
            for axis in coverage_slice.axis_subsets:
                metadata_entry_subsets.append(ExtraMetadataSliceSubset(axis.coverage_axis.axis.label, axis.interval))
            metadata_entries.append(ExtraMetadataEntry(evaluator_slice, metadata_entry_subsets))

        collector = ExtraMetadataCollector(self.sentence_evaluator,
                                           ExtraMetadataIngredientInformation(self.global_metadata_fields,
                                                                              self.local_metadata_fields),
                                           metadata_entries)
        return serializer.serialize(collector.collect())

    def _get_nil_values(self, index):
        """
        Returns the null values for a band of coverage by band index
        :param integer index: the current band index to get nilValues
        :rtype: list[RangeTypeNilValue]
        """
        if len(self.bands) < 0:
            raise RuntimeException("At least one band should be provided.")
        band = self.bands[index]
        # if nilValues was defined in ingredient file, then fetch values from the user_band
        valid_user_nil = band.nilValues is not None and len(band.nilValues) > 0 and band.nilValues[0] != ''
        if valid_user_nil is True:
            nil_values = band.nilValues
        else:
            # if nilValues was not defined in the ingredient file, then it will fetch from file's band's metadata
            nil_values = self._file_band_nil_values(index)

        if nil_values is not None:
            range_nils = []
            # This should contain only 1 nilValue for 1 band
            for nil_value in nil_values:
                # null_value could be 9999 (gdal), "9999", or "'9999'" (netcdf, grib) so remove the redundant quotes
                nil_value = str(nil_value).replace("'", "")
                # check if nil_value is an integer or float number (NaN is float number but it is not valid)
                if nil_value.lower().startswith("nan"):
                    log.info("\033[1mBand: {} has NilValue: \x1b[0m {}, "
                             "so ignore nilValue of this band.".format(band.identifier, nil_value))
                elif is_number(nil_value):
                    # NOTE: as rasdaman does not support nilValue is float (e.g: 1234.22323), so consider
                    # nilValue are floor(value), ceil(value): 1234:1235
                    floor_nill_value = int(math.floor(float(nil_value)))
                    ceil_nill_value = int(math.ceil(float(nil_value)))

                    if ceil_nill_value > 9223372036854775807 or floor_nill_value < -9223372036854775808:
                        log.info("\033[1mThe nodata value {} of band {} has been ignored "
                             "as it cannot be represented as a 64 bit integer.".format(nil_value, band.identifier))
                        return None

                    # as nilValue is integer already so only 1 nilValue
                    if floor_nill_value == ceil_nill_value:
                        range_nils.append(RangeTypeNilValue(band.nilReason, floor_nill_value))
                    else:
                        # or it will add both 2 integers as nill Values
                        range_nils.append(RangeTypeNilValue(band.nilReason, floor_nill_value))
                        range_nils.append(RangeTypeNilValue(band.nilReason, ceil_nill_value))
                else:
                    # nilValue is invalid number
                    raise RuntimeException("NilValue of band: {} must be a number.".format(nil_value))

            return range_nils
        else:
            # If files does not have any nilValues for bands, return None
            return None

    def _range_fields(self):
        """
        Returns the range fields for the coverage
        :rtype: list[RangeTypeField]
        """
        range_fields = []
        i = 0
        for band in self.bands:
            # NOTE: each range (band) should contain only 1 nilValue, e.g: [-99999]
            range_nils = self._get_nil_values(i)
            range_fields.append(RangeTypeField(band.name, band.definition, band.description, band.nilReason,
                                               range_nils, band.uomCode))
            i += 1

        return range_fields

    def _evaluate_bands_metadata(self, slice_file, bands):
        """
        Translate the metadata variable in bands (ranges) in ingredient file to values from the file
        e.g: ${grib:missingValue} -> 99999
        :param File slice_file: the current file which is used to evaluate for metadata of bands
        :param List[UserBand] bands: the list of bands need to be evaluated
        :return:
        """
        for band in bands:
            # List all attributes of object and evaluate them if they are defined with varible format (${...})
            attr_list = vars(band).items()
            for key_value in attr_list:
                # e.g: "description"
                key = key_value[0]
                # e.g: "'${grib:marsClass}'"
                value = key_value[1]
                # evaluate this metadata variable by eval() on the slice_file (all files should have same metadata)
                evaluator_slice = EvaluatorSliceFactory.get_evaluator_slice(self.recipe_type, slice_file)
                evaluated_value = self.sentence_evaluator.evaluate(value, evaluator_slice)
                # after that, the band's metadata for the current attribute is evaluated
                setattr(band, key, evaluated_value)

    def _slices(self, crs_axes):
        """
        Returns all the slices for this coverage
        :param crs_axes:
        :rtype: list[Slice]
        """
        slices = []
        count = 1
        for file in self.files:
            # print which file is analyzing
            FileUtil.print_feedback(count, len(self.files), file.filepath)
            slices.append(self._slice(file, crs_axes))
            count += 1
        # NOTE: we want to sort all the slices by date time axis
        # to avoid the case the later time slice is added before the sooner time slice
        sorted_slices = sort_slices_by_datetime(slices)

        return sorted_slices

    def _slice(self, file, crs_axes):
        """
        Returns a slice for a file
        :param File file: the path to the importing file
        :param list[CRSAxis] crs_axes: the crs axes for the coverage
        :rtype: Slice
        """
        axis_subsets = []
        file_structure = self._file_structure()
        for i in range(0, len(crs_axes)):
            axis_subsets.append(self._axis_subset(crs_axes[i], file))

        return Slice(axis_subsets, FileDataProvider(file, file_structure))

    def to_coverage(self):
        """
        Returns a Coverage from all the importing files (gdal|grib|netcdf)
        :rtype: Coverage
        """
        crs_axes = CRSUtil(self.crs).get_axes()
        slices = self._slices(crs_axes)
        # generate coverage extra_metadata from ingredient files
        metadata = self._metadata(slices)
        # Evaluate all the bands's metadata (each file should have same bands' metadata), so first file is ok
        self._evaluate_bands_metadata(self.files[0], self.bands)

        coverage = Coverage(self.coverage_id, slices, self._range_fields(), self.crs,
                            self._data_type(),
                            self.tiling, metadata)
        return coverage
