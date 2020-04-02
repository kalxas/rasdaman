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

from master.provider.metadata.coverage_axis import CoverageAxis
from master.importer.interval import Interval


class AxisSubset:
    def __init__(self, coverage_axis, interval):
        """
        A representation of a subset on one axis
        :param CoverageAxis coverage_axis: the definition of the axis on which the subset is being done
        :param Interval interval: the interval of the subset
        """
        self.coverage_axis = coverage_axis
        self.interval = interval

    def __str__(self):
        return self.coverage_axis.axis.label + "(" + str(self.interval) + ")"