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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
import numpy


class ResultArray(object):
    """
    A class represents the result (1D array) of collection query (e.g: select c + 1 from test_mr as c)
    which returns [value1, value2, value3,...] with value is binary data
    """

    def __init__(self, data, sdom, data_type, number_of_bands):
        """
        :param data: the binary string of rasql query
        :param sdom: the spatial domain of the result array
        :param data_type: the data type of result array (e.g: char, int, float,...)
        :param number_of_bands: the number of bands if result array has multiple bands
        """
        self.data = data
        self.sdom = sdom
        self.data_type = data_type
        self.number_of_bands = number_of_bands

    def __get_numpy_data_type(self):
        """
        From the rasdaman data type, return numpy data type
        https://www.tutorialspoint.com/numpy/numpy_data_types.htm
        :return: numpy data type
        """
        if self.data_type == "bool":
            return numpy.bool
        elif self.data_type == "char":
            return numpy.uint8
        elif self.data_type == "short":
            return numpy.int16
        elif self.data_type == "ushort":
            return numpy.uint16
        elif self.data_type == "long":
            return numpy.int32
        elif self.data_type == "ulong":
            return numpy.uint32
        elif self.data_type == "float":
            return numpy.float32
        elif self.data_type == "double":
            return numpy.double
        else:
            raise Exception("Unknown data type {} to convert to numpy data type ".format(self.data_type))

    def to_array(self):
        """
        Convert this 1D binary values to a nD numpy array by sdom
        :return: a numpy array object
        """
        # NOTE: numpy doesn't support bands in ndarray, so a 2D array with 3 bands is created as 3D array
        # e.g: sdom(2D MDD) = [0:400, 0:340] then 2D RGB array with 3 bands in ndarray is [0:2, 0:400, 0:340]
        shape_arr = []
        if self.number_of_bands > 1:
            shape_arr.append(self.number_of_bands)
        for sinterval in reversed(self.sdom.intervals):
            domain = sinterval.hi + 1 - sinterval.lo
            shape_arr.append(domain)

        numpy_data_type = self.__get_numpy_data_type()

        # NOTE: rasdaman output is column order, numpy array is row order by default, so needs to set it correctly
        # by swapped sdom and order='F'
        ndarr = numpy.ndarray(shape=tuple(shape_arr), buffer=numpy.array(self.data), dtype=numpy_data_type,
                              order='F')
        return ndarr
