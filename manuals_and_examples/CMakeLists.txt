#
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
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
####################################################################

#TODO:Create a CMakeLists.txt for the C++ examples, and a pom.xml or a CMakeLists.txt for the Java examples.
install(DIRECTORY examples/images
        DESTINATION "${SHARE_DATA_DIR}/examples")

install(DIRECTORY examples/java
        DESTINATION "${SHARE_DATA_DIR}/examples"
		PATTERN "*.in" EXCLUDE)

install(FILES examples/c++/query.cc
        examples/c++/basictypes.hh
        DESTINATION "${SHARE_DATA_DIR}/examples/c++")

install(DIRECTORY examples/queries
        DESTINATION "${SHARE_DATA_DIR}/examples")

install(FILES examples/rasdl/basictypes.dl
        DESTINATION "${SHARE_DATA_DIR}/examples/rasdl/")

configure_file("${CMAKE_CURRENT_SOURCE_DIR}/examples/c++/Makefile.in" "${CMAKE_CURRENT_BINARY_DIR}/examples/c++/Makefile" @ONLY)

configure_file("${CMAKE_CURRENT_SOURCE_DIR}/examples/java/Makefile.in" "${CMAKE_CURRENT_BINARY_DIR}/examples/java/Makefile" @ONLY)

install(FILES "${CMAKE_CURRENT_BINARY_DIR}/examples/c++/Makefile"
        DESTINATION "${SHARE_DATA_DIR}/examples/c++/"
        RENAME Makefile)

install(FILES "${CMAKE_CURRENT_BINARY_DIR}/examples/java/Makefile"
        DESTINATION "${SHARE_DATA_DIR}/examples/java/"
        RENAME Makefile)

install(DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/manuals/"
        DESTINATION "${SHARE_DATA_DIR}/doc"
        PATTERN "*.doc" EXCLUDE
        PATTERN "*.sh" EXCLUDE)

if (GENERATE_DOCS)
    find_package(Doxygen REQUIRED)

    add_custom_target(documentation ALL
            COMMAND ${DOXYGEN_EXECUTABLE} "${CMAKE_SOURCE_DIR}/manuals_and_examples/doxygen-all.cfg"
            COMMAND ${DOXYGEN_EXECUTABLE} "${CMAKE_SOURCE_DIR}/manuals_and_examples/doxygen-api.cfg"
            COMMAND ${DOXYGEN_EXECUTABLE} "${CMAKE_SOURCE_DIR}/manuals_and_examples/doxygen-basedbms.cfg"

            DEPENDS "${CMAKE_SOURCE_DIR}/manuals_and_examples/doxygen-all.cfg"
            "${CMAKE_SOURCE_DIR}/manuals_and_examples/doxygen-api.cfg"
            "${CMAKE_SOURCE_DIR}/manuals_and_examples/doxygen-basedbms.cfg"

            WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
            COMMENT "Building documentation."
            VERBATIM)

endif ()
