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
        expression = expression.replace("netcdf:", "").replace(self.PREFIX, "").replace(self.POSTFIX, "")
        return self._resolve(expression, evaluator_slice.get_dataset())

    def _apply_operation(self, variable, operation):
        """
        Applies operation on a given variable
        :param netCDF4.Variable variable: the netcdf variable
        :param str operation: the operation to apply
        :return:
        """
        if operation == "max":
            return max(variable)
        elif operation == "min":
            return min(variable)
        elif operation == "first":
            return variable[0]
        elif operation == "last":
            return variable[-1]
        else:
            try:
                return variable.__getattribute__(operation)
            except:
                # We will throw a runtime exception if this fails down the road
                pass
        raise RuntimeException(
            "Invalid operation on netcdf variable: " + operation + ". Currently supported: max, min, first, last or any metadata entry of the variable.")

    def _resolve(self, expression, nc_dataset):
        """
        Resolves the expression in the context of the netcdf dataset
        :param str expression: the expression to resolve
        :param netCDF4.Dataset nc_dataset: the dataset for which to resolve
        :return:
        """
        if expression.startswith("variable:"):
            parts = expression.split(":")
            if len(parts) < 3:
                raise RuntimeException("Invalid netcdf expression given: " + expression)
            variable_name = parts[1]
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
