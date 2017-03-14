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

from master.generator.model.model import Model
from master.generator.model.range_type_nill_value import RangeTypeNilValue


class RangeTypeField(Model):
    def __init__(self, name, definition="", description="", nilReason="", nilValues=None, uom=None):
        """
        Class to represent the range type field element of range type
        :param str name: the name of the field
        :param str definition: the definition of the field
        :param str description: the description of the field
        :param str nilReason: the reason of nil value
        :param list[RangeTypeNilValue] nilValues: the nil values for this field
        :param str uom: the unit of measure for the field
        """
        self.name = name
        self.definition = definition
        self.description = description
        self.nilReason = nilReason
        self.nilValues = nilValues
        self.uom = uom

    def get_template_name(self):
        return "gml_range_type_field.xml"