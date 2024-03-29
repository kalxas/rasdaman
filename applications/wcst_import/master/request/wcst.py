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
from abc import abstractmethod
import xml.etree.ElementTree as XMLProcessor
from collections import OrderedDict

from util.log import log, make_bold
from util.url_util import validate_and_read_url
from util.url_util import send_post_request
from util.import_util import encode_res, decode_res

import sys
if sys.version_info[0] < 3:
    from urllib import urlencode
else:
    from urllib.parse import urlencode


class WCSTSubset:
    def __init__(self, axis, dimension_min, dimension_max=None):
        """
        :param str axis: the axis on which to make the subset
        :param str|float dimension_min: the min value of the subset
        :param str|float dimension_max: the max value of the subset; if not provided, it will be considered a slice on min
        """
        self.axis = axis
        self.min = dimension_min
        self.max = dimension_max

    def to_request_kvp(self):
        """
        Returns the encode to kvp of the subset
        :rtype str
        """
        ret = self.axis + "(" + str(self.min)
        if self.max is not None:
            ret += "," + str(self.max)
        ret += ")"
        return ret

    __SUBSET_PARAM_NAME = "subset"


class WCSTRequest:
    """
    Generic class for WCST requests
    """
    SERVICE_PARAMETER = "service"
    SERVICE_VALUE = "WCS"
    VERSION_PARAMETER = "version"
    VERSION_VALUE = "2.0.1"
    REQUEST_PARAMETER = "request"

    def __init__(self):
        self.global_params = {}

    def get_query_string(self):
        """
        Returns the query string that defines the WCST requests (the get parameters in string format)
        :rtype str
        """
        params = self._get_request_type_parameters().copy()
        params.update(self.global_params)
        encoded_extra_params = ""
        for key, value in params.items():
            if value is not None:
                # We don't send the UpdateCoverage request with subset1=Lat(...)&subset2=Long(...) as they are not valid
                if str(key).startswith("subset"):
                    key = "subset"

                tmp_dict = {}
                tmp_dict[key] = value

                encoded_extra_params += "&" + urlencode(tmp_dict)

        tmp_dict = {self.SERVICE_PARAMETER: self.SERVICE_VALUE,
                    self.VERSION_PARAMETER: self.VERSION_VALUE,
                    self.REQUEST_PARAMETER: self._get_request_type()}

        query_string = urlencode(tmp_dict) + encoded_extra_params
        return query_string

    def add_global_param(self, key, value):
        """
        Adds extra parameters to the request, usually through the executor
        :param str key: the name of the parameter
        :param str value: the value of the parameter
        """
        self.global_params[key] = value

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


class WCSTInsertRequest(WCSTRequest):
    def __init__(self, coverage_ref, generate_id=False, pixel_data_type=None, tiling=None, insitu=None, coverage=None,
                 black_listed=None):
        """
        Class to represent WCST insert requests

        :param str coverage_ref: the name of the coverage in string format
        :param bool generate_id: true if a new id should be generated, false otherwise
        :param str pixel_data_type: the data type for each pixel in one band in GDAL format (e.g. Byte / Float32)
        :param str tiling: the tiling schema to be used for the coverage storage (e.g. regular [0:100, 0:100] )
        """
        WCSTRequest.__init__(self)
        self.coverage_ref = coverage_ref
        self.generate_id = generate_id
        self.pixel_data_type = pixel_data_type
        self.tiling = tiling
        self.insitu = insitu
        self.coverage = coverage
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
        request_kvp = OrderedDict()
        request_kvp[self.__GENERATE_ID_PARAMETER] = self.__GENERATE_ID_TRUE_VALUE if self.generate_id else self.__GENERATE_ID_FALSE_VALUE

        if self.pixel_data_type is not None:
            request_kvp[self.__PIXEL_DATA_TYPE_PARAMETER] = self.pixel_data_type
        if self.tiling is not None:
            request_kvp[self.__TILING_PARAMETER] = self.tiling

        if self.coverage is not None:
            request_kvp[self.__COVERAGE_PARAMETER] = self.coverage

        if self.coverage_ref is not None:
            request_kvp[self.__COVERAGE_REF_PARAMETER] = self.coverage_ref

        if self.black_listed is not None:
            request_kvp[self.__BLACK_LISTED] = self.black_listed

        return request_kvp

    __GENERATE_ID_TRUE_VALUE = "new"
    __GENERATE_ID_FALSE_VALUE = "existing"
    __GENERATE_ID_PARAMETER = "useId"
    __COVERAGE_PARAMETER = "coverage"
    __COVERAGE_REF_PARAMETER = "coverageRef"
    __PIXEL_DATA_TYPE_PARAMETER = "pixelDataType"
    __TILING_PARAMETER = "tiling"
    __REQUEST_TYPE = "InsertCoverage"
    __BLACK_LISTED = "BLACKLISTED"


class WCSTUpdateRequest(WCSTRequest):
    """
    Class to perform WCST update requests
    """

    def __init__(self, coverage_id, input_coverage_ref, subsets, insitu=None, input_coverage=None, file_path=None):
        """
        Constructor for the class

        :param coverage_id: string - the name of the coverage in string format
        :param input_coverage_ref: string - a link to the gml coverage
        :param subsets: list[CoverageSubset] - a list of coverage subsets objects
        """
        WCSTRequest.__init__(self)
        self.coverage_id = coverage_id
        self.input_coverage_ref = input_coverage_ref
        self.subsets = subsets
        self.insitu = insitu
        self.input_coverage = input_coverage
        self.file_path = file_path

    def _get_request_type(self):
        return self.__REQUEST_TYPE

    def _get_request_type_parameters(self):
        """
        Returns the request specific parameters
        :rtype dict
        """
        request_kvp = OrderedDict()
        request_kvp[self.__COVERAGE_ID_PARAMETER] = self.coverage_id

        # we will send subsets as subset=Lat(...)&subset=Long(...)
        subset_index = 1
        for subset in self.subsets:
            request_kvp["subset" + str(subset_index)] = subset.to_request_kvp()
            subset_index += 1

        if self.input_coverage is not None:
            request_kvp[self.__INPUT_COVERAGE_PARAMATER] = self.input_coverage

        if self.input_coverage_ref is not None:
            request_kvp[self.__INPUT_COVERAGE_REF_PARAMETER] = self.input_coverage_ref

        return request_kvp

    def get_request_params(self):
        key_values_dict = OrderedDict()
        key_values_dict[self.SERVICE_PARAMETER] = self.SERVICE_VALUE
        key_values_dict[self.VERSION_PARAMETER] = self.VERSION_VALUE
        key_values_dict[self.REQUEST_PARAMETER] = self._get_request_type()
        key_values_dict[self.__COVERAGE_ID_PARAMETER] = self.coverage_id

        subsets = []
        for subset in self.subsets:
            # e.g. Lat(20:30)
            subsets.append(subset.to_request_kvp())

        key_values_dict[self.__SUBSET_PARAM_NAME] = subsets

        if self.input_coverage is not None:
            key_values_dict[self.__INPUT_COVERAGE_PARAMATER] = self.input_coverage

        if self.input_coverage_ref is not None:
            key_values_dict[self.__INPUT_COVERAGE_REF_PARAMETER] = self.input_coverage_ref

        if self.insitu is not None and self.insitu is not False:
            key_values_dict[self.__INSITU_PARAMETER] = True

        return key_values_dict

    def _get_file_path(self):
        return self.file_path

    __SUBSET_PARAM_NAME = "subset"
    __COVERAGE_ID_PARAMETER = "coverageId"
    __INPUT_COVERAGE_PARAMATER = "inputCoverage"
    __INPUT_COVERAGE_REF_PARAMETER = "inputCoverageRef"
    __REQUEST_TYPE = "UpdateCoverage"
    __INSITU_PARAMETER = "insitu"


class WCSTDeleteRequest(WCSTRequest):
    def __init__(self, coverage_ref):
        """
        Class to perform WCST delete requests

        :param str coverage_ref: the id of the coverage in string format
        """
        WCSTRequest.__init__(self)
        self.coverage_ref = coverage_ref
        pass

    def _get_request_type(self):
        return self.__REQUEST_TYPE

    def _get_request_type_parameters(self):
        """
        Returns the request specific parameters
        :rtype dict
        """
        request_kvp = OrderedDict()
        request_kvp[self.__COVERAGE_REF_PARAMETER] = self.coverage_ref

        return request_kvp

    __COVERAGE_REF_PARAMETER = "coverageId"
    __REQUEST_TYPE = "DeleteCoverage"


class WCSTException(Exception):
    def __init__(self, exception_code, exception_text, service_call):
        """
        Exception that is thrown when a WCST request has gone wrong
        :param int exception_code: the exception code
        :param str exception_text:  the text of the exception
        :param str service_call: the service call for which the exception was thrown
        """
        self.exception_code = exception_code
        self.exception_text = exception_text
        self.service_call = service_call
        self.message = self.__str__()

    def __str__(self):
        return "Service Call: " + self.service_call + "\nError Code: " + str(self.exception_code) + \
               "\nError Text: " + self.exception_text


class WCSTBaseExecutor():
    __INSITU_PARAMETER = "insitu"

    def __init__(self, base_url, insitu=None):
        self.base_url = base_url
        self.insitu = insitu

    def prepare_request(self, request):
        if self.insitu is not None and isinstance(request, WCSTRequest):
            request.add_global_param(self.__INSITU_PARAMETER, self.insitu)
        return request


class WCSTExecutor(WCSTBaseExecutor):
    def __init__(self, base_url, insitu=None):
        """
        This class can be used to execute WCST requests and retrieve the result
        :param str base_url: the base url to the service that supports WCST
        """
        WCSTBaseExecutor.__init__(self, base_url, insitu)
        self.base_url = base_url

    @staticmethod
    def __check_request_for_errors(response, namespaces, service_call):
        """
        Checks if the WCST request was successful and if not raises an exception with the corresponding error code and
        error message

        :param str response: the response from the server
        :param dict namespaces: of namespaces to be used inside the xml parsing
        :param str service_call: the service call to the WCST
        """
        if response.find("ExceptionReport") != -1:
            xml = XMLProcessor.fromstring(response)
            error_code = ""
            error_text = response
            # this is WCST error
            if namespaces is not None:
                for error in xml.findall("ows:Exception", namespaces):
                    error_code = error.attrib["exceptionCode"]
            # check if a WMS error occurred and parse accordingly
            else:
                for error in xml:
                    error_code = error.attrib["code"]

            raise WCSTException(error_code, error_text, service_call)

    def execute(self, request, output=False, mock=False, input_base_url=None):
        """
        Executes a WCST request and returns the response to it

        :param WCSTRequest request: the request to be executed
        :rtype str
        """
        request = self.prepare_request(request)

        base_url_tmp = self.base_url
        if input_base_url is not None:
            base_url_tmp = input_base_url
        elif hasattr(request, "context_path"):
            if request.context_path is not None:
                base_url_tmp = request.context_path

        service_call = base_url_tmp + "?" + request.get_query_string()

        if output:
            log.info(service_call)
        if mock:
            log.info(make_bold("This is just a mocked request, no data will be changed."))
            log.info(service_call)
            return

        from config_manager import ConfigManager
        if ConfigManager.service_is_local is False and isinstance(request, WCSTUpdateRequest):
            # NOTE: WCS-T UpdateRequest can send file in case endpoint is not localhost
            request_params = request.get_request_params()
            file_path = request._get_file_path()
            response = send_post_request(base_url_tmp, request_params, file_path)
        else:
            try:
                response = decode_res(validate_and_read_url(base_url_tmp, request.get_query_string()))
            except Exception as ex:
                raise WCSTException(404, "Failed reading response from WCS service. "
                                         "Detailed error: {}".format(str(ex)), service_call)

        namespaces = ""

        # check if a WMS error occurred and parse accordingly
        if response.find("ServiceExceptionReport") != -1:
            namespaces = None
        # if WCST error then using ows:ExceptionReport
        elif response.find("ExceptionReport") != -1:
            namespaces = {"ows": "http://www.opengis.net/ows/2.0"}

        # Check error from response
        self.__check_request_for_errors(response, namespaces, service_call)

        try:
            if str(response) != "" and str(response) != "None":
                return encode_res(response)
            return ""
        except Exception as ex:
            raise WCSTException(0, "General exception while executing the request: " + str(ex), service_call)


class WCSTMockExecutor(WCSTBaseExecutor):
    def __init__(self, base_url, insitu=None):
        """
        A mock executor that only prints the service calls instead of executing them
        :param str base_url: the base url to the service that supports WCST
        """
        WCSTBaseExecutor.__init__(self, base_url, insitu)
        self.base_url = base_url
        self.insitu = insitu

    def execute(self, request):
        """
        Prints the service call that would be executed if a real executor would be used
        :param WCSTRequest request: the request to be executed
        :rtype str
        """
        request = self.prepare_request(request)
        service_call = self.base_url + "?" + request.get_query_string()
        log.info(make_bold("This is just a mocked request, no data will be changed."))
        log.info(service_call)
