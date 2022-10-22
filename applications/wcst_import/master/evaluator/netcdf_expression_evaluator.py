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
from util import list_util, import_util
from xml.sax.saxutils import escape
from decimal import Decimal

from master.error.runtime_exception import RuntimeException
from master.evaluator.evaluator import ExpressionEvaluator
from master.evaluator.evaluator_slice import NetcdfEvaluatorSlice

from util.log import log


def to_decimal(num):
    return Decimal(repr(num))

class NetcdfExpressionEvaluator(ExpressionEvaluator):

    PREFIX = "${"
    POSTFIX = "}"

    def __init__(self):
        pass

    def can_evaluate(self, expression):
        """
        Decides if this is a grib expression
        :param str expression: the expression to test
        :rtype: bool
        """
        if expression.startswith("netcdf:"):
            return True
        return False

    def evaluate(self, expression, evaluator_slice):
        """
        Evaluates a wcst import ingredient expression
        :param str expression: the expression to evaluate
        :param NetcdfEvaluatorSlice evaluator_slice: the slice on which to evaluate the expression
        :rtype: str
        """
        if not isinstance(evaluator_slice, NetcdfEvaluatorSlice):
            raise RuntimeException(
                "Cannot evaluate a netcdf expression on something that is not a netcdf valid file. Given expression: {}".format(
                    expression))
        # extract the axis and operation in the expression only
        # ${netcdf:variable:E:min} -> variable:E:min
        expression = expression.replace("netcdf:", "").replace(self.PREFIX, "").replace(self.POSTFIX, "")
        return self._resolve(expression, evaluator_slice.get_dataset())

    def __calculate_netcdf_resolution(self, coordinates):
        """
        Calculate the resolution for regular axis when "resolution" value is specified in the ingredient file as expression
        e.g: "resolution": "${netcdf:variable:lon:resolution}"
        GDAL and GRIB have their specified variables to get the resolution from file whilst netCDF does not have such method
        :param list coordinates: the list of coordinates of an axis
        :return: decimal: the resolution of this axis
        """
        first_point = to_decimal(coordinates[0])
        last_point = to_decimal(coordinates[-1])

        if first_point - last_point == 0:
            from master.helper.irregular_user_axis import IrregularUserAxis
            return IrregularUserAxis.DEFAULT_RESOLUTION

        return (last_point - first_point) / (len(coordinates) - 1)

    def _apply_operation(self, nc_dataset, nc_obj_name, operation):
        """
        Applies operation on a given variable which contains a list of values (e.g: lat = [0, 1, 2, 3,...]),
        (e.g: find the min of time variable ${netcdf:variable:time:min})
        :param netCDF4 nc_dataset: the netcdf dataset
        :param str nc_obj_name: name of netCDF variable or netCDF dimension
        :param str operation: the operation to apply:
        :return: str value: The value from the applied operation with precession
        """
        """ NOTE: min or max of list(variable) with values like [148654.08425925925,...]
        will return 148654.084259 in float which will cause problem with calculate coefficient as the first coeffcient should be 0
        but due to this change of min/max value, the coefficient is like 0.00000000001 (case: PML)
        "min": "${netcdf:variable:ansi:min} * 24 * 3600 - 11644560000.0", -> return: 1199152879.98
        "directPositions": "[float(x) * 24 * 3600 - 11644560000.0 for x in ${netcdf:variable:ansi}]", -> return 1199152880.0

        So we must use the values in the list by string and split it to a list to get the same values
        """
        MAX = "max"
        MIN = "min"
        LAST = "last"
        FIRST = "first"
        RESOLUTION = "resolution"
        METADATA = "metadata"

        # List of support operation on a netCDF variable
        supported_operations = [MAX, MIN, LAST, FIRST, RESOLUTION, METADATA]

        import_util.import_numpy()
        import numpy as np

        if nc_obj_name in nc_dataset.variables:
            nc_obj = nc_dataset.variables[nc_obj_name]

            if operation not in supported_operations:
                # it must be an attribute of variable
                return nc_obj.__getattribute__(operation)

            # It must be an operation that could be applied on a netCDF variable
            # convert list of string values to list of decimal values
            values = nc_obj[:].flatten()
        elif nc_obj_name in nc_dataset.dimensions:
            nc_obj = nc_dataset.dimensions[nc_obj_name]
            # Cannot determine list of values from variable but only dimension (e.g: station = 758)
            values = np.arange(0, nc_obj.size)
        else:
            raise Exception("Cannot find '" + nc_obj_name + "' from list of netCDF variables and dimensions.")

        if operation == MAX:
            return to_decimal(np.amax(values))
        elif operation == MIN:
            return to_decimal(np.amin(values))
        elif operation == LAST:
            last_index = len(nc_obj) - 1
            return to_decimal(values[last_index])
        elif operation == FIRST:
            return to_decimal(values[0])
        elif operation == RESOLUTION:
            # NOTE: only netCDF needs this expression to calculate resolution automatically
            # for GDAL: it uses: ${gdal:resolutionX} and GRIB: ${grib:jDirectionIncrementInDegrees} respectively
            return self.__calculate_netcdf_resolution(values)
        elif operation == METADATA:
            # return a dict of variable (axis) metadata with keys, values as string
            tmp_dict = {}
            for attr in nc_obj.ncattrs():
                try:
                    tmp_dict[attr] = escape(getattr(nc_obj, attr))
                except:
                    log.warn("Attribute '" + attr + "' of variable '" + nc_obj._getname() + "' cannot be parsed as string, ignored.")
            return tmp_dict

        # Not supported operation and not valid attribute of netCDF variable
        raise RuntimeException(
            "Invalid operation on netcdf variable: " + operation
            + ". Currently supported: " + ', '.join(supported_operations) + " or any metadata entry of the variable.")

    def _resolve(self, expression, nc_dataset):
        """
        Resolves the expression in the context of the netcdf dataset.
        :param str expression: the expression to resolve
        :param netCDF4.Dataset nc_dataset: the dataset for which to resolve
        :return:
        """
        if expression.startswith("variable:"):
            # Each variable can either be used as is or have an operation applied on it.
            # With operation, parse the operation and apply it to the variable
            # Without operation (returns a list like object)
            # e.g: expression: variable:E:min
            parts = expression.split(":")

            if len(parts) < 2:
                # e.g: variable is invalid
                raise RuntimeException("Invalid netcdf expression given: " + expression)
            nc_obj_name = parts[1]

            if len(parts) == 2:
                # return the entire variable translated to the string representation of a python list that can be
                # further passed to eval() which should only use list of strings to evaluate (not Decimal as eval has error)
                # e.g: variable:E
                array_str = list_util.numpy_array_to_string(nc_dataset.variables[nc_obj_name][:])
                return array_str
            else:
                # e.g: variable:E:min and operation is min
                operation = parts[2]

                return self._apply_operation(nc_dataset, nc_obj_name, operation)
        elif expression.startswith("metadata:"):
            meta_key = expression.replace("metadata:", "")
            try:
                return nc_dataset.__getattribute__(meta_key)
            except:
                # if not found we throw exception at the end of the method, no need to throw here as well
                pass
        elif expression.startswith("dimension:"):
            dim_key = expression.replace("dimension:", "")
            try:
                return len(nc_dataset.dimensions[dim_key])
            except:
                # if not found we throw exception at the end of the method, no need to throw here as well
                pass
        raise RuntimeException(
            "Cannot evaluate the given netcdf expression {} on the given container.".format(str(expression)))
