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

# This file contains a collection of CMake functions and macros used to simplify the build process.

# This function is used to generate C++ proto messages from Google Protobuf definition files.
# Syntax CompileProtobufFile(PROTO_FILE DESTINATION_DIR GENERATED_SOURCES)
#	PROTO_FILE: Path to the .proto file relative to the current source directory
#	DESTINATION_DIR: The directory where the generated files will be placed
#	GENERATED_SOURCES: The name of the variable that will store the list of generated files.
function(CompileProtobufFile PROTO_FILE DESTINATION_DIR GENERATED_SOURCES)
    get_filename_component(FILE_NAME_NO_EXT ${PROTO_FILE} NAME_WE)
    get_filename_component(SOURCE_DIR ${PROTO_FILE} PATH)

    set(SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/${SOURCE_DIR}")
    set(PROTO_FILE_PATH "${SOURCE_DIR}/${FILE_NAME_NO_EXT}.proto")

    add_custom_command(OUTPUT "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.pb.cc" "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.pb.h"
            COMMAND ${CMAKE_COMMAND} -E make_directory ${DESTINATION_DIR} # Make the destination directory if it doesn't exist
            # Compile the protobuf file
            COMMAND ${PROTOBUF_PROTOC_EXECUTABLE_PATH}
            ARGS --proto_path=${SOURCE_DIR} --cpp_out=${DESTINATION_DIR} ${PROTO_FILE_PATH}

            COMMAND ${SED_EXECUTABLE}
            ARGS -i 1i'\#pragma GCC diagnostic ignored \"-Wshadow\"' "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.pb.cc"
            COMMAND ${SED_EXECUTABLE}
            ARGS -i 1i'\#pragma GCC diagnostic ignored \"-Wunused-parameter\"' "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.pb.cc"
            COMMAND ${SED_EXECUTABLE}
            ARGS -i 1i'\#pragma GCC diagnostic ignored \"-Wsign-conversion\"' "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.pb.cc"
            COMMAND ${SED_EXECUTABLE}
            ARGS -i 1i'\#pragma GCC diagnostic ignored \"-Wshadow\"' "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.pb.h"

            # Remove the operator= definition as it messes up rascontrolgrammar.hh
            COMMAND ${SED_EXECUTABLE}
            ARGS -i '/DefineQueryCacheRule_Argument\(DefineQueryCacheRule_Argument&&/,+12d' "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.pb.h"
            COMMAND ${SED_EXECUTABLE}
            ARGS -i '/DefineQueryCacheRule\(DefineQueryCacheRule&&/,+12d' "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.pb.h"

            DEPENDS ${PROTO_FILE_PATH} ${PROTOBUF_PROTOC_EXECUTABLE})

    set(${GENERATED_SOURCES} ${${GENERATED_SOURCES}} ${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.pb.h ${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.pb.cc PARENT_SCOPE)
endfunction()

# This function is used to generate C++ GRPC files from Google Protobuf definition files.
# Syntax CompileGRPCFile(PROTO_FILE DESTINATION_DIR GENERATED_SOURCES)
#	PROTO_FILE: Path to the .proto file relative to the current source directory
#	DESTINATION_DIR: The directory where the generated files will be placed
#	GENERATED_SOURCES: The name of the variable that will store the list of generated files.
function(CompileGRPCFile PROTO_FILE DESTINATION_DIR GENERATED_SOURCES) #GENERATED_SOURCES
    get_filename_component(FILE_NAME_NO_EXT ${PROTO_FILE} NAME_WE)
    get_filename_component(SOURCE_DIR ${PROTO_FILE} PATH)

    set(SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/${SOURCE_DIR}")
    set(PROTO_FILE_PATH "${SOURCE_DIR}/${FILE_NAME_NO_EXT}.proto")

    add_custom_command(OUTPUT "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.grpc.pb.cc" "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.grpc.pb.h"
            COMMAND ${CMAKE_COMMAND} -E make_directory ${DESTINATION_DIR}

            COMMAND ${PROTOBUF_PROTOC_EXECUTABLE_PATH}
            ARGS --proto_path=${SOURCE_DIR} --grpc_out=${DESTINATION_DIR} --plugin=protoc-gen-grpc=${GRPC_CPP_PLUGIN_EXECUTABLE_PATH} ${PROTO_FILE_PATH}

            COMMAND ${SED_EXECUTABLE}
            ARGS -i 1i'\#pragma GCC diagnostic ignored \"-Wshadow\"' "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.grpc.pb.cc"

            COMMAND ${SED_EXECUTABLE}
            ARGS -i 1i'\#pragma GCC diagnostic ignored \"-Wsign-conversion\"' "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.grpc.pb.cc"

            COMMAND ${SED_EXECUTABLE}
            ARGS -i 1i'\#pragma GCC diagnostic ignored \"-Wunused-parameter\"' "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.grpc.pb.cc"

            COMMAND ${SED_EXECUTABLE}
            ARGS -i 1i'\#pragma GCC diagnostic ignored \"-Wshadow\"' "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.grpc.pb.h"

            DEPENDS ${PROTO_FILE_PATH} ${PROTOBUF_PROTOC_EXECUTABLE} ${GRPC_CPP_PLUGIN_EXECUTABLE})

    set(${GENERATED_SOURCES} ${${GENERATED_SOURCES}} "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.grpc.pb.cc" "${DESTINATION_DIR}/${FILE_NAME_NO_EXT}.grpc.pb.h" PARENT_SCOPE)
endfunction()

# This function is used to convert an underscore separated string to a pascal case string.
# Syntax UnderscoreToPascalCase(UNDERSCORE_STRING OPTION PASCAL_CASE_STRING)
#   UNDERSCORE_STRING: E.g. "bla_bla"
#   PASCAL_CASE_STRING: 
# Example: UnderscoreToPascalCase("bla_dla") -> "BlaDla"
function(UnderscoreToPascalCase UNDERSCORE_STRING PASCAL_CASE_STRING)
    execute_process(
            COMMAND ${CMAKE_COMMAND} -E echo ${UNDERSCORE_STRING}
            COMMAND ${SED_EXECUTABLE} -r "s/(^|_)([a-z])/\\U\\2/g"
            OUTPUT_VARIABLE OUTPUT
            OUTPUT_STRIP_TRAILING_WHITESPACE)

    set(${PASCAL_CASE_STRING} ${OUTPUT} PARENT_SCOPE)
endfunction()

# This function is used to extract the value of an option in a proto file.
# Syntax GetProtobufOption(SOURCE_FILE OPTION VALUE)
#	SOURCE_FILE: Path to the .proto file relative to the current source directory
#	OPTION: The name of the options that will be retrieved if it exists.
#	VALUE: The name of the variable that will contain the result.
function(GetProtobufOption SOURCE_FILE OPTION VALUE)
    # Check if the option java_package is used
    file(STRINGS ${SOURCE_FILE} PROTO_OPTION
            LIMIT_COUNT 1
            REGEX "^[\\t\\ ]*option[\\t\\ ]*${OPTION}[\\t\\ ]*")

    if (PROTO_OPTION)
        # Extract only the name of the package
        string(REGEX MATCH \"[a-zA-Z_\\.]*\" QUOTED_PROTO_OPTION ${PROTO_OPTION})
        string(STRIP ${QUOTED_PROTO_OPTION} QUOTED_PROTO_OPTION)

        string(LENGTH ${QUOTED_PROTO_OPTION} PROTO_OPTION_LENGTH)

        # Get the length of the package name excluding the " at the end.
        math(EXPR PROTO_OPTION_LENGTH "${PROTO_OPTION_LENGTH}-2")

        string(SUBSTRING ${QUOTED_PROTO_OPTION} 1 ${PROTO_OPTION_LENGTH} OPTION_VALUE)

        set(${VALUE} ${OPTION_VALUE} PARENT_SCOPE)
    else ()
        set(${VALUE} "" PARENT_SCOPE)
    endif ()
endfunction()

# This function is used to extract the package folder in which Java GRPC files will be placed.
# Syntax GetJavaProtoPackage(SOURCE_FILE PACKAGE_FOLDER)
#	SOURCE_FILE: Path to the .proto file relative to the current source directory
#	PACKAGE_FOLDER: The name of the variable that will contain the result.
#
# It uses both of the possible options for specifying a Java package.
# If the file contains:
#	option java_package = "org.package.name" the function will return org/package/name
# Otherwise, if the file contains:
#	package org.package.name; the function will return org/package/name

function(GetJavaProtoPackage SOURCE_FILE PACKAGE_FOLDER)
    GetProtobufOption(${SOURCE_FILE} "java_package" JAVA_PACKAGE_OPTION)

    if (JAVA_PACKAGE_OPTION)
        string(REPLACE "." "/" JAVA_PACKAGE_FOLDER ${JAVA_PACKAGE_OPTION})

        set(${PACKAGE_FOLDER} ${JAVA_PACKAGE_FOLDER} PARENT_SCOPE)
    else ()
        file(STRINGS ${SOURCE_FILE} JAVA_PACKAGE_OPTION
                LIMIT_COUNT 1
                REGEX "^[\\t\\ ]*package[\\t\\ ]*")

        if (JAVA_PACKAGE_OPTION)
            string(FIND ${JAVA_PACKAGE_OPTION} "package" JAVA_PACKAGE_START)
            string(LENGTH "package" PACKAGE_WORD_LENGTH)
            math(EXPR JAVA_PACKAGE_START "${JAVA_PACKAGE_START}+${PACKAGE_WORD_LENGTH}")

            string(FIND ${JAVA_PACKAGE_OPTION} ";" JAVA_PACKAGE_END)
            math(EXPR JAVA_PACKAGE_END "${JAVA_PACKAGE_END}-${JAVA_PACKAGE_START}+1")

            string(SUBSTRING ${JAVA_PACKAGE_OPTION} ${JAVA_PACKAGE_START} ${JAVA_PACKAGE_END} JAVA_PACKAGE)
            string(STRIP ${JAVA_PACKAGE} JAVA_PACKAGE)
            string(REPLACE "." "/" JAVA_PACKAGE_FOLDER ${JAVA_PACKAGE})

            set(${PACKAGE_FOLDER} ${JAVA_PACKAGE_FOLDER} PARENT_SCOPE)
        else ()
            set({PACKAGE_FOLDER} "" PARENT_SCOPE)
        endif ()
    endif ()

endfunction()

# This function is used to generate Java message files from Google Protobuf definition files.
# Syntax CompileJavaProtobufFile(PROTO_FILE DESTINATION_DIR GENERATED_SOURCES)
#	PROTO_FILE: Path to the .proto file, absolute or relative to the current source directory
#	DESTINATION_DIR: The directory where the generated files will be placed
#	GENERATED_SOURCES: The name of the variable that will store the list of generated files.
function(CompileJavaProtobufFile PROTO_FILE DESTINATION_DIR GENERATED_SOURCES)
    GetJavaProtoPackage(${PROTO_FILE} JAVA_PACKAGE_FOLDER)
    get_filename_component(FILE_NAME_NO_EXT ${PROTO_FILE} NAME_WE)
    get_filename_component(SOURCE_DIR ${PROTO_FILE} PATH)

    if (NOT IS_ABSOLUTE ${SOURCE_DIR})
        set(SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/${SOURCE_DIR}")
    endif ()

    set(PROTO_FILE_PATH "${SOURCE_DIR}/${FILE_NAME_NO_EXT}.proto")

    GetProtobufOption(${PROTO_FILE_PATH} "java_outer_classname" JAVA_CLASS)
    if (JAVA_CLASS)
        set(GENERATED_FILE_NAME_NO_EXT ${JAVA_CLASS})
    else ()
        UnderscoreToPascalCase(${FILE_NAME_NO_EXT} JAVA_CLASS)

        file(STRINGS ${PROTO_FILE_PATH} JAVA_CLASS_MATCHES
                LIMIT_COUNT 1
                REGEX "^[\\t\\ ]*(service|message)[\\t\\ ]*${JAVA_CLASS}[\\t\\ ]*({|$)")


        if (JAVA_CLASS_MATCHES)
            set(GENERATED_FILE_NAME_NO_EXT "${JAVA_CLASS}OuterClass")
        else ()
            set(GENERATED_FILE_NAME_NO_EXT ${JAVA_CLASS})
        endif ()
    endif ()

    set(GENERATED_FILE_PATH "${DESTINATION_DIR}/${JAVA_PACKAGE_FOLDER}/${GENERATED_FILE_NAME_NO_EXT}.java")

    add_custom_command(OUTPUT ${GENERATED_FILE_PATH}
            COMMAND ${CMAKE_COMMAND} -E make_directory ${DESTINATION_DIR} # Make the destination directory if it doesn't exist
            # Compile the protobuf file
            COMMAND ${PROTOBUF_PROTOC_EXECUTABLE_PATH}
            ARGS --proto_path=${SOURCE_DIR} --java_out=${DESTINATION_DIR} ${PROTO_FILE_PATH}

            DEPENDS ${PROTO_FILE_PATH} ${PROTOBUF_PROTOC_EXECUTABLE})

    set(${GENERATED_SOURCES} ${${GENERATED_SOURCES}} ${GENERATED_FILE_PATH} PARENT_SCOPE)
endfunction()

# This function is used to generate Java GRPC files from Google Protobuf definition files.
# Syntax CompileJavaGRPCFile(PROTO_FILE DESTINATION_DIR GENERATED_SOURCES)
#	PROTO_FILE: Path to the .proto file relative to the current source directory
#	DESTINATION_DIR: The directory where the generated files will be placed
#	GENERATED_SOURCES: The name of the variable that will store the list of generated files.
function(CompileJavaGRPCFile PROTO_FILE DESTINATION_DIR GENERATED_SOURCES)
    GetJavaProtoPackage(${PROTO_FILE} JAVA_PACKAGE_FOLDER)
    get_filename_component(FILE_NAME_NO_EXT ${PROTO_FILE} NAME_WE)
    get_filename_component(SOURCE_DIR ${PROTO_FILE} PATH)

    set(GENERATED_FILE_NAME_NO_EXT "")
    UnderscoreToPascalCase(${FILE_NAME_NO_EXT} GENERATED_FILE_NAME_NO_EXT)

    if (NOT IS_ABSOLUTE ${SOURCE_DIR})
        set(SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/${SOURCE_DIR}")
    endif ()

    set(PROTO_FILE_PATH "${SOURCE_DIR}/${FILE_NAME_NO_EXT}.proto")

    set(GENERATED_FILE_PATH "${DESTINATION_DIR}/${JAVA_PACKAGE_FOLDER}/${GENERATED_FILE_NAME_NO_EXT}Grpc.java")

    add_custom_command(OUTPUT ${GENERATED_FILE_PATH}
            COMMAND ${CMAKE_COMMAND} -E make_directory ${DESTINATION_DIR} # Make the destination directory if it doesn't exist
            # Compile the protobuf file
            COMMAND ${PROTOBUF_PROTOC_EXECUTABLE_PATH}
            ARGS --proto_path=${SOURCE_DIR} --grpc-java_out=${DESTINATION_DIR} --plugin=protoc-gen-grpc-java=${GRPC_JAVA_PLUGIN_EXECUTABLE_PATH} ${PROTO_FILE_PATH}

            DEPENDS ${PROTO_FILE_PATH} ${PROTOBUF_PROTOC_EXECUTABLE} ${GRPC_JAVA_PLUGIN_EXECUTABLE})

    set(${GENERATED_SOURCES} ${${GENERATED_SOURCES}} ${GENERATED_FILE_PATH} PARENT_SCOPE)
endfunction()

# This function is used to generate C++ files from files containing embedded SQL
# Syntax CompileEmbeddedSQL(FILE_NAME SOURCE_DIR DESTINATION_DIR GENERATED_SOURCES)
#	FILE_NAME: The name of the file containing embedded sql statements, without extension
# 	SOURCE_DIR: The source directory from which we read the source files.
#	DESTINATION_DIR: The directory where the generated files will be placed
#	GENERATED_SOURCES: The name of the variable that will store the list of generated files.
#
# The following variables must be set:
# EMBEDDEDSQLPRECOMPILER, EMBEDDEDSQLPRECOMPILER_FLAGS, EMBEDDEDSQL_EXT
function(CompileEmbeddedSQL FILE_NAME SOURCE_DIR DESTINATION_DIR GENERATED_SOURCES)
    if (NOT EMBEDDEDSQLPRECOMPILER)
        message(FATAL_ERROR "Please set the EMBEDDEDSQLPRECOMPILER variable.")
    endif ()

    if (NOT EMBEDDEDSQL_EXT)
        message(FATAL_ERROR "Please set the EMBEDDEDSQL_EXT variable.")
    endif ()
    
    add_custom_command(OUTPUT "${DESTINATION_DIR}/${FILE_NAME}.cc"
            COMMAND ${CMAKE_COMMAND} -E make_directory ${DESTINATION_DIR}
            COMMAND ${EMBEDDEDSQLPRECOMPILER} 
            ARGS "${FILE_NAME}.${EMBEDDEDSQL_EXT}" ${EMBEDDEDSQLPRECOMPILER_FLAGS} "${DESTINATION_DIR}/${FILE_NAME}.cc"
            DEPENDS "${FILE_NAME}.${EMBEDDEDSQL_EXT}"
            WORKING_DIRECTORY ${SOURCE_DIR}
            VERBATIM)

    set(${GENERATED_SOURCES} ${${GENERATED_SOURCES}} "${DESTINATION_DIR}/${FILE_NAME}.cc" PARENT_SCOPE)
endfunction()

# This function is used to install files only if they do not exist.
# Syntax install_if_not_exists(SOURCE DESTINATION)
# SOURCE: Path to the file to copy
# DESTINATION: Destination directory.
function(install_if_not_exists src dest)
    if (NOT IS_ABSOLUTE "${src}")
        set(src "${CMAKE_CURRENT_SOURCE_DIR}/${src}")
    endif ()

    get_filename_component(src_name "${src}" NAME)
    if (NOT IS_ABSOLUTE "${dest}")
        set(dest "${CMAKE_INSTALL_PREFIX}/${dest}")
    endif ()
    install(CODE "
    if(NOT EXISTS \"\$ENV{DESTDIR}${dest}/${src_name}\")
      #file(INSTALL \"${src}\" DESTINATION \"${dest}\")
      message(STATUS \"Installing: \$ENV{DESTDIR}${dest}/${src_name}\")
      execute_process(COMMAND \${CMAKE_COMMAND} -E copy \"${src}\"
                      \"\$ENV{DESTDIR}${dest}/${src_name}\"
                      RESULT_VARIABLE copy_result
                      ERROR_VARIABLE error_output)
      if(copy_result)
        message(FATAL_ERROR \${error_output})
      endif()
    else()
      message(STATUS \"Skipping  : \$ENV{DESTDIR}${dest}/${src_name}\")
    endif()
  ")
endfunction(install_if_not_exists)

# This function is used to install log files only if they do not exist or ar not
# changed.
# Syntax install_log_file(SOURCE DESTINATION)
# SOURCE: Path to the file to copy
# DESTINATION: Destination directory.
function(install_log_file src dest)
    if (NOT IS_ABSOLUTE "${src}")
        set(src "${CMAKE_CURRENT_SOURCE_DIR}/${src}")
    endif ()

    get_filename_component(src_name "${src}" NAME)
    if (NOT IS_ABSOLUTE "${dest}")
        set(dest "${CMAKE_INSTALL_PREFIX}/${dest}")
    endif ()
    install(CODE "
    if(NOT EXISTS \"\$ENV{DESTDIR}${dest}/${src_name}\")
      #file(INSTALL \"${src}\" DESTINATION \"${dest}\")
      message(STATUS \"Installing: \$ENV{DESTDIR}${dest}/${src_name}\")
      execute_process(COMMAND \${CMAKE_COMMAND} -E copy \"${src}\"
                      \"\$ENV{DESTDIR}${dest}/${src_name}\"
                      RESULT_VARIABLE copy_result
                      ERROR_VARIABLE error_output)
      if(copy_result)
        message(FATAL_ERROR \${error_output})
      endif()
    else()
      file(STRINGS \"\$ENV{DESTDIR}${dest}/${src_name}\" log_file_strings
           REGEX \"^//\")
      if (log_file_strings)
        message(STATUS \"Overwriting: \$ENV{DESTDIR}${dest}/${src_name}\")
        execute_process(COMMAND \${CMAKE_COMMAND} -E copy \"${src}\"
                        \"\$ENV{DESTDIR}${dest}/${src_name}\"
                        RESULT_VARIABLE copy_result
                        ERROR_VARIABLE error_output)
        if(copy_result)
          message(FATAL_ERROR \${error_output})
        endif()
      else()
        message(STATUS \"Skipping  : \$ENV{DESTDIR}${dest}/${src_name}\")
      endif()
    endif()
  ")
endfunction(install_log_file)

# Get the number of CPU on this machine.
# This function is used to set a concurrency limit for third_party make process.
# Syntax GetProcessorCount(PROCESSOR_COUNT)
# PROCESSOR_COUNT: Variable that will hold the number of CPU cores.
function(GetProcessorCount PROCESSOR_COUNT)
    # Unknown:
    set(PROCESSOR_COUNT 1)

    # Linux:
    set(cpuinfo_file "/proc/cpuinfo")
    if (EXISTS "${cpuinfo_file}")
        file(STRINGS "${cpuinfo_file}" procs REGEX "^processor.: [0-9]+$")
        list(LENGTH procs PROCESSOR_COUNT)
    endif ()

    # Mac:
    if (APPLE)
        find_program(cmd_sys_pro "system_profiler")
        if (cmd_sys_pro)
            execute_process(COMMAND ${cmd_sys_pro} OUTPUT_VARIABLE info)
            string(REGEX REPLACE "^.*Total Number Of Cores: ([0-9]+).*$" "\\1"
                    PROCESSOR_COUNT "${info}")
        endif ()
    endif ()

    # Windows:
    if (WIN32)
        set(PROCESSOR_COUNT "$ENV{NUMBER_OF_PROCESSORS}")
    endif ()

    set(PROCESSOR_COUNT ${PROCESSOR_COUNT} PARENT_SCOPE)
endfunction()

function(GetVersionInformation RMANVERSION GCCVERSION GCCTARGET)
    find_package(Git REQUIRED)
    find_package(SED REQUIRED)

    execute_process(COMMAND ${GIT_EXECUTABLE} describe --tags HEAD
            COMMAND ${SED_EXECUTABLE} "s/-[0-9]*-/-/1"
            WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
            OUTPUT_VARIABLE RMANVERSION
            ERROR_QUIET
            OUTPUT_QUIET
            OUTPUT_STRIP_TRAILING_WHITESPACE)

    if (NOT RMANVERSION)
        set(RMANVERSION ${PROJECT_VERSION})
    endif ()

    execute_process(COMMAND ${CMAKE_CXX_COMPILER} --version
            OUTPUT_VARIABLE GCCVERSION
            OUTPUT_STRIP_TRAILING_WHITESPACE)

    execute_process(COMMAND echo ${GCCVERSION}
            COMMAND head -n 1
            WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
            OUTPUT_VARIABLE GCCVERSION
            OUTPUT_STRIP_TRAILING_WHITESPACE)

    execute_process(COMMAND ${CMAKE_CXX_COMPILER} -v
            ERROR_VARIABLE GCCTARGET
            ERROR_STRIP_TRAILING_WHITESPACE)

    execute_process(COMMAND echo ${GCCTARGET}
            COMMAND grep Target:
            COMMAND ${SED_EXECUTABLE} "s/Target: //"
            WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
            OUTPUT_VARIABLE GCCTARGET
            OUTPUT_STRIP_TRAILING_WHITESPACE
            )

    set(RMANVERSION ${RMANVERSION} PARENT_SCOPE)
    set(GCCVERSION ${GCCVERSION} PARENT_SCOPE)
    set(GCCTARGET ${GCCTARGET} PARENT_SCOPE)
endfunction()
