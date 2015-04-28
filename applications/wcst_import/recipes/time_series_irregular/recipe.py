import re

from recipes.shared.base_recipe import BaseRecipe
from recipes.shared.validate_exception import RecipeValidationException
from recipes.time_series_irregular.importer import Importer
from util.gdal_util import GDALGmlUtil
from util.log import log
from util.time_gdal_tuple import TimeGdalTuple
from util.time_util import DateTimeUtil


class Recipe(BaseRecipe):
    def __init__(self, session):
        """
            The recipe class for irregular timeseries. To get an overview of the ingredients needed for this
            recipe check ingredients/time_series_irregular
            """
        super(Recipe, self).__init__(session)
        self.options = session.get_recipe()['options']

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
        log.info(str(len(timeseries)) + " files have been analyzed. Check that the timestamps are correct for each.")
        for slice in timeseries:
            log.info("File: " + slice.filepath + " | " + "Timestamp: " + slice.time.to_ansi())

    def insert(self):
        """
        Implementation of the base recipe insert method
        """
        import_tuples = self._generate_timeseries_tuples()
        importer = self._get_importer(import_tuples, False)
        importer.ingest()

    def update(self):
        """
        Implementation of the base recipe update method
        """
        import_tuples = self._generate_timeseries_tuples()
        importer = self._get_importer(import_tuples, True)
        importer.ingest()

    def _generate_timeseries_tuples(self, limit=None):
        """
        Generate the timeseries tuples from the original files based on the recipe
        :rtype: list[TimeGdalTuple]
        """
        ret = []
        if limit is None:
            limit = len(self.session.get_files()) + 1

        time_format = None
        if 'datetime_format' in self.options['time_parameter']:
            time_format = self.options['time_parameter']['datetime_format']

        if 'metadata_tag' in self.options['time_parameter']:
            mtag = self.options['time_parameter']['metadata_tag']['tag_name']
            for tfile in self.session.get_files():
                if len(ret) == limit:
                    break
                gdal_file = GDALGmlUtil(self.session.get_crs_resolver(), self.session.get_default_crs(), tfile)
                dtutil = DateTimeUtil(gdal_file.get_datetime(mtag), time_format)
                ret.append(TimeGdalTuple(dtutil, tfile))
        elif 'filename' in self.options['time_parameter'] and len(ret) < limit:
            regex = self.options['time_parameter']['filename']['regex']
            group = int(self.options['time_parameter']['filename']['group'])
            for tfile in self.session.get_files():
                if len(ret) == limit:
                    break
                dtutil = DateTimeUtil(re.search(regex, tfile).group(group), time_format)
                ret.append(TimeGdalTuple(dtutil, tfile))
        else:
            raise RecipeValidationException("No method to get the time parameter, you should either choose "
                                            "metadata_tag or filename.")
        return sorted(ret)

    def _get_importer(self, import_tuples, update=False):
        """
        Returns the correct importer for the import job
        """
        importer = Importer(import_tuples, self.session.get_coverage_id(), self.options['time_crs'],
                            self.session.get_crs_resolver(), self.session.get_default_crs(), self.session.get_util(),
                            self.options['tiling'], self.session.get_executor(), update)
        return importer

    @staticmethod
    def get_name():
        return "time_series_irregular"