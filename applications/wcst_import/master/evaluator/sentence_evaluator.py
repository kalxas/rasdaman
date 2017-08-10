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
from __future__ import division

import re
import decimal

from master.error.runtime_exception import RuntimeException
from master.evaluator.evaluator_functions import evaluator_utils
from master.evaluator.expression_evaluator_factory import ExpressionEvaluatorFactory


class SentenceEvaluator:
    PREFIX = "${"
    POSTFIX = "}"

    def __init__(self, evaluator_factory):
        """
        The sentence evaluator evaluates arbitrary strings containing several wcst expressions inside a python expression
        in the context of a slice of the dataset
        :param ExpressionEvaluatorFactory evaluator_factory: the evaluator factory
        """
        self.evaluator_factory = evaluator_factory

    def evaluate(self, input_sentence, evaluator_slice, statements=[]):
        """
        Evaluates a sentence
        :param str input_sentence: the sentence to evaluate
        :param master.evaluator.evaluator_slice.EvaluatorSlice evaluator_slice: the slice that provides the context for
         the evaluator
        :param array statements: an array of statements to be executed before evaluation, e.g. import
        :rtype: str
        """
        sentence = str(input_sentence).strip()
        if sentence == "":
            return sentence

        expressions = re.findall("\${([\w_:]*)}", sentence)
        instantiated_sentence = sentence
        # Iterate the expression and evaluate all the possible variables
        # e.g: grib_datetime(${grib:dataDate}, ${grib:dataTime}),
        # then first replace grib:dataDate from dataDate in metadata and then grib:dataTime from dataTime in metadata
        for expression in expressions:
            evaluator = self.evaluator_factory.get_expression_evaluator(expression)
            expression_result = evaluator.evaluate(expression, evaluator_slice)
            instantiated_sentence = instantiated_sentence.replace(self.PREFIX + expression + self.POSTFIX,
                                                                  str(expression_result))

        try:
            # Check if expression can be decimal value (i.e: resolution 4.6666666666666666666) must be kept.
            # as eval() always return float value (e.g: 4.66666666667)
            result = decimal.Decimal(instantiated_sentence)
            return result
        except:
            # not decimal value, need to evaluate (e.g: [0, 1, 2, 3, 4] + 84200)
            return self.evaluate_python_expression(sentence, instantiated_sentence, evaluator_utils, statements)

    def evaluate_python_expression(self, sentence, instantiated_sentence, locals, statements=[]):
        """
        Evaluates the python expression according to the evaluator with the option of using a set of local functions and variables
        :param str sentence: the expression from ingredient file to evaluate (e.g: ${netcdf:variable:unix} + 5)
        :param str instantiated_sentence: (e.g: [0, 1, 2, 3,...] + 5 from the sentence)
        :param dict locals: the locals to provide in the context of the evaluation
        :param array statements: an array of statements to be executed before evaluation, e.g. import
        """
        try:
            for statement in statements:
                # allow overriding the provided datetime function
                if "datetime" in statement and "datetime" in locals:
                    locals.pop("datetime")
                exec(statement, globals())

            result = eval(instantiated_sentence, globals(), locals)
            return result
        except Exception as e:
            try:
                # NOTE: metadata evaluation expression in string must be enquoted with single quotes in ingredient file
                # e.g: "'Parameter id'" or "'${grib:centreDescription}'"
                # This is backward compatibility when single quotes were missed for bands's metadata
                # We evaluate the metadata which is assigned in locals scope, such as "\n or \' in metadata"
                locals['instantiated_sentence'] = instantiated_sentence
                return eval('instantiated_sentence', globals(), locals)
            except Exception as ex2:
                raise RuntimeException(
                    "The following expression could not be evaluated:\n"
                    "Provided Expression: {}\n"
                    "Instantiated Expression: {}".format(sentence, instantiated_sentence))
