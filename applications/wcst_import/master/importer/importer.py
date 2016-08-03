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

from collections import OrderedDict
from time import sleep
from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException
from master.importer.coverage import Coverage
from master.importer.resumer import Resumer
from master.importer.slice import Slice
from master.importer.slice_restricter import SliceRestricter
from master.mediator.mediator import Mediator
from master.provider.data.tuple_list_data_provider import TupleListDataProvider
from master.provider.metadata.axis import Axis
from master.provider.metadata.grid_axis import GridAxis
from master.provider.metadata.metadata_provider import MetadataProvider
from util.coverage_util import CoverageUtil
from util.file_obj import File
from util.log import log
from wcst.wcst import WCSTInsertRequest, WCSTUpdateRequest, WCSTSubset
from wcst.wmst import WMSTFromWCSInsertRequest


class Importer:
    def __init__(self, coverage, insert_into_wms=False, grid_coverage=False):
        """
        Imports a coverage into wcst
        :param Coverage coverage: the coverage to be imported
        """
        self.coverage = coverage
        self.resumer = Resumer(coverage.coverage_id)
        self.coverage.slices = SliceRestricter(
            self.resumer.eliminate_already_imported_slices(self.coverage.slices)).get_slices()
        self.processed = 0
        self.total = len(coverage.slices)
        self.insert_into_wms = insert_into_wms
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
                self._insert_into_wms()

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
                executor.execute(request)
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
        max = ConfigManager.description_max_no_slices if ConfigManager.description_max_no_slices < len(
            self.coverage.slices) else len(self.coverage.slices)
        for i in range(0, max):
            slices.append(self.coverage.slices[i])
        return slices

    def _insert_slices(self):
        """
        Insert the slices of the coverage
        """
        for i in range(self.processed, self.total):
            try:
                self._insert_slice(self.coverage.slices[i])
            except Exception as e:
                if ConfigManager.skip:
                    log.warn("Skipped slice " + str(self.coverage.slices[i]))
                else:
                    raise e
            self.processed += 1

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
        executor.execute(request)
        executor.insitu = current_insitu_value
        file.release()

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
            if ConfigManager.subset_correction and high is not None and low != high and type(low) != str:
                low += float(axis_subset.coverage_axis.grid_axis.resolution) / 2
                if high is not None:
                    high -= float(axis_subset.coverage_axis.grid_axis.resolution) / 2
            subsets.append(WCSTSubset(axis_subset.coverage_axis.axis.label, low, high))
        return subsets

    def _generate_gml_slice(self, slice):
        """
        Generates the gml for a regular slice
        :param slice: the slice for which the gml should be created
        :rtype: File
        """
        metadata_provider = MetadataProvider(self.coverage.coverage_id, self._get_update_axes(slice),
                                             self.coverage.range_fields, self.coverage.crs, None, self.grid_coverage)
        data_provider = slice.data_provider
        file = Mediator(metadata_provider, data_provider).get_gml_file()
        return file

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
                # Get the first coefficient in irregular coverage  to create a initial slice
                axis.coefficient = [axis.coefficient[0]]
            axes_map[axis] = GridAxis(grid_axis.order, grid_axis.label, grid_axis.resolution, 0, 0)
        metadata_provider = MetadataProvider(self.coverage.coverage_id, axes_map,
                                             self.coverage.range_fields, self.coverage.crs, self.coverage.metadata, self.grid_coverage)
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
                                             self.coverage.range_fields, self.coverage.crs, self.coverage.metadata, self.grid_coverage)
        data_provider = self.coverage.slices[0].data_provider
        file = Mediator(metadata_provider, data_provider).get_gml_file()
        self.processed += 1
        self.resumer.add_imported_data(data_provider)
        return file

    def _insert_into_wms(self):
        """
        Inserts the coverage into the wms service
        """
        try:
            request = WMSTFromWCSInsertRequest(self.coverage.coverage_id, False)
            ConfigManager.executor.execute(request)
        except Exception as e:
            log.error(
                "Exception thrown when importing in WMS. Please try to reimport in WMS manually.")
            raise e

    def _is_insert(self):
        """
        Returns true if the coverage should be inserted, false if only updates are needed
        :rtype: bool
        """
        cov = CoverageUtil(self.coverage.coverage_id)
        return not cov.exists()
