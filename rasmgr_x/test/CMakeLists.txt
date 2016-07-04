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

set(TEST_DUMMY_SOURCES dummies/dummyrasmgrservice.hh)

set(TEST_MOCK_SOURCES mocks/clientmanagermock.hh
        mocks/databasehostmgrmock.hh
        mocks/mockaccessrights.hh
        mocks/mockrasserver.hh
        mocks/peermanagermock.hh
        mocks/serverfactorymock.hh
        mocks/servergroupfactorymock.hh
        mocks/servergroupmock.hh
        mocks/servermanagermock.hh
        mocks/usermanagermock.hh)

set(TEST_UTIL_SOURCES util/testutil.hh)

set(TEST_SOURCES ${TEST_DUMMY_SOURCES} ${TEST_MOCK_SOURCES} ${TEST_UTIL_SOURCES}
        useradminrightstest.cc
        userdatabaserightstest.cc
        usertest.cc
        usermanagertest.cc
        clienttest.cc
        clientmanagertest.cc
        databasetest.cc
        databasehosttest.cc
        databasehostmanagertest.cc
        databasemanagertest.cc
        peermanagertest.cc
        outpeertest.cc
        rasservertest.cc
        servergrouptest.cc
        servermanagertest.cc
        clientmanagementservicetest.cc
        main_test.cc)

# Include the binary directory where the protobuf files were generated
add_executable(rasmgr_test EXCLUDE_FROM_ALL ${TEST_SOURCES})

target_link_libraries(rasmgr_test rasmgr_lib ${GMOCK_LIBRARY})

add_test(rasmgr_testrunner rasmgr_test)

add_dependencies(unit-check rasmgr_test)