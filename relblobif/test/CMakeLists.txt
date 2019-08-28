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

set(TEST_SOURCES time/timertest.cc main_test.cc)

macro(AddRelblobifTest TEST_NAME)

    # ignore -Wwrite-strings warning
    set_source_files_properties(test_${TEST_NAME}.cc PROPERTIES COMPILE_FLAGS " -Wno-write-strings ")

    add_executable(${TEST_NAME}_test EXCLUDE_FROM_ALL test_${TEST_NAME}.cc testing.cc)
    target_link_libraries(${TEST_NAME}_test 
        rasodmg clientcomm raslib conversion commline
        relstorageif storagemgr relmddif mddmgr
        relindexif indexmgr relcatalogif 
        tilemgr catalogmgr relblobif reladminif
    )

    add_test(${TEST_NAME}_testrunner ${TEST_NAME}_test)
    # run on make unit-check
    add_dependencies(unit-check ${TEST_NAME}_test)

endmacro()

AddRelblobifTest(blobfs)
AddRelblobifTest(blobfstransactionlock)
AddRelblobifTest(blobfstransaction)
AddRelblobifTest(blobfile)
