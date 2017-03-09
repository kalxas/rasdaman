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

import math
import decimal

from master.error.runtime_exception import RuntimeException
from master.evaluator.evaluator_slice import GDALEvaluatorSlice
from master.evaluator.sentence_evaluator import SentenceEvaluator
from master.extra_metadata.extra_metadata_collector import ExtraMetadataCollector, ExtraMetadataEntry
from master.extra_metadata.extra_metadata_ingredient_information import ExtraMetadataIngredientInformation
from master.extra_metadata.extra_metadata_serializers import ExtraMetadataSerializerFactory
from master.extra_metadata.extra_metadata_slice import ExtraMetadataSliceSubset
from master.generator.model.range_type_field import RangeTypeField
from master.generator.model.range_type_nill_value import RangeTypeNilValue
from master.helper.regular_user_axis import RegularUserAxis
from master.helper.user_axis import UserAxis
from master.helper.user_band import UserBand
from master.importer.axis_subset import AxisSubset
from master.importer.coverage import Coverage
from master.importer.interval import Interval
from master.importer.slice import Slice
from master.provider.data.file_data_provider import FileDataProvider
from master.provider.metadata.coverage_axis import CoverageAxis
from master.provider.metadata.grid_axis import GridAxis
from master.provider.metadata.irregular_axis import IrregularAxis
from master.provider.metadata.regular_axis import RegularAxis
from recipes.general_coverage.abstract_to_coverage_converter import AbstractToCoverageConverter
from util.crs_util import CRSAxis, CRSUtil
from util.file_obj import File
from util.gdal_util import GDALGmlUtil
from master.helper.high_pixel_adjuster import HighPixelAjuster


class GdalToCoverageConverter(AbstractToCoverageConverter):
    def __init__(self, sentence_evaluator, coverage_id, bands, gdal_files, crs, user_axes, tiling,
                 global_metadata_fields, local_metadata_fields, metadata_type, grid_coverage):
        """
        Converts a grib list of files to a coverage
        :param SentenceEvaluator sentence_evaluator: the evaluator for wcst sentences
        :param str coverage_id: the id of the coverage
        :param list[UserBand] bands: the name of the coverage band
        :param list[File] gdal_files: a list of grib files
        :param str crs: the crs of the coverage
        :param list[UserAxis] user_axes: a list with user axes
        :param str tiling: the tiling string to be passed to wcst
        :param dict global_metadata_fields: the global metadata fields
        :param dict local_metadata_fields: the local metadata fields
        :param str metadata_type: the metadata type
        :param boolean grid_coverage: check if user want to import grid coverage
        """
        AbstractToCoverageConverter.__init__(self, sentence_evaluator)
        self.sentence_evaluator = sentence_evaluator
        self.coverage_id = coverage_id
        self.bands = bands
        self.gdal_files = gdal_files
        self.crs = crs
        self.user_axes = user_axes
        self.tiling = tiling
        self.global_metadata_fields = global_metadata_fields
        self.local_metadata_fields = local_metadata_fields
        self.metadata_type = metadata_type
        self.grid_coverage = grid_coverage

    def _get_null_value(self, band):
        """
        Returns the null value for this band
        :param band: the current file's band to get null value
        :rtype: list[RangeTypeNilValue]
        """
        if len(self.bands) < 0:
            raise RuntimeException("At least one band should be provided")
        # NOTE: current limit 1 nilValue only to 1 band in rasdaman
        # each band has 1 specified nilValue (e.g: band1: 10, band2: 20, band3: 5,...)
        if band.nilValues is not None:
            range_nils = []
            for nil_value in band.nilValues:
                range_nils.append(RangeTypeNilValue("", nil_value))
            return range_nils
        return None

    def _metadata(self, slices):
        """
        Returns the metadata in the corresponding format indicated in the converter
        :param list[Slice] slices: the slices of the coverage
        :rtype: str
        """
        if not self.local_metadata_fields and not self.global_metadata_fields:
            return ""
        serializer = ExtraMetadataSerializerFactory.get_serializer(self.metadata_type)
        metadata_entries = []
        for coverage_slice in slices:
            slice = GDALEvaluatorSlice(GDALGmlUtil(coverage_slice.data_provider.get_file_path()))
            metadata_entry_subsets = []
            for axis in coverage_slice.axis_subsets:
                metadata_entry_subsets.append(ExtraMetadataSliceSubset(axis.coverage_axis.axis.label, axis.interval))
            metadata_entries.append(ExtraMetadataEntry(slice, metadata_entry_subsets))

        collector = ExtraMetadataCollector(self.sentence_evaluator,
                                           ExtraMetadataIngredientInformation(self.global_metadata_fields,
                                                                              self.local_metadata_fields),
                                           metadata_entries)
        return serializer.serialize(collector.collect())

    def _slice(self, gdal_file, crs_axes):
        """
        Returns a slice for a grib file
        :param File gdal_file: the path to the gdal file
        :param list[CRSAxis] crs_axes: the crs axes for the coverage
        :rtype: Slice
        """
        axis_subsets = []
        file_structure = self._file_structure()
        for i in range(0, len(crs_axes)):
            crs_axis = crs_axes[i]
            user_axis = self._user_axis(self._get_user_axis_by_crs_axis_name(crs_axis.label),
                                        GDALEvaluatorSlice(GDALGmlUtil(gdal_file.get_filepath())))
            high = user_axis.interval.high if user_axis.interval.high else user_axis.interval.low

            if isinstance(user_axis, RegularUserAxis):
                geo_axis = RegularAxis(crs_axis.label, crs_axis.uom, user_axis.interval.low, high,
                                       user_axis.interval.low, crs_axis)
            else:
                geo_axis = IrregularAxis(crs_axis.label, crs_axis.uom, user_axis.interval.low, high,
                                         user_axis.interval.low, user_axis.directPositions, crs_axis)

            if not crs_axis.is_easting() and not crs_axis.is_northing():
                # GDAL model is 2D so on any axis except x/y we expect to have only one value
                grid_low = 0
                grid_high = 0
            else:
                grid_low = 0
                number_of_geopixels = decimal.Decimal( str(user_axis.interval.high) ) - decimal.Decimal( str(user_axis.interval.low) )
                # number_of_grid_points = (geo_max - geo_min) / resolution
                grid_high = grid_low + number_of_geopixels / decimal.Decimal(user_axis.resolution)
                grid_high = HighPixelAjuster.adjust_high(grid_high)

                # Negative axis, e.g: Latitude (min <--- max)
                if user_axis.resolution < 0:
                    grid_high = int(abs(math.floor(grid_high)))
                else:
                    # Positive axis, e.g: Longitude (min --> max)
                    grid_high = int(abs(math.ceil(grid_high)))


            # NOTE: Grid Coverage uses the direct intervals as in Rasdaman, modify the high bound will have error in petascope
            if not self.grid_coverage:
                if grid_high > grid_low:
                    grid_high -= 1

            grid_axis = GridAxis(user_axis.order, crs_axis.label, user_axis.resolution, grid_low, grid_high)

            if crs_axis.is_easting():
                geo_axis.origin = decimal.Decimal( str(geo_axis.low) ) + decimal.Decimal( str(user_axis.resolution) ) / 2
            elif crs_axis.is_northing():
                geo_axis.origin = decimal.Decimal( str(geo_axis.high) ) + decimal.Decimal( str(user_axis.resolution) ) / 2

            axis_subsets.append(
                AxisSubset(CoverageAxis(geo_axis, grid_axis, crs_axis.is_northing() or crs_axis.is_easting()),
                           Interval(user_axis.interval.low, user_axis.interval.high)))
        return Slice(axis_subsets, FileDataProvider(gdal_file, file_structure))

    def _file_structure(self):
        """
        Returns the file structure
        :rtype: dict
        """
        file_structure = {}
        variables = []
        for band in self.bands:
            if band.identifier is not None:
                variables.append(str(band.identifier))
        if len(variables) > 0:
            file_structure["variables"] = variables
        return file_structure

    def _slices(self, crs_axes):
        """
        Returns all the slices for this coverage
        :param crs_axes:
        :rtype: list[Slice]
        """
        slices = []
        for gdal_file in self.gdal_files:
            slices.append(self._slice(gdal_file, crs_axes))
        return slices

    def _range_fields(self):
        """
        Returns the range fields for the coverage
        :rtype: list[RangeTypeField]
        """
        range_fields = []
        for band in self.bands:
            range_nills = self._get_null_value(band)
            if range_nills is None and band.nilValues is not None:
                range_nills = []
                for nil_value in band.nilValues:
                    range_nills.append(RangeTypeNilValue("", nil_value))
            range_fields.append(
                RangeTypeField(band.name, band.definition, band.description, range_nills, band.uomCode))
        return range_fields

    def _data_type(self):
        if len(self.gdal_files) < 1:
            raise RuntimeException("No gdal files given for import!")
        return GDALGmlUtil(self.gdal_files[0].get_filepath()).get_band_gdal_type()

    def to_coverage(self):
        """
        Returns the grib files as a coverage
        :rtype: Coverage
        """
        crs_axes = CRSUtil(self.crs).get_axes()
        slices = self._slices(crs_axes)
        coverage = Coverage(self.coverage_id, slices, self._range_fields(), self.crs,
                            self._data_type(),
                            self.tiling, self._metadata(slices))
        return coverage
