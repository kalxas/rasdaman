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
from util.coverage_util import CoverageUtil
from util.log import log


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
        if 'uom/UCUM/0/d' in self.uom:
            return True
        return False

    def is_uom_second(self):
        if 'uom/UCUM/0/s' in self.uom:
            return True
        return False

    def is_date(self):
        if self.is_uom_day() or self.is_uom_second():
            return True
        return False


class CRSUtil:


    LAT_AXIS_LABEL = "Lat"

    LONG_AXIS_LABEL_EPSG_VERSION_85 = "Long"
    LONG_AXIS_LABEL_EPSG_VERSION_0 = "Lon"

    AUTHORITY = "authority"
    VERSION = "version"

    EPSG = "EPSG"
    EPSG_VERSION_85 = "8.5"
    EPSG_VERSION_0 = "0"

    # CRS in REST format
    EPSG_VERSION_85_REST = EPSG + "/" + EPSG_VERSION_85
    EPSG_VERSION_0_REST = EPSG + "/" + EPSG_VERSION_0

    # CRS in KVP format
    EPSG_AUTHORITY_KVP = AUTHORITY + "=" + EPSG
    EPSG_VERSION_85_KVP = VERSION + "=" + EPSG_VERSION_85
    EPSG_VERSION_0_KVP = VERSION + "=" + EPSG_VERSION_0

    # NOTE: Only fetch coverage axis labels once only for 1 coverage Id
    coverage_axis_labels = []

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

    def get_axes(self, coverage_id, axes_configurations=None):
        """
        Returns the axes of the CRS
        :param dict{ axis1(dict), axis2(dict),... }: dictionary of configurations from ingredient file
        :rtype: list[CRSAxis]
        """
        # update crs_axes if necessary for EPSG:4326
        CRSUtil.update_lon_axis_to_epsg_version_85_if_needed(coverage_id, self.axes, axes_configurations)

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

    @staticmethod
    def update_lon_axis_to_epsg_version_85_if_needed(coverage_id, crs_axes, axes_configurations=None):
        """
        NOTE: since rasdaman version 9.7+, in SECORE def/crs/EPSG/0 points to the newest EPSG version (e.g: 9.4.2 instead
        of 8.5 as before). The problem is for EPSG:4326, Longitude axis's abbreviation changes from "Long" -> "Lon".
        This method is used to allow import slices to existing coverage ("Lat Long" axes).
        :param str coverage_id: existing coverage
        :param list[CRSAxis] crs_axes: parsed CRSAxes from SECORE URL (e.g: def/crs/EPSG/4326 returns 2 CRSAxes: Lat, Lon)
        :param dict{ axis1(dict), axis2(dict),... }: dictionary of configurations from ingredient file
        """

        is_update = False

        cov = CoverageUtil(coverage_id)

        # Case 1: Coverage exists with "Lat Long" axes
        if len(CRSUtil.coverage_axis_labels) == 0:
            if cov.exists():
                CRSUtil.coverage_axis_labels = cov.get_axis_labels()

                if CRSUtil.LONG_AXIS_LABEL_EPSG_VERSION_85 in CRSUtil.coverage_axis_labels:
                    is_update = True

        # Case 2: Coverage not exist, but in ingredient file for general recipes, it contains configuration for "Lat Long" axes
        if axes_configurations is not None:
            for key, value in axes_configurations.iteritems():
                if key == CRSUtil.LONG_AXIS_LABEL_EPSG_VERSION_85:
                    is_update = True

                CRSUtil.coverage_axis_labels.append(key)

        # If it needs to update "Lon" -> "Long" axis
        if is_update:
            lat_axis = None
            long_axis = None

            for crs_axis in crs_axes:
                if crs_axis.label == CRSUtil.LAT_AXIS_LABEL:
                    lat_axis = crs_axis
                elif crs_axis.label == CRSUtil.LONG_AXIS_LABEL_EPSG_VERSION_0:
                    long_axis = crs_axis

                if lat_axis != None and long_axis != None:                   
                    
                    log.warn("EPSG/0/NNNN points to the latest EPSG dictionary version, "
                             "so CRS definitions may change with new releases of the EPSG dataset. \n"
                             "In particular, coverage '" + coverage_id + "' was created when latest EPSG "
                             "version was 8.5, which for longitude axis is now incompatible with the current "
                             "EPSG version (Long axis label changed to Lon). \n Therefore wcst_import will change "
                             "the CRS URL from EPSG/0/NNNN to EPSG/8.5/NNNN.")

                    long_axis.label = CRSUtil.LONG_AXIS_LABEL_EPSG_VERSION_85

                    if CRSUtil.EPSG_VERSION_0_KVP in long_axis.uri:
                        # CRS in KVP format
                        long_axis.uri = long_axis.uri.replace(CRSUtil.EPSG_VERSION_0_KVP, CRSUtil.EPSG_VERSION_85_KVP)
                        lat_axis.uri = lat_axis.uri.replace(CRSUtil.EPSG_VERSION_0_KVP, CRSUtil.EPSG_VERSION_85_KVP)
                    else:
                        # CRS in REST format
                        long_axis.uri = long_axis.uri.replace(CRSUtil.EPSG_VERSION_0_REST, CRSUtil.EPSG_VERSION_85_REST)
                        lat_axis.uri = long_axis.uri.replace(CRSUtil.EPSG_VERSION_0_REST, CRSUtil.EPSG_VERSION_85_REST)
                    break

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


