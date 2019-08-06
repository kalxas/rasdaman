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
# Copyright 2003-2015 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
###################################################################

# The following variables are set in this script and should be used wherever one of the third party libraries is needed
# GTEST_LIBRARY - The Google GTest library
# GMOCK_LIBRARY - The Google GMock library
# GRPC_LIBRARY - A GRPC library required by any target that uses GRPC
# GRPCPP_LIBRARY - A GRPC library required by any target that uses GRPC
# GRPC_CPP_PLUGIN_EXECUTABLE - Plugin used to generate GRPC C++ code from proto files.
# PROTOBUF_LIBRARY - The Google Protobuf library used by any target that uses Google Protobuf
# PROTOBUF_PROTOC_EXECUTABLE - The Protobuf Protoc compiler which is used to generate C++ and Java files from *.proto definitions
# GRPC_JAVA_PLUGIN_EXECUTABLE - Plugin used to generate GRPC Java code from proto files.
# JSONCPP - Used by the encoders and decoders for extra parameter passing

# `make clean` will not remove files built here with the setting below; we don't change
# third_party (often) so no need to rebuild on every make clean. If it is changed, then
# build dir would need to be removed instead of make clean because of this setting.
set_directory_properties(PROPERTIES CLEAN_NO_CUSTOM 1)

# Enable ExternalProject CMake module
include(ExternalProject)

macro(InstallExternalLibrary LIBRARY_NAME LIBRARY_PATH DEPENDENCIES)
    add_library(${LIBRARY_NAME} STATIC IMPORTED)
    set_property(TARGET ${LIBRARY_NAME} PROPERTY
            IMPORTED_LOCATION ${LIBRARY_PATH})

    # The library depends on the external project.
    add_dependencies(${LIBRARY_NAME} ${DEPENDENCIES})

    # Install the directory to the lib directory
    install(FILES ${LIBRARY_PATH}
            DESTINATION ${LIB_DIR}
            PERMISSIONS OWNER_READ OWNER_WRITE OWNER_EXECUTE)

endmacro()

# Add an executable built by a custom command to the environment.
# Syntax: AddExternalExecutable(NAME, PATH, DEPENDENCIES)
#   NAME - The name of the executable
#   PATH - The path to the executable file
#   DEPENDENCIES - The targets on which the existence of this executable depends.
macro(AddExternalExecutable NAME PATH DEPENDENCIES)
    add_executable(${NAME} IMPORTED GLOBAL)
    set_property(TARGET ${NAME} PROPERTY IMPORTED_LOCATION
            ${PATH})
    add_dependencies(${NAME} ${DEPENDENCIES})
endmacro()

# This macro is used to create a shared, imported library from an external project.
# Syntax InstallExternalStaticLibrary(LIBRARY_NAME LIBRARY_PATH DEPENDENCIES)
#   LIBRARY_NAME: The name of the library that will be used within the project
#   LIBRARY_PATH: The path to the library object
#   DEPENDENCIES: The name of the project or object this library depends on.
macro(InstallExternalDynamicLibrary LIBRARY_NAME LIBRARY_PATH DEPENDENCIES)
    add_library(${LIBRARY_NAME} SHARED IMPORTED GLOBAL)
    set_property(TARGET ${LIBRARY_NAME} PROPERTY
        IMPORTED_LOCATION ${LIBRARY_PATH})

    # The library depends on the external project.
    add_dependencies(${LIBRARY_NAME} ${DEPENDENCIES})
endmacro()

#########################################
# DOWNLOAD THE SUBMODULES				#
#########################################
execute_process(COMMAND git submodule update --init --recursive
        WORKING_DIRECTORY ${CMAKE_SOURCE_DIR})

# Create this folder first before tar can extract grpc.tar.gz
execute_process(COMMAND mkdir -p ${CMAKE_BINARY_DIR}/third_party)

# gRPC-C++.podspec is in grpc v1.10.0 but not in the previous release that was
# embedded in rasdaman (v1.7.2), so we check for its existence to determine if
# the extracted grpc/ dreictory is the most current one and remove it if not.
if(NOT EXISTS "${CMAKE_BINARY_DIR}/third_party/grpc/gRPC-C++.podspec")
    execute_process(COMMAND rm -rf grpc
            WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/third_party)
endif()

# If the grpc/ directory does not exist it needs to be extracted from grpc.tar.gz
if(NOT EXISTS "${CMAKE_BINARY_DIR}/third_party/grpc")
    execute_process(COMMAND tar xC ${CMAKE_BINARY_DIR}/third_party -f ${CMAKE_SOURCE_DIR}/third_party/grpc.tar.gz)
    execute_process(COMMAND find grpc -exec touch {} +
            WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/third_party)
endif()

############################################################################
# Begin Google Test
############################################################################
# Set variables to build GTest
# The name of the GoogleTest library.
# This variable can be used throughout the code whenever the Google GTest library is needed
set(GTEST_LIBRARY gtest)
set(GTEST_SOURCE_DIR "${CMAKE_SOURCE_DIR}/third_party/googletest")
set(GTEST_INCLUDE_DIR "${GTEST_SOURCE_DIR}/include")

set(GTEST_BINARY_DIR "${CMAKE_BINARY_DIR}/third_party/bin/gtest")
# The path to the build GoogleTest library.
set(GTEST_LIBRARY_PATH ${GTEST_BINARY_DIR}/${CMAKE_STATIC_LIBRARY_PREFIX}${GTEST_LIBRARY}${CMAKE_STATIC_LIBRARY_SUFFIX})

#The commands needed to build the library
set(GTEST_BUILD_COMMAND ${CMAKE_CXX_COMPILER} -isystem ${GTEST_INCLUDE_DIR} -I ${GTEST_SOURCE_DIR} -c ${GTEST_SOURCE_DIR}/src/gtest-all.cc -o ${GTEST_BINARY_DIR}/gtest-all.o ${CMAKE_THREAD_LIBS_INIT})
set(GTEST_BUILD_LIB_COMMAND ${CMAKE_AR} -rv ${GTEST_LIBRARY_PATH} ${GTEST_BINARY_DIR}/gtest-all.o)

# This external project will build the GoogleTest library and copy it.
ExternalProject_add(google-gtest
        DOWNLOAD_COMMAND ""
        SOURCE_DIR ${GTEST_SOURCE_DIR}
        BINARY_DIR ${GTEST_BINARY_DIR}
        CONFIGURE_COMMAND ""
        BUILD_COMMAND ${GTEST_BUILD_COMMAND} COMMAND ${GTEST_BUILD_LIB_COMMAND}
        INSTALL_COMMAND ""
        LOG_DOWNLOAD ON
        LOG_BUILD ON
        )

# Set the include directories for GTest
include_directories(SYSTEM ${GTEST_INCLUDE_DIR})
install(DIRECTORY ${GTEST_INCLUDE_DIR}/
        DESTINATION ${INCLUDE_DIR})

InstallExternalLibrary(${GTEST_LIBRARY} ${GTEST_LIBRARY_PATH} google-gtest)


############################################################################
# Begin JSONCPP
############################################################################
# Set variables to build json-cpp
# The name of the GoogleTest library.
# This variable can be used throughout the code whenever the json-cpp library is needed
set(JSONCPP_LIBRARY jsoncpp)
set(JSONCPP_SOURCE_DIR "${CMAKE_SOURCE_DIR}/third_party/jsoncpp")
set(JSONCPP_INCLUDE_DIR "${JSONCPP_SOURCE_DIR}")
set(JSONCPP_BINARY_DIR "${CMAKE_BINARY_DIR}/third_party/bin/jsoncpp")
set(JSONCPP_LIBRARY_PATH ${JSONCPP_BINARY_DIR}/${CMAKE_STATIC_LIBRARY_PREFIX}${JSONCPP_LIBRARY}${CMAKE_STATIC_LIBRARY_SUFFIX})

set(JSONCPP_BUILD_COMMAND ${CMAKE_CXX_COMPILER} -isystem ${JSONCPP_INCLUDE_DIR} -I ${JSONCPP_INCLUDE_DIR} -c ${JSONCPP_SOURCE_DIR}/jsoncpp.cpp -o ${JSONCPP_BINARY_DIR}/jsoncpp.o)
set(JSONCPP_BUILD_LIB_COMMAND ${CMAKE_AR} -rv ${JSONCPP_LIBRARY_PATH} ${JSONCPP_BINARY_DIR}/jsoncpp.o)


# This external project will build the json-cpp library and copy it.
ExternalProject_add(jsoncpp_project
        DOWNLOAD_COMMAND ""
        SOURCE_DIR ${JSONCPP_SOURCE_DIR}
        BINARY_DIR ${JSONCPP_BINARY_DIR}
        CONFIGURE_COMMAND ""
        BUILD_COMMAND ${JSONCPP_BUILD_COMMAND} COMMAND ${JSONCPP_BUILD_LIB_COMMAND}
        INSTALL_COMMAND ""
        LOG_DOWNLOAD ON
        LOG_BUILD ON
        )

include_directories(SYSTEM ${JSONCPP_INCLUDE_DIR})

install(DIRECTORY ${JSONCPP_INCLUDE_DIR}/
         DESTINATION ${INCLUDE_DIR})

InstallExternalLibrary(${JSONCPP_LIBRARY} ${JSONCPP_LIBRARY_PATH} jsoncpp_project)



############################################################################
# Begin Google Mock
############################################################################
set(GMOCK_LIBRARY gmock)
set(GMOCK_SOURCE_DIR "${CMAKE_SOURCE_DIR}/third_party/googlemock")
set(GMOCK_INCLUDE_DIR "${GMOCK_SOURCE_DIR}/include")

set(GMOCK_BINARY_DIR "${CMAKE_BINARY_DIR}/third_party/bin/gmock")
set(GMOCK_LIBRARY_PATH ${GMOCK_BINARY_DIR}/${CMAKE_STATIC_LIBRARY_PREFIX}${GMOCK_LIBRARY}${CMAKE_STATIC_LIBRARY_SUFFIX})

set(GMOCK_BUILD_COMMAND ${CMAKE_CXX_COMPILER} -isystem ${GTEST_INCLUDE_DIR} -I ${GTEST_SOURCE_DIR} -isystem ${GMOCK_INCLUDE_DIR} -I${GMOCK_SOURCE_DIR} -c ${GMOCK_SOURCE_DIR}/src/gmock-all.cc -o ${GMOCK_BINARY_DIR}/gmock-all.o ${CMAKE_THREAD_LIBS_INIT})
set(GMOCK_BUILD_LIB_COMMAND ${CMAKE_AR} -rv ${GMOCK_LIBRARY_PATH} ${GTEST_BINARY_DIR}/gtest-all.o ${GMOCK_BINARY_DIR}/gmock-all.o)

ExternalProject_Add(google-gmock
        DEPENDS google-gtest
        DOWNLOAD_COMMAND ""
        SOURCE_DIR ${GMOCK_SOURCE_DIR}
        BINARY_DIR ${GMOCK_BINARY_DIR}
        CONFIGURE_COMMAND ""
        BUILD_COMMAND ${GMOCK_BUILD_COMMAND} COMMAND ${GMOCK_BUILD_LIB_COMMAND}
        INSTALL_COMMAND ""
        LOG_BUILD ON
        LOG_INSTALL ON)

include_directories(SYSTEM ${GMOCK_INCLUDE_DIR})

# Install the contents of GTest directory to the given location
install(DIRECTORY ${GMOCK_INCLUDE_DIR}/
        DESTINATION ${INCLUDE_DIR})

# Setup the library and install the library files.
InstallExternalLibrary(${GMOCK_LIBRARY} ${GMOCK_LIBRARY_PATH} google-gmock)

find_package (Threads REQUIRED)
set_property(TARGET ${GMOCK_LIBRARY} APPEND PROPERTY
        IMPORTED_LINK_LIBRARIES ${CMAKE_THREAD_LIBS_INIT})


if (RMANRASNET)
    ############################################################################
    # Begin Google Protobuf
    ############################################################################
    # TODO: Convert the Google Protobuf build process to use External Project once we give up Autotools

    set(THIRD_PARTY_DIR "${CMAKE_BINARY_DIR}/third_party")
    # Google Protobuf is a third_party dependency of GRPC so we define the variables here.
    set(GRPC_SOURCE_DIR "${CMAKE_BINARY_DIR}/third_party/grpc")
    set(GRPC_BINARY_DIR "${CMAKE_BINARY_DIR}/third_party/bin/grpc")

    set(PROTOBUF_SOURCE_DIR "${GRPC_SOURCE_DIR}/third_party/protobuf")
    set(PROTOBUF_BINARY_DIR "${CMAKE_BINARY_DIR}/third_party/bin/protobuf")
    set(PROTOBUF_INCLUDE_DIR "${PROTOBUF_BINARY_DIR}/include")

    set(PROTOBUF_LIBRARY protobuf)
    set(PROTOBUF_LIBRARY_PATH "${PROTOBUF_BINARY_DIR}/lib/${CMAKE_STATIC_LIBRARY_PREFIX}${PROTOBUF_LIBRARY}${CMAKE_STATIC_LIBRARY_SUFFIX}")

    set(PROTOBUF_PROTOC_EXECUTABLE_PATH "${PROTOBUF_BINARY_DIR}/bin/protoc")
    set(PROTOBUF_PROTOC_EXECUTABLE protoc)

    # NOTE: we need to change to an existing gmock-1.7.0 for downloading (the old one does not exist)
    set(OLD_GMOCK_URL "https://googlemock.googlecode.com/files/gmock-1.7.0.zip")
    set(NEW_GMOCK_URL "http://rasdaman.org/chrome/site/gmock-1.7.0.zip")

    # Get the number of cores we can use to build the third_party dependency.
    GetProcessorCount(PROCESSOR_COUNT)

    add_custom_command(OUTPUT ${PROTOBUF_LIBRARY_PATH} ${PROTOBUF_PROTOC_EXECUTABLE_PATH}
	    # Change the URL for gmock before invoking script to build protobuf
	    COMMAND sed -i "s@${OLD_GMOCK_URL}@${NEW_GMOCK_URL}@g" autogen.sh
            COMMAND ./autogen.sh
            COMMAND sh -c "CXXFLAGS=\"-O2\" ./configure --prefix=${PROTOBUF_BINARY_DIR} --disable-shared"
            COMMAND export MAKEFLAGS_AUX=$MAKEFLAGS && export MAKEOVERRIDES_AUX=$MAKEOVERRIDES
            && export MAKEFLAGS= && export MAKEOVERRIDES=
            && $(MAKE) -j${PROCESSOR_COUNT}
            && export MAKEFLAGS=$MAKEFLAGS_AUX && export MAKEOVERRIDES=$MAKEOVERRIDES_AUX
            COMMAND $(MAKE) ARGS -j install
            WORKING_DIRECTORY ${PROTOBUF_SOURCE_DIR}
            COMMENT "Building Google Protocol Buffers library."
            VERBATIM)

    add_custom_target(google-protobuf
            DEPENDS ${PROTOBUF_LIBRARY_PATH})

    # Set the Protobuf include directory as a system include.
    include_directories(SYSTEM ${PROTOBUF_INCLUDE_DIR})

    # Install the Protobuf include files to our include dir.
    install(DIRECTORY ${PROTOBUF_INCLUDE_DIR}/
            DESTINATION ${INCLUDE_DIR})

    InstallExternalLibrary(${PROTOBUF_LIBRARY} ${PROTOBUF_LIBRARY_PATH} google-protobuf)
    AddExternalExecutable(${PROTOBUF_PROTOC_EXECUTABLE} ${PROTOBUF_PROTOC_EXECUTABLE_PATH} google-protobuf)

    ############################################################################
    # Begin Google RPC
    ############################################################################
    # TODO: Convert the GRPC build process to use External Project once we give up Autotools

    set(GRPC_INCLUDE_DIR ${GRPC_BINARY_DIR}/include)

    # GRPC Library
    set(GRPC_LIBRARY grpc_unsecure)
    set(GRPC_LIBRARY_PATH "${GRPC_BINARY_DIR}/lib/${CMAKE_STATIC_LIBRARY_PREFIX}${GRPC_LIBRARY}${CMAKE_STATIC_LIBRARY_SUFFIX}")
    # GRPC++ library
    set(GRPCPP_LIBRARY grpc++_unsecure)
    set(GRPCPP_LIBRARY_PATH "${GRPC_BINARY_DIR}/lib/${CMAKE_STATIC_LIBRARY_PREFIX}${GRPCPP_LIBRARY}${CMAKE_STATIC_LIBRARY_SUFFIX}")
    # GRPC CPP Plugin
    set(GRPC_CPP_PLUGIN_EXECUTABLE_PATH ${GRPC_BINARY_DIR}/bin/grpc_cpp_plugin)
    set(GRPC_CPP_PLUGIN_EXECUTABLE grpc_cpp_plugin)

    GetProcessorCount(PROCESSOR_COUNT)
    add_custom_command(OUTPUT ${GRPC_LIBRARY_PATH} ${GRPCPP_LIBRARY_PATH} ${GRPC_CPP_PLUGIN_EXECUTABLE_PATH}
            COMMAND export MAKEFLAGS_AUX=$MAKEFLAGS && export MAKEOVERRIDES_AUX=$MAKEOVERRIDES
            && sed -i "s|^CPPFLAGS += -g |CPPFLAGS += |" Makefile
            && export MAKEFLAGS= && export MAKEOVERRIDES=
            && $(MAKE) -j${PROCESSOR_COUNT} CONFIG=opt
            && export MAKEFLAGS=$MAKEFLAGS_AUX && export MAKEOVERRIDES=$MAKEOVERRIDES_AUX
            COMMAND $(MAKE) ARGS install prefix=${GRPC_BINARY_DIR} PROTOC=${PROTOBUF_PROTOC_EXECUTABLE_PATH}
            WORKING_DIRECTORY ${GRPC_SOURCE_DIR}
            COMMENT "Building Google GRPC library."
            VERBATIM)

    add_custom_target(google-grpc
            DEPENDS ${GRPC_LIBRARY_PATH} ${GRPCPP_LIBRARY_PATH})
    add_dependencies(google-grpc google-protobuf)

    include_directories(SYSTEM ${GRPC_INCLUDE_DIR})
    install(DIRECTORY ${GRPC_INCLUDE_DIR}/
            DESTINATION ${INCLUDE_DIR})

    find_package (Threads REQUIRED)

    InstallExternalLibrary(${GRPC_LIBRARY} ${GRPC_LIBRARY_PATH} google-grpc)
    set_property(TARGET ${GRPC_LIBRARY} APPEND PROPERTY
            IMPORTED_LINK_LIBRARIES ${CMAKE_THREAD_LIBS_INIT})

    InstallExternalLibrary(${GRPCPP_LIBRARY} ${GRPCPP_LIBRARY_PATH} google-grpc)
    set_property(TARGET ${GRPCPP_LIBRARY} APPEND PROPERTY
            IMPORTED_LINK_LIBRARIES ${CMAKE_THREAD_LIBS_INIT})

    AddExternalExecutable(${GRPC_CPP_PLUGIN_EXECUTABLE} ${GRPC_CPP_PLUGIN_EXECUTABLE_PATH} google-grpc)

    # A dependency of GRPC sets this variable for some reason.
    mark_as_advanced(BUILD_TESTING)

    ############################################################################
    # Begin Google Java RPC Compiler
    ############################################################################

    include("${CMAKE_SOURCE_DIR}/third_party/grpc-java/CMakeLists.txt")

endif (RMANRASNET)

############################################################################
# Begin Easyloggingpp (ELPP)
############################################################################

set(ELPP_SOURCE_DIR "${CMAKE_SOURCE_DIR}/third_party/easyloggingpp")
set(ELPP_INCLUDE_DIR "${ELPP_SOURCE_DIR}")

set(ELPP_LIBRARY easyloggingpp)
add_library(${ELPP_LIBRARY} STATIC ${ELPP_SOURCE_DIR}/easylogging++.cc)
target_include_directories(${ELPP_LIBRARY} PRIVATE include/
					SYSTEM INTERFACE include/)
install(FILES ${ELPP_INCLUDE_DIR}/easylogging++.h
        DESTINATION ${INCLUDE_DIR})
install(TARGETS ${ELPP_LIBRARY} ARCHIVE DESTINATION ${LIB_DIR})
