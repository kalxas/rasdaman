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

add_library(network akgnet_commbuffer.hh akgnet_commbuffer.cc
        akgnet_common.hh
        akgnet_inetaddr.hh akgnet_inetaddr.cc
        akgnet_fdescr.hh akgnet_fdescr.cc
        akgnet_file.hh akgnet_file.cc
        akgnet_nbcomm.hh akgnet_nbcomm.cc
        akgnetwork.hh
        akgnet_selector.hh akgnet_selector.cc
        akgnet_server.hh akgnet_server.cc
        akgnet_socket.hh akgnet_socket.cc)

target_include_directories(network PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})
target_include_directories(network PUBLIC "${CMAKE_SOURCE_DIR}/debug")

install(TARGETS network
        DESTINATION ${LIB_DIR})