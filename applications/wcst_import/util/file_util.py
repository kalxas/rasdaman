"""
 *
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
import shutil
import os
import sys
import uuid
from config_manager import ConfigManager
from util.log import log
import re
from master.error.runtime_exception import RuntimeException
import stat
from util.import_util import import_glob

class FileUtil:

    @staticmethod
    def delete_file(file_path):
        os.remove(file_path)

    @staticmethod
    def get_directory_path(file_path):
        return os.path.dirname(os.path.abspath(file_path))

    @staticmethod
    def print_feedback(current_number, number_of_files, file_path):
        log.info("Analyzing file ({}/{}): {} ...".format(current_number, number_of_files, file_path))

    @staticmethod
    def get_file_paths_by_regex(current_dir, file_path_regex):
        """
        From the file path in regular expression (e.g: *.txt, ./txt), return list of file paths
        :return: list of string
        """
        file_paths = []

        # If the input file is actually a regex pattern then glob can be used
        if "*" in file_path_regex or "?" in file_path_regex\
            or ".." in file_path_regex:
            glob = import_glob()
            if not file_path_regex.strip().startswith("/"):
                file_path_regex = current_dir + file_path_regex

            file_paths = file_paths + glob.glob(file_path_regex, recursive=True)
        else:
            # non regex file path
            if "/" not in file_path_regex:
                # only file name is listed
                file_path_regex = current_dir + "/" + file_path_regex

            file_paths.append(file_path_regex)

        return file_paths

    @staticmethod
    def validate_file_path(file_path):
        """
        Check if file exists, if not just log it and continue
        :param file_path: path to an input file
        :return: boolean
        """

        # For gdal virtual file path, example:
        # SENTINEL2_L1C:/vsizip//*_20181204T111726.zip/*_20181204T111726.SAFE/MTD_MSIL1C.xml:TCI:EPSG_32632
        pattern = re.compile(".*/vsi[a-z]+/.*")

        if pattern.match(file_path) or ":" in file_path or file_path.startswith("/vsi"):
            # It is gdal virtual file system or subdataset (e.g: NETCDF:file_path:variable),
            # or Amazon S3 file path, just ignore
            return True
        elif not os.access(file_path, os.R_OK):
            log.warn("File '" + file_path + "' is not accessible, will be skipped from further processing.")
            return False

        return True

    @staticmethod
    def validate_input_file_paths(file_paths):
        """
        If all input file paths are not available to analyze. Exit wcst_import process and log an warning.
        :param list[str] file_paths: list of input file paths
        """
        if len(file_paths) == 0:
            log.warn("No files provided. Check that the paths you provided are correct. Done.")
            exit(0)


    @staticmethod
    def ignore_coverage_slice_from_file_if_possible(file_path, exception):
        """
        In case, wcst_import cannot process 1 file due to some problem on it and "skip" is set to True,
        wcst_import should not throw exception but log an warning to user.

        :param str file_path: path to the problem file.
        :param Exception exception: exception was thrown from previous statements.
        """

        if ConfigManager.skip:
            log.warn("WARNING: input file '" + file_path + "' cannot be processed,\n"
                     "wcst_import will ignore this file as \"skip\" is set to true in the ingredient file. Reason: " + str(exception))
        else:
            # Throws the original source of exception(!)
            raise Exception(sys.exc_info()[1]).with_traceback(sys.exc_info()[2])


    @staticmethod
    def strip_root_url(input_file):
        """
        Strip file root (e.g: file://)
        :param str input_file: input file path
        """
        return input_file.replace(ConfigManager.root_url, "")


    @staticmethod
    def check_dir_writable(input_dir):
        """
        Check if wcst_import can write to 1 specific dir path
        :param str input_dir: path to a directory
        """
        import tempfile
        try:
            testfile = tempfile.TemporaryFile(dir=input_dir)
            testfile.close()
        except OSError as e:
            return False

        return True

    @staticmethod
    def read_file_to_string(file_path):
        """
        Read a file path to a string
        :param str file_path: absolute path
        :return: file content
        """

        if not os.access(file_path, os.R_OK):
            raise RuntimeException("Cannot read content from file '" + file_path + "' as string.")

        with open(file_path, "r") as myfile:
            data = myfile.read()

            return data


class TmpFile:
    def __init__(self):
        """
        A utility function to do most of the repetitive work
        :param str tmp_path: the *absolute* path to the temp directory
        :rtype Util
        """
        self.tmp_path = ConfigManager.tmp_directory
        pass

    def generate_tmp_path(self, ftype="data"):
        """
        Generates a tmp unique path
        :param str ftype: the type of the file
        :rtype str
        """
        tmp_path = self.tmp_path + str(uuid.uuid4()).replace("-", "_") + "." + ftype
        return tmp_path

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
        os.chmod(ret_path, stat.S_IRWXU | stat.S_IRWXG | stat.S_IRWXO)
        return ret_path

    def copy_file_to_tmp(self, file_path):
        """
        Copies the file into a new file in the tmp directory and returns the path
        :param str file_path: the path to the file
        :rtype: str
        """
        parts = file_path.split(".")
        ret_path = self.generate_tmp_path(parts[-1])
        shutil.copy(file_path, ret_path)
        os.chmod(ret_path, stat.S_IRWXU | stat.S_IRWXG | stat.S_IRWXO)
        return ret_path


class File(object):
    def __init__(self, filepath):
        self.filepath = filepath

    def get_filepath(self):
        return self.filepath

    def get_url(self):
        return ConfigManager.root_url + self.filepath

    def release(self):
        if ConfigManager.mock is False:
            FileUtil.delete_file(self.filepath)

    def __str__(self):
        return self.get_filepath()


class FilePair(File):
    """
    A pair of input file paths when there is pre hook changed original input file paths
    """

    def __init__(self, changed_file_path, original_file_path):
        super(FilePair, self).__init__(changed_file_path)
        self.original_file_path = original_file_path

    def get_original_url(self):
        return ConfigManager.root_url + self.original_file_path

    def get_original_file_path(self):
        return self.original_file_path
