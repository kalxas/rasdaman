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
        """
        Returns the path to the file
        :rtype: str
        """
        return self.file.get_filepath()

    def get_file_url(self):
        """
        Returns the url to the file using the default file protocol set in the config
        :rtype: str
        """
        return self.file.get_url()

    def get_mimetype(self):
        """
        Returns the mimetype
        :rtype: str
        """
        mimetypes.guess_type(self.get_file_url())

    def __str__(self):
        return self.get_file_url()

    def to_eq_hash(self):
        """
        Returns a hash of the object that can be used to compare with other providers that might be instantiated
        in a different run of the program. Two data providers must be hash equal if by importing them you
        obtain the same end result
        :rtype: str
        """
        return self.get_file_url()