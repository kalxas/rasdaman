import urllib
from lxml import etree
import urlparse

from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException


class CRSAxis:
    def __init__(self, label, axisDirection, uom):
        """
        Class to represent a crs axis with a set of utility methods to better determine its type
        :param str label: the label of the  axis
        :param str axisDirection: the direction of the axis
        :param str uom: the unit of measure
        """
        self.label = label
        self.axisDirection = axisDirection
        self.uom = uom
        pass

    def is_easting(self):
        if "east" in self.axisDirection.lower() or "west" in self.axisDirection.lower():
            return True
        return False

    def is_northing(self):
        if "north" in self.axisDirection.lower() or "south" in self.axisDirection.lower():
            return True
        return False

    def is_future(self):
        if "future" in self.axisDirection.lower() or "past" in self.axisDirection.lower():
            return True
        return False

    def is_height(self):
        if "up" in self.axisDirection.lower() or "down" in self.axisDirection.lower():
            return True
        return False


class CRSUtil:
    def __init__(self, crs_url):
        """
        Initializes the crs util
        :param str crs_url: the crs url
        """
        self.crs_url = crs_url
        self.axes = []
        self._parse()
        pass

    def get_axes(self):
        """
        Returns the axes of the CRS
        :rtype: list[CRSAxis]
        """
        return self.axes

    def get_crs_code(self):
        """
        Returns the code part of a CRS URI
        :return: str
        """
        return self.crs_url.rpartition("/")[-1]

    @staticmethod
    def get_crs_url(authority, code):
        """
        Returns a crs url from a resolver and the authority and code (common format for gdal)
        :param str authority: the authority, e.g. EPSG
        :param str code: the code for the crs e.g. 4326
        :rtype:
         str
        """
        crs_resolver = ConfigManager.crs_resolver
        if crs_resolver[-1] != '/':
            crs_resolver += '/'
        return crs_resolver + "crs/" + authority + "/0/" + code

    @staticmethod
    def get_compound_crs(crses):
        """
        Creates a compound crs from a list of crses
        :param list[str] crses: a list of crs urls to form the compund crs
        """
        index = 1
        crs_list = []
        for crs in crses:
            crs_list.append(str(index) + "=" + crs)
            index += 1
        compound = ConfigManager.crs_resolver + "crs-compound?" + "&".join(crs_list)
        return compound

    def _parse(self):
        self.axes = self.get_from_cache(self.crs_url)
        if len(self.axes) == 0:
            if self.crs_url.find("crs-compound") != -1:
                self._parse_compound_crs()
            else:
                self._parse_single_crs(self.crs_url)
            self.save_to_cache(self.crs_url, self.axes)

    def _parse_compound_crs(self):
        # http://kahlua.eecs.jacobs-university.de:8080/def/crs-compound?1=http://www.opengis.net/def/crs/EPSG/0/28992&2=http://www.opengis.net/def/crs/EPSG/0/5709
        try:
            url_parts = urlparse.urlparse(self.crs_url)
            get_params = urlparse.parse_qs(url_parts.query)
            index = 1
            while str(index) in get_params:
                self._parse_single_crs(get_params[str(index)][0])
                index += 1
        except Exception as e:
            raise RuntimeException(
                "We could not parse the compound crs at " + self.crs_url +
                ". Please check that the url is correct. Detailed error: " + str(e))

    def _parse_single_crs(self, crs):
        """
        Parses the axes out of the CRS definition
        """
        try:
            contents = str(urllib.urlopen(crs).read())
            root = etree.fromstring(contents)
            cselem = root.xpath("./*[contains(local-name(), 'CS')]")[0]
            xml_axes = cselem.xpath(".//*[contains(local-name(), 'SystemAxis')]")
            for xml_axis in xml_axes:
                label = xml_axis.xpath(".//*[contains(local-name(), 'axisAbbrev')]")[0].text
                direction = xml_axis.xpath(".//*[contains(local-name(), 'axisDirection')]")[0].text

                # IndexND crses do not define the direction properly so try to detect them here based
                # on their labels and direction
                if direction.find("indexedAxisPositive") != - 1 and label == "i":
                    direction = "east"
                if direction.find("indexedAxisPositive") != - 1 and label == "j":
                    direction = "north"

                # TODO: While not mandatory it would be nice if we could parse the uom as well
                self.axes.append(CRSAxis(label, direction, ""))
        except:
            raise RuntimeException(
                "We could not parse the crs at " + crs + ". Please check that the url is correct.")

    def save_to_cache(self, crs, axes):
        self.__CACHE__[crs] = axes

    def get_from_cache(self, crs):
        if crs in self.__CACHE__:
            return self.__CACHE__[crs]
        return []

    __CACHE__ = {}

