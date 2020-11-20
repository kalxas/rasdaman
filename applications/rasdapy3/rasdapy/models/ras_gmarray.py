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
from rasdapy.models.minterval import MInterval
from rasdapy.models.mdd_types import rDataFormat


class RasGMArray(object):

    """
    This class represents a generic MDD in the sense that it
    is independent of the cell base type. The only information
    available is the length in bytes of the base type.
    """
    DEFAULT_MDD_TYPE = "GreyString"

    # if type_length is not specified, consider it is char (1 byte)
    DEFAULT_TYPE_LENGTH = 1

    def __init__(self,
                 spatial_domain=None,
                 type_name=None,
                 type_length=DEFAULT_TYPE_LENGTH,
                 data=None,
                 storage_layout=None):
        """
        :param MInterval spatial_domain: the domain of this array
        :param str type_name: the name of array's type
        :param int type_length: length of the cell base type in bytes (e.g: char: 1 byte, short: 2 bytes, long: 4 bytes)
        :param long tile_size: the current tile size in bytes (optional)
        :param byte[] data: the binary data in 1D array
        :param RasStorageLayout: storage layout object to store the tile domain, tile size
        """

        self.spatial_domain = spatial_domain
        self.type_name = type_name
        self.type_length = type_length
        self.format = rDataFormat.r_Array
        self.data = data
        self.storage_layout = storage_layout

    @property
    def data_length(self):
        return len(self.data)

    @property
    def byte_size(self):
        return self.spatial_domain.cell_count*self.type_length

    def decompose_mdd(self):
        return self.storage_layout.decompose_mdd(self)

    def __repr__(self):
        s = f"spatial domain: {str(self.spatial_domain)}\n"
        s += f"type_name: {self.type_name}\n"
        s += f"type length: {self.type_length}\n"
        return s







