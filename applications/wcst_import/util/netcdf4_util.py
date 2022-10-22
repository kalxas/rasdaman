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
 * aint with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
from util.gdal_util import TIME_OUT_IF_FILE_CANNOT_BE_OPENED, MAX_RETRIES_TO_OPEN_FILE
from util.import_util import import_netcdf4
from util.time_util import timeout, execute_with_retry_on_timeout


def netcdf4_open(file_path):
    dataset = execute_with_retry_on_timeout(MAX_RETRIES_TO_OPEN_FILE,
                                  "Failed to open netCDF file '{}'",
                                            __netcdf4_open_dataset, file_path)
    return dataset


@timeout(TIME_OUT_IF_FILE_CANNOT_BE_OPENED)
def __netcdf4_open_dataset(*args):
    filepath = args[0][0]
    netCDF4 = import_netcdf4()
    dataset = netCDF4.Dataset(filepath, "r")
    return dataset
