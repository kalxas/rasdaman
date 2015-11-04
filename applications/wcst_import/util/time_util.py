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
import functools
from lib.arrow import api as arrow
from lib.arrow.parser import ParserError
from util.crs_util import CRSUtil
from master.error.runtime_exception import RuntimeException


@functools.total_ordering
class DateTimeUtil:
    CRS_CODE_ANSI_DATE = "AnsiDate"
    CRS_CODE_UNIX_TIME = "UnixTime"

    def __init__(self, datetime, dt_format=None, time_crs=None):
        """
        :param str datetime: the datetime value
        :param str dt_format: the datetime format, if none is given we'll try to guess
        """
        try:
            if dt_format is None:
                self.datetime = arrow.get(datetime)
            else:
                self.datetime = arrow.get(datetime, dt_format)
            if time_crs is None:
                self.time_crs_code = self.CRS_CODE_ANSI_DATE
            else:
                tmp_crs = CRSUtil(time_crs)
                self.time_crs_code = tmp_crs.get_crs_code()
        except ParserError as pe:
            dt_format_err = "auto" if dt_format is None else dt_format
            raise RuntimeException("Failed to parse the date " + datetime + " using format " + dt_format_err)

    def to_string(self):
        """
        Returns the datetime as a string, formatted depending on the time CRS code
        :return:
        """
        if self.time_crs_code == self.CRS_CODE_ANSI_DATE:
            return self.to_ansi()
        elif self.time_crs_code == self.CRS_CODE_UNIX_TIME:
            return self.to_unix()
        else:
            return self.to_unknown()

    def __str__(self):
        return self.to_string()

    def to_ansi(self):
        """
        Returns the datetime in iso format
        :return: str
        """
        return '"' + self.datetime.isoformat() + '"'

    def to_unix(self):
        """
        Returns the datetime in unix format
        :return: str
        """
        return str(self.datetime.timestamp)

    def to_unknown(self):
        """
        Handle unknown time CRS
        :return: throw RuntimeException
        """
        raise RuntimeException("Unsupported time CRS " + self.time_crs_code)

    def __eq__(self, other):
        """
        Compares two tuples ==
        :param DateTimeUtil other: the tuple to compare with
        :rtype bool
        """
        return self.datetime == other.datetime

    def __lt__(self, other):
        """
        Compares two tuples <
        :param DateTimeUtil other: the tuple to compare with
        :rtype bool
        """
        return self.datetime < other.datetime