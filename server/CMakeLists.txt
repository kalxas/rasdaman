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

add_definitions(${PNG_DEFINITIONS})
add_executable(rasserver createinitmdd.hh createinitmdd.cc
        rasserver_config.hh rasserver_config.cc
        rasserver_entry.hh rasserver_entry.cc
        rasserver_main.cc template_inst.hh)

find_package(OpenSSL REQUIRED)

target_include_directories(rasserver PUBLIC "${CMAKE_SOURCE_DIR}/debug" ${OPENSSL_INCLUDE_DIR})
target_link_libraries(rasserver servercomm qlparser conversion
        rasodmg raslib storagemgr reladminif tilemgr mddmgr
        catalogmgr relmddif relstorageif
        relcatalogif indexmgr relblobif relindexif relcatalogif
        commline lockmanager servercomm
        httpserver network
        ${OPENSSL_LIBRARIES} ${BASEDB_LIBRARIES})

if (RMANRASNET)
    target_link_libraries(rasserver rasnetserver rasnet)
endif (RMANRASNET)

install(TARGETS rasserver
        DESTINATION ${BIN_DIR})
