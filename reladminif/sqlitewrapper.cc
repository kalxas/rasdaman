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
#include <memory>
#include <cstdio>
#include <bool.h>
#include <unistd.h>
#include <logging.hh>

sqlite3* sqliteConn = NULL;

SQLiteQuery::SQLiteQuery(char q[]) :
    stmt(NULL), query(q), columnCounter(0)
{
    sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
    sqlite3_prepare_v2(sqliteConn, q, -1, &stmt, NULL);
    LDEBUG << "SQL query: " << query;
}

SQLiteQuery::SQLiteQuery(const char* format, ...) :
    stmt(NULL), query(""), columnCounter(0)
{
    std::unique_ptr<char[]> tmpQuery(new char[QUERY_MAXLEN]);
    va_list args;
    va_start(args, format);
    vsnprintf(tmpQuery.get(), QUERY_MAXLEN, format, args);
    va_end(args);
    query = std::string(tmpQuery.get());
    LDEBUG << "SQL query: " << query;
    sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
    sqlite3_prepare_v2(sqliteConn, query.c_str(), -1, &stmt, NULL);
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
        query = "";
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

void SQLiteQuery::bindString(const char* param, int size)
{
    sqlite3_bind_text(stmt, ++columnCounter, param, size, SQLITE_TRANSIENT);
}

void SQLiteQuery::bindBlob(const char* param, int size)
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
        failOnError(query.c_str(), sqliteConn);
    }
    else
    {
        warnOnError(query.c_str(), sqliteConn);
    }
}

void SQLiteQuery::execute(const char* q)
{
    LDEBUG << "SQL query: " << q;
    sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
    sqlite3_exec(sqliteConn, q, 0, 0, 0);
    failOnError(q, sqliteConn);
}

void SQLiteQuery::executeWithParams(const char* format, ...)
{
    std::unique_ptr<char[]> q(new char[QUERY_MAXLEN]);
    va_list args;
    va_start(args, format);
    vsnprintf(q.get(), QUERY_MAXLEN, format, args);
    va_end(args);
    LDEBUG << "SQL query: " << q.get();
    sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
    sqlite3_exec(sqliteConn, q.get(), 0, 0, 0);
    failOnError(q.get(), sqliteConn);
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
        failOnError(query.c_str(), sqliteConn);
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

void SQLiteQuery::closeConnection()
{
    if (sqliteConn != NULL)
    {
        if (sqlite3_close(sqliteConn) != SQLITE_OK)
        {
            warnOnError("close RASBASE connection", sqliteConn);
        }
        sqliteConn = NULL;
    }
}

bool SQLiteQuery::openConnection(const char* globalConnectId)
{
    sqlite3_enable_shared_cache(0);
    if (sqlite3_open(globalConnectId, &sqliteConn) != SQLITE_OK)
    {
        LERROR << "Connect unsuccessful; wrong connect string '" << globalConnectId << "'?";
        throw r_Error(DATABASE_CONNECT_FAILED);
    }
    else
    {
        LDEBUG << "Connected successfully to '" << globalConnectId << "'";
        std::string options = "PRAGMA journal_mode=WAL; PRAGMA busy_timeout=" + 
            std::to_string(SQLITE_BUSY_TIMEOUT);
        sqlite3_exec(sqliteConn, options.c_str(), NULL, 0, NULL);
    }
    return true;
}

#endif
