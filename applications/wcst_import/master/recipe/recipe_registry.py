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

import sys
from threading import Thread
from time import sleep

from master.recipe.base_recipe import BaseRecipe
from master.error.validate_exception import RecipeValidationException
from session import Session
from util.log import log
from util.reflection_util import ReflectionUtil


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

        # NOTE: Add provided recipes from rasdaman here manually.
        import recipes.general_coverage
        import recipes.map_mosaic
        import recipes.time_series_irregular
        import recipes.time_series_regular
        import recipes.wcs_extract

        util.import_submodules(recipes.general_coverage)
        util.import_submodules(recipes.map_mosaic)
        util.import_submodules(recipes.time_series_irregular)
        util.import_submodules(recipes.time_series_regular)
        util.import_submodules(recipes.wcs_extract)

        # NOTE: If user ever created a customized recipe, then the recipe needs to be added in recipes_custom folder
        # And in this case, WCST_Import just load all possible recipes in this folder.
        import recipes_custom
        util.import_submodules(recipes_custom)

        for recipe in BaseRecipe.__subclasses__():
            self.registry[recipe.get_name()] = recipe

    def run_recipe(self, session):
        """
        Recipe session
        :param Session session: the session of the import
        :rtype BaseRecipe
        """
        if session.get_recipe()['name'] not in self.registry:
            raise RecipeValidationException("Recipe '" + session.get_recipe()['name'] + "' not found; "
                                            "if it's a custom recipe, please put it in the "
                                            "'$RMANHOME/share/rasdaman/wcst_import/recipes_custom' folder.")
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
    while processed_items < total or total == 0:
        processed_items, total = recipe.status()
        if total != 0 and total != -1:
            update_progress(processed_items, total)
        if total == -1:
            return
        sleep(0.01)
