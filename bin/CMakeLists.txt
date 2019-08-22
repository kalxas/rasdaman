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

# The following variables are used to generate the scripts from the templates at configuration time
if (BASEDB_SQLITE)
    set(DBCONN "${FILE_DATA_DIR}/RASBASE")
elseif (BASEDB_PGSQL)
    set(DBCONN RASBASE)
else ()
    message(FATAL_ERROR "Invalid BASEDB option.")
endif ()

# Used to create the configuration scripts
# I added the last / because the *.in files do not follow a certain style for folders and it is safer to have // than a missing one
set(logdir "${LOG_DIR}/")
set(bindir "${BIN_DIR}/")
set(wardir "${WAR_DIR}/")
set(basedbms ${BASEDBSTRING})
set(dbconn ${DBCONN})
set(pkgdatadir "${SHARE_DATA_DIR}/")
set(sysconfdir "${CONF_DIR}/")
set(libdir "${LIB_DIR}/")
set(java_server ${JAVA_SERVER})
set(petascopedb ${PETASCOPEDB})

# Get the hostname of the current machine and save it to the hostname variable
# The hostname variable is used in the scripts.
execute_process(COMMAND hostname
        OUTPUT_VARIABLE hostname
        OUTPUT_STRIP_TRAILING_WHITESPACE)

configure_file(start_rasdaman.sh.in "${CMAKE_CURRENT_BINARY_DIR}/start_rasdaman.sh" @ONLY)
configure_file(stop_rasdaman.sh.in "${CMAKE_CURRENT_BINARY_DIR}/stop_rasdaman.sh" @ONLY)
configure_file(create_db.sh.in "${CMAKE_CURRENT_BINARY_DIR}/create_db.sh" @ONLY)
configure_file(update_db.sh.in "${CMAKE_CURRENT_BINARY_DIR}/update_db.sh" @ONLY)
configure_file(rasdaman_insertdemo.sh.in "${CMAKE_CURRENT_BINARY_DIR}/rasdaman_insertdemo.sh" @ONLY)
configure_file(watch_rasdaman.sh.in "${CMAKE_CURRENT_BINARY_DIR}/watch_rasdaman.sh" @ONLY)

configure_file(rasmgr.conf.in "${CMAKE_CURRENT_BINARY_DIR}/rasmgr.conf" @ONLY)

# Install the created scripts as executable programs in the bin directory.
install(PROGRAMS "${CMAKE_CURRENT_BINARY_DIR}/start_rasdaman.sh"            
        "${CMAKE_CURRENT_BINARY_DIR}/stop_rasdaman.sh"       
        "${CMAKE_CURRENT_BINARY_DIR}/create_db.sh"
        "${CMAKE_CURRENT_BINARY_DIR}/update_db.sh"
        "${CMAKE_CURRENT_BINARY_DIR}/rasdaman_insertdemo.sh"  
        "${CMAKE_CURRENT_BINARY_DIR}/watch_rasdaman.sh"  
        DESTINATION ${BIN_DIR})     

# Only install the files if they are not installed. This allows the user to update his installation without having the files overwritten.
install_if_not_exists("${CMAKE_CURRENT_BINARY_DIR}/rasmgr.conf" ${CONF_DIR})
install_log_file(log-client.conf ${CONF_DIR})
install_log_file(log-server.conf ${CONF_DIR})
install_log_file(log-rasmgr.conf ${CONF_DIR})

# Copy the error text files.
install(FILES errtxts DESTINATION ${SHARE_DATA_DIR})
