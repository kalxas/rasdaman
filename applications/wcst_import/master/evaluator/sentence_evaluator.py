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

    def evaluate(self, sentence, evaluator_slice):
        """
        Evaluates a sentence
        :param str sentence: the sentence to evaluate
        :param master.evaluator.evaluator_slice.EvaluatorSlice evaluator_slice: the slice that provides the context for
         the evaluator
        :rtype: str
        """
        sentence = str(sentence)
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

        # check if expression can be array of coefficients or not
        if instantiated_sentence.startswith("[") and instantiated_sentence.endswith("]"):
            regex_matches = re.findall("([a-zA-Z])", instantiated_sentence)
            if len(regex_matches) == 0:
                # try to parse it manually
                values = instantiated_sentence.split('[', 1)[1].split(']')[0].replace("'", "")
                array = values.split(", ")
                return array
            else:
                # not array of values, need to evaluate
                return self.get_evaluate_python_expression(sentence, instantiated_sentence, evaluator_utils)
        else:
            try:
                # Check if expression can be decimal value or not
                result = decimal.Decimal(instantiated_sentence)
                return result
            except Exception:
                # not decimal value, need to evaluate
                return self.get_evaluate_python_expression(sentence, instantiated_sentence, evaluator_utils)


    def get_evaluate_python_expression(self, sentence, instantiated_sentence, evaluator_utils):
        """
        Just call the evaluate python expression below
        :param sentence: the input expression from ingredient file
        :param instantiated_sentence: the expression needed to be evaluated
        :param evaluator_utils: some local helper methods to evaluate
        :return:
        """
        try:
            return self.evaluate_python_expression(instantiated_sentence, evaluator_utils)
        except Exception:
            raise RuntimeException(
                "The following expression could not be evaluated correctly:\nProvided Expression: {}\nInstantiated Expression: {}".format(
                    sentence, instantiated_sentence))

    def evaluate_python_expression(self, expression, locals):
        """
        Evaluates the python expression according to the evaluator with the option of using a set of local functions and variables
        :param str expression: the expression to evaluate
        :param dict locals: the locals to provide in the context of the evaluation
        """
        return eval(expression, globals(), locals)
