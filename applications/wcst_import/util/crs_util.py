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

from lxml import etree
import urlparse

from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException
from url_util import validate_and_read_url


class CRSAxis:
    def __init__(self, uri, label, axisDirection, uom):
        """
        Class to represent a crs axis with a set of utility methods to better determine its type
        :param str uri: the uri of the axis
        :param str label: the label of the axis
        :param str axisDirection: the direction of the axis
        :param str uom: the unit of measure
        """
        self.uri = uri
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

    def is_uom_day(self):
        if self.uom == 'http://www.opengis.net/def/uom/UCUM/0/d':
            return True
        return False

    def is_uom_second(self):
        if self.uom == 'http://www.opengis.net/def/uom/UCUM/0/s':
            return True
        return False

    def is_date(self):
        if self.is_uom_day() or self.is_uom_second():
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
        self.individual_crs_axes = {}
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

    def get_crs_for_axes(self, axis_list):
        """
        Returns the crs corresponding to the given axes
        :param list[CRSAxis] axis_list: the list of crs axes
        :return: url of the resulting crs
        """
        found_crses = []
        for axis in axis_list:
            for crs in self.individual_crs_axes:
                if axis.label in self.individual_crs_axes[crs] and crs not in found_crses:
                    found_crses.append(crs)
        if len(found_crses) > 1:
            return self.get_compound_crs(found_crses)
        elif len(found_crses) == 1:
            return found_crses[0]

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
        self.axes = self.get_from_cache(self.crs_url)["axes"]
        self.individual_crs_axes = self.get_from_cache(self.crs_url)["individual_crs_axes"]
        if len(self.axes) == 0:
            if self.crs_url.find("crs-compound") != -1:
                self._parse_compound_crs()
            else:
                self._parse_single_crs(self.crs_url)
            self.save_to_cache(self.crs_url, self.axes, self.individual_crs_axes)

    def _parse_compound_crs(self):
        # http://kahlua.eecs.jacobs-university.de:8080/def/crs-compound?1=http://www.opengis.net/def/crs/EPSG/0/28992&2=http://www.opengis.net/def/crs/EPSG/0/5709
        try:
            url_parts = urlparse.urlparse(self.crs_url)
            get_params = urlparse.parse_qs(url_parts.query)
            index = 1
            while str(index) in get_params:
                self._parse_single_crs(get_params[str(index)][0])
                index += 1
        except Exception as ex:
            raise RuntimeException("Failed parsing the compound crs at: {}. "
                                   "Detailed error: {}".format(self.crs_url, str(ex)))

    def _parse_single_crs(self, crs):
        """
        Parses the axes out of the CRS definition
        """
        try:
            contents = validate_and_read_url(crs)
            root = etree.fromstring(contents)
            cselem = root.xpath("./*[contains(local-name(), 'CS')]")[0]
            xml_axes = cselem.xpath(".//*[contains(local-name(), 'SystemAxis')]")
            axesLabels = []
            for xml_axis in xml_axes:
                label = xml_axis.xpath(".//*[contains(local-name(), 'axisAbbrev')]")[0].text
                direction = xml_axis.xpath(".//*[contains(local-name(), 'axisDirection')]")[0].text

                # IndexND crses do not define the direction properly so try to detect them here based
                # on their labels and direction
                if direction.find("indexedAxisPositive") != - 1 and label == "i":
                    direction = "east"
                if direction.find("indexedAxisPositive") != - 1 and label == "j":
                    direction = "north"
                if "future" in direction:
                    uom = root.xpath(".//*[contains(local-name(), 'CoordinateSystemAxis')]")[0].attrib['uom']
                else:
                    uom = ""

                # in some crs definitions the axis direction is not properly set, override
                if label in self.X_AXES:
                    direction = "east"
                elif label in self.Y_AXES:
                    direction = "north"

                crsAxis = CRSAxis(crs, label, direction, uom)
                axesLabels.append(label)
                self.axes.append(crsAxis)
            # add to the list of individual crs to axes
            self.individual_crs_axes[crs] = axesLabels
        except Exception as ex:
            raise RuntimeException("Failed parsing the crs at: {}. "
                                   "Detail error: {}".format(crs, str(ex)))

    def save_to_cache(self, crs, axes, individual_crs_axes):
        self.__CACHE__[crs] = {}
        self.__CACHE__[crs]["axes"] = axes
        self.__CACHE__[crs]["individual_crs_axes"] = individual_crs_axes

    def get_from_cache(self, crs):
        if crs in self.__CACHE__:
            return self.__CACHE__[crs]
        return {"axes": [], "individual_crs_axes": {}}

    __CACHE__ = {}

    X_AXES = ["X", "E", "M", "E(X)", "x", "e", "Long", "Lon", "i"]
    Y_AXES = ["Y", "N", "P", "E(Y)", "y", "n", "Lat", "j"]

