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

# - Find NETPBM
# Find the native NETPBM includes and library
# This module defines
#  NETPBM_INCLUDE_DIR, where to find jpeglib.h, etc.
#  NETPBM_LIBRARIES, the libraries needed to use NETPBM.
#  NETPBM_FOUND, If false, do not try to use NETPBM.
# also defined, but not for general use are
#  NETPBM_LIBRARY, where to find the NETPBM library.

include(FindPackageHandleStandardArgs)


find_path(NETPBM_INCLUDE_DIR pam.h
        /usr/local/include
        /usr/include
        /usr/include/netpbm)

set(NETPBM_NAMES ${NETPBM_NAMES} netpbm)
find_library(NETPBM_LIBRARY
        NAMES ${NETPBM_NAMES}
        PATHS /usr/lib /usr/local/lib /lib /lib64)

find_package_handle_standard_args(NETPBM DEFAULT_MSG NETPBM_INCLUDE_DIR NETPBM_LIBRARY)
mark_as_advanced(NETPBM_LIBRARY NETPBM_INCLUDE_DIR)