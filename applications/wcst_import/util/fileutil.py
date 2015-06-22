import shutil
import os
import uuid


class FileUtil:
    def __init__(self, tmp_path):
        """
        A utility function to do most of the repetitive work
        :param str tmp_path: the *absolute* path to the temp directory
        :rtype Util
        """
        self.tmp_path = tmp_path
        pass

    def generate_tmp_path(self, ftype="data"):
        """
        Generates a tmp unique path
        :param str ftype: the type of the file
        :rtype str
        """
        tmp_path = self.tmp_path + str(uuid.uuid4()).replace("-", "_") + "." + ftype
        return tmp_path

    def copy_file_to_tmp(self, file_path):
        """
        Copies the file into a new file in the tmp directory and returns the path
        :param str file_path: the path to the file
        :rtype: str
        """
        parts = file_path.split(".")
        ret_path = self.generate_tmp_path(parts[-1])
        shutil.copy(file_path, ret_path)
        os.chmod(ret_path, 0777)
        return ret_path

    def write_to_tmp_file(self, contents, ftype="gml"):
        """
        Writes a string to a temporary file and returns the path to it
        :param str contents: the contents to be written to the file
        :param str ftype: the type of the file
        :rtype str
        """
        ret_path = self.generate_tmp_path(ftype)
        wfile = open(ret_path, "w")
        wfile.write(contents)
        wfile.close()
        os.chmod(ret_path, 0777)
        return ret_path

    @staticmethod
    def get_directory_path(file_path):
        return os.path.dirname(os.path.abspath(file_path))
