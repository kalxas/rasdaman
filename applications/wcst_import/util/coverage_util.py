import urllib
from recipes.shared.runtime_exception import RuntimeException
from session import Session


class CoverageUtil:
    def __init__(self, wcs_service, coverage_id):
        """
        Class to retrieve axis labels from an already existing coverage id
        :param str wcs_service:  the wcs service where the coverage was ingested
        :param str coverage_id: the coverage id
        """
        self.wcs_service = wcs_service
        self.coverage_id = coverage_id

    def exists(self):
        """
        Returns true if the coverage exist, false otherwise
        :rtype bool
        """
        try:
            service_call = self.wcs_service + "?service=WCS&request=DescribeCoverage&version=" + \
                           Session.get_WCS_VERSION_SUPPORTED() + "&coverageId=" + self.coverage_id
            response = urllib.urlopen(service_call).read()
            if 'exceptionCode="NoSuchCoverage' in response:
                return False
            else:
                return True
        except Exception as ex:
            raise RuntimeException("Could not check if the coverage exists. "
                                   "Check that the WCS service is up and running.")

    def get_axis_labels(self):
        """
        Returns the axis labels as a list
        :rtype list[str]
        """
        try:
            service_call = self.wcs_service + "?service=WCS&request=DescribeCoverage&version=" + \
                           Session.get_WCS_VERSION_SUPPORTED() + "&coverageId=" + self.coverage_id
            response = urllib.urlopen(service_call).read()
            return response.split("axisLabels=\"")[1].split('"')[0].split(" ")
        except Exception as ex:
            raise RuntimeException("Could not retrieve the axis labels. Check that the WCS service is up and running.")