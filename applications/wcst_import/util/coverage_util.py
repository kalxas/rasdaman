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

from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException
from session import Session
from util.url_util import validate_and_read_url, url_read_exception
from util.import_util import decode_res
from lxml import etree


class CoverageUtil:
    def __init__(self, coverage_id):
        """
        Class to retrieve axis labels from an already existing coverage id
        :param str coverage_id: the coverage id
        """
        self.wcs_service = ConfigManager.wcs_service
        self.admin_service = ConfigManager.admin_service
        self.coverage_id = coverage_id
        # this value is cached in CoverageUtilCache
        self.cov_exist = None

    def exists(self):
        """
        Returns true if the coverage exist, false otherwise
        :rtype bool
        """
        if self.cov_exist is None:
            try:
                # Check if coverage exists via the Non-standard REST endpoint
                service_call = self.admin_service + "/coverage/exist?COVERAGEID=" + self.coverage_id
                response = decode_res(validate_and_read_url(service_call))

                self.cov_exist = (response == "true")
                return self.cov_exist
            except Exception as ex:
                # Something is wrong, try with the standard WCS DescribeCoverage request
                pass

            try:
                # Check if coverage exists in WCS DescribeCoverage result
                service_call = self.wcs_service + "?service=WCS&request=DescribeCoverage&version=" + \
                               Session.get_WCS_VERSION_SUPPORTED() \
                               + "&coverageId=" + self.coverage_id

                response = validate_and_read_url(service_call)
                if decode_res(response).strip() != "":
                    self.cov_exist = True
                else:
                    self.cov_exist = False
            except Exception as ex:
                exception_text = str(ex)

                if not "NoSuchCoverage" in exception_text:
                    raise RuntimeException("Could not check if the coverage exists. "
                                       "Reason: {}".format(exception_text))
                else:
                    # coverage doesn't exist
                    self.cov_exist = False

        return self.cov_exist

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
            exception_text = str(ex)

            if "Missing basic authentication header" in exception_text:
                raise RuntimeException("Endpoint '{}' requires valid rasdaman credentials with format username:password in a text file. \n"
                                       "Hint: Create this identify file first with read permission for user running wcst_import, \n"
                                       "then rerun wcst_import.sh ingredients.json -i path_to_the_identity_file.".format(self.wcs_service))

            raise RuntimeException("Could not retrieve the axis labels by WCS DescribeCoverage request. \n"                                   
                                   "Reason: {}".format(str(ex)))

    def get_axes_labels(self):
        """
        Return axes labels as a list
        :rtype list[str]
        """
        response = decode_res(self.__describe_coverage())
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


class CoverageUtilCache:
    COVERAGE_UTIL_CACHES_DICT = {}

    @staticmethod
    def get_cov_util(cov_id):
        if cov_id not in CoverageUtilCache.COVERAGE_UTIL_CACHES_DICT:
            CoverageUtilCache.COVERAGE_UTIL_CACHES_DICT[cov_id] = CoverageUtil(cov_id)

        return CoverageUtilCache.COVERAGE_UTIL_CACHES_DICT[cov_id]

    @staticmethod
    def clear_caches():
        CoverageUtilCache.COVERAGE_UTIL_CACHES_DICT.clear()
