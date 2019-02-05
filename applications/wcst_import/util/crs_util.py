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
import re
from collections import OrderedDict

from lxml import etree
import urlparse

from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException
from url_util import validate_and_read_url
from util.coverage_util import CoverageUtil
from util.log import log
from time_util import DateTimeUtil


class CRSAxis:

    AXIS_TYPE_X = "X"
    AXIS_TYPE_Y = "Y"

    AXIS_TYPE_ELEVATION_UP = "H"
    AXIS_TYPE_ELEVATION_DOWN = "D"

    AXIS_TYPE_TIME_DAY = "d"
    AXIS_TYPE_TIME_SECOND = "s"

    AXIS_TYPE_UNKNOWN = "UNKNOWN"

    # These axis abbreviations are collected from EPSG database, http://localhost:8080/def/cs/EPSG
    X_AXES = ["X", "E", "M", "E(X)", "x", "e", "Long", "Lon", "i"]
    Y_AXES = ["Y", "N", "P", "E(Y)", "y", "n", "Lat", "j"]

    ELEVATION_UP_AXES = ["h", "H"]
    ELEVATION_DOWN_AXES = ["D"]

    def __init__(self, uri, label, axis_type, axis_uom):
        """
        Class to represent a crs axis with a set of utility methods to better determine its type
        :param str uri: the uri of the axis
        :param str label: the label of the axis
        :param str axis_type: the type of the axis (x, y, time, elevation,...)
        :param str axis_uom: the uom of axis
        """
        self.uri = uri
        self.label = label
        self.type = axis_type
        self.uom = axis_uom

    def is_x_axis(self):
        return self.type == self.AXIS_TYPE_X

    def is_y_axis(self):
        return self.type == self.AXIS_TYPE_Y

    def is_time_axis(self):
        return self.type == self.AXIS_TYPE_TIME_DAY \
               or self.type == self.AXIS_TYPE_TIME_SECOND

    def is_elevation_up_axis(self):
        return self.type == self.AXIS_TYPE_ELEVATION_UP

    def is_elevation_down_axis(self):
        return self.type == self.AXIS_TYPE_ELEVATION_DOWN

    def is_time_day_axis(self):
        return self.type == self.AXIS_TYPE_TIME_DAY

    def is_time_second_axis(self):
        return self.type == self.AXIS_TYPE_TIME_SECOND

    def is_date_axis(self):
        return self.is_time_day_axis() or self.is_time_second_axis()

    @staticmethod
    def get_axis_type_by_name(axis_label):
        """
        Return the type of axis by name
        :param str axis_label: name of axis
        :return: str type of axis
        """
        if axis_label in CRSAxis.X_AXES:
            return CRSAxis.AXIS_TYPE_X
        elif axis_label in CRSAxis.Y_AXES:
            return CRSAxis.AXIS_TYPE_Y

        elif axis_label in CRSAxis.ELEVATION_UP_AXES:
            return CRSAxis.AXIS_TYPE_ELEVATION_UP
        elif axis_label in CRSAxis.ELEVATION_DOWN_AXES:
            return CRSAxis.AXIS_TYPE_ELEVATION_DOWN

        else:
            return CRSAxis.AXIS_TYPE_UNKNOWN

    @staticmethod
    def get_axis_type_by_uom(axis_uom):
        """
        Return the type of axis by uom
        :param str axis_uom: uom of axis
        :return: str type of axis
        """
        # Used for Time axis (uom day/second)
        if axis_uom == "d":
            return CRSAxis.AXIS_TYPE_TIME_DAY
        elif axis_uom == "s":
            return CRSAxis.AXIS_TYPE_TIME_SECOND
        else:
            return CRSAxis.AXIS_TYPE_UNKNOWN


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
    coverage_crs = None

    def __init__(self, crs_url):
        """
        Initializes the crs util
        :param str crs_url: the crs url
        """
        self.crs_url = crs_url
        self.axes = []
        self.individual_crs_axes = OrderedDict()
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
                axis_uri = axis.uri
                if axis_uri == crs and axis_uri not in found_crses:
                    found_crses.append(axis.uri)
                    break
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

        cov = CoverageUtil(coverage_id)

        # Case 1: Coverage exists with "Lat Long" axes
        if len(CRSUtil.coverage_axis_labels) == 0:
            if cov.exists():
                CRSUtil.coverage_axis_labels = cov.get_axis_labels()

                for index, axis_label in enumerate(CRSUtil.coverage_axis_labels):
                    if axis_label == CRSUtil.LONG_AXIS_LABEL_EPSG_VERSION_85:
                        CRSUtil.log_crs_replacement_epsg_version_0_by_version_85()
                        crs_axes[index].label = CRSUtil.LONG_AXIS_LABEL_EPSG_VERSION_85
                        break

        # Case 2: Coverage not exist, but in ingredient file for general recipes, it contains configuration for "Lat Long" axes
        if axes_configurations is not None:
            for key, value in axes_configurations.iteritems():
                CRSUtil.coverage_axis_labels.append(key)

                for crs_axis in crs_axes:
                    # "Long" axis exists in configuration for ingredient file
                    if key == CRSUtil.LONG_AXIS_LABEL_EPSG_VERSION_85:
                        if crs_axis.label == CRSUtil.LONG_AXIS_LABEL_EPSG_VERSION_0:
                            crs_axis.label = CRSUtil.LONG_AXIS_LABEL_EPSG_VERSION_85
                            break

    @staticmethod
    def log_crs_replacement_epsg_version_0_by_version_85():
        """
        Just log a warning when it needs to replace version 0 to version 8.5 for EPSG
        """
        log.warn("EPSG/0/NNNN points to the latest EPSG dictionary version, "
                 "so CRS definitions may change with new releases of the EPSG dataset. \n"
                 "In particular, coverage was created when latest EPSG "
                 "version was 8.5, which for longitude axis is now incompatible with the current "
                 "EPSG version ('Long' axis label changed to 'Lon').\n Therefore wcst_import will change "
                 "longitude axis label to 'Long' for EPSG/0/NNNN.")

    def _parse(self):
        tmp_dict = self.get_from_cache(self.crs_url)
        self.axes = tmp_dict["axes"]
        self.individual_crs_axes = tmp_dict["individual_crs_axes"]
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
                crs = get_params[str(index)][0]
                self._parse_single_crs(crs)
                index += 1
        except Exception as ex:
            raise RuntimeException("Failed parsing the compound crs at: {}. "
                                   "Detailed error: {}".format(self.crs_url, str(ex)))

    def _parse_single_crs(self, crs):
        """
        Parses the axes out of the CRS definition
        str crs: a complete CRS request (e.g: http://localhost:8080/def/crs/EPSG/0/4326)
        """
        try:
            gml = validate_and_read_url(crs)
            root = etree.fromstring(gml)
            cselem = root.xpath("./*[contains(local-name(), 'CS')]")[0]
            xml_axes = cselem.xpath(".//*[contains(local-name(), 'SystemAxis')]")
            axis_labels = []

            for xml_axis in xml_axes:
                axis_label = xml_axis.xpath(".//*[contains(local-name(), 'axisAbbrev')]")[0].text
                axis_type = CRSAxis.get_axis_type_by_name(axis_label)

                # e.g: http://localhost:8080/def/uom/EPSG/0/9122
                uom_url = root.xpath(".//*[contains(local-name(), 'CoordinateSystemAxis')]")[0].attrib['uom']
                try:
                    uom_gml = validate_and_read_url(uom_url)
                    uom_root_element = etree.fromstring(uom_gml)

                    # e.g: degree (supplier to define representation)
                    uom_name = uom_root_element.xpath(".//*[contains(local-name(), 'name')]")[0].text
                    axis_uom = uom_name.split(" ")[0]
                except Exception:
                    # e.g: def/uom/OGC/1.0/GridSpacing does not exist -> GridSpacing
                    axis_uom = uom_url.split("/")[-1]

                # With OGC Time axes, check axis type in different way
                if re.search(DateTimeUtil.CRS_CODE_ANSI_DATE, crs) is not None \
                   or re.search(DateTimeUtil.CRS_CODE_UNIX_TIME, crs) is not None:
                    axis_type = CRSAxis.get_axis_type_by_uom(axis_uom)

                crs_axis = CRSAxis(crs, axis_label, axis_type, axis_uom)
                axis_labels.append(axis_label)

                self.axes.append(crs_axis)

            # add to the list of individual crs to axes
            self.individual_crs_axes[crs] = axis_labels
        except Exception as ex:
            raise RuntimeException("Failed parsing the crs at: {}. "
                                   "Detail error: {}".format(crs, str(ex)))

    def save_to_cache(self, crs, axes, individual_crs_axes):
        self.__CACHE__[crs] = OrderedDict()
        self.__CACHE__[crs]["axes"] = axes
        self.__CACHE__[crs]["individual_crs_axes"] = individual_crs_axes

    def get_from_cache(self, crs):
        if crs in self.__CACHE__:
            return self.__CACHE__[crs]
        return {"axes": [], "individual_crs_axes": OrderedDict()}

    __CACHE__ = {}


