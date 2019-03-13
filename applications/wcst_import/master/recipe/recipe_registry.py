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
import sys
import subprocess
import glob2
from threading import Thread
from time import sleep

from master.evaluator.evaluator_slice_factory import EvaluatorSliceFactory
from master.evaluator.expression_evaluator_factory import ExpressionEvaluatorFactory
from master.evaluator.file_expression_evaluator import FileExpressionEvaluator
from master.evaluator.sentence_evaluator import SentenceEvaluator
from master.recipe.base_recipe import BaseRecipe
from master.error.validate_exception import RecipeValidationException
from session import Session
from util.file_obj import File, FilePair
from util.log import log, make_bold
from util.reflection_util import ReflectionUtil
from recipes.general_coverage.recipe import Recipe as GeneralRecipe
from recipes.general_coverage.gdal_to_coverage_converter import GdalToCoverageConverter


class RecipeRegistry:
    def __init__(self):
        """
        The registry contains all the recipes available for the wcst import
        To add one please register it in the _init_registry method
        """
        self.registry = {}
        self._init_registry()
        self.sentence_evaluator = SentenceEvaluator(ExpressionEvaluatorFactory())

    def _init_registry(self):
        """
        Initializes the registry with all the recipes available by looking into the recipes folder
        """
        util = ReflectionUtil()

        # load "official" rasdaman recipes manually
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

        # user-created recipes are put in the recipes_custom directory, so
        # wcst_import needs to load all possible recipes in this directory.
        import recipes_custom
        util.import_submodules(recipes_custom)

        # register all loaded recipes
        all_recipes = util.get_all_subclasses(BaseRecipe)
        for recipe in all_recipes:
            self.registry[recipe.get_name()] = recipe

    def __run_shell_command(self, command, abort_on_error=False):
        """
        Run a shell command and exit wcst_import if needed
        :param str command: shell command to run
        """
        try:
            log.info("Executing shell command '{}'...".format(command))
            output = subprocess.check_output(command, stderr=subprocess.STDOUT, shell=True)
            log.info("Output result '{}'".format(output))
        except subprocess.CalledProcessError as exc:
            log.warn("Failed, status code '{}', error message '{}'.".format(exc.returncode, str(exc.output).strip()))
            if abort_on_error:
                log.error("wcst_import terminated on running hook command.")
                exit(1)

    def __run_hooks(self, session, hooks):
        """
        Run some hooks before/after analyzing input files
        :param Session session:
        :param dict[str:str] hooks: dictionary of before and after ingestion hooks
        """
        # gdal (default), netcdf or grib
        recipe_type = GdalToCoverageConverter.RECIPE_TYPE
        if session.recipe["name"] == GeneralRecipe.RECIPE_TYPE:
            recipe_type = session.recipe["options"]["coverage"]["slicer"]["type"]

        for hook in hooks:
            abort_on_error = False if "abort_on_error" not in hook else bool(hook["abort_on_error"])

            if "description" in hook:
                log.info("Executing hook '{}'...".format(make_bold(hook["description"])))

            replace_paths = []
            replace_path_template = None
            if "replace_path" in hook:
                # All replaced input files share same template format (e.g: file:path -> file:path.projected)
                replace_path_template = hook["replace_path"][0]

            # Evaluate shell command expression to get a runnable shell command
            cmd_template = hook["cmd"]

            for file in session.files:
                evaluator_slice = EvaluatorSliceFactory.get_evaluator_slice(recipe_type, file)
                cmd = self.sentence_evaluator.evaluate(cmd_template, evaluator_slice)
                self.__run_shell_command(cmd, abort_on_error)

                if FileExpressionEvaluator.FILE_PATH_EXPRESSION not in cmd_template:
                    # Only need to run hook once if ${file:path} does not exist in cmd command,
                    # otherwise it runs duplicate commands for nothing (!)
                    break

                if replace_path_template is not None:
                    # Evaluate replace path expression to get a valid file input path
                    replace_path = self.sentence_evaluator.evaluate(replace_path_template, evaluator_slice)
                    tmp_files = glob2.glob(replace_path)
                    for tmp_file in tmp_files:
                        if not isinstance(file, FilePair):
                            # The first replacement (must keep original input file path)
                            replace_paths.append(FilePair(tmp_file, file.filepath))
                        else:
                            # From the second replacement
                            replace_paths.append(FilePair(tmp_file, file.original_file_path))

            if len(replace_paths) > 0:
                # Use replaced file paths instead of original input file paths to analyze and create coverage slices
                session.files = replace_paths

    def __run_recipe(self, session, recipe):
        """
        Run recipe
        :param Session session:
        :param BaseRecipe recipe:
        """

        if session.before_hooks:
            log.info(make_bold("Executing before ingestion hook(s)..."))
            self.__run_hooks(session, session.before_hooks)

        recipe.describe()

        if session.blocking and not session.is_automated():
            raw_input("Press Enter to Continue...: ")

        log.title("\nRunning")

        if session.blocking:
            # It needs to display progress bar (how many files imported)
            t = Thread(target=run_status, args=(recipe,))
            t.daemon = True
            t.start()
            recipe.run()
            t.join()
        else:
            # Non-blocking mode, only 1 file is importing, no need to show progress bar (it takes longer time to join threads)
            recipe.run()

        recipe.importer = None

        if session.after_hooks:
            log.info(make_bold("Executing after ingestion hook(s)..."))
            self.__run_hooks(session, session.after_hooks)

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
            number_of_files = len(session.get_files())
            if number_of_files > 10:
                number_of_files = 10
            log.info("Collected first " + str(number_of_files) + " files: "
                     + str(map(lambda f: str(f), session.get_files()[:10])) + "...")

            log.title("\nValidation")
            recipe.validate()

            # Show what recipe and coverage are imported only once
            super(recipe.__class__, recipe).describe()

            if session.blocking is True:
                # Default blocking import mode (analyze all files -> import)
                self.__run_recipe(session, recipe)
            else:
                # Non blocking import mode (analyze 1 file -> import then next file)
                files = list(session.get_files())

                for file in files:
                    session.files = [file]
                    self.__run_recipe(session, recipe)

            log.success("Recipe executed successfully")


def update_progress(processed_items, total):
    """
    Updates the progress using a progressbar
    """
    if os.isatty(1):
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
