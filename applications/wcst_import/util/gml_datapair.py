import os


class GMLDataPair:
    def __init__(self, gml_filepath, data_filepath):
        """
        Contains the path to the gml file and the path to its corresponding data object.
        :param str gml_filepath: the path to the gml
        :param str data_filepath: the path to the data
        :return:
        """
        self.gml = gml_filepath
        self.data = data_filepath
        pass

    def get_gml_path(self):
        """
        Returns the gml path
        :rtype str
        """
        return self.gml

    def get_gml_url(self):
        """
        Returns the gml path
        :rtype str
        """
        return self.get_url_method() + self.gml

    def get_data_path(self):
        """
        Returns the data path
        :rtype str
        """
        return self.data

    def get_data_url(self):
        """
        Returns the data path
        :rtype str
        """
        return self.get_url_method() + self.data

    def delete_record_files(self):
        """
        Deletes the gml file, the data file should stay in place
        """
        os.remove(self.gml)

    @staticmethod
    def get_url_method():
        return "file://"