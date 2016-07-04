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

# - Try to find libdl
# Once done this will define
#  TODO: Cleanup the code
#  LIBDL_FOUND - system has libdl
#  LIBDL_INCLUDE_DIRS - the libdl include directory
#  LIBDL_LIBRARIES - Link these to use libdl
#  LIBDL_NEEDS_UNDERSCORE - If extern "C" symbols are prefixed (BSD/Apple)
#
include(FindPackageHandleStandardArgs)

find_path(LIBDL_INCLUDE_DIRS NAMES dlfcn.h)
find_library(LIBDL_LIBRARIES NAMES dl)
include(FindPackageHandleStandardArgs)

FIND_PACKAGE_HANDLE_STANDARD_ARGS(LibDL DEFAULT_MSG
        LIBDL_LIBRARIES
        LIBDL_INCLUDE_DIRS)

SET(CMAKE_REQUIRED_LIBRARIES dl)
INCLUDE(CheckCSourceRuns)
CHECK_C_SOURCE_RUNS("#include <dlfcn.h>
#include <stdlib.h>
void testfunc() {}
int main() {
  testfunc();
  if (dlsym(0, \"_testfunc\") != (void*)0) {
    return EXIT_SUCCESS;
  } else {
    return EXIT_FAILURE;
  }
}" LIBDL_NEEDS_UNDERSCORE)

mark_as_advanced(LIBDL_INCLUDE_DIRS LIBDL_LIBRARIES LIBDL_NEEDS_UNDERSCORE)