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


class UrlDataProvider(DataProvider):
    def __init__(self, url):
        """
        Class representing a data provider backed by a network accessible resource
        :param str url: the url to the online resources
        """
        self.url = url

    def get_url(self):
        """
        Returns the url
        :rtype: str
        """
        return self.url

    def get_mimetype(self):
        """
        Returns the mimetype
        :rtype: str
        """
        mimetypes.guess_type(self.get_url())

    def __str__(self):
        return self.url

    def to_eq_hash(self):
        return self.url