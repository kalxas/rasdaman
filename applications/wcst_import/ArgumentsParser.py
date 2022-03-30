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
 * Copyright 2003 - 2019 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
import argparse


def parse_arguments():
    """
    Parse input arguments from command line
    """
    parser = argparse.ArgumentParser()
    parser.add_argument("-i", "--ingredients-file", help="Path to an ingredients file in json format", required=True, type=str)
    parser.add_argument("-d", "--daemon", help="Run wcst_import as daemon. Valid values: start|stop|status|restart", type=str)
    parser.add_argument("-w", "--watch", help="The daemon waits for [interval] seconds before checking the paths in the ingredient file for new data."
                                              " [interval] must be a positive number; By default the interval is 3600 (1 hour).", type=float)

    parser.add_argument("--user", help="Username with qualified privileges of the petascope endpoint"
                                       " in the ingredient file.", type=str)
    parser.add_argument("--passwd", help="Password of the username", type=str)

    parser.add_argument("--identity-file", help="specify a file from which credentials of a valid rasdaman user are read; "
                                                "the credentials must be specified as username:password", type=str)

    parser.add_argument("-c", "--gdal-cache-size",
                        help="the number of open gdal datasets to keep in cache in order to avoid reopening"
                             " the same files, which can be costly. The specified value can be one of: -1 "
                             "(no limit, cache all files), 0 (fully disable caching), N (clear the cache"
                             " whenever it has more than N datasets, N should be greater than 0). "
                             "The default value is -1 if this option is not specified.", type=int)

    result = parser.parse_args()
    return result
