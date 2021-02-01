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
 * Copyright 2003 - 2019 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
import os
import sys

from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException
from master.evaluator.evaluator_slice_factory import EvaluatorSliceFactory
from master.importer.importer import Importer
from master.importer.multi_importer import MultiImporter
from master.evaluator.sentence_evaluator import SentenceEvaluator
from master.evaluator.expression_evaluator_factory import ExpressionEvaluatorFactory
from master.helper.user_band import UserBand
from recipes.general_coverage.recipe import Recipe as GeneralCoverageRecipe
from recipes.general_coverage.gdal_to_coverage_converter import GdalToCoverageConverter
from util.crs_util import CRSUtil, CRSAxis
from util.file_util import FileUtil, File, FilePair
from util.gdal_util import GDALGmlUtil
from util.log import log, make_bold
from master.importer.resumer import Resumer

import re


class Recipe(GeneralCoverageRecipe):
    #
    # constants
    #

    RECIPE_NAME = "sentinel1"

    # coverage Id scheme: S1_GRD_${modebeam}_${polarisation}
    # e,g:                S1_GRD_IW/EW      _HH,VV,VH,..


    # Sentinel 1 tiff pattern
    # e.g: s1b-iw-grd-vh-20190324t164346-20190324t164411-015499-01d0a6-002.tiff
    GRD_FILE_PATTERN = "(.*)-(.*)-grd-(.*)-(.*)-(.*)-(.*)-(.*)-(.*).(.*)"
    SLC_FILE_PATTERN = "(.*)-(.*)-slc-(.*)-(.*)-(.*)-(.*)-(.*)-(.*).(.*)"
    grd_pattern = re.compile(GRD_FILE_PATTERN)
    slc_pattern = re.compile(SLC_FILE_PATTERN)

    # variables that can be used to template the coverage id
    VAR_MODEBEAM = '${modebeam}'
    VAR_POLARISATION = '${polarisation}'

    # 1 tiff file contains 1 band
    BAND = UserBand("0", "Grey", "", "", "", [], "")

    DEFAULT_PRODUCT = "GRD"
    SLC_PRODUCT = "SLC"

    DEFAULT_MODEBEAMS = ["EW", "IW"]
    DEFAULT_POLARISATIONS = ["HH", "HV", "VH", "VV"]

    # Sentinel 1 contains 1 band
    DEFAULT_BAND_DATA_TYPE = "UInt16"
    SLC_BAND_DATA_TYPE = "CInt16"

    EPSG_XY_CRS = "$EPSG_XY_CRS"
    CRS_TEMPLATE = "OGC/0/AnsiDate@" + EPSG_XY_CRS

    EPSG_4326 = "4326"

    DEFAULT_IMPORT_ORDER = GdalToCoverageConverter.IMPORT_ORDER_ASCENDING

    DEFAULT_NULL_VALUE = 0

    #
    # public
    #

    def __init__(self, session):
        super(Recipe, self).__init__(session)
        self._init_options()

    def validate(self):
        super(Recipe, self).validate()

        valid_files = []
        # Local validate for input files
        for file in self.session.get_files():
            file_name = os.path.basename(file.get_filepath())
            if not bool(re.match(self.GRD_FILE_PATTERN, file_name)
                        or re.match(self.SLC_FILE_PATTERN, file_name)):
                log.warn("File '" + file.get_filepath()
                         + "' is not valid GRD/SLC TIFF file, ignored for further processing.")
            else:
                valid_files.append(file)

        self.session.files = valid_files

    def describe(self):
        log.info("The recipe has been validated and is ready to run.")
        log.info(make_bold("Recipe: ") + self.session.get_recipe()['name'])
        log.info(make_bold("WCS Service: ") + ConfigManager.wcs_service)
        log.info(make_bold("Mocked: ") + str(ConfigManager.mock))
        if ConfigManager.track_files:
            log.info(make_bold("Track files: ") + str(ConfigManager.track_files))
        if ConfigManager.skip:
            log.info(make_bold("Skip: ") + str(ConfigManager.skip))
        if ConfigManager.retry:
            log.info(make_bold("Retries: ") + str(ConfigManager.retries))
        if ConfigManager.slice_restriction is not None:
            log.info(make_bold("Slice Restriction: ") + str(ConfigManager.slice_restriction))

        multiimporter = self._get_importer()
        cov_num = len(multiimporter.importers)
        i = 1
        for importer in multiimporter.importers:
            log.info("Coverage {}/{} - {}: {} files.".format(
                i, cov_num, make_bold(importer.coverage.coverage_id), len(importer.coverage.slices)))
            i += 1

    def ingest(self):
        self._get_importer().ingest()

    def status(self):
        return self._get_importer().get_progress()

    #
    # private
    #

    def _init_options(self):
        self._init_input_options()
        self._init_coverage_options()
        self.coverage_id = self.session.get_coverage_id()
        self.import_order = self._set_option(self.options, "import_order", self.DEFAULT_IMPORT_ORDER)
        self.wms_import = self._set_option(self.options, "wms_import", False)
        self.scale_levels = self._set_option(self.options, "scale_levels", [])
        self.grid_cov = False

    def _init_coverage_options(self):
        covopts = self.options["coverage"]
        self.epsg_xy_crs = None

        self._init_epsg_xy_crs()
        if self.epsg_xy_crs is None:
            raise RuntimeException("Cannot detect geo CRS from input files, make sure the files exist and are readable by gdal.")

        compound_crs = self.CRS_TEMPLATE.replace(self.EPSG_XY_CRS, self.epsg_xy_crs)
        self.crs = self._set_option(covopts, "crs", self._resolve_crs(compound_crs))
        self._set_option(covopts, "slicer", {})
        self._init_slicer_options(covopts)

    def _init_input_options(self):
        # specify a subset of resolutions to ingest
        inputopts = self.session.get_input()
        self.product = self._set_option(inputopts, "product", self.DEFAULT_PRODUCT)
        self.modebeams = self._set_option(inputopts, "modebeams", self.DEFAULT_MODEBEAMS)
        self.polarisations = self._set_option(inputopts, "polarisations", self.DEFAULT_POLARISATIONS)

    def _init_slicer_options(self, covopts):
        sliceropts = covopts["slicer"]
        self._set_option(sliceropts, "type", "gdal")
        self._set_option(sliceropts, "pixelIsPoint", False)
        axesopts = self._init_axes_options()

        if "axes" in sliceropts:
            for axis in sliceropts["axes"]:
                for i in sliceropts["axes"][axis]:
                    axesopts[axis][i] = sliceropts["axes"][axis][i]
        sliceropts["axes"] = axesopts

    def _init_axes_options(self):
        epsg_xy_axes_labels = self.__get_epsg_xy_axes_labels()

        file_pattern = self.GRD_FILE_PATTERN
        if self.product == self.SLC_PRODUCT:
            file_pattern = self.SLC_FILE_PATTERN

        return {
            "ansi": {
                # e.g. s1b-iw-grd-vh-20190324t164346-20190324t164411-015499-01d0a6-002.tiff
                "min": "datetime(regex_extract('${file:name}', '" + file_pattern + "', 4), 'YYYYMMDD')",
                "gridOrder": 0,
                "type": "ansidate",
                "irregular": True,
                "dataBound": False
            },
            epsg_xy_axes_labels[0]: {
                "min": "${gdal:minX}",
                "max": "${gdal:maxX}",
                "gridOrder": 1,
                "resolution": "${gdal:resolutionX}"
            },
            epsg_xy_axes_labels[1]: {
                "min": "${gdal:minY}",
                "max": "${gdal:maxY}",
                "gridOrder": 2,
                "resolution": "${gdal:resolutionY}"
            }
        }

    def _init_epsg_xy_crs(self):
        """
        From the first file of input file, detect its EPSG code for XY axes
        """
        for file in self.session.get_files():
            try:
                gdal_ds = GDALGmlUtil(file.get_filepath())
                self.epsg_xy_crs = gdal_ds.get_crs()
                break
            except Exception as e:
                if ConfigManager.skip == True:
                    pass
                else:
                    raise e

    def __get_epsg_xy_axes_labels(self):
        """
        Return a tuple of axis labels for X and Y axes
        """
        axes_labels = CRSUtil.get_axis_labels_from_single_crs(self.epsg_xy_crs)
        axis_type1 = CRSAxis.get_axis_type_by_name(axes_labels[0])

        # XY order (e.g: EPSG:3857)
        if axis_type1 == CRSAxis.AXIS_TYPE_X:
            return axes_labels[0], axes_labels[1]
        else:
            # YX order (e.g: EPSG:4326) needs to swap order
            return axes_labels[1], axes_labels[0]

    def _set_option(self, opts, key, default_value):
        if key not in opts:
            opts[key] = default_value
        return opts[key]

    def _get_importer(self):
        if self.importer is None:
            self.importer = MultiImporter(self._get_importers())
        return self.importer

    def _get_importers(self):
        ret = []
        convertors = self._get_convertors()
        for cov_id, conv in convertors.items():
            coverage_slices = conv.coverage_slices

            importer = Importer(conv.resumer, conv.to_coverage(coverage_slices),
                                self.wms_import, self.scale_levels, self.grid_cov)
            ret.append(importer)
        return ret

    def __filter_invalid_geo_bounds(self, slices):
        """
        Filter any coverage slices (scenes) which have invalid lat and long bounds
        in EPSG:4326
        """
        results = []

        for slice in slices:
            input_file = slice.data_provider.file.filepath
            axis_subsets = slice.axis_subsets

            is_valid = True
            for axis_subset in axis_subsets:
                axis = axis_subset.coverage_axis.axis
                geo_lower_bound = axis.low
                geo_upper_bound = axis.high
                axis_label = axis.label

                if axis.crs_axis.uri.endswith(self.EPSG_4326):
                    if CRSUtil.is_latitude_axis(axis_label):
                        is_valid = geo_lower_bound >= -90 and geo_upper_bound <= 90
                    elif CRSUtil.is_longitude_axis(axis_label):
                        is_valid = geo_lower_bound >= -180 and geo_upper_bound <= 180

                    if not is_valid:
                        log.warn("File '" + input_file
                                 + "' has invalid lat or long axes geo bounds in EPSG:4326 CRS, ignored for further processing.")
                        break

            if is_valid:
                results.append(slice)

        return results

    def _get_convertors(self):
        """
        Returns a map of coverage id -> GdalToCoverageConverter
        """
        convertors = {}

        band_data_type = self.DEFAULT_BAND_DATA_TYPE
        if self.product == self.SLC_PRODUCT:
            band_data_type = self.SLC_BAND_DATA_TYPE

        for file in self.session.get_files():

            # Check if this file still exists when preparing to import
            if not FileUtil.validate_file_path(file.get_filepath()):
                continue

            # Check if this file belongs to this coverage id
            modebeam, polarisation = self._get_modebeam_polarisation(file.filepath)
            cov_id = self._get_coverage_id(self.coverage_id, modebeam, polarisation)

            # This file already imported in coverage_id.resume.json
            self.resumer = Resumer(cov_id)
            if self.resumer.is_file_imported(file.filepath):
                continue

            conv = self._get_convertor(convertors, cov_id)

            file_pair = FilePair(file.filepath, file.filepath)
            conv.files = [file_pair]
            crs_axes = CRSUtil(conv.crs).get_axes(self.coverage_id)

            # Different file contains different datetime from its name
            evaluator_slice = EvaluatorSliceFactory.get_evaluator_slice(
                                GdalToCoverageConverter.RECIPE_TYPE, file)

            conv.data_type = band_data_type
            slices = conv._create_coverage_slices(crs_axes, evaluator_slice)
            slices = self.__filter_invalid_geo_bounds(slices)
            if len(slices) != 0:
                conv.coverage_slices += slices

            if len(conv.coverage_slices) != 0:
                first_slice = conv.coverage_slices[0]
                # This needs one available file to extract metadata later
                conv.files = [first_slice.data_provider.file]

        return convertors

    def _get_modebeam_polarisation(self, file_path):
        """
        If this file's name matches a combination of resolution(e.g: H), modebeam(e.g: EW), polarisation(e.g: HH)
        then it is valid to import to this coverage S1_GRDH_EW_HH
        """
        # e.g: s1a-iw-grd-vh-20190326t171654-20190326t171719-026512-02f856-002.tiff
        file_name = os.path.basename(file_path)
        if "grd" in file_name:
            matcher = self.grd_pattern.match(file_name)
        elif "slc" in file_name:
            matcher = self.slc_pattern.match(file_name)

        # e.g: iw1 and iw2 are same mode beam iw
        tmp_modebeam = matcher.group(2)[0:2]
        tmp_polarisation = matcher.group(3)[0:2]

        return tmp_modebeam.upper(), tmp_polarisation.upper()

    def _get_coverage_id(self, cov_id, modebeam, polarisation):
        return cov_id.replace(self.VAR_MODEBEAM, modebeam) \
                     .replace(self.VAR_POLARISATION, polarisation)

    def _get_convertor(self, convertors, cov_id):
        if not cov_id in convertors:
            convertors[cov_id] = \
                self._create_convertor(cov_id)
        return convertors[cov_id]

    def _create_convertor(self, cov_id):
        recipe_type = GdalToCoverageConverter.RECIPE_TYPE
        sentence_evaluator = SentenceEvaluator(ExpressionEvaluatorFactory())
        files = []
        bands_metadata_fields = {}
        axis_metadata_fields = {}

        default_null_values = []

        if self.product == self.DEFAULT_PRODUCT:
            default_null_values = [self.DEFAULT_NULL_VALUE]

        return GdalToCoverageConverter(self.resumer,
                                       default_null_values,
                                       recipe_type,
                                       sentence_evaluator,
                                       cov_id,
                                       [self.BAND],
                                       files,
                                       self.crs,
                                       self._read_axes(self.crs),
                                       self.options['tiling'],
                                       self._global_metadata_fields(),
                                       self._local_metadata_fields(),
                                       bands_metadata_fields,
                                       axis_metadata_fields,
                                       self._metadata_type(),
                                       self.grid_cov,
                                       self.import_order)

    @staticmethod
    def get_name():
        return Recipe.RECIPE_NAME
