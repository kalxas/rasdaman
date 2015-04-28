import urllib
import re

from recipes.shared.runtime_exception import RuntimeException


class CRSUtil:
    def __init__(self, crs_url):
        """
        Initializes the crs util
        :param crs_url: the crs url
        """
        self.crs_url = crs_url
        pass

    def get_axis_by_direction(self, axis_direction):
        """
        Returns the axis abbreviation based on the direction of the axis
        :return: str
        """
        try:
            contents = str(urllib.urlopen(self.crs_url).read())
            ret = re.search(
                '<gml:([^\0]*)CS>([^\0]*)<gml:axisAbbrev>(.*)</gml:axisAbbrev>(\s*)<gml:axisDirection(.*)>' + axis_direction + '</gml:axisDirection>',
                contents).group(3)
            return ret
        except Exception:
            raise RuntimeException("Could not retrieve the axis definitions from the crs at url: " + self.crs_url)

    @staticmethod
    def get_crs_url(crs_resolver, authority, code):
        """
        Returns a crs url from a resolver and the authority and code (common format for gdal)
        :param str crs_resolver: the url to the crs resolver (i.e. http://opengis.net/def/)
        :param str authority: the authority, e.g. EPSG
        :param str code: the code for the crs e.g. 4326
        :rtype: str
        """
        if crs_resolver[-1] != '/':
            crs_resolver += '/'
        return crs_resolver + "crs/" + authority + "/0/" + code


class CRSGeoUtil(CRSUtil):
    def get_east_axis(self):
        """
        Returns the axis abbreviation pointing east
        :rtype: str
        """
        return self.get_axis_by_direction("east")

    def get_north_axis(self):
        """
        Returns the axis abbreviation pointing north
        :rtype: str
        """
        return self.get_axis_by_direction("north")

    def get_up_axis(self):
        """
        Returns the axis abbreviation pointing up
        :rtype: str
        """
        return self.get_axis_by_direction("up")


class CRSTimeUtil(CRSUtil):
    def get_future_axis(self):
        """
        Returns the axis abbreviation pointing towards the future
        :rtype: str
        """
        try:
            contents = str(urllib.urlopen(self.crs_url).read())
            return contents.split('http://www.opengis.net/def/axisDirection/OGC/1.0/future</axisDirection>')[0] \
                .split('<axisAbbrev>')[1] \
                .split('</axisAbbrev>')[0]
        except Exception:
            raise RuntimeException("Could not retrieve the axis definitions from the crs at url: " + self.crs_url)