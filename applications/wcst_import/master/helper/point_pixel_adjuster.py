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
from lib import arrow
from master.helper.regular_user_axis import RegularUserAxis
from master.helper.user_axis import UserAxisType
from util.log import log


class PointPixelAdjuster:
    def __init__(self):
        """
        Class that handles the adjustments of coordinates from pixels being points in the middle of the interval to
        pixels stretching the interval.
        """
        pass

    @staticmethod
    def adjust_axis_bounds_to_continuous_space(user_axis):
        """
        Moves pixels inside the user_axis from the middle of the interval to the bounds of the interval.
        :param UserAxis user_axis: the axis given by the user
        :return: None
        """
        # convert to timestamps if date
        if user_axis.type == UserAxisType.DATE:
            user_axis.interval.low = arrow.get(user_axis.interval.low).float_timestamp
            if user_axis.interval.high:
                user_axis.interval.high = arrow.get(user_axis.interval.high).float_timestamp

        # if low < high, adjust it
        if user_axis.interval.high is not None and user_axis.interval.low > user_axis.interval.high:
            user_axis.interval.low, user_axis.interval.high = user_axis.interval.high, user_axis.interval.low

        if isinstance(user_axis, RegularUserAxis):
            user_axis.interval.low -= 0.5 * abs(user_axis.resolution)
            if user_axis.interval.high:
                user_axis.interval.high += 0.5 * abs(user_axis.resolution)

    @staticmethod
    def get_origin(user_axis):
        """
        Computes the origin for the user_axis by shifting the limit with half resolution.
        :param UserAxis user_axis: the axis as given by the user.
        :return: float origin
        """
        if isinstance(user_axis, RegularUserAxis):
            if user_axis.resolution > 0 or user_axis.interval.high is None:
                # axis goes from low to high, so origin is lowest, with half a pixel shift
                return user_axis.interval.low + 0.5 * user_axis.resolution
            else:
                # axis goes from high to low, so origin is highest, with half pixel shift (resolution is negative)
                return user_axis.interval.high + 0.5 * user_axis.resolution
        else:
            # irregular axis, the same but without no shift
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
        if not user_axis.interval.high:
            return 1
        if isinstance(user_axis, RegularUserAxis):
            # number of geo-intervals over resolution
            if user_axis.type != UserAxisType.DATE:
                grid_points = abs((user_axis.interval.high - user_axis.interval.low) / user_axis.resolution)
                if abs(round(grid_points) - grid_points) > 0.01:
                    log.warning("The computed number of grid points is not an integer for axis " + user_axis.name +
                                ". This usually indicates that the resolution is not correct.")
                return int(round(grid_points))
            else:
                time_difference = user_axis.interval.high - user_axis.interval.low
                if crs_axis.is_uom_day():
                    # days
                    return int(round(abs((time_difference / (24 * 3600)) / user_axis.resolution)))
                else:
                    # seconds
                    return int(round(abs(time_difference / user_axis.resolution)))
        else:
            # number of direct positions
            return len(user_axis.directPositions)

