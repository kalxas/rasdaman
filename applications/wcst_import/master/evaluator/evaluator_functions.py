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

import re
from dateutil.parser import parse

from lib import arrow

"""
Any functions that can be called from the evaluators should be added to evaluator_utils dictionary
"""


def grib_datetime(grib_date, grib_time):
    """
    Parses grib datetime strings
    :param int grib_date:
    :param int grib_time:
    :return:
    """
    grib_time_str = str(grib_time)
    if len(grib_time_str) < 4:
        grib_time_str = '0' + grib_time_str
    return parse(str(grib_date) + "T" + grib_time_str).isoformat()


def datetime(datetime, format=None):
    """
    Parses datetime strings or lists of date strings
    :param str datetime: the datetime string
    :param str format: the format or none
    :return:
    """
    if type(datetime) != list:
        return single_datetime(datetime, format)
    else:
        return [single_datetime(x, format) for x in datetime]


def single_datetime(datetime, format):
    if format:
        return arrow.get(datetime, format).isoformat()
    else:
        return arrow.get(datetime).isoformat()


def regex_extract(input, regex, group):
    """
    Extracts a pattern from a regex
    :param str input: the input to extract from
    :param str regex: the regex to match
    :param int group: the group from the regex to extract
    :return:
    """
    return re.search(regex, input).group(group)


def replace(input, old_part, new_part):
    """
    Replace old_part by new_part from an input string
    :param str input: an input (e.g: a file name: test_123.tif)
    :param str old_part: a part of input to be replaced  (e.g: .tif)
    :param str new_part: a part of input after replacement (e.g: .xml)
    :return: a replace input (e.g: test_123.xml)
    """
    return input.replace(old_part, new_part)

evaluator_utils = {
    "grib_datetime": grib_datetime,
    "regex_extract": regex_extract,
    "datetime": datetime,
    "replace": replace
}
