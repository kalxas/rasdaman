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

import functools
from util.file_util import File


@functools.total_ordering
class TimeFileTuple:
    def __init__(self, time, file):
        """
        This class is a representation of a tuple containing a date time value and a file
        :param util.time_util.DateTimeUtil time: the time of the record
        :param File file: the file associated with this tuple
        """
        self.time = time
        self.file = file

    def __eq__(self, other):
        """
        Compares two tuples ==
        :param TimeFileTuple other: the tuple to compare with
        :rtype bool
        """
        return self.time.datetime == other.time.datetime

    def __lt__(self, other):
        """
        Compares two tuples <
        :param TimeFileTuple other: the tuple to compare with
        :rtype bool
        """
        return self.time.datetime < other.time.datetime
