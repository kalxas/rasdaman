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
set(EMBEDDED_SQL_SOURCES adminif
        databaseif
        objectbroker
        oidif
        sqlerror
        transactionif)

set(COMPILED_SQL_SOURCES "")

foreach (FILE ${EMBEDDED_SQL_SOURCES})
    CompileEmbeddedSQL(${FILE} ${CMAKE_CURRENT_SOURCE_DIR} ${CMAKE_CURRENT_BINARY_DIR} COMPILED_SQL_SOURCES)
endforeach ()

add_library(reladminif ${COMPILED_SQL_SOURCES}
        adminif.hh
        adminifcommon.cc
        binaryrepresentation.hh
        databaseif.hh
        databaseifcommon.cc
        dbnamedobject.hh dbnamedobject.cc
        dbobject.hh dbobject.cc
        dbobjectiditerator.hh dbobjectiditerator.cc
        dbobjectiterator.hh dbobjectiterator.cc
        dbref.hh dbref.cc
        destroyable.hh
        eoid.hh eoid.cc
        externs.h
        lists.h
        objectbroker.hh
        objectbrokercommon.cc
        oidif.hh
        oidifcommon.cc
        sqlerror.hh
        sqlglobals.h
        sqlitewrapper.hh sqlitewrapper.cc
        transactionif.hh
        transactionifcommon.cc)

target_include_directories(reladminif PUBLIC ${BASEDB_INCLUDE_DIR})
target_include_directories(reladminif PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})

target_link_libraries(reladminif mymalloc_svc ${BASEDB_LIBRARIES})
install(TARGETS reladminif
        DESTINATION ${LIB_DIR})

install(DIRECTORY db_updates
        DESTINATION ${SHARE_DATA_DIR})
