from recipes.shared.validate_exception import RecipeValidationException
from recipes.time_series_irregular.recipe import Recipe as IrregularTimeSeriesRecipe
from recipes.time_series_regular.recipe import Recipe as RegularTimeSeriesRecipe
from recipes.map_mosaic.recipe import Recipe as MapMosaicRecipe
from session import Session
from util.log import log


class RecipeRegistry:
    def __init__(self):
        """
        The registry contains all the recipes available for the wcst import
        To add one please register it in the _init_registry method
        """
        self.registry = {}
        self._init_registry()

    def _init_registry(self):
        """
        Initializes the registry with all the recipes available
        If a new one is defined it should be added here
        @TODO at some point it could be done dynamically by looking into the recipes directory
        and importing the recipe scripts
        """
        self.registry[IrregularTimeSeriesRecipe.get_name()] = IrregularTimeSeriesRecipe
        self.registry[RegularTimeSeriesRecipe.get_name()] = RegularTimeSeriesRecipe
        self.registry[MapMosaicRecipe.get_name()] = MapMosaicRecipe

    def run_recipe(self, session):
        """
        Recipe session
        :param Session session: the session of the import
        :rtype BaseRecipe
        """
        if session.get_recipe()['name'] not in self.registry:
            raise RecipeValidationException("The recipe requested was not found in the registry. Check that the "
                                            "recipe name is correct and that recipe you are trying to use is"
                                            "found under recipes/")
        else:
            recipe = self.registry[session.get_recipe()['name']](session)
            log.title("Initialization")
            log.info("Recipe chosen: " + recipe.get_name())
            log.info("Collected files: " + str(session.get_files()))

            log.title("\nValidation")
            recipe.validate()
            recipe.describe()
            if not session.is_automated():
                raw_input("Press Enter to Continue...: ")
            log.title("\nRunning")
            recipe.run()
            log.success("Recipe executed successfully")