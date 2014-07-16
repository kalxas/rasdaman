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
// This is -*- C++ -*-
/*************************************************************************
 *
 *
 * PURPOSE:
 * process base DBMS errors (SQLite) by printing messages and throwing
 * exceptions.
 *
 * AUTHOR:
 * Dimitar Misev <misev@rasdaman.com>
 *
 ***********************************************************************/

#include "config.h"
#include <cstdlib>
#include <cstring>
#include <cstdio>

#include "sqlerror.hh"
#include "externs.h"

#include "raslib/rmdebug.hh"
#include "raslib/error.hh"
#include "debug-srv.hh"

// general message buffer size
#define BUFFER_SIZE 4000

// SQL error message max size
const int MSG_MAXLEN=BUFFER_SIZE;

char* error_message;
int error_code;

// error codes
#define SUCCESS 0
#define ERROR 1

/*
 * Return true if return_code is an error, or false else.
 */
bool
is_error_code(int return_code)
{
    return return_code > 0 &&
            return_code <= SQLITE_NOTADB;
}

/*
 * Return true if return_code is an error, or false else.
 * Set global variables error_code and error_message in case of an error.
 */
bool
is_error(sqlite3 *sqliteConn) throw (r_Error)
{
    int sqlite_err_code = sqlite3_errcode(sqliteConn);
    bool error = is_error_code(sqlite_err_code);
    if (error)
    {
        error_code = sqlite_err_code;
        error_message = (char*) sqlite3_errmsg(sqliteConn);
    }
    return error;
}

void
failOnError(const char *stmt, sqlite3 *sqliteConn) throw (r_Error)
{
    if (is_error(sqliteConn))
    {
        RMInit::logOut << "SQL query failed: " << stmt << endl;
        RMInit::logOut << "Database error, code: " << error_code <<
                ", message: " << error_message << endl;
        throw r_Ebase_dbms( error_code, error_message );
    }
}

void
warnOnError(const char *stmt, sqlite3 *sqliteConn) throw (r_Error)
{
    if (is_error(sqliteConn))
    {
        RMInit::logOut << "SQL query failed: " << stmt << endl;
        RMInit::logOut << "Database warning, code: " << error_code <<
                ", message: " << error_message << endl;
    }
}
