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
from recipes.general_coverage.abstract_to_coverage_converter import AbstractToCoverageConverter
from session import Session
from util.crs_util import CRSUtil
from util.gdal_validator import GDALValidator
from util.log import log
from util.string_util import escape_metadata_dict
from util.string_util import escape_metadata_nested_dicts
from util.file_util import FileUtil
from util.gdal_util import GDALGmlUtil
from master.importer.resumer import Resumer


class Recipe(BaseRecipe):


    RECIPE_TYPE = "general_coverage"

    def __init__(self, session):
        """
        :param Session session: the session for this import
        """
        super(Recipe, self).__init__(session)
        self.options = session.get_recipe()['options'] if "options" in session.get_recipe() else {}
        self.importer = None
        self.resumer = Resumer(self.session.get_coverage_id())

        validator = GDALValidator(self.session.files)
        if ConfigManager.skip:
            self.session.files = validator.get_valid_files()

    def validate(self):
        """
        Implementation of the base recipe validate method
        """
        super(Recipe, self).validate()

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
        if 'bands' not in self.options['coverage']['slicer'] \
                and (self.options['coverage']['slicer']['type'] == GRIBToCoverageConverter.RECIPE_TYPE \
                or self.options['coverage']['slicer']['type'] == NetcdfToCoverageConverter.RECIPE_TYPE):
            raise RecipeValidationException(
                "The netcdf/grib slicer requires the existence of a band parameter inside the slicer parameter.")
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
                # NOTE: if directPositions was not specified, it means the file does not contains the irregular axis
                # so the irregular axis must be fetched from file name and considered as slice with coefficient is [0]
                # However, [0] could be miscalculated with arrow so set it to [None] and return [0] later
                axis["directPositions"] = AbstractToCoverageConverter.DIRECT_POSITIONS_SLICING

        if "metadata" in self.options['coverage'] and "type" not in self.options['coverage']['metadata']:
            raise RecipeValidationException("No type given for the metadata parameter.")

        if "metadata" in self.options['coverage'] and "type" in self.options['coverage']['metadata']:
            if not ExtraMetadataSerializerFactory.is_encoding_type_valid(self.options['coverage']['metadata']['type']):
                raise RecipeValidationException(
                    "No valid type given for the metadata parameter, accepted values are xml and json")

        if "metadata" in self.options['coverage']:
            supported_recipe = (self.options['coverage']['slicer']['type'] == "netcdf"
                                or self.options['coverage']['slicer']['type'] == "gdal")
            if not supported_recipe:
                # global metadata auto is supported for netCDF/GDAL recipe
                if "global" in self.options['coverage']['metadata']:
                    # NOTE: if global is not specified in netCDF ingredient file, it is considered auto
                    # which means extract all the global attributes of netcdf file to create global metadata
                    if self.options['coverage']['metadata']['global'] == "auto":
                        raise RecipeValidationException(
                            "Global auto metadata only supported in general recipe with slicer's type: netcdf/gdal.")

                # bands metadata auto is supported for netCDF recipe
                if "bands" in self.options['coverage']['metadata']:
                    bands_metadata = self.options['coverage']['metadata']['bands']
                    if bands_metadata == "auto":
                        raise RecipeValidationException(
                            "Bands auto metadata only supported in general recipe with slicer's type: netcdf.")
                    elif type(bands_metadata) is dict:
                        # Check if one band of bands specified with "auto"
                        for key, value in bands_metadata.items():
                            if value == "auto":
                                raise RecipeValidationException(
                                    "Band auto metadata only supported in general recipe with slicer's type: netcdf, "
                                    "violated for band '" + key + "'.")

    def describe(self):
        """
        Implementation of the base recipe describe method
        """
        importer = self._get_importer()

        slices = importer.get_slices_for_description()
        number_of_files = len(slices)
        log.info(
            "All files have been analyzed. Please verify that the axis subsets of the first {} files above are correct.".format(
                number_of_files))
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
        Returns a user band extracted from the ingredients if specified (required for netCDF/GRIB)
        :rtype: list[UserBand]
        """
        if "bands" in self.options['coverage']['slicer']:
            bands = self.options['coverage']['slicer']['bands']

            number_of_bands = len(bands)

            # NOTE: rasdaman supports 1 band grib only to import
            recipe_type = self.options['coverage']['slicer']['type']
            if recipe_type == GRIBToCoverageConverter.RECIPE_TYPE and number_of_bands > 1:
                raise RuntimeError("Only single band grib files are currently supported. "
                                   "Given " + str(number_of_bands) + " bands in ingredient file.")

            ret_bands = []
            i = 0
            for band in bands:
                identifier = self._read_or_empty_string(band, "identifier")

                if recipe_type == GdalToCoverageConverter.RECIPE_TYPE:
                    # NOTE: for old ingredients with wrong defined "identifier" with band name instead of index 0-based
                    if not identifier.isdigit():
                        identifier = str(i)

                ret_bands.append(UserBand(
                    identifier,
                    self._read_or_empty_string(band, "name"),
                    self._read_or_empty_string(band, "description"),
                    self._read_or_empty_string(band, "definition"),
                    self._read_or_empty_string(band, "nilReason"),
                    self._read_or_empty_string(band, "nilValue").split(","),
                    self._read_or_empty_string(band, "uomCode")
                ))

                i += 1

            return ret_bands
        else:
            if self.options['coverage']['slicer']['type'] == GdalToCoverageConverter.RECIPE_TYPE:
                # If gdal does not specify bands in ingredient file, just fetch all bands from first file
                first_file = self.session.get_files()[0]
                gdal_util = GDALGmlUtil(first_file)
                gdal_fields = gdal_util.get_fields_range_type()

                ret_bands = []
                for field in gdal_fields:
                    ret_bands.append(UserBand(
                        field.field_name,
                        field.field_name,
                        None,
                        None,
                        None,
                        field.nill_values
                    ))
                return ret_bands
            else:
                raise RuntimeError("'bands' must be specified in ingredient file for netCDF/GRIB recipes.")

    def _read_axes(self, crs):
        """
        Returns a list of user axes extracted from the ingredients file
        :param str crs: the crs of the coverage
        :rtype: list[UserAxis]
        """
        axes_configurations = self.options['coverage']['slicer']['axes']
        user_axes = []

        crs_axes = CRSUtil(crs).get_axes(self.session.coverage_id, axes_configurations)

        default_order = 0
        for index, crs_axis in enumerate(crs_axes):
            exist = False

            for axis_label, axis_configuration_dicts in axes_configurations.items():
                # If axis label configured in ingredient file does not exist in CRS definition,
                # then "crsOrder" configuration must match with the crs axis order.
                if crs_axis.label == axis_label \
                        or ("crsOrder" in axis_configuration_dicts
                            and int(axis_configuration_dicts["crsOrder"]) == index):
                    crs_axes[index].label = axis_label
                    exist = True
                    break

            if not exist:
                raise RecipeValidationException(
                    "Could not find a definition for axis '" + crs_axis.label + "' in the axes parameter.")
            axis = axes_configurations[crs_axis.label]
            max = axis["max"] if "max" in axis else None
            if "type" in axis:
                type = axis["type"]
            elif crs_axis.is_date_axis():
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

            if "statements" in axis:
                if isinstance(axis["statements"], list):
                    statements = axis["statements"]
                else:
                    statements = [axis["statements"]]
            else:
                statements = []

            slice_group_size = None
            if "sliceGroupSize" in axis:
                if not irregular:
                    raise RuntimeException("Cannot set 'sliceGroupSize' for regular axis '{}' in ingredient file.".format(crs_axis.label))
                else:
                    # Irregular axis with dataBound:false only can use sliceGroupSize (!)
                    value_str = axis["sliceGroupSize"]

                    if "dataBound" not in axis or axis["dataBound"] is True:
                        raise RuntimeException("Option 'sliceGroupSize' can be set only for irregular axis '{}'"
                                               " with \"dataBound\": false in ingredient file.".format(crs_axis.label))

                    try:
                        slice_group_size = float(value_str)
                        if slice_group_size <= 0:
                            raise ValueError
                    except ValueError:
                        raise RuntimeException("Option 'sliceGroupSize' for irregular axis '{}'"
                                               " in ingredient file must be positive number. Given '{}'.".format(crs_axis.label, value_str))

            if not irregular:
                user_axes.append(
                    RegularUserAxis(crs_axis.label, resolution, order, axis["min"], max, type, data_bound,
                                    statements))
            else:
                # NOTE: irregular axis cannot set any resolution != 1
                if int(resolution) != IrregularUserAxis.DEFAULT_RESOLUTION:
                    raise RuntimeException("Cannot set 'resolution' value for irregular axis '{}' in ingredient file."
                                           " Given '{}'.".format(crs_axis.label, resolution))

                user_axes.append(
                    IrregularUserAxis(crs_axis.label, resolution, order, axis["min"], axis["directPositions"], max,
                                      type, data_bound, statements, slice_group_size))

        number_of_specified_axes = len(axes_configurations.items())
        number_of_crs_axes = len(crs_axes)

        if number_of_specified_axes != number_of_crs_axes:
            raise RuntimeException("Number of axes in the coverage CRS ({}) does not match "
                                   "the number of axes specified in the ingredients file ({}).".format(number_of_crs_axes, number_of_specified_axes))

        return user_axes

    def __add_color_palette_table_to_global_metadata(self, metadata_dict):
        """
        If colorPaletteTable is added in ingredient file, then add it to coverage's global metadata
        """
        supported_recipe = (self.options['coverage']['slicer']['type'] == "gdal")
        color_palette_table = None

        if "metadata" in self.options['coverage']:
            if "colorPaletteTable" in self.options['coverage']['metadata']:
                value = self.options['coverage']['metadata']['colorPaletteTable']

                if value.strip() != "":
                    if value == "auto" and not supported_recipe:
                        raise RecipeValidationException("colorPaletteTable auto is only supported"
                                                        " in general recipe with slicer's type: gdal.")
                    elif value == "auto":
                        # Get colorPaletteTable automatically from first file
                        gdal_dataset = GDALGmlUtil(self.session.get_files()[0])
                        color_palette_table = gdal_dataset.get_color_table()
                    else:
                        # file_path can be relative path or full path
                        file_paths = FileUtil.get_file_paths_by_regex(self.session.get_ingredients_dir_path(), value)

                        if len(file_paths) == 0:
                            raise RecipeValidationException(
                                "Color palette table file does not exist, given: '" + value + "'.")
                        else:
                            file_path = file_paths[0]
                            # Add the content of colorPaletteTable to coverage's metadata
                            with open(file_path, 'r') as file_reader:
                                color_palette_table = file_reader.read()
            elif supported_recipe:
                # If colorPaletteTable is not mentioned in the ingredient, automatically fetch it
                gdal_dataset = GDALGmlUtil(self.session.get_files()[0])
                color_palette_table = gdal_dataset.get_color_table()

            if color_palette_table is not None:
                metadata_dict["colorPaletteTable"] = color_palette_table

    def _global_metadata_fields(self):
        """
        Returns the global metadata fields
        :rtype: dict
        """
        file_path = self.session.get_files()[0].filepath

        if "coverage" in self.options and "metadata" in self.options['coverage']:

            if "global" in self.options['coverage']['metadata']:
                global_metadata = self.options['coverage']['metadata']['global']

                # global_metadata is manually defined with { ... some values }
                if type(global_metadata) is dict:
                    metadata_dict = self.options['coverage']['metadata']['global']
                else:
                    # global metadata is defined with "a string"
                    if global_metadata != "auto":
                        raise RuntimeException("No valid global metadata attribute for gdal slicer could be found, "
                                               "given: " + global_metadata)
                    else:
                        # global metadata is defined with auto, then parse the metadata from the first file to a dict
                        if self.options['coverage']['slicer']['type'] == "gdal":
                            metadata_dict = GdalToCoverageConverter.parse_gdal_global_metadata(file_path)
                        elif self.options['coverage']['slicer']['type'] == "netcdf":
                            metadata_dict = NetcdfToCoverageConverter.parse_netcdf_global_metadata(file_path)
            else:
                # global is not specified in ingredient file, it is considered as "global": "auto"
                if self.options['coverage']['slicer']['type'] == "gdal":
                    metadata_dict = GdalToCoverageConverter.parse_gdal_global_metadata(file_path)
                elif self.options['coverage']['slicer']['type'] == "netcdf":
                    metadata_dict = NetcdfToCoverageConverter.parse_netcdf_global_metadata(file_path)

            self.__add_color_palette_table_to_global_metadata(metadata_dict)

            result = escape_metadata_dict(metadata_dict)
            return result

        return {}

    def _local_metadata_fields(self):
        """
        Returns the local metadata fields
        :rtype: dict
        """
        if "coverage" in self.options and "metadata" in self.options['coverage']:
            if "local" in self.options['coverage']['metadata']:
                # Each file can have different local metadata (e.g: different netCDF attributes, tiff tags,...)
                local_metadata_dict = self.options['coverage']['metadata']['local']
                return local_metadata_dict
        return {}

    def _bands_metadata_fields(self):
        """
        Returns the bands metadata fields which are used to add to bands of output file which supports this option (e.g: netCDF)
        :rtype: dict
        """
        if "metadata" in self.options['coverage']:
            if "bands" in self.options['coverage']['metadata']:
                # a list of user defined bands
                user_bands = self._read_bands()
                # a dictionary of user defined bands's metadata
                bands_metadata = self.options['coverage']['metadata']['bands']

                # validate if band's name does exist in list of user defined bands
                for band, band_attributes in bands_metadata.items():
                    exist = False
                    for user_band in user_bands:
                        if str(band) == str(user_band.name):
                            exist = True
                            break
                    if exist is False:
                        raise RuntimeException(
                            "Band's metadata with name: '" + band + "' does not exist in user defined bands.")

                # it is a valid definition
                return bands_metadata
        return {}

    def _netcdf_bands_metadata_fields(self):
        """
        Returns the bands metadata for netCDF file
        + If "bands" is specified in ingredient file under "metadata" with: "bands": { "band1": "auto", "band2": { "key": "value" }
        then "band1" will get metadata automatically from file (same as "band1" is not specified), "band2" will get metadata
        from the specified keys, values.
        + If "bands" is not specified in ingredient file or specified with "bands": "auto", then all coverage's bands' metadata
        will be extracted automatically from file.
        :rtype: dict
        """
        # a list of user defined bands
        user_bands = self._read_bands()

        file_path = self.session.get_files()[0].filepath

        # Just fetch all metadata for user specified bands
        bands_metadata = NetcdfToCoverageConverter.parse_netcdf_bands_metadata(file_path, user_bands)

        if "metadata" in self.options['coverage']:
            if "bands" in self.options['coverage']['metadata']:
                # a dictionary of user defined bands' metadata
                bands_metadata_configured = self.options['coverage']['metadata']['bands']

                if bands_metadata_configured != "auto":

                    # validate if band's name does exist in list of user defined bands
                    for band, band_attributes in bands_metadata_configured.items():
                        exist = False
                        for user_band in user_bands:

                            if str(band) == str(user_band.name):
                                # Metadata for 1 band is configured manually in ingredient file
                                if band_attributes != "auto":
                                    bands_metadata[band] = band_attributes

                                exist = True
                                break

                        if exist is False:
                            raise RuntimeException(
                                "Band's metadata with name: '" + band + "' does not exist in user defined bands.")

            return escape_metadata_nested_dicts(bands_metadata)

        return {}

    def _axes_metadata_fields(self):
        """
        Returns the axes metadata fields which are used to add to dimensions (axes) of output file which supports this option (e.g: netCDF)
        :rtype: dict
        """
        if "metadata" in self.options['coverage']:
            if "axes" in self.options['coverage']['metadata']:
                # a list of user defined axes
                crs = self._resolve_crs(self.options['coverage']['crs'])
                user_axes = self._read_axes(crs)
                # a dictionary of user defined axes's metadata
                axes_metadata = self.options['coverage']['metadata']['axes']

                # validate if axis name does exist in list of user defined axes
                for axis, axis_attributes in axes_metadata.items():
                    exist = False
                    for user_axis in user_axes:
                        if str(axis) == str(user_axis.name):
                            exist = True
                            break
                    if exist is False:
                        raise RuntimeException(
                            "Metadata of axis with name '" + axis + "' does not exist in user defined axes.")

                # it is a valid definition
                return axes_metadata
        return {}

    def _netcdf_axes_metadata_fields(self):
        """
        Returns the axes metadata for netCDF file.

         + If "axes" is specified in ingredient file under "metadata" as "axes":"auto"
         or if "axes" is not specified in ingredient file. All axes's metadata are collected from the first file
         for coverage's global metadata.

         + If "axes" is specified in ingredient file under "metadata" with manual configuration, e.g:
         "axes": {
                "Long": "auto",
                "Lat": { "key1": "value1", "key2": "value2",... }
                 }

         then, axis "Long" will get metadata automatically from file (same as if "Long" is not specified),
         "Lat" will get metadata from the specified "keys": "values".

         NOTE: The axis variable names (e.g: lon, lat,...) in input file are the ones configured by "slicer"/"axes"
         in ingredient file by parsing axis's "min" or "max" configuration (e.g: Long's "min": "${netcdf:variable:lon:min}"
         which means CRS axis 'Long' refers to axis variable 'lon' in netCDF file).

        :rtype: dict
        """
        crs = self._resolve_crs(self.options['coverage']['crs'])
        user_axes = self._read_axes(crs)

        file_path = self.session.get_files()[0].filepath
        crs_axes_configured_dict = self.options['coverage']['slicer']['axes']

        # Just fetch all metadata for user specified axes
        axes_metadata = NetcdfToCoverageConverter.parse_netcdf_axes_metadata(file_path, crs_axes_configured_dict)

        if "metadata" in self.options['coverage']:
            if "axes" in self.options['coverage']['metadata']:
                # a dictionary of user defined axes' metadata
                axes_metadata_configured = self.options['coverage']['metadata']['axes']

                if axes_metadata_configured != "auto":

                    # validate if axis's name does exist in list of user defined axes
                    for axis, axis_attributes in axes_metadata_configured.items():
                        exist = False
                        for user_axis in user_axes:

                            if str(axis) == str(user_axis.name):
                                # Metadata for 1 axis is configured manually in ingredient file
                                if axis_attributes != "auto":
                                    axes_metadata[axis] = axis_attributes

                                exist = True
                                break

                        if exist is False:
                            raise RuntimeException(
                                "Metadata of axis with name '" + axis + "' does not exist in user defined bands.")

            return escape_metadata_nested_dicts(axes_metadata)

        return {}

    def _metadata_type(self):
        """
        Returns the metadata type
        :rtype: str
        """
        if "coverage" in self.options and "metadata" in self.options['coverage']:
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
                if not crs_parts[i].startswith("http"):
                    crs_parts[i] = crs_resolver + crs_parts[i]
            crs = CRSUtil.get_compound_crs(crs_parts)

        return crs

    def _get_coverage(self):
        """
        Returns the coverage to be used for the importer
        :rtype: master.importer.coverage.Coverage
        """
        recipe_type = self.options['coverage']['slicer']['type']
        if recipe_type == GdalToCoverageConverter.RECIPE_TYPE:
            coverage = self._get_gdal_coverage(recipe_type)
        elif recipe_type == NetcdfToCoverageConverter.RECIPE_TYPE:
            coverage = self._get_netcdf_coverage(recipe_type)
        elif recipe_type == GRIBToCoverageConverter.RECIPE_TYPE:
            coverage = self._get_grib_coverage(recipe_type)
        else:
            raise RuntimeException(
                "No valid slicer could be found, given: " + recipe_type)
        return coverage

    def _get_gdal_coverage(self, recipe_type):
        """
        Returns a coverage that uses the gdal slicer
        :param string: recipe_type the type of recipe
        :rtype: master.importer.coverage.Coverage
        """
        crs = self._resolve_crs(self.options['coverage']['crs'])
        sentence_evaluator = SentenceEvaluator(ExpressionEvaluatorFactory())
        coverage = GdalToCoverageConverter(self.resumer, self.session.default_null_values,
                                           recipe_type, sentence_evaluator, self.session.get_coverage_id(),
                                           self._read_bands(),
                                           self.session.get_files(), crs, self._read_axes(crs),
                                           self.options['tiling'], self._global_metadata_fields(),
                                           self._local_metadata_fields(),
                                           self._bands_metadata_fields(),
                                           self._axes_metadata_fields(),
                                           self._metadata_type(),
                                           self.options['coverage']['grid_coverage'],
                                           self.options['import_order']).to_coverage()
        return coverage

    def _get_netcdf_coverage(self, recipe_type):
        """
        Returns a coverage that uses the netcdf slicer
        :param: string recipe_type the type of netcdf
        :rtype: master.importer.coverage.Coverage
        """
        crs = self._resolve_crs(self.options['coverage']['crs'])
        sentence_evaluator = SentenceEvaluator(ExpressionEvaluatorFactory())
        # by default pixelIsPoint is not set to true in ingredient file
        pixel_is_point = False
        if 'pixelIsPoint' in self.options['coverage']['slicer'] and self.options['coverage']['slicer']['pixelIsPoint']:
            pixel_is_point = True

        coverage = NetcdfToCoverageConverter(self.resumer, self.session.default_null_values,
                                             recipe_type, sentence_evaluator, self.session.get_coverage_id(),
                                             self._read_bands(),
                                             self.session.get_files(), crs, self._read_axes(crs),
                                             self.options['tiling'], self._global_metadata_fields(),
                                             self._local_metadata_fields(),
                                             self._netcdf_bands_metadata_fields(),
                                             self._netcdf_axes_metadata_fields(),
                                             self._metadata_type(),
                                             self.options['coverage']['grid_coverage'], pixel_is_point,
                                             self.options['import_order']).to_coverage()
        return coverage

    def _get_grib_coverage(self, recipe_type):
        """
        Returns a coverage that uses the grib slicer
        :param: string recipe_type the type of grib
        :rtype: master.importer.coverage.Coverage
        """
        crs = self._resolve_crs(self.options['coverage']['crs'])
        sentence_evaluator = SentenceEvaluator(ExpressionEvaluatorFactory())
        pixel_is_point = False
        if 'pixelIsPoint' in self.options['coverage']['slicer'] and self.options['coverage']['slicer']['pixelIsPoint']:
            pixel_is_point = True

        coverage = GRIBToCoverageConverter(self.resumer, self.session.default_null_values,
                                           recipe_type,
                                           sentence_evaluator, self.session.get_coverage_id(),
                                           self._read_bands(),
                                           self.session.get_files(), crs, self._read_axes(crs),
                                           self.options['tiling'], self._global_metadata_fields(),
                                           self._bands_metadata_fields(),
                                           self._local_metadata_fields(),
                                           self._axes_metadata_fields(),
                                           self._metadata_type(),
                                           self.options['coverage']['grid_coverage'], pixel_is_point,
                                           self.options['import_order']).to_coverage()

        return coverage

    def _get_importer(self):
        """
        Returns the importer
        :rtype: Importer
        """
        if self.importer is None:
            self.importer = Importer(self.resumer, self._get_coverage(), self.options['wms_import'], self.options['scale_levels'],
                                     self.options['coverage']['grid_coverage'])
        return self.importer

    @staticmethod
    def get_name():
        return "general_coverage"
