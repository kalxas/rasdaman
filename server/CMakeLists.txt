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


add_library(rasserver_lib
    rasserver_config.hh rasserver_config.cc
    rasserver_entry.hh rasserver_entry.cc
    createinitmdd.hh createinitmdd.cc
    )
target_link_libraries(rasserver_lib
    PUBLIC commline rasnet
    )

add_definitions(${PNG_DEFINITIONS})
add_executable(rasserver
        rasserver_main.cc template_inst.hh
        rasserver_rasdl.hh rasserver_rasdl.cc
        rasserver_directql.cc rasserver_directql.hh
        ../applications/rasql/rasql_error.cc
)

target_include_directories(rasserver PUBLIC "${CMAKE_SOURCE_DIR}/debug" ${OPENSSL_INCLUDE_DIR})

target_link_libraries(rasserver rasserver_lib
        servercomm qlparser conversion
        rasodmg raslib storagemgr reladminif tilemgr mddmgr
        catalogmgr relmddif relstorageif
        relcatalogif indexmgr relblobif relindexif
        commline relcatalogif lockmanager servercomm
        mymalloc_svc common_logging clientcomm relcatalogif
        rasnetserver rasnet
        ${OPENSSL_LIBRARIES} ${BASEDB_LIBRARIES})


if (${CMAKE_VERSION} GREATER_EQUAL "3.16.1")
target_precompile_headers(rasserver_lib REUSE_FROM common_logging)
target_precompile_headers(rasserver REUSE_FROM common_logging)
endif()

install(TARGETS rasserver
        DESTINATION ${BIN_DIR})
        
install(CODE "execute_process(COMMAND ln -sf 
    ${BIN_DIR}/rasserver ${BIN_DIR}/directql)")
