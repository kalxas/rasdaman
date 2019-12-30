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

set(COMPILED_SQL_SOURCES "")
CompileEmbeddedSQL(ecpg_lockmanager ${CMAKE_CURRENT_SOURCE_DIR} ${CMAKE_CURRENT_BINARY_DIR} COMPILED_SQL_SOURCES)

add_library(lockmanager lockmanager.hh lockmanager.cc
        ecpg_lockmanager.hh ${COMPILED_SQL_SOURCES})

target_include_directories(lockmanager PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})
target_include_directories(lockmanager PUBLIC ${BASEDB_INCLUDE_DIR})

target_link_libraries(lockmanager
    PRIVATE
    raslib
    tilemgr
    ${BASEDB_LIBRARIES})
