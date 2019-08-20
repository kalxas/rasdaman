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
        time/timertest.cc main_test.cc
        grpc/grpcutilstest.cc
        grpc/healthserviceimpltest.cc
        grpc/healthserviceservertest.cc
        )

add_executable(common_test EXCLUDE_FROM_ALL ${TEST_SOURCES})

find_package(Threads REQUIRED)
target_link_libraries(common_test
        common_time
        common_grpc
        common_exceptions
        common_network
        common_logging
        ${GMOCK_LIBRARY}
        ${Boost_LIBRARIES}
        ${CMAKE_THREAD_LIBS_INIT})

add_test(common_testrunner common_test)

add_dependencies(unit-check common_test)
