import mimetypes

from master.provider.data.data_provider import DataProvider


class UrlDataProvider(DataProvider):
    def __init__(self, url):
        """
        Class representing a data provider backed by a network accessible resource
        :param str url: the url to the online resources
        """
        self.url = url

    def get_url(self):
        """
        Returns the url
        :rtype: str
        """
        return self.url

    def get_mimetype(self):
        """
        Returns the mimetype
        :rtype: str
        """
        mimetypes.guess_type(self.get_url())

    def __str__(self):
        return self.url