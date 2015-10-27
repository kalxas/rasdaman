"""
Utility class for checking the slices are valid or invalid
"""

import osgeo.gdal as gdal
from util.log import log


class GDALValidator:
    def __init__(self, files):
        self.files = files

    def get_valid_files(self):
        """
            Valid file path could be opened by GDAL
            files is list of files need to valid
        """
        # Validate input files by GDAL. If GDAL could not decode file then will have an warning.
	# GDAL needs file name encode in 'utf8' or file name with spaces could not open.
        file_paths = []

        for file in self.files:
            fileName = str(file).encode('utf8')
            check = gdal.Open(fileName)

            if check is not None:
                file_paths = file_paths + [file]
            else:
                log.warn("WARNING: File " + fileName + " is not is not a valid GDAL decodable file. The import process will ignore this file.\n")

        return file_paths
