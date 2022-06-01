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
import re
from collections import OrderedDict
from typing import List, Any

from lxml import etree
import sys


from master.error.validate_exception import RecipeValidationException

if sys.version_info[0] < 3:
    import urlparse
else:
    import urllib.parse as urlparse

from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException
from .url_util import validate_and_read_url
from util.log import log
from .time_util import DateTimeUtil


class CRSAxis:

    AXIS_TYPE_X = "X"
    AXIS_TYPE_Y = "Y"

    AXIS_TYPE_ELEVATION_UP = "H"
    AXIS_TYPE_ELEVATION_DOWN = "D"

    AXIS_TYPE_TIME_DAY = "d"
    AXIS_TYPE_TIME_SECOND = "s"

    AXIS_TYPE_UNKNOWN = "UNKNOWN"

    # These axis abbreviations are collected from EPSG database, http://localhost:8080/def/cs/EPSG
    X_AXES = ["X".lower(), "E".lower(), "M".lower(), "E(X)".lower(), "Long".lower(), "Lon".lower(), "i"]
    Y_AXES = ["Y".lower(), "N".lower(), "P".lower(), "E(Y)".lower(), "Lat".lower(), "j"]

    ELEVATION_UP_AXES = ["H".lower()]
    ELEVATION_DOWN_AXES = ["D".lower()]

    UOM_UCUM = "uom/UCUM"

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
        self.grid_order = -1

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
        axis_label = axis_label.lower()

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

    LONG_AXIS_LABEL_EPSG_8_5 = "Long".lower()
    LONG_AXIS_LABEL_EPSG_0 = "Lon".lower()
    LAT_AXIS_AXIS = "Lat".lower()

    axes = []

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
    def axis_label_match(axis_label1, axis_label2):
        axis_label1 = str(axis_label1).lower()
        axis_label2 = str(axis_label2).lower()

        return (axis_label1 == axis_label2) \
                or (CRSUtil.is_longitude_axis(axis_label1) and CRSUtil.is_longitude_axis(axis_label2))

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

        if len(crses) > 1:
            # at least 2 CRSs
            for crs in crses:
                crs_list.append(str(index) + "=" + crs)
                index += 1
            compound = ConfigManager.crs_resolver + "crs-compound?" + "&".join(crs_list)
        else:
            # Single CRS
            compound = crses[0]

        return compound

    @staticmethod
    def is_longitude_axis(axis_label):
        """
        Check if axis label is longitude
        :return: boolean
        """
        return axis_label == CRSUtil.LONG_AXIS_LABEL_EPSG_0 \
                or axis_label == CRSUtil.LONG_AXIS_LABEL_EPSG_8_5

    @staticmethod
    def is_latitude_axis(axis_label):
        """
        Check if axis label is latitude
        :return: boolean
        """
        return axis_label == CRSUtil.LAT_AXIS_AXIS


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

        # NOTE: In case of compound CRS (e.g: Index1D&EPSG:4326) then Index1D is not X axis type anymore
        has_x_axis = False
        has_y_axis = False
        for axis in self.axes:
            if "Index" not in axis.uri:
                if axis.type == CRSAxis.AXIS_TYPE_X:
                    has_x_axis = True
                if axis.type == CRSAxis.AXIS_TYPE_Y:
                    has_y_axis = True

                if has_x_axis and has_y_axis:
                    break

        if has_x_axis and has_y_axis:
            for axis in self.axes:
                if "Index" in axis.uri:
                    axis.type = CRSAxis.AXIS_TYPE_UNKNOWN

    def __get_axis_uom_from_uom_crs(self, uom_crs):
        """
        Return axis UoM from an UoM CRS by last part of URL
        :param str uom_crs: URL to an UoM (e.g: http://www.opengis.net/def/uom/UCUM/0/d)
        """
        result = uom_crs.split("/")[-1]
        return result

    @staticmethod
    def __parse_axes_elements_from_single_crs(crs):
        """
        Parse the axes XML elements out of the CRS definition
        :param str crs: a complete CRS request  (e.g: http://localhost:8080/def/crs/EPSG/0/4326)
        """

        crs = replace_crs_by_working_crs(crs)
        try:
            # allow SECORE to return result in maximum 2 minutes
            timeout_in_seconds = 120

            gml = validate_and_read_url(crs, None, timeout_in_seconds)
            root = etree.fromstring(gml)

            xpath_str = ".//*[not(ancestor::gml:baseCRS) and contains(local-name(), 'CS')]"

            # e.g. ellipsoidalCS or CartesianCS elements
            # but they must be not nested inside <gml:baseCRS> (COSMO 101 CRS has this special case)
            elements = root.xpath(xpath_str,
                                  namespaces={"gml": "http://www.opengis.net/gml/3.2"})

            if len(elements) > 0:
                # as proper CRS axes definitions are at the bottom of CRS definition
                cselem = elements[len(elements) - 1]

                xml_axes = cselem.xpath(".//*[contains(local-name(), 'SystemAxis')]")
            else:
                # Not sure when it can happen
                raise RuntimeException("Cannot parse axes elements from CRS '" + crs
                                       + "' with xpath: " + xpath_str + ". Hint: the CRS may have invalid definition.")

            return xml_axes
        except Exception as ex:
            raise RuntimeException("Failed parsing the crs at: {}. "
                                   "Reason: {}".format(crs, str(ex)))

    @staticmethod
    def get_axis_labels_from_single_crs(crs):
        """
        Parse axis labels out of the CRS definition
        :param str crs: a complete CRS request  (e.g: http://localhost:8080/def/crs/EPSG/0/4326)
        """
        xml_axes = CRSUtil.__parse_axes_elements_from_single_crs(crs)
        axis_labels = []

        for xml_axis in xml_axes:
            axis_label = xml_axis.xpath(".//*[contains(local-name(), 'axisAbbrev')]")[0].text
            axis_labels.append(axis_label)

        return axis_labels

    @staticmethod
    def validate_crs(coverage_crs, geo_axis_crs):
        if geo_axis_crs not in coverage_crs:
            error_message = "File CRS '" + geo_axis_crs + "' does not match coverage CRS '" + coverage_crs + "'."
            raise RecipeValidationException(error_message)

    def _parse_single_crs(self, crs):
        """
        Parses the axes out of the CRS definition
        str crs: a complete CRS request (e.g: http://localhost:8080/def/crs/EPSG/0/4326)
        """
        try:
            xml_axes = self.__parse_axes_elements_from_single_crs(crs)
            axis_labels = []

            for xml_axis in xml_axes:
                axis_label = xml_axis.xpath(".//*[contains(local-name(), 'axisAbbrev')]")[0].text
                axis_type = CRSAxis.get_axis_type_by_name(axis_label)

                # e.g: http://localhost:8080/def/uom/EPSG/0/9122
                uom_url = xml_axis.attrib['uom']
                try:
                    # NOTE: as opengis.net will redirect to another web page for UoM description, so don't follow it
                    if CRSAxis.UOM_UCUM in uom_url:
                        axis_uom = self.__get_axis_uom_from_uom_crs(uom_url)
                    else:
                        uom_gml = validate_and_read_url(uom_url)
                        uom_root_element = etree.fromstring(uom_gml)

                        # e.g: degree (supplier to define representation)
                        uom_name = uom_root_element.xpath(".//*[contains(local-name(), 'name')]")[0].text
                        axis_uom = uom_name.split(" ")[0]
                except Exception:
                    # e.g: def/uom/OGC/1.0/GridSpacing does not exist -> GridSpacing
                    axis_uom = self.__get_axis_uom_from_uom_crs(uom_url)

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
                                   "Reason: {}".format(crs, str(ex)))

    def save_to_cache(self, crs, axes, individual_crs_axes):
        self.__CACHE__[crs] = OrderedDict()
        self.__CACHE__[crs]["axes"] = axes
        self.__CACHE__[crs]["individual_crs_axes"] = individual_crs_axes

    def get_from_cache(self, crs):
        if crs in self.__CACHE__:
            return self.__CACHE__[crs]
        return {"axes": [], "individual_crs_axes": OrderedDict()}

    __CACHE__ = {}


def replace_crs_by_working_crs(crs):
    """
    Replace the first part of crs in the ingredients file with the checked working crs when wcst_import starts
    e.g. in the ingredients it has: "time_crs": "http://localhost:8082/def/crs/OGC/0/AnsiDate"
    but the working SECORE CRS when wcst_import starts, is: "http://localhost:8080/rasdaman/def/crs/OGC/0/AnsiDate"
    """
    from session import Session
    result = Session.RUNNING_SECORE_URL + "/" + crs.split("/def/")[1]
    return result

