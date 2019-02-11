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
from config_manager import ConfigManager
from master.evaluator.evaluator_slice_factory import EvaluatorSliceFactory
from master.importer.importer import Importer
from master.importer.multi_importer import MultiImporter
from master.error.runtime_exception import RuntimeException
from master.evaluator.sentence_evaluator import SentenceEvaluator
from master.evaluator.expression_evaluator_factory import ExpressionEvaluatorFactory
from master.helper.user_band import UserBand
from recipes.general_coverage.recipe import Recipe as GeneralCoverageRecipe
from recipes.general_coverage.gdal_to_coverage_converter import GdalToCoverageConverter
from util.crs_util import CRSUtil
from util.file_util import FileUtil
from util.gdal_util import GDALGmlUtil
from util.file_obj import File
from util.log import log, make_bold
from master.importer.resumer import Resumer


class Recipe(GeneralCoverageRecipe):

    #
    # constants
    #

    # supported product levels
    LVL_L1C = 'L1C'
    LVL_L2A = 'L2A'
    # resolutions in a single Sentinel 2 dataset; TCI (True Color Image) is 10m
    RES_10m = '10m'
    RES_20m = '20m'
    RES_60m = '60m'
    RES_TCI = 'TCI'

    RES_DICT = {RES_10m: [1, 10, -10], RES_20m: [1, 20, -20], RES_60m: [1, 60, -60], RES_TCI: [1, 10, -10]}

    # variables that can be used to template the coverage id
    VAR_CRS_CODE = '${crsCode}'
    VAR_RESOLUTION = '${resolution}'
    VAR_LEVEL = '${level}'
    # bands for each resolution
    BANDS_L1C = {
        RES_10m: [
            UserBand("1", "B4", "red, central wavelength 665 nm", "", "", [0], "nm"),
            UserBand("2", "B3", "green, central wavelength 560 nm", "", "", [0], "nm"),
            UserBand("3", "B2", "blue, central wavelength 490 nm", "", "", [0], "nm"),
            UserBand("4", "B8", "nir, central wavelength 842 nm", "", "", [0], "nm")
        ],
        RES_20m: [
            UserBand("1", "B5", "central wavelength 705 nm", "", "", [0], "nm"),
            UserBand("2", "B6", "central wavelength 740 nm", "", "", [0], "nm"),
            UserBand("3", "B7", "central wavelength 783 nm", "", "", [0], "nm"),
            UserBand("4", "B8A", "central wavelength 865 nm", "", "", [0], "nm"),
            UserBand("5", "B11", "central wavelength 1610 nm", "", "", [0], "nm"),
            UserBand("6", "B12", "central wavelength 2190 nm", "", "", [0], "nm")
        ],
        RES_60m: [
            UserBand("1", "B1", "central wavelength 443 nm", "", "", [0], "nm"),
            UserBand("2", "B9", "central wavelength 945 nm", "", "", [0], "nm"),
            UserBand("3", "B10", "central wavelength 1375 nm", "", "", [0], "nm")
        ],
        RES_TCI: [
            UserBand("1", "red", "B4, central wavelength 665 nm", "", "", [0], "nm"),
            UserBand("2", "green", "B3, central wavelength 560 nm", "", "", [0], "nm"),
            UserBand("3", "blue", "B2, central wavelength 490 nm", "", "", [0], "nm")
        ],
    }
    # L2A is same as L1C but doesn't have B10 in the 60m subdataset
    BANDS_L2A = {
        RES_10m: BANDS_L1C[RES_10m],
        RES_20m: BANDS_L1C[RES_20m],
        RES_60m: [
            UserBand("1", "B1", "central wavelength 443 nm", "", "", [0], "nm"),
            UserBand("2", "B9", "central wavelength 945 nm", "", "", [0], "nm"),
        ],
        RES_TCI: BANDS_L1C[RES_TCI],
    }
    BANDS = { LVL_L1C: BANDS_L1C, LVL_L2A: BANDS_L2A }
    # number of subdatasets in a Sentinel 2 dataset
    SUBDATASETS = 4
    DEFAULT_CRS = "OGC/0/AnsiDate@EPSG/0/${crsCode}"
    DEFAULT_IMPORT_ORDER = GdalToCoverageConverter.IMPORT_ORDER_ASCENDING
    
    #
    # public
    #

    def __init__(self, session):
        super(Recipe, self).__init__(session)
        self._init_options()
        # subdatasets have a specific path scheme and prepending "file://" interferes with it
        # TODO: however uncommenting the below causes another error:
        #       The URL provided in the coverageRef parameter is malformed.
        # ConfigManager.root_url = ""

    def validate(self):
        super(Recipe, self).validate()

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
        self._init_coverage_options()
        self.coverage_id = self.session.get_coverage_id()
        self.import_order = self._set_option(self.options, 'import_order', self.DEFAULT_IMPORT_ORDER)
        self.wms_import = self._set_option(self.options, 'wms_import', False)
        self.scale_levels = self._set_option(self.options, 'scale_levels', [])
        self.grid_cov = False
    
    def _init_coverage_options(self):
        covopts = self.options['coverage']
        self.crs = self._set_option(covopts, 'crs', self.DEFAULT_CRS)
        self._set_option(covopts, 'slicer', {})
        self._init_slicer_options(covopts)
    
    def _init_slicer_options(self, covopts):
        sliceropts = covopts['slicer']
        self._set_option(sliceropts, 'type', 'gdal')
        self._set_option(sliceropts, 'pixelIsPoint', False)
        if 'axes' not in sliceropts:
            self._init_axes_options(sliceropts)
    
    def _init_axes_options(self, sliceropts):
        sliceropts['axes'] = {
            'ansi': {
                "min": "datetime(regex_extract('${file:path}', '.*?/S2[^_]+_MSI[^_]+_([\\d]+)T[\\d]+_', 1), 'YYYYMMDD')",
                "gridOrder": 0,
                "type": "ansidate",
                "irregular": True,
                "dataBound": False
            },
            'E': {
                "min": "${gdal:minX}",
                "max": "${gdal:maxX}",
                "gridOrder": 1,
                "resolution": "${gdal:resolutionX}"
            },
            'N': {
                "min": "${gdal:minY}",
                "max": "${gdal:maxY}",
                "gridOrder": 2,
                "resolution": "${gdal:resolutionY}"
            }
        }
    
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
        for cov_id, conv in convertors.iteritems():
            coverage_slices = conv.coverage_slices

            importer = Importer(conv.resumer, conv.to_coverage(coverage_slices),
                                self.wms_import, self.scale_levels, self.grid_cov)
            ret.append(importer)
        return ret
    
    def _get_convertors(self):
        """
        Returns a map of coverage id -> GdalToCoverageConverter
        """
        convertors = {}
        for f in self.session.get_files():
            # This one does not contain any information for geo bounds
            if not FileUtil.validate_file_path(f.get_filepath()):
                continue

            gdal_ds = GDALGmlUtil(f.get_filepath())
            subdatasets = self._get_subdatasets(gdal_ds, f)
            gdal_ds.close()

            level = self._get_level(f.get_filepath())
            crs_code = ""

            evaluator_slice = None

            for res in [self.RES_10m, self.RES_20m, self.RES_60m, self.RES_TCI]:
                subds_file = self._get_subdataset_file(subdatasets, res)
                crs_code = self._get_crs_code(subds_file.get_filepath(), crs_code)
                cov_id = self._get_coverage_id(self.coverage_id, crs_code, level, res)
                conv = self._get_convertor(convertors, cov_id, crs_code, level, res)

                conv.files = [subds_file]
                crs_axes = CRSUtil(conv.crs).get_axes(self.coverage_id)

                if evaluator_slice is None:
                    # This one contains information for geo bounds
                    evaluator_slice = EvaluatorSliceFactory.get_evaluator_slice(GdalToCoverageConverter.RECIPE_TYPE,
                                                                                subds_file)

                # Resolution 10m, 20m and 60m have same data type (UInt16) while TCI has data type (Byte)
                if res == self.RES_TCI:
                    conv.data_type = "Byte"
                else:
                    conv.data_type = "UInt16"

                # Fixed values for 3 axes of Sentinel 2 coverage
                axis_resolutions = self.RES_DICT[res]
                slices = conv._create_coverage_slices(crs_axes, evaluator_slice, axis_resolutions)
                conv.coverage_slices += slices

        return convertors
    
    def _get_subdatasets(self, gdal_ds, f):
        subdatasets = gdal_ds.get_subdatasets()
        if len(subdatasets) != self.SUBDATASETS:
            raise RuntimeException("Cannot handle Sentinel 2 file " + f.get_filepath() + 
                                   ": GDAL reported " + str(len(subdatasets)) + 
                                   " subdatasets, expected " + str(self.SUBDATASETS) + ".")
        return [name for (name, _) in subdatasets]

    def _get_subdataset_file(self, subdatasets, res):
        check = ":" + res + ":"
        for name in subdatasets:
            if check in name:
                return File(name)
        # else not found
        raise RuntimeException("Resolution (string ':" + res + 
            ":') not found in subdatasets: " + str(subdatasets))
    
    def _get_crs_code(self, subds, crs_code):
        """
        Return the <crs_code> from subds of the form: 
        SENTINEL2_<level>:<file>:<resolution>:EPSG_<crs_code>
        """
        if crs_code == "":
            parts = subds.split(":EPSG_")
            if len(parts) != 2:
                raise RuntimeException("Cannot determine EPSG code from subdataset " + 
                                       subds)
            return parts[1]
        return crs_code

    def _get_level(self, file_path):
        if '_MSIL1C_' in file_path:
            return self.LVL_L1C
        elif '_MSIL2A_' in file_path:
            return self.LVL_L2A
        else:
            log.warn("Cannot determine level from collected file: " + file_path + "; assuming L1C.")
            return self.LVL_L1C

    def _get_coverage_id(self, cov_id, crs_code, level, resolution):
        return cov_id.replace(self.VAR_CRS_CODE, crs_code) \
                     .replace(self.VAR_LEVEL, level) \
                     .replace(self.VAR_RESOLUTION, resolution)

    def _get_convertor(self, convertors, cov_id, crs_code, level, res):
        if not cov_id in convertors:
            convertors[cov_id] = \
                self._create_convertor(convertors, cov_id, crs_code, level, res)
        return convertors[cov_id]
    
    def _create_convertor(self, convertors, cov_id, crs_code, level, res):
        self.resumer = Resumer(cov_id)
        recipe_type = GdalToCoverageConverter.RECIPE_TYPE
        sentence_evaluator = SentenceEvaluator(ExpressionEvaluatorFactory())
        files = []
        crs = self._get_crs(crs_code)
        bands_metadata_fields = {}
        axis_metadata_fields = {}

        return GdalToCoverageConverter(self.resumer, 
                                       recipe_type, 
                                       sentence_evaluator, 
                                       cov_id, 
                                       self.BANDS[level][res],
                                       files, 
                                       crs,
                                       self._read_axes(crs),
                                       self.options['tiling'],
                                       self._global_metadata_fields(),
                                       self._local_metadata_fields(),
                                       bands_metadata_fields,
                                       axis_metadata_fields,
                                       self._metadata_type(),
                                       self.grid_cov,
                                       self.import_order)
    
    def _get_crs(self, crs_code):
        crs = self.crs.replace(self.VAR_CRS_CODE, crs_code)
        return self._resolve_crs(crs)

    @staticmethod
    def get_name():
        return "sentinel2"
