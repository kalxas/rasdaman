from recipes.shared.base_recipe import BaseRecipe
from recipes.map_mosaic.importer import Importer


class Recipe(BaseRecipe):
    def __init__(self, session):
        """
        The recipe class for regular timeseries. To get an overview of the ingredients needed for this
        recipe check ingredients/time_series_regular
        """
        super(Recipe, self).__init__(session)
        self.options = session.get_recipe()['options']

    def validate(self):
        """
        Implementation of the base recipe validate method
        """
        super(Recipe, self).validate()
        if 'tiling' not in self.options:
            self.options['tiling'] = None

    def describe(self):
        """
        Implementation of the base recipe describe method
        """
        super(Recipe, self).describe()

    def insert(self):
        """
        Implementation of the base recipe insert method
        """
        importer = self._get_importer(self.session.get_files(), False)
        importer.ingest()

    def update(self):
        """
        Implementation of the base recipe update method
        """
        importer = self._get_importer(self.session.get_files(), True)
        importer.ingest()

    def _get_importer(self, files, update=False):
        """
        Returns the correct importer for the import job
        :param bool update: true if this is an update operation false otherwise
        """
        importer = Importer(files, self.session.get_coverage_id(),
                            self.session.get_crs_resolver(), self.session.get_default_crs(), self.session.get_util(),
                            self.options['tiling'], self.session.get_executor(), update)
        return importer

    @staticmethod
    def get_name():
        return "map_mosaic"