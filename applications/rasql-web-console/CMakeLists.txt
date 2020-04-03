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
# Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
###################################################################

set(SRC_DIR "${CMAKE_CURRENT_SOURCE_DIR}/")
set(RASQL_SHARE_DIR "${SHARE_DATA_DIR}/www/rasql-web-console")

# Copy rasql-web-console to (e.g: /opt/rasdaman/share/rasdaman/www/rasql-web-console)
install(DIRECTORY "${SRC_DIR}"
        DESTINATION "${RASQL_SHARE_DIR}")