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
import datetime
import functools
import decimal
from __builtin__ import staticmethod

from lib.arrow import api as arrow
from lib.arrow.parser import ParserError
from master.error.runtime_exception import RuntimeException
from util.string_util import stringify


@functools.total_ordering
class DateTimeUtil:
    MAX_DATE = arrow.get("9999-01-01 00:00:00")
    MIN_DATE = arrow.get("0001-01-01 00:00:00")
    CRS_CODE_ANSI_DATE = "AnsiDate"
    CRS_CODE_UNIX_TIME = "UnixTime"

    """ Standard codes
    for supported temporal Unit of Measures (which provide ISO8601 interface to the user)
    http://unitsofmeasure.org/ucum.html """

    UCUM_MILLIS = "ms"
    UCUM_SECOND = "s"
    UCUM_MINUTE = "min"
    UCUM_HOUR = "h"
    UCUM_DAY = "d"
    UCUM_WEEK = "wk"
    UCUM_MEAN_JULIAN_MONTH = "mo_j"
    UCUM_MEAN_GREGORIAN_MONTH = "mo_g"
    UCUM_SYNODAL_MONTH = "mo_s"
    UCUM_MONTH = "mo"
    UCUM_MEAN_JULIAN_YEAR = "a_j"
    UCUM_MEAN_GREGORIAN_YEAR = "a_g"
    UCUM_TROPICAL_YEAR = "a_t"
    UCUM_YEAR = "a"

    """ Milliseconds associated to each supported temporal UoM """
    MILLIS_MILLIS = 1
    MILLIS_SECOND = 1000
    MILLIS_MINUTE = MILLIS_SECOND * 60
    MILLIS_HOUR = MILLIS_MINUTE * 60
    MILLIS_DAY = MILLIS_HOUR * 24
    MILLIS_WEEK = MILLIS_DAY * 7

    MILLIS_MEAN_JULIAN_YEAR = (long)(MILLIS_DAY * 365.25)
    MILLIS_MEAN_GREGORIAN_YEAR = (long)(MILLIS_DAY * 365.2425)
    MILLIS_TROPICAL_YEAR = (long)(MILLIS_DAY * 365.24219)
    MILLIS_YEAR = MILLIS_MEAN_JULIAN_YEAR
    MILLIS_MEAN_JULIAN_MONTH = MILLIS_MEAN_JULIAN_YEAR / 12
    MILLIS_MEAN_GREGORIAN_MONTH = MILLIS_MEAN_GREGORIAN_YEAR / 12
    MILLIS_SYNODAL_MONTH = (long)(MILLIS_DAY * 29.53059)
    MILLIS_MONTH = MILLIS_MEAN_JULIAN_MONTH

    """ Time Axis Origin """
    ANSI_DATE_ORIGIN = "1600-12-31T00:00:00Z"
    UNIX_TIME_ORIGIN = "1970-01-01T00:00:00Z"

    """" AnsiDate To UnixTime """
    DAY_IN_SECONDS = 24 * 3600

    def __init__(self, datetime, dt_format=None, time_crs=None):
        """
        :param str datetime: the datetime value
        :param str dt_format: the datetime format, if none is given we'll try to guess
        """
        self.init_cache()
        from util.crs_util import CRSUtil

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
        Returns the datetime as a string
        :return:
        """
        if self.CRS_CODE_ANSI_DATE in self.time_crs_code:
            return self.to_iso_format()
        elif self.CRS_CODE_UNIX_TIME in self.time_crs_code:
            return self.to_iso_format()
        else:
            return self.to_unknown()

    def __str__(self):
        return self.to_string()

    def to_iso_format(self):
        """
        Returns the datetime in iso format
        :return: str
        """
        return '"' + self.datetime.isoformat() + '"'

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

    def init_cache(self):
        """
        Initialize the map from uoms to values in seconds and time crss origins
        :return:
        """
        self.__UOM_TIME_MAP_CACHE__.update({self.UCUM_MILLIS: self.MILLIS_MILLIS,
                                            self.UCUM_SECOND: self.MILLIS_SECOND,
                                            self.UCUM_MINUTE: self.MILLIS_MINUTE,
                                            self.UCUM_HOUR: self.MILLIS_HOUR,
                                            self.UCUM_DAY: self.MILLIS_DAY,
                                            self.UCUM_WEEK: self.MILLIS_WEEK,
                                            self.UCUM_MEAN_JULIAN_MONTH: self.MILLIS_MEAN_JULIAN_MONTH,
                                            self.UCUM_MEAN_GREGORIAN_MONTH: self.MILLIS_MEAN_GREGORIAN_MONTH,
                                            self.UCUM_SYNODAL_MONTH: self.MILLIS_SYNODAL_MONTH,
                                            self.UCUM_MONTH: self.MILLIS_MONTH,
                                            self.UCUM_MEAN_JULIAN_YEAR: self.MILLIS_MEAN_JULIAN_YEAR,
                                            self.UCUM_MEAN_GREGORIAN_YEAR: self.MILLIS_MEAN_GREGORIAN_YEAR,
                                            self.UCUM_TROPICAL_YEAR: self.MILLIS_TROPICAL_YEAR,
                                            self.UCUM_YEAR: self.MILLIS_YEAR
                                            })

        self.__TIME_CRS_MAP_CACHE__.update({self.CRS_CODE_ANSI_DATE: self.ANSI_DATE_ORIGIN,
                                            self.CRS_CODE_UNIX_TIME: self.UNIX_TIME_ORIGIN})

    def get_milli_by_uom(self, time_uom):
        """
        Based on time_uom to return the correct milliseconds (e.g: 1d = 24 * 60 * 60 * 1000 milliseconds)
        :param time_uom: uom of time crs (e.g: ansidate: d, unixtime: s)
        :return: long
        """
        # return the uom value in milliseconds
        milli_seconds = self.__UOM_TIME_MAP_CACHE__.get(time_uom)
        if milli_seconds is None:
            raise RuntimeException("Time uom: " + time_uom + " does not support yet, cannot calculate time value in milliseconds from time uom.")
        return milli_seconds

    def get_time_crs_origin(self, crs_uri):
        """
        Based on crs_uri (e.g: http://localhost:8080/def/crs/OGC/0/AnsiDate) to get the datetime origin
        :param crs_uri: string
        :return:
        """
        date_origin = None
        for key, value in self.__TIME_CRS_MAP_CACHE__.iteritems():
            # e.g: AnsiDate in crs_uri
            if key in crs_uri:
                date_origin = value
                break

        if date_origin is None:
            raise RuntimeException("Time crs: " + crs_uri + " does not support yet, cannot get the date origin from the time crs.")
        return date_origin


    def count_offset_dates(self, origin_date, date, time_uom):
        """
        Count the offset between two dates (ISO 8601 format) and return the time_delta in milliseconds
        :param origin_date: string
        :param date: string
        :param time_uom: string
        :return: float
        """
        uom_milli_seconds = self.get_milli_by_uom(time_uom)
        origin_date_milli_seconds = arrow.get(origin_date).timestamp * 1000
        date_milli_seconds = arrow.get(date).timestamp * 1000
        time_delta_in_uom = (float(date_milli_seconds) - float(origin_date_milli_seconds)) / float(uom_milli_seconds)
        return time_delta_in_uom

    @staticmethod
    def get_datetime_iso(datetime):
        """
        Convert the datetime in decimal to ISO datetime format by arrow
        :param datetime: decimal
        :return: str
        """
        return stringify(arrow.get(datetime))

    @staticmethod
    def get_now_datetime_stamp():
        """
        Return the current date time for now with pattern as string.
        """
        now = datetime.datetime.now()
        result = now.strftime("%Y-%m-%d %H:%M:%S")

        return result


    __UOM_TIME_MAP_CACHE__ = {}
    __TIME_CRS_MAP_CACHE__ = {}
