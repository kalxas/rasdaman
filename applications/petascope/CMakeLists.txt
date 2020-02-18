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
set(SRC_DIR "${CMAKE_CURRENT_SOURCE_DIR}/")
set(BUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/")
set(SRC_COVERAGE_EXAMPLES "${SRC_DIR}/coverages_example")

# Constants used in the template (.in) files; if you change the key you need to change the corresponding files as well
set(PETASCOPE_TARGET_DIR "${BUILD_DIR}/target")
set(CONF_DIR "${CONF_DIR}/")
set(GENERATED_rasdaman_bin_path "${BIN_DIR}/")
set(bindir "${BIN_DIR}/")
set(pkgdatadir "${SHARE_DATA_DIR}/")
set(sysconfdir "${CONF_DIR}/")
set(logdir "${LOG_DIR}/")
set(libdir "${LIB_DIR}/")
set(java_server ${JAVA_SERVER})
set(petascopedb ${PETASCOPEDB})
set(RASJ_PATH "${CMAKE_BINARY_DIR}/java/target/rasj-jar-with-dependencies.jar")
set(PETASCOPE_SHARE_DIR "${SHARE_DATA_DIR}/petascope/")

# Will not rebuild petascope package when make install
set(CMAKE_SKIP_INSTALL_ALL_DEPENDENCY TRUE)

# Copy the content of resources folder which will be included in rasdaman.war
file(COPY "${SRC_DIR}/petascope_main/src/main/resources" DESTINATION "${BUILD_DIR}/petascope_main/src/main/")

# Necessary parameters to start main method in application (petascope_main)
set(APPLICATION_FILE_TEMPLATE "${SRC_DIR}/petascope_main/src/main/resources/application.properties.in")
set(APPLICATION_FILE "${SRC_DIR}/petascope_main/src/main/resources/application.properties")

# Log4j for Spring framework and used only when it starts (not for petascope applications)
set(LOG4J_SPRING_FILE_TEMPLATE "${SRC_DIR}/petascope_core/src/main/resources/log4j.properties.in")
set(LOG4J_SPRING_FILE "${SRC_DIR}/petascope_core/src/main/resources/log4j.properties")

# Only has 1 petascope.properties for all databases in application (petascope_main)
set(SETTINGS_FILE_TEMPLATE "${SRC_DIR}/petascope_main/src/main/resources/petascope.properties.in")
set(SETTINGS_FILE "${SRC_DIR}/petascope_main/src/main/resources/petascope.properties")

# Copy liquibase.properties to build directory to be attached inside the war file
set(LIQUIBASE_PROPERTIES_FILE "${SRC_DIR}/petascope_main/src/main/resources/liquibase.properties")

# Script to migrate old petascopedb to new petascopedb
set(MIGRATE_PETASCOPEDB_SCRIPT_TEMPLATE "${SRC_DIR}/migrate_petascopedb.sh.in")
set(MIGRATE_PETASCOPEDB_SCRIPT "${BUILD_DIR}/migrate_petascopedb.sh")

# Deprecated migrate script
set(CREATE_PETASCOPEDB_SCRIPT_TEMPLATE "${SRC_DIR}/update_petascopedb.sh.in")
set(CREATE_PETASCOPEDB_SCRIPT "${BUILD_DIR}/update_petascopedb.sh")

# Script to import coverages example to petascopedb
set(INSERT_DATA_SCRIPT_TEMPLATE "${SRC_COVERAGE_EXAMPLES}/petascope_insertdemo.sh.in")
set(INSERT_DATA_SCRIPT "${SRC_COVERAGE_EXAMPLES}/petascope_insertdemo.sh")

# Server decision: embeded (jar) vs external (war); JAVA_SERVER_EMBEDDED variable is provided from the main CMakeLists.txt file
if (JAVA_SERVER_EMBEDDED)
    set(GENERATED_java_server "embedded")
    set(GENERATED_embedded_petascope "true") 
else ()
    set(GENERATED_java_server "external")
    set(GENERATED_embedded_petascope "false")
endif ()

# Generate file from .in file by replacing any variables that can be configured at compile time
# Do the same for the Application.in 
configure_file("${APPLICATION_FILE_TEMPLATE}" "${APPLICATION_FILE}" @ONLY)

# Do the same for the petascope_parent pom.xml 
configure_file("${SRC_DIR}/pom.xml.in" "${SRC_DIR}/pom.xml" @ONLY)
# Do the same for the petascope_core pom.xml 
configure_file("${SRC_DIR}/petascope_core/pom.xml.in" "${SRC_DIR}/petascope_core/pom.xml" @ONLY)
# Do the same for the petascope_main pom.xml 
configure_file("${SRC_DIR}/petascope_main/pom.xml.in" "${SRC_DIR}/petascope_main/pom.xml" @ONLY)

# Do the same for petascope.properties.in
configure_file("${SETTINGS_FILE_TEMPLATE}" "${SETTINGS_FILE}" @ONLY)

# Do the same for migrate_petascopedb.sh.in
configure_file("${MIGRATE_PETASCOPEDB_SCRIPT_TEMPLATE}" "${MIGRATE_PETASCOPEDB_SCRIPT}" @ONLY)

# Do the same for update_petascopedb.sh.in
configure_file("${CREATE_PETASCOPEDB_SCRIPT_TEMPLATE}" "${CREATE_PETASCOPEDB_SCRIPT}" @ONLY)

# Do the same for petascope_insertdemo.sh.in
configure_file("${INSERT_DATA_SCRIPT_TEMPLATE}" "${INSERT_DATA_SCRIPT}" @ONLY)

# Create the package for the war profile (Petascope embedded or external can use only one war package)
add_custom_target(petascope ALL
    COMMAND ${MAVEN_EXECUTABLE} -q -B package -f "pom.xml"
                                -DbuildDir=${BUILD_DIR}
                                -Dversion=${PROJECT_VERSION}
                                -DgdalJavaJarPath=${GDAL_JAVA_JAR_PATH}

    DEPENDS rasj
    WORKING_DIRECTORY ${SRC_DIR}
    COMMENT "building petascope"
    VERBATIM)
add_dependencies(petascope rasj)

install(PROGRAMS "${CREATE_PETASCOPEDB_SCRIPT}" "${INSERT_DATA_SCRIPT}"
        DESTINATION ${BIN_DIR})

# Move the log4j.properties to log4j.properties.unused as it was merged in petascope.properties
install( CODE "execute_process(COMMAND mv ${CONF_DIR}/log4j.properties ${CONF_DIR}/log4j.properties.unused ERROR_QUIET)" )

# Install petascope.properties if does not exist and run update_properties.sh to merge new configuration file with old configuration file
install( CODE "execute_process(COMMAND bash ${SRC_DIR}/update_properties.sh ${CONF_DIR}/petascope.properties ${SETTINGS_FILE})" )

# Install built package (war) to the web applications folder (e.g: Tomcat /var/lib/tomcat/webapps/)
# Petascope can be run as embedded or external with war package.
install( FILES "${PETASCOPE_TARGET_DIR}/rasdaman.war"
         DESTINATION "${WAR_DIR}" )


install( PROGRAMS "${BUILD_DIR}/migrate_petascopedb.sh"
         DESTINATION "${BIN_DIR}" )

# Copy update_properties.sh to $RMANHOME/bin to be used by rasdaman installer for installing packages (e.g: rpm)
# which will update the changes from user's old petascope.properties and new built-in petascope.properties in the package.
install( PROGRAMS "${SRC_DIR}/update_properties.sh"
         DESTINATION "${BIN_DIR}" )

# Copy petascope_insertdemo.sh to bin dir
install( PROGRAMS "${INSERT_DATA_SCRIPT}"
         DESTINATION "${BIN_DIR}" )

# Copy coverages_example/petascope_insertdemo_data to (e.g: /opt/rasdaman/share/rasdaman/petascope)
install(DIRECTORY "${SRC_COVERAGE_EXAMPLES}/petascope_insertdemo_data"
        DESTINATION "${PETASCOPE_SHARE_DIR}")

if (GENERATE_DOCS)

    #TODO: make install fails (cannot find applications/petascope//target/petascope_core/site)

    #add_custom_target(doc-petascope ALL
    #                  COMMAND ${MAVEN_EXECUTABLE} -q -B javadoc:javadoc -f "${BUILD_DIR}/petascope_core/pom.xml"
    #                  WORKING_DIRECTORY ${SRC_DIR}/petascope_core
    #                  VERBATIM)
    # Copy java doc from target folders to (e.g: /opt/rasdaman/share/doc/)
    #install(DIRECTORY "${PETASCOPE_TARGET_DIR}/petascope_core/site"
    #    DESTINATION "${SHARE_DATA_DIR}/doc/doc-petascope/classes/petascope_core")

    #TODO: not working at the moment (javadoc: error - No public or protected classes found to document)

    #add_custom_target(doc-petascope-main ALL
    #                  COMMAND ${MAVEN_EXECUTABLE} -q -B javadoc:javadoc -f "${BUILD_DIR}/petascope_main/pom.xml"
    #                  WORKING_DIRECTORY ${SRC_DIR}/petascope_main
    #                  VERBATIM)
    #install(DIRECTORY "${PETASCOPE_TARGET_DIR}/petascope_main/site"
    #    DESTINATION "${SHARE_DATA_DIR}/doc/doc-petascope/classes/petascope_main")

endif(GENERATE_DOCS)
