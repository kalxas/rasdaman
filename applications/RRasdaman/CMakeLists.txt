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
#
# MAKEFILE FOR:
#   rasdaman R package
#
##################################################################
set(PACKAGE_NAME "RRasdaman")
set(RRASDAMAN_PACKAGE "${PACKAGE_NAME}_${PROJECT_VERSION}.tar.gz")

# Maven will compile and package the java files to this directory.
set(RRASDAMAN_JAVA_TARGET_DIR "${CMAKE_CURRENT_BINARY_DIR}/java_target")
# Used to create the R package
set(RRASDAMAN_R_TARGET_DIR "${CMAKE_CURRENT_BINARY_DIR}/r_target")

# We create this file because CMake does not create it automatically
file(MAKE_DIRECTORY ${RRASDAMAN_R_TARGET_DIR})

# Generate the pom.xml file with the current configuration
configure_file(pom.xml.in "${CMAKE_CURRENT_SOURCE_DIR}/pom.xml" @ONLY)

set(R_SRCS "${CMAKE_CURRENT_SOURCE_DIR}/R/array.R"
	  "${CMAKE_CURRENT_SOURCE_DIR}/R/connection.R"
	  "${CMAKE_CURRENT_SOURCE_DIR}/R/driver.R"   
	  "${CMAKE_CURRENT_SOURCE_DIR}/R/onLoad.R"    
	  "${CMAKE_CURRENT_SOURCE_DIR}/R/Rasdaman.R"
	  "${CMAKE_CURRENT_SOURCE_DIR}/R/result.R"
	  "${CMAKE_CURRENT_SOURCE_DIR}/R/util.R")

set(RD_SRCS "${CMAKE_CURRENT_SOURCE_DIR}/man/dbCommit-methods.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/dbGetInfo-methods.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/dbListCollections-methods.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/getCell-methods.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/RasdamanDriver-class.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/Rasdaman.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/simplify-methods.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/dbConnect-methods.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/dbGetQuery-methods.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/dbReadCollection-methods.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/RasdamanArray-class.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/RasdamanResult-class.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/RasdamanHandle-class.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/dbFetch-methods.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/RRasdaman-package.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/RasdamanObject-class.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/RasdamanConnection-class.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/dbRemoveCollection-methods.Rd"
	"${CMAKE_CURRENT_SOURCE_DIR}/man/dbInsertCollection-methods.Rd"
	)

set(MISC_SRCS "${CMAKE_CURRENT_SOURCE_DIR}/DESCRIPTION" 
	"${CMAKE_CURRENT_SOURCE_DIR}/NAMESPACE"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/main/java/README")

set(TEST_SRCS "${CMAKE_CURRENT_SOURCE_DIR}/tests/test-all.R"
	"${CMAKE_CURRENT_SOURCE_DIR}/tests/testthat/test.connection.R"
	"${CMAKE_CURRENT_SOURCE_DIR}/tests/testthat/test.select.R"
	"${CMAKE_CURRENT_SOURCE_DIR}/tests/testthat/test.write.R")


find_package(Maven REQUIRED)

add_custom_target(RRasdaman_java ALL
            COMMAND ${MAVEN_EXECUTABLE} package
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
            VERBATIM)

add_dependencies(RRasdaman_java rasj)

#START: build folder
set(R_BUILD_FOLDER "${CMAKE_CURRENT_BINARY_DIR}/build")
set(RJAVA_INST_DIR "${R_BUILD_FOLDER}/inst/java")

file(MAKE_DIRECTORY ${R_BUILD_FOLDER})
file(MAKE_DIRECTORY ${RJAVA_INST_DIR})
file(COPY "${CMAKE_CURRENT_SOURCE_DIR}/DESCRIPTION" DESTINATION "${R_BUILD_FOLDER}")
file(COPY "${CMAKE_CURRENT_SOURCE_DIR}/NAMESPACE" DESTINATION "${R_BUILD_FOLDER}")
file(COPY "${CMAKE_CURRENT_SOURCE_DIR}/R" DESTINATION "${R_BUILD_FOLDER}")
file(COPY "${CMAKE_CURRENT_SOURCE_DIR}/src" DESTINATION "${R_BUILD_FOLDER}")
file(COPY "${CMAKE_CURRENT_SOURCE_DIR}/tests" DESTINATION "${R_BUILD_FOLDER}")
file(COPY "${CMAKE_CURRENT_SOURCE_DIR}/man" DESTINATION "${R_BUILD_FOLDER}")

add_custom_target(RRasdaman_create_inst ALL 
                   COMMAND ${CMAKE_COMMAND} -E copy "${RRASDAMAN_JAVA_TARGET_DIR}/RRasdaman.jar" ${RJAVA_INST_DIR}
                   COMMAND ${CMAKE_COMMAND} -E copy "${RASJ_TARGET_DIR}/rasj-jar-with-dependencies.jar" "${RJAVA_INST_DIR}/rasj.jar")

add_dependencies(RRasdaman_create_inst RRasdaman_java)
#END: build folder

# TODO(vliaukevich): check for needed R packages
find_package(R REQUIRED)

add_custom_command(OUTPUT "${RRASDAMAN_R_TARGET_DIR}/${RRASDAMAN_PACKAGE}"
	DEPENDS ${R_SRCS} ${RD_SRCS} ${TEST_SRCS} ${MISC_SRCS}
	COMMAND ${R_COMMAND} ARGS CMD  build ${R_BUILD_FOLDER}
	WORKING_DIRECTORY ${RRASDAMAN_R_TARGET_DIR}
	VERBATIM)

add_custom_target(RRasdaman_build ALL 
	DEPENDS "${RRASDAMAN_R_TARGET_DIR}/${RRASDAMAN_PACKAGE}"
	COMMAND ${R_COMMAND} CMD build ${R_BUILD_FOLDER}
	WORKING_DIRECTORY ${RRASDAMAN_R_TARGET_DIR}
	COMMENT "Building RRasdaman"
	VERBATIM)

add_custom_target(RRasdaman_install 
	DEPENDS "${RRASDAMAN_R_TARGET_DIR}/${RRASDAMAN_PACKAGE}"
	COMMAND ${R_COMMAND} CMD INSTALL ${RRASDAMAN_PACKAGE}
	WORKING_DIRECTORY ${RRASDAMAN_R_TARGET_DIR}
	COMMENT "Installing RRasdaman"
	VERBATIM)

install(CODE "execute_process(COMMAND \"${CMAKE_COMMAND}\" --build \"${CMAKE_CURRENT_BINARY_DIR}\" --target RRasdaman_install)")