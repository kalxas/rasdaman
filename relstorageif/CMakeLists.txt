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

# The name of the SQL sources files without extension
set(EMBEDDED_SQL_SOURCES dbstoragelayout)

set(COMPILED_SQL_SOURCES "")

foreach (FILE ${EMBEDDED_SQL_SOURCES})
    CompileEmbeddedSQL(${FILE} ${CMAKE_CURRENT_SOURCE_DIR} ${CMAKE_CURRENT_BINARY_DIR} COMPILED_SQL_SOURCES)
endforeach ()

add_library(relstorageif ${COMPILED_SQL_SOURCES}
        dbstoragelayout.hh
        empty.cc
        storageid.hh)

target_include_directories(relstorageif PUBLIC ${BASEDB_INCLUDE_DIR})
target_include_directories(relstorageif PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})

target_link_libraries(relstorageif
    PUBLIC
#    reladminif
#    relcatalogif
    
    PRIVATE
    storagemgr
#    common_logging
#    mymalloc_svc
#    raslib
    )

if (${CMAKE_VERSION} GREATER_EQUAL "3.16.1")
target_precompile_headers(relstorageif REUSE_FROM common_logging)
endif()
