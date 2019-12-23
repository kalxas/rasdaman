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
###################################################################

add_executable(rasql rasql.cc
        rasql_error.hh rasql_error.cc)

target_link_libraries(rasql
    rasodmg raslib clientcomm commline common_logging rasnet)

if (${CMAKE_VERSION} GREATER_EQUAL "3.16.1")
target_precompile_headers(rasql REUSE_FROM common_logging)
endif()

install(TARGETS rasql DESTINATION ${BIN_DIR})

add_subdirectory(test)
