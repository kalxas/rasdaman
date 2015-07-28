from master.helper.gdal_axis_filler import GdalAxisFiller
from master.helper.gdal_range_fields_generator import GdalRangeFieldsGenerator
from master.importer.coverage import Coverage
from master.importer.importer import Importer
from master.importer.slice import Slice
from master.provider.data.file_data_provider import FileDataProvider
from master.recipe.base_recipe import BaseRecipe
from session import Session
from util.crs_util import CRSUtil
from util.gdal_util import GDALGmlUtil


class Recipe(BaseRecipe):
    def __init__(self, session):
        """
        The recipe class for map_mosaic. To get an overview of the ingredients needed for this
        recipe check ingredients/map_mosaic
        :param Session session: the session for this import
        """
        super(Recipe, self).__init__(session)
        self.options = session.get_recipe()['options']
        self.importer = None

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

        if 'band_names' not in self.options:
            self.options['band_names'] = None

    def describe(self):
        """
        Implementation of the base recipe describe method
        """
        super(Recipe, self).describe()

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

    def _get_slices(self, gdal_dataset):
        """
        Returns the slices for the collection of files given
        """
        files = self.session.get_files()
        crs = gdal_dataset.get_crs()
        crs_axes = CRSUtil(crs).get_axes()
        slices = []
        for file in files:
            subsets = GdalAxisFiller(crs_axes, GDALGmlUtil(file.get_filepath())).fill()
            slices.append(Slice(subsets, FileDataProvider(file)))
        return slices

    def _get_coverage(self):
        """
        Returns the coverage to be used for the importer
        """
        gdal_dataset = GDALGmlUtil(self.session.get_files()[0].get_filepath())
        slices = self._get_slices(gdal_dataset)
        fields = GdalRangeFieldsGenerator(gdal_dataset, self.options['band_names']).get_range_fields()

        coverage = Coverage(self.session.get_coverage_id(), slices, fields, gdal_dataset.get_crs(),
            gdal_dataset.get_band_gdal_type(), self.options['tiling'])
        return coverage

    @staticmethod
    def get_name():
        return "map_mosaic"