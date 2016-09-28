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
    # will not rebuild secore when make install
    set(CMAKE_SKIP_INSTALL_ALL_DEPENDENCY TRUE)

    set(LOGDIR "${LOG_DIR}")
    find_package(Maven REQUIRED)

    # This variable will be used in the to configure the pom files.
    set(SECORE_TARGET_DIR ${CMAKE_CURRENT_BINARY_DIR})
    # This variable will be used in secore-core/etc/secore.conf.in.
    set(SHARE_DATA_DIR ${SHARE_DATA_DIR})

    configure_file(secore-core/pom.xml.in "${CMAKE_CURRENT_SOURCE_DIR}/secore-core/pom.xml" @ONLY)
    configure_file(secore-web/pom.xml.in "${CMAKE_CURRENT_SOURCE_DIR}/secore-web/pom.xml" @ONLY)

    # Generate files from *.in by replacing any variables that can be configured at compile time
    # configuration file for secore.properties.in
    configure_file("secore-core/etc/secore.properties.in" ${CMAKE_CURRENT_SOURCE_DIR}/secore-core/etc/secore.properties @ONLY)        
    # configuration for web.xml.in
    configure_file("secore-web/src/main/webapp/WEB-INF/web.xml.in" ${CMAKE_CURRENT_SOURCE_DIR}/secore-web/src/main/webapp/WEB-INF/web.xml @ONLY)

    if (GENERATE_DOCS)
        add_custom_target(secore_doc ALL
            COMMAND ${MAVEN_EXECUTABLE} javadoc:javadoc 2>&1 > /dev/null || exit 0
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
            VERBATIM)

        install(DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/site/apidocs/"
            DESTINATION "${SHARE_DATA_DIR}/doc/doc-secore"
            PATTERN "*.sh" EXCLUDE)

    endif(GENERATE_DOCS)

    add_custom_target(secore ALL
            COMMAND ${MAVEN_EXECUTABLE} package
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
            VERBATIM)

    # install secore.properties if does not exist and run update_properties.sh to merge new configuration file with old configuration file
    install(CODE "execute_process(COMMAND sh  ${CMAKE_CURRENT_SOURCE_DIR}/update_properties.sh ${CONF_DIR}/secore.properties ${CMAKE_CURRENT_SOURCE_DIR}/secore-core/etc/secore.properties)")
    install(DIRECTORY DESTINATION "${WAR_DIR}/secoredb"
	    DIRECTORY_PERMISSIONS OWNER_EXECUTE OWNER_WRITE OWNER_READ 
	    GROUP_EXECUTE GROUP_WRITE GROUP_READ 
	    WORLD_EXECUTE WORLD_WRITE WORLD_READ)

    install(FILES "${SECORE_TARGET_DIR}/def.war"
    DESTINATION ${WAR_DIR})

    # these files contain update CRS defintions for SECORE userdb
    install(DIRECTORY db_updates/ DESTINATION "${SHARE_DATA_DIR}/secore")

endif ()
