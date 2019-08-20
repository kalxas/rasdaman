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

# TODO: We should refactor so that we have a folder for communication classes 
# (used by client and server) and one for client classes
set(SRC_FILES
    clientcomm.hh clientcomm.cc rpcif.h
    "${CMAKE_SOURCE_DIR}/rasnetprotocol/rasnetclientcomm.hh"
    "${CMAKE_SOURCE_DIR}/rasnetprotocol/rasnetclientcomm.cc")

set_source_files_properties(
    "${CMAKE_SOURCE_DIR}/rasnetprotocol/rasnetclientcomm.cc"
    PROPERTIES COMPILE_FLAGS ${DISABLE_PROTOBUF_WARNINGS_FLAGS})

set(RASNET_LIBRARIES
    rasnet common_exceptions common_crypto common_grpc
    common_logging common_time common_uuid common_file
    ${Boost_THREAD_LIBRARY})

add_library(clientcomm ${SRC_FILES})

target_include_directories(clientcomm PUBLIC ${CMAKE_CURRENT_BINARY_DIR})

target_link_libraries(clientcomm ${RASNET_LIBRARIES} ${OPENSSL_LIBRARIES})

install(TARGETS clientcomm DESTINATION ${LIB_DIR})

install(FILES clientcomm.hh DESTINATION "${INCLUDE_DIR}/rasodmg/clientcomm")
