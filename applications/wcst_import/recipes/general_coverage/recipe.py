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
from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException
from master.error.validate_exception import RecipeValidationException
from master.evaluator.expression_evaluator_factory import ExpressionEvaluatorFactory
from master.evaluator.sentence_evaluator import SentenceEvaluator
from master.extra_metadata.extra_metadata_serializers import ExtraMetadataSerializerFactory
from master.helper.irregular_user_axis import IrregularUserAxis
from master.helper.regular_user_axis import RegularUserAxis
from master.helper.user_axis import UserAxis, UserAxisType
from master.helper.user_band import UserBand
from master.importer.importer import Importer
from master.recipe.base_recipe import BaseRecipe
from recipes.general_coverage.gdal_to_coverage_converter import GdalToCoverageConverter
from recipes.general_coverage.grib_to_coverage_converter import GRIBToCoverageConverter
from recipes.general_coverage.netcdf_to_coverage_converter import NetcdfToCoverageConverter
from recipes.general_coverage.pp_netcdf_to_coverage_converter import PointPixelNetcdfToCoverageConverter
from recipes.general_coverage.pp_grib_to_coverage_converter import PointPixelGRIBToCoverageConverter
from session import Session
from util.crs_util import CRSUtil
from util.gdal_validator import GDALValidator
from util.log import log


class Recipe(BaseRecipe):
    GRIB_TYPE = 'grib'
    GDAL_TYPE = 'gdal'
    NETCDF_TYPE = "netcdf"

    def __init__(self, session):
        """
        The recipe class for map_mosaic. To get an overview of the ingredients needed for this
        recipe check ingredients/map_mosaic
        :param Session session: the session for this import
        """
        super(Recipe, self).__init__(session)
        self.options = session.get_recipe()['options']
        self.importer = None

        validator = GDALValidator(self.session.files)
        if ConfigManager.skip:
            self.session.files = validator.get_valid_files()

    def validate(self):
        """
        Implementation of the base recipe validate method
        """
        super(Recipe, self).validate()

        if 'wms_import' not in self.options:
            self.options['wms_import'] = False
        else:
            self.options['wms_import'] = bool(self.options['wms_import'])

        if 'tiling' not in self.options:
            self.options['tiling'] = None

        if 'coverage' not in self.options:
            raise RecipeValidationException("No coverage parameter supplied in the recipe parameters.")
        else:
            # NOTE: only general coverage support this grid coverage type
            if 'grid_coverage' not in self.options['coverage']:
                self.options['coverage']['grid_coverage'] = False
            else:
                self.options['coverage']['grid_coverage'] = bool(self.options['coverage']['grid_coverage'])

        if 'crs' not in self.options['coverage']:
            raise RecipeValidationException("No crs parameter in the coverage parameter of the recipe parameters.")
        if 'slicer' not in self.options['coverage']:
            raise RecipeValidationException("No slicer parameter in the coverage parameter of the recipe parameters")
        if 'type' not in self.options['coverage']['slicer']:
            raise RecipeValidationException("No type parameter in the slicer parameter of the recipe parameters")
        if 'bands' not in self.options['coverage']['slicer'] and self.options['coverage']['slicer'][
            'type'] == self.GRIB_TYPE:
            raise RecipeValidationException(
                "The grib slicer requires the existence of a band parameter inside the slicer parameter.")
        if 'axes' not in self.options['coverage']['slicer']:
            raise RecipeValidationException("No axes parameter in the slicer parameter of the recipe parameters")
        for name, axis in self.options['coverage']['slicer']['axes'].items():
            if "min" not in axis:
                raise RecipeValidationException("No min value given for axis " + name)
            if "type" in axis and axis["type"] == "ansidate":
                """backwards compatibility, support axis type 'ansidate' after moving to 'date'"""
                axis["type"] = UserAxisType.DATE
            if "type" in axis and not UserAxisType.valid_type(axis["type"]):
                raise RecipeValidationException("Invalid axis type \"" + axis[
                    "type"] + "\" for axis " + name + ". Only \"" + UserAxisType.DATE + "\" and \"" + UserAxisType.NUMBER + "\" are supported.")
            if "resolution" not in axis and "irregular" in axis and not axis["irregular"]:
                raise RecipeValidationException("No resolution value given for regular axis " + name)
            if "directPositions" not in axis and "irregular" in axis and axis["irregular"]:
                log.warning("No direct positions found for irregular axis, assuming slice.")
                axis["directPositions"] = "[0]"

        if "metadata" in self.options['coverage'] and "type" not in self.options['coverage']['metadata']:
            raise RecipeValidationException("No type given for the metadata parameter.")

        if "metadata" in self.options['coverage'] and "type" in self.options['coverage']['metadata']:
            if not ExtraMetadataSerializerFactory.is_encoding_type_valid(self.options['coverage']['metadata']['type']):
                raise RecipeValidationException(
                    "No valid type given for the metadata parameter, accepted values are xml and json")

        if "metadata" in self.options['coverage']:
            if ("global" not in self.options['coverage']['metadata']) and ("local" not in self.options['coverage'][
                'metadata']):
                raise RecipeValidationException("No local or global metadata fields given for the metadata parameter.")

    def describe(self):
        """
        Implementation of the base recipe describe method
        """
        super(Recipe, self).describe()
        log.info("\033[1mWMS Import:\x1b[0m " + str(self.options['wms_import']))
        importer = self._get_importer()
        log.info("A couple of files have been analyzed. Check that the axis subsets are correct.")
        index = 1
        for slice in importer.get_slices_for_description():
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

    def _read_or_empty_string(self, js, param):
        """
        Returns the value in a json object if it exists or empty string otherwise
        :param dict js: the parsed json object
        :param str param: the param to look for
        :rtype: str
        """
        if param in js:
            return str(js[param])
        return ""

    def _read_bands(self):
        """
        Returns a user band extracted from the ingredients
        :rtype: list[UserBand]
        """
        if "bands" in self.options['coverage']['slicer']:
            bands = self.options['coverage']['slicer']['bands']
            ret_bands = []
            for band in bands:
                ret_bands.append(UserBand(self._read_or_empty_string(band, "name"),
                                          self._read_or_empty_string(band, "definition"),
                                          self._read_or_empty_string(band, "description"),
                                          self._read_or_empty_string(band, "nilReason"),
                                          self._read_or_empty_string(band, "nilValue").split(","),
                                          self._read_or_empty_string(band, "uomCode"),
                                          self._read_or_empty_string(band, "identifier")))
            return ret_bands
        raise RuntimeError("Bands parameter was not checked for validity")

    def _read_axes(self, crs):
        """
        Returns a list of user axes extracted from the ingredients file
        :param str crs: the crs of the coverage
        :rtype: list[UserAxis]
        """
        axes = self.options['coverage']['slicer']['axes']
        user_axes = []
        crs_axes = CRSUtil(crs).get_axes()
        default_order = 0
        for crs_axis in crs_axes:
            if crs_axis.label not in axes:
                raise RecipeValidationException(
                    "Could not find a definition for axis " + crs_axis.label + " in the axes parameter.")
            axis = axes[crs_axis.label]
            max = axis["max"] if "max" in axis else None
            if "type" in axis:
                type = axis["type"]
            elif crs_axis.is_date():
                type = UserAxisType.DATE
            else:
                type = UserAxisType.NUMBER

            order = axis["gridOrder"] if "gridOrder" in axis else default_order
            irregular = axis["irregular"] if "irregular" in axis else False
            data_bound = axis["dataBound"] if "dataBound" in axis else True
            # for irregular axes we consider the resolution 1 / -1 as gmlcov requires resolution for all axis types,
            # even irregular
            if "resolution" in axis:
                resolution = axis["resolution"]
            else:
                resolution = 1
            default_order += 1
            if not irregular:
                user_axes.append(
                    RegularUserAxis(crs_axis.label, resolution, order, axis["min"], max, type, data_bound))
            else:
                user_axes.append(
                    IrregularUserAxis(crs_axis.label, resolution, order, axis["min"], axis["directPositions"], max,
                                      type, data_bound))
        return user_axes

    def _global_metadata_fields(self):
        """
        Returns the global metadata fields
        :rtype: dict
        """
        if "metadata" in self.options['coverage']:
            if "global" in self.options['coverage']['metadata']:
                return self.options['coverage']['metadata']['global']
        return {}

    def _local_metadata_fields(self):
        """
        Returns the local metadata fields
        :rtype: dict
        """
        if "metadata" in self.options['coverage']:
            if "local" in self.options['coverage']['metadata']:
                return self.options['coverage']['metadata']['local']
        return {}

    def _metadata_type(self):
        """
        Returns the metadata type
        :rtype: str
        """
        if "metadata" in self.options['coverage']:
            return self.options['coverage']['metadata']['type']
        return None

    def _resolve_crs(self, crs):
        """
        Resolves a crs string to an url
        :param str crs: the crs string
        :rtype: str
        """
        crs_resolver = self.session.get_crs_resolver() + "crs/"
        if not crs.startswith("http"):
            crs_parts = crs.split("@")
            for i in range(0, len(crs_parts)):
                crs_parts[i] = crs_resolver + crs_parts[i]
            return CRSUtil.get_compound_crs(crs_parts)
        return crs

    def _get_coverage(self):
        """
        Returns the coverage to be used for the importer
        :rtype: master.importer.coverage.Coverage
        """
        coverage = None
        if self.options['coverage']['slicer']['type'] == self.GRIB_TYPE:
            coverage = self._get_grib_coverage()
        elif self.options['coverage']['slicer']['type'] == self.GDAL_TYPE:
            coverage = self._get_gdal_coverage()
        elif self.options['coverage']['slicer']['type'] == self.NETCDF_TYPE:
            coverage = self._get_netcdf_coverage()
        else:
            raise RuntimeException(
                "No valid slicer could be found, given: " + self.options['coverage']['slicer']['type'])
        return coverage

    def _get_gdal_coverage(self):
        """
        Returns a coverage that uses the gdal slicer
        :rtype: master.importer.coverage.Coverage
        """
        crs = self._resolve_crs(self.options['coverage']['crs'])
        sentence_evaluator = SentenceEvaluator(ExpressionEvaluatorFactory())
        coverage = GdalToCoverageConverter(sentence_evaluator, self.session.get_coverage_id(),
                                           self._read_bands(),
                                           self.session.get_files(), crs, self._read_axes(crs),
                                           self.options['tiling'], self._global_metadata_fields(),
                                           self._local_metadata_fields(), self._metadata_type(),
                                           self.options['coverage']['grid_coverage']).to_coverage()
        return coverage

    def _get_netcdf_coverage(self):
        """
        Returns a coverage that uses the netcdf slicer
        :rtype: master.importer.coverage.Coverage
        """
        crs = self._resolve_crs(self.options['coverage']['crs'])
        sentence_evaluator = SentenceEvaluator(ExpressionEvaluatorFactory())
        if 'pixelIsPoint' in self.options['coverage']['slicer'] and self.options['coverage']['slicer']['pixelIsPoint']:
            coverage = PointPixelNetcdfToCoverageConverter(sentence_evaluator, self.session.get_coverage_id(),
                                                           self._read_bands(),
                                                           self.session.get_files(), crs, self._read_axes(crs),
                                                           self.options['tiling'], self._global_metadata_fields(),
                                                           self._local_metadata_fields(), self._metadata_type(),
                                                           self.options['coverage']['grid_coverage']).to_coverage()
        else:
            coverage = NetcdfToCoverageConverter(sentence_evaluator, self.session.get_coverage_id(),
                                                 self._read_bands(),
                                                 self.session.get_files(), crs, self._read_axes(crs),
                                                 self.options['tiling'], self._global_metadata_fields(),
                                                 self._local_metadata_fields(), self._metadata_type(),
                                                 self.options['coverage']['grid_coverage']).to_coverage()
        return coverage

    def _get_grib_coverage(self):
        """
        Returns a coverage that uses the grib slicer
        :rtype: master.importer.coverage.Coverage
        """
        crs = self._resolve_crs(self.options['coverage']['crs'])
        sentence_evaluator = SentenceEvaluator(ExpressionEvaluatorFactory())
        if 'pixelIsPoint' in self.options['coverage']['slicer'] and self.options['coverage']['slicer']['pixelIsPoint']:
            coverage = PointPixelGRIBToCoverageConverter(sentence_evaluator, self.session.get_coverage_id(),
                                                        self._read_bands()[0],
                                                        self.session.get_files(), crs, self._read_axes(crs),
                                                        self.options['tiling'], self._global_metadata_fields(),
                                                        self._local_metadata_fields(), self._metadata_type(),
                                                        self.options['coverage']['grid_coverage']).to_coverage()
        else:
            coverage = GRIBToCoverageConverter(sentence_evaluator, self.session.get_coverage_id(),
                                               self._read_bands()[0],
                                               self.session.get_files(), crs, self._read_axes(crs),
                                               self.options['tiling'], self._global_metadata_fields(),
                                               self._local_metadata_fields(), self._metadata_type(),
                                               self.options['coverage']['grid_coverage']).to_coverage()
        return coverage


    def _get_importer(self):
        """
        Returns the importer
        :rtype: Importer
        """
        if self.importer is None:
            self.importer = Importer(self._get_coverage(), self.options['wms_import'],
                                     self.options['coverage']['grid_coverage'])
        return self.importer

    @staticmethod
    def get_name():
        return "general_coverage"
