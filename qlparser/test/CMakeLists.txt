#CMakeLists.txt for the test directory

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


set(TEST_SOURCES 
        clippingtests/clippinghelpfunctions.cc
        ../../applications/rasql/rasql_error.cc
        main_test.cc)

add_executable(clipping_test EXCLUDE_FROM_ALL ${TEST_SOURCES})

find_package(Threads REQUIRED)
target_link_libraries(clipping_test
        servercomm qlparser conversion rasserver_lib
        rasodmg raslib storagemgr reladminif tilemgr mddmgr
        catalogmgr relmddif relstorageif
        relcatalogif indexmgr relblobif relindexif
        commline relcatalogif lockmanager servercomm
        mymalloc_svc common_logging clientcomm relcatalogif
        rasnetserver rasnet
        ${OPENSSL_LIBRARIES} ${BASEDB_LIBRARIES}
        ${GMOCK_LIBRARY}
        ${CMAKE_THREAD_LIBS_INIT})

add_test(clipping_testrunner clipping_test)

add_dependencies(unit-check clipping_test)
