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

from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException
from util.time_util import DateTimeUtil


class SliceRestricter:
    def __init__(self, slices):
        """
        The slice restricter analyzes the slices in a coverage an removes the ones that do not feed the given restriction
        :param list[Slice] slices: the slices to be analyzed
        """
        self.slices = slices
        self._eliminate_unwanted_slices()

    def get_slices(self):
        """
        Returns the new slice list
        :rtype: list[Slice]
        """
        return self.slices

    def _eliminate_unwanted_slices(self):
        """
        Eliminates the restricted slices
        """
        if ConfigManager.slice_restriction is not None:
            new_slices = []
            for slice in self.slices:
                if not self._slice_is_restricted(slice):
                    new_slices.append(slice)
            self.slices = new_slices

    def _slice_is_restricted(self, slice):
        """
        Checks if the slice is restricted
        :param Slice slice: the slices to check for the restriction
        :return:
        """
        if ConfigManager.slice_restriction is None:
            return False
        for i in range(0, len(slice.axis_subsets)):
            axis_subset = slice.axis_subsets[i]
            restrict_low, restrict_high = self._get_restriction(i)
            if axis_subset.interval.low < restrict_low or axis_subset.interval.low > restrict_high:
                return True
            if axis_subset.interval.high is not None and axis_subset.interval.high > restrict_high:
                return True
        return False

    @staticmethod
    def _get_restriction(index):
        """
        Returns the restriction parameter from the recipe for the current subset on the axis
        :param int index: the axis index
        :rtype: (float | DateTimeUtil, float | DateTimeUtil)
        """
        if len(ConfigManager.slice_restriction) <= index:
            raise RuntimeException(
                "You have provided less slice restriction intervals then there are axes. Please provide one restriction interval per axis")

        restriction = ConfigManager.slice_restriction[index]
        if "low" not in restriction or "high" not in restriction:
            raise RuntimeException("You have to provide a low and a high for each restriction interval")
        low, high = None, None
        if "type" in restriction and restriction["type"] == "date":
            low = DateTimeUtil(restriction['low'])
            high = DateTimeUtil(restriction['high'])
        else:
            if type(restriction['low']) not in [int, float, long] and type(restriction['high']) not in [int, float,
                                                                                                        long]:
                raise RuntimeException("You provided a non-numeric restriction interval" + str(
                    restriction) + " without providing a type for it. Please check the correct syntax for non-numeric intervals.")
            low = restriction['low']
            high = restriction['high']
        return low, high