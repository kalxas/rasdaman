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

# These are folders that contain files used to build this library (generated files or source files)
set(CLIENTCOMM_SOURCE_DIR "${CMAKE_SOURCE_DIR}/clientcomm")
set(CLIENTCOMM_BINARY_DIR "${CMAKE_BINARY_DIR}/clientcomm")

set(SRC_FILES
    cliententry.cc cliententry.hh
    accesscontrol.cc accesscontrol.hh
    httpserver.hh httpserver.cc
    servercomm.hh servercomm.cc
    "${CMAKE_SOURCE_DIR}/rasnetprotocol/rasnetservercomm.hh"
    "${CMAKE_SOURCE_DIR}/rasnetprotocol/rasnetservercomm.cc")

add_library(servercomm ${SRC_FILES})

target_link_libraries(servercomm
    rasnet common_exceptions common_grpc common_logging common_time common_uuid)

if (ENABLE_PROFILING)
    target_link_libraries(servercomm profiler tcmalloc)
endif ()

if (${CMAKE_VERSION} GREATER_EQUAL "3.16.1")
target_precompile_headers(servercomm REUSE_FROM common_logging)
endif()

install(TARGETS servercomm DESTINATION ${LIB_DIR})
