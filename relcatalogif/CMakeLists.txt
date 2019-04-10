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
set(EMBEDDED_SQL_SOURCES
    dbminterval dbnullvalues
    mddbasetype mdddimensiontype mdddomaintype mddtype
    settype structtype
    )

set(COMPILED_SQL_SOURCES "")

foreach (FILE ${EMBEDDED_SQL_SOURCES})
    CompileEmbeddedSQL(${FILE} ${CMAKE_CURRENT_SOURCE_DIR} ${CMAKE_CURRENT_BINARY_DIR} COMPILED_SQL_SOURCES)
endforeach ()

add_library(relcatalogif ${COMPILED_SQL_SOURCES}
    alltypes.hh
    atomictype.C atomictype.hh
    basetype.C basetype.hh
    booltype.C booltype.hh
    chartype.C chartype.hh
    collectiontype.C collectiontype.hh
    complextype.hh complextype.icc
    compositetype.C compositetype.hh
    dbminterval.hh
    dbnullvalues.hh
    doubletype.C doubletype.hh
    floattype.C floattype.hh
    inlineminterval.cc inlineminterval.hh
    integraltype.hh integraltype.icc
    longtype.C longtype.hh
    mddbasetype.hh
    mddbasetypecommon.cc
    mdddimensiontype.hh mdddimensiontypecommon.cc
    mdddomaintype.hh mdddomaintypecommon.cc
    mddtype.hh mddtypecommon.cc
    octettype.C octettype.hh
    realtype.hh realtype.icc
    settype.hh settypecommon.cc
    shorttype.C shorttype.hh
    structtype.hh structtype.icc structtypecommon.cc
    type.C type.hh
    typeiterator.hh
    uintegraltype.hh uintegraltype.icc
    ulongtype.C ulongtype.hh
    ushorttype.C ushorttype.hh
    )

target_include_directories(relcatalogif PUBLIC ${BASEDB_INCLUDE_DIR})
target_include_directories(relcatalogif PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})

target_link_libraries(relcatalogif PUBLIC
        relmddif reladminif raslib)

install(TARGETS relcatalogif
        DESTINATION ${LIB_DIR})
