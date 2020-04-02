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
import math
from unicodedata import decimal
from master.helper.high_pixel_adjuster import HighPixelAjuster

from lib import arrow
from master.helper.regular_user_axis import RegularUserAxis
from master.helper.user_axis import UserAxisType
from util.log import log

import decimal
from decimal import ROUND_UP
from util.time_util import DateTimeUtil


class PointPixelAdjuster:
    def __init__(self):
        """
        Class that handles the adjustments of coordinates from pixels being points in the middle of the interval to
        pixels stretching the interval.
        """
        pass

    @staticmethod
    def adjust_axis_bounds_to_continuous_space(user_axis, crs_axis):
        """
        Moves pixels inside the user_axis from the middle of the interval to the bounds of the interval.
        :param UserAxis user_axis: the axis given by the user
        :param crs_axis is the axis from SECORE (e.g: 4326, Ansidate, UnixTime)
        :return: None
        """
        if user_axis.type == UserAxisType.DATE:
            PointPixelAdjuster.adjust_axis_bounds_for_time_axis(user_axis, crs_axis)
        else:
            # if low < high, adjust it
            if user_axis.interval.high is not None and user_axis.interval.low > user_axis.interval.high:
                user_axis.interval.low, user_axis.interval.high = user_axis.interval.high, user_axis.interval.low

            if isinstance(user_axis, RegularUserAxis):
                user_axis.interval.low = decimal.Decimal(str(user_axis.interval.low)) \
                                         - decimal.Decimal(str(0.5)) * abs(decimal.Decimal(str(user_axis.resolution)))
                if user_axis.interval.high:
                    user_axis.interval.high = decimal.Decimal(str(user_axis.interval.high)) \
                                              + decimal.Decimal(str(0.5)) * abs(decimal.Decimal(str(user_axis.resolution)))

    @staticmethod
    def adjust_axis_bounds_for_time_axis(user_axis, crs_axis):
        # convert to timestamp and change the axis type
        user_axis.interval.low = arrow.get(user_axis.interval.low).float_timestamp
        if user_axis.interval.high:
            user_axis.interval.high = arrow.get(user_axis.interval.high).float_timestamp

        # if low < high, adjust it
        if user_axis.interval.high is not None and user_axis.interval.low > user_axis.interval.high:
            user_axis.interval.low, user_axis.interval.high = user_axis.interval.high, user_axis.interval.low

        # The formula for all regular axes is when "pixelIsPoint":
        # min = min - 0.5 * resolution
        # max = max + 0.5 * resolution
        if isinstance(user_axis, RegularUserAxis):
            # if axis is time axis
            if crs_axis.is_time_day_axis():
                user_axis.interval.low = decimal.Decimal(str(user_axis.interval.low)) \
                                       - decimal.Decimal(0.5) * decimal.Decimal(str(abs(user_axis.resolution))) * DateTimeUtil.DAY_IN_SECONDS
                if user_axis.interval.high:
                    user_axis.interval.high = decimal.Decimal(str(user_axis.interval.high)) \
                                            + decimal.Decimal(0.5) * decimal.Decimal(str(abs(user_axis.resolution))) * DateTimeUtil.DAY_IN_SECONDS
            else:
                # if axis is normal axis (lat, lon, index1d,...)
                user_axis.interval.low = decimal.Decimal(str(user_axis.interval.low)) \
                                       - decimal.Decimal(0.5) * decimal.Decimal(str(abs(user_axis.resolution)))
                if user_axis.interval.high:
                    user_axis.interval.high = decimal.Decimal(str(user_axis.interval.high)) \
                                            + decimal.Decimal(0.5) * decimal.Decimal(str(abs(user_axis.resolution)))

    @staticmethod
    def get_origin(user_axis, crs_axis):
        """
        Computes the origin for the user_axis by shifting the limit with half resolution.
        :param UserAxis user_axis: the axis as given by the user.
        :return: float origin
        """
        # if axis is datetime then just return the low
        if user_axis.type == UserAxisType.DATE:
            return PointPixelAdjuster.get_origin_for_time_axis(user_axis, crs_axis)

        if isinstance(user_axis, RegularUserAxis):
            if user_axis.resolution > 0 or user_axis.interval.high is None:
                # axis goes from low to high, so origin is lowest, with half a pixel shift
                # min (min + 0.5*resolution) --> max
                return decimal.Decimal(str(user_axis.interval.low))\
                       + decimal.Decimal(0.5) * decimal.Decimal(str(user_axis.resolution))
            else:
                # axis goes from high to low, so origin is highest, with half pixel shift (resolution is negative)
                # min <---- (max + 0.5*resolution) max
                return decimal.Decimal(str(user_axis.interval.high))\
                       + decimal.Decimal(0.5) * decimal.Decimal(str(user_axis.resolution))
        else:
            # irregular axis, the same but without shift
            # normally, irregular axis points from min ---> max, so origin is min (e.g: time ansidate, unixtime)
            if user_axis.resolution > 0 or user_axis.interval.high is None:
                return user_axis.interval.low
            else:
                return user_axis.interval.high

    @staticmethod
    def get_origin_for_time_axis(user_axis, crs_axis):
        user_axis.interval.low = arrow.get(user_axis.interval.low).float_timestamp
        if user_axis.interval.high:
            user_axis.interval.high = arrow.get(user_axis.interval.high).float_timestamp

        if isinstance(user_axis, RegularUserAxis):
            # ansidate, need to calculate with day in seconds
            if crs_axis.is_time_day_axis:
                if user_axis.resolution > 0 or user_axis.interval.high is None:
                    # axis goes from low to high, so origin is lowest, with half a pixel shift
                    return decimal.Decimal(str(user_axis.interval.low))\
                         + decimal.Decimal(0.5) * decimal.Decimal(str(user_axis.resolution)) * DateTimeUtil.DAY_IN_SECONDS
                else:
                    # axis goes from high to low, so origin is highest, with half pixel shift (resolution is negative)
                    return decimal.Decimal(str(user_axis.interval.high))\
                        + decimal.Decimal(0.5) * decimal.Decimal(str(user_axis.resolution)) * DateTimeUtil.DAY_IN_SECONDS
            else:
                # unix time, already in seconds
                if user_axis.resolution > 0 or user_axis.interval.high is None:
                    # axis goes from low to high, so origin is lowest, with half a pixel shift
                    return decimal.Decimal(str(user_axis.interval.low))\
                         + decimal.Decimal(0.5) * decimal.Decimal(str(user_axis.resolution))
                else:
                    # axis goes from high to low, so origin is highest, with half pixel shift (resolution is negative)
                    return decimal.Decimal(str(user_axis.interval.high))\
                         + decimal.Decimal(0.5) * decimal.Decimal(str(user_axis.resolution))
        else:
            # irregular axis, the same but without shift
            if user_axis.resolution > 0 or user_axis.interval.high is None:
                return user_axis.interval.low
            else:
                return user_axis.interval.high

    @staticmethod
    def get_grid_points(user_axis, crs_axis):
        """
        Computes the number of grid points corresponding to the axis.
        :param UserAxis user_axis: the axis as given by the user
        :param CrsAxis crs_axis: the corresponding crs axis
        :return: int gridPoints
        """
        # if no interval on the axis (slice), then 1 single grid point
        if user_axis.interval.high is None:
            return 1
        if isinstance(user_axis, RegularUserAxis):
            # number of geo-intervals over resolution
            if user_axis.type != UserAxisType.DATE:
                # number_of_grid_points = (geo_max - geo_min) / resolution
                grid_points = abs((decimal.Decimal(str(user_axis.interval.high)) - decimal.Decimal(str(user_axis.interval.low)))
                             / decimal.Decimal(str(user_axis.resolution)))
                # The resolution in ingredient file can have big factor to the calculation, so must take care
                if abs(decimal.Decimal(str(grid_points), decimal.Context(rounding = ROUND_UP)) - grid_points) > HighPixelAjuster.THRESHOLD:
                    log.warning("The computed number of grid points is not an integer for axis " + user_axis.name +
                                ". This usually indicates that the resolution is not correct.")

                grid_points = HighPixelAjuster.adjust_high(grid_points)
                # Negative axis (e.g: latitude) min <--- max
                if user_axis.resolution < 0:
                    return int(math.floor(grid_points))
                else:
                    # Positive axis (e.g: longitude) min ---> max
                    return int(math.ceil(grid_points))
            else:
                time_difference = user_axis.interval.high - user_axis.interval.low
                # AS time always point to future (min --> max)
                if crs_axis.is_time_day_axis():
                    # days ((seconds / 86400) / resolution)
                    grid_points = abs((decimal.Decimal(str(time_difference)) / decimal.Decimal(DateTimeUtil.DAY_IN_SECONDS))
                                     / decimal.Decimal(str(user_axis.resolution)))
                    grid_points = HighPixelAjuster.adjust_high(grid_points)

                    return int(math.ceil(grid_points))
                else:
                    # seconds (seconds / resolution)
                    grid_points = abs((decimal.Decimal(str(time_difference))
                                         / decimal.Decimal(str(user_axis.resolution))))
                    grid_points = HighPixelAjuster.adjust_high(grid_points)

                    return int(math.ceil(grid_points))
        else:
            # number of direct positions (i.e: irregular axis with coefficients [0, 1, 3, 5, 8, 15])
            return len(user_axis.directPositions)
