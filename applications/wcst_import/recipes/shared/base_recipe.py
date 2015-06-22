from abc import ABCMeta, abstractmethod
import os

from recipes.shared.validate_exception import RecipeValidationException
from util.coverage_util import CoverageUtil
from util.log import log


class BaseRecipe:
    """
    This class represents an abstract
    """
    __metaclass__ = ABCMeta

    def __init__(self, session):
        """
        Initializes the recipe
        :param session: the session for the import tun
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
    def insert(self):
        """
        This method is called when the import is run and no coverage id with the same name exists
        on the wcs server. In this case, you should assume that you have to insert a coverage and all
        the data associated with it.
        """
        pass

    @abstractmethod
    def update(self):
        """
        This method is called when the import is run and a coverage already exists. In this case, you should assume
        that some initial data was already ingested and new parts are added replaced. You are highly encouraged to make
        little to no assumptions about what already was ingested in your recipe and should treat each file individually.
        """
        pass

    @abstractmethod
    def describe(self):
        """
        This methods is called before insert or update is run. You should override the method and add any comments
        regarding the operations that you will perform via log.info to inform the user. You should explicitly state
        the information that you deduced (e.g. timestamps for a timeseries) so that the consequences are clear.
        """
        cov = CoverageUtil(self.session.get_wcs_service(), self.session.get_coverage_id())
        operation_type = "UPDATE" if cov.exists() else "INSERT"
        log.info("The recipe has been validated and is ready to run.")
        log.info("The following recipe has been chosen: " + self.session.get_recipe()['name'])
        log.info("The following operation type has been deduced: " + operation_type)
        pass

    @abstractmethod
    def status(self):
        """
        This method is called continuously to find out the status of the recipe. Use it to print information only
        when necessary and always return a tuple of form (numberOfItemsProcessed, numberOfTotalItems)
        :rtype (int, int)
        """
        pass

    def run(self):
        """
        Runs the recipe
        """
        cov = CoverageUtil(self.session.get_wcs_service(), self.session.get_coverage_id())
        if cov.exists():
            self.update()
        else:
            self.insert()
        pass

    def validate_base(self):
        """
        Validates the configuration and the input files
        """
        if self.session.get_wcs_service() is None or self.session.get_wcs_service() == "":
            raise RecipeValidationException("No valid wcs endpoint provided")
        if self.session.get_crs_resolver() is None or self.session.get_crs_resolver() == "":
            raise RecipeValidationException("No valid crs resolver provided")
        if self.session.get_coverage_id() is None or self.session.get_coverage_id() == "":
            raise RecipeValidationException("No valid coverage id provided")
        if self.session.get_util().tmp_path is None or (not os.access(self.session.get_util().tmp_path, os.W_OK)):
            raise RecipeValidationException("No valid tmp directory provided")
        if len(self.session.get_files()) == 0:
            raise RecipeValidationException("No files provided. Check that the paths you provided are correct.")
        for file_path in self.session.get_files():
            if not os.access(file_path, os.R_OK):
                raise RecipeValidationException("File on path " + file_path + " is not accessible")

    @staticmethod
    @abstractmethod
    def get_name():
        pass
