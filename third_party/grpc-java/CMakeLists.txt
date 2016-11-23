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
include_directories("${PROTOBUF_BINARY_DIR}/include")

add_executable(protoc-gen-grpc-java java_generator.cpp java_generator.h java_plugin.cpp)

# this order is important (libprotoc, libprotobuf) or it cannot link when creating protoc-gen-grpc-java
target_link_libraries(protoc-gen-grpc-java libprotoc.a libprotobuf.a )

# We can only build this executable when protobuf was compiled
add_dependencies(protoc-gen-grpc-java google-protobuf)


# Create the folder to store the compiled file
add_custom_command(TARGET protoc-gen-grpc-java POST_BUILD
 	    COMMAND ${CMAKE_COMMAND} -E make_directory "${THIRD_PARTY_DIR}/bin/grpc-java")
# Then copy this compiled binary to be used later
add_custom_command(
	TARGET protoc-gen-grpc-java POST_BUILD
	COMMAND ${CMAKE_COMMAND} -E copy
	        "${THIRD_PARTY_DIR}/grpc-java/protoc-gen-grpc-java"
	        "${THIRD_PARTY_DIR}/bin/grpc-java")






