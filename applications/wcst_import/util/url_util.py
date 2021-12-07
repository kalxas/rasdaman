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
import sys

from util.string_util import parse_error_message

if sys.version_info[0] < 3:
    from urllib2 import Request, urlopen
else:
    from urllib.request import Request, urlopen

import urllib, base64
import ssl
from master.error.runtime_exception import RuntimeException
from util.import_util import decode_res

def __encode_quote(url):
    """
    Encode " from URL
    :param str url: URL to be encoded
    """
    return url.replace('"', "%22")


def validate_and_read_url(url, data=None, timeout_in_seconds=None):
    """
    Open url and validate the status code before returning the response in string
    :param str url: the url to open
    :param dict data: POST parameters
    :rtype: str
    """
    url = __encode_quote(url)

    try:
        request = Request(url)
        if data is not None:
            if sys.version_info[0] < 3:
                request.add_data(data)
            else:
                request.data = bytes(data, encoding = "ISO-8859-1")

        from config_manager import ConfigManager

        if ConfigManager.user is not None:
            tmp = (ConfigManager.user + ":" + ConfigManager.passwd).encode("utf-8")
            base64string = base64.b64encode(tmp).decode("utf-8")
            request.add_header("Authorization", "Basic %s" % base64string)

        ret = urlopen(request, timeout=timeout_in_seconds, context=ssl._create_unverified_context())
    except Exception as e:
        if hasattr(e, "reason"):
            if hasattr(e.reason, "strerror"):
                # URLError
                exception_text = e.reason.strerror
            else:
                # HTTPError
                exception_text = decode_res(e.read())

            error_message = parse_error_message(exception_text)
        else:
            error_message = str(e)

        raise RuntimeException("Failed opening connection to '{}'. \n"                               
                               "Reason: {}".format(url, error_message))
    response = ret.read()

    if ret.getcode() != 200:
        raise RuntimeException("Server failed to respond for request '{}'. "
                               "Reason: {}".format(url, response))

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
    ret = urlopen(url, context=ssl._create_unverified_context())
    response = ret.read()
    if ret.getcode() == 200:
        return False
    elif ret.getcode() != 200 and exception_message in response:
        return True

    # Exception message is not in response and status is not 200, so it is an error
    raise RuntimeException("Failed opening connection to '{}'. Check that the service is up and running.".format(url))
