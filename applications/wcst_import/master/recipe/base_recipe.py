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

from abc import ABCMeta, abstractmethod
from collections import OrderedDict

from config_manager import ConfigManager

from master.error.validate_exception import RecipeValidationException
from master.helper.overview import Overview
from recipes.general_coverage.abstract_to_coverage_converter import AbstractToCoverageConverter

from session import Session
from util.coverage_util import CoverageUtil, CoverageUtilCache
from util.list_util import join_list
from util.log import log, make_bold
from util.file_util import FileUtil
import copy
from decimal import Decimal


class BaseRecipe:
    __metaclass__ = ABCMeta

    """
    This class represents an abstract
    """

    def __init__(self, session):
        """
        Initializes the recipe
        :param Session session: the session for the import tun
        """
        self.session = session
        self.options = {}

    def validate(self):
        """
        Validates the session and recipe parameters in order to ensure a correct run
        Recipes are encouraged to override this method and add further validation functionality
        based on their parameters
        """
        self.validate_base()

    @abstractmethod
    def describe(self):
        """
        This methods is called before insert or update is run. You should override the method and add any comments
        regarding the operations that you will perform via log.info to inform the user. You should explicitly state
        the information that you deduced (e.g. timestamps for a timeseries) so that the consequences are clear.
        """
        cov = CoverageUtilCache.get_cov_util(self.session.get_coverage_id())
        operation_type = "UPDATE" if cov.exists() else "INSERT"
        log.info("The recipe has been validated and is ready to run.")
        log.info(make_bold("Recipe: ") + self.session.get_recipe()['name'])
        log.info(make_bold("Coverage: ") + self.session.get_coverage_id())
        log.info(make_bold("WCS Service: ") + ConfigManager.wcs_service)
        log.info(make_bold("Operation: ") + operation_type)
        log.info(make_bold("Subset Correction: ") + str(ConfigManager.subset_correction))
        log.info(make_bold("Mocked: ") + str(ConfigManager.mock))
        log.info(make_bold("WMS Import: ") + str(self.session.wms_import))

        # Blocking means analyzing all input files before importing all coverage slices
        # Non-blocking means analyzing 1 file then import 1 file then continue with next file.
        import_mode = "Blocking"
        if not self.session.blocking:
            import_mode = "Non-blocking"
        log.info(make_bold("Import mode: ") + import_mode)

        if ConfigManager.track_files:
            log.info(make_bold("Track files: ") + str(ConfigManager.track_files))
        if ConfigManager.skip:
            log.info(make_bold("Skip: ") + str(ConfigManager.skip))
        if ConfigManager.retry:
            log.info(make_bold("Retries: ") + str(ConfigManager.retries))
        if ConfigManager.slice_restriction is not None:
            log.info(make_bold("Slice Restriction: ") + str(ConfigManager.slice_restriction))
        pass

    @abstractmethod
    def status(self):
        """
        This method is called continuously to find out the status of the recipe. Use it to print information only
        when necessary and always return a tuple of form (numberOfItemsProcessed, numberOfTotalItems)
        :rtype (int, int)
        """
        pass

    @abstractmethod
    def ingest(self):
        """
        This method is called when the ingestion process is ready to be started. In thise method the developer should
        call the importer with the correct slices and start importing the given files
        """
        pass

    def run(self):
        """
        Runs the recipe
        """
        self.ingest()

    def validate_base(self, ignore_no_files=False):
        """
        Validates the configuration and the input files
        :param bool ignore_no_files: if the extending recipe does not work with files, set this to true to skip
        the validation check for no files (used in wcs_extract recipe).
        """
        if self.session.get_wcs_service() is None or self.session.get_wcs_service() == "":
            raise RecipeValidationException("No valid wcs endpoint provided")
        if self.session.get_crs_resolver() is None or self.session.get_crs_resolver() == "":
            raise RecipeValidationException("No valid crs resolver provided")
        if self.session.get_coverage_id() is None or self.session.get_coverage_id() == "":
            raise RecipeValidationException("No valid coverage id provided")

        import recipes.virtual_coverage.recipe as super_coverage
        if self.session.get_recipe_name() == super_coverage.Recipe.RECIPE_NAME:
            # NOTE: virtual_coverage recipe does not require any input files
            return

        if not FileUtil.check_dir_writable(ConfigManager.tmp_directory):
            raise RecipeValidationException("Cannot write to tmp directory '{}'".format(ConfigManager.tmp_directory))

        checked_files = []

        for file in self.session.get_files():
            if FileUtil.validate_file_path(file.get_filepath()):
                checked_files.append(file)

        if not ignore_no_files:
            # If no input file is available, exit wcst_import.
            FileUtil.validate_input_file_paths(checked_files)

        self.session.files = checked_files

        if 'wms_import' not in self.options:
            self.options['wms_import'] = False
        else:
            self.options['wms_import'] = bool(self.options['wms_import'])

        if 'tiling' not in self.options:
            self.options['tiling'] = None

        if 'scale_levels' not in self.options:
            self.options['scale_levels'] = None

        if 'scale_factors' not in self.options:
            self.options['scale_factors'] = None

        if self.options['scale_levels'] is not None \
           and self.options['scale_factors'] is not None:
            raise RecipeValidationException("Only one of 'scale_levels' or 'scale_factors' "
                                            "setting can exist in the ingredients file.")
        if self.options['scale_factors'] is not None:
            # as scale_factors and scale_levels are only valid when initializing a new coverage
            cov = CoverageUtilCache.get_cov_util(self.session.get_coverage_id())
            if not cov.exists():
                for obj in self.options['scale_factors']:
                    if 'coverage_id' not in obj or 'factors' not in obj:
                        raise RecipeValidationException("All elements of 'scale_factors' list must contain "
                                                        "'coverage_id' and 'factors' properties")
                    coverage_id = obj['coverage_id']
                    cov = CoverageUtilCache.get_cov_util(coverage_id)
                    if cov.exists():
                        raise RecipeValidationException("Downscaled level coverage '" + coverage_id + "' already exists, "
                                                        "please use a different 'coverage_id' in 'scale_factors' list")

        self.validate_pyramid_members()
        self.validate_pyramid_bases()

        if "import_order" in self.options:
            value = self.options['import_order']
            supported_list = [AbstractToCoverageConverter.IMPORT_ORDER_ASCENDING,
                              AbstractToCoverageConverter.IMPORT_ORDER_DESCENDING,
                              AbstractToCoverageConverter.IMPORT_ORDER_NONE]
            if value not in supported_list:
                error_message = "'import_order' option must be one of: {}. Given '{}'.".\
                                  format(join_list(supported_list),
                                         value)
                raise RecipeValidationException(error_message)
        else:
            # if not defined, then it is sorted by ascending
            self.options['import_order'] = AbstractToCoverageConverter.IMPORT_ORDER_ASCENDING

    def validate_pyramid_members(self):
        """
        Check if pyramid members coverage ids exist
        """
        if 'pyramid_members' in self.options:
            for pyramid_member_coverage_id in self.options['pyramid_members']:
                cov = CoverageUtilCache.get_cov_util(pyramid_member_coverage_id)
                if not cov.exists():
                    error_message = "Pyramid member coverage '" + pyramid_member_coverage_id \
                                    + "' does not exist locally'"
                    raise RecipeValidationException(error_message)

    def validate_pyramid_bases(self):
        """
        Check if pyramid bases coverage ids exist
        """
        if 'pyramid_bases' in self.options:
            for pyramid_base_coverage_id in self.options['pyramid_bases']:
                cov = CoverageUtilCache.get_cov_util(pyramid_base_coverage_id)
                if not cov.exists():
                    error_message = "Pyramid base coverage '" + pyramid_base_coverage_id \
                                    + "' does not exist locally'"
                    raise RecipeValidationException(error_message)

    @staticmethod
    def create_subsets_for_overview(subsets, overview_index, gdal_file):
        """
        Given subsets for all base coverage's axes and the scale_ratio of the overview in the first file
        Return a new subsets object which has the smaller grid domains and geo resolutions for XY axes
        of the corresponding overview
        :param subsets: list[AxisSubset]
        :param overview_index: 0-based overview index
        :param gdal_file: gdal dataset of a file
        """
        size_x_overview = gdal_file.get_raster_x_size_by_overview(overview_index)
        size_y_overview = gdal_file.get_raster_y_size_by_overview(overview_index)

        overview = Overview(overview_index, size_x_overview, size_y_overview)

        subsets_overview = copy.deepcopy(subsets)

        for axis_subset in subsets_overview:
            geo_axis = axis_subset.coverage_axis.axis
            geo_lower_bound = geo_axis.low
            geo_upper_bound = geo_axis.high

            grid_axis = axis_subset.coverage_axis.grid_axis

            if axis_subset.coverage_axis.axis.crs_axis.is_x_axis():
                axis_subset.coverage_axis.grid_axis.grid_high = overview.grid_upper_bound_x
                resolution = Decimal(geo_upper_bound - geo_lower_bound) / Decimal(overview.grid_size_x)

                if grid_axis.resolution < 0:
                    resolution = resolution * -1
                grid_axis.resolution = resolution
            elif axis_subset.coverage_axis.axis.crs_axis.is_y_axis():
                axis_subset.coverage_axis.grid_axis.grid_high = overview.grid_upper_bound_y
                resolution = Decimal(geo_upper_bound - geo_lower_bound) / Decimal(overview.grid_size_y)

                if grid_axis.resolution < 0:
                    resolution = resolution * -1
                grid_axis.resolution = resolution

        return subsets_overview

    @staticmethod
    def create_dict_of_slices(import_overviews):
        """
        Create a dict to hold slices of base coverage and overview coverges if any
        :return:
        """
        slices_dict = OrderedDict()
        slices_dict["base"] = []

        if len(import_overviews) > 0:
            for overview_index in import_overviews:
                slices_dict[str(overview_index)] = []

        return slices_dict

    @staticmethod
    @abstractmethod
    def get_name():
        pass
