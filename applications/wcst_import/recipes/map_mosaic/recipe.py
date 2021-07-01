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
from collections import OrderedDict

from master.evaluator.evaluator_slice_factory import EvaluatorSliceFactory
from master.evaluator.expression_evaluator_factory import ExpressionEvaluatorFactory
from master.evaluator.sentence_evaluator import SentenceEvaluator
from master.helper.gdal_axis_filler import GdalAxisFiller
from master.helper.gdal_range_fields_generator import GdalRangeFieldsGenerator
from master.helper.overview import Overview
from master.importer.coverage import Coverage
from master.importer.importer import Importer
from master.importer.multi_importer import MultiImporter
from master.importer.slice import Slice
from master.provider.data.file_data_provider import FileDataProvider
from master.recipe.base_recipe import BaseRecipe
from recipes.general_coverage.gdal_to_coverage_converter import GdalToCoverageConverter
from session import Session
from util.crs_util import CRSUtil
from util.gdal_util import GDALGmlUtil
from util.log import log
from util.gdal_validator import GDALValidator
from config_manager import ConfigManager
from util.file_util import FileUtil
from master.importer.resumer import Resumer
from recipes.general_coverage.recipe import Recipe as GeneralRecipe
from util.string_util import create_coverage_id_for_overview

from util.timer import Timer


class Recipe(BaseRecipe):
    def __init__(self, session):
        """
        The recipe class for map_mosaic. To get an overview of the ingredients needed for this
        recipe check ingredients/map_mosaic
        :param Session session: the session for this import
        """
        super(Recipe, self).__init__(session)
        self.options = session.get_recipe()['options'] if "options" in session.get_recipe() else {}
        self.importer = None
        self.resumer = Resumer(self.session.get_coverage_id())

        self.recipe_type = GdalToCoverageConverter.RECIPE_TYPE
        if "coverage" in self.options:
            self.options['coverage']['slicer'] = {}
            self.options['coverage']['slicer']['type'] = GdalToCoverageConverter.RECIPE_TYPE

    def validate(self):
        """
        Implementation of the base recipe validate method
        """
        super(Recipe, self).validate()

        if 'band_names' not in self.options:
            self.options['band_names'] = None

    def describe(self):
        """
        Implementation of the base recipe describe method
        """
        importer = self._get_importer()

        slices = importer.get_slices_for_description()
        number_of_files = len(slices)
        log.info("All files have been analyzed. Please verify that the axis subsets of the first {} files above are correct.".format(number_of_files))
        index = 1
        for slice in slices:
            log.info("Slice " + str(index) + ": " + str(slice))
            index += 1

    def ingest(self):
        """
        Starts the ingesting process
        """
        importer = self._get_importer()
        importer.ingest()

    def status(self):
        """
        Implementation of the status method
        :rtype (int, int)
        """
        return self._get_importer().get_progress()

    def _get_coverage_slices(self, crs, gdal_coverage_converter):
        """
        Returns the slices for the collection of files given
        """
        files = self.session.get_files()
        crs_axes = CRSUtil(crs).get_axes(self.session.coverage_id)

        slices_dict = self.create_dict_of_slices(self.session.import_overviews)

        count = 1

        for file in files:
            # NOTE: don't process any imported file from *.resume.json as it is just waisted time
            if not self.resumer.is_file_imported(file.filepath):
                timer = Timer()

                # print which file is analyzing
                FileUtil.print_feedback(count, len(files), file.filepath)
                if not FileUtil.validate_file_path(file.filepath):
                    continue

                valid_coverage_slice = True

                try:
                    gdal_file = GDALGmlUtil(file.get_filepath())
                    # array of AxisSubset
                    subsets = GdalAxisFiller(crs_axes, gdal_file).fill()
                except Exception as ex:
                    # If skip: true then just ignore this file from importing, else raise exception
                    FileUtil.ignore_coverage_slice_from_file_if_possible(file.get_filepath(), ex)
                    valid_coverage_slice = False

                if valid_coverage_slice:
                    # Generate local metadata string for current coverage slice
                    self.evaluator_slice = EvaluatorSliceFactory.get_evaluator_slice(self.recipe_type, file)
                    local_metadata = gdal_coverage_converter._generate_local_metadata(subsets, self.evaluator_slice)
                    slices_dict["base"].append(Slice(subsets, FileDataProvider(file), local_metadata))

                    # Then, create slices for selected overviews from user
                    for overview_index in self.session.import_overviews:
                        subsets_overview = self.create_subsets_for_overview(subsets, overview_index, gdal_file)

                        slices_dict[str(overview_index)].append(Slice(subsets_overview, FileDataProvider(file),
                                                                                    local_metadata))

                timer.print_elapsed_time()
                count += 1

        return slices_dict

    def _get_coverages(self):
        """
        Returns the list of coverages to be used for the importer
        """
        gdal_dataset = GDALGmlUtil.open_gdal_dataset_from_any_file(self.session.get_files())
        crs = gdal_dataset.get_crs()

        general_recipe = GeneralRecipe(self.session)
        global_metadata_fields = general_recipe._global_metadata_fields()
        local_metadata_fields = general_recipe._local_metadata_fields()

        sentence_evaluator = SentenceEvaluator(ExpressionEvaluatorFactory())

        gdal_coverage_converter = GdalToCoverageConverter(self.resumer, self.session.get_default_null_values(),
                                                          self.recipe_type, sentence_evaluator,
                                                          self.session.get_coverage_id(),
                                                          None, self.session.get_files(),
                                                          crs, None, None,
                                                          global_metadata_fields, local_metadata_fields,
                                                          None, None, general_recipe._metadata_type(),
                                                          None, None, self.session)

        coverage_slices_dict = self._get_coverage_slices(crs, gdal_coverage_converter)
        fields = GdalRangeFieldsGenerator(gdal_dataset, self.options['band_names']).get_range_fields()

        global_metadata = None
        if len(coverage_slices_dict["base"]) > 0:
            global_metadata = gdal_coverage_converter._generate_global_metadata(coverage_slices_dict["base"][0], self.evaluator_slice)

        results = []
        base_coverage_id = self.session.get_coverage_id()

        for key, value in coverage_slices_dict.items():
            if key == "base":
                # base coverage
                coverage = Coverage(base_coverage_id, coverage_slices_dict[key], fields, crs,
                                    gdal_dataset.get_band_gdal_type(), self.options['tiling'], global_metadata)
            else:
                # overview coverage (key = overview_index)
                coverage_id = create_coverage_id_for_overview(base_coverage_id, key)
                coverage = Coverage(coverage_id, coverage_slices_dict[key], fields,
                                    crs,
                                    gdal_dataset.get_band_gdal_type(), self.options['tiling'], global_metadata,
                                    base_coverage_id, key)

            results.append(coverage)

        return results

    def _get_importer(self):
        if self.importer is None:
            grid_coverage = False
            if 'coverage' in self.options:
                grid_coverage = self.options['coverage']['grid_coverage'] if 'grid_coverage' in self.options['coverage'] else False
            wms_import = self.options['wms_import'] if 'wms_import' in self.options else False
            scale_levels = self.options['scale_levels'] if self.options['scale_levels'] is not None else None

            coverages = self._get_coverages()
            importers = []

            for coverage in coverages:
                importers.append(Importer(self.resumer, coverage, wms_import, scale_levels, grid_coverage,
                                          self.session, self.options['scale_factors']))

            self.importer = MultiImporter(importers)

        return self.importer

    @staticmethod
    def get_name():
        return "map_mosaic"
