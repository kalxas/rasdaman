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
/*
 * File:   sqlitewrapper.cc
 * Author: Dimitar Misev
 *
 * Created on May 8, 2014, 6:05 PM
 */
#include "config.h"
#ifdef BASEDB_SQLITE

#include "sqlitewrapper.hh"
#include "sqlerror.hh"
#include "raslib/rminit.hh"
#include "debug/debug-srv.hh"
#include <cstdarg>
#include <cstdio>
#include <bool.h>
#include <easylogging++.h>

extern sqlite3* sqliteConn;

// 60s timeout, in case RASBASE is locked by another rasserver for writing
#define SQLITE_BUSY_TIMEOUT 60000

SQLiteQuery::SQLiteQuery(char q[]) :
    stmt(NULL), columnCounter(0)
{
    query = query;
    sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
    sqlite3_prepare_v2(sqliteConn, q, -1, &stmt, NULL);
    //RMInit::logOut << "SQL query: " << query << endl;
    LDEBUG << "SQL query: " << query;
}

SQLiteQuery::SQLiteQuery(const char* format, ...) :
    stmt(NULL), columnCounter(0)
{
    char q[QUERY_MAXLEN];
    va_list args;
    va_start(args, format);
    vsnprintf(q, QUERY_MAXLEN, format, args);
    va_end(args);
    query = q;
    sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
    sqlite3_prepare_v2(sqliteConn, query, -1, &stmt, NULL);
    //RMInit::logOut << "SQL query: " << query << endl;
    LDEBUG << "SQL query: " << query;
}

SQLiteQuery::~SQLiteQuery()
{
    finalize();
}

SQLiteQuery::SQLiteQuery(const SQLiteQuery& o)
{
    stmt = o.stmt;
    query = o.query;
    columnCounter = o.columnCounter;
}

void SQLiteQuery::finalize()
{
    if (stmt != NULL)
    {
        sqlite3_finalize(stmt);
        stmt = NULL;
    }
}

void SQLiteQuery::bindNull()
{
    sqlite3_bind_null(stmt, ++columnCounter);
}

void SQLiteQuery::bindInt(int param)
{
    sqlite3_bind_int(stmt, ++columnCounter, param);
}

void SQLiteQuery::bindLong(long long param)
{
    sqlite3_bind_int64(stmt, ++columnCounter, param);
}

void SQLiteQuery::bindDouble(double param)
{
    sqlite3_bind_double(stmt, ++columnCounter, param);
}

void SQLiteQuery::bindString(char* param, int size)
{
    sqlite3_bind_text(stmt, ++columnCounter, param, size, SQLITE_TRANSIENT);
}

void SQLiteQuery::bindBlob(char* param, int size)
{
    sqlite3_bind_blob(stmt, ++columnCounter, param, size, SQLITE_TRANSIENT);
}

void SQLiteQuery::execute(int fail)
{
    LDEBUG << "SQL query: " << query;
    sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
    sqlite3_step(stmt);
    if (fail)
    {
        failOnError(query, sqliteConn);
    }
    else
    {
        warnOnError(query, sqliteConn);
    }
}

void SQLiteQuery::execute(const char* query)
{
    //RMInit::logOut << "SQL query: " << query << endl;
    LDEBUG << "SQL query: " << query;
    sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
    sqlite3_exec(sqliteConn, query, 0, 0, 0);
    failOnError(query, sqliteConn);
}

void SQLiteQuery::executeWithParams(const char* format, ...)
{
    char query[QUERY_MAXLEN];
    va_list args;
    va_start(args, format);
    vsnprintf(query, QUERY_MAXLEN, format, args);
    va_end(args);
    //RMInit::logOut << "SQL query: " << query << endl;
    LDEBUG << "SQL query: " << query;
    sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
    sqlite3_exec(sqliteConn, query, 0, 0, 0);
    failOnError(query, sqliteConn);
}

sqlite3* SQLiteQuery::getConnection()
{
    return sqliteConn;
}

int SQLiteQuery::isTransactionActive()
{
    return sqliteConn != NULL && sqlite3_get_autocommit(sqliteConn) == 0;
}

int SQLiteQuery::nextRow()
{
    int rc = sqlite3_step(stmt);
    if (rc != SQLITE_ROW && rc != SQLITE_DONE)
    {
        failOnError((const char*) query, sqliteConn);
    }
    columnCounter = 0;
    return rc == SQLITE_ROW;
}

void SQLiteQuery::nextColumn()
{
    ++columnCounter;
}

int SQLiteQuery::nextColumnInt()
{
    return sqlite3_column_int(stmt, columnCounter++);
}

long long SQLiteQuery::nextColumnLong()
{
    return sqlite3_column_int64(stmt, columnCounter++);
}

double SQLiteQuery::nextColumnDouble()
{
    return sqlite3_column_double(stmt, columnCounter++);
}

char* SQLiteQuery::nextColumnString()
{
    return (char*)(const_cast<unsigned char*>(sqlite3_column_text(stmt, columnCounter++)));
}

char* SQLiteQuery::nextColumnBlob()
{
    return static_cast<char*>(const_cast<void*>(sqlite3_column_blob(stmt, columnCounter++)));
}

int SQLiteQuery::currColumnBytes()
{
    return sqlite3_column_bytes(stmt, columnCounter - 1);
}

int SQLiteQuery::currColumnType()
{
    return sqlite3_column_type(stmt, columnCounter);
}

int SQLiteQuery::currColumnNull()
{
    return currColumnType() == SQLITE_NULL;
}

#endif