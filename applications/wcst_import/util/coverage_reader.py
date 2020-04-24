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
from lxml import etree

import itertools

from lxml.etree import XMLSyntaxError

from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException
from master.generator.model.range_type_field import RangeTypeField
from master.importer.axis_subset import AxisSubset
from master.importer.coverage import Coverage
from master.importer.slice import Slice
from master.provider.data.tuple_list_data_provider import TupleListDataProvider
from master.provider.data.url_data_provider import UrlDataProvider
from master.provider.metadata.coverage_axis import CoverageAxis
from master.importer.interval import Interval
from master.provider.metadata.grid_axis import GridAxis
from master.provider.metadata.irregular_axis import IrregularAxis
from master.provider.metadata.regular_axis import RegularAxis
from util.crs_util import CRSUtil, CRSAxis
from util.file_util import TmpFile
from util.gdal_util import GDALGmlUtil
from util.time_util import DateTimeUtil
from util.url_util import validate_and_read_url
from util.import_util import decode_res


class CoverageReader():
    time_util = DateTimeUtil("1600-12-31T00:00:00Z")
    def __init__(self, wcs_url, coverage_id, partitioning_scheme):
        """
        Reads a coverage from a wcs and builds an internal representation of it, including the sliced data according
        to a partitioning scheme. If not partitioning scheme is chosen, a default one will be used.
        :param str wcs_url: the url to the wcs service
        :param str coverage_id: the id of the coverage to be built
        :param list[int] partitioning_scheme: the partitioning scheme as a list of the maximum number of pixels on each
        axis dimension e.g. [500, 500, 1] will split the 3-D coverage in 2-D slices of 500 by 500. The partitioning
        scheme should always generate 2-D slices as they can be exported in a compressed format.
        """
        self.wcs_url = wcs_url
        self.coverage_id = coverage_id
        self.partitioning_scheme = partitioning_scheme
        self._read()

    @staticmethod
    def _get_ns():
        """
        Returns the namespaces for the coverage gml
        :rtype: dict
        """
        return {"gml": "http://www.opengis.net/gml/3.2", "gmlcov": "http://www.opengis.net/gmlcov/1.0",
                "swe": "http://www.opengis.net/swe/2.0", "wcs": "http://www.opengis.net/wcs/2.0",
                "gmlrgrid": "http://www.opengis.net/gml/3.3/rgrid"}

    def _get_crs(self, root):
        """
        Returns the crs for the coverage
        :param root: the xml root
        :rtype: str
        """
        crs = root.xpath("//gml:Envelope/@srsName", namespaces=self._get_ns())[0].strip()
        return crs

    def _get_range_types(self, root):
        """
        Returns the range types for the coverage
        :param root: the xml root
        :rtype: list[RangeTypeField]
        """
        range_types = [RangeTypeField(r) for r in root.xpath("//gmlcov:rangeType//swe:field/@name", namespaces=self._get_ns())]
        return range_types

    def _get_raster_coords(self, root):
        """
        Returns the raster coordinates as a list in the order of the grid axes (not necesarily the order of the geo axes)
        :param root: the xml root
        :rtype: list[Interval]
        """
        raster_low = root.xpath("//gml:GridEnvelope/gml:low", namespaces=self._get_ns())[0].text.strip().split(" ")
        raster_high = root.xpath("//gml:GridEnvelope/gml:high", namespaces=self._get_ns())[0].text.strip().split(" ")
        raster = []
        for index in range(0, len(raster_low)):
            raster.append(Interval(raster_low[index], raster_high[index]))
        return raster

    def _get_geo_coords(self, root):
        """
        Returns the raster coordinates as a list in the order of the geo axes of the crs
        :param root: the xml root
        :rtype: list[Interval]
        """
        geo_low = root.xpath("//gml:Envelope/gml:lowerCorner", namespaces=self._get_ns())[0].text.strip().split(" ")
        geo_high = root.xpath("//gml:Envelope/gml:upperCorner", namespaces=self._get_ns())[0].text.strip().split(" ")
        geo = []
        for index in range(0, len(geo_low)):
            geo.append(Interval(geo_low[index], geo_high[index]))
        return geo

    def _get_coverage_id(self, root):
        """
        Returns the coverage id of the coverage
        :param root: the xml root
        :rtype: str
        """
        coverage_id = root.xpath("//wcs:CoverageId", namespaces=self._get_ns())[0].text.strip()
        return coverage_id

    def _get_origin(self, root):
        """
        Returns the origin of the coverage
        :param root: the root of the xml
        :rtype: list[str]
        """
        origin_options = root.xpath("//*[contains(local-name(), 'origin')]//gml:pos", namespaces=self._get_ns())
        if origin_options is None or origin_options == []:
            origin = ['0'] * len(self._get_raster_coords(root))
        else:
            origin = origin_options[0].text.strip().split(" ")
        return origin

    def _get_resolutions(self, root, crs_axes):
        """
        Returns the resolution and if existing, the coefficient list for irregular axis, alongside a position value
        determining their relationship to the order of the crs axes
        :param root: the root of the xml
        :param list[CRSAxis] crs_axes: the crs axes of the coverage
        :return:
        """
        vectors_option = root.xpath("//*[contains(local-name(), 'offsetVector')]")
        if vectors_option is None or vectors_option == []:
            resolutions = []
            for i in range(0, len(crs_axes)):
                resolutions.append({
                    "resolution": 1,
                    "coefficient": None,
                    "position": i
                })
            return resolutions
        else:
            resolutions = [None] * len(vectors_option)
            position_in_grid = 0
            for vector_option in vectors_option:
                vector = vector_option.text.strip().split(" ")
                (position_in_crs, resolution) = self._find_index_of_nonzero_offset(vector)
                coefficient = self._get_coefficient(root, crs_axes[position_in_crs].label)
                resolutions[position_in_crs] = {
                    "resolution": resolution,
                    "coefficient": coefficient,
                    "position": position_in_grid
                }
                position_in_grid += 1
            return resolutions


    @staticmethod
    def _find_index_of_nonzero_offset(offset):
        """
        Finds the first nonzero offset value and returns the position and value
        :param offset: the offset for which to find the value
        :rtype: (int, str)
        """
        index = 0
        for value in offset:
            if value != '0':
                return index, value
            index += 1

    def _get_coefficient(self, root, crs_axis_name):
        """
        Returns a list of coefficients or None if the axis is regular
        :param root: the xml root of the gml document
        :param crs_axis_name: the name of the axis for which to find the coefficient
        :return:
        """
        coefficient_option = root.xpath(
            "//gmlrgrid:gridAxesSpanned[text() = '" + crs_axis_name + "']/../gmlrgrid:coefficients",
            namespaces=self._get_ns())
        if coefficient_option is not None and len(coefficient_option) != 0 and coefficient_option[0].text is not None:
            coefficient = coefficient_option[0].text.strip().split(" ")
        else:
            coefficient = None
        return coefficient

    def _get_coverage_axes(self, geo_coords, raster_coords, origin, crs_axes, resolutions):
        """
        Generates the coverage axes for this coverage
        :param list[Interval] geo_coords: the geographical coords in the order of the gml
        :param list[Interval] raster_coords: the grid coords in the order of the grid (not necessarily the order of the geo axes)
        :param list[str] origin: the origin of the coverage
        :param list[CRSAxis] crs_axes: a list of the crs axes
        :param list[dict] resolutions: a list of triples containing the resolution, coefficient list and position in grid
        :rtype: list[CoverageAxis]
        """

        axis_index = 0
        coverage_axes = []
        for crs_axis in crs_axes:
            resolution = resolutions[axis_index]
            order = resolution['position']
            grid_axis = GridAxis(order, crs_axis.label, resolution['resolution'], raster_coords[order].low,
                                 raster_coords[order].high)
            if resolution['coefficient'] is not None:
                geo_axis = IrregularAxis(crs_axis.label, crs_axis.uom,
                                         geo_coords[axis_index].low, geo_coords[axis_index].high,
                                         origin[axis_index], resolution['coefficient'], crs_axis)
            else:
                geo_axis = RegularAxis(crs_axis.label, crs_axis.uom,
                                       geo_coords[axis_index].low, geo_coords[axis_index].high,
                                       origin[axis_index], crs_axis)
            data_bound = crs_axis.is_x_axis() or crs_axis.is_y_axis()
            coverage_axis = CoverageAxis(geo_axis, grid_axis, data_bound)
            coverage_axes.append(coverage_axis)
            axis_index += 1
        return coverage_axes

    def _get_coverage_axis_by_grid_index(self, coverage_axes, grid_index):
        """
        Returns the coverage axis with the corresponding grid index
        :param list[CoverageAxis] coverage_axes: the list of axes
        :param int grid_index: the grid index
        :rtype: CoverageAxis
        """
        for coverage_axis in coverage_axes:
            if coverage_axis.grid_axis.order == grid_index:
                return coverage_axis
        return None

    def _get_intervals(self, coverage_axes, partition_scheme):
        """
        Returns the slices
        :param list[CoverageAxis] coverage_axes: a list of coverage axes
        :param list[int] partition_scheme: a list of the number of pixels to be included on each dimension
        :rtype: list[list[Interval]]
        """
        intervals = []
        for index in range(0, len(coverage_axes)):
            axis_intervals = []
            if coverage_axes[index].axis.coefficient is not None:
                # Axis is irregular compute it using its coefficient list
                pop(partition_scheme)
                origin = coverage_axes[index].axis.origin
                # if axis is time axis then need to convert coeffcient from datetime to float
                if ("\"" in origin):
                    origin_date = self.time_util.get_time_crs_origin(coverage_axes[index].axis.crs_axis.uri)
                    # uom here is a URI (e.g: http://www.opengis.net/def/uom/UCUM/0/d ) so need to extract the unit only (d)
                    time_uom = coverage_axes[index].axis.crs_axis.uom.rsplit('/', 1)[-1]
                    origin = self.time_util.count_offset_dates(origin_date, origin, time_uom)

                origin = float(origin)
                resolution = float(coverage_axes[index].grid_axis.resolution)
                for coefficient in coverage_axes[index].axis.coefficient:
                    # if axis is time axis then need to convert coeffcient from datetime to float
                    if ("\"" in coefficient):
                        coefficient = float(self.time_util.count_offset_dates(origin_date, coefficient, time_uom))
                    value = origin + resolution * float(coefficient - origin)
                    axis_intervals.append(Interval(value))
            else:
                # Regular axis, compute it by stepping through the spatial domain
                if coverage_axes[index].axis.crs_axis.is_x_axis() or coverage_axes[index].axis.crs_axis.is_y_axis():
                    # For x and y axes we can split them according to the user's partitioning
                    resolution = float(coverage_axes[index].grid_axis.resolution)
                    error_correction = (resolution / 2) if ConfigManager.subset_correction else 0
                    geo_pixels_per_slice = float(pop(partition_scheme)) * resolution
                    stop = float(coverage_axes[index].axis.high) if resolution > 0 else float(
                        coverage_axes[index].axis.low)
                    low = float(coverage_axes[index].axis.low) if resolution > 0 else float(
                        coverage_axes[index].axis.high)
                    high = low + geo_pixels_per_slice
                    if (resolution > 0 and high >= stop) or (resolution < 0 and stop >= high):
                        high = stop
                    while (resolution > 0 and high <= stop) or (resolution < 0 and stop <= high):
                        if low < high:
                            axis_intervals.append(Interval(low, high))
                        else:
                            axis_intervals.append(Interval(high, low))
                        # To make sure there is no grid pixel slipping through the cracks due to the decimal computations
                        # start the next slice with one geo pixel before the last one ended.
                        # Error correction is disabled by default, the user can enable it
                        low = high - error_correction
                        high = low + geo_pixels_per_slice

                    # if the interval is not exactly divided by the number of geo pixels per slice, compute the last slice
                    if ((resolution > 0) and ((low + error_correction) < stop)) or \
                            ((resolution < 0) and (stop < (low + error_correction))):
                        axis_intervals.append(Interval(low, stop))
                    index += 1
                else:
                    # Not an x, y axis and we are exporting as geotiff, so we cannot honor the user's choice of
                    # partitioning, we have to step exactly one geo pixel each time
                    pop(partition_scheme)
                    resolution = float(coverage_axes[index].grid_axis.resolution)
                    low = coverage_axes[index].axis.low
                    high = coverage_axes[index].axis.high
                    # if low and high are DateTime then need to calculate it to numeric values from origin of time crs
                    if ("\"" in low):
                        origin_date = self.time_util.get_time_crs_origin(coverage_axes[index].axis.crs_axis.uri)
                        # uom here is a URI (e.g: http://www.opengis.net/def/uom/UCUM/0/d ) so need to extract the unit only (d)
                        time_uom = coverage_axes[index].axis.crs_axis.uom.rsplit('/', 1)[-1]
                        low = self.time_util.count_offset_dates(origin_date, low, time_uom)
                        high = self.time_util.count_offset_dates(origin_date, high, time_uom)

                    low = float(low) if resolution > 0 else float(high)
                    stop = float(high) if resolution > 0 else float(low)
                    while (resolution > 0 and low <= stop) or (resolution < 0 and stop <= low):
                        axis_intervals.append(Interval(low))
                        low += resolution

            intervals.append(axis_intervals)
        return itertools.product(*intervals)

    def _get_description_url(self):
        """
        Returns the url to the coverage description
        :rtype: str
        """
        return self.wcs_url + "?service=WCS&version=2.0.1&request=DescribeCoverage&coverageId=" + self.coverage_id

    def _get_coverage_url(self, axis_subsets):
        """
        Returns a get coverage request for the given coverage with the given subsets
        :param list[AxisSubset] axis_subsets: a list of axis subsets
        :rtype: str
        """
        subsets = []
        format = "&format=image/tiff&" if len(axis_subsets) > 1 else "&"
        for axis_subset in axis_subsets:
            error_correction = (float(
                axis_subset.coverage_axis.grid_axis.resolution) / 2) if ConfigManager.subset_correction else 0
            high = axis_subset.interval.high - error_correction if axis_subset.interval.high is not None else None
            new_interval = Interval(axis_subset.interval.low + error_correction, high)
            subsets.append("subset=" + axis_subset.coverage_axis.axis.label + "(" + str(new_interval) + ")")
        return self.wcs_url + "?service=WCS&version=2.0.1&request=GetCoverage&coverageId=" + \
               self.coverage_id + format + "&".join(subsets)

    def _get_coverage_data_as_array(self, data_url):
        xmlstr = validate_and_read_url(data_url)
        root = etree.fromstring(xmlstr)
        tupleList = root.xpath("//gml:tupleList", namespaces=self._get_ns())
        return tupleList[0].text.split(",")

    def _get_slices(self, coverage_axes, intervals):
        """
        Returns the slices
        :param list[CoverageAxis] coverage_axes: the coverage axes
        :param list[list[Interval]] intervals: all the possible intervals defining the coverage space
        :return:
        """
        slices = []
        for interval_list in intervals:
            subsets = []
            for index in range(0, len(coverage_axes)):
                subsets.append(AxisSubset(coverage_axes[index], interval_list[index]))
            if len(coverage_axes) == 1:
                # For 1D we have to parse the gml and create a tuple data provider
                data_provider = TupleListDataProvider(self._get_coverage_data_as_array(self._get_coverage_url(subsets)))
            else:
                data_provider = UrlDataProvider(self._get_coverage_url(subsets))
            slices.append(Slice(subsets, data_provider))
        return slices

    def _get_data_type(self, slice):
        """
        Returns the data type of the slice by downloading the slice and trying to guess it with GDAL
        :param Slice slice: slice
        :rtype: str
        """
        if not ConfigManager.mock and isinstance(slice.data_provider, UrlDataProvider):
            # Do this only for coverages that have more than one axis
            if len(slice.axis_subsets) > 1:
                contents = decode_res(validate_and_read_url(slice.data_provider.get_url()))
                file_path = TmpFile().write_to_tmp_file(contents, "tif")
                return GDALGmlUtil(file_path).get_band_gdal_type()
        return None

    def description(self):
        """
        Gets the description from coverage_id in wcs_url
        :rtype: String
        """
        xmlstr = validate_and_read_url(self._get_description_url())
        # Check if coverage id does not exist in wcs_endpoint by returning an Exception
        if decode_res(xmlstr).find("ExceptionReport") != -1:
            raise RuntimeException("Could not read the coverage description for coverage id: {} with url: {} ".format(self.coverage_id, self.wcs_url))
        # If coverage id does exist then return its description
        return xmlstr

    def _read(self):
        """
        Reads the metadata from the describe coverage and creates the virtual coverage object
        :rtype: Coverage
        """
        try:
            xmlstr = self.description()
            root = etree.fromstring(xmlstr)
            crs = self._get_crs(root)
            crs_axes = CRSUtil(crs).get_axes(self.coverage_id)
            range_type = self._get_range_types(root)
            raster_coords = self._get_raster_coords(root)
            geo_coords = self._get_geo_coords(root)
            coverage_id = self._get_coverage_id(root)
            resolutions = self._get_resolutions(root, crs_axes)
            origin = self._get_origin(root)
            coverage_axes = self._get_coverage_axes(geo_coords, raster_coords, origin, crs_axes, resolutions)
            intervals = self._get_intervals(coverage_axes, self.partitioning_scheme)
            slices = self._get_slices(coverage_axes, intervals)
            pixel_data_type = self._get_data_type(slices[0])
            coverage = Coverage(coverage_id, slices, range_type, crs, pixel_data_type)
            self.coverage = coverage
        except IOError as e:
            raise RuntimeException(
                "Could not read the coverage description for url: " + self._get_description_url() +
                ". Check that the url is accessible and try again. More details: " + str(e))
        except XMLSyntaxError as e:
            raise RuntimeException("Could not decode the xml description for url " + self._get_description_url() +
                                   ". Check that the url is correct and try again. More details: " + str(e))

    def get_coverage(self):
        """
        Returns the virtual coverage object
        :rtype: Coverage
        """
        return self.coverage


def pop(l):
    """
    Pops the first element of l if l has any
    :param l: the list to pop from
    """
    if len(l) != 0:
        return l.pop(0)
