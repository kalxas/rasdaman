#!/bin/bash

# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#

# Description: Compare source/src/main/resources/gml.tar.gz and build/src/main/resources/gml.tar.gz
# If they are different, then overide the one in build directory by the one from source directory.
# After that, untar this newly updated gml.tar.gz in build directory for new GML EPSG version directories.

# NOTE: This script is invoked by pom.xml only.

# exit on error from any command
set -e

SUCCESS=0
FAILED=1

if [ "$#" -ne 2 ]; then
    echo "Usage: bash script.sh PATH_TO_SOURCE/gml.tar.gz PATH_TO_BUILD/gml.tar.gz"
    exit "$SUCCESS"
fi

source_tar_file="$1"
build_tar_file="$2"

# Only run this function when build/gml.tar.gz has been changed
untar_file() {
    local build_tar_folder=$(dirname "${build_tar_file}")
    tar -zxf "$build_tar_file" --directory "$build_tar_folder"
}

if [ ! -f "$source_tar_file" ]; then
    echo "$source_tar_file not found."
    exit "$FAILED"
fi

# New build, empty folder
if [ ! -f "$build_tar_file" ]; then
    build_tar_folder=$(dirname "${build_tar_file}")
    mkdir -p "$build_tar_folder"
    cp "$source_tar_file" "$build_tar_file" && untar_file
else
    # Check if build/gml.tar.gz is as same as source/gml.tar.gz
    cmp --quiet "$source_tar_file" "$build_tar_file" || untar_file
fi

exit "$SUCCESS"


