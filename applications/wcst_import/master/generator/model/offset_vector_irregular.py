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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""

from master.generator.model.offset_vector import OffsetVector


class OffsetVectorIrregular(OffsetVector):
    def __init__(self, crs, axisLabels, uomLabels, noOfDimensions, offset, coefficient, axisSpanned):
        """
        Representation of the offset vector on a regular axis
        :param str crs: the crs of the offset vector
        :param list[str] axisLabels: the labels of the axes
        :param list[str] uomLabels: the labels of the uoms
        :param int noOfDimensions: the number of dimensions
        :param list[float] offset: the offset value
        :param str coefficient: the coefficient(s) for the axis
        :param str axisSpanned: the label of the axis to which this vector corresponds to
        """
        super(OffsetVectorIrregular, self).__init__(crs, axisLabels, uomLabels, noOfDimensions, offset, axisSpanned)
        self.coefficient = coefficient

    def get_template_name(self):
        return "gml_offset_vector_irregular.xml"