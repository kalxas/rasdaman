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
import urllib
from master.error.runtime_exception import RuntimeException


def validate_and_read_url(url):
    """
    Open url and validate the status code before returning the response in string
    :param str url: the url to open
    :rtype: str
    """
    try:
        ret = urllib.urlopen(url)
    except Exception as e:
        raise RuntimeException("Failed opening connection to '{}'. "
                               "Check that the service is up and running."
                               "Detail error: {}.".format(url, str(e)))

    response = ret.read()
    if ret.getcode() != 200:
        raise RuntimeException("Server responded failed for request '{}'."
                               "Detail error: {}.".format(url, response))

    return response


def url_read_exception(url, exception_message):
    """
    Open url which could return an exception (e.g: DescribeCoverage when coverage does not exist in Petascope).
    If it returns an exception, check the exception_message is in exception, if not -> not valid.

    :param str url: the url to open
    :param str exception_message: the error message could be in the exception from the requested URL
    :return: boolean (false: if status is 200 or true if message does exist)
    """
    ret = urllib.urlopen(url)
    response = ret.read()
    if ret.getcode() == 200:
        return False
    elif ret.getcode() != 200 and exception_message in response:
        return True

    # Exception message is not in response and status is not 200, so it is an error
    raise RuntimeException("Failed opening connection to '{}'. Check that the service is up and running.".format(url))
