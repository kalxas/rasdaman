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
import decimal


def to_list_decimal(input_list):
    """
    This is used most for the cases of netcdf (or grib)
    e.g: ncdump shows the time axis with:    148654.084259259,  148656.99994213,    148658.082291667,   148661.002060185
    but, actually the list contains values: 148654.08425925925, 148656.99994212962, 148658.08229166668, 148661.00206018519
    and min/max (list) will return the value as ncdump

    However, eval(list) will use the more precision values to evaluate, so to keep it consistently, we use the second values
    :param list input_list (normally is float): list of values need to be convert to more precision
    :rtype: list decimal
    """
    # NOTE: ncdump shows the short values of list(variables), e.g: 79.05 not 79.05000000000005
    # so we have to keep it consistent with eval() by using the latter values
    tmp = str(list(input_list))
    values = tmp.split('[', 1)[1].split(']')[0]
    output_list = values.split(", ")

    # convert all the string values to Decimal (e.g: '0.0000000000001' -> 0.0000000000001)
    output_list = map(decimal.Decimal, output_list)
    return output_list


def to_list_string(input_list):
    """
    This is used to translate a list of decimal values to list string values as eval() cannot evaluate
    [Decimal(1), Decimal(2),...,Decimal(5)]
    :param input_list: list of decimal values
    :return: list string
    """
    output_list = [str(value) for value in input_list]
    return output_list


def sort_slices_by_datetime(slices):
    """
    Sort a list of slices (axes subsets from all analyzed files) by datetime
    e.g: we have a list of files: a_2005-01-01, b_2004-01-01, c_2003-01-01 and the datetime is extracted from file name
    Without the sort, the slices will be "2005-01-01", "2004-01-01", "2003-01-01" and cannot import in petascope
    as only can add the slice in top of a coverage for datetime axis.
    :param list input_list: list of slices
    :return: list of sorted slices by datetime axis
    """
    # Determine which axis contains datetime string
    time_axis_order = __get_time_axis_order(slices[0].axis_subsets)
    # Time axis exists in the list of axis subsets
    if time_axis_order != None:
        # sort the list by the date time axis order
        slices.sort(key=lambda slice : slice.axis_subsets[time_axis_order].interval.low)

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
