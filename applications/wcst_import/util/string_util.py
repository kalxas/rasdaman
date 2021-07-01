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
from datetime import datetime
import decimal
from xml.sax.saxutils import escape
import random


def stringify(thing):
    """
    Adds quotes to the given parameter and casts to string if the object is not a string
    :param str thing: the thing to stringify
    :rtype: str
    """
    return '"' + str(thing) + '"'


def is_number(value):
    """
    Check if string value is a number (e.g: -5, 2, 3.5, 1e+5)
    :param string value: the value needs to check
    :return boolean:
    """
    try:
        value = float(value)
        return True
    except ValueError:
        return False


def escape_metadata_dict(metadata_dict):
    """
    Escape a dict with values are strings which can contain invalid characters (<, >, &) for XML
    :param metadata_dict:
    :return: an escaped dict
    """
    # NOTE: metadata can contain invalid characters for XML such as: <, >, & then needs to escape them
    for key, value in metadata_dict.items():
        metadata_dict[key] = escape(str(value))

    return metadata_dict


def escape_metadata_nested_dicts(metadata_dict):
    """
    Escape a dict and its children nested dicts (or string) with values are strings which can contain invalid characters (<, >, &) for XML
    :param metadata_dict:
    :return: an escaped dict with nested dicts
    """
    # NOTE: metadata can contain invalid characters for XML such as: <, >, & then needs to escape them
    for key_parent, value_parent in metadata_dict.items():
        if type(value_parent) is dict:
            # e.g: "band1": { "key_1": "value_1", "key_2": "value_2" }
            for key_child, value_child in value_parent.items():
                metadata_dict[key_parent][key_child] = escape(str(value_child))
        else:
            # e.g: ${netcdf:variable:lat:metadata}
            metadata_dict[key_parent] = escape(str(value_parent))

    return metadata_dict


def strip_whitespace(s):
    """
    Strip all whitespace from a string s
    :param str s: input string
    :rtype: str
    """
    return s.strip(" \t\n\r")


def strip_trailing_zeros(number_str):
    """
    Strip any zeros from number in string (e.g: 111.0 -> 111)
    :param str number_str
    """
    if number_str.strip() == '':
        return number_str

    num = decimal.Decimal(number_str)
    return str(num.to_integral()) if num == num.to_integral() else str(num.normalize()).lower()


def replace_template_by_dict(template, keys_values_dict):
    """
    Replace a template string with keys and values from a dict
    :param str template: a string contains place holders
    :param dict keys_values_dict: a dictionary of place holders and their replaced values
    :return: str
    """
    result = template
    for key, value in keys_values_dict.items():
        result = result.replace("{{" + str(key) + "}}", str(value))

    return result


def create_downscaled_coverage_id(base_coverage_id, level):
    """
    Create a downscaled coverage id, base on base coverage id and level
    e.g: covA, level2.5 -> covA_2_5
    """
    return base_coverage_id + "_" + str(level).replace(".", "_")


def add_date_time_suffix(input_str):
    """
    Given a string, add the dd_mm_yy_hh_mm_ss_randomNumber as suffix
    randomNumber between 000
    e.g: covA -> covA_20_11_2020_20_13_55_3322
    """
    date_time = datetime.now().strftime("%d_%m_%Y_%H_%M_%S") + random.randint(0000, 9999).zfill(4)
    return input_str + "_" + date_time


def is_integer(val):
    """
    Check if input string is integer, not float
    :param str val
    """
    try:
        int(val)
        return True
    except ValueError:
        return False


def create_coverage_id_for_overview(base_coverage_id, overview_index):
    """
    From the base coverage id, create the overview coverage id by overview index
    e.g: cov_a with overview 1 -> cov_a_1
    """
    return base_coverage_id + "_" + overview_index

