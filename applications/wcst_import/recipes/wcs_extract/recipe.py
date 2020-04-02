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

from master.error.runtime_exception import RuntimeException
from master.importer.importer import Importer
from master.recipe.base_recipe import BaseRecipe
from session import Session
from util.coverage_reader import CoverageReader
from util.log import log
from util.url_util import validate_and_read_url
from master.importer.resumer import Resumer


class Recipe(BaseRecipe):
    def __init__(self, session):
        """
        The recipe class for wcs. To get an overview of the ingredients needed for this
        recipe check ingredients/map_mosaic
        :param Session session: the session for this import
        """
        super(Recipe, self).__init__(session)
        self.options = session.get_recipe()['options'] if "options" in session.get_recipe() else {}
        self.importer = None
        self.resumer = Resumer(self.session.get_coverage_id())
        self.coverage = None

    def validate(self):
        """
        Implementation of the base recipe validate method
        """
        self.validate_base(True)
        if 'coverage_id' not in self.options:
            self.options['coverage_id'] = self.session.get_coverage_id()

        if 'partitioning_scheme' not in self.options:
            self.options['partitioning_scheme'] = self.__DEFAULT_PARTITIONING

        if 'backup' not in self.options:
            self.options['backup'] = False
        else:
            self.options['backup'] = bool(self.options['backup'])

        if self.options['backup']:
            if 'backup_dir' not in self.options:
                raise RuntimeException(
                    "You need to provide a directory where to store the backup. Use the backup_dir parameter for this")
            if not os.access(self.options['backup_dir'], os.W_OK):
                raise RuntimeException("Your backup directory is not writable, please remedy this.")


        # Validate wcs_endpoint
        validate_and_read_url(self.options['wcs_endpoint'] + "?service=WCS&version=2.0.1&request=GetCapabilities")

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
        self._get_importer().ingest()

    def status(self):
        """
        Implementation of the status method
        :rtype (int, int)
        """
        return self.importer.get_progress()

    def _get_coverage(self):
        if self.coverage is None:
            self.coverage = CoverageReader(self.options['wcs_endpoint'], self.options['coverage_id'],
                                           self.options['partitioning_scheme']).get_coverage()
            self.coverage.coverage_id = self.session.get_coverage_id()
            self.coverage.tiling = self.options['tiling']
        return self.coverage

    def _get_importer(self):
        if self.importer is None:
            self.importer = Importer(self.resumer, self._get_coverage(), self.options['wms_import'], self.options['scale_levels'])
        return self.importer

    @staticmethod
    def get_name():
        return "wcs_extract"

    __DEFAULT_PARTITIONING = [4000, 4000]
