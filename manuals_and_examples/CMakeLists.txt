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

# TODO: Create a CMakeLists.txt for the C++ examples, and a pom.xml or a CMakeLists.txt for the Java examples.

set(EXAMPLES_DIR "${SHARE_DATA_DIR}/examples")
install(DIRECTORY examples/images
        DESTINATION "${EXAMPLES_DIR}")
install(DIRECTORY examples/java
        DESTINATION "${EXAMPLES_DIR}"
		PATTERN "*.in" EXCLUDE)
install(FILES examples/c++/query.cc
        DESTINATION "${EXAMPLES_DIR}/c++")
install(DIRECTORY examples/queries
        DESTINATION "${EXAMPLES_DIR}")

configure_file("${CMAKE_CURRENT_SOURCE_DIR}/examples/c++/Makefile.in" 
               "${CMAKE_CURRENT_BINARY_DIR}/examples/c++/Makefile" @ONLY)
configure_file("${CMAKE_CURRENT_SOURCE_DIR}/examples/java/Makefile.in"
               "${CMAKE_CURRENT_BINARY_DIR}/examples/java/Makefile" @ONLY)

install(FILES "${CMAKE_CURRENT_BINARY_DIR}/examples/c++/Makefile"
        DESTINATION "${EXAMPLES_DIR}/c++/")
install(FILES "${CMAKE_CURRENT_BINARY_DIR}/examples/java/Makefile"
        DESTINATION "${EXAMPLES_DIR}/java/")

install(DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/manuals/"
        DESTINATION "${SHARE_DATA_DIR}/doc"
        PATTERN "*.doc" EXCLUDE
        PATTERN "*.sh" EXCLUDE)

if (GENERATE_DOCS)

    find_package(Doxygen)

    if (Doxygen_FOUND)
    
        configure_file("${CMAKE_CURRENT_SOURCE_DIR}/doxygen-all.cfg.in"
                       "${CMAKE_CURRENT_BINARY_DIR}/doxygen-all.cfg" @ONLY)
        configure_file("${CMAKE_CURRENT_SOURCE_DIR}/doxygen-api.cfg.in"
                       "${CMAKE_CURRENT_BINARY_DIR}/doxygen-api.cfg" @ONLY)
        configure_file("${CMAKE_CURRENT_SOURCE_DIR}/doxygen-basedbms.cfg.in"
                       "${CMAKE_CURRENT_BINARY_DIR}/doxygen-basedbms.cfg" @ONLY)

        # this is where the doxygen output goes
        file(MAKE_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/manuals")

        add_custom_target(doc-cpp ALL
                COMMAND ${DOXYGEN_EXECUTABLE} "${CMAKE_CURRENT_BINARY_DIR}/doxygen-all.cfg"
                COMMAND ${DOXYGEN_EXECUTABLE} "${CMAKE_CURRENT_BINARY_DIR}/doxygen-api.cfg"
                COMMAND ${DOXYGEN_EXECUTABLE} "${CMAKE_CURRENT_BINARY_DIR}/doxygen-basedbms.cfg"

                DEPENDS "${CMAKE_CURRENT_BINARY_DIR}/doxygen-all.cfg"
                        "${CMAKE_CURRENT_BINARY_DIR}/doxygen-api.cfg"
                        "${CMAKE_CURRENT_BINARY_DIR}/doxygen-basedbms.cfg"

                WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
                COMMENT "Building documentation."
                VERBATIM)

        add_dependencies(doc doc-cpp)

        install(DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/manuals"
                DESTINATION "${SHARE_DATA_DIR}/doc")

    endif()

endif ()
