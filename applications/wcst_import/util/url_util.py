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
import urllib, urllib2, base64
import ssl
from master.error.runtime_exception import RuntimeException

def __encode_quote(url):
    """
    Encode " from URL
    :param str url: URL to be encoded
    """
    return url.replace('"', "%22")


def validate_and_read_url(url, data=None):
    """
    Open url and validate the status code before returning the response in string
    :param str url: the url to open
    :param dict data: POST parameters
    :rtype: str
    """
    url = __encode_quote(url)
    try:
        request = urllib2.Request(url)
        if data is not None:
            request.add_data(data)

        from config_manager import ConfigManager

        if ConfigManager.user is not None:
            base64string = base64.b64encode(ConfigManager.user + ":" + ConfigManager.passwd)
            request.add_header("Authorization", "Basic %s" % base64string)

        ret = urllib2.urlopen(request, context=ssl._create_unverified_context())
    except Exception as e:
        raise RuntimeException("Failed opening connection to '{}'. "
                               "Check that the service is up and running."
                               "Detail error: {}.".format(url, e.read()))

    response = ret.read()
    if ret.getcode() != 200:
        raise RuntimeException("Server responded failed for request '{}'."
                               "Detail error: {}.".format(url, response))

    if "<html" in response:
        raise RuntimeException("Server requests credentials for authentication,"
                               "please provide them (check wcst_import.sh -h for details)")

    return response


def url_read_exception(url, exception_message):
    """
    Open url which could return an exception (e.g: DescribeCoverage when coverage does not exist in Petascope).
    If it returns an exception, check the exception_message is in exception, if not -> not valid.

    :param str url: the url to open
    :param str exception_message: the error message could be in the exception from the requested URL
    :return: boolean (false: if status is 200 or true if message does exist)
    """
    url = __encode_quote(url)
    ret = urllib2.urlopen(url, context=ssl._create_unverified_context())
    response = ret.read()
    if ret.getcode() == 200:
        return False
    elif ret.getcode() != 200 and exception_message in response:
        return True

    # Exception message is not in response and status is not 200, so it is an error
    raise RuntimeException("Failed opening connection to '{}'. Check that the service is up and running.".format(url))
