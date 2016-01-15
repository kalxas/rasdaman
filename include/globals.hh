/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/**
 * SOURCE: globals.hh
 *
 * MODULE: rasdaman
 * CLASS:  Globals
 *
 * PURPOSE:
 * This class provides basic constants used across rasdaman.
 * Any value that is of external importance (default names, timeouts, ...)
 * but are NOT TO BE DELIVERED should be defined here.
 *
 * COMMENTS:
 * - make this a real class
 * - successively collect all important values here
 *
*/

/**
*   @file globals.hh
*
*   @ingroup Include
*/

#ifndef GLOBALS_HH
#define GLOBALS_HH

/// default rasmgr listen port
#define DEFAULT_PORT        7001

/// default name of server machine, if gethostname() fails
#define DEFAULT_HOSTNAME    "localhost"

/// default database name
#define DEFAULT_DBNAME      "RASBASE"

/// name of rasmgr configuration file
#define RASMGR_CONF_FILE    "rasmgr.conf"

/// name of server logging configuration file
#define SERVER_LOG_CONF    "log-server.conf"

/// name of rasmgr logging configuration file
#define RASMGR_LOG_CONF    "log-rasmgr.conf"

/// name of client logging configuration file
#define CLIENT_LOG_CONF    "log-client.conf"

/// name of rasmgr authentication file
#define RASMGR_AUTH_FILE    "rasmgr.auth"

/// default resource directory (e.g. for configuration files)
#define RAS_USER_RESOURCEDIR ".rasdaman"

/// default r/o login name for client tools
#define DEFAULT_USER        "rasguest"
/// default password for this user
#define DEFAULT_PASSWD      "rasguest"

#define DEFAULT_ADMIN        "rasadmin"
#define DEFAULT_ADMIN_PASSWD    "rasadmin"

/// log file suffix
#define LOG_SUFFIX          "log"

// default digest used for encoding passwords
#define DEFAULT_DIGEST    "MD5"

/// max transfer buffer size (import clients)
#define MAX_BUFFER_SIZE     4194304L

/// client timeout [secs]
#define CLIENT_TIMEOUT      300

///  The number of milliseconds a server has to reply to a service request.
#define SERVICE_CALL_TIMEOUT 5000

#endif // GLOBALS_HH

