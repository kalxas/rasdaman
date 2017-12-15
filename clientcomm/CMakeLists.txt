#
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
# Copyright 2003-2015 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
###################################################################

# Directory in which we will generate the RPC files.
set(RPCGEN_DIR ${CMAKE_CURRENT_BINARY_DIR})

add_custom_command(OUTPUT "${RPCGEN_DIR}/rpcif.h"
    COMMAND ${CMAKE_COMMAND} -E remove -f "${RPCGEN_DIR}/rpcif.h.tmp"
    COMMAND ${CMAKE_COMMAND} -E copy_if_different rpcif.x "${RPCGEN_DIR}/rpcif.x"

    COMMAND ${RPCGEN_EXECUTABLE}
    ARGS -h -o "${RPCGEN_DIR}/rpcif.h.tmp" "${RPCGEN_DIR}/rpcif.x"

    COMMAND ${AWK_EXECUTABLE}
    ARGS -f rpcif.h.awk "${RPCGEN_DIR}/rpcif.h.tmp" > "${RPCGEN_DIR}/rpcif.h"

    COMMAND ${CMAKE_COMMAND} -E remove -f "${RPCGEN_DIR}/rpcif.h.tmp"

    DEPENDS rpcif.x
    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
    )

add_custom_command(OUTPUT "${RPCGEN_DIR}/rpcif_clnt.cc"
    COMMAND ${CMAKE_COMMAND} -E remove -f "${RPCGEN_DIR}/rpcif_clnt.cc.tmp"
    COMMAND ${CMAKE_COMMAND} -E copy_if_different rpcif.x "${RPCGEN_DIR}/rpcif.x"

    COMMAND ${RPCGEN_EXECUTABLE}
    ARGS -l -o "${RPCGEN_DIR}/rpcif_clnt.cc.tmp" "${RPCGEN_DIR}/rpcif.x"

    COMMAND ${AWK_EXECUTABLE}
    ARGS -f rpcif_clnt.c.awk "${RPCGEN_DIR}/rpcif_clnt.cc.tmp" > "${RPCGEN_DIR}/rpcif_clnt.cc"

    COMMAND ${CMAKE_COMMAND} -E remove -f "${RPCGEN_DIR}/rpcif_clnt.cc.tmp"

    DEPENDS rpcif.x
    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
    )

add_custom_command(OUTPUT "${RPCGEN_DIR}/rpcif_xdr.cc"
    COMMAND ${CMAKE_COMMAND} -E remove -f "${RPCGEN_DIR}/rpcif_xdr.cc.tmp"
    COMMAND ${CMAKE_COMMAND} -E copy_if_different rpcif.x "${RPCGEN_DIR}/rpcif.x"

    COMMAND ${RPCGEN_EXECUTABLE}
    ARGS -c -o "${RPCGEN_DIR}/rpcif_xdr.cc.tmp" "${RPCGEN_DIR}/rpcif.x"

    COMMAND ${AWK_EXECUTABLE}
    ARGS -f rpcif_xdr.cc.awk "${RPCGEN_DIR}/rpcif_xdr.cc.tmp" > "${RPCGEN_DIR}/rpcif_xdr.cc"

    COMMAND ${CMAKE_COMMAND} -E remove -f "${RPCGEN_DIR}/rpcif_xdr.cc.tmp"

    DEPENDS rpcif.x

    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
    )

set(RNPROTOCOL_SOURCE_DIR "${CMAKE_SOURCE_DIR}/rnprotocol")

set(RASNET_LIBRARIESS "")
set(SRC_FILES "")

# TODO: We should refactor so that we have a folder for communication classes 
# (used by client and server) and one for client classes
if (RMANRASNET)
    set(SRC_FILES ${SRC_FILES}
        "${CMAKE_SOURCE_DIR}/rasnetprotocol/rasnetclientcomm.hh"
        "${CMAKE_SOURCE_DIR}/rasnetprotocol/rasnetclientcomm.cc")

    set_source_files_properties("${CMAKE_SOURCE_DIR}/rasnetprotocol/rasnetclientcomm.cc" 
        PROPERTIES COMPILE_FLAGS ${DISABLE_PROTOBUF_WARNINGS_FLAGS})

    set(RASNET_LIBRARIESS ${RASNET_LIBRARIESS}
        rasnet common_exceptions common_crypto common_grpc
        common_logging common_time common_uuid
        ${Boost_THREAD_LIBRARY})
endif ()

set(SRC_FILES ${SRC_FILES}
    "${RPCGEN_DIR}/rpcif.h"
    "${RPCGEN_DIR}/rpcif_clnt.cc"
    "${RPCGEN_DIR}/rpcif_xdr.cc"

    clientcomm.hh clientcomm.cc clientcomm.icc
    rpcclientcomm.hh rpcclientcomm.cc

    "${RNPROTOCOL_SOURCE_DIR}/rnprotocol.hh" "${RNPROTOCOL_SOURCE_DIR}/rnprotocol.cc"
    "${RNPROTOCOL_SOURCE_DIR}/rnpembedded.hh" "${RNPROTOCOL_SOURCE_DIR}/rnpembedded.cc"
    "${RNPROTOCOL_SOURCE_DIR}/rnpcommunication.hh" "${RNPROTOCOL_SOURCE_DIR}/rnpcommunication.cc"
    "${RNPROTOCOL_SOURCE_DIR}/rnpclientcomm.hh" "${RNPROTOCOL_SOURCE_DIR}/rnpclientcomm.cc" 
    "${RNPROTOCOL_SOURCE_DIR}/rnpclientcomm2.cc"
    "${RNPROTOCOL_SOURCE_DIR}/rnprasserver.hh" "${RNPROTOCOL_SOURCE_DIR}/rnprasserver.cc")


add_library(clientcomm ${SRC_FILES})

target_include_directories(clientcomm PUBLIC "${CMAKE_SOURCE_DIR}/mymalloc")
target_include_directories(clientcomm PUBLIC ${RNPROTOCOL_SOURCE_DIR})
target_include_directories(clientcomm PUBLIC ${CMAKE_CURRENT_BINARY_DIR})

target_link_libraries(clientcomm mymalloc_cln ${RASNET_LIBRARIESS} ${OPENSSL_LIBRARIES})

install(TARGETS clientcomm DESTINATION ${LIB_DIR})
