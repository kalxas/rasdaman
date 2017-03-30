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

import re

from master.error.runtime_exception import RuntimeException
from master.helper.gdal_axis_filler import GdalAxisFiller
from master.helper.gdal_range_fields_generator import GdalRangeFieldsGenerator
from master.importer.axis_subset import AxisSubset
from master.importer.coverage import Coverage
from master.importer.importer import Importer
from master.importer.slice import Slice
from master.provider.data.file_data_provider import FileDataProvider
from master.provider.metadata.regular_axis import RegularAxis
from master.recipe.base_recipe import BaseRecipe
from master.error.validate_exception import RecipeValidationException
from util.crs_util import CRSUtil
from util.gdal_util import GDALGmlUtil
from util.log import log
from master.helper.time_gdal_tuple import TimeFileTuple
from util.time_util import DateTimeUtil
from util.gdal_validator import GDALValidator
from config_manager import ConfigManager
from util.file_util import FileUtil


class Recipe(BaseRecipe):
    def __init__(self, session):
        """
        The recipe class for regular timeseries. To get an overview of the ingredients needed for this
        recipe check ingredients/time_series_regular
        """
        super(Recipe, self).__init__(session)
        self.options = session.get_recipe()['options']
        self.importer = None

        validator = GDALValidator(self.session.files)
        if ConfigManager.skip:
            self.session.files = validator.get_valid_files()

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

        if 'band_names' not in self.options:
            self.options['band_names'] = None

    def describe(self):
        """
        Implementation of the base recipe describe method
        """
        super(Recipe, self).describe()
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
        Ingests the input files
        """
        self._get_importer().ingest()

    def status(self):
        """
        Implementation of the status method
        :rtype (int, int)
        """
        return self._get_importer().get_progress()

    def _generate_timeseries_tuples(self, limit=None):
        """
        Generate the timeseries tuples from the original files based on the recipe
        :rtype: list[TimeFileTuple]
        """
        ret = []
        if limit is None:
            limit = len(self.session.get_files())

        time_offset = 0
        time_format = self.options['time_format'] if self.options['time_format'] != "auto" else None
        time_start = DateTimeUtil(self.options['time_start'], time_format, self.options['time_crs'])
        for tfile in self.session.get_files():
            if len(ret) == limit:
                break
            time_tuple = TimeFileTuple(self._get_datetime_with_step(time_start, time_offset), tfile)
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

    def _get_slices(self, crs):
        """
        Returns the slices for the collection of files given
        """
        crs_axes = CRSUtil(crs).get_axes()
        slices = []
        timeseries = self._generate_timeseries_tuples()
        count = 1
        for tpair in timeseries:
            # print which file is analyzing
            FileUtil.print_feedback(count, len(timeseries), tpair.file.filepath)
            subsets = GdalAxisFiller(crs_axes, GDALGmlUtil(tpair.file.get_filepath())).fill()
            subsets = self._fill_time_axis(tpair, subsets)
            slices.append(Slice(subsets, FileDataProvider(tpair.file)))
            count += 1
        return slices

    def _fill_time_axis(self, tpair, subsets):
        """
        Fills the time axis parameters
        :param TimeFileTuple tpair: the input pair
        :param list[AxisSubset] subsets: the axis subsets for the tpair
        """
        days, hours, minutes, seconds = self._get_real_step()
        number_of_days = days + hours / float(24) + minutes / float(60 * 24) + seconds / float(60 * 60 * 24)
        for i in range(0, len(subsets)):
            if subsets[i].coverage_axis.axis.crs_axis is not None and subsets[i].coverage_axis.axis.crs_axis.is_future():
                subsets[i].coverage_axis.axis = RegularAxis(subsets[i].coverage_axis.axis.label,
                                                            subsets[i].coverage_axis.axis.uomLabel,
                                                            subsets[i].coverage_axis.axis.low,
                                                            subsets[i].coverage_axis.axis.high, tpair.time.to_string(),
                                                            subsets[i].coverage_axis.axis.crs_axis)
                subsets[i].coverage_axis.grid_axis.resolution = number_of_days
                subsets[i].interval.low = tpair.time.to_string()
        return subsets

    def _get_coverage(self):
        """
        Returns the coverage to be used for the importer
        """
        gdal_dataset = GDALGmlUtil(self.session.get_files()[0].get_filepath())
        crs = CRSUtil.get_compound_crs([gdal_dataset.get_crs(), self.options['time_crs']])
        slices = self._get_slices(crs)
        fields = GdalRangeFieldsGenerator(gdal_dataset, self.options['band_names']).get_range_fields()
        coverage = Coverage(self.session.get_coverage_id(), slices, fields, crs,
            gdal_dataset.get_band_gdal_type(), self.options['tiling'])
        return coverage

    def _get_importer(self):
        if self.importer is None:
            self.importer = Importer(self._get_coverage())
        return self.importer

    @staticmethod
    def get_name():
        return "time_series_regular"
