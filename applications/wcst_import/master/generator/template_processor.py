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

import os


class TemplateProcessor:
    def __init__(self):
        """
        The template processor transforms a model to a gml representation based on the template
        """

    def convert_to_gml(self, model):
        """
        Converts the given model to gml
        :param Model model: the model to fill the template with
        """
        template = open(self.__TEMPLATE_PATH + model.get_template_name()).read()
        for variable, value in model.__dict__.items():
            to_replace = self.__TEMPLATE_VARIABLE_WRAPPER.replace("Variable", str(variable))
            replacement = ""
            if value is None:
                replacement = ""
            elif isinstance(value, list):
                value = [str(x) for x in value]
                replacement = " ".join(value)
            else:
                replacement = str(value)

            template = template.replace(to_replace, replacement)
        return template

    __TEMPLATE_PATH = os.path.dirname(os.path.realpath(__file__)) + "/templates/"
    __TEMPLATE_VARIABLE_WRAPPER = "{{Variable}}"