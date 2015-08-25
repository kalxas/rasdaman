import os

from master.error.runtime_exception import RuntimeException
from master.importer.importer import Importer
from master.recipe.base_recipe import BaseRecipe
from session import Session
from util.coverage_reader import CoverageReader
from util.log import log


class Recipe(BaseRecipe):
    def __init__(self, session):
        """
        The recipe class for wcs. To get an overview of the ingredients needed for this
        recipe check ingredients/map_mosaic
        :param Session session: the session for this import
        """
        super(Recipe, self).__init__(session)
        self.options = session.get_recipe()['options']
        self.importer = None
        self.coverage = None

    def validate(self):
        """
        Implementation of the base recipe validate method
        """
        self.validate_base(True)
        if 'coverage_id' not in self.options:
            self.options['coverage_id'] = self.session.get_coverage_id()

        if 'tiling' not in self.options:
            self.options['tiling'] = None

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

        if 'wms_import' not in self.options:
            self.options['wms_import'] = False
        else:
            self.options['wms_import'] = bool(self.options['wms_import'])

    def describe(self):
        """
        Implementation of the base recipe describe method
        """
        super(Recipe, self).describe()
        cov = self._get_coverage()
        log.info("We have partitioned the coverage in " + str(len(cov.slices)) + " slices. Here are the first 5 of them: ")
        for i in range(0, 5):
            slice = cov.slices[i]
            log.info("Slice " + str(i) + ": " + str(slice))

    def ingest(self):
        """
        Starts the ingesting process
        """
        self.importer = Importer(self._get_coverage(), self.options['wms_import'])
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

    def _get_coverage(self):
        if self.coverage is None:
            self.coverage = CoverageReader(self.options['wcs_endpoint'], self.options['coverage_id'],
                                           self.options['partitioning_scheme']).get_coverage()
            self.coverage.coverage_id = self.session.get_coverage_id()
            self.coverage.tiling = self.options['tiling']
        return self.coverage

    @staticmethod
    def get_name():
        return "wcs_extract"

    __DEFAULT_PARTITIONING = [4000, 4000]