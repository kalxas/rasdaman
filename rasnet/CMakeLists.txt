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

set(PROTO_SRC protomessages/client_rassrvr_service.proto
        protomessages/common_service.proto
        protomessages/rasmgr_client_service.proto
        protomessages/rasmgr_rasctrl_service.proto
        protomessages/rasmgr_rassrvr_service.proto
        protomessages/rassrvr_rasmgr_service.proto
        protomessages/rasmgr_rasmgr_service.proto)
set(GRPC_SRC ${PROTO_SRC})

set(PROTO_BIN_DIR "${CMAKE_CURRENT_BINARY_DIR}/messages")

set(GENERATED_PROTO_SOURCES "")

foreach (PROTO_FILE ${PROTO_SRC})
    CompileProtobufFile(${PROTO_FILE} ${PROTO_BIN_DIR} GENERATED_PROTO_SOURCES)
endforeach ()

set(GENERATED_GRPC_SOURCES "")

foreach (GRPC_FILE ${GRPC_SRC})
    CompileGRPCFile(${GRPC_FILE} ${PROTO_BIN_DIR} GENERATED_GRPC_SOURCES)
endforeach ()

add_library(rasnet STATIC
        ${PROTO_SRC} # Added to the list of files so that QtCreator sees them.
        ${GENERATED_PROTO_SOURCES}
        ${GENERATED_GRPC_SOURCES})

target_link_libraries(rasnet ${GRPCPP_LIBRARY} ${GRPC_LIBRARY} ${PROTOBUF_LIBRARY} ${CMAKE_THREAD_LIBS_INIT})

install(TARGETS rasnet
        DESTINATION ${LIB_DIR})
