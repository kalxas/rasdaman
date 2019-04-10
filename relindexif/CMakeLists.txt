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
set(EMBEDDED_SQL_SOURCES dbrcindexds dbtcindex hierindex)

set(COMPILED_SQL_SOURCES "")

foreach (FILE ${EMBEDDED_SQL_SOURCES})
    CompileEmbeddedSQL(${FILE} ${CMAKE_CURRENT_SOURCE_DIR} ${CMAKE_CURRENT_BINARY_DIR} COMPILED_SQL_SOURCES)
endforeach ()

add_library(relindexif ${COMPILED_SQL_SOURCES}
    dbrcindexds.hh dbrcindexdscommon.cc
    dbtcindex.hh dbtcindexcommon.cc
    hierindex.hh hierindexcommon.cc
    indexid.hh
    )

target_include_directories(relindexif PUBLIC ${BASEDB_INCLUDE_DIR})
target_include_directories(relindexif PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})

target_link_libraries(relindexif PUBLIC
    reladminif
    relcatalogif
    relblobif
    relstorageif
    )

install(TARGETS relindexif
        DESTINATION ${LIB_DIR})
