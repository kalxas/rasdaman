from abc import ABCMeta, abstractmethod
import urllib as url_lib
import xml.etree.ElementTree as XMLProcessor

from util.log import log


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
    __metaclass__ = ABCMeta
    SERVICE_PARAMETER = "service"
    SERVICE_VALUE = "WCS"
    VERSION_PARAMETER = "version"
    VERSION_VALUE = "2.0.1"
    REQUEST_PARAMETER = "request"

    def get_query_string(self):
        """
        Returns the query string that defines the WCST requests (the get parameters in string format)
        :rtype str
        """
        extra_params = ""
        for key, value in self._get_request_type_parameters().iteritems():
            extra_params += "&" + key + "=" + value
        return self.SERVICE_PARAMETER + "=" + self.SERVICE_VALUE + "&" + \
               self.VERSION_PARAMETER + "=" + self.VERSION_VALUE + "&" + \
               self.REQUEST_PARAMETER + "=" + self._get_request_type() + extra_params

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
    def __init__(self, coverage_ref, generate_id=False, pixel_data_type=None, tiling=None):
        """
        Class to represent WCST insert requests

        :param str coverage_ref: the name of the coverage in string format
        :param bool generate_id: true if a new id should be generated, false otherwise
        :param str pixel_data_type: the data type for each pixel in one band in GDAL format (e.g. Byte / Float32)
        :param str tiling: the tiling schema to be used for the coverage storage (e.g. regular [0:100, 0:100] )
        """
        self.coverage_ref = coverage_ref
        self.generate_id = generate_id
        self.pixel_data_type = pixel_data_type
        self.tiling = tiling

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
        ret = {
            self.__GENERATE_ID_PARAMETER: self.__GENERATE_ID_TRUE_VALUE if self.generate_id else self.__GENERATE_ID_FALSE_VALUE,
            self.__COVERAGE_REF_PARAMETER: self.coverage_ref,
        }
        if self.pixel_data_type is not None:
            ret[self.__PIXEL_DATA_TYPE_PARAMETER] = self.pixel_data_type
        if self.tiling is not None:
            ret[self.__TILING_PARAMETER] = self.tiling
        return ret

    __GENERATE_ID_TRUE_VALUE = "new"
    __GENERATE_ID_FALSE_VALUE = "existing"
    __GENERATE_ID_PARAMETER = "useId"
    __COVERAGE_REF_PARAMETER = "coverageRef"
    __PIXEL_DATA_TYPE_PARAMETER = "pixelDataType"
    __TILING_PARAMETER = "tiling"
    __REQUEST_TYPE = "InsertCoverage"


class WCSTUpdateRequest(WCSTRequest):
    """
    Class to perform WCST insert requests
    """

    def __init__(self, coverage_id, input_coverage_ref, subsets):
        """
        Constructor for the class

        :param coverage_id: string - the name of the coverage in string format
        :param input_coverage_ref: string - a link to the gml coverage
        :param subsets: list[CoverageSubset] - a list of coverage subsets objects
        """
        self.coverage_id = coverage_id
        self.input_coverage_ref = input_coverage_ref
        self.subsets = subsets

    def _get_request_type(self):
        return self.__REQUEST_TYPE

    def _get_request_type_parameters(self):
        """
        Returns the request specific parameters
        :rtype dict
        """
        request_kvp = {
            self.__COVERAGE_ID_PARAMETER: self.coverage_id,
            self.__COVERAGE_INPUT_PARAMETER: self.input_coverage_ref
        }
        # we will send subsets as subset1=Lat(...)&subset2=Long(...)
        subset_index = 1
        for subset in self.subsets:
            request_kvp["subset" + str(subset_index)] = subset.to_request_kvp()
            subset_index += 1
        return request_kvp

    __SUBSET_PARAM_NAME = "subset"
    __COVERAGE_ID_PARAMETER = "coverageId"
    __COVERAGE_INPUT_PARAMETER = "inputCoverageRef"
    __REQUEST_TYPE = "UpdateCoverage"


class WCSTDeleteRequest(WCSTRequest):
    def __init__(self, coverage_ref):
        """
        Class to perform WCST delete requests

        :param str coverage_ref: the name of the coverage in string format
        """
        self.coverage_ref = coverage_ref
        pass

    def _get_request_type(self):
        return self.__REQUEST_TYPE

    def _get_request_type_parameters(self):
        """
        Returns the request specific parameters
        :rtype dict
        """
        return {
            self.__COVERAGE_REF_PARAMETER: self.coverage_ref
        }

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


class WCSTExecutor():
    def __init__(self, base_url):
        """
        This class can be used to execute WCST requests and retrieve the result
        :param str base_url: the base url to the service that supports WCST
        """
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
        if response.find("ows:ExceptionReport") != -1:
            xml = XMLProcessor.fromstring(response)
            error_code = ""
            error_text = response
            for error in xml.findall("ows:Exception", namespaces):
                error_code = error.attrib["exceptionCode"]

            raise WCSTException(error_code, error_text, service_call)

    def execute(self, request, output=True):
        """
        Executes a WCST request and returns the response to it

        :param WCSTRequest request: the request to be executed
        :rtype str
        """
        service_call = self.base_url + "?" + request.get_query_string()
        if output:
            log.info(service_call)
        response = url_lib.urlopen(service_call).read()
        namespaces = {"ows": "http://www.opengis.net/ows/2.0"}
        self.__check_request_for_errors(response, namespaces, service_call)
        try:
            if str(response) != "" and str(response) != "None":
                xml = XMLProcessor.fromstring(response)
                result = xml.text
                return result
            return ""
        except Exception as ex:
            raise WCSTException(0, "General exception while executing the request: " + str(ex), service_call)


class WCSTMockExecutor():
    def __init__(self, base_url):
        """
        A mock executor that only prints the service calls instead of executing them
        :param str base_url: the base url to the service that supports WCST
        """
        self.base_url = base_url

    def execute(self, request):
        """
        Prints the service call that would be executed if a real executor would be used
        :param str request: WCSTRequest - the request to be executed
        :rtype str
        """
        service_call = self.base_url + "?" + request.get_query_string()
        print service_call
