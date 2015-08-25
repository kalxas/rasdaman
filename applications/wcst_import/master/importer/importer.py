from collections import OrderedDict
from config_manager import ConfigManager
from master.importer.coverage import Coverage
from master.mediator.mediator import Mediator
from master.provider.data.tuple_list_data_provider import TupleListDataProvider
from master.provider.metadata.axis import Axis
from master.provider.metadata.grid_axis import GridAxis
from master.provider.metadata.metadata_provider import MetadataProvider
from util.coverage_util import CoverageUtil
from wcst.wcst import WCSTInsertRequest, WCSTUpdateRequest, WCSTSubset
from wcst.wmst import WMSTFromWCSInsertRequest


class Importer:
    def __init__(self, coverage, insert_into_wms=False):
        """
        Imports a coverage into wcst
        :param Coverage coverage: the coverage to be imported
        """
        self.coverage = coverage
        self.processed = 0
        self.total = len(coverage.slices)
        self.insert_into_wms = insert_into_wms

    def ingest(self):
        """
        Ingests the given coverage
        """
        if self._is_insert():
            self._initialize_coverage()
        else:
            # If this is an update do not insert into wms
            self.insert_into_wms = False

        # Insert the remaining slices
        self._insert_slices()

        if self.insert_into_wms:
            self._insert_into_wms()

    def get_progress(self):
        """
        Returns the progress of the import
        :rtype: tuple
        """
        return self.processed, self.total

    def _insert_slices(self):
        for i in range(self.processed, self.total):
            current = self.coverage.slices[i]
            file = self._generate_slice(current)
            subsets = self._get_update_subsets_for_slice(current)
            request = WCSTUpdateRequest(self.coverage.coverage_id, file.get_url(), subsets, ConfigManager.insitu)
            executor = ConfigManager.executor
            executor.execute(request)
            self.processed += 1
            file.release()

    def _initialize_coverage(self):
        file = self._generate_initial_slice()
        request = WCSTInsertRequest(file.get_url(), False, self.coverage.pixel_data_type,
            self.coverage.tiling, ConfigManager.insitu)
        executor = ConfigManager.executor
        executor.execute(request)
        file.release()

    def _get_update_subsets_for_slice(self, slice):
        subsets = []
        for axis_subset in slice.axis_subsets:
            low = axis_subset.interval.low
            high = axis_subset.interval.high
            if ConfigManager.subset_correction and high is not None and low != high:
                low += float(axis_subset.coverage_axis.grid_axis.resolution) / 2
                if high is not None:
                    high -= float(axis_subset.coverage_axis.grid_axis.resolution) / 2
            subsets.append(WCSTSubset(axis_subset.coverage_axis.axis.label, low, high))
        return subsets

    def _generate_slice(self, slice):
        metadata_provider = MetadataProvider(self.coverage.coverage_id, self.coverage.get_update_axes(),
                                             self.coverage.range_fields, self.coverage.crs)
        data_provider = slice.data_provider
        file = Mediator(metadata_provider, data_provider).get_gml_file()
        return file

    def _generate_initial_slice(self):
        if ConfigManager.insitu:
            return self._generate_initial_slice_inistu()
        else:
            return self._generate_initial_slice_db()

    def _generate_initial_slice_db(self):
        # Transform the axes domains such that only a point is defined.
        # For the first slice we need to import a single point, which will then be updated with the real data
        axes_map = OrderedDict()
        for axis, grid_axis in self.coverage.get_insert_axes().iteritems():
            axes_map[axis] = GridAxis(grid_axis.order, grid_axis.label, grid_axis.resolution, 0, 0)
        metadata_provider = MetadataProvider(self.coverage.coverage_id, axes_map,
                                             self.coverage.range_fields, self.coverage.crs)
        tuple_list = ",".join(['0'] * len(self.coverage.range_fields))
        data_provider = TupleListDataProvider(tuple_list)
        file = Mediator(metadata_provider, data_provider).get_gml_file()
        return file

    def _generate_initial_slice_inistu(self):
        metadata_provider = MetadataProvider(self.coverage.coverage_id, self.coverage.get_insert_axes(),
                                             self.coverage.range_fields, self.coverage.crs)
        data_provider = self.coverage.slices[0].data_provider
        file = Mediator(metadata_provider, data_provider).get_gml_file()
        self.processed += 1
        return file

    def _insert_into_wms(self):
        try:
            request = WMSTFromWCSInsertRequest(self.coverage.coverage_id, False)
            ConfigManager.executor.execute(request)
        except:
            pass

    def _is_insert(self):
        cov = CoverageUtil(self.coverage.coverage_id)
        return not cov.exists()
