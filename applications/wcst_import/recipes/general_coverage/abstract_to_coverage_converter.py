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
from master.evaluator.grib_expression_evaluator import GribExpressionEvaluator
from util.time_util import DateTimeUtil
from master.helper.user_axis import UserAxisType
from master.helper.irregular_user_axis import IrregularUserAxis
from master.helper.regular_user_axis import RegularUserAxis
from master.helper.user_axis import UserAxis
from util.crs_util import CRSUtil
import decimal


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
            if GribExpressionEvaluator.FORMAT_TYPE in user_axis.directPositions:
                # grib irregular axis will be calculated later when all the messages is evaluated
                direct_positions = user_axis.directPositions
            else:
                direct_positions = self.sentence_evaluator.evaluate(user_axis.directPositions, evaluator_slice)

            return IrregularUserAxis(user_axis.name, resolution, user_axis.order, min, direct_positions, max,
                                     user_axis.type, user_axis.dataBound)

    def _translate_number_direct_position_to_coefficients(self, origin, direct_positions):
        # just translate 1 -> 1 as origin is 0 (e.g: irregular Index1D)
        return map(lambda x: decimal.Decimal(str(x)) - decimal.Decimal(str(origin)), direct_positions)

    def _translate_seconds_date_direct_position_to_coefficients(self, origin, direct_positions):
        # just translate 1 -> 1 as origin is 0 (e.g: irregular UnixTime)
        return map(lambda x: (decimal.Decimal(str(arrow.get(x).float_timestamp)) - decimal.Decimal(str(origin))), direct_positions)

    def _translate_day_date_direct_position_to_coefficients(self, origin, direct_positions):
        # coefficients in AnsiDate (day) -> coefficients in UnixTime (seconds)
        coeff_list = []
        if direct_positions == [0]:
            return [0]
        else:
            for coeff in direct_positions:
                coeff_seconds = ((decimal.Decimal(str(arrow.get(coeff).float_timestamp)) - decimal.Decimal(str(origin)))
                                 / decimal.Decimal(DateTimeUtil.DAY_IN_SECONDS))
                coeff_list.append(coeff_seconds)

        return coeff_list

    def _translate_decimal_to_datetime(self, user_axis, geo_axis):
        """
        DateTime axis must be translated from seconds to ISO format
        :param User_Axis user_axis: the dateTime user axis which needs to be translated
        :param Regular/Irregular geo_axis: the dateTime axis which needs to be translated
        """
        if user_axis.type == UserAxisType.DATE:
            geo_axis.origin = DateTimeUtil.get_datetime_iso(geo_axis.origin)
            geo_axis.low = DateTimeUtil.get_datetime_iso(geo_axis.low)

            if geo_axis.high is not None:
                geo_axis.high = DateTimeUtil.get_datetime_iso(geo_axis.high)

            user_axis.interval.low = DateTimeUtil.get_datetime_iso(user_axis.interval.low)
            if user_axis.interval.high is not None:
                user_axis.interval.high = DateTimeUtil.get_datetime_iso(user_axis.interval.high)

    def _get_user_axis_by_crs_axis_name(self, crs_axis_name):
        """
        Returns the user axis corresponding to
        :param crs_axis_name: the name of the crs axis to retrieve
        :rtype: UserAxis
        """
        for user_axis in self.user_axes:
            if user_axis.name == crs_axis_name:
                return user_axis

    def _get_crs_axis_by_user_axis_name(self, user_axis_name):
        """
        Returns the crs axis from the list by user_axis_name
        :param user_axis_name:
        :return: crs_axis
        """
        crs_axes = CRSUtil(self.crs).get_axes()

        for i in range(0, len(crs_axes)):
            crs_axis = crs_axes[i]
            if crs_axis.label == user_axis_name:
                return crs_axis