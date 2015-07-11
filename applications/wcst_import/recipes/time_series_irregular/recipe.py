import re

from master.helper.gdal_axis_filler import GdalAxisFiller
from master.helper.gdal_range_fields_generator import GdalRangeFieldsGenerator
from master.importer.axis_subset import AxisMetadata
from master.importer.coverage import Coverage
from master.importer.importer import Importer
from master.importer.slice import Slice
from master.provider.data.file_data_provider import FileDataProvider
from master.provider.metadata.irregular_axis import IrregularAxis
from master.recipe.base_recipe import BaseRecipe
from master.error.validate_exception import RecipeValidationException
from util.crs_util import CRSUtil
from util.gdal_util import GDALGmlUtil
from util.log import log
from master.helper.time_gdal_tuple import TimeFileTuple
from util.time_util import DateTimeUtil


class Recipe(BaseRecipe):
    def __init__(self, session):
        """
            The recipe class for irregular timeseries. To get an overview of the ingredients needed for this
            recipe check ingredients/time_series_irregular
            """
        super(Recipe, self).__init__(session)
        self.options = session.get_recipe()['options']
        self.importer = None

    def validate(self):
        super(Recipe, self).validate()

        if "time_crs" not in self.options or self.options['time_crs'] == "":
            raise RecipeValidationException("No valid time crs provided")

        if 'time_parameter' not in self.options:
            raise RecipeValidationException("No valid time parameter provided")

        if 'metadata_tag' not in self.options['time_parameter'] and 'filename' not in self.options['time_parameter']:
            raise RecipeValidationException(
                "You have to provide either a metadata_tag or a filename pattern for the time parameter")

        if 'datetime_format' not in self.options['time_parameter']:
            raise RecipeValidationException("No valid datetime_format provided")

        if 'metadata_tag' in self.options['time_parameter'] and \
                        "tag_name" not in self.options['time_parameter']['metadata_tag']:
            raise RecipeValidationException("No metadata tag to extract time from gdal was provided")

        if 'filename' in self.options['time_parameter'] \
                and self.options['time_parameter']['filename']['regex'] == "" \
                and self.options['time_parameter']['filename']['group'] == "":
            raise RecipeValidationException("No filename regex and group to extract time from gdal was provided")

        if 'tiling' not in self.options:
            self.options['tiling'] = None

    def describe(self):
        """
        Implementation of the base recipe describe method
        """
        super(Recipe, self).describe()
        timeseries = self._generate_timeseries_tuples(5)  # look at the first 5 records only and show them to the user
        log.info(
            "\n" + str(len(timeseries)) + " files have been analyzed. Check that the timestamps are correct for each.")
        for slice in timeseries:
            log.info("File: " + slice.file.get_filepath() + " | " + "Timestamp: " + slice.time.to_string())

    def ingest(self):
        """
        Ingests the input files
        """
        self.importer = Importer(self._get_coverage())
        self.importer.ingest()

    def status(self):
        """
        Implementation of the status method
        :rtype (int, int)
        """
        if self.importer is None:
            return 0, 0
        else:
            return self.importer.get_progress()

    def _generate_timeseries_tuples(self, limit=None):
        """
        Generate the timeseries tuples from the original files based on the recipe
        :rtype: list[TimeFileTuple]
        """
        ret = []
        if limit is None:
            limit = len(self.session.get_files())

        time_format = None
        if 'datetime_format' in self.options['time_parameter']:
            time_format = self.options['time_parameter']['datetime_format']

        if 'metadata_tag' in self.options['time_parameter']:
            mtag = self.options['time_parameter']['metadata_tag']['tag_name']
            for tfile in self.session.get_files():
                if len(ret) == limit:
                    break
                gdal_file = GDALGmlUtil(tfile.get_filepath())
                dtutil = DateTimeUtil(gdal_file.get_datetime(mtag), time_format, self.options['time_crs'])
                ret.append(TimeFileTuple(dtutil, tfile))
        elif 'filename' in self.options['time_parameter'] and len(ret) < limit:
            regex = self.options['time_parameter']['filename']['regex']
            group = int(self.options['time_parameter']['filename']['group'])
            for tfile in self.session.get_files():
                if len(ret) == limit:
                    break
                dtutil = DateTimeUtil(re.search(regex, tfile.filepath).group(group),
                                      time_format, self.options['time_crs'])
                ret.append(TimeFileTuple(dtutil, tfile))
        else:
            raise RecipeValidationException("No method to get the time parameter, you should either choose "
                                            "metadata_tag or filename.")
        return sorted(ret)

    def _get_slices(self, crs):
        """
        Returns the slices for the collection of files given
        """
        crs_axes = CRSUtil(crs).get_axes()
        slices = []
        timeseries = self._generate_timeseries_tuples()
        for tpair in timeseries:
            subsets = GdalAxisFiller(crs_axes, GDALGmlUtil(tpair.file.get_filepath())).fill()
            subsets = self._fill_time_axis(tpair, subsets)
            slices.append(Slice(subsets, FileDataProvider(tpair.file)))
        return slices

    def _fill_time_axis(self, tpair, subsets):
        """
        Fills the time axis parameters
        :param TimeFileTuple tpair: the input pair
        :param list[AxisMetadata] subsets: the axis subsets for the tpair
        """
        for i in range(0, len(subsets)):
            if subsets[i].axis.crs_axis is not None and subsets[i].axis.crs_axis.is_future():
                subsets[i].axis = IrregularAxis(subsets[i].axis.label, subsets[i].axis.uomLabel, subsets[i].axis.low,
                                                subsets[i].axis.high, tpair.time.to_string(), 0,
                                                subsets[i].axis.crs_axis)
                subsets[i].grid_axis.resolution = 1
                subsets[i].interval.low = tpair.time.to_string()
        return subsets

    def _get_coverage(self):
        """
        Returns the coverage to be used for the importer
        """
        gdal_dataset = GDALGmlUtil(self.session.get_files()[0].get_filepath())
        crs = CRSUtil.get_compound_crs([gdal_dataset.get_crs(), self.options['time_crs']])
        slices = self._get_slices(crs)
        fields = GdalRangeFieldsGenerator(gdal_dataset).get_range_fields()
        coverage = Coverage(self.session.get_coverage_id(), slices, fields, crs,
            gdal_dataset.get_band_gdal_type(), self.options['tiling'])
        return coverage

    @staticmethod
    def get_name():
        return "time_series_irregular"