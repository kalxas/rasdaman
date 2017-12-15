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
    find_package(Readline REQUIRED)

    add_executable(rascontrol rascontrol.hh rascontrol.cc
            rascontrol_class.cc
            rasmgr_utils_comm.hh rasmgr_utils_comm.cc
            rasmgr_utils_conf.hh rasmgr_utils_conf.cc
            ../rasmgr/ras_crypto.cc ../rasmgr/ras_crypto.hh
            ../raslib/log_config.cc ../raslib/log_config.hh)

    target_include_directories(rascontrol PUBLIC ${OPENSSL_INCLUDE_DIR})
    target_link_libraries(rascontrol network commline ${READLINE_LIBRARIES} ${OPENSSL_LIBRARIES})

    install(TARGETS rascontrol
            DESTINATION ${BIN_DIR})

    add_executable(raspasswd raspasswd.hh raspasswd.cc
            rasmgr_utils_comm.hh rasmgr_utils_comm.cc
            rasmgr_utils_conf.hh rasmgr_utils_conf.cc
            ../rasmgr/ras_crypto.cc ../rasmgr/ras_crypto.hh
            ../raslib/log_config.cc ../raslib/log_config.hh)

    target_include_directories(raspasswd PUBLIC ${OPENSSL_INCLUDE_DIR})
    target_link_libraries(raspasswd network commline ${READLINE_LIBRARIES} ${OPENSSL_LIBRARIES})

    install(TARGETS raspasswd
            DESTINATION ${BIN_DIR})

endif ()
