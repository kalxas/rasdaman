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

from util.crs_util import CRSAxis


class Axis:
    def __init__(self, label, uomLabel, low, high, origin, crs_axis):
        """
        Class to represent one gml axis
        :param str label: the label of the axis
        :param str uomLabel: the uom of the axis
        :param str | int | float low: the bbox low
        :param str | int | float high: the bbox high
        :param str | int | float origin: the origin of the axis
        :param CRSAxis crs_axis: the crs axis definition for the axis corresponding to this subset. As some axis might
        be created otherwise than through a crs_axis this parameter is optional and should be checked before being
        used in other classes
        """
        self.label = label
        self.uomLabel = uomLabel
        self.low = low
        self.high = high
        self.origin = origin
        # TODO: Handle the coefficient on irregular axis in some other way
        self.coefficient = None
        self.crs_axis = crs_axis
