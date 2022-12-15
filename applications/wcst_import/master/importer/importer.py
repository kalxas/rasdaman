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
import os
import time
import json
import signal

from lib import arrow
from collections import OrderedDict
from time import sleep
from config_manager import ConfigManager
from master.importer.bbox import BBox
from master.importer.coverage import Coverage
from master.importer.slice import Slice
from master.importer.slice_restricter import SliceRestricter
from master.mediator.mediator import Mediator
from master.provider.data.tuple_list_data_provider import TupleListDataProvider
from master.provider.metadata.axis import Axis
from master.provider.metadata.grid_axis import GridAxis
from master.provider.metadata.irregular_axis import IrregularAxis
from master.provider.metadata.metadata_provider import MetadataProvider
from master.request.admin import InspireUpdateMetadataURLRequest
from util.coverage_util import CoverageUtil, CoverageUtilCache
from util.file_util import File
from util.import_util import decode_res
from util.log import log, prepend_time
from util.string_util import strip_trailing_zeros, create_downscaled_coverage_id, add_date_time_suffix
from util.url_util import validate_and_read_url
from master.request.wcst import WCSTInsertRequest, WCSTUpdateRequest, WCSTSubset
from master.request.pyramid import CreatePyramidMemberRequest, AddPyramidMemberRequest, ListPyramidMembersRequest
from util.crs_util import CRSUtil, CRSAxis
from util.time_util import DateTimeUtil


class Importer:

    # Check if coverage exist in Petascope
    coverage_exists_dict = {}

    DEFAULT_INSERT_VALUE = "0"

    processed_files_count = 0

    def __init__(self, resumer, coverage, insert_into_wms=False, scale_levels=None, grid_coverage=False, session=None, scale_factors=None):
        """
        Imports a coverage into wcst
        :param Coverage coverage: the coverage to be imported
        """
        self.coverage = coverage
        self.resumer = resumer

        slices = self.resumer.eliminate_already_imported_slices(self.coverage.slices)
        obj = SliceRestricter(slices)
        self.coverage.slices = obj.slices
        self.processed = 0
        self.total = len(coverage.slices)
        self.insert_into_wms = insert_into_wms
        self.scale_levels = scale_levels
        self.grid_coverage = grid_coverage
        self.session = session
        self.scale_factors = scale_factors

        self.current_data_provider = None

        self.__register_signal_handlers()

    def ingest(self):
        """
        Ingests the given coverage
        """
        if len(self.coverage.slices) > 0:
            if self._is_insert():
                self._initialize_coverage()
                Importer.coverage_exists_dict[self.coverage.coverage_id] = True

            # Insert the remaining slices
            self._insert_slices()

            if self.insert_into_wms:
                self._insert_update_into_wms()

            if self.coverage.base_coverage_id is not None \
                    and (self.session is not None and self.session.import_overviews_only is False):
                # for importing overviews nested inside input files
                self.add_pyramid_member_if_any(self.coverage.base_coverage_id, self.coverage.coverage_id,
                                               self.session.pyramid_harvesting)

            # NOTE: this request is invoked once whenever updating a coverage from input files
            if self.session is not None:
                self.update_coverage_inspire_metadata(self.coverage.coverage_id, self.session.inspire.metadata_url)

    def get_progress(self):
        """
        Returns the progress of the import
        :rtype: tuple
        """
        if self.total == 0:
            log.warn("No slices to import.")
            return -1, -1
        return self.processed, self.total

    def _insert_slice(self, current):
        """
        Inserts one slice
        :param Slice current: the slice to be imported
        """
        current_exception = None
        current_str = ""

        for attempt in range(0, ConfigManager.retries):
            self.current_data_provider = current.data_provider
            try:
                current_str = str(current)
                gml_obj = self._generate_gml_slice(current)

                subsets = self._get_update_subsets_for_slice(current)

                if ConfigManager.mock:
                    request = WCSTUpdateRequest(self.coverage.coverage_id, gml_obj.get_url(), subsets, ConfigManager.insitu, None)
                else:
                    file_path = None
                    if hasattr(current.data_provider, "file"):
                        file_path = current.data_provider.file.filepath

                    request = WCSTUpdateRequest(self.coverage.coverage_id, None, subsets, ConfigManager.insitu, gml_obj, file_path)

                executor = ConfigManager.executor
                executor.execute(request, mock=ConfigManager.mock)

                file_path = None

                if hasattr(current.data_provider, "file"):
                    file_path = current.data_provider.file.filepath
                bbox_dict = self.__get_bbox_dict(current.axis_subsets)

                if self.session is not None and file_path is not None:
                    self.session.IMPORTED_FILE_AXIS_BBOX_DICT[file_path] = bbox_dict

                self.resumer.add_imported_data(self.current_data_provider)

            except Exception as e:
                if ConfigManager.retry is True:
                    log.warn(
                        "\nException thrown when trying to insert slice: \n" + current_str + "Retrying, you can safely ignore the warning for now. Tried " + str(
                            attempt + 1) + " times.\n")
                    current_exception = e
                    sleep(ConfigManager.retry_sleep)
                    pass
                else:
                    raise e
            else:
                break
        else:
            log.warn("\nFailed to insert slice. Attempted " + str(ConfigManager.retries) + " times.")
            raise current_exception

    def __get_bbox_dict(self, axis_subsets):
        """
        Return the dict of bboxes (one bbox for one coverage's axis)
        :param List<AxisSubset> axis_subsets
        """
        bbox_dict = {}
        for axis_subset in axis_subsets:
            axis = axis_subset.coverage_axis.axis
            bbox = BBox(axis.label, axis.low, axis.high)
            bbox_dict[axis.label] = bbox

        return bbox_dict

    def get_slices_for_description(self):
        """
        Returns a list with the first slices to be used in the import description
        :rtype: list[Slice]
        """
        slices = []
        coverages = []

        if hasattr(self, "importers"):
            # for MultiImporter (e.g: used by Sentinel 2 recipe) contains 4 separate importers
            for importer in self.importers:
                coverages.append(importer.coverage)
        else:
            # for other recipes
            coverages = [self.coverage]
        
        if len(coverages) == 0:
            # no data collected to be imported
            return slices

        # If number of files < 5 print all files, or only print first 5 files
        max = ConfigManager.description_max_no_slices \
            if ConfigManager.description_max_no_slices < len(coverages[0].slices) else len(coverages[0].slices)
        for i in range(0, max):
            slices.append(coverages[0].slices[i])
        return slices

    def __get_grid_domains(self, slice):
        """
        Get the grid domains of current importing coverage slice
        :return: str
        """
        values = []

        for axis_subset in slice.axis_subsets:
            grid_axis = axis_subset.coverage_axis.grid_axis

            if axis_subset.coverage_axis.dataBound is False:
                values.append(str(grid_axis.grid_low))
            else:
                values.append(str(grid_axis.grid_low) + ":" + str(grid_axis.grid_high))

        return ",".join(values)

    def _insert_slices(self):
        """
        Insert the slices of the coverage
        """
        is_loggable = True
        is_ingest_file = True
        file_name = ""

        log_file = None

        try:
            log_file = open(ConfigManager.log_file, "a+")
            log_file.write("\n-------------------------------------------------------------------------------------")
            log_file.write(prepend_time("Ingesting coverage '{}'...".format(self.coverage.coverage_id)))
        except Exception as e:
            is_loggable = False
            log.warn("\nCannot create log file for this ingestion process, only log to console.")

        add_pyramid_members = False

        for i in range(self.processed, self.total):
            try:
                # Log the time to send the slice (file) to server to ingest
                # NOTE: in case of using wcs_extract recipe, it will fetch file from server, so don't know the file size
                if hasattr(self.coverage.slices[i].data_provider, "file"):
                    file_path = self.coverage.slices[i].data_provider.file.filepath
                    file_name = os.path.basename(file_path)
                    file_size_in_mb = 0
                    try:
                        file_size_in_mb = round((float)(os.path.getsize(file_path)) / (1000*1000), 2)
                    except:
                        file_name = file_path
                        pass
                    start_time = time.time()
                    self._insert_slice(self.coverage.slices[i])
                    grid_domains = self.__get_grid_domains(self.coverage.slices[i])

                    import_with_blocking = True
                    if self.session is not None and self.session.blocking is False:
                        import_with_blocking = False

                    if import_with_blocking is True:
                        # Default mode, analyze all files then import
                        index = "{}/{}".format(str(i + 1), str(self.total))
                    else:
                        # Analyze one file then import this file
                        Importer.processed_files_count += 1
                        index = "{}/{}".format(str(Importer.processed_files_count), str(self.session.total_files_to_import))

                    self._log_file_ingestion(index, file_name, start_time, grid_domains, file_size_in_mb, is_loggable, log_file)

                    if add_pyramid_members is False \
                            and (self.session is not None and self.session.import_overviews_only is False):
                        # NOTE: add pyramid members after the first file imported successfully if specified in the ingredients file
                        self._add_pyramid_members()
                        add_pyramid_members = True
                else:
                    is_ingest_file = False
                    # extract coverage from petascope to ingest a new coverage
                    start_time = time.time()
                    self._insert_slice(self.coverage.slices[i])
                    end_time = time.time()
                    time_to_ingest = round(end_time - start_time, 2)
                    log.info(prepend_time("Total time to ingest: {} s.".format(time_to_ingest)))

                if hasattr(self.coverage.slices[i].data_provider, "file"):
                    imported_file = self.coverage.slices[i].data_provider.file
                    if self.session is not None:
                        if self.session.blocking == True:
                            self.session.imported_files.append(imported_file)
                        else:
                            # import file by file with blocking: false
                            self.session.imported_files = [imported_file]
            except Exception as e:
                if ConfigManager.skip:
                    log.warn("Skipped slice " + str(self.coverage.slices[i]))
                    if is_loggable and is_ingest_file:
                        log_file.write("\nSkipped file: " + file_name + ".")
                        log_file.write("\nReason: " + str(e))
                else:
                    if is_loggable and is_ingest_file:
                        log_file.write("\nError file: " + file_name + ".")
                        log_file.write("\nReason: " + str(e))
                        log_file.write("\nResult: failed.")
                        log_file.close()

                    raise e
            self.processed += 1

        if is_loggable:
            log_file.write("\nResult: success.")
            log_file.close()
    
    def _log_file_ingestion(self, index, file_name, start_time, grid_domains, file_size_in_mb, is_loggable, log_file):
        end_time = time.time()
        time_to_ingest = round(end_time - start_time, 2)
        if time_to_ingest < 0.0000001:
            time_to_ingest = 0.0000001

        if file_size_in_mb != 0:
            size_per_second = round(file_size_in_mb / time_to_ingest, 2)
            log_text = "coverage '{}' - {} - file '{}' - grid domains [{}] of size {} MB; Total time to ingest file {} s @ {} MB/s.".format(
                                                                self.coverage.coverage_id,
                                                                index, file_name,
                                                                grid_domains,
                                                                file_size_in_mb, time_to_ingest, size_per_second)
        else:
            log_text = "coverage '{}' - {} - file '{}' - grid domains [{}]; Total time to ingest file {} s.".format(
                                                                self.coverage.coverage_id,
                                                                index, file_name,
                                                                grid_domains,
                                                                time_to_ingest)

        log_text = prepend_time(log_text)
        # write to console
        log.info(log_text)
        if is_loggable:
            # write to log file
            log_file.write(log_text)
        
    def _initialize_coverage(self):
        """
        Initializes the coverage
        """
        executor = ConfigManager.executor
        gml_obj = self._generate_initial_gml_slice()

        if ConfigManager.mock:
            request = WCSTInsertRequest(gml_obj.get_url(), False, self.coverage.pixel_data_type,
                                        self.coverage.tiling, executor.insitu, None, ConfigManager.black_listed)
        else:
            request = WCSTInsertRequest(None, False, self.coverage.pixel_data_type,
                                        self.coverage.tiling, executor.insitu, gml_obj, ConfigManager.black_listed)

        current_insitu_value = executor.insitu
        executor.insitu = None
        executor.execute(request, mock=ConfigManager.mock)
        executor.insitu = current_insitu_value

        base_coverage_id = self.coverage.coverage_id
        base_pyramid_member_coverage_ids = self.list_pyramid_member_coverages(base_coverage_id, ConfigManager.mock)

        # If scale_levels specified in ingredient files, send request to Petascope to create downscaled level coverages
        if self.scale_levels is not None:
            # Levels be ascending order
            sorted_list = sorted(self.scale_levels)
            # NOTE: each level is processed separately with each HTTP request
            for level in sorted_list:
                downscaled_level_coverage_id = create_downscaled_coverage_id(self.coverage.coverage_id, level)

                if downscaled_level_coverage_id in base_pyramid_member_coverage_ids:
                    # if downscaled level coverage id exists, then add the timestamp as suffix to avoid conflict
                    downscaled_level_coverage_id = add_date_time_suffix(downscaled_level_coverage_id)

                scale_factors = self.get_scale_factors(self.coverage.crs, level)
                request = CreatePyramidMemberRequest(self.coverage.coverage_id, downscaled_level_coverage_id, scale_factors)
                executor.execute(request, mock=ConfigManager.mock, input_base_url=request.context_path)

        elif self.scale_factors is not None:
            # if scale_factors exist in the ingredients file, then send CreatePyramidMember request
            # to create downscaled level coverages and add them as pyramid members of the base coverage
            for obj in self.scale_factors:
                downscaled_level_coverage_id = obj["coverage_id"]
                factors = obj["factors"]

                request = CreatePyramidMemberRequest(self.coverage.coverage_id, downscaled_level_coverage_id,
                                                     factors)
                executor.execute(request, mock=ConfigManager.mock, input_base_url=request.context_path)

    def _add_pyramid_members(self):
        """
        Depend on what configured in the ingredients file, send AddPyramidMember requests to petascope
        """
        executor = ConfigManager.executor
        base_coverage_id = self.coverage.coverage_id
        base_pyramid_member_coverage_ids = self.list_pyramid_member_coverages(base_coverage_id, ConfigManager.mock)

        if self.session is not None:

            # add listed pyramid members coverage ids in the ingredients file to this base coverage's pyramid
            if self.session.pyramid_members is not None:
                for base_coverage_id in self.session.pyramid_members:
                    if base_coverage_id not in base_pyramid_member_coverage_ids:
                        request = AddPyramidMemberRequest(self.coverage.coverage_id, base_coverage_id, self.session.pyramid_harvesting)
                        executor.execute(request, mock=ConfigManager.mock, input_base_url=request.context_path)

            # add this importing coverage id to the listed pyramid members of base coverages configured in the ingredients file
            if self.session.pyramid_bases is not None:
                for base_coverage_id in self.session.pyramid_bases:

                    base_pyramid_member_coverage_ids = self.list_pyramid_member_coverages(base_coverage_id,
                                                                                          ConfigManager.mock)

                    if self.coverage.coverage_id not in base_pyramid_member_coverage_ids:
                        request = AddPyramidMemberRequest(base_coverage_id, self.coverage.coverage_id, self.session.pyramid_harvesting)
                        executor.execute(request, mock=ConfigManager.mock, input_base_url=request.context_path)

    @staticmethod
    def list_pyramid_member_coverages(coverage_id, mock=False):
        """
        Return the list of pyramid_member_coverages of the current importing coverage
        """
        if mock is False:
            request = ListPyramidMembersRequest(coverage_id)
            service_call = request.context_path + "?" + request.get_query_string()
            json_obj = json.loads(decode_res(validate_and_read_url(service_call)))

            results = []
            for obj in json_obj:
                results.append(obj["coverage"])

            return results

        return []

    def update_coverage_inspire_metadata(self, base_coverage_id, inspire_metadata_url):
        """
        Update coverage's INSPIRE metadata
        :param base_coverage_id: str
        :param inspire_metadata_url: str (if this value is non-empty, then coverage is marked as INSPIRE coverage)

        """
        executor = ConfigManager.executor
        request = InspireUpdateMetadataURLRequest(base_coverage_id, inspire_metadata_url)
        executor.execute(request, mock=ConfigManager.mock, input_base_url=None)

    def add_pyramid_member_if_any(self, base_coverage_id, pyramid_member_coverage_id, pyramid_haversting=False):
        """
        If pyramid member coverage doesn't exist in base coverage's pyramid, then add it
        """
        current_pyramid_member_coverage_ids = self.list_pyramid_member_coverages(base_coverage_id, ConfigManager.mock)
        if pyramid_member_coverage_id not in current_pyramid_member_coverage_ids:
            executor = ConfigManager.executor

            request = AddPyramidMemberRequest(base_coverage_id, pyramid_member_coverage_id, pyramid_haversting)
            executor.execute(request, mock=ConfigManager.mock, input_base_url=request.context_path)

    @staticmethod
    def get_scale_factors(coverage_crs, level):
        """
        Return the array of scale factors for each axis, based on the input scale level
        """
        scale_factors = []

        NO_SCALING_SCALE_FACTOR = 1

        crs_util = CRSUtil(coverage_crs)
        for crs_axis in crs_util.axes:
            scale_factor = NO_SCALING_SCALE_FACTOR
            if crs_axis.type == CRSAxis.AXIS_TYPE_X or crs_axis.type == CRSAxis.AXIS_TYPE_Y:
                scale_factor = level

            scale_factors.append(scale_factor)

        return scale_factors

    def _get_update_subsets_for_slice(self, slice):
        """
        Returns the given slice's interval as a list of wcst subsets
        :param slice: the slice for which to generate this
        :rtype: list[WCSTSubset]
        """
        subsets = []
        for axis_subset in slice.axis_subsets:
            low = axis_subset.interval.low
            high = axis_subset.interval.high
            # if ConfigManager.subset_correction and high is not None and low != high and type(low) != str:
            if ConfigManager.subset_correction and high is not None and low != high and type(low) == str:
                # Time axis with type = str (e.g: "1970-01-01T02:03:06Z")
                time_seconds = 1
                # AnsiDate (need to change from date to seconds)
                if axis_subset.coverage_axis.axis.crs_axis.is_time_day_axis():
                    time_seconds = DateTimeUtil.DAY_IN_SECONDS
                low = decimal.Decimal(str(arrow.get(low).float_timestamp)) + decimal.Decimal(str(axis_subset.coverage_axis.grid_axis.resolution * time_seconds)) / 2
                low = DateTimeUtil.get_datetime_iso(low)

                if high is not None:
                    high = decimal.Decimal(str(arrow.get(high).float_timestamp)) - decimal.Decimal(str(axis_subset.coverage_axis.grid_axis.resolution * time_seconds)) / 2
                    high = DateTimeUtil.get_datetime_iso(high)

            elif ConfigManager.subset_correction and high is not None and low != high and type(low) != str:
                # regular axes (e.g: latitude, longitude, index1d)
                low = decimal.Decimal(str(low)) + decimal.Decimal(str(axis_subset.coverage_axis.grid_axis.resolution)) / 2
                if high is not None:
                    high = decimal.Decimal(str(high)) - decimal.Decimal(str(axis_subset.coverage_axis.grid_axis.resolution)) / 2

            subsets.append(WCSTSubset(axis_subset.coverage_axis.axis.label, low, high))
        return subsets

    def _get_update_crs(self, slice, crs):
        """
        Returns the crs corresponding to the axes that are data bound
        :param slice: the slice for which the gml should be created
        :param crs: the crs of the coverage
        :return: String
        """
        crsAxes = []
        for axis_subset in slice.axis_subsets:
            if axis_subset.coverage_axis.dataBound:
                crsAxes.append(axis_subset.coverage_axis.axis.crs_axis)
        crsUtil = CRSUtil(crs)
        return crsUtil.get_crs_for_axes(crsAxes)

    def _generate_gml_slice(self, slice):
        """
        Generates the gml for a regular slice
        :param slice: the slice for which the gml should be created
        :rtype: GML file (if mock is true to debug) / gml string
        """
        metadata_provider = MetadataProvider(self.coverage.coverage_id, self._get_update_axes(slice),
                                             self.coverage.range_fields, self._get_update_crs(slice, self.coverage.crs),
                                             slice.local_metadata, self.grid_coverage, self.coverage.overview_index)
        data_provider = slice.data_provider
        mediator = Mediator(metadata_provider, data_provider)
        return mediator.get_gml_file() if ConfigManager.mock else mediator.get_gml_str()

    def _get_update_axes(self, slice):
        """
        Returns the axes for the slices that are bound to the data (e.g. Lat and Long for a 2-D raster)
        :param slice: the slice for which the gml should be created
        :rtype: dict[Axis, GridAxis]
        """
        axes = OrderedDict()
        for axis_subset in slice.axis_subsets:
            if axis_subset.coverage_axis.dataBound:
                axes[axis_subset.coverage_axis.axis] = axis_subset.coverage_axis.grid_axis
        return axes

    def _generate_initial_gml_slice(self):
        """
        Returns the initial slice in gml format
        :rtype: File
        """
        return self._generate_initial_gml_db()

    def _generate_initial_gml_db(self):
        """
        Generates the initial slice in gml for importing using the database method and returns the gml for it
        :rtype: File
        """
        # Transform the axes domains such that only a point is defined.
        # For the first slice we need to import a single point, which will then be updated with the real data
        axes_map = OrderedDict()
        for axis, grid_axis in self.coverage.get_insert_axes().items():
            if axis.coefficient is not None:
                assert type(axis.coefficient) == list, "Axis coefficients not of type list."
                assert len(axis.coefficient) > 0, "The list of coefficients is empty."
                # Get the first coefficient in irregular coverage  to create a initial slice
                axis = IrregularAxis(axis.label, axis.uomLabel, axis.low, axis.high, axis.origin, [axis.coefficient[0]],
                                     axis.crs_axis)
            axes_map[axis] = GridAxis(grid_axis.order, grid_axis.label, grid_axis.resolution, 0, 0)
        metadata_provider = MetadataProvider(self.coverage.coverage_id, axes_map,
                                             self.coverage.range_fields, self.coverage.crs, self.coverage.metadata,
                                             self.grid_coverage, self.coverage.overview_index)
        tuple_list = []

        # Tuple list for InsertCoverage request should be created from null values if they exist
        for range_field in self.coverage.range_fields:
            # If band doesn't have null value, default insert value is 0
            insert_value = self.DEFAULT_INSERT_VALUE
            if (range_field.nilValues is not None) and (len(range_field.nilValues) > 0):
                # e.g. band has null values  ["9.96921e+35:*", 9.96921e+36]
                # 9.96921e+35 is used inside <gml:tupleList> when inserting first empty point with INSERT INTO query
                # e.g. INSERT INTO test VALUES <[0:0,0:0] 9.96921e+35f> TILING ALIGNED [0:366, 0:500]
                insert_value = str(range_field.nilValues[0].value)

                if ":" in insert_value:
                    tmps = insert_value.split(":")
                    first = tmps[0]
                    second = tmps[1]
                    if first != "*":
                       insert_value = first
                    elif second != "*":
                        insert_value = second
                    else:
                        insert_value = None

                if insert_value is None or insert_value == "":
                    insert_value = self.DEFAULT_INSERT_VALUE

            tuple_list.append(insert_value)

        data_provider = TupleListDataProvider(",".join(tuple_list))
        mediator = Mediator(metadata_provider, data_provider)
        return mediator.get_gml_file() if ConfigManager.mock else mediator.get_gml_str()

    def _generate_initial_gml_inistu(self):
        """
        Generates the initial slice in gml for importing using the insitu method and returns the gml file for it
        :rtype: File
        """
        metadata_provider = MetadataProvider(self.coverage.coverage_id, self.coverage.get_insert_axes(),
                                             self.coverage.range_fields, self.coverage.crs, self.coverage.metadata,
                                             self.grid_coverage,
                                             self.coverage.overview_index)
        data_provider = self.coverage.slices[0].data_provider
        file = Mediator(metadata_provider, data_provider).get_gml_file()
        self.processed += 1
        self.resumer.add_imported_data(data_provider)
        return file

    def _insert_update_into_wms(self):
        """
        Inserts or Update the coverage into the wms service
        """
        try:
            # First check if layer exists or not from nonstandard HTTP request
            service_call = ConfigManager.admin_service + "/layer/isactive?COVERAGEID=" + self.coverage.coverage_id
            if ConfigManager.mock is False:
                response = decode_res(validate_and_read_url(service_call))
                layer_exist = True if response == "true" else False

                # WMS layer does not exist, just insert new WMS layer from imported coverage

                if layer_exist is False:
                    service_call = ConfigManager.admin_service + "/layer/activate?COVERAGEID=" + self.coverage.coverage_id
                    if ConfigManager.black_listed is True:
                        service_call += "&BLACKLISTED=true"

                    validate_and_read_url(service_call)
            else:
                log.info(service_call)

        except Exception as e:
            log.error(
                "Exception thrown when importing in WMS. Please try to reimport in WMS manually.")
            raise e

    def _is_insert(self):
        """
        Returns true if the coverage should be inserted, false if only updates are needed
        :rtype: bool
        """
        if self.coverage.coverage_id not in Importer.coverage_exists_dict:
            cov = CoverageUtilCache.get_cov_util(self.coverage.coverage_id)
            Importer.coverage_exists_dict[self.coverage.coverage_id] = cov.exists()

        result = Importer.coverage_exists_dict[self.coverage.coverage_id]
        return not result

    def __signal_handler(self, signum, sigframe):
        """
        NOTE: if wcst_import sends an UpdateCoverage request and it is terminated by ctrl + c or pkill -f
        then this method will be invoked
        """
        if self.current_data_provider is not None:
            self.resumer.add_imported_data(self.current_data_provider)

    def __register_signal_handlers(self):
        """
        Register event handlers when wcst_import is terminated
        """
        signal.signal(signal.SIGINT, self.__signal_handler)
        signal.signal(signal.SIGTERM, self.__signal_handler)
