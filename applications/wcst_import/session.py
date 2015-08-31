import glob2 as glob
from config_manager import ConfigManager
from util.file_obj import File

from util.log import log
from wcst.wcst import WCSTMockExecutor, WCSTExecutor


class Session:
    def __init__(self, config, inp, recipe, ingredients_dir_path):
        """
        This class is used to hold the configuration for this importing session
        :param dict[str,str] config: the config part of the json input
        :param dict[str,str] inp: the input part of the json input
        :param dict[str,dict|str] recipe: the recipe configuration
        :param str ingredients_dir_path: the filepath to the directory containing the ingredients to be used
        for relative paths
        :rtype: Session
        """
        self.config = config
        self.ingredients_dir_path = ingredients_dir_path if ingredients_dir_path.endswith("/") \
            else ingredients_dir_path + "/"
        self.files = self.parse_input(inp['paths'] if 'paths' in inp else [])
        self.coverage_id = inp['coverage_id'] if 'coverage_id' in inp else None
        self.recipe = recipe
        self.wcs_service = config['service_url'] if "service_url" in config else None
        self.tmp_directory = config['tmp_directory'] if "tmp_directory" in config else "/tmp/"
        self.crs_resolver = config['crs_resolver'] if "crs_resolver" in config else None
        self.default_crs = config['default_crs'] if "default_crs" in config else None
        self.insitu = config['insitu'] if "insitu" in config else None
        self.default_null_values = config['default_null_values'] if "default_null_values" in config else []
        self.mock = False if "mock" not in config else bool(self.config["mock"])
        self.root_url = self.config["root_url"] if "root_url" in self.config else "file://"
        self.subset_correction = bool(self.config['subset_correction']) if "subset_correction" in self.config else False
        self.skip = bool(self.config['skip']) if "skip" in self.config else False
        self.retry = bool(self.config['retry']) if "retry" in self.config else False
        self.slice_restriction = self.config['slice_restriction'] if "slice_restriction" in self.config else None
        self.retries = int(self.config['retries']) if "retries" in self.config else 5
        self.retry_sleep = float(self.config['retry_sleep']) if "retry_sleep" in self.config else 1
        self.resumer_dir_path = self.resumer_dir_path if "resumer_dir_path" in self.config else self.ingredients_dir_path
        self.setup_config_manager()

    def setup_config_manager(self):
        """
        Initializes the configuration manager
        """
        ConfigManager.automated = self.is_automated()
        ConfigManager.crs_resolver = self.crs_resolver if self.crs_resolver[-1] == "/" else self.crs_resolver + "/"
        ConfigManager.default_crs = self.default_crs
        ConfigManager.default_null_values = self.default_null_values
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

    def parse_input(self, paths):
        """
        Parses the list of paths and returns an ordered list of complete file paths
        :param list[str] paths: the list of paths
        :rtype list[File]
        """
        file_paths = []
        for path in paths:
            path = path.strip()
            if not path.startswith("/"):
                path = self.ingredients_dir_path + path
            file_paths = file_paths + glob.glob(path)
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

    def get_coverage_id(self):
        """
        Returns the coverage id for this session
        :rtype str
        """
        return self.coverage_id

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
        if self.mock:
            return WCSTMockExecutor(self.get_wcs_service(), self.insitu)
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
