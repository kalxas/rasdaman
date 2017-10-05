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

#set(TEST_DUMMY_SOURCES)

#set(TEST_MOCK_SOURCES)

#set(TEST_UTIL_SOURCES util/testutil.hh)

set(TEST_SOURCES 
        clippingtests/clippinghelpfunctions.cc
        main_test.cc)

add_executable(clipping_test ${TEST_SOURCES})

find_package(Threads REQUIRED)
target_link_libraries(clipping_test
        raslib 
        qlparser
        mymalloc_cln
        ${Boost_LIBRARIES}
        ${GMOCK_LIBRARY}
        ${CMAKE_THREAD_LIBS_INIT})

add_test(clipping_testrunner clipping_test)

add_dependencies(unit-check clipping_test)


