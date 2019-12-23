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

# Generate the C++ classes in the binary folder
set(PROTO_BIN_DIR "${CMAKE_CURRENT_BINARY_DIR}/messages")

# The .proto files used for generating the messages
set(PROTO_SRC protomessages/error.proto
        protomessages/health_service.proto)

# The .proto files used for generating services
set(GRPC_SRC protomessages/health_service.proto)

set(GENERATED_PROTO_SOURCES "")

foreach (PROTO_FILE ${PROTO_SRC})
    CompileProtobufFile(${PROTO_FILE} ${PROTO_BIN_DIR} GENERATED_PROTO_SOURCES)
endforeach ()


set(GENERATED_GRPC_SOURCES "")

foreach (GRPC_FILE ${GRPC_SRC})
    CompileGRPCFile(${GRPC_FILE} ${PROTO_BIN_DIR} GENERATED_GRPC_SOURCES)
endforeach ()

# Add the binery directory to the include path so that the build system
# can find the generated files.
include_directories(${CMAKE_CURRENT_BINARY_DIR})

add_library(common_grpc STATIC
        grpcutils.cc
        grpcutils.hh
        healthserviceimpl.cc
        healthserviceimpl.hh
        ${GENERATED_PROTO_SOURCES}
        ${GENERATED_GRPC_SOURCES})

find_package(LibDL REQUIRED)

target_link_libraries(common_grpc
    common_exceptions common_network common_logging
    ${GRPCPP_LIBRARY} ${GRPC_LIBRARY} ${PROTOBUF_LIBRARY} ${LIBDL_LIBRARIES} ${ZLIB_LIBRARIES})

if (${CMAKE_VERSION} GREATER_EQUAL "3.16.1")
target_precompile_headers(common_grpc REUSE_FROM common_logging)
endif()

install(TARGETS common_grpc
        DESTINATION ${LIB_DIR})
