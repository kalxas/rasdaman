from recipes.shared.image.gdal_image_gml_generator import GDALImageGmlGenerator
from recipes.time_series_irregular.generator.generator import Generator
from util.crs_util import CRSGeoUtil, CRSTimeUtil
from util.gdal_util import GDALGmlUtil
from util.gml_datapair import GMLDataPair
from wcst.wcst import WCSTExecutor, WCSTInsertRequest
from wcst.wcst import WCSTUpdateRequest, WCSTSubset
from util.time_gdal_tuple import TimeGdalTuple
from util.util import Util


class Importer:
    def __init__(self, timeseries, coverage_id, time_crs, crs_resolver, default_crs, util, tiling, executor,
                 update=False):
        """
        This class can be used to import time series as a referenceable coverage
        :param list[TimeGdalTuple] timeseries: a map of form "datetime_value" => "/gdal/file/path"
        :param str coverage_id: the id of the coverage to be created
        :param str time_crs: the crs to be used for time
        :param str crs_resolver: the crs resolver for the session
        :param str default_crs: the default_crs to be used if one is not given
        :param Util util: the utility object
        :param str tiling: the tiling to use in rasdaman
        :param WCSTExecutor executor: the executor of wcst objects
        :param bool update: flag to indicate if the operation is an update (i.e. the coverage was already created)
        """
        self.timeseries = timeseries
        self.coverage_id = coverage_id
        self.time_crs = time_crs
        self.crs_resolver = crs_resolver
        self.default_crs = default_crs
        self.util = util
        self.tiling = tiling
        self.executor = executor
        self.update = update

    def ingest(self):
        """
        Ingests the provided timeseries into WCSTs
        """
        if not self.update:
            self.initiate_coverage()
        self.insert_slices()

    def initiate_coverage(self):
        """
        Creates and submits the initial 3d gml coverage to WCST
        """
        first_record = self.timeseries[0]
        gdal_util = GDALGmlUtil(self.crs_resolver, self.default_crs, first_record.filepath)
        gml_url = "file://" + self.create_init_gml_file(first_record)
        request = WCSTInsertRequest(gml_url, False, gdal_util.get_band_gdal_type(), self.tiling)
        self.executor.execute(request)

    def insert_slices(self):
        """
        Inserts the slices from the gdal files once the coverage was initiated
        """
        gdal_record = GDALGmlUtil(self.crs_resolver, self.default_crs, self.timeseries[0].filepath)
        crs_util = CRSGeoUtil(gdal_record.get_crs())
        crs_util_time = CRSTimeUtil(self.time_crs)
        for record in self.timeseries:
            gml_pair = self.create_slice_gml_file(record.filepath)
            gdal_record = GDALGmlUtil(self.crs_resolver, self.default_crs, record.filepath)

            subset_east = WCSTSubset(crs_util.get_east_axis(), gdal_record.get_extents()['x'][0],
                gdal_record.get_extents()['x'][1])
            subset_north = WCSTSubset(crs_util.get_north_axis(), gdal_record.get_extents()['y'][0],
                gdal_record.get_extents()['y'][1])
            subset_time = WCSTSubset(crs_util_time.get_future_axis(), '"' + record.time.to_ansi() + '"')

            request = WCSTUpdateRequest(self.coverage_id, "file://" + gml_pair.get_gml_path(),
                                        [subset_time, subset_east, subset_north])
            self.executor.execute(request)
            gml_pair.delete_record_files()

    def create_init_gml_file(self, time_tuple):
        """
        Creates the initial gml file for the insert
        :param TimeGdalTuple time_tuple: the time tuple record
        :rtype str
        """
        insert_gml = Generator(self.util, self.coverage_id, time_tuple.filepath, self.crs_resolver, self.default_crs,
                               self.time_crs, time_tuple.time).to_file()
        return insert_gml

    def create_slice_gml_file(self, gdal_file_path):
        """
        Creates a gml slice from one gdal file
        :param str gdal_file_path: the gdal file path
        :rtype GMLDataPair
        """
        datafile_path = self.util.copy_file_to_tmp(gdal_file_path)
        gmlfile_path = GDALImageGmlGenerator(self.util, self.crs_resolver, self.default_crs, datafile_path,
                                             self.coverage_id, "file://" + datafile_path).to_file()
        return GMLDataPair(gmlfile_path, datafile_path)
