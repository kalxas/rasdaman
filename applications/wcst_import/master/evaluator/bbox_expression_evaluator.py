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
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
import os

from master.error.runtime_exception import RuntimeException
from master.evaluator.evaluator import ExpressionEvaluator
from master.evaluator.evaluator_slice import FileEvaluatorSlice
from session import Session
from util.file_util import FilePair
import re

class BBoxExpressionEvaluator(ExpressionEvaluator):
    BBOX_EXPRESSION = "bbox:.*:.*"
    regexp = re.compile(BBOX_EXPRESSION)

    MIN = "min"
    MAX = "max"

    SUPPORTED_VALUES = [MIN, MAX]

    def __init__(self):
        pass

    def can_evaluate(self, expression):
        """
        Decides if this is a grib expression
        :param str expression: the expression to test
        :rtype: bool
        """
        if expression.startswith("bbox:"):
            return True
        return False

    def evaluate(self, expression, evaluator_slice):
        """
        Evaluates a wcst import ingredient expression
        :param str expression: the expression to evaluate
        :param FileEvaluatorSlice evaluator_slice: the slice on which to evaluate the expression
        :rtype: str
        """
        if not self.regexp.search(expression):
            raise RuntimeException("bbox expression in the ingredients file is not valid: '" + expression + "';"
                                   " expected an expression matching the pattern '" + self.BBOX_EXPRESSION + "'")

        file = evaluator_slice.get_file()
        bbox_dict = Session.IMPORTED_FILE_AXIS_BBOX_DICT[file.filepath]

        # e.g: expression bbox:Lat:min
        parts = expression.split(":")
        axis_label = parts[1]
        value = parts[2]

        if axis_label not in bbox_dict:
            raise RuntimeException(
                "bbox expression in the ingredients file is not valid: '" + expression + "'"
                " as the axis label " + axis_label + " was not found in the bbox of the file: " + str(bbox_dict)
            )
        if value not in self.SUPPORTED_VALUES:
            raise RuntimeException(
                "bbox expression in the ingredients file is not valid: '" + expression + "' "
                "as the specified property " + value + " is not one of " + str(self.SUPPORTED_VALUES)
            )

        result = ""
        if value == self.MIN:
            result = str(bbox_dict[axis_label].min)
        elif value == self.MAX:
            result = str(bbox_dict[axis_label].max)

        return result

