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

struct BusyHandlerAttr {
	int maxRetry;
	int sleepMs;
};

// globally defined here, used as an extern in the new engine as well.
sqlite3 *sqliteConn = NULL;

SQLiteQuery::SQLiteQuery(const char *q) :
    stmt(nullptr), query(q), columnCounter(0)
{
    LTRACE << "SQL query: " << query;
    if (sqliteConn)
    {
        sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT_MS);
        sqlite3_prepare_v2(sqliteConn, q, -1, &stmt, nullptr);
        failOnError(query.c_str());
    }
    else
    {
        LERROR << "Connection to RASBASE has not been opened, cannot execute query: " << q;
        throw r_Error(DATABASE_CONNECT_FAILED);
    }
}

SQLiteQuery::SQLiteQuery(const std::string &q)
    : stmt(nullptr), query(q), columnCounter(0)
{
    LTRACE << "SQL query: " << query;

    if (sqliteConn)
    {
        sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT_MS);
        sqlite3_prepare_v2(sqliteConn, query.c_str(), -1, &stmt, nullptr);
        failOnError(query.c_str());
    }
    else
    {
        LERROR << "Connection to RASBASE has not been opened, cannot execute query: " << query;
        throw r_Error(DATABASE_CONNECT_FAILED);
    }
}

SQLiteQuery::SQLiteQuery(std::string &&q)
  : stmt(nullptr), query(std::move(q)), columnCounter(0)
{
    LTRACE << "SQL query: " << query;
  
    if (sqliteConn)
    {
        sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT_MS);
        sqlite3_prepare_v2(sqliteConn, query.c_str(), -1, &stmt, nullptr);
        failOnError(query.c_str());
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

    sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT_MS);
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
    LTRACE << "SQL query: " << q;
    if (sqliteConn)
    {
        sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT_MS);
        sqlite3_exec(sqliteConn, q, nullptr, nullptr, nullptr);
        failOnError(q);
    }
    else
    {
        LERROR << "Connection to RASBASE has not been opened, cannot execute query: " << q;
        throw r_Error(DATABASE_CONNECT_FAILED);
    }
}

void SQLiteQuery::execute(std::string &&q)
{
    LTRACE << "SQL query: " << q;
    if (sqliteConn)
    {
        sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT_MS);
        sqlite3_exec(sqliteConn, q.c_str(), nullptr, nullptr, nullptr);
        failOnError(q.c_str());
    }
    else
    {
        LERROR << "Connection to RASBASE has not been opened, cannot execute query: " << q;
        throw r_Error(DATABASE_CONNECT_FAILED);
    }
}

bool SQLiteQuery::returnsRows(const std::string &format)
{
    SQLiteQuery query(format);
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

long SQLiteQuery::nextColumnLong()
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
        sqlite3_busy_timeout(sqliteConn, SQLITE_BUSY_TIMEOUT_MS);
        if (sqlite3_close(sqliteConn) != SQLITE_OK)
        {
            warnOnError("close RASBASE connection");
        }
        sqliteConn = nullptr;
    }
}

void SQLiteQuery::openConnection(const char *globalConnectId)
{
    sqlite3_enable_shared_cache(0);
    auto rc = sqlite3_open(globalConnectId, &sqliteConn);
    if (rc != SQLITE_OK)
    {
        LERROR << "Connect unsuccessful; wrong connect string '" << globalConnectId << "'? "
               << "Reason: " << sqlite3_errstr(rc);
        throw r_Error(DATABASE_CONNECT_FAILED);
    }
    else
    {
        LDEBUG << "Connected successfully to '" << globalConnectId << "'";
        BusyHandlerAttr attr;
        attr.sleepMs = SQLITE_BUSY_WAIT;
        attr.maxRetry = SQLITE_BUSY_TIMEOUT_MS / SQLITE_BUSY_WAIT;
        sqlite3_busy_handler(sqliteConn, busyHandler, &attr);
        std::string options = "PRAGMA journal_mode=WAL";
        sqlite3_exec(sqliteConn, options.c_str(), NULL, 0, NULL);
        failOnError(options.c_str());
        sqlite3_extended_result_codes(sqliteConn, 1);
        warnOnError("enable extended result codes");
    }
}

void SQLiteQuery::interruptTransaction()
{
    if (sqliteConn != nullptr) {
        sqlite3_interrupt(sqliteConn);
    }
}

bool SQLiteQuery::isConnected()
{
    return sqliteConn != nullptr;
}

int SQLiteQuery::busyHandler(void *data, int retry)
{
    BusyHandlerAttr* attr = (BusyHandlerAttr*)data;
    
    if (retry < attr->maxRetry) {
      // sleep some ms before retrying
      LDEBUG << "RASBASE is locked for transaction, waiting before retrying for the " 
             << retry << " time.";
      sqlite3_sleep(attr->sleepMs);
      // return non-zero to let caller retry again
      return 1;
    }
    // sleeping timed out, return zero to let caller return SQLITE_BUSY
    return 0;
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
