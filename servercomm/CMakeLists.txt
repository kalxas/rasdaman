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
set(RNPROTOCOL_SOURCE_DIR "${CMAKE_SOURCE_DIR}/rnprotocol")
set(CLIENTCOMM_SOURCE_DIR "${CMAKE_SOURCE_DIR}/clientcomm")
set(CLIENTCOMM_BINARY_DIR "${CMAKE_BINARY_DIR}/clientcomm")

# Generate the RPC files in the current binary directory
set(RPCGEN_DIR "${CMAKE_CURRENT_BINARY_DIR}")

find_package(RpcGen REQUIRED)
find_package(AWK REQUIRED)

add_custom_command(OUTPUT "${RPCGEN_DIR}/rpcif.h"
        COMMAND ${CMAKE_COMMAND} -E remove -f "${RPCGEN_DIR}/rpcif.h.tmp"
        COMMAND ${CMAKE_COMMAND} -E copy_if_different "${CLIENTCOMM_SOURCE_DIR}/rpcif.x" "${RPCGEN_DIR}/rpcif.x"

        COMMAND ${RPCGEN_EXECUTABLE}
        ARGS -h -o "${RPCGEN_DIR}/rpcif.h.tmp" "${RPCGEN_DIR}/rpcif.x"

        COMMAND ${AWK_EXECUTABLE}
        ARGS -f "${CLIENTCOMM_SOURCE_DIR}/rpcif.h.awk" "${RPCGEN_DIR}/rpcif.h.tmp" > "${RPCGEN_DIR}/rpcif.h"

        COMMAND ${CMAKE_COMMAND} -E remove -f "${RPCGEN_DIR}/rpcif.h.tmp"

        DEPENDS "${CLIENTCOMM_SOURCE_DIR}/rpcif.x"
        WORKING_DIRECTORY ${CLIENTCOMM_SOURCE_DIR}
        )

add_custom_command(OUTPUT "${RPCGEN_DIR}/rpcif_svc.cc"
        COMMAND ${CMAKE_COMMAND} -E remove -f "${RPCGEN_DIR}/rpcif_svc.cc.tmp"
        COMMAND ${CMAKE_COMMAND} -E copy_if_different "${CLIENTCOMM_SOURCE_DIR}/rpcif.x" "${RPCGEN_DIR}/rpcif.x"

        COMMAND ${RPCGEN_EXECUTABLE}
        ARGS -m -o "${RPCGEN_DIR}/rpcif_svc.cc.tmp" "${RPCGEN_DIR}/rpcif.x"

        COMMAND ${AWK_EXECUTABLE}
        ARGS -f "${CLIENTCOMM_SOURCE_DIR}/rpcif_svc.c.awk" "${RPCGEN_DIR}/rpcif_svc.cc.tmp" > "${RPCGEN_DIR}/rpcif_svc.cc"

        COMMAND ${CMAKE_COMMAND} -E remove -f "${RPCGEN_DIR}/rpcif_svc.cc.tmp"

        DEPENDS "${CLIENTCOMM_SOURCE_DIR}/rpcif.x"
        WORKING_DIRECTORY ${CLIENTCOMM_SOURCE_DIR}
        )

add_custom_command(OUTPUT ${RPCGEN_DIR}/rpcif_xdr.c
        COMMAND ${CMAKE_COMMAND} -E copy_if_different "${CLIENTCOMM_SOURCE_DIR}/rpcif.x" "${RPCGEN_DIR}/rpcif.x"

        COMMAND ${RPCGEN_EXECUTABLE}
        ARGS -c -o "${RPCGEN_DIR}/rpcif_xdr.c" "${RPCGEN_DIR}/rpcif.x"

        DEPENDS "${CLIENTCOMM_SOURCE_DIR}/rpcif.x"

        WORKING_DIRECTORY ${CLIENTCOMM_SOURCE_DIR}
        )

set(SRC_FILES callbackmgr.hh callbackmgr.cc
        httpserver.hh httpserver.cc httpserver.icc
        manager.cc
        servercomm.hh servercomm.cc servercomm2.cc servercomm.icc
        "${RNPROTOCOL_SOURCE_DIR}/rnprotocol.hh" "${RNPROTOCOL_SOURCE_DIR}/rnprotocol.cc"
        "${RNPROTOCOL_SOURCE_DIR}/rnpembedded.hh" "${RNPROTOCOL_SOURCE_DIR}/rnpembedded.cc"
        "${RNPROTOCOL_SOURCE_DIR}/rnpcommunication.hh" "${RNPROTOCOL_SOURCE_DIR}/rnpcommunication.cc"
        "${RNPROTOCOL_SOURCE_DIR}/rnpserver.hh" "${RNPROTOCOL_SOURCE_DIR}/rnpserver.cc"
        "${RNPROTOCOL_SOURCE_DIR}/srvrasmgrcomm.hh" "${RNPROTOCOL_SOURCE_DIR}/srvrasmgrcomm.cc"
        "${RNPROTOCOL_SOURCE_DIR}/rnprasserver.hh" "${RNPROTOCOL_SOURCE_DIR}/rnprasserver.cc"
        "${RNPROTOCOL_SOURCE_DIR}/rnpservercomm.hh" "${RNPROTOCOL_SOURCE_DIR}/rnpservercomm.cc")

set(RASNET_LIBRARIES "")

# If rasnet is enabled, build the servercomm and add the libraries as requirements
if (RMANRASNET)
    set(SRC_FILES ${SRC_FILES}
            "${CMAKE_SOURCE_DIR}/rasnetprotocol/rasnetservercomm.hh"
            "${CMAKE_SOURCE_DIR}/rasnetprotocol/rasnetservercomm.cc")

    set(RASNET_LIBRARIES ${RASNET_LIBRARIES}
            rasnet common_exceptions common_grpc common_logging common_time common_uuid)
endif (RMANRASNET)

add_library(servercomm ${SRC_FILES}
        "${RPCGEN_DIR}/rpcif.h"
        "${RPCGEN_DIR}/rpcif_svc.cc"
        "${RPCGEN_DIR}/rpcif_xdr.c")

# Add the directories that contain files needed by this
target_include_directories(servercomm PUBLIC "${CMAKE_SOURCE_DIR}/mymalloc")
target_include_directories(servercomm PUBLIC "${CMAKE_SOURCE_DIR}/debug")
target_include_directories(servercomm PUBLIC "${RNPROTOCOL_SOURCE_DIR}")
target_include_directories(servercomm PUBLIC "${CMAKE_SOURCE_DIR}/rasnetprotocol")
target_include_directories(servercomm PUBLIC "${CMAKE_CURRENT_BINARY_DIR}")

target_include_directories(servercomm PUBLIC "${BASEDB_INCLUDE_DIR}")

target_link_libraries(servercomm ${RASNET_LIBRARIES})

install(TARGETS servercomm
        DESTINATION ${LIB_DIR})
