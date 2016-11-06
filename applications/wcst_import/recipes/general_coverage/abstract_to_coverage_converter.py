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
from lib import arrow
from master.helper.irregular_user_axis import IrregularUserAxis
from master.helper.regular_user_axis import RegularUserAxis
from master.helper.user_axis import UserAxis

class AbstractToCoverageConverter:
    def __init__(self, sentence_evaluator):
        """
        Abstract class capturing common functionality between coverage converters.
        :param SentenceEvaluator sentence_evaluator: the evaluator for wcst sentences
        """
        self.sentence_evaluator = sentence_evaluator

    def _user_axis(self, user_axis, evaluator_slice):
        """
        Returns an evaluated user axis from a user supplied axis
        The user supplied axis contains for each attribute an expression that can be evaluated.
         We need to return a user axis that contains the actual values derived from the expression evaluation
        :param UserAxis | IrregularUserAxis user_axis: the user axis to evaluate
        :param evaluator_slice: the sentence evaluator for the slice
        :rtype: UserAxis | IrregularUserAxis
        """
        min = self.sentence_evaluator.evaluate(user_axis.interval.low, evaluator_slice)
        max = None
        if user_axis.interval.high:
            max = self.sentence_evaluator.evaluate(user_axis.interval.high, evaluator_slice)
        resolution = self.sentence_evaluator.evaluate(user_axis.resolution, evaluator_slice)
        if isinstance(user_axis, RegularUserAxis):
            return RegularUserAxis(user_axis.name, resolution, user_axis.order, min, max, user_axis.type,
                                   user_axis.dataBound)
        else:
            direct_positions = self.sentence_evaluator.evaluate(user_axis.directPositions, evaluator_slice)
            return IrregularUserAxis(user_axis.name, resolution, user_axis.order, min, direct_positions, max,
                                     user_axis.type, user_axis.dataBound)

    def _translate_number_direct_position_to_coefficients(self, origin, direct_positions):
        return map(lambda x: float((x - origin)), direct_positions)

    def _translate_seconds_date_direct_position_to_coefficients(self, origin, direct_positions):
        return map(lambda x: (arrow.get(x).float_timestamp - origin), direct_positions)

    def _translate_day_date_direct_position_to_coefficients(self, origin, direct_positions):
        return map(lambda x: (arrow.get(x).float_timestamp - origin) / float(24*3600), direct_positions)
