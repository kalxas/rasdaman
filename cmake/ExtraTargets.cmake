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

# This file contains commands to add the "format" target to the makefile.
# The following variables must be set for this command to work:
#
# THIRD_PARTY_DIR_PATH - Path to the directory containing third party code.
# CPP_FILE_EXTENSIONS - The extensions used by C++ files.

# AddFormatTarget is used to add the "format" target to make build.
# This target will format all the files that have an extension listed in the CPP_FILE_EXT variable
#   THIRD_PARTY_DIR: The path to the third_party directory. This folder is excluded from the format action.
#   CPP_FILE_EXT: List of C++ file extensions. Regularly cc and hh
function(AddFormatTarget THIRD_PARTY_DIR CPP_FILE_EXT)

    file(GLOB_RECURSE ALL_SOURCE_FILES ${CPP_FILE_EXT})

    foreach (SOURCE_FILE ${ALL_SOURCE_FILES})
        string(FIND ${SOURCE_FILE} ${THIRD_PARTY_DIR} FOUND_IN_THIRD_PARTY_DIR)

        if (NOT ${FOUND_IN_THIRD_PARTY_DIR} EQUAL -1)
            list(REMOVE_ITEM ALL_SOURCE_FILES ${SOURCE_FILE})
        endif ()
    endforeach ()

    find_package(AStyle)
    if (AStyle_FOUND)
        add_custom_target(format COMMAND ${AStyle_EXECUTABLE} --style=allman -n -c ${ALL_SOURCE_FILES})
    else ()
        message(AUTHOR_WARNING "AStyle is not installed on this system. format target was not added.")
    endif ()

endfunction()