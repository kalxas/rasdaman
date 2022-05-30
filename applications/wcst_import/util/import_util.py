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
from master.error.runtime_exception import RuntimeException
from util.log import log
import sys
import urllib, base64
if sys.version_info[0] < 3:
    from urllib2 import Request, urlopen
    from urllib import urlencode
else:
    from urllib.request import Request, urlopen
    from urllib.parse import urlencode

"""
  Utilities to import optional dependencies and throw proper exceptions for user to install these missing libraries. 
"""

imported_pygrib = None
imported_netcdf = None


def import_numpy():
    """
    Import numpy library
    """
    try:
        import numpy
    except ImportError as e:
        raise RuntimeException("Numpy package is not installed, please install it first (sudo pip3 install numpy)."
                               "Reason: {}.".format(e))


def import_pygrib():
    """
    Import pygrib which is used for importing GRIB file.
    """
    global imported_pygrib
    if imported_pygrib is None:
        try:
            import pygrib
            imported_pygrib = pygrib
        except ImportError as e:
            raise RuntimeException("Cannot import GRIB data, please install pygrib first (sudo pip3 install pygrib). "
                                   "Reason: {}.".format(e))

    return imported_pygrib


def import_netcdf4():
    """
    Import netCDF4 which is used for importing netCDF file.
    """
    global imported_netcdf
    if imported_netcdf is None:
        try:
            import netCDF4
            imported_netcdf = netCDF4
        except ImportError as e:
            raise RuntimeException("Cannot import netCDF data, please install netCDF4 first \
                                    (sudo pip3 install netCDF4)."
                                   "Reason: {}.".format(e))

    return imported_netcdf


def import_jsonschema():
    """
    Import jsonschema which is used for validating the options in the ingredients file
    """
    try:
        import jsonschema
        return jsonschema
    except ImportError:
         log.warning("The jsonschema package is not installed, ingredient file validation will be skipped. \
          To enable validation please install jsonschema (sudo pip3 install jsonschema)")
         pass


def encode_res(data):
    """
    Encoding of data according to python version
    """
    if sys.version_info[0] < 3:
        return data
    return bytes(data, encoding = "ISO-8859-1")

def decode_res(data):
    """
    Decoding of data according to python version
    """
    if sys.version_info[0] < 3 or isinstance(data, str):
        return data
    return data.decode("ISO-8859-1")

def import_glob():
    """
    Importing glob according to python version
    """
    try:
        if sys.version_info[0] < 3:
            import glob2 as glob
        else:
            import glob
        return glob
    except ImportError:
        log.warning("The glob package is not installed, ingredient file validation will be skipped. \
        To enable validation please install glob (sudo pip3 install glob)")
        pass


def import_requests():
    """
    Import requests library
    """
    try:
        import requests
        return requests
    except ImportError as e:
        raise RuntimeException("Cannot import requests library, please install it first (sudo pip3 install requests). "
                               "Reason: {}.".format(e))
