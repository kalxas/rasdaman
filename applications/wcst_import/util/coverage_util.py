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

from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException
from session import Session
from util.url_util import validate_and_read_url
from util.url_util import url_read_exception
from lxml import etree


class CoverageUtil:
    def __init__(self, coverage_id):
        """
        Class to retrieve axis labels from an already existing coverage id
        :param str coverage_id: the coverage id
        """
        self.wcs_service = ConfigManager.wcs_service
        self.coverage_id = coverage_id

    def exists(self):
        """
        Returns true if the coverage exist, false otherwise
        :rtype bool
        """
        try:
            # Check if coverage exists in WCS GetCapabilities result
            service_call = self.wcs_service + "?service=WCS&request=GetCapabilities&acceptVersions=" + \
                           Session.get_WCS_VERSION_SUPPORTED()
            response = validate_and_read_url(service_call)

            root = etree.fromstring(response)
            coverage_id_elements = root.xpath("//*[local-name() = 'CoverageId']")
            # Iterate all <CoverageId> elements to check coverageId exists already
            for element in coverage_id_elements:
                if self.coverage_id == element.text:
                    return True
            return False
        except Exception as ex:
            raise RuntimeException("Could not check if the coverage exists. "                                   
                                   "Detail error: {}".format(str(ex)))

    def __describe_coverage(self):
        """
        Send a DescribeCoverage request to petascope
        """
        try:
            service_call = self.wcs_service + "?service=WCS&request=DescribeCoverage&version=" + \
                       Session.get_WCS_VERSION_SUPPORTED() + "&coverageId=" + self.coverage_id
            response = validate_and_read_url(service_call)

            return response
        except Exception as ex:
            raise RuntimeException("Could not retrieve the axis labels. "                                   
                                   "Detail error: {}".format(str(ex)))

    def get_axes_labels(self):
        """
        Return axes labels as a list
        :rtype list[str]
        """
        response = self.__describe_coverage()
        return response.split("axisLabels=\"")[1].split('"')[0].split(" ")

    def get_axes_lower_bounds(self):
        """
        Return axes lower bounds as a list
        :return: list[str]
        """
        response = self.__describe_coverage()
        root = etree.fromstring(response)
        value = root.xpath(".//*[contains(local-name(), 'lowerCorner')]")[0].text
        lower_bounds = value.split(" ")

        return lower_bounds

