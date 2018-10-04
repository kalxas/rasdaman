from master.error.validate_exception import RecipeValidationException
from master.helper.gdal_axis_filler import GdalAxisFiller
from master.helper.gdal_range_fields_generator import GdalRangeFieldsGenerator
from master.importer.coverage import Coverage
from master.importer.importer import Importer
from master.importer.slice import Slice
from master.provider.data.file_data_provider import FileDataProvider
from master.recipe.base_recipe import BaseRecipe
from util.crs_util import CRSUtil
from util.gdal_util import GDALGmlUtil
from util.log import log
from master.importer.resumer import Resumer


class Recipe(BaseRecipe):
    def __init__(self, session):
        """
        The recipe class for my_custom_recipe (check WCST_Import guide from rasdaman web page for more details).
        :param Session session: the session for the import tun
        """
        super(Recipe, self).__init__(session)
        self.options = session.get_recipe()['options']
        self.importer = None
        self.resumer = Resumer(self.session.get_coverage_id())

    def validate(self):
        super(Recipe, self).validate()
        if "time_crs" not in self.options or self.options['time_crs'] == "":
            raise RecipeValidationException("No valid time crs provided")

        if 'time_tag' not in self.options:
            raise RecipeValidationException("No valid time tag parameter provided")

        if 'time_format' not in self.options:
            raise RecipeValidationException("You have to provide a valid time format")

    def describe(self):
        """
        Implementation of the base recipe describe method
        """
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
        return self._get_importer().get_progress()

    def _get_importer(self):
        if self.importer is None:
            self.importer = Importer(self.resumer, self._get_coverage())
        return self.importer

    def _get_coverage(self):
        # Get the crs of one of the images using a GDAL helper class. We are assuming all images have the same CRS
        gdal_dataset = GDALGmlUtil(self.session.get_files()[0].get_filepath())
        # Get the crs of the coverage by compounding the two crses
        crs = CRSUtil.get_compound_crs([gdal_dataset.get_crs(), self.options['time_crs']])
        fields = GdalRangeFieldsGenerator(gdal_dataset).get_range_fields()
        pixel_type = gdal_dataset.get_band_gdal_type()
        coverage_id = self.session.get_coverage_id()
        slices = self._get_slices(crs)
        return Coverage(coverage_id, slices, fields, crs, pixel_type)

    def _get_slices(self, crs):
        # Let's first extract all the axes from our crs
        crs_axes = CRSUtil(crs).get_axes()
        # Prepare a list container for our slices
        slices = []
        # Iterate over the files and create a slice for each one
        for infile in self.session.get_files():
            # We need to create the exact position in time and space in which to place this slice
            # For the space coordinates we can use the GDAL helper to extract it for us
            # The helper will return a list of subsets based on the crs axes that we extracted
            # and will fill the coordinates for the ones that it can (the easting and northing axes)
            subsets = GdalAxisFiller(crs_axes, GDALGmlUtil(infile.get_filepath())).fill()
            # Now we must fill the time axis as well and indicate the position in time
            for subset in subsets:
                # Find the time axis
                if subset.coverage_axis.axis.crs_axis.is_future():
                    # Set the time position for it. Our recipe extracts it from a GDAL tag provided by the user
                    # datetime format needs enquoted (e.g: "2015-01")
                    subset.interval.low = '"' + GDALGmlUtil(infile).get_datetime(self.options["time_tag"]) + '"'
            slices.append(Slice(subsets, FileDataProvider(infile)))

        return slices

    @staticmethod
    def get_name():
        return "test_custom_recipe"
