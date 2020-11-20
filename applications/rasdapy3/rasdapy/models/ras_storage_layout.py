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

from rasdapy.models.mdd_types import rDataFormat
from rasdapy.models.minterval import MInterval
from rasdapy.models.sinterval import SInterval
from rasdapy.models.ras_gmarray import RasGMArray


class RasStorageLayOut(object):
    """
    This class is used to express the storage options for MDD objects.
    In the current version, either the tile-size (for example 256000 bytes) or the
    tiling-domain (for example "[0:127,0:511]") can be specified.
    """

    # this size is related to max size that can be sent via gRPC
    DEFAULT_TILE_SIZE = 524288

    def __init__(self, tile_size=None, spatial_domain=None):
        """
        :param long tile_size: The current tile size in bytes (optional)
        :param MInterval spatial_domain: The spatial domain of the current storageLayout (optional)
        """
        self.format = rDataFormat.r_Array
        self.spatial_domain = spatial_domain
        self.tile_size = tile_size

    def decompose_mdd(self, gm_array):
        # by default no decomposition, return the mdd itself
        return [gm_array]


class BandStorageLayout(RasStorageLayOut):

    def __init__(self):
        RasStorageLayOut.__init__(self)

    def decompose_mdd(self, gm_array):

        # Size is small enough, no need to split domain, just return the gm_array itself
        if gm_array.data_length <= RasStorageLayOut.DEFAULT_TILE_SIZE:
            return [gm_array]

        return BandMDDIterator(gm_array)

    def compute_spatial_domain(self, gm_array):

        # Size is small enough, no need to split domain, use spatial domain.
        if gm_array.data_length <= RasStorageLayOut.DEFAULT_TILE_SIZE:
            return str(gm_array.spatial_domain)

        mintervals = gm_array.spatial_domain.intervals

        bytes_incr = self.get_hyper_plane_bytesize(gm_array)

        if bytes_incr > RasStorageLayOut.DEFAULT_TILE_SIZE:
            raise Exception(f"tile size {bytes_incr} is bigger than maximum {RasStorageLayOut.DEFAULT_TILE_SIZE} size")

        i_dim = int(RasStorageLayOut.DEFAULT_TILE_SIZE / bytes_incr)

        self.tile_size = i_dim*bytes_incr
        intervals = [SInterval(0, i_dim - 1)]
        for i in range(1, len(mintervals)):
            hi_value = mintervals[i].hi
            intervals.append(SInterval(0, hi_value))

        self.spatial_domain = MInterval(intervals)
        print(self.spatial_domain)


    @staticmethod
    def get_hyper_plane_bytesize(gm_array):
        mintervals = gm_array.spatial_domain.intervals

        bytes_incr = gm_array.type_length
        for i in range(1, len(mintervals)):
            bytes_incr *= mintervals[i].extent
        return bytes_incr


class BandMDDIterator:
    def __init__(self, gm_array):
        self.gm_array = gm_array
        self.storage_intervals = gm_array.storage_layout.spatial_domain.intervals
        self.bytes_incr = BandStorageLayout.get_hyper_plane_bytesize(gm_array)

        self.barray = bytearray(gm_array.data)
        self.bytes_size = self.storage_intervals[0].extent * self.bytes_incr

        self.offset = 0
        self.lo = self.storage_intervals[0].lo
        self.hi = self.storage_intervals[0].hi

        print(f"full size {gm_array.data_length}")

    def __iter__(self):
        while self.offset + self.bytes_size <= self.gm_array.data_length:
            ras_array = self._create_ras_array(self.bytes_size)
            yield ras_array
            self.offset += self.bytes_size
            self.lo += self.storage_intervals[0].extent
            self.hi += self.storage_intervals[0].extent

        remaining_size = self.gm_array.data_length - self.offset
        if remaining_size > 0:
            i_remaining = remaining_size / self.bytes_incr
            self.hi = self.lo + i_remaining - 1
            ras_array = self._create_ras_array(remaining_size)
            yield ras_array

    def _create_ras_array(self, size):
        i_interval = SInterval(self.lo, self.hi)
        print(f"get buffer from {self.offset} to {self.offset + size - 1} with {i_interval}")
        ras_array = RasGMArray()
        ras_array.spatial_domain = MInterval([i_interval, self.storage_intervals[1], self.storage_intervals[2]])
        ras_array.storage_layout = self.gm_array.storage_layout
        ras_array.type_name = self.gm_array.type_name
        ras_array.type_length = self.gm_array.type_length
        ras_array.data = bytes(self.barray[self.offset:self.offset + size - 1])
        return ras_array





