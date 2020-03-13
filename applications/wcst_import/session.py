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
import os
import glob2 as glob
import time
from config_manager import ConfigManager
from master.importer.resumer import Resumer

from util.log import log
from wcst.wcst import WCSTMockExecutor, WCSTExecutor
from master.error.runtime_exception import RuntimeException
from util.file_util import FileUtil, File
from util.list_util import get_null_values


class Session:
    def __init__(self, config, inp, recipe, hooks, ingredient_file_name, ingredients_dir_path):
        """
        This class is used to hold the configuration for this importing session
        :param dict[str,str] config: the config part of the json input
        :param dict[str,str] inp: the input part of the json input
        :param dict[str,dict|str] recipe: the recipe configuration
        :param list[dict{}] hooks: list of command shoud be run before/after importing data
        :param str ingredient_file_name: the input file name of wcst_import.sh
        :param str ingredients_dir_path: the filepath to the directory containing the ingredients to be used
        for relative paths
        :rtype: Session
        """
        self.config = config
        self.ingredient_file_name = ingredient_file_name
        self.ingredients_dir_path = ingredients_dir_path if ingredients_dir_path.endswith("/") \
            else ingredients_dir_path + "/"
        self.files = self.parse_input(inp['paths'] if 'paths' in inp else [])
        self.coverage_id = inp['coverage_id'] if 'coverage_id' in inp else None
        self.recipe = recipe
        self.input = inp
        self.wcs_service = config['service_url'] if "service_url" in config else None
        if "tmp_directory" in config:
            self.tmp_directory = config['tmp_directory']
        else:
            log.info("No tmp directory specified, will use '{}'.".format(ConfigManager.tmp_directory))
            # No "tmp_directory" is configured in ingredient file, then use this default folder to store temp files.
            if not os.path.exists(ConfigManager.tmp_directory):
                os.makedirs(ConfigManager.tmp_directory)
                os.chmod(ConfigManager.tmp_directory, 0777)
            self.tmp_directory = ConfigManager.tmp_directory
        self.crs_resolver = self.__get_crs_resolver_configuration()
        self.default_crs = config['default_crs'] if "default_crs" in config else None

        # NOTE: only old recipes before general recipe using the default_crs inside the ingredient files
        if self.default_crs:
            self.default_crs = self.__replace_secore_prefix(self.default_crs.strip())
        self.root_url = self.config["root_url"] if "root_url" in self.config else "file://"
        self.root_url = self.root_url.strip()

        self.insitu = config['insitu'] if "insitu" in config else None
        self.default_null_values = config['default_null_values'] if "default_null_values" in config else []
        self.mock = False if "mock" not in config else bool(self.config["mock"])
        # By default, analyze all files then import (blocking import mode). With non_blocking_import mode, analyze and import each file separately.
        self.blocking = True if "blocking" not in config else bool(self.config["blocking"])

        self.subset_correction = bool(self.config['subset_correction']) if "subset_correction" in self.config else False
        self.skip = bool(self.config['skip']) if "skip" in self.config else False
        self.retry = bool(self.config['retry']) if "retry" in self.config else False
        self.slice_restriction = self.config['slice_restriction'] if "slice_restriction" in self.config else None
        self.retries = int(self.config['retries']) if "retries" in self.config else 5
        self.retry_sleep = float(self.config['retry_sleep']) if "retry_sleep" in self.config else 1
        self.resumer_dir_path = self.resumer_dir_path if "resumer_dir_path" in self.config else self.ingredients_dir_path
        self.description_max_no_slices = int(
            self.config['description_max_no_slices']) if "description_max_no_slices" in self.config else 5
        self.track_files = bool(self.config['track_files']) if "track_files" in self.config else True

        self.wms_import = False
        if "options" in self.recipe:
            self.wms_import = True if "wms" not in self.recipe["options"] else bool(self.recipe["options"])

        # Pre/Post hooks to run before analyzing files/after import replaced files
        # (original files are not used but processed files (e.g: by gdalwarp))
        self.before_hooks = []
        self.after_hooks = []

        if hooks is not None:
            for hook in hooks:
                if hook["when"] == "before_ingestion":
                    self.before_hooks.append(hook)
                elif hook["when"] == "after_ingestion":
                    self.after_hooks.append(hook)

        self.setup_config_manager()

        self.filter_imported_files()

    def filter_imported_files(self):
        """
        Filer all imported files from coverage_id.resume.json
        """
        resumer = Resumer(self.coverage_id)
        not_imported_files = resumer.get_not_imported_files(self.files)
        self.files = not_imported_files

    def setup_config_manager(self):
        """
        Initializes the configuration manager
        """
        ConfigManager.automated = self.is_automated()
        ConfigManager.crs_resolver = self.crs_resolver
        ConfigManager.default_crs = self.default_crs
        ConfigManager.default_null_values = get_null_values(self.default_null_values)
        ConfigManager.insitu = self.insitu
        ConfigManager.mock = self.mock
        ConfigManager.tmp_directory = self.tmp_directory if self.tmp_directory[-1] == "/" else self.tmp_directory + "/"
        ConfigManager.root_url = self.root_url
        ConfigManager.wcs_service = self.wcs_service
        ConfigManager.executor = self.get_executor()
        ConfigManager.subset_correction = self.subset_correction
        ConfigManager.skip = self.skip
        ConfigManager.retry = self.retry
        ConfigManager.retries = self.retries if self.retry is True else 1
        ConfigManager.retry_sleep = self.retry_sleep
        ConfigManager.slice_restriction = self.slice_restriction
        ConfigManager.resumer_dir_path = self.resumer_dir_path if self.resumer_dir_path[-1] == "/" else self.resumer_dir_path + "/"
        ConfigManager.description_max_no_slices = self.description_max_no_slices
        ConfigManager.track_files = self.track_files
        ConfigManager.ingredient_file_name = self.ingredient_file_name

    def __get_crs_resolver_configuration(self):
        """
        From the petascope.properties file which is configured in config_manager.py when building wcst_import,
        it can read the secore_urls configuration of Petascope and set as default crs resolver.
        It must replace the SECORE prefix in ingredient file by petascope's SECORE prefix.
        (e.g: http://localhost:8080/def  to http://opengis.net/def).
        :return: str
        """
        # We will check if the environment variable exists first (it is exported in wcst_import.sh)
        try:
            petascope_properties_path = os.environ["PETASCOPE_PROPERTIES_PATH"]
        except:
            # If one does not run wcst_import with wcst_import.sh but wcst_import.py then it will check the well-known configuration paths
            try:
                # first is $RMANHOME/etc/petascope.properties
                petascope_properties_path = os.environ["RMANHOME"] + "/etc/petascope.properties"
            except:
                # $RMANHOME does not exist, try to find petascope.properties in /opt/rasdaman/etc/petascope.properties
                if os.path.exists("/opt/rasdaman/etc/petascope.properties"):
                    petascope_properties_path = "/opt/rasdaman/etc/petascope.properties"
                elif os.path.exists("/etc/rasdaman/petascope.properties"):
                    petascope_properties_path = "/etc/rasdaman/petascope.properties"
                else:
                    # It cannot find petascope.properties internally
                    raise RuntimeException("Could not locate the petascope.properties file, "
                                           "please export the environment variable PETASCOPE_PROPERTIES_PATH before executing this script.")

        crs_resolver = None
        with open(petascope_properties_path) as f:
            for line in f:
                if "secore_urls=" in line:
                    crs_resolver = line.split("=")[1].strip()
                    # wcst_import needs the SECORE prefix with "/" as last character
                    if crs_resolver[-1] != "/":
                        crs_resolver += "/"
                    return crs_resolver
        if crs_resolver is None:
            raise RuntimeException("Cannot find secore_urls configuration "
                                   "in petascope's properties file '{}' for crs_resolver".format(petascope_properties_path))

    def __replace_secore_prefix(self, crs):
        """
        Replace the SECORE prefix in ingredient file by petascope's configuration, e.g:
        http://localhost:8080/def/crs/EPSG/0/4326 to http://opengis.net/def/crs/EPSG/0/4326
        NOTE: this is only used by old recipes before general recipe and without compound CRSs.
        :param str crs: the input crs from the ingredient file
        :return: str
        """
        crs_postfix = crs.split("/def/")[1]
        updated_crs = self.crs_resolver + crs_postfix

        return updated_crs


    def __get_file_paths(self, paths):
        """"
        Get the list of file paths to be imported
        """
        file_paths = []
        for path in paths:
            path = path.strip()
            file_paths = file_paths + FileUtil.get_file_paths_by_regex(self.ingredients_dir_path, path)

        return file_paths


    def parse_input(self, paths):
        """
        Parses the list of paths and returns an ordered list of complete file paths
        :param list[str] paths: the list of paths
        :rtype list[File]
        """
        file_paths = self.__get_file_paths(paths)

        if len(file_paths) < len(paths):
            log.warn("WARNING: The materialized paths contain less files than the initial paths. This can be normal if "
                     "a directory provided in the paths is empty or if a path regex returns no results. If this is not "
                     "the case, make sure the paths are correct and readable by the importer.")

        file_paths.sort()
        file_obs = map(lambda f: File(f), file_paths)
        return file_obs

    def get_wcs_service(self):
        """
        Returns the path to wcs service
        :rtype str
        """
        return self.wcs_service

    def get_ingredients_dir_path(self):
        """
        Returns the ingredient dir path
        :return: str
        """
        return self.ingredients_dir_path

    def is_automated(self):
        """
        Returns true if the import is automated, meaning no human input needed, false otherwise
        :rtype: bool
        """
        if "automated" in self.config:
            return bool(self.config['automated'])
        return False

    def get_crs_resolver(self):
        """
        Returns the url to the crs resolver, this includes the /def/ path
        :rtype str
        """
        return self.crs_resolver

    def get_default_crs(self):
        """
        Returns the default crs without the url of the crs resolver
        :rtype str
        """
        return self.default_crs

    def get_recipe(self):
        """
        Returns the given recipe options
        :rtype dict[str,str]
        """
        return self.recipe

    def get_input(self):
        """
        Returns the input section of the ingredients
        :rtype dict[str,str]
        """
        return self.input

    def get_coverage_id(self):
        """
        Returns the coverage id for this session
        :rtype str
        """
        return self.coverage_id

    def get_recipe_name(self):
        """
        Return the name of the recipe for this session
        :return: str
        """
        return self.recipe["name"]

    def get_files(self):
        """
        Returns the input file paths
        :rtype list[str]
        """
        return self.files

    def get_executor(self):
        """
        Returns the configured executor
        """
        return WCSTExecutor(self.get_wcs_service(), self.insitu)

    def get_default_null_values(self):
        """
        :rtype list
        """
        return self.default_null_values


    @staticmethod
    def get_WCS_VERSION_SUPPORTED():
        """
        Returns the wcs version supported by the wcst import script
        As petascope currently does not seem to support acceptversions parameter, use the exact version
        """
        return "2.0.1"
