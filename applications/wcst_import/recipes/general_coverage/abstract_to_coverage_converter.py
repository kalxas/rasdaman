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

import decimal
import math
from abc import abstractmethod
from collections import OrderedDict

from lib import arrow
from master.error.validate_exception import RecipeValidationException
from master.extra_metadata.extra_metadata_slice import ExtraMetadataSliceSubset
from util.coverage_util import CoverageUtil, CoverageUtilCache
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
from util.crs_util import CRSUtil
from master.importer.coverage import Coverage
from master.importer.slice import Slice
from master.provider.data.file_data_provider import FileDataProvider
from util.string_util import is_number, create_coverage_id_for_overview
from util.file_util import FileUtil
from util.timer import Timer
import copy


class AbstractToCoverageConverter:

    # if irregular axis for netcdf, grib is from fileName (e.g: datetime) so the file is a slicing on this axis
    DIRECT_POSITIONS_SLICING = [None]
    COEFFICIENT_SLICING = [0]

    IMPORT_ORDER_ASCENDING = "ascending"
    IMPORT_ORDER_DESCENDING = "descending"
    # no sorts by file paths / timeseries
    IMPORT_ORDER_NONE = "none"

    analyzed_files_count = 0

    # A dictionary of irregular axes and their geo lower bounds
    irregular_axis_geo_lower_bound_dict = {}

    def __init__(self, resumer, recipe_type, sentence_evaluator, import_order, session):
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
        self.coverage_slices = OrderedDict()
        self.session = session
        self.evaluator_slice = None

        self.user_axes = None
        self.crs = None
        self.coverage_id = None
        self.bands = None
        self.global_metadata_fields = None
        self.bands_metadata_fields = None
        self.axes_metadata_fields = None
        self.local_metadata_fields = None
        self.metadata_type = None
        self.files = None
        self.tiling = None
        self.data_type = None

    # Override by subclasses
    @abstractmethod
    def _file_band_nil_values(self, index):
        pass

    @abstractmethod
    def _axis_subset(self, crs_axis, evaluator_slice, resolution=None):
        pass

    def _data_type(self):
        return self.data_type

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
            direct_positions = user_axis.directPositions
            if GribExpressionEvaluator.FORMAT_TYPE not in user_axis.directPositions:
                if user_axis.directPositions != AbstractToCoverageConverter.DIRECT_POSITIONS_SLICING:
                    direct_positions = self.sentence_evaluator.evaluate(user_axis.directPositions, evaluator_slice, user_axis.statements)

            return IrregularUserAxis(user_axis.name, resolution, user_axis.order, min, direct_positions, max,
                                     user_axis.type, user_axis.dataBound, [], user_axis.slice_group_size)

    def _translate_number_direct_position_to_coefficients(self, origin, direct_positions):
        # just translate 1 -> 1 as origin is 0 (e.g: irregular Index1D)
        if direct_positions == self.DIRECT_POSITIONS_SLICING:
            return self.COEFFICIENT_SLICING
        else:
            return [decimal.Decimal(str(x)) - decimal.Decimal(str(direct_positions[0])) for x in direct_positions]

    def _translate_seconds_date_direct_position_to_coefficients(self, origin, direct_positions):
        # just translate 1 -> 1 as origin is 0 (e.g: irregular UnixTime)
        if direct_positions == self.DIRECT_POSITIONS_SLICING:
            return self.COEFFICIENT_SLICING
        else:
            return [(decimal.Decimal(str(arrow.get(x).float_timestamp)) - decimal.Decimal(str(origin))) for x in direct_positions]

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

    def _adjust_irregular_axis_geo_lower_bound(self, coverage_id, axis_label, axis_type, is_day_unit, axis_geo_lower_bound, slice_group_size):
        """
        (!) Use only if irregular axis's dataBound is False, then adjust it's geo lower bound by current coverage's axis lower bound.
        e.g: coverage's axis lower bound is '2015-01-10' and slice_group_size = 5 then irregular axis's lower bound
        from '2015-01-08' or '2015-01-13' will be adjusted to '2015-01-10'.

        :param int slice_group_size: a positive integer
        """

        resolution = IrregularUserAxis.DEFAULT_RESOLUTION

        if axis_label not in self.irregular_axis_geo_lower_bound_dict:
            # Need to parse it from coverage's DescribeCoverage result
            cov = CoverageUtilCache.get_cov_util(coverage_id)
            if cov.exists():
                axes_labels = cov.get_axes_labels()

                # Get current coverage's axis geo lower bound
                i = axes_labels.index(axis_label)

                lower_bounds = cov.get_axes_lower_bounds()
                coverage_geo_lower_bound = lower_bounds[i]

                # Translate datetime format to seconds
                if axis_type == UserAxisType.DATE:
                    coverage_geo_lower_bound = arrow.get(coverage_geo_lower_bound).float_timestamp
                    axis_geo_lower_bound = arrow.get(axis_geo_lower_bound).float_timestamp
            else:
                # Coverage does not exist, first InsertCoverage request
                self.irregular_axis_geo_lower_bound_dict[axis_label] = axis_geo_lower_bound

                return axis_geo_lower_bound
        else:
            coverage_geo_lower_bound = self.irregular_axis_geo_lower_bound_dict[axis_label]

        if is_day_unit:
            # AnsiDate (8600 seconds / day)
            slice_group_size = slice_group_size * DateTimeUtil.DAY_IN_SECONDS

        # e.g: irregular axis's level with current coverage's lower bound is 5, axis's lower bound is 12
        # and sliceGroupSize = 5. Result is: (12 - 5) / (1 * 5) = 1
        distance = math.floor((axis_geo_lower_bound - coverage_geo_lower_bound) / (resolution * slice_group_size))

        # So the adjusted value is 5 + 1 * 1 * 5 = 10
        adjusted_bound = coverage_geo_lower_bound + (distance * resolution * slice_group_size)

        if adjusted_bound < coverage_geo_lower_bound:
            # Save this one to cache for further processing
            self.irregular_axis_geo_lower_bound_dict[axis_label] = adjusted_bound

        return adjusted_bound

    def _update_for_slice_group_size(self, coverage_id, user_axis, crs_axis, coefficients):
        """
        In case an irregular axis is used with "sliceGroupSize" then need to update its geo lower bound.

        :param UserAxis user_axis
        :param CRSAxis crs_axis
        :param List[Decimal] coefficients
        """
        slice_group_size = user_axis.slice_group_size
        if slice_group_size is not None and coefficients == self.COEFFICIENT_SLICING:
            # In case dataBound:false (e.g: datetime from filename), GML of coverage to UpdateCoverage request
            # doesn't contain coefficient. So, update lower bound of irregular axis instead.
            user_axis.interval.low = self._adjust_irregular_axis_geo_lower_bound(coverage_id, crs_axis.label,
                                                                                 user_axis.type,
                                                                                 crs_axis.is_time_day_axis(),
                                                                                 user_axis.interval.low,
                                                                                 slice_group_size)

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
        crs_axes = CRSUtil(self.crs).get_axes(self.coverage_id)

        for i in range(0, len(crs_axes)):
            crs_axis = crs_axes[i]
            if crs_axis.label == user_axis_name:

                return crs_axis

    def _file_structure(self):
        """
        Returns the file structure (i.e: the list of all defined bands in ingredient files)
        :rtype: dict
        """
        from recipes.general_coverage.gdal_to_coverage_converter import GdalToCoverageConverter

        file_structure = {}
        variables = []
        for band in self.bands:
            identifier = band.identifier

            if self.recipe_type == GdalToCoverageConverter.RECIPE_TYPE:
                if identifier.isdigit():
                    identifier = int(band.identifier)

            variables.append(identifier)
        if len(variables) > 0:
            file_structure["variables"] = variables
        return file_structure

    def _generate_global_metadata(self, first_slice, evaluator_slice=None):
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

        if not evaluator_slice is None:
            self.evaluator_slice = evaluator_slice

        # get the evaluator for the current recipe_type (each recipe has different evaluator)
        metadata_entry_subsets = []
        for axis in first_slice.axis_subsets:
            metadata_entry_subsets.append(ExtraMetadataSliceSubset(axis.coverage_axis.axis.label, axis.interval))

        global_extra_metadata_collector = ExtraGlobalMetadataCollector(
                                                 self.sentence_evaluator,
                                                 ExtraGlobalMetadataIngredientInformation(self.global_metadata_fields,
                                                                                          self.bands_metadata_fields,
                                                                                          self.axes_metadata_fields),
                                                 ExtraMetadataEntry(self.evaluator_slice, metadata_entry_subsets))

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

    def __validate_null_values(self, input_nil_value):
        tmp = [input_nil_value]
        if isinstance(input_nil_value, list):
            tmp = input_nil_value

        for nil_value in tmp:
            # null_value could be 9999 (gdal), "9999", or "'9999'" (netcdf, grib) so remove the redundant quotes
            nil_value = str(nil_value).replace("'", "")

            # nill value can be single (e.g: "-9999") or interval (e.g: "-9999:-9998")
            values = nil_value.split(":")

            for value in values:
                if "-nan" in value.lower():
                    value = "-NaN"
                if "nan" in value.lower():
                    value = "NaN"

                if not (value == "*" or is_number(value)):
                    # nilValue is invalid number
                    raise RuntimeException("NilValue of band: {} is not valid.".format(nil_value))

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
            # nil_values = [[2,3], [1], [4]] or [999:9999, 999:*, 20]
            has_all_nested_lists = []

            for nil_value in nil_values:
                has_all_nested_lists.append(isinstance(nil_value, list))

            if True in has_all_nested_lists and False in has_all_nested_lists:
                # e.g. [ [2,3], 4, [5,7] ]
                raise RecipeValidationException("default_null_values setting must contain a list for each band.")

            if True in has_all_nested_lists:
                # default_null_values contains list of null values per band
                nil_values = nil_values[index]

            for nil_value in nil_values:
                self.__validate_null_values(nil_value)

                range_nils.append(RangeTypeNilValue(band.nilReason, nil_value))

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
                evaluated_value = self.sentence_evaluator.evaluate(value, self.evaluator_slice)
                # after that, the band's metadata for the current attribute is evaluated
                setattr(band, key, evaluated_value)

    def _create_coverage_slices(self, coverage_crs, crs_axes, calculated_evaluator_slice=None, axis_resolutions=None):
        """
        Returns the slices for the collection of files given
        :param crs_axes:
        :rtype: list[Slice]
        """
        from master.recipe.base_recipe import BaseRecipe
        slices_dict = BaseRecipe.create_dict_of_slices(self.session.import_overviews)

        count = 1
        for file in self.files:
            timer = Timer()

            if self.session.blocking is True:
                FileUtil.print_feedback(count, len(self.files), file.filepath)
            else:
                self.analyzed_files_count += 1
                FileUtil.print_feedback(self.analyzed_files_count, self.session.total_files_to_import, file.filepath)

            # print which file is analyzing
            if not FileUtil.validate_file_path(file.filepath):
                continue

            valid_coverage_slice = True

            try:
                if calculated_evaluator_slice is None:
                    # get the evaluator for the current recipe_type (each recipe has different evaluator)
                    self.evaluator_slice = EvaluatorSliceFactory.get_evaluator_slice(self.recipe_type, file)
                else:
                    self.evaluator_slice = calculated_evaluator_slice

                if self.data_type is None:
                    self.data_type = self.evaluator_slice.get_data_type(self)

                coverage_slice = self._create_coverage_slice(file, crs_axes, self.evaluator_slice, axis_resolutions)
            except Exception as ex:
                # If skip: true then just ignore this file from importing, else raise exception
                FileUtil.ignore_coverage_slice_from_file_if_possible(file.get_filepath(), ex)
                valid_coverage_slice = False

            if valid_coverage_slice:
                if self.session.recipe["options"]["coverage"]["slicer"]["type"] == "gdal":
                    gdal_file = GDALGmlUtil(file.get_filepath())
                    geo_axis_crs = gdal_file.get_crs()
                    try:
                        CRSUtil.validate_crs(coverage_crs, geo_axis_crs)
                    except Exception as ex:
                        FileUtil.ignore_coverage_slice_from_file_if_possible(file.get_filepath(), ex)
                        valid_coverage_slice = False

                    if valid_coverage_slice:

                        if self.session.import_overviews_only is False:
                            slices_dict["base"].append(coverage_slice)

                        # Then, create slices for selected overviews from user
                        for overview_index in self.session.import_overviews:
                            axis_subsets_overview = BaseRecipe.create_subsets_for_overview(coverage_slice.axis_subsets,
                                                                                           overview_index, gdal_file)

                            coverage_slice_overview = copy.deepcopy(coverage_slice)
                            coverage_slice_overview.axis_subsets = axis_subsets_overview

                            slices_dict[str(overview_index)].append(coverage_slice_overview)
                else:
                    if self.session.import_overviews_only is False:
                        slices_dict["base"].append(coverage_slice)

            timer.print_elapsed_time()
            count += 1

        # Currently, only sort by datetime to import coverage slices (default is ascending)
        if self.import_order != self.IMPORT_ORDER_NONE:
            reverse = (self.import_order == self.IMPORT_ORDER_DESCENDING)
            for key, value in slices_dict.items():
                slices_dict[key] = sort_slices_by_datetime(value, reverse)

        return slices_dict

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

    def to_coverages(self, coverage_slices_dict=None):
        """
        Returns list coverages from all the importing files (gdal|grib|netcdf)
        :rtype: Array[Coverage]
        """
        crs_axes = CRSUtil(self.crs).get_axes(self.coverage_id)

        if coverage_slices_dict is None:
            # Build list of coverage slices from input files
            coverage_slices_dict = self._create_coverage_slices(self.crs, crs_axes)

        if len(coverage_slices_dict) == 0 or len(list(coverage_slices_dict.values())[0]) == 0:
            return []

        global_metadata = None
        first_coverage_slice = None

        if len(coverage_slices_dict) > 0:
            for coverage_level, slices in coverage_slices_dict.items():
                if len(slices) > 0:
                    first_coverage_slice = slices[0]
                    break

            if first_coverage_slice is not None:
                # generate coverage extra_metadata from ingredient file based on first input file of first coverage slice.
                global_metadata = self._generate_global_metadata(first_coverage_slice)

        # Evaluate all the swe bands's metadata (each file should have same swe bands's metadata), so first file is ok
        self._evaluate_swe_bands_metadata(self.files[0], self.bands)

        results = []
        base_coverage_id = self.coverage_id
        for key, value in coverage_slices_dict.items():
            slices = coverage_slices_dict[key]
            if key == "base":
                coverage = Coverage(base_coverage_id, slices, self._range_fields(), self.crs,
                                    self._data_type(),
                                    self.tiling, global_metadata)
            else:
                # overview coverage (key = overview_index)
                coverage_id = create_coverage_id_for_overview(self.coverage_id, key)

                coverage = Coverage(coverage_id, slices, self._range_fields(), self.crs,
                                    self._data_type(),
                                    self.tiling, global_metadata, base_coverage_id, key)

            results.append(coverage)

        return results
