#ifndef _SQLERROR_HH_
#define _SQLERROR_HH_

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
/************************************************************************
 *
 *
 * PURPOSE:
 *
 *
 * COMMENTS:
 *
 ***********************************************************************/

//@ManMemo: Module: {\bf reladminif}.

/*@Doc:

  SQL Errors Handling

*/
#include <iostream>
#include <cstring>
#include "config.h"
using std::cout;
using std::endl;

#include "raslib/error.hh"

#ifdef BASEDB_DB2
#define generateException() generateExceptionn(sqlca)

void generateExceptionn(struct sqlca &);

/*@Doc:
generates a new r_Ebase_dbms exception and throws it.
*/

#define is_error(msg) checkk(msg, sqlca)
int checkk(const char *msg, struct sqlca &mysql);
/*@Doc:
returns sqlcode, prints error messages when appropriate.
the msg is inserted in the error message.
changes are not rolledback, nothing is done to the connection.
*/

#endif

#ifdef BASEDB_ORACLE
void generateException();
/*@Doc:
generates a new r_Ebase_dbms exception and throws it.
*/

int is_error(const char *msg);
/*@Doc:
returns sqlcode, prints error messages when appropriate.
the msg is inserted in the error message.
changes are not rolledback, nothing is done to the connection.
*/

void printSQLError(void *err, int status);
void printSQLError(void *err);
/*@Doc:
This diplays cli errors.
*/
#endif

#ifdef BASEDB_INFORMIX
void generateException();
/*@Doc:
This generates exceptions.
*/

int is_error(const char *msg, bool displayWarning = false);
/*@Doc:
This diplays esql errors.
*/

void printSQLError(int error, const char *);
/*@Doc:
This diplays cli errors.
*/
#endif

#ifdef BASEDB_PGSQL
void generateException();
/*@Doc:
This generates exceptions.
*/

int check(const char *msg);
/*@Doc:
Display error message if SQL errors have occurred.
*/
#endif

#ifdef BASEDB_SQLITE
#define UNDEFINED_RETVAL -10000
#include <sqlite3.h>

bool is_error(sqlite3 *db);
/*@Doc:
Display error message if SQL errors have occurred.
*/

void failOnError(const char *msg, sqlite3 *db);
/*@Doc:
 * Throw an exception when an error happens.
 * retval is an optional return value from an sqlite3_* function execution.
*/

void warnOnError(const char *msg, sqlite3 *db);
/*@Doc:
 * Print a warning when an error happens.
 * retval is an optional return value from an sqlite3_* function execution.
*/
#endif

#endif

int sqlstate_err();

void disp_sqlstate_err(char *msgbuf, size_t length);

void disp_error(const char *stmt, char *msgbuf, size_t length);

char *disp_exception(const char *stmt, int sqlerr_code);

bool is_error_code(int return_code);
