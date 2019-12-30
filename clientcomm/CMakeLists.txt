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

set_source_files_properties(
    rasnetclientcomm.cc
    PROPERTIES COMPILE_FLAGS ${DISABLE_PROTOBUF_WARNINGS_FLAGS})

add_library(clientcomm
    clientcomm.hh clientcomm.cc
    rasnetclientcomm.hh rasnetclientcomm.cc
    )

target_include_directories(clientcomm PUBLIC ${CMAKE_CURRENT_BINARY_DIR})

target_link_libraries(clientcomm
    PUBLIC
    raslib
    rasnet
    common_grpc
    ${Boost_THREAD_LIBRARY}
    ${PROTOBUF_LIBRARY}
    
    PRIVATE
    rasodmg
    mymalloc_cln
    common_logging
    common_exceptions
    common_crypto
    common_uuid 
    ${Boost_SYSTEM_LIBRARY}
    ${GRPCPP_LIBRARY}
    ${GRPC_LIBRARY}
    ${ZLIB_LIBRARIES}
    )

if (${CMAKE_VERSION} GREATER_EQUAL "3.16.1")
target_precompile_headers(clientcomm REUSE_FROM common_logging)
endif()

install(TARGETS clientcomm DESTINATION ${LIB_DIR})
install(FILES clientcomm.hh DESTINATION "${INCLUDE_DIR}/clientcomm")
