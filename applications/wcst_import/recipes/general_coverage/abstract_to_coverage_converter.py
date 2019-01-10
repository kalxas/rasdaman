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
from master.extra_metadata.extra_metadata import GlobalExtraMetadata
from master.extra_metadata.extra_metadata_slice import ExtraMetadataSliceSubset

from util.gdal_util import GDALGmlUtil
from util.list_util import sort_slices_by_datetime

from master.evaluator.evaluator_slice_factory import EvaluatorSliceFactory
from master.evaluator.grib_expression_evaluator import GribExpressionEvaluator
from util.time_util import DateTimeUtil
from master.helper.user_axis import UserAxisType
from master.helper.irregular_user_axis import IrregularUserAxis
from master.helper.regular_user_axis import RegularUserAxis
from master.helper.user_axis import UserAxis
from master.extra_metadata.extra_metadata_collector \
    import ExtraGlobalMetadataCollector, ExtraLocalMetadataCollector, ExtraMetadataEntry
from master.extra_metadata.extra_metadata_ingredient_information \
    import ExtraGlobalMetadataIngredientInformation, ExtraLocalMetadataIngredientInformation
from master.extra_metadata.extra_metadata_serializers import ExtraMetadataSerializerFactory
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
import time

from util.timer_util import Timer


class AbstractToCoverageConverter:

    # if irregular axis for netcdf, grib is from fileName (e.g: datetime) so the file is a slicing on this axis
    DIRECT_POSITIONS_SLICING = [None]
    COEFFICIENT_SLICING = [0]

    IMPORT_ORDER_ASCENDING = "ascending"
    IMPORT_ORDER_DESCENDING = "descending"

    def __init__(self, resumer, recipe_type, sentence_evaluator, import_order):
        """
        Abstract class capturing common functionality between coverage converters.
        :param String recipe_type: the type of recipe (gdal|grib|netcdf)
        :param SentenceEvaluator sentence_evaluator: the evaluator for wcst sentences
        :param import_order: ascending(default), descending if specified in ingredient file
        """
        self.resumer = resumer
        self.recipe_type = recipe_type
        self.sentence_evaluator = sentence_evaluator
        self.import_order = import_order
        self.coverage_slices = []

    def _user_axis(self, user_axis, evaluator_slice):
        """
        Returns an evaluated user axis from a user supplied axis
        The user supplied axis contains for each attribute an expression that can be evaluated.
         We need to return a user axis that contains the actual values derived from the expression evaluation
        :param UserAxis | IrregularUserAxis user_axis: the user axis to evaluate
        :param evaluator_slice: the sentence evaluator for the slice
        :rtype: UserAxis | IrregularUserAxis
        """
        if isinstance(user_axis, IrregularUserAxis):
            if not user_axis.dataBound and user_axis.directPositions != self.DIRECT_POSITIONS_SLICING:
                raise RuntimeException("The dataBound option is set to false for irregular axis '{}', "
                                       "so directPositions option can not be set in the ingredients file for this axis.".format(user_axis.name))

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

    def _generate_global_metadata(self, first_slice):
        """
        Factory method to generate global metadata defined in ingredient file to xml/json and ingest as
        WCS coverage's global extra metadata via WCS-T InsertCoverage request.
        :param Slice first_slice: First slice of coverage to be used to generate global metadata for a coverage.
        :return: str
        """
        if not self.global_metadata_fields \
            and not self.bands_metadata_fields and not self.axes_metadata_fields:
            return ""
        serializer = ExtraMetadataSerializerFactory.get_serializer(self.metadata_type)

        # get the evaluator for the current recipe_type (each recipe has different evaluator)
        evaluator_slice = EvaluatorSliceFactory.get_evaluator_slice(self.recipe_type, first_slice.data_provider.file)
        metadata_entry_subsets = []
        for axis in first_slice.axis_subsets:
            metadata_entry_subsets.append(ExtraMetadataSliceSubset(axis.coverage_axis.axis.label, axis.interval))

        global_extra_metadata_collector = ExtraGlobalMetadataCollector(
                                                 self.sentence_evaluator,
                                                 ExtraGlobalMetadataIngredientInformation(self.global_metadata_fields,
                                                                                    self.bands_metadata_fields,
                                                                                    self.axes_metadata_fields),
                                                 ExtraMetadataEntry(evaluator_slice, metadata_entry_subsets))

        global_extra_metadata = global_extra_metadata_collector.collect()
        metadata_str = serializer.serialize(global_extra_metadata)

        return metadata_str

    def _generate_local_metadata(self, axis_subsets, evaluator_slice):
        """
        Factory method to generate local metadata defined in ingredient file to xml/json and appended as
        WCS coverage's local extra metadata via WCS-T UpdateCoverage request.
        :param list[AxisSubset] axis_subsets: the position of this slice in the coverage represented through a list
        of axis subsets
        :param FileEvaluatorSlice evaluator_slice: evaluator for a specific recipe type (gdal/netcdf/grib)
        :return: str
        """
        if not self.local_metadata_fields:
            return ""

        serializer = ExtraMetadataSerializerFactory.get_serializer(self.metadata_type)

        metadata_entry_subsets = []
        for axis in axis_subsets:
            metadata_entry_subsets.append(ExtraMetadataSliceSubset(axis.coverage_axis.axis.label, axis.interval))

        # Each local metadata should have a reference to input file path which it fetched metadata from
        self.local_metadata_fields["fileReferenceHistory"] = evaluator_slice.file.filepath

        extra_local_metadata_collector = ExtraLocalMetadataCollector(
                                                self.sentence_evaluator,
                                                ExtraLocalMetadataIngredientInformation(self.local_metadata_fields),
                                                ExtraMetadataEntry(evaluator_slice, metadata_entry_subsets))

        local_extra_metadata = extra_local_metadata_collector.collect()
        metadata_str = serializer.serialize(local_extra_metadata)

        return metadata_str

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

                # e.g: test_float_4d contains a missing_value as "NaNf"
                if "-nan" in nil_value.lower():
                    nil_value = "-NaN"
                if "nan" in nil_value.lower():
                    nil_value = "NaN"

                if is_number(nil_value):
                    range_nils.append(RangeTypeNilValue(band.nilReason, nil_value))
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

    def _evaluate_swe_bands_metadata(self, slice_file, bands):
        """
        NOTE: These bands metadata are used to put in swe element when DescribeCoverage, which limits to few metadata:
        swe:description, swe:definition, swe:nilValue. swe:nilReason.
        Other bands metadata which are not supported by swe element are put in coverage's global metadata as an element.
        They can be used to add to output file which allowed to add band's metadata such as netCDF.

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

    def _create_coverage_slices(self, crs_axes, calculated_evaluator_slice=None, axis_resolutions=None):
        """
        Returns all the coverage slices for this coverage
        :param crs_axes:
        :rtype: list[Slice]
        """
        slices = []
        count = 1
        for file in self.files:
            # NOTE: don't process any previously imported file (recorded in *.resume.json)
            if not self.resumer.check_file_imported(file.filepath):
                timer = Timer()

                # print which file is analyzing
                FileUtil.print_feedback(count, len(self.files), file.filepath)

                evaluator_slice = None

                if calculated_evaluator_slice is None:
                    # get the evaluator for the current recipe_type (each recipe has different evaluator)
                    evaluator_slice = EvaluatorSliceFactory.get_evaluator_slice(self.recipe_type, file)
                else:
                    evaluator_slice = calculated_evaluator_slice

                if self.data_type is None:
                    self.data_type = evaluator_slice.get_data_type(self)
                coverage_slice = self._create_coverage_slice(file, crs_axes, evaluator_slice, axis_resolutions)

                timer.print_elapsed_time()

                slices.append(coverage_slice)
                count += 1

        # Currently, only sort by datetime to import coverage slices (default is ascending)
        reverse = (self.import_order == self.IMPORT_ORDER_DESCENDING)
        return sort_slices_by_datetime(slices, reverse)

    def _create_coverage_slice(self, file, crs_axes, evaluator_slice, axis_resolutions=None):
        """
        Returns a coverage slice for a file
        :param File file: the path to the importing file
        :param list[CRSAxis] crs_axes: the crs axes for the coverage
        :param FileEvaluatorSlice evaluator_slice: depend on kind of recipe (gdal/netcdf/grib) to pass corresponding evaluator
        :param list[number] axis_resolutions: if they are known values (e.g: in Sentinel 2 recipes: 10m, 20m, 60m),
                                              just keep them for calculating grid bounds.
        :rtype: Slice
        """
        axis_subsets = []
        file_structure = self._file_structure()

        for i in range(0, len(crs_axes)):
            resolution = None
            if axis_resolutions is not None:
                resolution = axis_resolutions[i]

            axis_subset = self._axis_subset(crs_axes[i], evaluator_slice, resolution)
            axis_subsets.append(axis_subset)

        # Generate local metadata string for current coverage slice
        local_metadata = self._generate_local_metadata(axis_subsets, evaluator_slice)

        return Slice(axis_subsets, FileDataProvider(file, file_structure), local_metadata)

    def to_coverage(self, coverage_slices=None):
        """
        Returns a Coverage from all the importing files (gdal|grib|netcdf)
        :rtype: Coverage
        """
        crs_axes = CRSUtil(self.crs).get_axes()

        if coverage_slices is None:
            # Build list of coverage slices from input files
            coverage_slices = self._create_coverage_slices(crs_axes)

        first_coverage_slice = coverage_slices[0]
        # generate coverage extra_metadata from ingredient file based on first input file of first coverage slice.
        global_metadata = self._generate_global_metadata(first_coverage_slice)

        # Evaluate all the swe bands's metadata (each file should have same swe bands's metadata), so first file is ok
        self._evaluate_swe_bands_metadata(self.files[0], self.bands)

        coverage = Coverage(self.coverage_id, coverage_slices, self._range_fields(), self.crs,
                            self._data_type(),
                            self.tiling, global_metadata)
        return coverage
