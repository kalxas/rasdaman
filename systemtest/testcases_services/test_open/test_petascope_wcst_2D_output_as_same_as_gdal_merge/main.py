#!/usr/bin/env python

# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003 - 2018 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

import os 
import glob
import collections
import subprocess
import sys
import shutil

from shutil import copyfile
from osgeo import gdal   

dir_path = os.path.dirname(os.path.realpath(__file__))

prog="main.py: "

###### configurations
petascope_endpoint = sys.argv[1]
coverage_id = "test_overlapping_map_mosaic"

RASADMIN_USER = sys.argv[2]
RASADMIN_PASS = sys.argv[3]
RASADMIN_CREDENTIALS_FILE = sys.argv[4]

ingredient_file = dir_path + "/ingest.json"

input_files_path = dir_path + "/" + "testdata"
tmp_folder = dir_path + "/" + "tmp"

merged_file_prefix = "merged_"
# all input files are merged by gdal_merge.py to this file
final_merged_file = ""
# output from rasdaman after all input files are imported
output_file = tmp_folder + "/" + "output.tif"
ingredient_file_tmp = tmp_folder + "/" + "ingest.json"

###### main process

# Creat tmp folder if not exists
if not os.path.exists(tmp_folder):
    os.makedirs(tmp_folder)

list_files = []
# Collect all the input files to a list
for f in glob.glob(input_files_path + "/*.tif"):
    list_files.append(f)
list_files = collections.deque(list_files)

print(prog + "testing results from gdal_merge.py and petascope WCS-T ...")

# Rotate the list of files to import and check with result from gdal_merge
for x in range(len(list_files)):
    print(prog + "testing import files with rotation number: " + str(x + 1) + " ...")
    # Clean tmp folder
    for f in glob.glob(tmp_folder + "/*.*"):
        os.unlink(f)

    # Then, import all files in this rotation
    for i in range(len(list_files)):

        if i < len(list_files) - 1:
            tmp_file = tmp_folder + "/" + merged_file_prefix + str(i) + ".tiff"
            final_merged_file = tmp_file

            if i == 0:
                file_1 = list_files[i]
            else:
                # input is a gdal_merged file
                file_1 = tmp_folder + "/" + merged_file_prefix + str(i - 1) + ".tiff"
            file_2 = list_files[i + 1]

            subprocess.call("gdal_merge.py -o " + tmp_file + " " + file_1 + " " + file_2, shell=True, stdout=open(os.devnull, 'wb'))

        # Copy ingredient files to this tmp folder
        copyfile(ingredient_file, tmp_folder + "/" + "ingest.json") 
        
        # Copy files to this tmp folder to be used for WCST_Import
        src_file = list_files[i]
        dst_file = tmp_folder + "/input_" + str(i + 1) + ".tif"
        copyfile(src_file, dst_file)

    # When everything is done, now import files with WCST_Import and check the result from GetCoverage request with gdal_merge.py
    subprocess.call("wcst_import.sh -i " + RASADMIN_CREDENTIALS_FILE + " -q " + ingredient_file_tmp, shell=True, stdout=open(os.devnull, 'wb'))

    get_coverage_request = petascope_endpoint + "?SERVICE=WCS&VERSION=2.0.1&REQUEST=GetCoverage&COVERAGEID=" + coverage_id + "&FORMAT=image/tiff"
    subprocess.call("wget --auth-no-challenge --user '" + RASADMIN_USER + "' --password '" + RASADMIN_PASS + "' -q '" + get_coverage_request + "' -O " + output_file, shell=True, stdout=open(os.devnull, 'wb'))

    # Now compare 2 files file
    process = subprocess.Popen(['gdalinfo', final_merged_file], stdout=subprocess.PIPE)
    src_stdout = process.communicate()[0]

    process = subprocess.Popen(['gdalinfo', output_file], stdout=subprocess.PIPE)
    dst_stdout = process.communicate()[0]

    src_tmp = src_stdout.splitlines()
    dst_tmp = dst_stdout.splitlines()

    # now delete the imported coverage and check the result
    delete_coverage_request = petascope_endpoint + "?SERVICE=WCS&VERSION=2.0.1&REQUEST=DeleteCoverage&COVERAGEID=" + coverage_id
    subprocess.call("wget --auth-no-challenge --user '" + RASADMIN_USER + "' --password '" + RASADMIN_PASS + "' -q '" + delete_coverage_request + "' -O /dev/null", shell=True, stdout=open(os.devnull, 'wb'))

    if src_tmp[2] != dst_tmp[2]:
        print(prog + "Size is different, gdal_merge: " + src_tmp[2] + ", petascope: " + dst_tmp[2] + ".")
        exit(1)
    # output from petascope containing nodata_value in tiff metadata while gdal_merge doesn't, 
    # hence different indices in string array.
    elif src_tmp[19] != dst_tmp[20]:
        print(prog + "Upper Left is different, gdal_merge: " + src_tmp[19] + ", petascope: " + dst_tmp[20] + ".")
        exit(1)
    elif src_tmp[20] != dst_tmp[21]:
        print(prog + "Lower Left is different, gdal_merge: " + src_tmp[20] + ", petascope: " + dst_tmp[21] + ".")
        exit(1)
    elif src_tmp[21] != dst_tmp[22]:
        print(prog + "Upper Right is different, gdal_merge: " + src_tmp[21] + ", petascope: " + dst_tmp[22] + ".")
        exit(1)
    elif src_tmp[22] != dst_tmp[23]:
        print(prog + "Lower Right is different, gdal_merge: " + src_tmp[22] + ", petascope: " + dst_tmp[23] + ".")
        exit(1)

    # Finally, rotate the list_files and continue testing different file combinations
    list_files.rotate(1)

# No difference from gdal_merge.py for all rotations, clean tmp folder and returns success
shutil.rmtree(tmp_folder)
exit(0)
