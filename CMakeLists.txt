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

cmake_minimum_required(VERSION 3.4 FATAL_ERROR)

# Set cmake policies for compatibility
cmake_policy(SET CMP0012 NEW)
cmake_policy(SET CMP0009 NEW)
cmake_policy(SET CMP0048 NEW)

project(rasdaman VERSION 9.7.0 LANGUAGES C CXX)

# No rebuild dependency when make install
# https://cmake.org/Wiki/CMake_Useful_Variables#Various_Options
set(CMAKE_SKIP_RULE_DEPENDENCY TRUE)
set(CMAKE_SKIP_INSTALL_ALL_DEPENDENCY TRUE)

################################################################################
# CMake configuration options
################################################################################

# This sets the folder from which cmake modules can be loaded.
# When find_package(X) is called, the module path is search of a FindX.cmake
SET(CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake" ${CMAKE_MODULE_PATH})
SET(CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake/modules" ${CMAKE_MODULE_PATH})

set(CMAKE_VERBOSE_OUTPUT OFF CACHE BOOL 
    "Enable this if you need detailed output from the make process.")
if (CMAKE_VERBOSE_OUTPUT)
    set(CMAKE_VERBOSE_MAKEFILE ON)
endif ()

# Override the default installation directory
if (CMAKE_INSTALL_PREFIX_INITIALIZED_TO_DEFAULT)
    set(CMAKE_INSTALL_PREFIX "/opt/rasdaman" CACHE PATH "Installation directory." FORCE)
endif ()

set(CPP_FILE_EXTENSIONS *.cc *.hh *.sc *.icc)

################################################################################
# Configure options
################################################################################

# Add make unit-check target. Running make unit-check will run the unit tests.
include(CTest)
add_custom_target(unit-check COMMAND ${CMAKE_CTEST_COMMAND})

# Add make format target. This will format all the files in the source tree.
include(${CMAKE_CURRENT_SOURCE_DIR}/cmake/ExtraTargets.cmake)
AddFormatTarget("${CMAKE_SOURCE_DIR}/third_party" "*.cc *.hh")

include(CheckLibraryExists)

# Include the custom functions and macros so that they are available in the subdirs
include(cmake/Functions.cmake)

# Check for functions and symbols needed by rasdaman
include(cmake/CheckForRequiredSymbols.cmake)

# Set a default build type if none was specified
if(NOT CMAKE_BUILD_TYPE AND NOT CMAKE_CONFIGURATION_TYPES)
    message(STATUS "Setting build type to 'Release' as none was specified.")
    set(CMAKE_BUILD_TYPE Release CACHE STRING "Choose the type of build." FORCE)
    # Set the possible values of build type for cmake-gui
    set_property(CACHE CMAKE_BUILD_TYPE PROPERTY STRINGS "Debug" "Release"
      "MinSizeRel" "RelWithDebInfo")
else()
    message(STATUS "Build type: ${CMAKE_BUILD_TYPE}")
endif()

#
# Enable C++11
#
include(CheckCXXCompilerFlag)

CHECK_CXX_COMPILER_FLAG("-std=c++11" COMPILER_SUPPORTS_CXX11)
if (COMPILER_SUPPORTS_CXX11)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
else ()
    message(FATAL_ERROR "The compiler ${CMAKE_CXX_COMPILER} has no C++11 support.")
endif ()

set(ASAN_FLAG "")
set(ENABLE_ASAN OFF CACHE BOOL
    "Use AddressSanitizer (compile with -fsanitize=address).")
if (ENABLE_ASAN)
    set(ASAN_FLAG "-fsanitize=address")
    message(STATUS "AddressSanitizer enabled (-DENABLE_ASAN=${ENABLE_ASAN}).")
endif ()

SET(CMAKE_CXX_FLAGS_DEBUG "-O0 -g3 -ggdb3 -gdwarf-2 -fno-omit-frame-pointer -rdynamic ${ASAN_FLAG}")
SET(CMAKE_C_FLAGS_DEBUG "-O0 -g3 -ggdb3 -gdwarf-2 -fno-omit-frame-pointer -rdynamic ${ASAN_FLAG}")
SET(CMAKE_CXX_FLAGS_RELEASE "-O3 -ftree-vectorize -fno-omit-frame-pointer ${ASAN_FLAG}")
SET(CMAKE_C_FLAGS_RELEASE "-O3 -ftree-vectorize -fno-omit-frame-pointer ${ASAN_FLAG}")

# Disable default log file from easylogging, otherwise we get by default
# logs/myeasylog.log
add_definitions(-DELPP_NO_DEFAULT_LOG_FILE)
add_definitions(-DELPP_FEATURE_CRASH_LOG)
add_definitions(-DELPP_STACKTRACE)
add_definitions(-DELPP_DISABLE_DEFAULT_CRASH_HANDLING)
add_definitions(-DELPP_NO_CHECK_MACROS)

# For development
set(ENABLE_DEBUG OFF CACHE BOOL 
    "Generate (slower) binaries that can be debugged / produce debug logs.")
if (ENABLE_DEBUG)
    add_definitions(-DRASDEBUG)
    set(RMANDEBUG ${ENABLE_DEBUG})
    set(DEBUG ${ENABLE_DEBUG})

    message(STATUS "Compile in debug mode enabled " 
        "(-DENABLE_DEBUG=${ENABLE_DEBUG}).")
else ()
    message(STATUS "Compile in debug mode disabled " 
        "(-DENABLE_DEBUG=${ENABLE_DEBUG}).")
endif ()

# If release and not ENABLE_DEBUG, disable Debug and Trace logs
string(TOUPPER ${CMAKE_BUILD_TYPE} uppercase_CMAKE_BUILD_TYPE)
if (uppercase_CMAKE_BUILD_TYPE STREQUAL "RELEASE")
    if(NOT ENABLE_DEBUG)
        set(_DISABLE_TRACE_LOGS 1)
        add_definitions(-DELPP_DISABLE_TRACE_LOGS)
        add_definitions(-DELPP_DISABLE_VERBOSE_LOGS)
        add_definitions(-DNDEBUG)
    endif()
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${CMAKE_CXX_FLAGS_RELEASE}")
    # Used by some performance implementation of math libraries.
    set(__NO_MATH_INLINES 1)
elseif (uppercase_CMAKE_BUILD_TYPE STREQUAL "DEBUG")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${CMAKE_CXX_FLAGS_DEBUG}")
    # thread-safe logging if in Debug mode
    #add_definitions(-DELPP_THREAD_SAFE)
endif()

#
# Options for selecting the network protocol
#
set(NETWORK_PROTOCOL rasnet CACHE STRING 
    "Set the network protocol used in rasdaman (rasnet or rnp).")
set_property(CACHE NETWORK_PROTOCOL PROPERTY STRINGS rasnet rnp)
message(STATUS "Building rasdaman with the ${NETWORK_PROTOCOL} protocol (-DNETWORK_PROTOCOL=${NETWORK_PROTOCOL}).")
if (${NETWORK_PROTOCOL} STREQUAL rasnet)
    set(RMANRASNET 1)
    set(DISABLE_PROTOBUF_WARNINGS_FLAGS " -Wno-sign-conversion -Wno-shadow -Wno-pedantic -Wno-undef ")    
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -pthread")
else ()
    set(RMANRASNET 0)
endif ()

#
# RASBASE
#
set(DEFAULT_BASEDB "sqlite" CACHE STRING 
    "Specify the DBMS that rasdaman uses for storing RASBASE.")
set_property(CACHE DEFAULT_BASEDB PROPERTY STRINGS sqlite postgresql)

find_library(NSL_LIBRARY nsl DOC "The NSL library.")
mark_as_advanced(NSL_LIBRARY)
set(BASEDB_LIBRARIES ${NSL_LIBRARY})
set(BASEDB_INCLUDE_DIR "")
find_package(ZLIB REQUIRED)

if (${DEFAULT_BASEDB} STREQUAL sqlite)

    find_package(Sqlite REQUIRED)
    # For config-cmake.h
    set(BASEDB_SQLITE 1)
    set(BASEDBSTRING sqlite)

    # Used in subdirectories
    set(EMBEDDEDSQL_EXT sc)
    set(EMBEDDEDSQL_OUT cc)

    set(EMBEDDEDSQLPRECOMPILER cp)
    set(EMBEDDEDSQLPRECOMPILER_FLAGS "-a")

    set(BASEDB_LIBRARIES ${BASEDB_LIBRARIES} ${SQLITE_LIBRARIES})
    set(BASEDB_INCLUDE_DIR ${BASEDB_INCLUDE_DIR} ${SQLITE_INCLUDE_DIR})

elseif (${DEFAULT_BASEDB} STREQUAL postgresql)

    set(POSTGRESQL_MIN_VERSION 9.1.0)

    # Used in config-cmake.h
    set(BASEDB_PGSQL 1)
    set(BASEDBSTRING pgsql)

    # Used in subdirectories
    set(EMBEDDEDSQL_EXT pgc)
    set(EMBEDDEDSQL_OUT cc)

    find_package(PostgreSQL REQUIRED)
    if (${PostgreSQL_VERSION_STRING} VERSION_LESS ${POSTGRESQL_MIN_VERSION})
        message(FATAL_ERROR "PostgreSQL newer than ${POSTGRESQL_MIN_VERSION} not found.")
    endif ()

    # TODO may not be necessary
    check_library_exists(pq PQexec "" HAVE_LIBPQ)
    check_library_exists(pgtypes PGTYPESnumeric_add "" HAVE_LIBPGTYPES)
    if ((NOT ${HAVE_LIBPQ}) OR (NOT ${HAVE_LIBPGTYPES}))
        message(FATAL_ERROR "libpq not found, please install.")
    endif ()

    find_program(ECPG ecpg DOC "A precompiler for PostgreSQL embedded code.")
    set(EMBEDDEDSQLPRECOMPILER "${ECPG}")
    set(EMBEDDEDSQLPRECOMPILER_FLAGS  "-o")

    find_library(ECPG_LIBARY ecpg DOC "The ECPG library.")
    find_package(ZLIB REQUIRED)

    set(BASEDB_LIBRARIES ${BASEDB_LIBRARIES} ${PostgreSQL_LIBRARIES} 
                         ${ZLIB_LIBRARIES} ${ECPG_LIBARY})
    set(BASEDB_INCLUDE_DIR ${BASEDB_INCLUDE_DIR} ${PostgreSQL_INCLUDE_DIRS})

else ()

    message(FATAL_ERROR "Unsupported base DBMS '${DEFAULT_BASEDB}'.")

endif ()

message(STATUS "Building rasdaman with support for ${DEFAULT_BASEDB} "
    "(-DDEFAULT_BASEDB=${DEFAULT_BASEDB}).")

#
# Format support
#
message(STATUS "Support for data formats:")

set(USE_HDF4 OFF CACHE BOOL "Enable inclusion of HDF4 library during installation.")
if (USE_HDF4)
    set(HAVE_HDF ON) # Flag used in config-cmake.h
    message(STATUS "  HDF4 enabled (-DUSE_HDF4=${USE_HDF4}).")
else()
    message(STATUS "  HDF4 disabled (-DUSE_HDF4=${USE_HDF4}).")
endif ()

set(USE_NETCDF OFF CACHE BOOL "Enable inclusion of netCDF library during installation.")
if (USE_NETCDF)
    set(HAVE_NETCDF ON) # Flag used in config-cmake.h
    message(STATUS "  NetCDF enabled (-DUSE_NETCDF=${USE_NETCDF}).")
else()
    message(STATUS "  NetCDF disabled (-DUSE_NETCDF=${USE_NETCDF}).")
endif ()

set(USE_GDAL ON CACHE BOOL "Enable inclusion of GDAL library during installation. ")
if (USE_GDAL)
    set(HAVE_GDAL ON) # Flag used in config-cmake.h

    message(STATUS "  GDAL enabled (-DUSE_GDAL=${USE_GDAL}).")
else()
    message(STATUS "  GDAL disabled (-DUSE_GDAL=${USE_GDAL}).")
endif ()

set(USE_GRIB OFF CACHE BOOL "Enable inclusion of GRIB library during installation.")
if (USE_GRIB)
    set(HAVE_GRIB ON) # Flag used in config-cmake.h

    message(STATUS "  GRIB enabled (-DUSE_GRIB=${USE_GRIB}).")
else()
    message(STATUS "  GRIB disabled (-DUSE_GRIB=${USE_GRIB}).")
endif ()

#
# Java
#
set(ENABLE_JAVA ON CACHE BOOL
    "Generate and install of Java-based components (rasj, petascope, secore).")
if (ENABLE_JAVA)
    # This variable is necessary to allow the Java components to be built.
    # Petascope depends on rasj being built to this directory.
    set(RASJ_TARGET_DIR "${CMAKE_BINARY_DIR}/java/target")
    file(MAKE_DIRECTORY ${RASJ_TARGET_DIR})

    find_package(Java COMPONENTS Runtime REQUIRED)
    find_package(Java COMPONENTS Development REQUIRED)

    # gdal-java
    find_library(GDAL_JAVA_VERSION_1 NAMES libgdaljni.so
                 PATHS /usr/lib/java/gdal /usr/lib/jni/)

    if (NOT GDAL_JAVA_VERSION_1)
        # gdal-java is installed with version 2.x
        find_library(GDAL_JAVA_VERSION_2 NAMES libgdalalljni.so
                 PATHS /usr/lib/java/gdal /usr/lib/jni/)

        if (NOT GDAL_JAVA_VERSION_2)
            # both version 1.x and 2.x not installed
            message(FATAL_ERROR "gdal-java not found.")
        else ()
            get_filename_component(GDAL_JAVA_DIR ${GDAL_JAVA_VERSION_2} DIRECTORY)
            message(STATUS "Found gdal-java: ${GDAL_JAVA_DIR}")
            set(GDAL_JAVA_DIR ${GDAL_JAVA_DIR})
            # used in Petascope-core pom.xml.in
            set(GDAL_JAVA_VERSION "2.3.0")
        endif()    
    else ()
        # gdal-java is installed with version 1.x
        get_filename_component(GDAL_JAVA_DIR ${GDAL_JAVA_VERSION_1} DIRECTORY)
        message(STATUS "Found gdal-java: ${GDAL_JAVA_DIR}")
        set(GDAL_JAVA_DIR ${GDAL_JAVA_DIR})
        # used in Petascope-core pom.xml.in
        set(GDAL_JAVA_VERSION "1.10.1")
    endif()
    

    # R
    set(ENABLE_R OFF CACHE BOOL "Compile and install R package.")

    # deployment mode
    set(JAVA_SERVER "external" CACHE STRING 
        "Set the Java application deployment mode (external or embedded)")
    set_property(CACHE JAVA_SERVER PROPERTY STRINGS external embedded)
    if (${JAVA_SERVER} STREQUAL external)
        set(JAVA_SERVER_EXTERNAL true)
    elseif (${JAVA_SERVER} STREQUAL embedded)
        set(JAVA_SERVER_EMBEDDED true)
    else ()
        message(FATAL_ERROR "Invalid JAVA_SERVER option '${JAVA_SERVER}'.")
    endif ()

    message(STATUS "Java-based components (rasj, petascope, secore) enabled " 
        "(-DENABLE_JAVA=${ENABLE_JAVA}).")
else()
    message(STATUS "Java-based components (rasj, petascope, secore) disabled " 
        "(-DENABLE_JAVA=${ENABLE_JAVA}).")
endif ()

#
# Various
#

set(GENERATE_PIC ON CACHE BOOL "Generate position independent code (PIC)")
if (GENERATE_PIC)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")

    message(STATUS "Position independent code (PIC) enabled " 
        "(-DGENERATE_PIC=${GENERATE_PIC}).")
else()
    message(STATUS "Position independent code (PIC) disabled " 
        "(-DGENERATE_PIC=${GENERATE_PIC}).")
endif ()

set(ENABLE_BENCHMARK OFF CACHE BOOL 
    "Generate binaries that contain extra code for benchmark output. ")
if (ENABLE_BENCHMARK)
    add_definitions(-DRMANBENCHMARK)

    message(STATUS "Benchmarking of queries enabled " 
        "(-DENABLE_BENCHMARK=${ENABLE_BENCHMARK}).")
else()
    message(STATUS "Benchmarking of queries disabled " 
        "(-DENABLE_BENCHMARK=${ENABLE_BENCHMARK}).")
endif ()

set(ENABLE_PROFILING OFF CACHE BOOL 
    "Enable profiling with google-perftools.")
if (ENABLE_PROFILING)
    find_package(GooglePerftools)
    if (GOOGLEPERFTOOLS_FOUND)
        add_definitions(-DGPERFTOOLS_ENABLED)
        add_definitions(-DENABLE_PROFILING)
    endif ()

    message(STATUS "Profiling with google-perftools enabled " 
        "(-DENABLE_PROFILING=${ENABLE_PROFILING}).")
else()
    message(STATUS "Profiling with google-perftools disabled " 
        "(-DENABLE_PROFILING=${ENABLE_PROFILING}).")
endif ()

set(ENABLE_STRICT OFF CACHE BOOL 
    "Enable compilation in strict mode (warnings terminate compilation).")
if (ENABLE_STRICT)
    # list from http://stackoverflow.com/a/9862800
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -pedantic -Werror -Wall -Wextra -Wshadow ")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wcast-align -Wcast-qual -Wsign-promo")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wctor-dtor-privacy -Wdisabled-optimization")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wformat=2 -Winit-self -Wlogical-op")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wmissing-declarations -Wmissing-include-dirs")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wnoexcept -Woverloaded-virtual -Wredundant-decls")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wsign-conversion -Wstrict-null-sentinel")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wstrict-overflow=5 -Wswitch-default -Wundef")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-unused -Wno-long-long -Wno-format-nonliteral")

    message(STATUS "Strict mode (warnings terminate compilation) " 
        "enabled (-DENABLE_STRICT=${ENABLE_STRICT}).")
else()
    message(STATUS "Strict mode (warnings terminate compilation) " 
        "disabled (-DENABLE_STRICT=${ENABLE_STRICT}).")
endif ()

set(GENERATE_DOCS OFF CACHE BOOL "Build and install documentation.")
if (GENERATE_DOCS)
    message(STATUS "Documentation building enabled " 
        "(-DGENERATE_DOCS=${GENERATE_DOCS}).")
else()
    message(STATUS "Documentation building disabled " 
        "(-DGENERATE_DOCS=${GENERATE_DOCS}).")
endif()

########################################
# Begin backward compatibility section
########################################

#TODO: The following variables could be factored out with some care
#Enable the standard library
set(CPPSTDLIB 1)

# The name of the architecture for which the system is compiling.
set(RASARCHITECTURE "X86")
set(X86 1)
set(EARLY_TEMPLATE 1)
set(NOPRE 1)
set(NO_in_addr_t 1)
set(RASSCHEMAVERSION 5)

include(TestBigEndian)
test_big_endian(IS_BIG_ENDIAN)
if (NOT ${IS_BIG_ENDIAN})
    set(IS_LITTLE_ENDIAN 1)
endif ()

add_definitions(-DCOMPDATE="2017-08-24")

# TODO: These do not seem safe but are kept because the old code requires them
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-stack-protector -fno-strict-overflow")

########################################
# End backward compatibility section
########################################

#
# Paths
#

set(INCLUDE_DIR "${CMAKE_INSTALL_PREFIX}/include")
set(LIB_DIR "${CMAKE_INSTALL_PREFIX}/lib")
set(BIN_DIR "${CMAKE_INSTALL_PREFIX}/bin")
set(CONF_DIR "${CMAKE_INSTALL_PREFIX}/etc")
set(SHARE_DIR "${CMAKE_INSTALL_PREFIX}/share")
set(SHARE_DATA_DIR "${SHARE_DIR}/rasdaman")
set(FILE_DATA_DIR "${CMAKE_INSTALL_PREFIX}/data" CACHE STRING
    "The path where the server stores array tiles as files.")
set(WAR_DIR "${SHARE_DATA_DIR}/war" CACHE STRING
    "The path where Java war files should be installed.")
set(LOG_DIR "${CMAKE_INSTALL_PREFIX}/log" CACHE STRING 
    "The path where server logs are stored.")
install(DIRECTORY DESTINATION ${LOG_DIR})

#
# Directories used in the source-code
#
add_definitions(-DCONFDIR="${CONF_DIR}/")
add_definitions(-DBINDIR="${BIN_DIR}/")
add_definitions(-DLOGDIR="${LOG_DIR}/")
add_definitions(-DINCLUDE_DIR="${INCLUDE_DIR}/")
add_definitions(-DSHARE_DATA_DIR="${SHARE_DATA_DIR}/")
add_definitions(-DFILEDATADIR="${FILE_DATA_DIR}/")

# Definition added for use of protobuf libraries. In particular, the protobuf
# libraries (correctly) assume that when a variable x is not defined, it is
# assumed false (i.e. !x = 1); however, when compiling with -Werror and -Wunused,
# the warning for this turns into an error, and it cannot be suppressed with a 
# pragma.
add_definitions(-DPROTOBUF_INLINE_NOT_IN_HEADERS=0)

#
# Generate the configuration header files.
#
GetVersionInformation(RMANVERSION GCCVERSION GCCTARGET)
configure_file("${CMAKE_CURRENT_SOURCE_DIR}/config-cmake.h.in" "${CMAKE_CURRENT_BINARY_DIR}/config.h")
configure_file("${CMAKE_CURRENT_SOURCE_DIR}/version.h.in" "${CMAKE_CURRENT_BINARY_DIR}/version.h")
install(FILES
    "${CMAKE_CURRENT_BINARY_DIR}/config.h"
    "${CMAKE_CURRENT_BINARY_DIR}/version.h"
    DESTINATION "${INCLUDE_DIR}/")

#
# Execute the 3rd party CMakeLists.txt
#
include(third_party/CMakeLists.txt)

#
# Include dirs
#
include_directories(${CMAKE_SOURCE_DIR})
include_directories(${CMAKE_SOURCE_DIR}/include)
include_directories(${CMAKE_SOURCE_DIR}/debug)
include_directories(${CMAKE_SOURCE_DIR}/common/src/logging)
include_directories(${CMAKE_SOURCE_DIR}/third_party/easyloggingpp)
# All the generated files should be placed in the binary dir. We add this to the
# include path so that the compiler can find the files.
include_directories(${CMAKE_BINARY_DIR})

# 3rd party packages
SET(Boost_USE_STATIC_LIBS ON)
SET(Boost_USE_STATIC_RUNTIME ON)
#add_definitions(-DBOOST_ALL_NO_LIB)
find_package(Boost REQUIRED COMPONENTS thread system filesystem)
find_package(RpcGen REQUIRED)
find_package(AWK REQUIRED)
find_package(OpenSSL REQUIRED)

#
# Make subdirectories visible
#
add_subdirectory(applications)
add_subdirectory(bin)
add_subdirectory(catalogmgr)
add_subdirectory(clientcomm)
add_subdirectory(commline)
add_subdirectory(common)
add_subdirectory(conversion)
add_subdirectory(httpserver)
add_subdirectory(include)
add_subdirectory(indexmgr)
add_subdirectory(java)
add_subdirectory(lockmgr)
add_subdirectory(manuals_and_examples)
add_subdirectory(mddmgr)
add_subdirectory(mymalloc)
add_subdirectory(network)
add_subdirectory(qlparser)
add_subdirectory(rascontrol)
add_subdirectory(rascontrol_x)
add_subdirectory(rasdl)
add_subdirectory(raslib)
add_subdirectory(rasmgr)
add_subdirectory(rasmgr_x)
add_subdirectory(rasnet)
add_subdirectory(rasodmg)
add_subdirectory(rasserver_x)
add_subdirectory(reladminif)
add_subdirectory(relblobif)
add_subdirectory(relcatalogif)
add_subdirectory(relindexif)
add_subdirectory(relmddif)
add_subdirectory(relstorageif)
add_subdirectory(server)
add_subdirectory(servercomm)
add_subdirectory(storagemgr)
add_subdirectory(systemtest)
add_subdirectory(tilemgr)

message(STATUS "Final compiler options: ${CMAKE_CXX_FLAGS}")
message(STATUS "Rasdaman will be installed in ${CMAKE_INSTALL_PREFIX}")
