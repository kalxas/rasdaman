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

# Find gdal-java native library
#
#   GDAL_JAVA_DIR - The gdal-java directory
#   GDAL_JAVA_VERSION - The gdal-java version, used in petascope-core pom.xml.in

include(FindPackageHandleStandardArgs)

# try to find version 2.x, x >= 2.3
find_library(GDAL_JAVA_VERSION_2 NAMES libgdalalljni.so.20
             PATHS /usr/lib/java/gdal /usr/lib/jni/)
if (GDAL_JAVA_VERSION_2)
    get_filename_component(GDAL_JAVA_DIR ${GDAL_JAVA_VERSION_2} DIRECTORY)
    set(GDAL_JAVA_DIR ${GDAL_JAVA_DIR})
else()

    # try to find version 2.x, x < 2.3
    find_library(GDAL_JAVA_VERSION_2 NAMES libgdaljni.so.20
                 PATHS /usr/lib/java/gdal /usr/lib/jni/)
    if (GDAL_JAVA_VERSION_2)
        get_filename_component(GDAL_JAVA_DIR ${GDAL_JAVA_VERSION_2} DIRECTORY)
        set(GDAL_JAVA_DIR ${GDAL_JAVA_DIR})
    else()

        # try to find version 1.x
        find_library(GDAL_JAVA_VERSION_1 NAMES libgdaljni.so
                     PATHS /usr/lib/java/gdal /usr/lib/jni/)
        if (GDAL_JAVA_VERSION_1)
            # found version 1.x
            get_filename_component(GDAL_JAVA_DIR ${GDAL_JAVA_VERSION_1} DIRECTORY)
            set(GDAL_JAVA_DIR ${GDAL_JAVA_DIR})
        endif()
    endif()
endif()  


# throw error on REQUIRED if GDAL_JAVA_DIR is not found, otherwise print found
find_package_handle_standard_args(GdalJava DEFAULT_MSG GDAL_JAVA_DIR)

mark_as_advanced(GDAL_JAVA_DIR)
