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
            service_call = self.wcs_service + "?service=WCS&request=DescribeCoverage&version=" + \
                           Session.get_WCS_VERSION_SUPPORTED() + "&coverageId=" + self.coverage_id
            # Check if exception is thrown in the response
            ret = url_read_exception(service_call, 'exceptionCode="NoSuchCoverage"')
            if ret:
                # exception is in the response, coverage does not exist
                return False
            else:
                # exception is not the in the response, coverage does exist
                return True
        except Exception as ex:
            raise RuntimeException("Could not check if the coverage exists. "
                                   "Check that the WCS service is up and running on url: {}. "
                                   "Detail error: {}".format(self.wcs_service, str(ex)))

    def get_axis_labels(self):
        """
        Returns the axis labels as a list
        :rtype list[str]
        """
        try:
            service_call = self.wcs_service + "?service=WCS&request=DescribeCoverage&version=" + \
                           Session.get_WCS_VERSION_SUPPORTED() + "&coverageId=" + self.coverage_id
            response = validate_and_read_url(service_call)

            return response.split("axisLabels=\"")[1].split('"')[0].split(" ")
        except Exception as ex:
            raise RuntimeException("Could not retrieve the axis labels. "
                                   "Check that the WCS service is up and running on url: {}. "
                                   "Detail error: {}".format(self.wcs_service, str(ex)))