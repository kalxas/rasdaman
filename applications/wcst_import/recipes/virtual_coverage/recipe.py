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
from master.error.validate_exception import RecipeValidationException
from master.extra_metadata.extra_metadata_serializers import ExtraMetadataSerializerFactory
from master.importer.coverage import Coverage
from master.importer.importer import Importer
from master.importer.resumer import Resumer
from master.recipe.base_recipe import BaseRecipe
from master.request.pyramid import AddPyramidMemberRequest, CreatePyramidMemberRequest
from session import Session
from util.string_util import replace_template_by_dict, create_downscaled_coverage_id
from util.file_util import TmpFile
from config_manager import ConfigManager
from util.xml_util import XMLUtil
from master.request.wcst import WCSTInsertRequest, WCSTUpdateRequest
from util.url_util import validate_and_read_url
import xml.etree.ElementTree as ET
import re
from master.importer.importer import Importer

# Valid characters for coverage ids
legal_characters = r'\w+$'
r = re.compile(legal_characters)


def is_regex(test_string):
    """
    Check if a coverage id contains regex characters (e.g: .,*,...)
    :param str test_string: coverage id string
    """
    return not re.match(r, test_string)


class Recipe(BaseRecipe):

    RECIPE_NAME = "virtual_coverage"

    __TEMPLATE_PATH = os.path.dirname(os.path.realpath(__file__)) + "/templates/"

    def __init__(self, session):
        """
        :param Session session: the session for this import
        """
        super(Recipe, self).__init__(session)
        self.session = session
        self.options = session.get_recipe()['options']
        self.validate()

        self.coverage_id = self.session.get_input()["coverage_id"]
        self.resumer = Resumer(self.coverage_id)
        self.source_coverage_ids = self.parse_source_coverage_ids(self.session.get_input()["source_coverage_ids"])
        self.envelope = self.options["envelope"]
        self.srs_name = XMLUtil.escape(self.envelope["srsName"])
        # array of axis
        self.axes = self.envelope["axis"]
        self.import_wms = self.options["wms_import"] if "wms_import" in self.options else None

    def __raise_exception(self, missing_option):
        """
        Raise a validation error when an required option is missing from ingredient files
        :param str missing_option:
        """
        raise RecipeValidationException("{} option is required".format(missing_option))

    def validate(self):
        """
        Validate if the the recipe's mandatory parameters are available
        """
        if "source_coverage_ids" not in self.session.get_input():
            self.__raise_exception("source_coverage_ids")
        elif "envelope" not in self.options:
            self.__raise_exception("envelope")
        elif "srsName" not in self.options["envelope"]:
            self.__raise_exception("srsName")
        elif "axis" not in self.options["envelope"]:
            self.__raise_exception("axis")

        self.validate_pyramid_members()
        self.validate_pyramid_bases()

    def describe(self):
        pass

    def status(self):
        pass

    def parse_source_coverage_ids(self, input_source_coverage_ids):
        """
        Source coverage ids is a list which contains full coverage id or coverage ids defined by regex
        :param list[string] input_source_coverage_ids: source of coverage ids in the ingredients file
        :return: list[string]: a list of concrete coverage ids without regex
        """
        gml = validate_and_read_url(ConfigManager.wcs_service + "?service=WCS&version=2.0.1&request=GetCapabilities")

        tree = ET.ElementTree(ET.fromstring(gml))
        namespaces = {'wcs': 'http://www.opengis.net/wcs/2.0'}
        elements = tree.findall(".//wcs:CoverageId", namespaces)

        # all available coverage ids which server provides
        current_coverage_ids = []

        for element in elements:
            current_coverage_ids.append(element.text)

        result_coverage_ids = []

        for coverage_id_pattern in input_source_coverage_ids:
            if is_regex(coverage_id_pattern):
                # coverage id by regex (e.g: test_*)
                for coverage_id in current_coverage_ids:
                    if re.match(coverage_id_pattern, coverage_id):
                        result_coverage_ids.append(coverage_id)
            else:
                # concrete coverage id (e.g: test_cov1)
                result_coverage_ids.append(coverage_id_pattern)

        return result_coverage_ids

    def __insert_coverage_request(self):
        """
        Create WCS-T InsertCoverage request to insert a virtual coverage
        """
        gml = self.__generate_gml()
        executor = ConfigManager.executor
        fu = TmpFile()
        tmp_file = fu.write_to_tmp_file(gml)

        if ConfigManager.mock:
            request = WCSTInsertRequest(tmp_file, False, None,
                                        None, None, None)
        else:
            request = WCSTInsertRequest(None, False, None,
                                        None, None, gml)

        executor.execute(request, mock=ConfigManager.mock)

    def __add_pyramid_members(self):
        """
        Add the listed pyramid members in the ingredient files to this base coverage
        """
        executor = ConfigManager.executor
        base_coverage_id = self.coverage_id
        current_pyramid_member_coverage_ids = Importer.list_pyramid_member_coverages(base_coverage_id, ConfigManager.mock)

        # add listed pyramid members coverage ids in the ingredients file to this base coverage's pyramid
        if self.session.pyramid_members is not None:
            for pyramid_member_coverage_id in self.session.pyramid_members:
                if pyramid_member_coverage_id not in current_pyramid_member_coverage_ids:
                    request = AddPyramidMemberRequest(self.coverage_id, pyramid_member_coverage_id, self.session.pyramid_harvesting)
                    executor.execute(request, mock=ConfigManager.mock, input_base_url=request.context_path)

    def __add_pyramid_bases(self):
        """
        Add current importing coverage id to the listed pyramid members of pyramid_bases the ingredient files
        """
        executor = ConfigManager.executor

        if self.session.pyramid_bases is not None:
            for base_coverage_id in self.session.pyramid_bases:

                base_pyramid_member_coverage_ids = Importer.list_pyramid_member_coverages(base_coverage_id,
                                                                                          ConfigManager.mock)

                if self.coverage_id not in base_pyramid_member_coverage_ids:
                    request = AddPyramidMemberRequest(base_coverage_id, self.coverage_id, self.session.pyramid_harvesting)
                    executor.execute(request, mock=ConfigManager.mock, input_base_url=request.context_path)

    def __create_pyramid_members(self):
        """
        Create a list of non-virtual pyramid members from this base virtual coverage member
        """
        executor = ConfigManager.executor

        scale_levels = self.options["scale_levels"] if "scale_levels" in self.options else None
        scale_factors = self.options["scale_factors"] if "scale_factors" in self.options else None

        # If scale_levels specified in ingredient files, send request to Petascope to create downscaled level coverages
        if scale_levels is not None:
            # Levels be ascending order
            sorted_scale_levels = sorted(scale_levels)
            coverage_crs = self.options["envelope"]["srsName"]
            # NOTE: each level is processed separately with each HTTP request
            for level in sorted_scale_levels:
                downscaled_level_coverage_id = create_downscaled_coverage_id(self.coverage_id, level)

                scale_factors = Importer.get_scale_factors(coverage_crs, level)
                request = CreatePyramidMemberRequest(self.coverage_id, downscaled_level_coverage_id, scale_factors)
                executor.execute(request, mock=ConfigManager.mock, input_base_url=request.context_path)
        elif scale_factors is not None:
            # if scale_factors exist in the ingredients file, then send CreatePyramidMember request
            # to create downscaled level coverages and add them as pyramid members of the base coverage
            for obj in scale_factors:
                downscaled_level_coverage_id = obj["coverage_id"]
                factors = obj["factors"]

                request = CreatePyramidMemberRequest(self.coverage_id, downscaled_level_coverage_id,
                                                     factors)
                executor.execute(request, mock=ConfigManager.mock, input_base_url=request.context_path)

    def __update_coverage_request(self):
        """
        Create WCS-T UpdateCoverage request to update a virtual coverage
        :return:
        """
        gml = self.__generate_gml()
        executor = ConfigManager.executor
        fu = TmpFile()
        tmp_file = fu.write_to_tmp_file(gml)

        if ConfigManager.mock:
            request = WCSTUpdateRequest(self.coverage_id, tmp_file, [], None, None)
        else:
            request = WCSTUpdateRequest(self.coverage_id, None, [], None, gml)

        executor.execute(request, mock=ConfigManager.mock)

    def __read_template_file(self, file_name):
        """
        Read template file content from templates folder
        :return: str
        """
        with open(self.__TEMPLATE_PATH + "/" + file_name + ".xml", 'r') as file:
            template = file.read().rstrip()

        return template

    def __build_metadata(self):
        """
        Build the GML for Metadata element
        """
        metadata_template = self.__read_template_file("metadata")

        metadata = ""

        if "coverage" in self.options and \
            "metadata" in self.options["coverage"] and \
             "global" in self.options["coverage"]["metadata"]:
            global_metadata_dict = self.options["coverage"]["metadata"]["global"]
            metadata_type = self.options["coverage"]["metadata"]["type"]

            serializer = ExtraMetadataSerializerFactory.get_serializer(metadata_type)
            result = serializer.serialize(global_metadata_dict)

            temp_dict = {
                "Metadata": result
            }
            metadata = replace_template_by_dict(metadata_template, temp_dict)

        return metadata

    def __build_envelope_gml(self):
        """
        Build GML for Envelope element
        :return: str
        """
        axis_extent_template = self.__read_template_file("axis_extent")

        axis_extents = ""
        for axis in self.axes:
            temp_dict = {
                "axisLabel": axis["axisLabel"],
                "resolution": axis["resolution"]
            }
            axis_extent = replace_template_by_dict(axis_extent_template, temp_dict)
            axis_extents += "\n" + axis_extent

        envelope_template = self.__read_template_file("envelope")
        temp_dict = {
            "srsName": self.srs_name,
            "AxisExtents": axis_extents
        }
        envelope = replace_template_by_dict(envelope_template, temp_dict)

        return envelope

    def __build_partition_set_gml(self):
        """
        Build GML for PartionSet elemen
        :return: str
        """
        partition_template = self.__read_template_file("partition")

        partitions = ""
        for source_coverage_id in self.source_coverage_ids:
            temp_dict = {
                "coverageRef": source_coverage_id
            }
            partition = replace_template_by_dict(partition_template, temp_dict)

            partitions += "\n" + partition

        partition_set_template = self.__read_template_file("partition_set")
        temp_dict = {
            "Partitions": partitions
        }
        partition_set = replace_template_by_dict(partition_set_template, temp_dict)

        return partition_set

    def __generate_gml(self):
        """
        Virtual coverage needs a particular GML format to import
        :return: str
        """
        gml_template = self.__read_template_file("gml_coverage")
        envelope = self.__build_envelope_gml()
        partion_set = self.__build_partition_set_gml()
        metadata = self.__build_metadata()

        temp_dict = {
            "coverage_id": self.coverage_id,
            "Envelope": envelope,
            "PartitionSet": partion_set,
            "Metadata": metadata
        }

        gml = replace_template_by_dict(gml_template, temp_dict)

        return gml

    def ingest(self):
        """
        Starts the ingesting process
        """
        coverage = Coverage(self.coverage_id, [], None, None, None)
        importer = Importer(self.resumer, coverage, self.import_wms, None, None)

        if importer._is_insert():
            self.__insert_coverage_request()
            self.__add_pyramid_members()
            self.__add_pyramid_bases()
            self.__create_pyramid_members()
        else:
            self.__update_coverage_request()

        if self.import_wms:
            importer._insert_update_into_wms()

        if self.session is not None:
            importer.update_coverage_inspire_metadata(self.coverage_id, self.session.inspire.metadata_url)


    @staticmethod
    def get_name():
        return Recipe.RECIPE_NAME

