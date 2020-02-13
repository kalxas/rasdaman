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
from master.error.runtime_exception import RuntimeException
from util.log import log

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
        raise RuntimeException("Numpy package is not installed, please install it first (sudo pip install numpy)."
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
            raise RuntimeException("Cannot import GRIB data, please install pygrib first (sudo pip install pygrib). "
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
                                    (yum install netcdf4-python, or apt-get install python-netcdf, or apt-get install python-netcdf4)."
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
          To enable validation please install jsonschema (sudo pip install jsonschema)")
         pass
