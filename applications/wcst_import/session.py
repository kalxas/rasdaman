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
import os
from config_manager import ConfigManager
from master.helper.inspire import Inspire
from master.helper.overview import Overview
from master.importer.resumer import Resumer

from util.log import log
from master.request.wcst import WCSTExecutor
from master.error.runtime_exception import RuntimeException
from util.file_util import FileUtil, File
from util.list_util import get_null_values
from util.import_util import import_glob
from util.string_util import is_integer, get_petascope_endpoint_without_ows
from decimal import Decimal


glob = import_glob()

INTERNAL_SECORE = "internal"
DEFAULT_SECORE_URL = "http://localhost:8080/def"
INTERNAL_SECORE_URL_CONTEXT_PATH = "/rasdaman/def"
INTERNAL_SECORE_URL = "http://localhost:8080/" + INTERNAL_SECORE_URL_CONTEXT_PATH


class Session:

    RUNNING_SECORE_URL = None

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
        # input files to import
        self.files = self.parse_input(inp['paths'] if 'paths' in inp else [])
        # imported files from the list of input files (files are added in .resume.json will be ignored)
        self.imported_files = []
        self.coverage_id = inp['coverage_id'] if 'coverage_id' in inp else None

        metadata_url = ""
        if "inspire" in inp:
            inspire = inp["inspire"]
            if "metadata_url" in inspire:
                metadata_url = inspire["metadata_url"]
        self.inspire = Inspire(metadata_url)

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
                os.chmod(ConfigManager.tmp_directory, 0o777)
            self.tmp_directory = ConfigManager.tmp_directory
        self.crs_resolver, self.embedded_petascope_port = self.__get_crs_resolver_and_embedded_petascope_port_configuration()
        self.default_crs = config['default_crs'] if "default_crs" in config else None

        # NOTE: only old recipes before general recipe using the default_crs inside the ingredient files
        if self.default_crs:
            self.default_crs = self.__replace_secore_prefix(self.default_crs.strip())
        self.root_url = self.config["root_url"] if "root_url" in self.config else "file://"
        self.root_url = self.root_url.strip()

        self.insitu = config['insitu'] if "insitu" in config else None
        self.black_listed = config["black_listed"] if "black_listed" in config else None
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
        self.pyramid_members = None
        self.pyramid_bases = None
        self.pyramid_harvesting = False

        self.wms_import = False
        self.import_overviews = []
        self.import_overviews_only = False

        if "options" in self.recipe:
            self.wms_import = True if "wms" not in self.recipe["options"] else bool(self.recipe["options"])
            self.pyramid_members = None if "pyramid_members" not in self.recipe["options"] else self.recipe["options"]["pyramid_members"]
            self.pyramid_bases = None if "pyramid_bases" not in self.recipe["options"] else self.recipe["options"][
                "pyramid_bases"]
            # If set to true, then request: /rasdaman/admin/coverage/pyramid/add?COVERAGEID=s2_10m&MEMBERS=s2_60m&harvesting=true
            # mean: recursively add pyramid member coverages of s2_60m coverage to base s2_10m coverage
            self.pyramid_harvesting = False if "pyramid_harvesting" not in self.recipe["options"] else bool(self.recipe["options"]["pyramid_harvesting"])

            if self.pyramid_members is None and self.pyramid_bases is None and self.pyramid_harvesting is True:
                raise RuntimeException("'pyramid_harvesting' setting can be used only, "
                                       "when either 'pyramid_members' or 'pyramid_bases' setting are specified with values in the ingredients file.")

            self.__get_import_overviews()
            self.import_overviews_only = False if "import_overviews_only" not in self.recipe["options"] else bool(self.recipe["options"]["import_overviews_only"])

        # Pre/Post hooks to run before analyzing files/after import replaced files
        # (original files are not used but processed files (e.g: by gdalwarp))
        self.before_hooks = []
        self.after_hooks = []

        if hooks is not None:
            for hook in hooks:
                if hook["when"] == "before_import" or hook["when"] == "before_ingestion":
                    self.before_hooks.append(hook)
                elif hook["when"] == "after_import" or hook["when"] == "after_ingestion":
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
        ConfigManager.black_listed = self.black_listed
        ConfigManager.mock = self.mock
        ConfigManager.tmp_directory = self.tmp_directory if self.tmp_directory[-1] == "/" else self.tmp_directory + "/"
        ConfigManager.root_url = self.root_url
        ConfigManager.wcs_service = self.wcs_service
        ConfigManager.admin_service = get_petascope_endpoint_without_ows(self.wcs_service) + "/admin"

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

    def __get_import_overviews(self):
        """
        Get the OVERVIEWs in the ingredients file if user wants to import
        """

        if "options" in self.recipe:

            if "import_all_overviews" in self.recipe["options"] and "import_overviews" in self.recipe["options"] \
                    and self.recipe["options"]["import_all_overviews"] is True:
                raise RuntimeException("Both settings '{}' or '{}' cannot exist in the ingredients file, "
                                       "please specify only one of them."
                                       .format("import_all_overviews", "import_overviews"))

            self.import_overviews = self.recipe["options"]["import_overviews"] \
                if "import_overviews" in self.recipe["options"] else []

            if len(self.files) > 0 and ("import_all_overviews" in self.recipe["options"] or "import_overviews" in self.recipe["options"]):

                from util.gdal_util import GDALGmlUtil
                first_input_file_gdal = GDALGmlUtil(self.files[0].filepath)

                if "import_all_overviews" in self.recipe["options"] \
                        and bool(self.recipe["options"]["import_all_overviews"]) is True:

                    # import all overviews
                    number_of_overviews = first_input_file_gdal.get_number_of_overviews()
                    self.import_overviews = []
                    if number_of_overviews > 0:
                        self.import_overviews = range(0, number_of_overviews)

                for overview_index in self.import_overviews:
                    if is_integer(overview_index) is False or int(overview_index) < 0:
                        raise RuntimeException("'{}' must contain non-negative integers integer values. "
                                               "Given: {}".format("import_overviews", overview_index))
                    elif self.recipe["name"] != "sentinel2":
                        # NOTE: sentinel 2 custom recipe to import .zip file has special format (xml to combine subdatasets (10m, 20m, 60m))
                        # e.g: SENTINEL2_L2A:/vsizip//home/vagrant/S2A_..._20210601T140140.zip/S2A_..._20210601T140140.SAFE/MTD_MSIL2A.xml:10m:EPSG_32632

                        overview = first_input_file_gdal.get_overview(overview_index)
                        if overview is None:
                            raise RuntimeException(
                                "Overview index '{}' does not exist in the input file '{}'.".format(overview_index,
                                                                                                first_input_file_gdal.gdal_file_path))



                from util.gdal_util import GDALGmlUtil
                # e.g 101140
                gdal_version = int(GDALGmlUtil.get_gdal_version()[0:2])
                if gdal_version < 20:
                    from osgeo import gdal
                    # e.g: 1.11.4
                    version = gdal.__version__
                    raise RuntimeException("NOTE: Importing overviews is only supported since gdal version 2.0, "
                                           "and your system has GDAL version '" + version + "'.")

    def __get_setting_value_from_properties_file(self, properties_file, setting_key):
        """
        Given a properties file, e.g. /opt/rasdaman/etc/petascope.properties and setting key, e.g. secore_urls
        return setting value of this key
        """
        try:
            with open(properties_file) as f:
                pass
        except Exception as ex:
            raise RuntimeException("Cannot open properties file '" + properties_file
                                   + "', reason: " + str(ex)
                                   + ". Hint: make sure user running wcst_import has permission to read this file.")

        with open(properties_file) as f:
            for line in f:
                line = line.strip()
                # e.g: server.port=8080
                if "=" in line and line.startswith(setting_key):
                    value = line.split("=")[1].strip()
                    return value

        raise RuntimeException("Cannot find setting '" + setting_key + "' from properties file '" + properties_file + "'.")

    def __get_crs_resolver_and_embedded_petascope_port_configuration(self):
        """
        From the petascope.properties file which is configured in config_manager.py when building wcst_import,
        it can read the secore_urls configuration of Petascope and set as default crs resolver.
        It must replace the SECORE prefix in ingredient file by petascope's SECORE prefix.
        (e.g: http://localhost:8080/def  to http://opengis.net/def).
        :return: tuple(secore_urls, embedded_petascope_port)
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

        embedded_petascope_port = self.__get_setting_value_from_properties_file(petascope_properties_path,
                                                                                "server.port")

        crs_resolver_urls = self.__get_setting_value_from_properties_file(petascope_properties_path, "secore_url").split(",")
        crs_resolver = self.get_running_crs_resolver(crs_resolver_urls, embedded_petascope_port)

        if crs_resolver is None:
            raise RuntimeException("Cannot find secore_urls configuration "
                                   "in petascope's properties file '{}' for crs_resolver".format(petascope_properties_path))

        # wcst_import needs the SECORE prefix with "/" as last character
        if crs_resolver[-1] != "/":
            crs_resolver += "/"

        return (crs_resolver, embedded_petascope_port)

    def get_running_crs_resolver(self, crs_resolvers, embedded_petascope_port):
        """
        From a list of SECORE configured in petascope.properties, find the first running SECORE
        to be used for wcst_import
        :param string[] crs_resolvers: list of SECORE URLs
        :return: string (the running SECORE)
        """
        i = 0
        crs_resolvers_tmp = []
        for url_prefix in crs_resolvers:
            url_prefix = url_prefix.strip()

            # NOTE: if secore_urls=internal in petascope.properties, it means
            # wcst_import will use the internal SECORE inside petascope with the sub endpoint at /rasdaman/def
            if url_prefix == INTERNAL_SECORE or url_prefix == DEFAULT_SECORE_URL:
                url_prefix = INTERNAL_SECORE_URL

                crs_resolvers_tmp.append(url_prefix)

                # Also, in case petascope runs at different port than 8080
                if embedded_petascope_port != "8080":
                    url_prefix = "http://localhost:" + embedded_petascope_port + INTERNAL_SECORE_URL_CONTEXT_PATH
                    crs_resolvers_tmp.append(url_prefix)
            else:
                crs_resolvers_tmp.append(url_prefix)

        for url_prefix in crs_resolvers_tmp:
            try:
                url_prefix = url_prefix.strip()

                test_url = url_prefix + "/crs/EPSG/0/4326"
                from util.crs_util import CRSUtil
                Session.RUNNING_SECORE_URL = url_prefix
                CRSUtil.get_axis_labels_from_single_crs(test_url)

                # e.g: http://localhost:8080/def
                return url_prefix
            except Exception as ex:
                log.warn("CRS resolver '" + url_prefix + "' is not working.")
                if i < len(crs_resolvers) - 1:
                    log.warn("Trying with another fallback resolver...")
            i += 1

        # none of resolvers work, then assume there is SECORE embedded in petascope
        internal_secore_url = self.wcs_service.replace("/rasdaman/ows", "/rasdaman/def").strip()
        try:
            test_url = internal_secore_url + "/crs/EPSG/0/4326"
            from util.crs_util import CRSUtil
            Session.RUNNING_SECORE_URL = internal_secore_url
            CRSUtil.get_axis_labels_from_single_crs(test_url)

            log.warn("None of the configured secore_urls in petascope.properties respond to requests. "
                     "wcst_import will use the internal CRS resolver at endpoint '" + internal_secore_url + "'. "
                     "Hint: set secore_urls=internal in petascope.properties to suppress this warning.")

            return internal_secore_url
        except Exception as ex:
            raise RuntimeException(
                "No configured CRS resolvers in petascope.properties work. Given: " + ",".join(crs_resolvers))

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
            if not path.startswith("/vsi"):
                file_paths = file_paths + FileUtil.get_file_paths_by_regex(self.ingredients_dir_path, path)
            else:
                file_paths.append(path)

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
        file_obs = [File(f) for f in file_paths]
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
