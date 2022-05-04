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
from master.evaluator.bbox_expression_evaluator import BBoxExpressionEvaluator
from master.evaluator.file_expression_evaluator import FileExpressionEvaluator
from master.evaluator.gdal_expression_evaluator import GdalExpressionEvaluator
from master.evaluator.grib_expression_evaluator import GribExpressionEvaluator
from master.evaluator.netcdf_expression_evaluator import NetcdfExpressionEvaluator


class ExpressionEvaluatorFactory:
    def __init__(self):
        """
        An evaluator factory decides if
        """
        self.evaluators = [FileExpressionEvaluator(), GdalExpressionEvaluator(), GribExpressionEvaluator(),
                           NetcdfExpressionEvaluator(), BBoxExpressionEvaluator()]

    def get_expression_evaluator(self, expression):
        """
        Returns an expression evaluator that can evaluate this expression
        :param expression: the expression for which an evaluator should be provided
        :rtype: master.evaluator.evaluator.ExpressionEvaluator
        """
        for evaluator in self.evaluators:
            if evaluator.can_evaluate(expression):
                return evaluator
        raise RuntimeException(
            "No expression evaluator found for {}. Available evaluators exist for grib, file and gdal.".format(
                expression))
