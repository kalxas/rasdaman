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

from master.generator.model.range_set import RangeSet


class RangeSetFile(RangeSet):
    def __init__(self, fileReference, mimetype):
        """
        Class to represent a range set that has a file as a data container
        :param fileReference: the full filepath
        :param mimetype: the mimetype of the file
        """
        self.fileReference = fileReference
        self.mimetype = mimetype

    def get_template_name(self):
        return "gml_range_set_file_ref.xml"
