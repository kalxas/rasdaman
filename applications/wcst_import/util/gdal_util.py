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
from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException
from util.crs_util import CRSUtil
from util.file_util import FileUtil
from util.gdal_field import GDALField
from decimal import Decimal
import json
from util.log import log, log_to_file, prepend_time
from util.import_util import decode_res
from util.time_util import timeout, execute_with_retry_on_timeout

_spatial_ref_cache = {}
_gdal_dataset_cache = {}

# in seconds
TIME_OUT_IF_FILE_CANNOT_BE_OPENED = 60

MAX_RETRIES_TO_OPEN_FILE = 3


class GDALGmlUtil:
    def __init__(self, gdal_file_path):
        """
        Utility class to extract information from a gdal file. Best to isolate
        all gdal fuctionality to one class as gdallib is known to be problematic
        in imports.
        :param str gdal_file_path: the file path to the gdal supported file
        """
        self.gdal_file_path = gdal_file_path
        self.gdal_dataset = None  # properly set below

        gdal_cache_size = ConfigManager.gdal_cache_size
        if gdal_cache_size != 0:
            # cache of gdal datasets is enabled
            # to avoid costs for opening the same file multiple times
            # (-1: unlimited or N > 0: maximum number of files in cache)
            global _gdal_dataset_cache

            if gdal_file_path not in _gdal_dataset_cache:
                self.gdal_dataset = self.gdal_open(gdal_file_path)
                _gdal_dataset_cache[gdal_file_path] = self.gdal_dataset
            else:
                self.gdal_dataset = _gdal_dataset_cache[gdal_file_path]

            if (gdal_cache_size > 0) and (len(_gdal_dataset_cache) > gdal_cache_size):
                # clear the cache as it's larger than allowed by --gdal-cache-size
                self.__clear_gdal_dataset_cache()

        else:
            # cache of gdal datasets is not enabled (--gdal-cache-size is set to 0)
            self.gdal_dataset = self.gdal_open(gdal_file_path)

    def __clear_gdal_dataset_cache(self):
        """
        Clear gdal dataset cache when it reaches the maximum size (default it is unlimited) if
         -c, --gdal-cache-size is not set as argument for wcst_import.sh
        :return: True if the cache was cleared, False otherwise
        """
        if ConfigManager.gdal_cache_size != 0:
            global _gdal_dataset_cache
            for _, ds in _gdal_dataset_cache.items():
                ds = None
            _gdal_dataset_cache = {}
            return True
        else:
            return False

    def gdal_open(self, file_path):
        dataset = execute_with_retry_on_timeout(MAX_RETRIES_TO_OPEN_FILE,
                                                "Failed to open GDAL file '{}'",
                                                self.__gdal_open_dataset, file_path)
        return dataset

    @timeout(TIME_OUT_IF_FILE_CANNOT_BE_OPENED)
    def __gdal_open_dataset(self, *args):
        """
        :param str filepath: path to a valid file which gdal can decode
        :return: gdal_dataset for further analyzing
        """
        filepath = args[0][0]
        from util.import_util import import_gdal
        gdal = import_gdal()
        gdal_dataset = None
        try:
            # GDAL wants filename in utf8 or filename with spaces could not open
            gdal_dataset = gdal.Open(str(filepath).encode('utf8'))
        except RuntimeError as e:
            msg = str(e)
            if "Too many open files" in msg:
                # this error can be triggered if the cache holds too many
                # gdal datasets (1000+ usually), so we retry to open the
                # the file again after clearing the cache
                if self.__clear_gdal_dataset_cache():
                    log.warn("GDAL dataset cache had to be cleared because of a "
                             "'too many open files' error; consider increasing "
                             "the limit on open files (see 'ulimit -n').")
                    gdal_dataset = gdal.Open(filepath)
            else:
                raise e

        if gdal_dataset is None:
            # not sure if this could ever happen, but just in case
            raise RuntimeException("The file at path '" + filepath +
                                   "' is not a valid GDAL decodable file.")
        return gdal_dataset

    def close(self):
        """
        Close the dataset if it was open.
        """
        if self.gdal_dataset is not None:
            self.gdal_dataset = None

    def get_filepath(self):
        """
        Returns the file path to current gdal file
        :return: str
        """
        return self.gdal_file_path

    def get_offset_vectors(self):
        """
        Returns the offset vectors for the coverage calculated from the dataset
        :rtype: list[float]
        """
        offset_x = repr(self.gdal_dataset.GetGeoTransform()[1])
        offset_y = repr(self.gdal_dataset.GetGeoTransform()[5])
        return Decimal(offset_x), Decimal(offset_y)

    def get_offset_vector_x(self):
        """
        Returns the offset vectors for the coverage calculated from the dataset on the x axis
        :rtype: float
        """
        result = repr(self.get_offset_vectors()[0])
        return Decimal(result)

    def get_offset_vector_y(self):
        """
        Returns the offset vectors for the coverage calculated from the dataset on the y axis
        :rtype: float
        """
        result = repr(self.get_offset_vectors()[1])
        return Decimal(result)

    def get_origin_x(self):
        """
        Returns the origin of the dataset on the x axis
        :rtype: str
        """
        result = self.get_origin()[0]
        return result

    def get_origin_y(self):
        """
        Returns the origin of the dataset on the y axis
        :rtype: str
        """
        result = self.get_origin()[1]
        return result

    def get_origin(self):
        """
        Returns the origin of the dataset. This is calculated using the origin from the dataset + 0.5 of an offset
                vector, as petascope requires it so.
        :rtype: list[str]
        """
        geo = self.gdal_dataset.GetGeoTransform()
        result = Decimal(repr(geo[0])) + Decimal(0.5) * Decimal(repr(self.gdal_dataset.GetGeoTransform()[1])), \
                 Decimal(repr(geo[3])) + Decimal(0.5) * Decimal(repr(self.gdal_dataset.GetGeoTransform()[5]))
        return result

    def get_coefficients(self):
        """
        Returns the coefficients for the
        :rtype: list[float]
        """
        return [0, 0]

    def get_coefficient_x(self):
        """
        Return the coefficients of the dataset for the x axis
        :rtype: float
        """
        result = repr(self.get_coefficients()[0])
        return Decimal(result)

    def get_coefficient_y(self):
        """
        Return the coefficients of the dataset for the y axis
        :rtype: float
        """
        result = repr(self.get_coefficients()[1])
        return Decimal(result)

    def get_extents(self):
        """
        Return the extents of the dataset
        :rtype: dict[str, list]
        """
        geo_transform = self.gdal_dataset.GetGeoTransform()
        minx = Decimal(repr(geo_transform[0]))
        maxy = Decimal(repr(geo_transform[3]))
        maxx = minx + Decimal(repr(geo_transform[1])) * self.gdal_dataset.RasterXSize
        miny = maxy + Decimal(repr(geo_transform[5])) * self.gdal_dataset.RasterYSize
        return {
            "x": (minx, maxx),
            "y": (miny, maxy)
        }

    def get_extents_x(self):
        """
        Return the extents of the dataset for the x axis
        :rtype: tuple
        """
        return self.get_extents()['x']

    def get_extents_y(self):
        """
        Return the extents of the dataset for the y axis
        :rtype: tuple
        """
        return self.get_extents()['y']

    def get_resolution_x(self):
        """
        Returns the resolution of the coverage calculated from the dataset
        :rtype: float
        """
        geo_transform = self.gdal_dataset.GetGeoTransform()
        result = repr(geo_transform[1])
        return Decimal(result)

    def get_resolution_y(self):
        """
        Returns the resolution of the coverage calculated from the dataset
        :rtype: float
        """
        geo_transform = self.gdal_dataset.GetGeoTransform()
        result = repr(geo_transform[5])
        return Decimal(result)

    def get_number_of_overviews(self):
        """
        Return the total number of overviews in the file
        """
        first_band = self.get_raster_band(1)
        result = first_band.GetOverviewCount()
        return result

    def get_overview(self, overview_index):
        """
        Return the overview if it exists from the file
        :param overview_index: 0-based index
        """
        first_band = self.get_raster_band(1)
        overview = first_band.GetOverview(overview_index)
        return overview
    
    def get_raster_band(self, index):
        """
        Return 1 raster band by index of its in GDAL file
        :param int index:        
        """
        raster_band = self.gdal_dataset.GetRasterBand(index)
        return raster_band

    def get_fields_range_type(self):
        """
        Returns the range type fields from a dataset
        :rtype: list[GDALField]
        """
        from util.import_util import import_gdal
        gdal = import_gdal()

        fields = []

        number_of_bands = self.gdal_dataset.RasterCount

        for i in range(1, number_of_bands + 1):
            band = self.gdal_dataset.GetRasterBand(i)

            # Get the field name
            if band.GetColorInterpretation():
                field_name = gdal.GetColorInterpretationName(band.GetColorInterpretation())
            else:
                field_name = ConfigManager.default_field_name_prefix + repr(i)

            if ConfigManager.default_null_values is not None:
                nil_values = ConfigManager.default_null_values
            else:
                # If not, then detects it from file's bands
                nil_value = repr(band.GetNoDataValue()) if band.GetNoDataValue() is not None else ""
                nil_values = [nil_value]

            # Get the unit of measure
            uom = band.GetUnitType() if band.GetUnitType() else ConfigManager.default_unit_of_measure

            # Add it to the list of fields
            fields.append(GDALField(field_name, uom, nil_values))

        return fields

    def get_band_gdal_type(self):
        """
        Returns the gdal data type
        :rtype: str
        """
        import osgeo.gdal as gdal

        band1 = self.gdal_dataset.GetRasterBand(1)
        band1_image_metadata = band1.GetMetadata_List("IMAGE_STRUCTURE")
        if band1_image_metadata  is not None:
            if "PIXELTYPE=SIGNEDBYTE" in band1_image_metadata:
                return "SignedByte"

        return gdal.GetDataTypeName(self.gdal_dataset.GetRasterBand(1).DataType)

    def get_crs(self):
        """
        Returns the CRS associated with this dataset. If none is found the default for the session is returned
        :rtype: str
        """
        wkt = self.gdal_dataset.GetProjection()
        spatial_ref = self._get_spatial_ref(wkt)
        crs = ConfigManager.default_crs
        if spatial_ref.GetAuthorityName(None) is not None:
            crs = CRSUtil.get_crs_url(spatial_ref.GetAuthorityName(None),
                                      spatial_ref.GetAuthorityCode(None))
        if crs is None:
            raise RuntimeException("Cannot implicitly detect EPSG code from WKT of input file. "
                                   "Please explicitly specify the CRS in the ingredient (option \"default_crs\").")
        return crs

    def get_crs_code(self):
        """
        Returns the CRS code associated with this dataset. If none is found the default for the session is returned
        :rtype: str
        """
        wkt = self.gdal_dataset.GetProjection()
        return self._get_spatial_ref(wkt).GetAuthorityCode(None)
    
    def _get_spatial_ref(self, wkt):
        """
        Return a SpatialReference for the given wkt. Takes care of caching the returned objects.
        :rtype: SpatialReference
        """
        global _spatial_ref_cache
        if wkt not in _spatial_ref_cache:
            import osgeo.osr as osr
            spatial_ref = osr.SpatialReference()
            spatial_ref.ImportFromWkt(wkt)
            _spatial_ref_cache[wkt] = spatial_ref

        return _spatial_ref_cache[wkt]

    def get_raster_x_size(self):
        """
        Returns the raster size on the x axis in pixels
        :rtype: int
        """
        return self.gdal_dataset.RasterXSize

    def get_raster_y_size(self):
        """
        Returns the raster size on the y axis in pixels
        :rtype: int
        """
        return self.gdal_dataset.RasterYSize

    def get_raster_x_size_by_overview(self, overview_index):
        """
        Return the raster size on the x axis by overview index (0 based)
        :return: int
        """
        return self.get_overview(overview_index).XSize

    def get_raster_y_size_by_overview(self, overview_index):
        """
        Return the raster size on the y axis by overview index (0 based)
        :return: int
        """
        return self.get_overview(overview_index).YSize

    def get_datetime(self, time_tag=None):
        """
        Returns the datetime value for the dataset if one exists
        :param str time_tag: the tag if it is different from TIFFTAG_DATETIME
        :rtype: str
        """
        if time_tag is None:
            time_tag = "TIFFTAG_DATETIME"
        metadata = self.gdal_dataset.GetMetadata()
        if time_tag not in metadata:
            raise RuntimeException(
                "No tifftag " + time_tag + " for datetime was found in the file: " + self.gdal_file_path)
        return metadata[time_tag]

    def get_metadata_tag(self, tag):
        """
        Returns a specific metadata tag
        :param str tag: the tag to return
        :return: str
        """
        metadata = self.gdal_dataset.GetMetadata()
        if tag not in metadata:
            raise RuntimeException(
                "No tifftag " + tag + " found for " + self.gdal_file_path)
        return metadata[tag]

    def get_metadata(self):
        """
        Returns the file's metadata as dictionary
        :return: dict
        """
        metadata = self.gdal_dataset.GetMetadata()
        import collections
        metadata = collections.OrderedDict(sorted(metadata.items()))

        return metadata

    def get_color_table(self):
        """
        Return a valid colorTable as JSON string if any
        e.g: { "colorTable": [[0, 0, 0, 255], .... [43, 131, 186, 255], [43, 131, 186, 255]] }

        :return: str
        """
        result = None

        band = self.gdal_dataset.GetRasterBand(1)
        if band is not None:
            color_table = band.GetRasterColorTable()

            if color_table is not None:
                color_list = []
                for i in range(color_table.GetCount()):
                    color_list.append(list(color_table.GetColorEntry(i)))

                color_dict = {}
                color_dict["colorTable"] = color_list

                result = json.dumps(color_dict)

        return result

    def get_subdatasets(self):
        """
        Returns the datasets in the GDAL dataset as a list of (name, description) pairs.
        :return: list[(str, str)]
        """
        return self.gdal_dataset.GetSubDatasets()

    @staticmethod
    def data_type_to_gdal_type(data_type):
        """
        In WCST we use the gdal data types, so we need a transformation from numpy netcdf types
        :param str data_type: the numpy type of data file
        :rtype: str
        """
        numpy_to_gdal_dict = {
            "uint8": 1,
            "int8": 1,
            "uint16": 2,
            "int16": 3,
            "uint32": 4,
            "int32": 5,
            "float32": 6,
            "float64": 7,
            "complex64": 10,
            "complex128": 11,
        }
        from osgeo import gdal
        return gdal.GetDataTypeName(numpy_to_gdal_dict[data_type])


    @staticmethod
    def open_gdal_dataset_from_any_file(files):
        """
        This method is used to open 1 dataset to get the common metadata shared from all input files.
        :param list files: input files
        """
        gdal_dataset = None

        for file in files:
            file_path = file.get_filepath()
            try:
                gdal_dataset = GDALGmlUtil(file_path)
                return gdal_dataset
            except Exception as ex:
                error_message = "Failed to open GDAL file '{}'. Reason: {}".format(file_path, str(ex))
                log.warn(error_message)
                log_to_file(error_message)

                # Cannot open file by gdal, try with next file
                if ConfigManager.skip:
                    continue
                else:
                    raise ex

        if gdal_dataset is None:
            # Cannot open any dataset from input files, just exit wcst_import process
            FileUtil.validate_input_file_paths([])

    @staticmethod
    def get_gdal_version():
        import osgeo.gdal as gdal
        # e.g: 1110400 for version 11.1.4
        return gdal.VersionInfo()
