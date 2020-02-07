  
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
#
###################################################################

# Create error messages from bin/errtxts via bash script to a Python file
add_custom_target(rasdapy_generate_errtxts_message ALL
    COMMAND bash ${CMAKE_CURRENT_SOURCE_DIR}/generate_errtxts_messages.sh 
                 ${CMAKE_CURRENT_SOURCE_DIR}/rasdapy/cores/exception_factories.py.in 
    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )
