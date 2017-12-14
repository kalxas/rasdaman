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

from master.provider.metadata.axis import Axis
from util.crs_util import CRSAxis


class IrregularAxis(Axis):
    def __init__(self, label, uomLabel, low, high, origin, coefficients, crs_axis):
        """
        An irregular axis is defined by its crs axis and a list of coefficients
        :param str label: the label of the axis
        :param str uomLabel: the unit of measure
        :param str | float | int low: the low value on the axis bounding interval
        :param str | float | int high: the high value on the axis bounding interval
        :param str | float | int origin: the origin on the axis
        :param list[str | float | int] coefficients: a list of coefficients describing the points
        :param CRSAxis crs_axis: the corresponding virtual crs axis of which this axis is an instance of
        :return:
        """
        Axis.__init__(self, label, uomLabel, low, high, origin, crs_axis)
        self.coefficient = coefficients