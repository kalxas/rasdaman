import re

from recipes.shared.base_recipe import BaseRecipe
from recipes.shared.runtime_exception import RuntimeException
from recipes.shared.validate_exception import RecipeValidationException
from recipes.time_series_regular.importer import Importer
from util.log import log
from util.time_gdal_tuple import TimeGdalTuple
from util.time_util import DateTimeUtil


class Recipe(BaseRecipe):
    def __init__(self, session):
        """
        The recipe class for regular timeseries. To get an overview of the ingredients needed for this
        recipe check ingredients/time_series_regular
        """
        super(Recipe, self).__init__(session)
        self.options = session.get_recipe()['options']
        self.importer = None

    def validate(self):
        super(Recipe, self).validate()

        if "time_crs" not in self.options or self.options['time_crs'] == "":
            raise RecipeValidationException("No valid time crs provided")

        if 'time_start' not in self.options:
            raise RecipeValidationException("No valid time start parameter provided")

        if 'time_step' not in self.options:
            raise RecipeValidationException(
                "You have to provide a valid time step indicating both the value and the unit of time")

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
        self.importer = self._get_importer(import_tuples, False)
        self.importer.ingest()

    def update(self):
        """
        Implementation of the base recipe update method
        """
        import_tuples = self._generate_timeseries_tuples()
        self.importer = self._get_importer(import_tuples, True)
        self.importer.ingest()

    def status(self):
        """
        Implementation of the status method
        :rtype (int, int)
        """
        if self.importer is None:
            return 0, 0
        else:
            return self.importer.get_processed_slices(), len(self.importer.timeseries)

    def _generate_timeseries_tuples(self, limit=None):
        """
        Generate the timeseries tuples from the original files based on the recipe
        :rtype: list[TimeGdalTuple]
        """
        ret = []
        if limit is None:
            limit = len(self.session.get_files()) + 1

        time_offset = 0
        time_format = self.options['time_format'] if self.options['time_format'] != "auto" else None
        time_start = DateTimeUtil(self.options['time_start'], time_format, self.options['time_crs'])
        for tfile in self.session.get_files():
            if len(ret) == limit:
                break
            time_tuple = TimeGdalTuple(self._get_datetime_with_step(time_start, time_offset), tfile)
            ret.append(time_tuple)
            time_offset += 1

        return sorted(ret)

    def _get_datetime_with_step(self, current, offset):
        """
        Returns the new datetime
        :param DateTimeUtil current: the date to add the step
        :param int offset: the number of steps to make
        """
        days, hours, minutes, seconds = tuple([offset * item for item in self._get_real_step()])
        return DateTimeUtil(current.datetime.replace(days=+days, hours=+hours, minutes=+minutes,
                                                     seconds=+seconds).isoformat(), None, self.options['time_crs'])

    def _get_real_step(self):
        res = re.search(
            "([0-9]*[\s]*days)?[\s]*"
            "([0-9]*[\s]*hours)?[\s]*"
            "([0-9]*[\s]*minutes)?[\s]*"
            "([0-9]*[\s]*seconds)?[\s]*",
            self.options['time_step'])
        days_s = res.group(1)
        hours_s = res.group(2)
        minutes_s = res.group(3)
        seconds_s = res.group(4)

        if days_s is None and hours_s is None and minutes_s is None and seconds_s is None:
            raise RuntimeException(
                'The time step does not have a valid unit of measure. '
                'Example of a valid time step: 1 days 2 hours 10 seconds')

        days = (int(days_s.replace("days", "").strip()) if days_s is not None else 0)
        hours = (int(hours_s.replace("hours", "").strip()) if hours_s is not None else 0)
        minutes = (int(minutes_s.replace("minutes", "").strip()) if minutes_s is not None else 0)
        seconds = (int(seconds_s.replace("seconds", "").strip()) if seconds_s is not None else 0)
        return days, hours, minutes, seconds

    def _get_importer(self, import_tuples, update=False):
        """
        Returns the correct importer for the import job
        :param list[TimeGdalTuple] import_tuples: the tuples to be imported
        :param bool update: true if this is an update operation false otherwise
        """
        days, hours, minutes, seconds = self._get_real_step()
        number_of_days = days + hours / float(24) + minutes / float(60 * 24) + seconds / float(60 * 60 * 24)
        importer = Importer(self.session, import_tuples, number_of_days, self.options['tiling'],
                            self.options['time_crs'], update)
        return importer

    @staticmethod
    def get_name():
        return "time_series_regular"