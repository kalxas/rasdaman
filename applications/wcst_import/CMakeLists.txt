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
if (ENABLE_JAVA)
    set(INSTALL_DIR "${SHARE_DATA_DIR}/wcst_import")
    set(WCST_IMPORT_PY_SCRIPT "${INSTALL_DIR}/wcst_import.py")
    set(WCST_IMPORT_DAEMON_PY_SCRIPT "${INSTALL_DIR}/wcst_import_daemon.py")

    # Run pylint with `make pylint.check` on every rasdaman build; note that
    # the make will run only if python files have changed since the last run.
    add_custom_target(wcst_import_pylint ALL
        COMMAND make pylint.check
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        COMMENT "Run pylint on wcst_import sources...")


    configure_file(wcst_import.sh.in "${CMAKE_CURRENT_BINARY_DIR}/wcst_import.sh" @ONLY)

    install(PROGRAMS "${CMAKE_CURRENT_BINARY_DIR}/wcst_import.sh"
            DESTINATION ${BIN_DIR})

    # Install the contents of this directory to the installation directory.
    install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/
            DESTINATION ${INSTALL_DIR})
endif ()
