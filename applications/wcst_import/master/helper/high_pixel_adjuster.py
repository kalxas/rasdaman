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
import decimal
import math


class HighPixelAjuster():
    THRESHOLD = decimal.Decimal(str(0.001))

    def __init__(self):
        """
        Class that handles the adjustments of high grid pixel if the value is
        """
        pass

    @staticmethod
    def adjust_high(high_pixel):
        """
        This method will adjust the high_pixel to nearest grid integer (the threshold is 0.0001)
        e.g: 4.99999999 -> 5 and 5.0000001 -> 5
        :param decimal high_pixel: the total number of grid for an axis (geo_max - geo_min) / resolution
        :return decimal:
        """
        if high_pixel + HighPixelAjuster.THRESHOLD >= math.ceil(high_pixel):
            return math.ceil(high_pixel)
        elif high_pixel - HighPixelAjuster.THRESHOLD <= math.floor(high_pixel):
            return math.floor(high_pixel)
        else:
            return high_pixel