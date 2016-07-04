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

# This file should contain all the checks for system functions
# used in rasdaman
include(CheckIncludeFiles)

# Checks for header files.
check_include_files(malloc.h HAVE_MALLOC_H)
check_include_files(arpa/inet.h HAVE_ARPA_INET_H)
check_include_files(fcntl.h HAVE_FCNTL_H)
check_include_files(float.h HAVE_FLOAT_H)
check_include_files(limits.h HAVE_LIMITS_H)
check_include_files(memory.h HAVE_MEMORY_H)
check_include_files(netdb.h HAVE_NETDB_H)
check_include_files(netinet/in.h HAVE_NETINET_IN_H)
check_include_files(stdlib.h HAVE_STDLIB_H)
check_include_files(string.h HAVE_STRING_H)
check_include_files(strings.h HAVE_STRINGS_H)
check_include_files(sys/socket.h HAVE_SYS_SOCKET_H)
check_include_files(sys/time.h HAVE_SYS_TIME_H)
check_include_files(unistd.h HAVE_UNISTD_H)
check_include_files(values.h HAVE_VALUES_H)

check_include_files(hdf/mfhdf.h HAVE_HDF_MFHDF_H)
check_include_files(hdf/hdf.h HAVE_HDF_HDF_H)

check_include_files(hdf.h HAVE_HDF_H)
check_include_files(mfhdf.h HAVE_MFHDF_H)

check_include_files(netcdfcpp.h HAVE_NETCDFCPP_H)

# Check if some functions exist
include(CheckFunctionExists)

check_function_exists(alloca HAVE_ALLOCA_H)

check_function_exists(mktime HAVE_MKTIME)
if (NOT ${HAVE_MKTIME})
    message(FATAL_ERROR "mktime was not found on this system.")
endif ()

check_function_exists(alarm HAVE_ALARM)
check_function_exists(bzero HAVE_BZERO)
check_function_exists(clock_gettime HAVE_CLOCK_GETTIME)
check_function_exists(dup2 HAVE_DUP2)
check_function_exists(floor HAVE_FLOOR)
check_function_exists(getcwd HAVE_GETCWD)
check_function_exists(gethostbyaddr HAVE_GETHOSTBYADDR)
check_function_exists(gethostbyname HAVE_GETHOSTBYNAME)
check_function_exists(gethostname HAVE_GETHOSTNAME)
check_function_exists(getpass HAVE_GETPASS)
check_function_exists(gettimeofday HAVE_GETTIMEOFDAY)
check_function_exists(inet_ntoa HAVE_INET_NTOA)
check_function_exists(localtime_r HAVE_LOCALTIME_R)
check_function_exists(memmove HAVE_MEMMOVE)
check_function_exists(memset HAVE_MEMSET)
check_function_exists(mkdir HAVE_MKDIR)
check_function_exists(pathconf HAVE_PATHCONF)
check_function_exists(pow HAVE_POW)
check_function_exists(rint HAVE_RINT)
check_function_exists(select HAVE_SELECT)
check_function_exists(socket HAVE_SOCKET)
check_function_exists(sqrt HAVE_SQRT)
check_function_exists(strcasecmp HAVE_STRCASECMP)
check_function_exists(strchr HAVE_STRCHR)
check_function_exists(strdup HAVE_STRDUP)
check_function_exists(strerror HAVE_STRERROR)
check_function_exists(strncasecmp HAVE_STRNCASECMP)
check_function_exists(strrchr HAVE_STRRCHR)
check_function_exists(strstr HAVE_STRSTR)
check_function_exists(strtol HAVE_STRTOL)
check_function_exists(strtoul HAVE_STRTOUL)
