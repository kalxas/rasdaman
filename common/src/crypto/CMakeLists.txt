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

add_library(common_crypto crypto.hh crypto.cc)

find_package(OpenSSL REQUIRED)
target_link_libraries(common_crypto ${OPENSSL_LIBRARIES})
target_include_directories(common_crypto PUBLIC ${OPENSSL_INCLUDE_DIR})

# Install the library to the lib folder
install(TARGETS common_crypto
        DESTINATION ${LIB_DIR})

install(FILES crypto.hh
        DESTINATION "${INCLUDE_DIR}/common/crypto")
