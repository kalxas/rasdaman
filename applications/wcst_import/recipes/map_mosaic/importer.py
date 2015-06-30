from recipes.shared.image.gdal_image_gml_generator import GDALImageGmlGenerator
from util.crs_util import CRSGeoUtil
from util.gdal_util import GDALGmlUtil
from util.gml_datapair import GMLDataPair
from wcst.wcst import WCSTInsertRequest
from wcst.wcst import WCSTUpdateRequest, WCSTSubset
from wcst.wmst import WMSTFromWCSInsertRequest


class Importer:
    def __init__(self, session, files, tiling, update=False, import_in_wms=False):
        """
        This class can be used to import time series as a referenceable coverage
        :param list[str] files: the files to be imported
        :param Session session: the session of the import
        :param str tiling: the tiling to use in rasdaman
        :param bool update: flag to indicate if the operation is an update (i.e. the coverage was already created)
        :param bool import_in_wms: imports the data in wms as well
        """
        self.files = files
        self.session = session
        self.tiling = tiling
        self.update = update
        self.import_in_wms = import_in_wms
        self.processed_slices = 0
        self.total = len(self.files)

    def ingest(self):
        """
        Ingests the provided files into WCSTs
        """
        if not self.update:
            self.initiate_coverage()
        self.insert_slices()
        if self.import_in_wms and not self.update:
            try:
                self.insert_into_wms()
            except Exception:
                pass

    def initiate_coverage(self):
        """
        Creates and submits the initial 3d gml coverage to WCST
        """
        first_record = self.files[0]
        gdal_util = GDALGmlUtil(self.session, first_record)
        gml_pair = self.create_slice_gml_file(first_record)
        gml_url = gml_pair.get_gml_url()
        request = WCSTInsertRequest(gml_url, False, gdal_util.get_band_gdal_type(), self.tiling)
        self.session.get_executor().execute(request)
        gml_pair.delete_record_files()
        self.processed_slices += 1
        self.files.pop(0)

    def insert_slices(self):
        """
        Inserts the slices from the gdal files once the coverage was initiated
        """
        if len(self.files) > 0:
            gdal_record = GDALGmlUtil(self.session, self.files[0])
            crs_util = CRSGeoUtil(gdal_record.get_crs())
            for record in self.files:
                gml_pair = self.create_slice_gml_file(record)
                gdal_record = GDALGmlUtil(self.session, record)
                subset_east = WCSTSubset(crs_util.get_east_axis(), gdal_record.get_extents()['x'][0],
                    gdal_record.get_extents()['x'][1])
                subset_north = WCSTSubset(crs_util.get_north_axis(), gdal_record.get_extents()['y'][0],
                    gdal_record.get_extents()['y'][1])
                request = WCSTUpdateRequest(self.session.get_coverage_id(), gml_pair.get_gml_url(),
                    [subset_east, subset_north])
                self.session.get_executor().execute(request)
                gml_pair.delete_record_files()
                self.processed_slices += 1

    def get_processed_slices(self):
        """
        Returns the number of processed slices so far
        :rtype: int
        """
        return self.processed_slices

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

    def insert_into_wms(self):
        """
        Inserts the new coverage into WMS
        """
        wms_req = WMSTFromWCSInsertRequest(self.session.get_coverage_id(), False)
        self.session.get_executor().execute(wms_req)
