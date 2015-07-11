from config_manager import ConfigManager
from util.file_util import FileUtil


class File:
    def __init__(self, filepath):
        self.filepath = filepath

    def get_filepath(self):
        return self.filepath

    def get_url(self):
        return ConfigManager.root_url + self.filepath

    def release(self):
        if ConfigManager.mock is False:
            fu = FileUtil()
            fu.delete_file(self.filepath)

    def __str__(self):
        return self.get_filepath()