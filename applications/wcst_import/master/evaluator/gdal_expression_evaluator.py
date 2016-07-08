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
from master.evaluator.evaluator_slice import GDALEvaluatorSlice
from master.helper.user_band import UserBand


class GdalExpressionEvaluator(ExpressionEvaluator):
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
        if expression.startswith("gdal:"):
            return True
        return False

    def evaluate(self, expression, evaluator_slice):
        """
        Evaluates a wcst import ingredient expression
        :param str expression: the expression to evaluate
        :param GDALEvaluatorSlice evaluator_slice: the slice on which to evaluate the expression
        :rtype: str
        """
        if not isinstance(evaluator_slice, GDALEvaluatorSlice):
            raise RuntimeException(
                "Cannot evaluate a gdal expression on something that is not a gdal valid file. Given expression: {}".format(
                    expression))
        expression = expression.replace("gdal:", "")
        return self._resolve(expression, evaluator_slice.get_dataset())

    def _resolve(self, expression, gdal_dataset):
        """
        Resolves the expression in the context of the gdal dataset
        :param str expression: the expression to resolve
        :param util.gdal_util.GDALGmlUtil gdal_dataset: the dataset for which to resolve
        :return:
        """
        if expression.startswith("metadata:"):
            value = gdal_dataset.get_metadata_tag(expression.replace("metadata:", ""))
        else:
            user_bands = []
            for band in gdal_dataset.get_fields_range_type():
                user_bands.append(UserBand(band.field_name, "", band.uom_code, "", band.nill_values))

            gdal_dictionary = {
                "resolutionX": gdal_dataset.get_offset_vectors()[0],
                "resolutionY": gdal_dataset.get_offset_vectors()[1],
                "originX": gdal_dataset.get_origin_x(),
                "originY": gdal_dataset.get_origin_y(),
                "minX": gdal_dataset.get_extents_x()[0],
                "maxX": gdal_dataset.get_extents_x()[1],
                "minY": gdal_dataset.get_extents_y()[0],
                "maxY": gdal_dataset.get_extents_y()[1],
                "bands": user_bands
            }
            if expression in gdal_dictionary:
                value = gdal_dictionary[expression]
            else:
                raise RuntimeException(
                    "Cannot evaluate the given grib expression {} on the given container.".format(str(expression)))
        return value
