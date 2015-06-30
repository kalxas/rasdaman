import os

from util.gdal_util import GDALGmlUtil
from recipes.shared.gml_generator import GMLGenerator


class GDALImageGmlGenerator:
    """
    Class to convert a GDAL supported file to GML

    Usage:
    <code>
    converter = GDALToGmlConverter("path/to/some/file", "someCoverageId", "file://mydata.tif")
    gml_string = converter.to_gml()
    </code>
    """

    def __init__(self, session, gdal_file_path, url_to_coverage_data):
        """
        Constructor for the class
        :param Session session: the session for this import
        :param str gdal_file_path: path to the file to be converted
        :param str url_to_coverage_data: an url pointing to a file that contains the coverage data;
        usually this is the same as the gdal_file_path. Please note that an url format is required
        (i.e. local files should be file:///path)
        """
        self.util = session.get_util()
        self.coverage_id = session.get_coverage_id()
        self.url_to_coverage_data = url_to_coverage_data
        self.gdal_util = GDALGmlUtil(session, gdal_file_path)

    def to_gml(self):
        """
        Returns a GML representation of the given dataset in string format.
        :rtype: str
        """
        gml = GMLGenerator(self.TEMPLATES_PATH) \
            .fields(self.gdal_util.get_fields_range_type()) \
            .coverage_data_url(self.url_to_coverage_data) \
            .coverage_id(self.coverage_id) \
            .crs(self.gdal_util.get_crs()) \
            .grid_envelope_low([0, 0]) \
            .grid_envelope_high([self.gdal_util.get_raster_x_size(), self.gdal_util.get_raster_y_size()]) \
            .offset_vectors(self.gdal_util.get_offset_vectors()) \
            .origin(self.gdal_util.get_origin()) \
            .coverage_data_url_mimetype(self.gdal_util.get_mime()) \
            .generate()
        return gml

    def to_file(self):
        return self.util.write_to_tmp_file(self.to_gml())

    TEMPLATES_PATH = os.path.join(os.path.dirname(__file__), "templates/")