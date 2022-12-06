#!/usr/bin/env python
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

"""
Utility to insert coverages into rasdaman using WCST
"""
import os
import json
import sys
import traceback
import decimal

from master.error.runtime_exception import RuntimeException

from master.recipe.recipe_registry import RecipeRegistry
from master.error.validate_exception import RecipeValidationException
from session import Session
from util.file_util import FileUtil
from util.log import log
from util.import_util import import_jsonschema
from master.request.wcst import WCSTException
from osgeo import gdal
from config_manager import ConfigManager

from ArgumentsParser import parse_arguments
from util.log import log_to_file
from util.import_util import check_required_libraries

# Enable GDAL/OGR exceptions
gdal.UseExceptions()
# Prevent GDAL from printing errors itself, leave it to wcst_import to handle
# the printing of exception texts
gdal.PushErrorHandler('CPLQuietErrorHandler')
# Avoid slowdown when importing files in directories with many other files
# https://trac.osgeo.org/gdal/wiki/ConfigOptions#GDAL_DISABLE_READDIR_ON_OPEN
gdal.SetConfigOption('GDAL_DISABLE_READDIR_ON_OPEN', 'TRUE')


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


def load_schema():
    script_dir = os.path.dirname(os.path.abspath( __file__ ))
    try:
        with open(script_dir + "/ingredients/ingredients_schema.json", "r") as ingredients_schema_fd:
            ingredients_schema = json.load(ingredients_schema_fd)
            return ingredients_schema
    except IOError as e:
        log.error("We could not open the ingredients schema file. Make sure the file exists and is readable.\n" + str(e))
        exit_error()

def validate_ingredients(ingredients):
    """
    Validates against unknown settings
    """
    jsonschema = import_jsonschema()
    if jsonschema is not None:
        ingredients_schema = load_schema()
        try:
            jsonschema.validate(ingredients, ingredients_schema)
        except NameError:
            pass
        except jsonschema.exceptions.ValidationError as e:
            log.error("The ingredients file contains unknown option(s): \n" + str(e.message))
            exit_error()

def validate():
    """
    Validates the commandline arguments
    """
    if len(sys.argv) == 1:
        print_usage()
        exit(1)

    # daemon flag and watch flag are optional
    if len(sys.argv) <= 2 and len(sys.argv) >= 4:
        log.error("WCST Import: wrong number of arguments. See the manual entry for the script below:")
        print_usage()
        exit_error()


def read_ingredients(ingredient_file):
    """
    Reads the ingredient file and returns a string of json
    :rtype: str
    """
    try:
        file_contents = open(ingredient_file)
        raw_content = file_contents.read()
        file_contents.close()
        # Remove any comments (staring with //) from ingredient file
        lines = raw_content.splitlines()

        result = ""
        for line in lines:
            if not line.lstrip().startswith("//"):
                result += line

        return result

    except IOError:
        log.error("We could not read the ingredients file at '" + ingredient_file + "'. Make sure the file exists and is readable.")
        exit_error()


def decode_ingredients(ingredients_raw):
    """
    Decodes the raw json ingredients string into a python dict
    NOTE: keep all the numbers as decimal to avoid losing precision (e.g: 0.041666666666666666666666 to 0.04116666666667 as float)
    :param str ingredients_raw: the raw json string
    :rtype: dict[str,dict|str|bool|int|float]
    """
    try:
        ingredients = json.loads(ingredients_raw, parse_float = decimal.Decimal)
    except Exception as ex:
        log.error("We could not decode the ingredients file. This is usually due to " \
                  "a problem with the json format. Please check that you have a valid json file." \
                  " The JSON decoder error was: " + str(ex))
        exit_error()

    validate_ingredients(ingredients)

    return ingredients


def main():
    """
    Main function to put the pieces together and run the recipe
    """

    # NOTE: not allow GDAL to create auxilary file which causes problem when no permission on the input data folder
    command = "export GDAL_PAM_ENABLED=NO"
    os.system(command)

    # for each time interval, daemon must have new caches loaded from .resume.json
    from master.importer.resumer import Resumer
    Resumer.clear_caches()

    from util.coverage_util import CoverageUtilCache
    CoverageUtilCache.clear_caches()

    Session.clear_caches()

    reg = RecipeRegistry()
    validate()

    # Parse input arguments from command line
    arguments = parse_arguments()
    ingredients_file_path = arguments.ingredients_file

    ConfigManager.user = arguments.user
    ConfigManager.passwd = arguments.passwd

    if arguments.gdal_cache_size is None:
        arguments.gdal_cache_size = -1
    try:
        ConfigManager.gdal_cache_size = int(arguments.gdal_cache_size)
        if ConfigManager.gdal_cache_size < -1:
            # this exception will be caught in the except below
            raise RuntimeException("")
    except Exception as ex:
        raise RecipeValidationException("Value for gdal-cache-size must be integer >= -1. Given: " + str(arguments.gdal_cache_size))

    if arguments.identity_file is not None:
        key_value = FileUtil.read_file_to_string(arguments.identity_file)
        if ":" not in key_value:
            raise RuntimeException("credentials in the identity file '" + arguments.identity_file + "' "
                                    "must be specified in the file as username:password")
        tmps = key_value.split(":")
        ConfigManager.user = tmps[0].strip()
        ConfigManager.passwd = tmps[1].strip()

    try:
        ingredients = decode_ingredients(read_ingredients(ingredients_file_path))
        hooks = ingredients["hooks"] if "hooks" in ingredients else None
        session = Session(ingredients['config'], ingredients['input'], ingredients['recipe'], hooks,
                          os.path.basename(ingredients_file_path),
                          FileUtil.get_directory_path(ingredients_file_path))

        check_required_libraries()
        reg.run_recipe(session)
    except Exception as ex:
        # log error message to console
        error_message = "Failed to import data. Reason: " + str(ex)
        log.error(error_message)

        # log stack trace to log file (coverage_id.log)
        error_message += "\nStack trace: " + traceback.format_exc()
        log_to_file(error_message)
        exit_error()


if __name__ == "__main__":
    main()
