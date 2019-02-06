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
import os
import time

import session
from lib import arrow
from collections import OrderedDict
from time import sleep
from config_manager import ConfigManager
from master.importer.coverage import Coverage
from master.importer.resumer import Resumer
from master.importer.slice import Slice
from master.importer.slice_restricter import SliceRestricter
from master.mediator.mediator import Mediator
from master.provider.data.tuple_list_data_provider import TupleListDataProvider
from master.provider.metadata.axis import Axis
from master.provider.metadata.grid_axis import GridAxis
from master.provider.metadata.irregular_axis import IrregularAxis
from master.provider.metadata.metadata_provider import MetadataProvider
from util.coverage_util import CoverageUtil
from util.file_obj import File
from util.log import log
from wcst.wcst import WCSTInsertRequest, WCSTInsertScaleLevelsRequest, WCSTUpdateRequest, WCSTSubset
from wcst.wmst import WMSTFromWCSInsertRequest, WMSTFromWCSUpdateRequest
from wcst.wmst import WMSTGetCapabilities
from util.crs_util import CRSUtil
from util.time_util import DateTimeUtil
from lxml import etree


class Importer:

    # Check if coverage exist in Petascope
    coverage_exists = None

    def __init__(self, resumer, coverage, insert_into_wms=False, scale_levels=None, grid_coverage=False):
        """
        Imports a coverage into wcst
        :param Coverage coverage: the coverage to be imported
        """
        self.coverage = coverage
        self.resumer = resumer
        self.coverage.slices = SliceRestricter(
                                    self.resumer.eliminate_already_imported_slices(self.coverage.slices)).get_slices()
        self.processed = 0
        self.total = len(coverage.slices)
        self.insert_into_wms = insert_into_wms
        self.scale_levels = scale_levels
        self.grid_coverage = grid_coverage

    def ingest(self):
        """
        Ingests the given coverage
        """
        if len(self.coverage.slices) > 0:
            if self._is_insert():
                self._initialize_coverage()

            # Insert the remaining slices
            self._insert_slices()

            if self.insert_into_wms:
                self._insert_update_into_wms()

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
            try:
                current_str = str(current)
                file = self._generate_gml_slice(current)
                subsets = self._get_update_subsets_for_slice(current)
                request = WCSTUpdateRequest(self.coverage.coverage_id, file.get_url(), subsets, ConfigManager.insitu)
                executor = ConfigManager.executor
                executor.execute(request, mock=ConfigManager.mock)
                file.release()
                self.resumer.add_imported_data(current.data_provider)
            except Exception as e:
                log.warn(
                    "\nException thrown when trying to insert slice: \n" + current_str + "Retrying, you can safely ignore the warning for now. Tried " + str(
                        attempt + 1) + " times.\n")
                current_exception = e
                sleep(ConfigManager.retry_sleep)
                pass
            else:
                break
        else:
            log.warn("\nFailed to insert slice. Attempted " + str(ConfigManager.retries) + " times.")
            raise current_exception

    def get_slices_for_description(self):
        """
        Returns a list with the first slices to be used in the import description
        :rtype: list[Slice]
        """
        slices = []
        # If number of files < 5 print all files, or only print first 5 files
        max = ConfigManager.description_max_no_slices if ConfigManager.description_max_no_slices < len(
            self.coverage.slices) else len(self.coverage.slices)
        for i in range(0, max):
            slices.append(self.coverage.slices[i])
        return slices

    def _insert_slices(self):
        """
        Insert the slices of the coverage
        """
        is_loggable = True
        is_ingest_file = True
        file_name = ""
        try:
            log_file = open(ConfigManager.resumer_dir_path + "/" + ConfigManager.ingredient_file_name + ".log", "a+")
            log_file.write("\n-------------------------------------------------------------------------------------")
            log_file.write("\nIngesting coverage '" + self.coverage.coverage_id + "'...")
        except Exception as e:
            is_loggable = False
            log.warn("\nCannot create log file for this ingestion process, only log to console.")

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
                    self._log_file_ingestion(file_name, start_time, file_size_in_mb, is_loggable, log_file)
                else:
                    is_ingest_file = False
                    # extract coverage from petascope to ingest a new coverage
                    start_time = time.time()
                    self._insert_slice(self.coverage.slices[i])
                    end_time = time.time()
                    time_to_ingest = round(end_time - start_time, 2)
                    log.info("\nTotal time to ingest: " + str(time_to_ingest) + " s.")
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

        log_file.write("\nResult: success.")
        log_file.close()
    
    def _log_file_ingestion(self, file_name, start_time, file_size_in_mb, is_loggable, log_file):
        end_time = time.time()
        time_to_ingest = round(end_time - start_time, 2)
        if time_to_ingest < 0.0000001:
            time_to_ingest = 0.0000001
        log_text = ""
        if file_size_in_mb != 0:
            size_per_second = round(file_size_in_mb / time_to_ingest, 2)
            log_text = "\nFile {} of size {} MB; Total time to ingest {} s @ {} MB/s.".format(
                file_name, file_size_in_mb, time_to_ingest, size_per_second)
        else:
            log_text = "\nTotal time to ingest file {}: {} s.".format(file_name, time_to_ingest);
        # write to console
        log.info(log_text)
        if is_loggable:
            # write to log file
            log_file.write(log_text)
        

    def _initialize_coverage(self):
        """
        Initializes the coverage
        """
        file = self._generate_initial_gml_slice()
        request = WCSTInsertRequest(file.get_url(), False, self.coverage.pixel_data_type,
                                    self.coverage.tiling)
        executor = ConfigManager.executor
        current_insitu_value = executor.insitu
        executor.insitu = None
        executor.execute(request, mock=ConfigManager.mock)
        executor.insitu = current_insitu_value
        file.release()

        # If scale_levels specified in ingredient files, send the query to Petascope to create downscaled collections
        if self.scale_levels:
            # Levels be ascending order
            sorted_list = sorted(self.scale_levels)
            # NOTE: each level is processed separately with each HTTP request
            for level in sorted_list:
                request = WCSTInsertScaleLevelsRequest(self.coverage.coverage_id, level)
                executor.execute(request, mock=ConfigManager.mock)

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
            if axis_subset.coverage_axis.data_bound:
                crsAxes.append(axis_subset.coverage_axis.axis.crs_axis)
        crsUtil = CRSUtil(crs)
        return crsUtil.get_crs_for_axes(crsAxes)

    def _generate_gml_slice(self, slice):
        """
        Generates the gml for a regular slice
        :param slice: the slice for which the gml should be created
        :rtype: File
        """
        metadata_provider = MetadataProvider(self.coverage.coverage_id, self._get_update_axes(slice),
                                             self.coverage.range_fields, self._get_update_crs(slice, self.coverage.crs),
                                             slice.local_metadata, self.grid_coverage)
        data_provider = slice.data_provider
        gml_file = Mediator(metadata_provider, data_provider).get_gml_file()
        return gml_file

    def _get_update_axes(self, slice):
        """
        Returns the axes for the slices that are bound to the data (e.g. Lat and Long for a 2-D raster)
        :param slice: the slice for which the gml should be created
        :rtype: dict[Axis, GridAxis]
        """
        axes = OrderedDict()
        for axis_subset in slice.axis_subsets:
            if axis_subset.coverage_axis.data_bound:
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
        for axis, grid_axis in self.coverage.get_insert_axes().iteritems():
            if axis.coefficient is not None:
                assert type(axis.coefficient) == list, "Axis coefficients not of type list."
                assert len(axis.coefficient) > 0, "The list of coefficients is empty."
                # Get the first coefficient in irregular coverage  to create a initial slice
                axis = IrregularAxis(axis.label, axis.uomLabel, axis.low, axis.high, axis.origin, [axis.coefficient[0]],
                                     axis.crs_axis)
            axes_map[axis] = GridAxis(grid_axis.order, grid_axis.label, grid_axis.resolution, 0, 0)
        metadata_provider = MetadataProvider(self.coverage.coverage_id, axes_map,
                                             self.coverage.range_fields, self.coverage.crs, self.coverage.metadata,
                                             self.grid_coverage)
        tuple_list = ",".join(['0'] * len(self.coverage.range_fields))
        data_provider = TupleListDataProvider(tuple_list)
        file = Mediator(metadata_provider, data_provider).get_gml_file()
        return file

    def _generate_initial_gml_inistu(self):
        """
        Generates the initial slice in gml for importing using the insitu method and returns the gml file for it
        :rtype: File
        """
        metadata_provider = MetadataProvider(self.coverage.coverage_id, self.coverage.get_insert_axes(),
                                             self.coverage.range_fields, self.coverage.crs, self.coverage.metadata,
                                             self.grid_coverage)
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
            # First check from WMS GetCapabilities if layer name (coverage id) existed
            request = WMSTGetCapabilities()
            response = ConfigManager.executor.execute(request)

            root_element = etree.fromstring(response)
            namespace = {"wms": "http://www.opengis.net/wms"}
            exist = root_element.xpath("//wms:Capability/wms:Layer/wms:Layer/wms:Name/text()='"
                                       + self.coverage.coverage_id + "'", namespaces=namespace)

            # WMS layer does not exist, just insert new WMS layer from imported coverage
            if exist is False:
                request = WMSTFromWCSInsertRequest(self.coverage.coverage_id, False)
            else:
                # WMS layer existed, update WMS layer from updated coverage
                request = WMSTFromWCSUpdateRequest(self.coverage.coverage_id, False)
            ConfigManager.executor.execute(request, mock=ConfigManager.mock)
        except Exception as e:
            log.error(
                "Exception thrown when importing in WMS. Please try to reimport in WMS manually.")
            raise e

    def _is_insert(self):
        """
        Returns true if the coverage should be inserted, false if only updates are needed
        :rtype: bool
        """
        if Importer.coverage_exists is None:
            cov = CoverageUtil(self.coverage.coverage_id)
            Importer.coverage_exists = cov.exists()

        return not Importer.coverage_exists

