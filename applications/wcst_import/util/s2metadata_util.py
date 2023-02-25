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
from util.type_util import NoPublicConstructor
import re
import os

_IS_S2_DATA_PATTERN = re.compile(".*/GRANULE/.*/(IMG_DATA/R..m/|QI_DATA/).*jp2")
_dataset_cache = {}


class S2MetadataUtil(metaclass=NoPublicConstructor):

    @staticmethod
    def is_s2_data(file_path):
        global _IS_S2_DATA_PATTERN
        return _IS_S2_DATA_PATTERN.search(str(file_path))

    @classmethod
    def get(cls, file_path):
        global _dataset_cache
        filepath = str(file_path)
        if "IMG_DATA" in filepath:
            components = filepath.split("IMG_DATA")
        elif "QI_DATA" in filepath:
            components = filepath.split("QI_DATA")
        else:
            return None
        mtd_path = components[0] + "MTD_TL.xml"
        # get the resolution
        res = components[1].split(".")[0].split("_")[-1]
        cache_path = mtd_path + res
        if cache_path not in _dataset_cache:
            ret = cls._create(filepath, mtd_path, res)
            if ret.exists():
                ret.read()
            else:
                ret = None
            _dataset_cache[cache_path] = ret
        return _dataset_cache[cache_path]

    @staticmethod
    def enabled_in_ingredients(recipe):
        return "options" in recipe and \
               "coverage" in recipe["options"] and \
               "slicer" in recipe["options"]["coverage"] and \
               "subtype" in recipe["options"]["coverage"]["slicer"] and \
               recipe["options"]["coverage"]["slicer"]["subtype"] == "sentinel2"

    def __init__(self, file_path, mtd_path, resolution):
        """
        Utility class to extract information from a MTD_TL.xml file found in
        a Sentinel 2 SAFE directory.
        Do not use this constructor directly, use the get(file_path) method instead.
        :param str file_path: the file path to a file in the IMG_DATA directory.
        :param str mtd_path: the file path to MTD_TL.xml file
        :param resolution: one of "10m", "20m", "60m"
        """
        self.file_path = file_path
        self.mtd_path = mtd_path
        self.resolution = resolution

    def exists(self):
        return os.path.exists(self.mtd_path)

    def _get_value(self, line):
        # example input: "        <ULX>300000</ULX>"
        # example output: "300000"
        return line.split(">")[1].split("<")[0]

    def read(self):
        if self.resolution == "10m":
            self.xsize = Decimal(10980)
            self.ysize = Decimal(10980)
            self.xres = Decimal(10)
            self.yres = Decimal(-10)
            self.overviews = [5490, 2745, 1373, 687, 344]
        elif self.resolution == "20m":
            self.xsize = Decimal(5490)
            self.ysize = Decimal(5490)
            self.xres = Decimal(20)
            self.yres = Decimal(-20)
            self.overviews = [2745, 1373, 687, 344, 172]
        elif self.resolution == "60m":
            self.xsize = Decimal(1830)
            self.ysize = Decimal(1830)
            self.xres = Decimal(60)
            self.yres = Decimal(-60)
            self.overviews = [915, 458, 229, 115]
        else:
            raise RuntimeException("Invalid resolution in path: " + self.file_path)

        with open(self.mtd_path) as f:
            # relevant are only the first 31 lines:
            #
            # <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            # <n1:Level-2A_Tile_ID ...
            #   <n1:General_Info>
            #     <L1C_TILE_ID met...
            #     <TILE_ID metadataLevel=...
            #     <DATASTRIP_ID metadataLevel=...
            #     <DOWNLINK_PRIORITY metadataLevel=...
            #     <SENSING_TIME metadataLevel=...
            #     <Archiving_Info metadataLevel="Expertise">
            #       <ARCHIVING_CENTRE>VGS4</ARCHIVING_CENTRE>
            #       <ARCHIVING_TIME>2021-11-13T15:00:30.821570Z</ARCHIVING_TIME>
            #     </Archiving_Info>
            #   </n1:General_Info>
            #   <n1:Geometric_Info>
            #     <Tile_Geocoding metadataLevel="Brief">
            #       <HORIZONTAL_CS_NAME>WGS84 / UTM zone 32N</HORIZONTAL_CS_NAME>
            #       <HORIZONTAL_CS_CODE>EPSG:32632</HORIZONTAL_CS_CODE>
            #       <Size resolution="10">
            #         <NROWS>10980</NROWS>
            #         <NCOLS>10980</NCOLS>
            #       </Size>
            #       <Size resolution="20">
            #         <NROWS>5490</NROWS>
            #         <NCOLS>5490</NCOLS>
            #       </Size>
            #       <Size resolution="60">
            #         <NROWS>1830</NROWS>
            #         <NCOLS>1830</NCOLS>
            #       </Size>
            #       <Geoposition resolution="10">
            #         <ULX>300000</ULX>
            #         <ULY>5200020</ULY>
            for _ in range(35):
                line = next(f)
                if line.startswith("      <HORIZONTAL_CS_CODE"):
                    # example line 16:
                    #      <HORIZONTAL_CS_CODE>EPSG:32632</HORIZONTAL_CS_CODE>
                    self.crs = self._get_value(line)
                    self.crs_code = self.crs.split(':')[1]
                    self.crs = CRSUtil.get_crs_url('EPSG', self.crs_code)
                elif line.startswith("        <ULX"):
                    # example line 30:
                    #        <ULX>300000</ULX>
                    self.ulx = Decimal(self._get_value(line))
                elif line.startswith("        <ULY"):
                    # example line 31:
                    #        <ULY>5200020</ULY>
                    self.uly = Decimal(self._get_value(line))
                    break

    def get_filepath(self):
        """
        Returns the file path to current gdal file
        :return: str
        """
        return self.file_path

    def get_offset_vectors(self):
        """
        Returns the offset vectors for the coverage calculated from the dataset
        :rtype: list[float]
        """
        return self.xres, self.yres

    def get_offset_vector_x(self):
        """
        Returns the offset vectors for the coverage calculated from the dataset on the x axis
        :rtype: float
        """
        return self.xres

    def get_offset_vector_y(self):
        """
        Returns the offset vectors for the coverage calculated from the dataset on the y axis
        :rtype: float
        """
        return self.yres

    def get_origin_x(self):
        """
        Returns the origin of the dataset on the x axis
        :rtype: str
        """
        return self.ulx + Decimal(0.5) * self.xres

    def get_origin_y(self):
        """
        Returns the origin of the dataset on the y axis
        :rtype: str
        """
        return self.uly + Decimal(0.5) * self.yres

    def get_origin(self):
        """
        Returns the origin of the dataset. This is calculated using the origin from the dataset + 0.5 of an offset
                vector, as petascope requires it so.
        :rtype: list[str]
        """
        return self.ulx + Decimal(0.5) * self.xres, \
               self.uly + Decimal(0.5) * self.yres

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
        return Decimal(0)

    def get_coefficient_y(self):
        """
        Return the coefficients of the dataset for the y axis
        :rtype: float
        """
        return Decimal(0)

    def get_extents(self):
        """
        Return the extents of the dataset
        :rtype: dict[str, list]
        """
        minx = self.ulx
        maxy = self.uly
        maxx = minx + self.xres * self.xsize
        miny = maxy + self.yres * self.ysize
        return { "x": (minx, maxx), "y": (miny, maxy) }

    def get_extents_x(self):
        """
        Return the extents of the dataset for the x axis
        :rtype: tuple
        """
        return self.ulx, self.ulx + self.xres * self.xsize

    def get_extents_y(self):
        """
        Return the extents of the dataset for the y axis
        :rtype: tuple
        """
        return self.uly + self.yres * self.ysize, self.uly

    def get_resolution_x(self):
        """
        Returns the resolution of the coverage calculated from the dataset
        :rtype: float
        """
        return float(self.xres)

    def get_resolution_y(self):
        """
        Returns the resolution of the coverage calculated from the dataset
        :rtype: float
        """
        return float(self.yres)

    def get_number_of_overviews(self):
        """
        Return the total number of overviews in the file
        """
        return 5

    def get_fields_range_type(self):
        """
        Returns the range type fields from a dataset
        :rtype: list[GDALField]
        """
        if '_TCI_' not in self.file_path:
            return [GDALField("Gray", "10^0", [0.0])]
        else:
            return [GDALField("Red", "10^0", [0.0]),
                    GDALField("Green", "10^0", [0.0]),
                    GDALField("Blue", "10^0", [0.0])]

    def get_band_gdal_type(self):
        """
        Returns the gdal data type
        :rtype: str
        """
        if '_TCI_' not in self.file_path:
            return "UInt16"
        else:
            return "Byte"

    def get_crs(self):
        """
        Returns the CRS associated with this dataset. If none is found the default for the session is returned
        :rtype: str
        """
        return self.crs

    def get_crs_code(self):
        """
        Returns the CRS code associated with this dataset. If none is found the default for the session is returned
        :rtype: str
        """
        return self.crs_code

    def get_raster_x_size(self):
        """
        Returns the raster size on the x axis in pixels
        :rtype: int
        """
        return self.xsize

    def get_raster_y_size(self):
        """
        Returns the raster size on the y axis in pixels
        :rtype: int
        """
        return self.ysize

    def get_raster_x_size_by_overview(self, overview_index):
        """
        Return the raster size on the x axis by overview index (0 based)
        :return: int
        """
        return self.overviews[overview_index]

    def get_raster_y_size_by_overview(self, overview_index):
        """
        Return the raster size on the y axis by overview index (0 based)
        :return: int
        """
        return self.overviews[overview_index]

    def get_metadata(self):
        return {}

    def get_overview(self, overview_index):
        """
        Return the overview if it exists from the file
        :param overview_index: 0-based index
        """
        if overview_index < len(self.overviews):
            return self.overviews[overview_index]
        else:
            return None
