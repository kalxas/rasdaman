from recipes.shared.image.gdal_image_gml_generator import GDALImageGmlGenerator
from recipes.time_series_irregular.generator.generator import Generator
from util.crs_util import CRSGeoUtil, CRSTimeUtil
from util.gdal_util import GDALGmlUtil
from util.gml_datapair import GMLDataPair
from wcst.wcst import WCSTExecutor, WCSTInsertRequest
from wcst.wcst import WCSTUpdateRequest, WCSTSubset
from util.time_gdal_tuple import TimeGdalTuple
from util.fileutil import FileUtil


class Importer:
    def __init__(self, session, timeseries, time_crs, tiling, update=False):
        """
        This class can be used to import time series as a referenceable coverage
        :param Session session: the import session
        :param list[TimeGdalTuple] timeseries: a map of form "datetime_value" => "/gdal/file/path"
        :param str time_crs: the crs to be used for time
        :param str tiling: the tiling to use in rasdaman
        :param bool update: flag to indicate if the operation is an update (i.e. the coverage was already created)
        """
        self.timeseries = timeseries
        self.session = session
        self.time_crs = time_crs
        self.tiling = tiling
        self.update = update
        self.processed_slices = 0

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
        gdal_util = GDALGmlUtil(self.session, first_record.filepath)
        gml_pair = self.create_init_gml_file(first_record)
        request = WCSTInsertRequest(gml_pair.get_gml_url(), False, gdal_util.get_band_gdal_type(), self.tiling)
        self.session.get_executor().execute(request)
        self.processed_slices += 1
        gml_pair.delete_record_files()
        self.timeseries.pop()

    def insert_slices(self):
        """
        Inserts the slices from the gdal files once the coverage was initiated
        """
        if len(self.timeseries) > 0:
            gdal_record = GDALGmlUtil(self.session, self.timeseries[0].filepath)
            crs_util = CRSGeoUtil(gdal_record.get_crs())
            crs_util_time = CRSTimeUtil(self.time_crs)
            for record in self.timeseries:
                gml_pair = self.create_slice_gml_file(record.filepath)
                gdal_record = GDALGmlUtil(self.session, record.filepath)

                subset_east = WCSTSubset(crs_util.get_east_axis(), gdal_record.get_extents()['x'][0],
                    gdal_record.get_extents()['x'][1])
                subset_north = WCSTSubset(crs_util.get_north_axis(), gdal_record.get_extents()['y'][0],
                    gdal_record.get_extents()['y'][1])
                subset_time = WCSTSubset(crs_util_time.get_future_axis(), '"' + record.time.to_ansi() + '"')

                request = WCSTUpdateRequest(self.session.get_coverage_id(), gml_pair.get_gml_url(),
                                            [subset_time, subset_east, subset_north])
                self.session.get_executor().execute(request)
                gml_pair.delete_record_files()
                self.processed_slices += 1

    def create_init_gml_file(self, time_tuple):
        """
        Creates the initial gml file for the insert
        :param TimeGdalTuple time_tuple: the time tuple record
        :rtype GMLDataPair
        """
        insert_gml = Generator(self.session, time_tuple.filepath, self.time_crs, time_tuple.time).to_file()
        return GMLDataPair(insert_gml, time_tuple.filepath)

    def create_slice_gml_file(self, gdal_file_path):
        """
        Creates a gml slice from one gdal file
        :param str gdal_file_path: the gdal file path
        :rtype GMLDataPair
        """
        datafile_path = gdal_file_path
        gmlfile_path = GDALImageGmlGenerator(self.session, datafile_path,
                                             GMLDataPair.get_url_method() + datafile_path).to_file()
        return GMLDataPair(gmlfile_path, datafile_path)

    def get_processed_slices(self):
        """
        Returns the number of processed slices so far
        :rtype: int
        """
        return self.processed_slices
