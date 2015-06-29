import os

from util.gdal_util import GDALGmlUtil
from recipes.shared.gml_generator import GMLGenerator
from util.time_util import DateTimeUtil


class Generator:
    def __init__(self, util, coverage_id, gdal_file_path, crs_resolver, default_crs, time_crs, time_start, time_step):
        """
        Generates the initial WCST Insert gml template for a timeseries coverage
        :param util.fileutil.FileUtil util: the utility object
        :param str coverage_id: the id of the coverage to be generated
        :param str gdal_file_path: the gdal_file_path of one of the slices
        :param str crs_resolver: the crs resolver
        :param str default_crs: the default crs for gdal datasets
        :param str time_crs: the crs for the time axis
        :param DateTimeUtil time_start: the datetime value for the start
        :param float time_step: the time step for each slice
        """
        self.util = util
        self.crs_resolver = crs_resolver
        self.default_crs = default_crs
        self.coverage_id = coverage_id
        self.time_crs = time_crs
        self.time_start = time_start
        self.time_step = time_step
        self.gdal_util = GDALGmlUtil(crs_resolver, default_crs, gdal_file_path)

    def get_grid_envelope_low(self):
        """
        Petascope calculates it automatically from the first inserted slice so we ignore it
        :rtype list[float]
        """
        grid_envelope_low = [0, 0, 0]
        return grid_envelope_low

    def get_grid_envelope_high(self):
        """
        Petascope calculates it automatically from the first inserted slice so we ignore it
        :rtype list[float]
        """
        grid_envelope_high = [0, 0, 0]
        return grid_envelope_high

    def get_offset_vectors(self):
        """
        Returns the offset vectors for this
        :rtype list[float]
        """
        vectors = self.gdal_util.get_offset_vectors()
        for i in range(0, 2):
            vectors[i].append(0)
        vectors.append([0, 0, self.time_step])
        return vectors

    def get_coefficients(self):
        """
        Returns the coefficients for the grids
        :rtype list[float]
        """
        return [None, None, None]

    def get_origin(self):
        """
        Returns the origin
        :rtype: list[float]
        """
        origin = self.gdal_util.get_origin()
        origin.append(self.time_start.to_string())
        return origin

    def get_crs(self):
        """
        Returns the full compound crs for the timeseries
        :rtype: str
        """
        return self.crs_resolver + "crs-compound?1=" + self.gdal_util.get_crs() + "&amp;2=" + self.time_crs

    def to_gml(self):
        """
        Returns the generated gml string
        :rtype: str
        """
        gml = GMLGenerator(self.TEMPLATES_PATH) \
            .fields(self.gdal_util.get_fields_range_type()) \
            .coverage_id(self.coverage_id) \
            .crs(self.get_crs()) \
            .grid_envelope_low(self.get_grid_envelope_low()) \
            .grid_envelope_high(self.get_grid_envelope_high()) \
            .offset_vectors(self.get_offset_vectors(), self.get_coefficients()) \
            .origin(self.get_origin()) \
            .coverage_data_url("") \
            .coverage_data_url_mimetype("") \
            .generate()
        return gml

    def to_file(self):
        """
        Writes the gml to a file and returns the file path
        :rtype: str
        """
        return self.util.write_to_tmp_file(self.to_gml())

    TEMPLATES_PATH = os.path.join(os.path.dirname(__file__), "templates/")
