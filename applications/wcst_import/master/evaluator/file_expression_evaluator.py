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
import os

from master.error.runtime_exception import RuntimeException
from master.evaluator.evaluator import ExpressionEvaluator
from master.evaluator.evaluator_slice import FileEvaluatorSlice
from util.file_util import FilePair


class FileExpressionEvaluator(ExpressionEvaluator):
    PREFIX = "${"
    POSTFIX = "}"

    FILE_PATH_EXPRESSION = PREFIX + "file:path" + POSTFIX

    def __init__(self):
        pass

    def can_evaluate(self, expression):
        """
        Decides if this is a grib expression
        :param str expression: the expression to test
        :rtype: bool
        """
        if expression.startswith("file:"):
            return True
        return False

    def evaluate(self, expression, evaluator_slice):
        """
        Evaluates a wcst import ingredient expression
        :param str expression: the expression to evaluate
        :param FileEvaluatorSlice evaluator_slice: the slice on which to evaluate the expression
        :rtype: str
        """
        if not isinstance(evaluator_slice, FileEvaluatorSlice):
            raise RuntimeException(
                "Cannot evaluate a file expression on something that is not a valid file. Given expression: {}".format(
                    expression))
        expression = expression.replace("file:", "")
        return self._resolve(expression, evaluator_slice.get_file())

    def _resolve(self, expression, file):
        """
        Resolves the expression in the context of a file container
        :param str expression: the expression to resolve
        :param File file: the file for which to resolve the expression
        :return:
        """
        file_dictionary = {
            "path": file.get_filepath(),
            "name": os.path.basename(file.get_filepath()),
            "dir_path": os.path.dirname(file.get_filepath())
        }

        if isinstance(file, FilePair):
            # # In case input file path replaced by pre hook's replace_path
            file_dictionary["original_path"] = file.get_original_file_path()
            file_dictionary["original_dir_path"] = os.path.dirname(file.get_original_file_path())
        else:
            file_dictionary["original_path"] = file.get_filepath()
            file_dictionary["original_dir_path"] = os.path.dirname(file.get_filepath())

        if expression in file_dictionary:
            value = file_dictionary[expression]
        else:
            raise RuntimeException(
                "Cannot evaluate the given file expression {} on the given container.".format(str(expression)))
        return value
