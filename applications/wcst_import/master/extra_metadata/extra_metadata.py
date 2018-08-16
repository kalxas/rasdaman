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


class GlobalExtraMetadata:
    def __init__(self, global_extra_metadata, bands_extra_metadata, axes_extra_metadata):
        """
        Representation of the extra metadata extracted from a dataset
        :param dict global_extra_metadata: a dictionary of global extra metadata values
        :param dict bands_extra_metadata: a dictionary of bands extra metadata values
        :param dict axes_extra_metadata: a dictionary of axes extra metadata values
        """
        self.global_extra_metadata = global_extra_metadata
        self.bands_extra_metadata = bands_extra_metadata
        self.axes_extra_metadata = axes_extra_metadata


class LocalExtraMetadata:
    def __init__(self, local_extra_metadata, slice_subsets):
        """
        Representation of the extra metadata extracted from a dataset
        :param dict local_extra_metadata: a dictionary of local extra metadata values for a coverage slice
        :param list[ExtraMetadataSliceSubset] slice_subsets: the spatio-temporal position of the slice
        """
        self.local_extra_metadata = local_extra_metadata
        self.slice_subsets = slice_subsets
