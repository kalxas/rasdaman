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


class RasStorageLayOut(object):
    """
    This class is used to express the storage options for MDD objects.
    In the current version, either the tile-size (for example 256000 bytes) or the
    tiling-domain (for example "[0:127,0:511]") can be specified.
    """
    DEFAULT_TILE_SIZE = 128000

    def __init__(self, spatial_domain=None, tile_size=None):
        """
        :param long tile_size: The current tile size in bytes (optional)
        :param MInterval spatial_domain: The spatial domain of the current storageLayout (optional)
        """
        self.spatial_domain = spatial_domain
        self.tile_size = tile_size