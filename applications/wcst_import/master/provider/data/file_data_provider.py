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

import mimetypes

from master.provider.data.data_provider import DataProvider
from util.file_obj import File


class FileDataProvider(DataProvider):
    def __init__(self, file, structure=None, mimetype=None):
        """
        Class representing a data provider backed by a local accessible file
        :param File file: the file to back the provider
        :param object structure: the structure of the file optionally
        :param str mimetype: the mimetype of the file
        """
        self.file = file
        self.structure = structure
        self.mimetype = mimetype

    def get_file_path(self):
        """
        Returns the path to the file
        :rtype: str
        """
        return self.file.get_filepath()

    def get_file_url(self):
        """
        Returns the url to the file using the default file protocol set in the config
        :rtype: str
        """
        return self.file.get_url()

    def get_mimetype(self):
        """
        Returns the mimetype
        :rtype: str
        """
        if self.mimetype is None:
            self.mimetype, _ = mimetypes.guess_type(self.get_file_url())
        return self.mimetype

    def get_structure(self):
        """
        Returns the custom structure of the data provider if it exists
        :rtype: object
        """
        return self.structure

    def __str__(self):
        return self.get_file_url()

    def to_eq_hash(self):
        """
        Returns a hash of the object that can be used to compare with other providers that might be instantiated
        in a different run of the program. Two data providers must be hash equal if by importing them you
        obtain the same end result
        :rtype: str
        """
        return self.get_file_url()
