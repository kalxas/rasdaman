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

import re

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
        expressions = re.findall("\${([a-zA-z0-9_:]*)}", sentence)
        instantiated_sentence = sentence
        for expression in expressions:
            evaluator = self.evaluator_factory.get_expression_evaluator(expression)
            expression_result = evaluator.evaluate(expression, evaluator_slice)
            instantiated_sentence = instantiated_sentence.replace(self.PREFIX + expression + self.POSTFIX,
                                                                  str(expression_result))

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
