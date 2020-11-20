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
from rasdapy.models.sinterval import SInterval


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
        self.dimension = len(intervals)

    @property
    def empty(self):
        return self.dimension == 0

    @property
    def cell_count(self):
        cell_count = 1
        for interval in self.intervals:
            cell_count *= interval.extent
        return cell_count

    def get_extent(self):
        extent = ()
        for interval in self.intervals:
            extent += (interval.extent,)
        return extent

    def cell_point(self, offset):
        factor = 1
        for interval in self.intervals:
            factor *= interval.extent

        pt = ()
        for interval in self.intervals:
            factor /= interval.extent
            coord = interval.lo + ((offset - (offset % factor)) / factor)
            pt += (int(coord),)
            offset %= factor

        return pt

    def cell_offset(self, tuple):
        """
        :param tuple: (i, j, k,...) coordinates in all dimensions
        :return: offset in raw array (memory ordering)
        """
        offset_ = 0
        cur_interval = self.intervals[0]
        for i in range(0, self.dimension - 1):
            next_interval = self.intervals[i+1]
            offset_ = (offset_ + (tuple[i] - cur_interval.lo))*next_interval.extent
            cur_interval = next_interval

        i = self.dimension - 1
        offset_ += (tuple[i] - self.intervals[i].lo)

        return offset_

    @property
    def shape(self):
        _shape = []
        for i in self.intervals:
            _shape.append(i.hi + 1)
        return tuple(_shape)

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


    @staticmethod
    def from_shape(shape):
        intervals = []
        for i_max in shape:
            intervals.append(SInterval(0, i_max - 1))
        return MInterval(intervals)

    def _inner_cartesian_product(self, list, t, depth):
        if depth == self.dimension:
            list.append(tuple(t.copy()))
            return

        interval = self.intervals[depth]
        for i in range(interval.lo, interval.hi + 1):
            t[depth] = i
            self._inner_cartesian_product(list, t, depth=depth + 1)

    def cartesian_product(self):
        list = []
        t = [0 for i in range(self.dimension)]
        depth = 0
        self._inner_cartesian_product(list, t, depth)

        return list


