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

project(rasdaman VERSION 9.8.0 LANGUAGES C CXX)

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

set(CPP_FILE_EXTENSIONS *.cc *.hh *.sc *.icc *.tcc *.C)

# Print elapsed time to build targets
set_property(GLOBAL PROPERTY RULE_LAUNCH_COMPILE "${CMAKE_CURRENT_SOURCE_DIR}/cmake/report_compile_time.sh")
set_property(GLOBAL PROPERTY RULE_LAUNCH_LINK    "${CMAKE_CURRENT_SOURCE_DIR}/cmake/report_link_time.sh")
set_property(GLOBAL PROPERTY RULE_LAUNCH_CUSTOM  "${CMAKE_CURRENT_SOURCE_DIR}/cmake/report_custom_time.sh")

################################################################################
# Configure options
################################################################################

# Add make unit-check target. Running make unit-check will run the unit tests.
include(CTest)
add_custom_target(unit-check COMMAND ${CMAKE_CTEST_COMMAND})

# Add make format target. This will format all the files in the source tree.
include(${CMAKE_CURRENT_SOURCE_DIR}/cmake/ExtraTargets.cmake)
AddFormatTarget("${CMAKE_SOURCE_DIR}/third_party" "${CPP_FILE_EXTENSIONS}")

#include(cmake/ClangTidy.cmake)
#AddClangTidyTarget("${CMAKE_SOURCE_DIR}/third_party" "${CPP_FILE_EXTENSIONS}")

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
# Use clang + lld for compilation
#
set(USE_LLD "") # do not use lld
set(USE_CLANG OFF CACHE BOOL "Use clang + lld for compiling/linking.")
if (USE_CLANG)
    set(CMAKE_CXX_COMPILER "clang")
    set(CMAKE_C_COMPILER "clang")
    set(CMAKE_CXX_COMPILER_ID "Clang")
    set(USE_LLD "-Qunused-arguments -fuse-ld=lld")
endif ()

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

#
# Sanitizer
#
set(SANITIZER_FLAG "")

set(ENABLE_ASAN OFF CACHE BOOL "Use AddressSanitizer (compile with -fsanitize=address).")
if (ENABLE_ASAN)
    set(SANITIZER_FLAG "-fsanitize=address -fsanitize=undefined -lubsan")
    message(STATUS "AddressSanitizer enabled (-DENABLE_ASAN=${ENABLE_ASAN}).")
endif ()

#
# Set warnings
#
set(COMPILER_WARNINGS "-pedantic -Wall -Wextra -Wshadow -Wcast-align -Wcast-qual -Wsign-promo -Wredundant-decls")
set(COMPILER_WARNINGS "${COMPILER_WARNINGS} -Wctor-dtor-privacy -Wdisabled-optimization -Wsign-conversion -Wstrict-overflow=5")
set(COMPILER_WARNINGS "${COMPILER_WARNINGS} -Wformat=2 -Winit-self -Wmissing-declarations -Wmissing-include-dirs -Woverloaded-virtual")
set(COMPILER_WARNINGS "${COMPILER_WARNINGS} -Wno-unused -Wno-unused-parameter -Wno-unused-function -Wno-format-nonliteral -Wno-switch-default")
set(RDYNAMIC_FLAG "")
if ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
    # using Clang
    set(ENABLE_MSAN OFF CACHE BOOL "Use MemorySanitizer (compile with -fsanitize=memory).")
    if (ENABLE_MSAN)
        set(SANITIZER_FLAG "-fsanitize=memory -fsanitize=undefined -lubsan")
        message(STATUS "MemorySanitizer enabled (-DENABLE_MSAN=${ENABLE_MSAN}).")
    endif ()
    set(COMPILER_WARNINGS "${COMPILER_WARNINGS} -Wthread-safety")
    set(RDYNAMIC_FLAG "-Wl,--export-dynamic")
elseif ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "GNU")
    set(COMPILER_WARNINGS "${COMPILER_WARNINGS} -Wlogical-op -Wno-long-long")
    set(COMPILER_WARNINGS "${COMPILER_WARNINGS} -Wnoexcept -Wstrict-null-sentinel")
elseif ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Intel")
    # using Intel C++
elseif ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "MSVC")
    # using Visual Studio C++
endif()

#
# Set compiler flags
#
SET(CMAKE_CXX_FLAGS_DEBUG "-O0 -g -ggdb3 -gdwarf-2 -gsplit-dwarf -fno-omit-frame-pointer -pthread ${USE_LLD} ${SANITIZER_FLAG} ${RDYNAMIC_FLAG}")
SET(CMAKE_C_FLAGS_DEBUG "-O0 -g -ggdb3 -gdwarf-2 -gsplit-dwarf -fno-omit-frame-pointer -pthread ${USE_LLD} ${SANITIZER_FLAG} ${RDYNAMIC_FLAG}")
SET(CMAKE_CXX_FLAGS_RELEASE "-O3 -ftree-vectorize -fno-omit-frame-pointer -pthread ${USE_LLD} ${SANITIZER_FLAG} ${RDYNAMIC_FLAG}")
SET(CMAKE_C_FLAGS_RELEASE "-O3 -ftree-vectorize -fno-omit-frame-pointer -pthread ${USE_LLD} ${SANITIZER_FLAG} ${RDYNAMIC_FLAG}")

set(PROJECT_RASDAMAN 1)
add_definitions(-DPROJECT_RASDAMAN)

# Disable default log file from easylogging, otherwise we get by default
# logs/myeasylog.log
add_definitions(-DELPP_NO_DEFAULT_LOG_FILE)
add_definitions(-DELPP_FEATURE_CRASH_LOG)
add_definitions(-DELPP_STACKTRACE)
add_definitions(-DELPP_DISABLE_DEFAULT_CRASH_HANDLING)
add_definitions(-DELPP_NO_CHECK_MACROS)
add_definitions(-DELPP_THREAD_SAFE)

# For development
set(ENABLE_DEBUG OFF CACHE BOOL "Generate (slower) binaries that can be debugged / produce debug logs.")
if (ENABLE_DEBUG)
    add_definitions(-DRASDEBUG)
    set(RMANDEBUG ${ENABLE_DEBUG})
    # Do not enable below as it clashes with define from easylogging
    # set(DEBUG ${ENABLE_DEBUG})
    set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -g -gsplit-dwarf")

    add_definitions(-DELPP_DEBUG_ERRORS)

    message(STATUS "Compile in debug mode enabled " 
        "(-DENABLE_DEBUG=${ENABLE_DEBUG}).")
else ()
    message(STATUS "Compile in debug mode disabled " 
        "(-DENABLE_DEBUG=${ENABLE_DEBUG}).")
endif ()

# Find libdw
find_path(LIBDW_INCLUDE_DIR NAMES "elfutils/libdw.h" "elfutils/libdwfl.h")
find_library(LIBDW_LIBRARY dw)
if (LIBDW_LIBRARY AND LIBDW_INCLUDE_DIR)
    set(LIBDW_INCLUDE_DIRS ${LIBDW_INCLUDE_DIR} )
    set(LIBDW_LIBRARIES ${LIBDW_LIBRARY} )
    include(FindPackageHandleStandardArgs)
    find_package_handle_standard_args(libdw DEFAULT_MSG
            LIBDW_LIBRARY LIBDW_INCLUDE_DIR)
    mark_as_advanced(LIBDW_INCLUDE_DIR LIBDW_LIBRARY)
    add_definitions(-DBACKWARD_HAS_DW=1)
    set(HAVE_LIBDW ON)
else()
    set(HAVE_LIBDW OFF)
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
    # Used by some performance implementation of math libraries.
    set(__NO_MATH_INLINES 1)
endif()

set(DISABLE_PROTOBUF_WARNINGS_FLAGS 
    " -Wno-sign-conversion -Wno-shadow -Wno-pedantic -Wno-undef -Wno-missing-declarations ")

#
# clang-tidy
#
set(ENABLE_CLANG_TIDY OFF CACHE BOOL "Enable static analysis with clang-tidy.")
if (ENABLE_CLANG_TIDY)
    set(CMAKE_CXX_CLANG_TIDY "clang-tidy;-style=file;-header-filter=.*/(src|test)/.*")
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
    if (${ECPG} STREQUAL "ECPG-NOTFOUND")
        message(FATAL_ERROR "ecpg not found, please install and add to the PATH.")
    endif()
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
    find_package(HDF4 REQUIRED)
    set(HAVE_HDF ON)
    message(STATUS "  HDF4 enabled (-DUSE_HDF4=${USE_HDF4}).")
else()
    message(STATUS "  HDF4 disabled (-DUSE_HDF4=${USE_HDF4}).")
endif ()

set(USE_NETCDF OFF CACHE BOOL "Enable inclusion of netCDF library during installation.")
if (USE_NETCDF)
    find_package(NetCDF REQUIRED)
    set(HAVE_NETCDF ON)
    message(STATUS "  NetCDF enabled (-DUSE_NETCDF=${USE_NETCDF}).")
else()
    message(STATUS "  NetCDF disabled (-DUSE_NETCDF=${USE_NETCDF}).")
endif ()

set(USE_GDAL ON CACHE BOOL "Enable inclusion of GDAL library during installation. ")
if (USE_GDAL)
    find_package(GDAL REQUIRED)
    set(GDAL_LIBRARIES ${GDAL_LIBRARY})
    set(GDAL_INCLUDE_DIRECTORIES ${GDAL_INCLUDE_DIR})
    include_directories(SYSTEM ${GDAL_INCLUDE_DIR})
    set(HAVE_GDAL ON)

    message(STATUS "  internal GDAL converter enabled (-DUSE_GDAL=${USE_GDAL}).")
else()
    message(STATUS "  internal GDAL converter disabled (-DUSE_GDAL=${USE_GDAL}).")
endif ()

set(USE_GRIB OFF CACHE BOOL "Enable inclusion of GRIB library during installation.")
if (USE_GRIB)
    find_package(GRIB REQUIRED)
    set(HAVE_GRIB ON)
    message(STATUS "  internal GRIB converter enabled (-DUSE_GRIB=${USE_GRIB}).")
else()
    message(STATUS "  internal GRIB converter disabled (-DUSE_GRIB=${USE_GRIB}).")
endif ()

# Custom encoder/decoder implementations (not relying on GDAL)
set(USE_TIFF OFF CACHE BOOL "Enable compilation of TIFF encoder/decoder during installation.")
if (USE_TIFF)
    find_package(TIFF REQUIRED)
    set(HAVE_TIFF ON)
    message(STATUS "  internal TIFF converter enabled (-DUSE_TIFF=${USE_TIFF}).")
else()
    message(STATUS "  internal TIFF converter disabled (-DUSE_TIFF=${USE_TIFF}).")
endif ()
set(USE_JPEG OFF CACHE BOOL "Enable compilation of JPEG encoder/decoder during installation.")
if (USE_JPEG)
    set(HAVE_JPEG ON)
    find_package(JPEG REQUIRED)
    message(STATUS "  internal JPEG converter enabled (-DUSE_JPEG=${USE_JPEG}).")
else()
    message(STATUS "  internal JPEG converter disabled (-DUSE_JPEG=${USE_JPEG}).")
endif ()
set(USE_PNG OFF CACHE BOOL "Enable compilation of PNG encoder/decoder during installation.")
if (USE_PNG)
    find_package(PNG REQUIRED)
    set(HAVE_PNG ON)
    message(STATUS "  internal PNG converter enabled (-DUSE_PNG=${USE_PNG}).")
else()
    message(STATUS "  internal PNG converter disabled (-DUSE_PNG=${USE_PNG}).")
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
    find_package(Maven REQUIRED)
    find_package(GdalJava REQUIRED)

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
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wformat=2 -Winit-self -Wlogical-op ")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-strict-aliasing -Wno-missing-include-dirs")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wnoexcept -Woverloaded-virtual -Wredundant-decls")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wsign-conversion -Wstrict-null-sentinel")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wstrict-overflow=5 -Wundef")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-unused -Wno-empty-body -Wno-unused-function") 
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-unused-result -Wno-long-long -Wno-format-nonliteral ")
    if ("${CMAKE_CXX_COMPILER_VERSION}" GREATER_EQUAL "7.0")
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-implicit-fallthrough")
    endif()
    if ("${CMAKE_CXX_COMPILER_VERSION}" LESS_EQUAL "4.8.5")
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-unused-parameter")
    endif()

    message(STATUS "Strict mode (warnings terminate compilation) " 
        "enabled (-DENABLE_STRICT=${ENABLE_STRICT}).")
else()
    message(STATUS "Strict mode (warnings terminate compilation) " 
        "disabled (-DENABLE_STRICT=${ENABLE_STRICT}).")
endif ()

set(GENERATE_DOCS ON CACHE BOOL "Build and install documentation.")
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
if ("${CMAKE_INSTALL_PREFIX}"  STREQUAL "")
	message( FATAL_ERROR "-DCMAKE_INSTALL_PREFIX is not set, refusing to install into /")
endif()
set(INCLUDE_DIR "${CMAKE_INSTALL_PREFIX}/include")
set(LIB_DIR "${CMAKE_INSTALL_PREFIX}/lib")
set(BIN_DIR "${CMAKE_INSTALL_PREFIX}/bin")
set(CONF_DIR "${CMAKE_INSTALL_PREFIX}/etc")
set(SHARE_DIR "${CMAKE_INSTALL_PREFIX}/share")
set(SHARE_DATA_DIR "${SHARE_DIR}/rasdaman")
set(DOC_DIR "${SHARE_DATA_DIR}/doc")
set(FILE_DATA_DIR "${CMAKE_INSTALL_PREFIX}/data" CACHE STRING
    "The path where the server stores array tiles as files.")
set(WAR_DIR "${SHARE_DATA_DIR}/war" CACHE STRING
    "The path where Java war files should be installed.")
if (NOT ("${WAR_DIR}" STREQUAL "${SHARE_DATA_DIR}/war") AND NOT (EXISTS "${WAR_DIR}/"))
	message( FATAL_ERROR "${WAR_DIR} doesn't exist or cannot be accessed.")	
endif()
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
add_definitions(-DSRC_DIR="${CMAKE_SOURCE_DIR}/")

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
include_directories(${CMAKE_SOURCE_DIR}/common/logging)
include_directories(SYSTEM ${CMAKE_SOURCE_DIR}/third_party/easyloggingpp)
# All the generated files should be placed in the binary dir. We add this to the
# include path so that the compiler can find the files.
include_directories(${CMAKE_BINARY_DIR})

# 3rd party packages
SET(Boost_USE_STATIC_LIBS ON)
SET(Boost_USE_STATIC_RUNTIME ON)
#add_definitions(-DBOOST_ALL_NO_LIB)
find_package(Boost REQUIRED COMPONENTS thread system filesystem)
find_package(AWK REQUIRED)
find_package(OpenSSL REQUIRED)

#
# Make subdirectories visible
#
# common must be defined first for precompiled headers to work
add_subdirectory(common)

add_subdirectory(applications)
add_subdirectory(bin)
add_subdirectory(catalogmgr)
add_subdirectory(clientcomm)
add_subdirectory(commline)
add_subdirectory(conversion)
add_subdirectory(doc)
add_subdirectory(include)
add_subdirectory(indexmgr)
add_subdirectory(java)
add_subdirectory(lockmgr)
add_subdirectory(manuals_and_examples)
add_subdirectory(mddmgr)
add_subdirectory(mymalloc)
add_subdirectory(qlparser)
add_subdirectory(rascontrol)
add_subdirectory(raslib)
add_subdirectory(rasmgr)
add_subdirectory(rasnet)
add_subdirectory(rasodmg)
add_subdirectory(rasserver)
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


include(cmake/CPPCheck.cmake)
AddCPPCheckTarget("${CMAKE_SOURCE_DIR}/third_party" "${CPP_FILE_EXTENSIONS}")

set(ENABLE_UNITY_BUILD ON CACHE BOOL "Enable unity build.")
if (ENABLE_UNITY_BUILD)
    # Set targets for unity build
    # https://cmake.org/cmake/help/v3.16/prop_tgt/UNITY_BUILD.html
    set_target_properties(
        common_exceptions 
        common_util
        common_logging
        common_network
        common_grpc
        clientcomm
        conversion
        indexmgr
        mddmgr
        qlparser
        raslib
        rasodmg
        rascontrol_lib
        rasmgr_lib
        rasnet
        #rasnetserver
        relblobif
        relcatalogif
        relindexif
        relmddif
        relstorageif
        reladminif
        servercomm
        storagemgr
        rasserver
        PROPERTIES UNITY_BUILD ON)

endif() # ENABLE_UNITY_BUILD

message(STATUS "Final compiler options: ${CMAKE_CXX_FLAGS}")
message(STATUS "Rasdaman will be installed in ${CMAKE_INSTALL_PREFIX}")
