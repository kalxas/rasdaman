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

function(AddCPPCheckTarget THIRD_PARTY_DIR CPP_FILE_EXTENSIONS)
	find_package(CPPCheck)
    if (CPPCHECK_FOUND)

    	file(GLOB_RECURSE ALL_SOURCE_FILES ${CPP_FILE_EXTENSIONS})

    	foreach (SOURCE_FILE ${ALL_SOURCE_FILES})
    		string(FIND ${SOURCE_FILE} ${THIRD_PARTY_DIR} FOUND_IN_THIRD_PARTY_DIR)
    		if (NOT ${FOUND_IN_THIRD_PARTY_DIR} EQUAL -1)
    			list(REMOVE_ITEM ALL_SOURCE_FILES ${SOURCE_FILE})
    		else()
    			string(FIND ${SOURCE_FILE} "/cmake-" FOUND_IN_CMAKE_DIR)
    			if (NOT ${FOUND_IN_CMAKE_DIR} EQUAL -1)
    				list(REMOVE_ITEM ALL_SOURCE_FILES ${SOURCE_FILE})
    			endif ()
    		endif ()
    	endforeach ()

        get_directory_property(include_dirs INCLUDE_DIRECTORIES)

        add_custom_target(cppcheck COMMAND ${CPPCHECK_EXECUTABLE}
            --enable=warning,style,performance,portability,information,missingInclude
            --std=c++11
            --template=gcc
            --quiet
            -i third_party -i systemtest -i messages -i lex.cc -i oql.cc -i yacc.c
            -I ${include_dirs}
            --relative-paths
            --suppress=missingOverride
            --suppress=unusedVariable
            --suppress=noExplicitConstructor
            --suppress=syntaxError
            --error-exitcode=1
            -DBACKWARD_HAS_DW=1 -DRASDEBUG -DRMANDEBUG -DRMANVERSION="v10.0.0"
            -DBINDIR="/opt/rasdaman/bin/" -DCONFDIR="/opt/rasdaman/etc/" 
            -DINCLUDE_DIR="/opt/rasdaman/include/" -DLOGDIR="/opt/rasdaman/log/"
            -DSHARE_DATA_DIR="/opt/rasdaman/share/rasdaman/" -DSRC_DIR="/home/rasdaman/src/"
            -DELPP_DEBUG_ERRORS -DELPP_DISABLE_DEFAULT_CRASH_HANDLING -DELPP_FEATURE_CRASH_LOG 
            -DELPP_NO_CHECK_MACROS -DELPP_NO_DEFAULT_LOG_FILE -DELPP_STACKTRACE -DELPP_THREAD_SAFE
            -DPROJECT_RASDAMAN -DPROTOBUF_INLINE_NOT_IN_HEADERS=0 
            -DCOMPDATE="2019-08-24" -DRASARCHITECTURE="X86" -DCPPSTDLIB=1 -DX86=1
            -DHAVE_TIFF -DHAVE_GDAL -DHAVE_GRIB -DHAVE_JPEG -DHAVE_PNG -DHAVE_NETCDF -DHAVE_HDF
            -DBASEDB_SQLITE=1 -DBASEDBSTRING="sqlite" -DEARLY_TEMPLATE=1 -DRASSCHEMAVERSION=5
            -DNOPRE=1 -DIS_LITTLE_ENDIAN=1
            ${ALL_SOURCE_FILES}
            )

    endif()

endfunction(AddCPPCheckTarget)
