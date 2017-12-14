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
/*****************************************************************************
 *
 *
 * PURPOSE:
 * provide global definitions for embedded SQL usage (any base DBMS)
 *
 *
 * COMMENTS:
 * - further SQL-relevant constants for C++ usage can be found in externs.h
 *
 *****************************************************************************/

#ifndef _SQLGLOBALS_H_
#define _SQLGLOBALS_H_

// this syntax should be the same for all embedded SQL versions
// right now only tested & used with Informix, though

// max length of varchar attributes and other string buffers; incl padding zero
// see $(INFORMIXDIR)/incl/esql/varchar.h
// unfortunately there it is not made known to ESQL, so we must use a literal here
#ifdef BASEDB_PGSQL
EXEC SQL define VARCHAR_MAXLEN 255;
EXEC SQL define STRING_MAXLEN 255;
#else
#define VARCHAR_MAXLEN 255
#define STRING_MAXLEN 255
#define QUERY_MAXLEN 2000

#define UPDATE_QUERY(c) { \
        sqlite3_exec(SQLiteQuery::getConnection(), c, 0, 0, 0); \
        failOnError(c, SQLiteQuery::getConnection()); \
    }
#define DROP_TABLE(table_name) \
    UPDATE_QUERY("DROP TABLE IF EXISTS "#table_name);
#define DROP_VIEW(view_name) \
    UPDATE_QUERY("DROP VIEW IF EXISTS "#view_name);

#endif

// SQL query string buffer size
#define SQL_QUERY_BUFFER_SIZE 400

#endif // _SQLGLOBALS_H_
