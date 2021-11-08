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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
from abc import ABCMeta, abstractmethod

import sys
from collections import OrderedDict

if sys.version_info[0] < 3:
    from urllib import urlencode
else:
    from urllib.parse import urlencode

from config_manager import ConfigManager


class AdminRequest:
    __metaclass__ = ABCMeta

    """
    Generic class for Admin requests
    """

    def __init__(self):
        self.context_path = ConfigManager.admin_service

    @abstractmethod
    def _get_request_type_parameters(self):
        """
        Returns the request specific parameters
        :rtype dict
        """
        pass

    def get_query_string(self):
        """
        return the part in the KVP request after ?
        """
        params = self._get_request_type_parameters()
        encoded_extra_params = ""
        for key, value in params.items():
            if value is not None:
                tmp_dict = {}
                tmp_dict[key] = value

                encoded_extra_params += "&" + urlencode(tmp_dict)

        query_string = encoded_extra_params
        return query_string


class InspireUpdateMetadataURLRequest(AdminRequest):

    def __init__(self, base_coverage_id, inspire_metadata_url):
        """
        Class to request to create a downscaled level coverage and add this coverage to a base coverage's pyramid

        e.g. Example: /admin/inspire/metadata/update?COVERAGEID=test_cov1
                                     &METADATAURL=http://xxx123.566/abcdef/123456/aaa.xml
        """
        super(InspireUpdateMetadataURLRequest, self).__init__()

        self.context_path += "/inspire/metadata/update"

        self.base_coverage_id = base_coverage_id
        self.inspire_metadata_url = inspire_metadata_url

    def _get_request_type_parameters(self):
        request_kvp = OrderedDict()
        request_kvp["COVERAGEID"] = self.base_coverage_id
        request_kvp["METADATAURL"] = self.inspire_metadata_url

        return request_kvp
