#!/usr/bin/env python
"""
Utility to insert coverages into rasdaman using WCST
"""
import json
import sys
import traceback

from recipes.shared.recipe_registry import RecipeRegistry
from recipes.shared.runtime_exception import RuntimeException
from recipes.shared.validate_exception import RecipeValidationException
from session import Session
from util.fileutil import FileUtil
from util.log import log
from wcst.wcst import WCSTException


def print_usage():
    """
    Prints the usage of the program
    """
    log.title("NAME")
    log.info("\tWCST Import - imports georeferenced files to a WCS service that supports the Transactional extension.")
    log.title("\nSYNOPSIS")
    log.info("\twcst_import.py ingredients.json")
    log.title("\nDESCRIPTION")
    log.info("\tThe WCST Import utility imports georeferenced files supported by GDAL into a WCS service that supports "
             "Transactional extension.\n\tThis utility is based on a recipe (custom code for a specific scenario) being"
             " used to transform the ingredients file (input files and configuration options)."
             "\n\n\tThe following recipes are supported as now:"
             "\n\t  * 2D Mosaic - Given a set of 2D GDAL files, the recipe will produce a 2D mosaic of the given files."
             "Ingredients example under ingredients/map_mosaic.json"
             "\n\t  * Regular Timeseries  - Given a set of 2D GDAL files, the recipe will build a timeseries out of "
             "them. The initial time of the series and the step for each additional file must be provided. "
             "Ingredients example under ingredients/time_series_regular"
             "\n\t  * Irregular - Given a set of 2D GDAL files, the recipe will produce  a timeseries out of "
             "them. The initial time of the series and the step for each additional file must be provided. "
             "Ingredients example under ingredients/time_series_irregular"
             )


def exit_error():
    """
    Exits the program with an error code
    """
    exit(1)


def validate():
    """
    Validates the commandline arguments (i.e. makes sure it's only one)
    """
    if len(sys.argv) == 1:
        print_usage()
        exit(1)
    if len(sys.argv) != 2:
        log.error("WCST Import expects exactly one argument. See the manual entry for the script below:")
        print_usage()
        exit_error()


def read_ingredients():
    """
    Reads the ingredient file and returns a string of json
    :rtype: str
    """
    path = ""
    try:
        path = sys.argv[1]
        return file(path).read()
    except IOError:
        log.error("We could not read the ingredients file at " + path + ". Make sure the file exists and is readable.")
        exit_error()


def decode_ingredients(ingredients_raw):
    """
    Decodes the raw json ingredients string into a python dict
    :param str ingredients_raw: the raw json string
    :rtype: dict[str,dict|str|bool|int|float]
    """
    try:
        return json.loads(ingredients_raw)
    except Exception as ex:
        log.error("We could not decode the ingredients file. This is usually due to " \
                  "a problem with the json format. Please check that you have a valid json file." \
                  " The JSON decoder error was: " + str(ex))
        exit_error()


def main():
    """
    Main function to put the pieces together and run the recipe
    """
    reg = RecipeRegistry()
    validate()
    try:
        ingredients = decode_ingredients(read_ingredients())
        session = Session(ingredients['config'], ingredients['input'], ingredients['recipe'],
                          FileUtil.get_directory_path(sys.argv[1]))
        reg.run_recipe(session)
    except RecipeValidationException as re:
        log.error(str(re))
        exit_error()
    except RuntimeException as re:
        log.error(str(re))
        exit_error()
    except WCSTException as re:
        log.error(str(re))
        exit_error()
    except Exception as ex:
        log.error("An error has occured in the execution of the program. Error Message: " + str(
            ex) + "\nStack Trace: " + traceback.format_exc())
        exit_error()


if __name__ == "__main__":
    main()