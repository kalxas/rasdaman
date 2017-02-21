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

cmake_minimum_required(VERSION 2.6 FATAL_ERROR)

#Set cmake policies for compatibility
cmake_policy(SET CMP0012 NEW)
cmake_policy(SET CMP0009 NEW)
cmake_policy(SET CMP0048 NEW)

project(rasdaman VERSION 9.4.0
        LANGUAGES C CXX)

# no rebuild dependency when make install
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

set(CMAKE_VERBOSE_OUTPUT OFF CACHE BOOL "Enable  this if you need detailed output from the make process.")
if (CMAKE_VERBOSE_OUTPUT)
    set(CMAKE_VERBOSE_MAKEFILE ON)
endif ()

################################################################################
# Override CMake defaults
################################################################################
# Override the default installation directory
if (CMAKE_INSTALL_PREFIX_INITIALIZED_TO_DEFAULT)
    set(CMAKE_INSTALL_PREFIX "/opt/rasdaman" CACHE PATH "Installation directory." FORCE)
endif ()

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

# Options for selecting the network protocol
set(NETWORK_PROTOCOL rasnet CACHE STRING "Set the network protocol used in rasdaman (rasnet or rnp).")
set_property(CACHE NETWORK_PROTOCOL PROPERTY STRINGS rasnet rnp)

# Enable C++11 for rasnet.
include(CheckCXXCompilerFlag)

CHECK_CXX_COMPILER_FLAG("-std=c++11" COMPILER_SUPPORTS_CXX11)
CHECK_CXX_COMPILER_FLAG("-std=c++0x" COMPILER_SUPPORTS_CXX0X)
if (COMPILER_SUPPORTS_CXX11)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
elseif (COMPILER_SUPPORTS_CXX0X)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++0x")
else ()
    message(FATAL_ERROR "The compiler ${CMAKE_CXX_COMPILER} has no C++11 support. Please use a different C++ compiler.")
endif ()

message("Building rasdaman with the ${NETWORK_PROTOCOL} protocol.")
if (${NETWORK_PROTOCOL} STREQUAL rasnet)
    set(RMANRASNET 1)
    set(DISABLE_PROTOBUF_WARNINGS_FLAGS " -Wno-sign-conversion -Wno-shadow -Wno-pedantic -Wno-undef ")    
else ()
    set(RMANRASNET 0)
endif ()

set(USE_HDF4 OFF CACHE BOOL "This feature enables the inclusion of HDF4 library during installation.")
if (USE_HDF4)
    # Flag used in config-cmake.h
    set(HAVE_HDF ON)
endif ()

set(USE_NETCDF OFF CACHE BOOL "This feature enables the inclusion of netCDF library during installation.")
if (USE_NETCDF)
    # Flag used in config-cmake.h
    set(HAVE_NETCDF ON)
endif ()

set(USE_GDAL ON CACHE BOOL "This feature enables the inclusion of GDAL library during installation.")
if (USE_GDAL)
    # Flag used in config-cmake.h
    set(HAVE_GDAL ON)
endif ()

set(USE_GRIB OFF CACHE BOOL "This feature enables the inclusion of GRIB library during installation.")
if (USE_GRIB)
    # Flag used in config-cmake.h
    set(HAVE_GRIB ON)
endif ()

set(GENERATE_PIC ON CACHE BOOL "Generate position independent code (PIC)")
if (GENERATE_PIC)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")
endif ()

set(PETASCOPEDB "postgresql" CACHE STRING "Change the default DBMS used by petascope (postgresql, hsqldb, sqlite)")
set_property(CACHE PETASCOPEDB PROPERTY STRINGS postgresql hsqldb sqlite)

if (${PETASCOPEDB} STREQUAL postgresql)
    set(PETASCOPEDB_PGSQL 1)
elseif (${PETASCOPEDB} STREQUAL hsqldb)
    set(PETASCOPEDB_HSQLDB 1)
elseif (${PETASCOPEDB} STREQUAL sqlite)
    set(PETASCOPEDB_SQLITE 1)
else ()
    message(FATAL_ERROR "Unsupported Petascope DB.")
endif ()

set(ENABLE_JAVA ON CACHE BOOL
        "Enable or disable compilation and installation of Java-based components, including rasj and petascope.")
if (ENABLE_JAVA)
    # This variable is necessary to allow the Java components to be built.
    # Petascope depends on rasj being built to this directory.
    set(RASJ_TARGET_DIR "${CMAKE_BINARY_DIR}/java/target")
    file(MAKE_DIRECTORY ${RASJ_TARGET_DIR})

    find_package(Java COMPONENTS Runtime REQUIRED)
    find_package(Java COMPONENTS Development REQUIRED)

    set(ENABLE_R OFF CACHE BOOL "Compile and install R package.")
endif ()

set(DEFAULT_BASEDB "sqlite" CACHE STRING "Change the default Base DBMS (sqlite, postgresql)")
set_property(CACHE DEFAULT_BASEDB PROPERTY STRINGS sqlite postgresql)

find_library(NSL_LIBRARY nsl DOC "The NSL library.")
mark_as_advanced(NSL_LIBRARY)

set(BASEDB_LIBRARIES ${NSL_LIBRARY})
set(BASEDB_INCLUDE_DIR "")

set(POSTGRESQL_MIN_VERSION 9.1.0)
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

    if (${ENABLE_JAVA} AND ${PETASCOPEDB} STREQUAL postgresql)
        find_package(PostgreSQL REQUIRED)
        check_library_exists(pq PQexec "" HAVE_LIBPQ)
        check_library_exists(pgtypes PGTYPESnumeric_add "" HAVE_LIBPGTYPES)

        if ((NOT ${HAVE_LIBPQ}) OR (NOT ${HAVE_LIBPGTYPES}))
            message(FATAL_ERROR "Library not found! Please install...")
        endif ()

        if (${PostgreSQL_VERSION_STRING} VERSION_LESS ${POSTGRESQL_MIN_VERSION})
            message(FATAL_ERROR "Could not find the minimum version of PostgreSQL. Minimum version is ${POSTGRESQL_MIN_VERSION}" )
        endif ()

        find_library(ECPG_LIBARY ecpg DOC "The ECPG library.")
        mark_as_advanced(ECPG_LIBARY)

        find_package(ZLIB REQUIRED)

        set(BASEDB_LIBRARIES ${BASEDB_LIBRARIES} ${PostgreSQL_LIBRARIES} ${ZLIB_LIBRARIES} ${ECPG_LIBARY})
        set(BASEDB_INCLUDE_DIR ${BASEDB_INCLUDE_DIR} ${PostgreSQL_INCLUDE_DIRS})
    endif ()
elseif (${DEFAULT_BASEDB} STREQUAL postgresql)
    # Used in config-cmake.h
    set(BASEDB_PGSQL 1)
    set(BASEDBSTRING pgsql)

    # Used in subdirectories
    set(EMBEDDEDSQL_EXT pgc)
    set(EMBEDDEDSQL_OUT cc)

    find_package(PostgreSQL REQUIRED)
    check_library_exists(pq PQexec "" HAVE_LIBPQ)
    check_library_exists(pgtypes PGTYPESnumeric_add "" HAVE_LIBPGTYPES)

    if ((NOT ${HAVE_LIBPQ}) OR (NOT ${HAVE_LIBPGTYPES}))
        message(FATAL_ERROR "Library not found! Please install...")
    endif ()

    if (${PostgreSQL_VERSION_STRING} VERSION_LESS ${POSTGRESQL_MIN_VERSION})
        message(FATAL_ERROR "Could not find the minimum version of PostgreSQL. Minimum version is ${POSTGRESQL_MIN_VERSION}")
    endif ()

    find_program(ECPG ecpg
            DOC "A precompiler for PostgreSQL embedded code.")
    set(EMBEDDEDSQLPRECOMPILER "${ECPG}")
    set(EMBEDDEDSQLPRECOMPILER_FLAGS  "-o")

    find_library(ECPG_LIBARY ecpg DOC "The ECPG library.")
    find_package(ZLIB REQUIRED)

    set(BASEDB_LIBRARIES ${BASEDB_LIBRARIES} ${PostgreSQL_LIBRARIES} ${ZLIB_LIBRARIES} ${ECPG_LIBARY})
    set(BASEDB_INCLUDE_DIR ${BASEDB_INCLUDE_DIR} ${PostgreSQL_INCLUDE_DIRS})
else ()
    message(FATAL_ERROR "Unsupported base DBMS.")
endif ()

set(JAVA_SERVER "external" CACHE STRING "Set the Java application deployment mode (external or embedded)")
set_property(CACHE JAVA_SERVER PROPERTY STRINGS external embedded)
if (${JAVA_SERVER} STREQUAL external)
    set(JAVA_SERVER_EXTERNAL true)
elseif (${JAVA_SERVER} STREQUAL embedded)
    set(JAVA_SERVER_EMBEDDED true)
else ()
    message(FATAL_ERROR "Invalid JAVA_SERVER option.")
endif ()

set(ENABLE_DEBUG OFF CACHE BOOL "Enable debug symbols and define RMANDEBUG and DEBUG. If you do not need RMANDEBUG, only create a Debug build using CMake.")
if (ENABLE_DEBUG)
    set(RMANDEBUG ${ENABLE_DEBUG})
    set(DEBUG ${ENABLE_DEBUG})

    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O0 -g3 -gdwarf-2 -rdynamic")
else ()
    #TODO:Double check if these are used and remove them if not
    set(_DISABLE_TRACE_LOGS 1)
    # Used by some performance implementation of math libraries.
    set(__NO_MATH_INLINES 1)
endif ()

set(ENABLE_BENCHMARK OFF CACHE BOOL "This feature enables generation of binaries that contain extra code for benchmark output.")
if (ENABLE_BENCHMARK)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DRMANBENCHMARK")
endif ()

set(ENABLE_STRICT OFF CACHE BOOL "Enable compiling in strict mode: warnings terminate compilation.")
if (ENABLE_STRICT)
    # list from http://stackoverflow.com/a/9862800
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -pedantic -Werror -Wall -Wextra -Wshadow -Wcast-align -Wcast-qual -Wctor-dtor-privacy -Wdisabled-optimization -Wformat=2 -Winit-self -Wlogical-op -Wmissing-declarations -Wmissing-include-dirs -Wnoexcept -Woverloaded-virtual -Wredundant-decls -Wsign-conversion -Wsign-promo -Wstrict-null-sentinel -Wstrict-overflow=5 -Wswitch-default -Wundef -Wno-unused -Wno-long-long -Wno-format-nonliteral ")
endif ()

set(GENERATE_DOCS OFF CACHE BOOL
        "Generate doxygen documentation during build.")

########################################
# Begin backward compatibility section
########################################
#TODO: The following variables could be factored out with some care
#Enable the standard library
set(CPPSTDLIB 1)

# The name of the architecture for which the system is compiling.
set(RASARCHITECTURE "X86")
# Compile for a X86 based system.
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

########################################
# End backward compatibility section
########################################
set(INCLUDE_DIR "${CMAKE_INSTALL_PREFIX}/include")
set(LIB_DIR "${CMAKE_INSTALL_PREFIX}/lib")
set(BIN_DIR "${CMAKE_INSTALL_PREFIX}/bin")
set(CONF_DIR "${CMAKE_INSTALL_PREFIX}/etc")

set(SHARE_DIR "${CMAKE_INSTALL_PREFIX}/share")
set(SHARE_DATA_DIR "${SHARE_DIR}/rasdaman")

set(LOG_DIR "${CMAKE_INSTALL_PREFIX}/log" CACHE STRING "The path where the servers will store their logs.")

install(DIRECTORY DESTINATION ${LOG_DIR})

set(FILE_DATA_DIR "${CMAKE_INSTALL_PREFIX}/data" CACHE STRING "The path where the server stores blobs as files.")

set(WAR_DIR "${SHARE_DATA_DIR}/war" CACHE STRING "The path where the war files should be installed.")


#Directories used in the source-code
add_definitions(-DCONFDIR="${CONF_DIR}/")
add_definitions(-DBINDIR="${BIN_DIR}/")
add_definitions(-DLOGDIR="${LOG_DIR}/")
add_definitions(-DINCLUDE_DIR="${INCLUDE_DIR}/")
add_definitions(-DSHARE_DATA_DIR="${SHARE_DATA_DIR}/")
add_definitions(-DFILEDATADIR="${FILE_DATA_DIR}/")

#TODO: These do not seem safe but are kept because the old code requires them
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-stack-protector -fno-strict-overflow")

GetVersionInformation(RMANVERSION GCCVERSION GCCTARGET)
# Generate the configuration header files.
configure_file("${CMAKE_CURRENT_SOURCE_DIR}/config-cmake.h.in" "${CMAKE_CURRENT_BINARY_DIR}/config.h")
configure_file("${CMAKE_CURRENT_SOURCE_DIR}/version.h.in" "${CMAKE_CURRENT_BINARY_DIR}/version.h")

# Execute the 3rd party CMakeLists.txt
include(third_party/CMakeLists.txt)

include_directories(${CMAKE_SOURCE_DIR})
include_directories(${CMAKE_SOURCE_DIR}/include)
include_directories(${CMAKE_SOURCE_DIR}/debug)

# All the generated files should be placed in the binary dir. We add this to the include path so that the compiler can
# find the files.
include_directories(${CMAKE_BINARY_DIR})

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

#  This folder MUST be refactored. It does not need to produce any library.
## add_subdirectory(rasnetprotocol)
## add_subdirectory(rnprotocol)
