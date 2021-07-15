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

import json
import os
from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException
from master.importer.slice import Slice
from master.provider.data.data_provider import DataProvider
from util.file_util import FileUtil
from util.log import log


class Resumer:

    # Store all imported data files by coverage ids (key: coverage id)
    __IMPORTED_DATA_DICT = {}

    # Store all coverage_id.resume.json files
    __RESUMER_FILE_NAME_DICT = {}

    def __init__(self, coverage_id):
        """
        The resumer keeps track of data providers that have been imported so that a record is kept if several
        runs are performed
        :param str coverage_id: the id of the coverage that is imported
        """
        self.coverage_id = coverage_id
        self.__load_imported_data_from_resume_file(coverage_id)

    def __load_imported_data_from_resume_file(self, coverage_id):
        """
        Try to load a resume file coverage_id.resume.json from input data folder.
        :param str coverage_id: coverage id of current importer to find the resume file.
        """
        if coverage_id not in Resumer.__IMPORTED_DATA_DICT:
            resume_file_path = ConfigManager.resumer_dir_path + coverage_id + Resumer.__RESUMER_FILE_SUFFIX
            Resumer.__RESUMER_FILE_NAME_DICT[coverage_id] = resume_file_path
            try:
                if os.path.isfile(resume_file_path) \
                        and os.access(resume_file_path, os.R_OK):
                    log.info(
                        "We found a resumer file in the ingredients folder. The slices listed in '" + resume_file_path
                        + "' will not be imported.")
                    file = open(Resumer.__RESUMER_FILE_NAME_DICT[coverage_id])
                    data = json.loads(file.read())
                    Resumer.__IMPORTED_DATA_DICT[coverage_id] = data
                    file.close()
            except IOError as e:
                raise RuntimeException("Could not read the resume file, full error message: " + str(e))
            except ValueError as e:
                log.warn("The resumer JSON file could not be parsed. A new one will be created.")


    def checkpoint(self):
        """
        Adds a checkpoint and saves to the backing file
        """
        if ConfigManager.track_files and not ConfigManager.mock:
            resume_file_path = self.__RESUMER_FILE_NAME_DICT[self.coverage_id]

            try:
                file = open(resume_file_path, "w")
                json.dump(Resumer.__IMPORTED_DATA_DICT[self.coverage_id], file)
                file.close()
            except Exception as e:
                log.error("Cannot create resume file '{}'. \n"
                          "Hint: make sure the folder containing the resume file is writeable "
                          "for the user running wcst_import.sh.".format(resume_file_path))
                exit(1)

    def add_imported_data(self, data_provider):
        """
        Adds a data provider to the list of imported data
        :param DataProvider data_provider: The data provider that was imported
        """
        if ConfigManager.track_files and not ConfigManager.mock:
            if self.coverage_id not in Resumer.__IMPORTED_DATA_DICT:
                Resumer.__IMPORTED_DATA_DICT[self.coverage_id] = []

            if data_provider.to_eq_hash_original_file() not in Resumer.__IMPORTED_DATA_DICT[self.coverage_id]:
                Resumer.__IMPORTED_DATA_DICT[self.coverage_id].append(data_provider.to_eq_hash_original_file())
                self.checkpoint()

    def eliminate_already_imported_slices(self, slices):
        """
        Eliminates the slices that were already imported and returns a new array of slices
        :param list[Slice] slices: a list of slices
        :rtype: list[Slice]
        """
        if not ConfigManager.track_files:
            return slices

        ret_slices = []
        for slice in slices:
            if self.coverage_id in Resumer.__IMPORTED_DATA_DICT:
                # coverage_id.resume.json file exists
                if slice.data_provider.to_eq_hash() not in Resumer.__IMPORTED_DATA_DICT[self.coverage_id]:
                    ret_slices.append(slice)
            else:
                # No resume file exists
                ret_slices.append(slice)

        return ret_slices

    def is_file_imported(self, input_file_path):
        """
        Check if a file was imported and exists in *.resume.json
        :param input_file_path: path to input file
        """
        if self.coverage_id in Resumer.__IMPORTED_DATA_DICT:
            for imported_file in Resumer.__IMPORTED_DATA_DICT[self.coverage_id]:
                if FileUtil.strip_root_url(input_file_path) == FileUtil.strip_root_url(imported_file):
                    return True

        return False

    def get_not_imported_files(self, files):
        """
        Filter all not imported files from input files and files inside coverageId.resume.json file
        :param List[File] files: input files list
        """
        collected_files = []

        for file in files:
            if not self.is_file_imported(file.get_filepath()):
                collected_files.append(file)

        return collected_files

    __RESUMER_FILE_SUFFIX = ".resume.json"
