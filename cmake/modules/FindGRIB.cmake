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

include(FindPackageHandleStandardArgs)
include(CheckLibraryExists)

find_library(GRIB_LIBRARIES NAMES grib_api)
find_path(GRIB_INCLUDE_DIR NAMES grib_api.h)

# since there's no grib_api.pc let's check if this installation of grib required jasper and jpeg
set(CMAKE_REQUIRED_LIBRARIES m)

check_library_exists(${GRIB_LIBRARIES} grib_index_new_from_file "" GRIB_COMPILES)
if(GRIB_COMPILES)
	find_package_handle_standard_args(GRIB DEFAULT_MSG GRIB_LIBRARIES GRIB_INCLUDE_DIR)
endif(GRIB_COMPILES)

set(CMAKE_REQUIRED_LIBRARIES)

mark_as_advanced(GRIB_LIBRARIES GRIB_INCLUDE_DIR)
