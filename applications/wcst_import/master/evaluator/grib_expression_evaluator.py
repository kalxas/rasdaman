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

from master.error.runtime_exception import RuntimeException
from master.evaluator.evaluator import ExpressionEvaluator
from master.evaluator.evaluator_slice import GribMessageEvaluatorSlice


class GribExpressionEvaluator(ExpressionEvaluator):
    FORMAT_TYPE = "grib:"

    def __init__(self):
        pass

    def can_evaluate(self, expression):
        """
        Decides if this is a grib expression
        :param str expression: the expression to test
        :rtype: bool
        """
        if expression.startswith(self.FORMAT_TYPE):
            return True
        return False

    def evaluate(self, expression, evaluator_slice):
        """
        Evaluates a wcst import ingredient expression
        :param str expression: the expression to evaluate
        :param GribMessageEvaluatorSlice evaluator_slice: the slice on which to evaluate the expression
        :rtype: str
        """
        if not isinstance(evaluator_slice, GribMessageEvaluatorSlice):
            raise RuntimeException(
                "Cannot evaluate a grib expression on something that is not a grib message. Given expression: {}".format(
                    expression))

        # e.g: grib:dataDate, grib:axis:time
        expression = expression.replace(self.FORMAT_TYPE, "")
        try:
            # NOTE: as could not get all the aggregated values for axis in netcdf, grib needs to evaluate all messages
            # to get the list of evaluated values
            # Only when directPositions (grib:axis:label) are used then it can get the evaluated values from messages
            if ":" in expression:
                resolved_list = str(evaluator_slice.get_direct_positions())
                return resolved_list
            else:
                # here it uses the Grib Message from grib file to extract variable values
                # e.g: expression is: dataDate and value for this variable in message 1 is: 19700101
                resolved_variable = str(evaluator_slice.get_grib_message()[expression])
                return resolved_variable
        except Exception:
            raise RuntimeException("Could not find the given GRIB key: " + expression)
