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

from abc import ABCMeta, abstractmethod
import os
from config_manager import ConfigManager

from master.error.validate_exception import RecipeValidationException
from recipes.general_coverage.abstract_to_coverage_converter import AbstractToCoverageConverter

from session import Session
from util.coverage_util import CoverageUtil
from util.log import log, make_bold
from util.file_util import FileUtil


class BaseRecipe:
    """
    This class represents an abstract
    """
    __metaclass__ = ABCMeta

    def __init__(self, session):
        """
        Initializes the recipe
        :param Session session: the session for the import tun
        """
        self.session = session

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
        cov = CoverageUtil(self.session.get_coverage_id())
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
        # Non-blocking means analazying 1 file then import 1 file then continue with next file.
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

        if "import_order" in self.options:
            if self.options['import_order'] != AbstractToCoverageConverter.IMPORT_ORDER_ASCENDING \
                    and self.options['import_order'] != AbstractToCoverageConverter.IMPORT_ORDER_DESCENDING:
                error_message = "'import_order' option must be '{}' or '{}', given '{}'.".\
                                  format(AbstractToCoverageConverter.IMPORT_ORDER_ASCENDING,
                                         AbstractToCoverageConverter.IMPORT_ORDER_DESCENDING,
                                         self.options['import_order'])
                raise RecipeValidationException(error_message)
        else:
            self.options['import_order'] = None

    @staticmethod
    @abstractmethod
    def get_name():
        pass
