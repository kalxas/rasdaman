import mimetypes

from master.provider.data.data_provider import DataProvider
from util.file_obj import File


class FileDataProvider(DataProvider):
    def __init__(self, file):
        """
        Class representing a data provider backed by a local accessible file
        :param File file: the file to back the provider
        """
        self.file = file

    def get_file_path(self):
        return self.file.get_filepath()

    def get_file_url(self):
        return self.file.get_url()

    def get_mimetype(self):
        mimetypes.guess_type(self.get_file_url())