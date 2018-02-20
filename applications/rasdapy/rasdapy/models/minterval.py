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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
from sinterval import SInterval


class MInterval(object):
    """
    A class represents the multiple interval in rasdaman (e.g: select sdom(c) from test_mr as c)
    which returns [0:255, 0:210].
    """

    def __init__(self, intervals):
        """
        :param intervals: the List of SIntervals
        """
        self.intervals = intervals

    def __str__(self):
        """
        String representing the multiple interval (e.g: [SInterval1, SInterval2] -> [0:250, 0:210]
        """
        output = "[" + ",".join(str(x) for x in self.intervals) + "]"
        return output

    @staticmethod
    def from_str(str_mdd):
        """
        Parse the string representing MDD domain (e.g: "[0:18,0:18]") to a MInterval object with list of SIntervals
        :param str str_mdd: represents MDD domain
        :return: MInterval result: a MInterval object
        """
        tmp = str_mdd.strip("[]")
        sinterval_str_arr = tmp.split(",")

        result_arr = []
        for tmp in sinterval_str_arr:
            tmp_arr = tmp.split(":")
            sinterval = SInterval(tmp_arr[0], tmp_arr[1])
            result_arr.append(sinterval)

        minterval = MInterval(result_arr)
        return minterval
