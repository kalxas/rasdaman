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


############################################################################
# Begin Google Java RPC Compiler
############################################################################

if (ENABLE_JAVA)

    set(GRPC_JAVA_SOURCE_DIR "${CMAKE_SOURCE_DIR}/third_party/grpc-java/")
    set(GRPC_JAVA_BINARY_DIR "${CMAKE_BINARY_DIR}/third_party/bin/grpc-java/bin")

    set(GRPC_JAVA_PLUGIN_EXECUTABLE_PATH ${GRPC_JAVA_BINARY_DIR}/protoc-gen-grpc-java)
    set(GRPC_JAVA_PLUGIN_EXECUTABLE protoc-gen-grpc-java)

    add_custom_command(
        OUTPUT ${GRPC_JAVA_PLUGIN_EXECUTABLE_PATH}
        COMMAND ${CMAKE_COMMAND} ARGS -E make_directory "${GRPC_JAVA_BINARY_DIR}"
        COMMAND ${CMAKE_CXX_COMPILER} ARGS -std=c++11 -O2 -pthread -I${PROTOBUF_INCLUDE_DIR} ${GRPC_JAVA_SOURCE_DIR}/java_generator.cpp ${GRPC_JAVA_SOURCE_DIR}/java_generator.h ${GRPC_JAVA_SOURCE_DIR}/java_plugin.cpp -o ${GRPC_JAVA_PLUGIN_EXECUTABLE_PATH} ${PROTOBUF_BINARY_DIR}/lib/libprotoc.a ${PROTOBUF_BINARY_DIR}/lib/libprotobuf.a
        DEPENDS google-grpc
        COMMENT "Building GRPC Java plugin."
    )

    add_custom_target(${GRPC_JAVA_PLUGIN_EXECUTABLE}
        DEPENDS ${GRPC_JAVA_PLUGIN_EXECUTABLE_PATH})

    set_property(TARGET ${GRPC_JAVA_PLUGIN_EXECUTABLE} PROPERTY IMPORTED_LOCATION
            ${GRPC_JAVA_PLUGIN_EXECUTABLE_PATH})

endif (ENABLE_JAVA)




