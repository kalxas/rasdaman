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

add_executable(directql directql.cc
        directql_error.hh directql_error.cc
        template_inst.hh
        ../../server/rasserver_config.hh
        ../../server/rasserver_config.cc
        )

target_link_libraries(directql rasodmg clientcomm
        commline network relcatalogif relindexif
        reladminif catalogmgr relcatalogif relstorageif storagemgr
        relindexif indexmgr tilemgr relblobif relcatalogif
        servercomm mddmgr relmddif indexmgr qlparser catalogmgr lockmanager
        tilemgr reladminif conversion raslib common_logging ${BASEDB_LIBRARIES} ${ELPP_LIBRARY})

if (RMANRASNET)
    target_link_libraries(directql rasnet)
endif ()

install(TARGETS directql
        DESTINATION ${BIN_DIR})
