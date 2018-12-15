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

if (ENABLE_JAVA)
    set(SRC_DIR "${CMAKE_CURRENT_SOURCE_DIR}/")
    set(BUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/")
    set(SECORE_TARGET_DIR "${BUILD_DIR}/target")
    # will not rebuild secore when make install
    set(CMAKE_SKIP_INSTALL_ALL_DEPENDENCY TRUE)

    configure_file(pom.xml.in "${BUILD_DIR}/pom.xml" @ONLY)    

    # Generate files from *.in by replacing any variables that can be configured at compile time
    # configuration file for secore.properties.in
    configure_file("src/main/resources/secore.properties.in" "${BUILD_DIR}/src/main/resources/secore.properties" @ONLY)     
  
    # configuration file for application.properties.in
    configure_file("src/main/resources/application.properties.in" "${BUILD_DIR}/src/main/resources/application.properties" @ONLY)

    # configuration file for log4j.properties.in which is used only when Spring Boot starts
    configure_file("src/main/resources/log4j.properties.in" "${BUILD_DIR}/src/main/resources/log4j.properties" @ONLY)

    if (GENERATE_DOCS)

        #TODO: make install fails (cannot find applications/secore//target/site/apidocs)
        #add_custom_target(secore_doc ALL
        #    COMMAND ${MAVEN_EXECUTABLE} -q -B javadoc:javadoc 2>&1 > /dev/null || exit 0
        #    WORKING_DIRECTORY ${SRC_DIR}
        #    VERBATIM)
        #install(DIRECTORY "${SECORE_TARGET_DIR}/site/apidocs/"
        #    DESTINATION "${SHARE_DATA_DIR}/doc/doc-secore/classes"
        #    PATTERN "*.sh" EXCLUDE)

    endif(GENERATE_DOCS)

    add_custom_target(secore ALL
            COMMAND ${MAVEN_EXECUTABLE} -q -B package -f "${BUILD_DIR}/pom.xml" > /dev/null
            WORKING_DIRECTORY ${SRC_DIR}
            COMMENT "building secore"
            VERBATIM)

    # install secore.properties if does not exist and run update_properties.sh to merge new configuration file with old configuration file
    install(CODE "execute_process(COMMAND bash  ${SRC_DIR}/update_properties.sh ${CONF_DIR}/secore.properties ${BUILD_DIR}/src/main/resources/secore.properties)")

    install(DIRECTORY DESTINATION "${WAR_DIR}/secoredb"
	        DIRECTORY_PERMISSIONS OWNER_EXECUTE OWNER_WRITE OWNER_READ 
	        GROUP_EXECUTE GROUP_WRITE GROUP_READ 
	        WORLD_EXECUTE WORLD_WRITE WORLD_READ)

    install(FILES "${SECORE_TARGET_DIR}/def.war"
            DESTINATION ${WAR_DIR})

    # these files contain update CRS defintions for SECORE userdb
    install(DIRECTORY db_updates/ DESTINATION "${SHARE_DATA_DIR}/secore")

endif ()
