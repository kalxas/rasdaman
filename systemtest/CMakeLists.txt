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

set(TEST_SEQUENCE "testcases_mandatory/* ")
if (ENABLE_JAVA)
	set(TEST_SEQUENCE "testcases_mandatory/* testcases_services/*")
endif()

configure_file("${CMAKE_CURRENT_SOURCE_DIR}/run_test.sh.in" "${CMAKE_CURRENT_SOURCE_DIR}/run_test.sh" @ONLY)
configure_file("${CMAKE_CURRENT_SOURCE_DIR}/conf/test.cfg.in" "${CMAKE_CURRENT_SOURCE_DIR}/conf/test.cfg" @ONLY)

add_custom_target(check
        COMMAND ./run_test.sh ${BIN_DIR}
        DEPENDS run_test.sh
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        COMMENT "Running system tests."
        VERBATIM)

file(GLOB_RECURSE TEST_FILES_TO_CLEAN *.log failed_cases output)

set_directory_properties(PROPERTIES ADDITIONAL_MAKE_CLEAN_FILES "${TEST_FILES_TO_CLEAN}")
