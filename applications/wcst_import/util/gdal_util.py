"""
Utility class for translating certain features from gdal to gml
"""
import magic

from gml_field import GMLField
from recipes.shared.runtime_exception import RuntimeException
from crs_util import CRSUtil


class GDALGmlUtil:
    def __init__(self, crs_resolver, default_crs, gdal_file_path):
        """
        Utility class to extract information from a gdal file. Best to isolate all gdal fuctionality to one class
        as gdallib is known to be problematic in imports
        :param str crs_resolver: the crs resolver for the session
        :param str default_crs: the default_crs to be used if one is not given
        :param str gdal_file_path: the file path to the gdal supported file
        """
        import osgeo.gdal as gdal

        self.crs_resolver = crs_resolver
        self.default_crs = default_crs
        self.gdal_file_path = gdal_file_path
        self.gdal_dataset = gdal.Open(self.gdal_file_path)

    def get_offset_vectors(self):
        """
        Returns the offset vectors for the coverage calculated from the dataset
        :rtype list[list[float]]]
        """
        offset_x = [self.gdal_dataset.GetGeoTransform()[1], 0]
        offset_y = [0, self.gdal_dataset.GetGeoTransform()[5]]
        return [offset_x, offset_y]

    def get_origin(self):
        """
        Returns the origin of the dataset. This is calculated using the origin from the dataset + 0.5 of an offset
                vector, as petascope requires it so.
        :rtype list[float]
        """
        geo = self.gdal_dataset.GetGeoTransform()
        return [str(geo[0] + 0.5 * self.gdal_dataset.GetGeoTransform()[1]),
                str(geo[3] + 0.5 * self.gdal_dataset.GetGeoTransform()[5])]

    def get_coefficients(self):
        """
        Returns the coefficients for the
        :rtype list[float]
        """
        return [None, None]

    def get_extents(self):
        """
        Return the extents of the dataset
        :rtype dict[str, list]
        """
        geo_transform = self.gdal_dataset.GetGeoTransform()
        minx = geo_transform[0]
        maxy = geo_transform[3]
        maxx = minx + geo_transform[1] * self.gdal_dataset.RasterXSize
        miny = maxy + geo_transform[5] * self.gdal_dataset.RasterYSize
        return {
            "x": (minx, maxx),
            "y": (miny, maxy)
        }

    def get_fields_range_type(self):
        """
        Returns the range type fields from a gdal dataset
        :rtype list[GMLField]
        """
        import osgeo.gdal as gdal

        fields = []
        for i in range(1, self.gdal_dataset.RasterCount + 1):
            band = self.gdal_dataset.GetRasterBand(i)
            nill_value = band.GetNoDataValue() if band.GetNoDataValue() is not None else ""
            field_name = gdal.GetColorInterpretationName(
                band.GetColorInterpretation()) if band.GetColorInterpretation() else "field" + str(i)
            uom = band.GetUnitType() if band.GetUnitType() else ""
            fields.append(GMLField(field_name, uom, nill_value))
        return fields

    def get_band_gdal_type(self):
        """
        Returns the gdal data type
        :rtype str
        """
        import osgeo.gdal as gdal

        return gdal.GetDataTypeName(self.gdal_dataset.GetRasterBand(1).DataType)

    def get_crs(self):
        """
        Returns the CRS associated with this dataset. If none is found the default for the session is returned
        :rtype str
        """
        import osgeo.osr as osr

        wkt = self.gdal_dataset.GetProjection()
        spatial_ref = osr.SpatialReference()
        spatial_ref.ImportFromWkt(wkt)
        crs = self.default_crs
        if spatial_ref.GetAuthorityName(None) is not None:
            crs = CRSUtil.get_crs_url(self.crs_resolver, spatial_ref.GetAuthorityName(None),
                                      spatial_ref.GetAuthorityCode(None))
        return crs

    def get_raster_x_size(self):
        """
        Returns the raster size on the x axis in pixels
        :rtype int
        """
        return self.gdal_dataset.RasterXSize

    def get_raster_y_size(self):
        """
        Returns the raster size on the y axis in pixels
        :rtype int
        """
        return self.gdal_dataset.RasterYSize

    def get_datetime(self, time_tag=None):
        """
        Returns the datetime value for the dataset if one exists
        :param str time_tag: the tag if it is different from TIFFTAG_DATETIME
        :rtype str
        """
        if time_tag is None:
            time_tag = "TIFFTAG_DATETIME"
        metadata = self.gdal_dataset.GetMetadata()
        if time_tag not in metadata:
            raise RuntimeException(
                "No tifftag " + time_tag + " for datetime was found in the file: " + self.gdal_file_path)
        return metadata[time_tag]

    def get_mime(self):
        """
        Returns the mime type of the given gdal file
        :rtype str
        """
        m = magic.open(magic.MAGIC_MIME)
        m.load()
        mime = m.file(self.gdal_file_path)
        mime = "" if mime is None else mime
        return mime
