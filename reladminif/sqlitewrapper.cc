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

#include "sqlitewrapper.hh"        // for SQLiteQuery
#ifdef BASEDB_SQLITE

#include "sqlglobals.h"            // for QUERY_MAXLEN
#include "raslib/error.hh"         // for r_Error, DATABASE_CONNECT_FAILED
#include <logging.hh>              // for Writer, CDEBUG, LDEBUG, CFATAL

#include <sqlite3.h>               // for sqlite3_busy_timeout, sqlite3_exec
#include <cstdarg>                 // for va_end, va_list, va_start
#include <cstdio>                  // for vsnprintf, NULL
#include <memory>                  // for unique_ptr
#include <stdexcept>               // for runtime_error
#include <string>                  // for string
#include <cassert>
#include <unistd.h>

// globally defined here, used as an extern in the new engine as well.
sqlite3 *sqliteConn = NULL;

SQLiteQuery::SQLiteQuery(char q[]) :
    stmt(nullptr), query(q), columnCounter(0)
{
    LDEBUG << "SQL query: " << query;
    if (sqliteConn)
    {
        sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
        sqlite3_prepare_v2(sqliteConn, q, -1, &stmt, nullptr);
        failOnError(query.c_str());
    }
    else
    {
        LERROR << "Connection to RASBASE has not been opened, cannot execute query: " << q;
        throw r_Error(DATABASE_CONNECT_FAILED);
    }
}

SQLiteQuery::SQLiteQuery(const char *format, ...)
    : stmt(nullptr), query(""), columnCounter(0)
{
    std::unique_ptr<char[]> tmpQuery(new char[QUERY_MAXLEN]);
    va_list args;
    va_start(args, format);
    vsnprintf(tmpQuery.get(), QUERY_MAXLEN, format, args);
    va_end(args);
    query = std::string(tmpQuery.get());
    LDEBUG << "SQL query: " << query;

    if (sqliteConn)
    {
        sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
        sqlite3_prepare_v2(sqliteConn, tmpQuery.get(), -1, &stmt, nullptr);
        failOnError(tmpQuery.get());
    }
    else
    {
        LERROR << "Connection to RASBASE has not been opened, cannot execute query: " << query;
        throw r_Error(DATABASE_CONNECT_FAILED);
    }
}

SQLiteQuery::~SQLiteQuery()
{
    finalize();
}

SQLiteQuery::SQLiteQuery(const SQLiteQuery &o)
{
    stmt = o.stmt;
    query = o.query;
    columnCounter = o.columnCounter;
}

void SQLiteQuery::finalize()
{
    if (stmt != nullptr)
    {
        sqlite3_finalize(stmt);
        auto msg = "finalize prepared statement for query: " + query;
        warnOnError(msg.c_str());
        stmt = nullptr;
        query = "";
    }
}

void SQLiteQuery::bindNull()
{
    assert(stmt);
    sqlite3_bind_null(stmt, ++columnCounter);
    warnOnError("bind null");
}

void SQLiteQuery::bindInt(int param)
{
    assert(stmt);
    sqlite3_bind_int(stmt, ++columnCounter, param);
    warnOnError("bind int");
}

void SQLiteQuery::bindLong(long long param)
{
    assert(stmt);
    sqlite3_bind_int64(stmt, ++columnCounter, param);
    warnOnError("bind long");
}

void SQLiteQuery::bindDouble(double param)
{
    assert(stmt);
    sqlite3_bind_double(stmt, ++columnCounter, param);
    warnOnError("bind double");
}

void SQLiteQuery::bindString(const char *param, int size)
{
    assert(stmt);
    sqlite3_bind_text(stmt, ++columnCounter, param, size, SQLITE_TRANSIENT);
    warnOnError("bind string");
}

void SQLiteQuery::bindBlob(const char *param, int size)
{
    assert(stmt);
    sqlite3_bind_blob(stmt, ++columnCounter, param, size, SQLITE_TRANSIENT);
    warnOnError("bind blob");
}

void SQLiteQuery::execute(int fail)
{
    assert(stmt);

    sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
    sqlite3_step(stmt);
    if (fail)
    {
        failOnError(query.c_str());
    }
    else
    {
        warnOnError(query.c_str());
    }
}

void SQLiteQuery::execute(const char *q)
{
    LDEBUG << "SQL query: " << q;
    if (sqliteConn)
    {
        sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
        sqlite3_exec(sqliteConn, q, nullptr, nullptr, nullptr);
        failOnError(q);
    }
    else
    {
        LERROR << "Connection to RASBASE has not been opened, cannot execute query: "
               << q;
        throw r_Error(DATABASE_CONNECT_FAILED);
    }
}

void SQLiteQuery::executeWithParams(const char *format, ...)
{
    std::unique_ptr<char[]> qtmp(new char[QUERY_MAXLEN]);
    va_list args;
    va_start(args, format);
    vsnprintf(qtmp.get(), QUERY_MAXLEN, format, args);
    va_end(args);

    const auto *q = qtmp.get();
    LDEBUG << "SQL query: " << q;

    if (sqliteConn)
    {
        sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT);
        sqlite3_exec(sqliteConn, q, nullptr, nullptr, nullptr);
        failOnError(q);
    }
    else
    {
        LERROR << "Connection to RASBASE has not been opened, cannot execute query: " << q;
        throw r_Error(DATABASE_CONNECT_FAILED);
    }
}

bool SQLiteQuery::returnsRows(const std::string &format)
{
    SQLiteQuery query(format.c_str());
    return static_cast<bool>(query.nextRow());
}

sqlite3 *SQLiteQuery::getConnection()
{
    return sqliteConn;
}

int SQLiteQuery::isTransactionActive()
{
    return sqliteConn != nullptr && sqlite3_get_autocommit(sqliteConn) == 0;
}

int SQLiteQuery::nextRow()
{
    assert(stmt);
    int rc = sqlite3_step(stmt);
    if (rc != SQLITE_ROW && rc != SQLITE_DONE)
    {
        failOnError(query.c_str());
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
    assert(stmt);
    auto ret = sqlite3_column_int(stmt, columnCounter++);
    warnOnError("getting int column");
    return ret;
}

long long SQLiteQuery::nextColumnLong()
{
    assert(stmt);
    auto ret = sqlite3_column_int64(stmt, columnCounter++);
    warnOnError("getting long column");
    return ret;
}

double SQLiteQuery::nextColumnDouble()
{
    assert(stmt);
    auto ret = sqlite3_column_double(stmt, columnCounter++);
    warnOnError("getting double column");
    return ret;
}

const char *SQLiteQuery::nextColumnString()
{
    assert(stmt);
    auto ret = reinterpret_cast<const char *>(sqlite3_column_text(stmt, columnCounter++));
    warnOnError("getting string column");
    return ret;
}

const char *SQLiteQuery::nextColumnBlob()
{
    assert(stmt);
    auto ret = static_cast<const char *>(sqlite3_column_blob(stmt, columnCounter++));
    warnOnError("getting blob column");
    return ret;
}

int SQLiteQuery::currColumnBytes()
{
    assert(stmt);
    auto ret = sqlite3_column_bytes(stmt, columnCounter - 1);
    warnOnError("getting bytes column");
    return ret;
}

int SQLiteQuery::currColumnType()
{
    assert(stmt);
    auto ret = sqlite3_column_type(stmt, columnCounter);
    warnOnError("getting column type");
    return ret;
}

int SQLiteQuery::currColumnNull()
{
    return currColumnType() == SQLITE_NULL;
}

void SQLiteQuery::closeConnection()
{
    if (sqliteConn != nullptr)
    {
        if (sqlite3_close(sqliteConn) != SQLITE_OK)
        {
            warnOnError("close RASBASE connection");
        }
        sqliteConn = nullptr;
    }
}

bool SQLiteQuery::openConnection(const char *globalConnectId)
{
    sqlite3_enable_shared_cache(0);
    auto rc = sqlite3_open(globalConnectId, &sqliteConn);
    if (rc != SQLITE_OK)
    {
        LERROR << "Connect unsuccessful; wrong connect string '" << globalConnectId << "'?";
        LERROR << "Reason: " << sqlite3_errstr(rc);
        throw r_Error(DATABASE_CONNECT_FAILED);
    }
    else
    {
        LDEBUG << "Connected successfully to '" << globalConnectId << "'";
        std::string options = "PRAGMA journal_mode=WAL; PRAGMA busy_timeout=" +
                              std::to_string(SQLITE_BUSY_TIMEOUT);
        sqlite3_exec(sqliteConn, options.c_str(), NULL, 0, NULL);
        failOnError(options.c_str());
        sqlite3_extended_result_codes(sqliteConn, 1);
        warnOnError("enable extended result codes");
    }
    return true;
}

bool SQLiteQuery::isConnected()
{
    return sqliteConn != nullptr;
}

void SQLiteQuery::failOnError(const char *stmt)
{
    if (sqliteConn == nullptr)
    {
        LERROR << "Not connected to database, cannot execute query: " << stmt;
        throw r_Error(DATABASE_CONNECT_FAILED);
    }
    else
    {
        auto rc = sqlite3_errcode(sqliteConn);
        if (rc != SQLITE_OK && rc != SQLITE_ROW && rc != SQLITE_DONE &&
            rc != SQLITE_NOTICE && rc != SQLITE_WARNING)
        {
            const auto *msg = sqlite3_errstr(rc);
            LERROR << "SQL query failed \"" << stmt << "\", "
                   << "database error " << rc << ": " << msg;
            throw r_Ebase_dbms(rc, msg);
        }
    }
}

void SQLiteQuery::warnOnError(const char *stmt)
{
    if (sqliteConn == nullptr)
    {
        LWARNING << "Not connected to database, cannot execute query: " << stmt;
    }
    else
    {
        auto rc = sqlite3_errcode(sqliteConn);
        if (rc != SQLITE_OK && rc != SQLITE_ROW && rc != SQLITE_DONE &&
                rc != SQLITE_NOTICE && rc != SQLITE_WARNING)
        {
            const auto *msg = sqlite3_errstr(rc);
            LWARNING << "SQL query failed \"" << stmt << "\", "
                     << "database error " << rc << ": " << msg;
        }
    }
}

#endif
