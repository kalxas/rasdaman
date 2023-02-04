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

# rasnetservercomm

add_library(rasnetservercomm
        rasnetservercomm.hh rasnetservercomm.cc
        )

target_link_libraries(rasnetservercomm
    PUBLIC
    rasnetserver
    rasnet
    raslib
    
    PRIVATE
    common_grpc
    common_uuid
    rasserver_lib
    mymalloc_svc
    # qlparser
    )

if (${CMAKE_VERSION} GREATER_EQUAL "3.16.1")
target_precompile_headers(rasnetservercomm REUSE_FROM common_logging)
endif()

# servercomm

set(SRC_FILES
    cliententry.cc cliententry.hh
    accesscontrol.cc accesscontrol.hh
    httpserver.hh httpserver.cc
    servercomm.hh servercomm.cc)

add_library(servercomm ${SRC_FILES})

target_link_libraries(servercomm
    PUBLIC
    raslib
    reladminif
    
    PRIVATE
    rasnetservercomm
    rasserver_lib
    rasnet
    mymalloc_svc
    qlparser
    lockmanager
    storagemgr
    tilemgr
    mddmgr
    relcatalogif
    common_exceptions
    common_grpc
    common_logging
    common_time
    common_uuid
    common_string
    )

if (ENABLE_PROFILING)
    target_link_libraries(servercomm PRIVATE profiler tcmalloc)
endif ()

if (${CMAKE_VERSION} GREATER_EQUAL "3.16.1")
target_precompile_headers(servercomm REUSE_FROM common_logging)
endif()
