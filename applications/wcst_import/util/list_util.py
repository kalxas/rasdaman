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
import decimal


def numpy_array_to_list_decimal(numpy_array):
    """
    Convert a numpy array to list of bigdecimal without losing precision.
    This is used for the cases of netcdf (or grib), e.g: ncdump shows the time axis with: 148654.084259259, 148656.99994213
    but, actually the list contains values in numpy array: 148654.08425925925, 148656.99994212962, 148658.08229166668
    and min/max (numpy_array) will return the value as ncdump which causes problem with coefficient in petascope
    (e.g: the first coefficient for irregular time axis is like 2.810E-7 not 0.0) and coverage will not be ingested.

    The reason is, for instance: "directPositions": "[float(x) * 24 * 3600 - 11644560000.0 for x in ${netcdf:variable:ansi}]"
    will return the whole numpy of array with precision so some operations to apply on input numpy_array like min(), max(),
    first(), last() *must* have same input list of decimal with same precision.

    :param array (numpy) numpy_array: array of values in numpy need to be convert to list of decimal without losing precision
    :rtype: list decimal
    """
    # NOTE: str(list(variable[:])) is *must* to keep all the precision of float values in numpy array
    output_list = list(map(decimal.Decimal, str(list(numpy_array))[1:-1].split(",")))

    return output_list


def numpy_array_to_string(numpy_array):
    """
    Convert a numpy array to string which represents the list of values with precision
    :param numpy_array: array of values in numpy
    :return: str: the representation string of numpy array
    """
    return str(list(numpy_array))


def to_list_string(input_list):
    """
    This is used to translate a list of decimal values to list string values as eval() cannot evaluate
    [Decimal(1), Decimal(2),...,Decimal(5)]
    :param input_list: list of decimal values
    :return: list string
    """
    output_list = [str(value) for value in input_list]
    return output_list


def sort_slices_by_datetime(slices, reverse=False):
    """
    Sort a list of slices (axes subsets from all analyzed files) by datetime
    e.g: we have a list of files: a_2005-01-01, b_2004-01-01, c_2003-01-01 and the datetime is extracted from file name
    Without the sort, the slices will be "2005-01-01", "2004-01-01", "2003-01-01" and cannot import in petascope
    as only can add the slice in top of a coverage for datetime axis.
    :param list input_list: list of slices
    :param bool reverse: sort list by reverse order
    :return: list of sorted slices by datetime axis
    """
    if len(slices) > 0:
        # Determine which axis contains datetime string
        time_axis_order = __get_time_axis_order(slices[0].axis_subsets)
        # Time axis exists in the list of axis subsets
        if time_axis_order != None:
            # sort the list by the date time axis order
            slices.sort(key=lambda slice : slice.axis_subsets[time_axis_order].interval.low, reverse=reverse)

    return slices


def __get_time_axis_order(axis_subsets):
    """
    Return the time axis order from the list of axis subsets
    :param list subsets: list of analyzed subsets of a slice
    :return: int axis_order: the order of time axis in the list of axis subsets
    """
    i = 0
    for axis_subset in axis_subsets:
        # time axis will be in datetime format, e.g "2001-01-01T01:00:00Z", interval could be decimal value.
        if '"' in str(axis_subset.interval.low):
            return i
        i += 1

    return None


def get_null_values(default_null_values):
    """
    Parse from 1 list of user-defined null values to individual null values
     (e.g: ["20", "30, 40, 50"] -> [20, 30, 40, 50])
    :param list[str] default_null_values: user defined null values in ingredient file
    """
    null_values = None
    if default_null_values is not None:
        null_values = []
        for value in default_null_values:
            values = str(value).strip().split(",")
            values = [x.strip() for x in values]
            null_values += values

    return null_values
