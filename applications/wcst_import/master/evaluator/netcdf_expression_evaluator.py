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
from util import list_util

from master.error.runtime_exception import RuntimeException
from master.evaluator.evaluator import ExpressionEvaluator
from master.evaluator.evaluator_slice import NetcdfEvaluatorSlice


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

    def _apply_operation(self, variable, operation):
        """
        Applies operation on a given variable which contains a list of values (e.g: lat = [0, 1, 2, 3,...]),
        (e.g: find the min of time variable ${netcdf:variable:time:min})
        :param netCDF4.Variable variable: the netcdf variable
        :param str operation: the operation to apply
        :return: str value: The value from the applied operation with precession
        """
        """ NOTE: min or max of list(variable) with values like [148654.08425925925,...]
        will return 148654.084259 in float which will cause problem with calculate coefficient as the first coeffcient should be 0
        but due to this change of min/max value, the coefficient is like 0.00000000001 (case: PML)
        "min": "${netcdf:variable:ansi:min} * 24 * 3600 - 11644560000.0", -> return: 1199152879.98
        "directPositions": "[float(x) * 24 * 3600 - 11644560000.0 for x in ${netcdf:variable:ansi}]", -> return 1199152880.0

        So we must use the values in the list by string and split it to a list to get the same values
        """
        # convert list of string values list of decimal values
        array = list_util.to_list_decimal(list(variable))

        if operation == "max":
            return max(array)
        elif operation == "min":
            return min(array)
        elif operation == "last":
            last_index = len(variable) - 1
            return array[last_index]
        elif operation == "first":
            return array[0]
        else:
            try:
                return variable.__getattribute__(operation)
            except:
                # We will throw a runtime exception if this fails down the road
                pass
        raise RuntimeException(
            "Invalid operation on netcdf variable: " + operation
            + ". Currently supported: max, min, first, last or any metadata entry of the variable.")

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
            variable_name = parts[1]

            if len(parts) == 2:
                # return the entire variable translated to the string representation of a python list that can be
                # further passed to eval() which should only use list of strings to evaluate (not Decimal as eval has error)
                # e.g: variable:E
                array_str = str(list(nc_dataset.variables[variable_name][:]))
                return array_str
            else:
                # e.g: variable:E:min and operation is min
                operation = parts[2]
                return self._apply_operation(nc_dataset.variables[variable_name], operation)
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
