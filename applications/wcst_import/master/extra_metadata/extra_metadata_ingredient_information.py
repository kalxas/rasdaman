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


class ExtraMetadataIngredientInformation:
    def __init__(self, global_attributes, slice_attributes):
        """
        Contains an extra-metadata description of the fields that need to be collected from the dataset via wcst expressions
        :param dict global_attributes: the attributes that need to be collected only once per coverage and do not change per slice
        :param dict slice_attributes: the attributes that should be collected per slice
        """
        self.global_attributes = global_attributes
        self.slice_attributes = slice_attributes
