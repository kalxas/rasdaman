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
from xml.sax.saxutils import escape

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
    for key, value in metadata_dict.iteritems():
        metadata_dict[key] = escape(value)
    return metadata_dict