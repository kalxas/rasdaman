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
from abc import ABCMeta, abstractmethod
from collections import OrderedDict


class WMSTRequest:
    __metaclass__ = ABCMeta

    """
    Generic class for WCST requests
    """
    SERVICE_PARAMETER = "service"
    SERVICE_VALUE = "WMS"
    VERSION_PARAMETER = "version"
    VERSION_VALUE = "1.3.0"
    REQUEST_PARAMETER = "request"

    def get_query_string(self):
        """
        Returns the query string that defines the WMST requests (the get parameters in string format)
        :rtype str
        """
        extra_params = ""
        for key, value in self._get_request_type_parameters().items():
            if value is not None:
                extra_params += "&" + key + "=" + str(value)
        return self.SERVICE_PARAMETER + "=" + self.SERVICE_VALUE + "&" + \
               self.VERSION_PARAMETER + "=" + self.VERSION_VALUE + "&" + \
               self.REQUEST_PARAMETER + "=" + self._get_request_type() + extra_params

    def add_global_param(self, key, value):
        """
        Adds extra parameters to the request, usually through the executor
        :param str key: the name of the parameter
        :param str value: the value of the parameter
        """
        pass

    @abstractmethod
    def _get_request_type_parameters(self):
        """
        Returns the request specific parameters
        :rtype dict
        """
        pass

    @abstractmethod
    def _get_request_type(self):
        """
        Returns the request type
        :rtype str
        """
        pass


class WMSTGetCapabilities(WMSTRequest):
    def __init__(self):
        """
        Class to request GetCapabilities from WMS 1.3.0 endpoint
        """

    def _get_request_type(self):
        """
        Returns the request type
        :rtype str
        """
        return self.__REQUEST_TYPE

    def _get_request_type_parameters(self):
        """
        Returns the request specific parameters
        :rtype dict
        """
        ret = {}

        return ret

    __REQUEST_TYPE = "GetCapabilities"


class WMSTDescribeLayer(WMSTRequest):

    def __init__(self, layer_name):
        """
        Class to request DescribeLayer
        """
        self.layer_name = layer_name

    def _get_request_type(self):
        """
        Returns the request type
        :rtype str
        """
        return self.__REQUEST_TYPE

    def _get_request_type_parameters(self):
        """
        Returns the request specific parameters
        :rtype dict
        """
        ret = OrderedDict()
        ret[self.__LAYER_NAME] = self.layer_name

        return ret

    __REQUEST_TYPE = "DescribeLayer"
    __LAYER_NAME = "layer"


class WMSTFromWCSInsertRequest(WMSTRequest):
    def __init__(self, wcs_coverage_id, with_pyramids, black_listed=None):
        """
        Class to insert a new wcs coverage into a WMS layer. This is not a standard way in OGC but a custom method in the
        WMS service offered by rasdaman to allow for automatic insertion
        Constructor for the class

        :param str wcs_coverage_id: the coverage id to be used as a layer
        """
        self.wcs_coverage_id = wcs_coverage_id
        self.with_pyramids = with_pyramids
        self.black_listed = black_listed

    def _get_request_type(self):
        """
        Returns the request type
        :rtype str
        """
        return self.__REQUEST_TYPE

    def _get_request_type_parameters(self):
        """
        Returns the request specific parameters
        :rtype dict
        """
        ret = OrderedDict()
        ret[self.__COVERAGE_ID_PARAMETER] = self.wcs_coverage_id
        ret[self.__BLACK_LISTED] = self.black_listed

        return ret

    __REQUEST_TYPE = "InsertWCSLayer"
    __COVERAGE_ID_PARAMETER = "wcsCoverageId"
    __BLACK_LISTED = "blackListed"


class WMSTFromWCSUpdateRequest(WMSTRequest):
    def __init__(self, wcs_coverage_id, with_pyramids):
        """
        Class to update a wcs coverage into an existing wms layer. This is not a standard way in OGC but a custom method in the
        WMS service offered by rasdaman to allow for automatic insertion
        Constructor for the class

        :param str wcs_coverage_id: the coverage id to be used as a layer
        """
        self.wcs_coverage_id = wcs_coverage_id
        self.with_pyramids = with_pyramids

    def _get_request_type(self):
        """
        Returns the request type
        :rtype str
        """
        return self.__REQUEST_TYPE

    def _get_request_type_parameters(self):
        """
        Returns the request specific parameters
        :rtype dict
        """
        ret = OrderedDict()
        ret[self.__COVERAGE_ID_PARAMETER] = self.wcs_coverage_id

        return ret

    __REQUEST_TYPE = "UpdateWCSLayer"
    __COVERAGE_ID_PARAMETER = "wcsCoverageId"


class WMSTFromWCSDeleteRequest(WMSTRequest):
    """
    Class to delete a wcs coverage into wms. This is not a standard way in OGC but a custom method in the
    WMS service offered by rasdaman to allow for automatic insertion
    """

    def __init__(self, wcs_coverage_id, with_pyramids):
        """
        Class to delete a wcs coverage into wms. This is not a standard way in OGC but a custom method in the
        WMS service offered by rasdaman to allow for automatic deletion
        Constructor for the class

        :param str wcs_coverage_id: the coverage id to be used as a layer
        """
        self.wcs_coverage_id = wcs_coverage_id
        self.with_pyramids = with_pyramids

    def _get_request_type(self):
        """
        Returns the request type
        :rtype str
        """
        return self.__REQUEST_TYPE

    def _get_request_type_parameters(self):
        """
        Returns the request specific parameters
        :rtype dict
        """
        ret = OrderedDict()
        ret[self.__COVERAGE_ID_PARAMETER] = self.wcs_coverage_id

        return ret

    __REQUEST_TYPE = "DeleteWCSLayer"
    __COVERAGE_ID_PARAMETER = "wcsCoverageId"
