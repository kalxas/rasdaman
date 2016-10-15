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
# Copyright 2003 - 2016 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# Constants
# Directories
set(SRC "${CMAKE_CURRENT_SOURCE_DIR}/src/main")
set(SRC_MAIN "${SRC}/java")
set(SRC_RES "${SRC}/resources")
set(SRC_WEB "${SRC}/webapp")
set(SRC_DB "${SRC}/db/")

# Constants used in the template (.in) files; if you change the key you need to change the corresponding files as well
set(SERVICE_FILENAME wms_service.properties)
set(SERVICE_SRC_FILE "src/main/resources/wms/wms_service.properties")
set(PETASCOPE_WEB_XML_PATH "${SRC_WEB}/WEB-INF/web.xml")
set(PETASCOPE_TARGET_DIR "${CMAKE_CURRENT_BINARY_DIR}/target")
set(CONF_DIR "${CONF_DIR}/")
set(GENERATED_rasdaman_bin_path "${BIN_DIR}/")
set(GENERATED_jetty_extracted_path ${WAR_DIR})
set(LOGDIR "${LOG_DIR}")
set(bindir "${BIN_DIR}/")
set(pkgdatadir "${SHARE_DATA_DIR}/")
set(sysconfdir "${CONF_DIR}/")
set(libdir "${LIB_DIR}/")
set(java_server ${JAVA_SERVER})
set(petascopedb ${PETASCOPEDB})
set(RASJ_PATH "${CMAKE_BINARY_DIR}/java/target/rasj-jar-with-dependencies.jar")

# Database files
set(CREATE_PETASCOPEDB_SCRIPT "${SRC_DB}/update_petascopedb.sh")
set(CREATE_PETASCOPEDB_TEMPLATE "${CREATE_PETASCOPEDB_SCRIPT}.in")
set(INSERT_DATA_SCRIPT "${SRC_DB}/petascope_insertdemo.sh")
set(INSERT_DATA_SCRIPT_TEMPLATE "${INSERT_DATA_SCRIPT}.in")
set(DATABASE_UPDATES_DIR "${SRC_DB}/petascope")

# Server decision: embeded vs external; JAVA_SERVER_EMBEDDED variable is provided from the main CMakeLists.txt file
if (JAVA_SERVER_EMBEDDED)
    set(GENERATED_java_server "embedded")
    set(GENERATED_embedded_petascope "true")
    set(USE_EMBEDDED_JAVA_SERVER true)    
else ()
    set(GENERATED_java_server "external")
    set(GENERATED_embedded_petascope "false")
    set(USE_EMBEDDED_JAVA_SERVER false)
endif ()

# Choose the correct templates file based on the selected database for petascope
if (PETASCOPEDB_PGSQL)
    set(SETTINGS_FILE_TEMPLATE "${CMAKE_CURRENT_SOURCE_DIR}/src/main/resources/petascope.properties.in")
    set(SETTINGS_FILE "${CMAKE_CURRENT_SOURCE_DIR}/src/main/resources/petascope.properties")
endif ()

if (PETASCOPEDB_SQLITE)
    set(SETTINGS_FILE_TEMPLATE "${CMAKE_CURRENT_SOURCE_DIR}/src/main/resources/petascope_sqlite.properties.in")
    set(SETTINGS_FILE "${CMAKE_CURRENT_SOURCE_DIR}/src/main/resources/petascope_sqlite.properties")
    set(SQLITE 1)
endif ()

if (PETASCOPEDB_HSQLDB)
    set(SETTINGS_FILE_TEMPLATE "${CMAKE_CURRENT_SOURCE_DIR}/src/main/resources/petascope_hsqldb.properties.in")
    set(SETTINGS_FILE "${CMAKE_CURRENT_SOURCE_DIR}/src/main/resources/petascope_hsqldb.properties")
    set(HSQLDB 1)
endif ()

set(LOG_SETTINGS_FILE_TEMPLATE "${CMAKE_CURRENT_SOURCE_DIR}/src/main/resources/log4j.properties.in")
set(LOG_SETTINGS_FILE "${CMAKE_CURRENT_SOURCE_DIR}/src/main/resources/log4j.properties")

# Generate web.xml out of an .in file by replacing any variables that can be configured at compile time
configure_file("${SRC_WEB}/WEB-INF/web.xml.in" ${PETASCOPE_WEB_XML_PATH} @ONLY)

# Do the same for the pom.xml 
configure_file(pom.xml.in "${CMAKE_CURRENT_SOURCE_DIR}/pom.xml" @ONLY)

# Do the same for petascope.properties
configure_file(${SETTINGS_FILE_TEMPLATE} ${SETTINGS_FILE} @ONLY)

# Do the same for log4j.properties
configure_file(${LOG_SETTINGS_FILE_TEMPLATE} ${LOG_SETTINGS_FILE} @ONLY)

# Do the same for update_petascopedb.sh
configure_file(${CREATE_PETASCOPEDB_TEMPLATE} ${CREATE_PETASCOPEDB_SCRIPT} @ONLY)

# Do the same for petascope_insertdemo.sh
configure_file(${INSERT_DATA_SCRIPT_TEMPLATE} ${INSERT_DATA_SCRIPT} @ONLY)

# Make sure maven is available
find_package(Maven REQUIRED)

# Create the target for building petascope war
add_custom_target(petascope ALL
        COMMAND ${MAVEN_EXECUTABLE} package "-Ppetascope"
        DEPENDS rasj
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        VERBATIM)

add_custom_target(setlogdir ALL
        COMMAND test -f ${CONF_DIR}/log4j.properties && sed -i 's|log4j.appender.rollingFile.File=petascope.log|log4j.appender.rollingFile.File=${LOG_DIR}/petascope.log|g' ${CONF_DIR}/log4j.properties || true
)

add_dependencies(petascope rasj setlogdir)

# If petascope should work as a standalone server let's build the jetty profile in maven
if (JAVA_SERVER_EMBEDDED)
    add_custom_target(petascope-embeded ALL
        COMMAND ${MAVEN_EXECUTABLE} package "-Pjetty"
        DEPENDS rasj
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        VERBATIM)
    
    add_dependencies(petascope-embeded petascope)

# Install it in the war dir
    install(FILES "${PETASCOPE_TARGET_DIR}/petascope-standalone.jar"
            DESTINATION ${WAR_DIR})
# Copy the standalone jetty archive    
    install(FILES "${CMAKE_CURRENT_SOURCE_DIR}/jetty/jetty.jar"
            DESTINATION ${WAR_DIR})    
endif(JAVA_SERVER_EMBEDDED)

# Install the database files
install(PROGRAMS "${CREATE_PETASCOPEDB_SCRIPT}" "${INSERT_DATA_SCRIPT}"
        DESTINATION ${BIN_DIR})

install(DIRECTORY "${DATABASE_UPDATES_DIR}"
        DESTINATION ${SHARE_DATA_DIR}
        PATTERN "*.sh"
        PERMISSIONS OWNER_EXECUTE OWNER_WRITE OWNER_READ
                    GROUP_EXECUTE GROUP_READ)

# Install the settings file
install_if_not_exists(${SETTINGS_FILE} ${CONF_DIR})

# Install log4j configuration if it does not exist
install_if_not_exists("${CMAKE_CURRENT_SOURCE_DIR}/src/main/resources/log4j.properties" ${CONF_DIR})

# Install wms_service.properties if it does not exist
install_if_not_exists("${CMAKE_CURRENT_SOURCE_DIR}/src/main/resources/wms/wms_service.properties" ${CONF_DIR})

# Install it to the war directory specified during configure
install(FILES "${PETASCOPE_TARGET_DIR}/rasdaman.war"
        DESTINATION ${WAR_DIR})
