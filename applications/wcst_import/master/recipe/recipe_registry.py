import sys
from threading import Thread
from time import sleep

from master.recipe.base_recipe import BaseRecipe
from master.error.validate_exception import RecipeValidationException
from session import Session
from util.log import log
from util.reflection_util import ReflectionUtil
import recipes


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
        Initializes the registry with all the recipes available by looking into the recipes folder
        """
        util = ReflectionUtil()
        util.import_submodules(recipes)
        for recipe in BaseRecipe.__subclasses__():
            self.registry[recipe.get_name()] = recipe

    def run_recipe(self, session):
        """
        Recipe session
        :param Session session: the session of the import
        :rtype BaseRecipe
        """
        if session.get_recipe()['name'] not in self.registry:
            raise RecipeValidationException("The recipe requested was not found in the registry. Check that the "
                                            "recipe name is correct and that recipe you are trying to use is "
                                            "found under recipes/ and extends BaseRecipe")
        else:
            recipe = self.registry[session.get_recipe()['name']](session)
            log.title("Initialization")
            log.info("Collected files: " + str(map(lambda f: str(f), session.get_files()[:10])) + "...")

            log.title("\nValidation")
            recipe.validate()
            recipe.describe()
            if not session.is_automated():
                raw_input("Press Enter to Continue...: ")
            t = Thread(target=run_status, args=(recipe,))
            t.daemon = True
            log.title("\nRunning")
            t.start()
            recipe.run()
            t.join()
            log.success("Recipe executed successfully")


def update_progress(processed_items, total):
    """
    Updates the progress using a progressbar
    """
    progress = float(processed_items) / float(total)
    bar_length = 30
    status = ""
    if isinstance(progress, int):
        progress = float(progress)
    if progress >= 1:
        progress = 1
        status = "Done.\r\n"
    block = int(round(bar_length * progress))
    text = "\rProgress: [{0}] {3}/{4} {1:.2f}% {2}".format("#" * block + "-" * (bar_length - block), progress * 100,
                                                           status, processed_items, total)
    sys.stdout.write(text)
    sys.stdout.flush()


def run_status(recipe):
    """
    Function to run in a different thread that continuously checks the progress
    :param BaseRecipe recipe: the recipe to get the status from
    """
    processed_items, total = 0, 1
    while processed_items < total or total == 0 or processed_items != -1:
        processed_items, total = recipe.status()
        if total != 0 and total != -1:
            update_progress(processed_items, total)
        sleep(1)
