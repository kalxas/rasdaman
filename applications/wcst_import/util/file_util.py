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
 * Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
import shutil
import os
import uuid
from config_manager import ConfigManager
from util.log import log


class FileUtil:
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

    def delete_file(self, file_path):
        os.remove(file_path)

    @staticmethod
    def get_directory_path(file_path):
        return os.path.dirname(os.path.abspath(file_path))

    @staticmethod
    def print_feedback(current_number, number_of_files, file_path):
        log.info("Analyzing file ({}/{}): {} ...".format(current_number, number_of_files, file_path))
