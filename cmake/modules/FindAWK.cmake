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

# Find the AWK executable
#
#  AWK_EXECUTABLE         - AWK executable
#  AWK_FOUND              - TRUE if AWK binary was found.

INCLUDE(FindPackageHandleStandardArgs)

IF (AWK_EXECUTABLE)
    # Already in cache, be silent
    SET(AWK_FIND_QUIETLY TRUE)
ENDIF (AWK_EXECUTABLE)

SET(AWK_NAMES awk gawk mawk nawk)
find_program(AWK_EXECUTABLE NAMES ${AWK_NAMES})

# Handle the QUIETLY and REQUIRED arguments and set AWK_FOUND to TRUE
FIND_PACKAGE_HANDLE_STANDARD_ARGS(AWK DEFAULT_MSG AWK_EXECUTABLE)

# Show these variables only in the advanced view in the GUI, and make them global
MARK_AS_ADVANCED(AWK_FOUND AWK_EXECUTABLE)