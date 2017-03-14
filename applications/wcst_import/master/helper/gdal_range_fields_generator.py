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

from master.generator.model.range_type_field import RangeTypeField
from master.generator.model.range_type_nill_value import RangeTypeNilValue
from util.gdal_util import GDALGmlUtil


class GdalRangeFieldsGenerator:
    def __init__(self, gdal_dataset, field_names=None):
        """
        Class to generate the range fields from a gdal dataset
        :param GDALGmlUtil gdal_dataset: the gdal dataset
        :param list[str] field_names: the default names of the fields if the user provided them
        """
        self.gdal_dataset = gdal_dataset
        self.field_names = field_names

    def get_range_fields(self):
        """
        Returns the range fields for this gdal dataset
        :rtype: list[RangeTypeField]
        """
        fields = []
        field_id = 0
        for range_field in self.gdal_dataset.get_fields_range_type():
            nill_values = []
            for nill_value in range_field.nill_values:
                nill_values.append(RangeTypeNilValue("", nill_value))
            field_name = range_field.field_name
            if self.field_names is not None and len(self.field_names) > field_id:
                field_name = self.field_names[field_id]
            field_id += 1
            # Old recipes don't support nilReason option
            fields.append(RangeTypeField(field_name, "", "", "", nill_values, range_field.uom_code))
        return fields