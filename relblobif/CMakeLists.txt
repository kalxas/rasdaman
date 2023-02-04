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
set(EMBEDDED_SQL_SOURCES blobtile)

set(COMPILED_SQL_SOURCES "")

foreach (FILE ${EMBEDDED_SQL_SOURCES})
    CompileEmbeddedSQL(${FILE} ${CMAKE_CURRENT_SOURCE_DIR} ${CMAKE_CURRENT_BINARY_DIR} COMPILED_SQL_SOURCES)
endforeach ()

set(SQLITE_SRC "")

if (BASEDB_SQLITE)
  set(SQLITE_SRC
      blobfs.cc blobfs.hh
      dirwrapper.cc dirwrapper.hh
      blobfstransaction.cc blobfstransaction.hh
      blobfstransactionlock.cc blobfstransactionlock.hh
      blobfile.cc blobfile.hh
      lockfile.cc lockfile.hh
      )
endif(BASEDB_SQLITE)

add_library(relblobif
    ${COMPILED_SQL_SOURCES}
    ${SQLITE_SRC}
    blobfscommon.hh
    blobtile.hh
    blobtilecommon.cc
    dbtile.hh dbtile.cc
    dirwrapper.hh dirwrapper.cc
    inlinetile.hh inlinetile.cc
    tileid.hh
    )

target_include_directories(relblobif PUBLIC ${BASEDB_INCLUDE_DIR})
target_include_directories(relblobif PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})

target_link_libraries(relblobif
    PUBLIC
    raslib
#    reladminif
    
    PRIVATE
#    relindexif
#    relcatalogif
#    relstorageif
    ${BASEDB_LIBRARIES}
    ${FMT_LIBRARY}
    )

if (${CMAKE_VERSION} GREATER_EQUAL "3.16.1")
target_precompile_headers(relblobif REUSE_FROM common_logging)
endif()

add_subdirectory(test)
