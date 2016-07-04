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

if (NOT RMANRASNET)
    add_executable(rasmgr
            hostcmp.cc
            ras_crypto.hh ras_crypto.cc
            rasmgr.hh
            rasmgr_comm_nb.hh rasmgr_comm_nb.cc
            rasmgr_config.hh rasmgr_config.cc
            rasmgr_dbm.hh rasmgr_dbm.cc
            rasmgr_error.hh rasmgr_error.cc
            rasmgr_host.hh rasmgr_host.cc
            rasmgr_localsrv.hh rasmgr_localsrv.cc
            rasmgr_main.cc
            rasmgr_master.hh rasmgr_master_nb.cc
            rasmgr_protocol.hh
            rasmgr_random.cc
            rasmgr_rascontrol.hh rasmgr_rascontrol.cc
            rasmgr_rascontrol_help.cc
            rasmgr_srv.hh rasmgr_srv.cc
            rasmgr_users.hh rasmgr_users.cc)

    target_include_directories(rasmgr PUBLIC ${BASEDB_INCLUDE_DIR})

    find_package(OpenSSL REQUIRED)
    target_link_libraries(rasmgr raslib network commline mymalloc_cln ${OPENSSL_LIBRARIES})

    install(TARGETS rasmgr
            DESTINATION ${BIN_DIR})
endif ()