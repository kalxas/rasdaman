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
# Copyright 2003-2016 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

#
# Find the HDF4 includes and get all installed hdf4 library settings from
# HDF4-config.cmake file : Requires a CMake compatible hdf-4.2.6 or later
# for this feature to work. The following vars are set if hdf4 is found.
#
# HDF4_FOUND               - True if found, otherwise all other vars are undefined
# HDF4_INCLUDE_DIR         - The include dir for main *.h files
# HDF4_LIBRARIES
include(FindPackageHandleStandardArgs)

# The HINTS option should only be used for values computed from the system.
set(_HDF4_HINTS
        $ENV{HOME}/.local
        $ENV{HDF4_ROOT}
        $ENV{HDF4_ROOT_DIR_HINT})

# Hard-coded guesses should still go in PATHS. This ensures that the user
# environment can always override hard guesses.
set(_HDF4_PATHS
        $ENV{HOME}/.local
        $ENV{HDF4_ROOT}
        $ENV{HDF4_ROOT_DIR_HINT}
        /usr/lib
        /usr/share
        /usr/include
        /usr/local)

set(_HDF4_PATH_SUFFIXES hdf)

find_path(HDF4_INCLUDE_DIRS "hdf.h"
        HINTS ${_HDF4_HINTS}
        PATHS ${_HDF4_PATHS}
        PATH_SUFFIXES ${_HDF4_PATH_SUFFIXES})

find_path(MFHDF4_INCLUDE_DIRS "mfhdf.h"
        HINTS ${_HDF4_HINTS}
        PATHS ${_HDF4_PATHS}
        PATH_SUFFIXES ${_HDF4_PATH_SUFFIXES})

find_library(LIBDF NAMES df dfalt PATHS ${_HDF4_PATHS})
find_library(LIBMFHDF NAMES mfhdf mfhdfalt PATHS ${_HDF4_PATHS})

if (LIBDF AND LIBMFHDF)
    set(HDF4_LIBRARIES ${LIBDF} ${LIBMFHDF})
else ()
    message(FATAL_ERROR "USE_HDF4 was given but libhdf was not found! Please install libhdf or libhdf-alt.")
endif ()

set(HDF4_INCLUDE_DIR "${HDF4_INCLUDE_DIRS}")

# handle the QUIETLY and REQUIRED arguments and set HDF4_FOUND to TRUE if
# all listed variables are TRUE
include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(HDF4 REQUIRED_VARS HDF4_LIBRARIES HDF4_INCLUDE_DIRS MFHDF4_INCLUDE_DIRS)