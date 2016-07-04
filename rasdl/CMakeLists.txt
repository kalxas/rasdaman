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

find_package(FLEX REQUIRED)
find_package(BISON REQUIRED)

# -d generates token definitions in .h file
BISON_TARGET(rasdlParser odl.yy "${CMAKE_CURRENT_BINARY_DIR}/odl.cc"
        COMPILE_FLAGS "-d -y")

# -I gnererates an interactive scanner which doesn't try to look ahead past a newline
# -i generates a scanner which doesn't care about upper and lower case; doesn't work
FLEX_TARGET(rasdlScanner lex.ll "${CMAKE_CURRENT_BINARY_DIR}/lex.cc"
        COMPILE_FLAGS "-I")

ADD_FLEX_BISON_DEPENDENCY(rasdlScanner rasdlParser)

add_executable(rasdl ${BISON_rasdlParser_OUTPUTS} ${FLEX_rasdlScanner_OUTPUTS}
        lex.ll odl.yy
        parse.hh parse.cc
        symbtbl.hh symbtbl.cc
        alloca.cc rasdl.cc
        rasdl_error.hh rasdl_error.cc
        template_inst.hh yparse.hh)

target_include_directories(rasdl PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})
target_include_directories(rasdl PUBLIC ${CMAKE_CURRENT_BINARY_DIR})

target_link_libraries(rasdl reladminif relmddif
        relstorageif relindexif relcatalogif relblobif
        indexmgr catalogmgr storagemgr tilemgr commline raslib conversion
        rasodmg ${BASEDB_LIBRARIES})

install(TARGETS rasdl
        DESTINATION ${BIN_DIR})

#Types needed to run rasdl
install(FILES test/basictypes.dl
        DESTINATION "${SHARE_DATA_DIR}/examples/rasdl/")