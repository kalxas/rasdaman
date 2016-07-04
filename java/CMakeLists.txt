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
    if (RMANRASNET)
        set(RASNET_SRC_DIR "${CMAKE_SOURCE_DIR}/rasnet")
        set(COMMON_SRC_DIR "${CMAKE_SOURCE_DIR}/common")

        set(GRPC_SRC "${RASNET_SRC_DIR}/protomessages/client_rassrvr_service.proto"
                "${RASNET_SRC_DIR}/protomessages/rasmgr_client_service.proto"
                "${COMMON_SRC_DIR}/src/grpc/protomessages/health_service.proto")

        set(PROTO_SRC ${GRPC_SRC}
                "${COMMON_SRC_DIR}/src/grpc/protomessages/error.proto"
                "${RASNET_SRC_DIR}/protomessages/common_service.proto")

        set(GENERATED_PROTO_SOURCES "")

        foreach (PROTO_FILE ${PROTO_SRC})
            CompileJavaProtobufFile(${PROTO_FILE} "${CMAKE_CURRENT_SOURCE_DIR}/src/main/java" GENERATED_PROTO_SOURCES)
        endforeach ()

        set(GENERATED_GRPC_SOURCES "")

        foreach (GRPC_FILE ${GRPC_SRC})
            CompileJavaGRPCFile(${GRPC_FILE} "${CMAKE_CURRENT_SOURCE_DIR}/src/main/java" GENERATED_GRPC_SOURCES)
        endforeach ()

    endif (RMANRASNET)

    # Configure the pom file    
    configure_file(pom.xml.in "${CMAKE_CURRENT_SOURCE_DIR}/pom.xml" @ONLY)

    # Run maven to build the project.
    find_package(Maven REQUIRED)
    add_custom_target(rasj ALL
            COMMAND ${MAVEN_EXECUTABLE} package -P${NETWORK_PROTOCOL}
            DEPENDS ${GENERATED_PROTO_SOURCES} ${GENERATED_GRPC_SOURCES}
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
            VERBATIM)

    if (GENERATE_DOCS)
        add_custom_target(rasj_doc ALL
            COMMAND ${MAVEN_EXECUTABLE} javadoc:javadoc -P${NETWORK_PROTOCOL} 2>&1 > /dev/null || exit 0
            DEPENDS ${GENERATED_PROTO_SOURCES} ${GENERATED_GRPC_SOURCES}
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
            VERBATIM)   

        install(DIRECTORY "${RASJ_TARGET_DIR}/site/apidocs/"            
            DESTINATION "${SHARE_DATA_DIR}/doc/doc-rasj"
            PATTERN "*.sh" EXCLUDE)

    endif(GENERATE_DOCS)

    install(FILES "${RASJ_TARGET_DIR}/rasj-jar-with-dependencies.jar"
            RENAME "rasj.jar"
            DESTINATION ${LIB_DIR})

endif (ENABLE_JAVA)
