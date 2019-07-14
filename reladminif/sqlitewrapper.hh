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
 * File:   sqlitewrapper.hh
 * Author: Dimitar Misev
 *
 * Created on May 8, 2014, 6:05 PM
 */

#pragma once

#include "config.h"
#ifdef BASEDB_SQLITE
#include "sqlglobals.h"
#include <sqlite3.h>
#include <string>


/**
 * Convenience class for executing SQLite queries.
 */
class SQLiteQuery
{
public:
    /**
     * Construct a query object from a constant string SQL query.
     * @param query the SQL query to be turned into an SQLite statement.
     */
    SQLiteQuery(char query[]);

    /**
     * Construct a query object from an SQL query given as a printf format
     * string and a list of parameters to be substituted with vsnprintf.
     * @param query the SQL query to be turned into an SQLite statement.
     */
    SQLiteQuery(const char *format, ...);

    /**
     * Destructor
     */
    ~SQLiteQuery();

    /**
     * Copy constructor.
     */
    SQLiteQuery(const SQLiteQuery &o);

    /**
     * Finalize any query objects.
     */
    void finalize();

    /**
     * @return go to next row of the SQL result set, and return true if there
     * is data or false otherwise. Initializes the column counter to 0.
     */
    int nextRow();

    /**
     * advance to next column. The first call of this method after a nextRow()
     * will go to the second column.
     */
    void nextColumn();

    /**
     * Returns the current column value, and then advances the counter to the
     * next column.
     * @return the int value of the current column.
     */
    int nextColumnInt();

    /**
     * Returns the current column value, and then advances the counter to the
     * next column.
     * @return the long long value of the current column.
     */
    long long nextColumnLong();

    /**
     * Returns the current column value, and then advances the counter to the
     * next column.
     * @return the double value of the current column.
     */
    double nextColumnDouble();

    /**
     * Returns the current column value, and then advances the counter to the
     * next column.
     * @return the string value of the current column.
     */
    const char *nextColumnString();

    /**
     * Returns the current column value, and then advances the counter to the
     * next column.
     * @return the blob value of the current column.
     */
    const char *nextColumnBlob();

    /**
     * Returns the current column length in bytes, if the column is blob/text.
     * @return the length of the value of the current column.
     */
    int currColumnBytes();

    /**
     * Returns the current column type, and then advances the counter to the
     * next column.
     * @return the type of the current column.
     */
    int currColumnType();

    /**
     * @return true if the value of the current column is null, or false
     * otherwise.
     */
    int currColumnNull();

    /**
     * Bind null value to the SQL statement.
     */
    void bindNull();

    /**
     * @param param the int parameter to bind to the SQL statement.
     */
    void bindInt(int param);

    /**
     * @param param the long long parameter to bind to the SQL statement.
     */
    void bindLong(long long param);

    /**
     * @param param the double parameter to bind to the SQL statement.
     */
    void bindDouble(double param);

    /**
     * @param param the string parameter to bind to the SQL statement.
     */
    void bindString(const char *param, int size);

    /**
     * @param param the blob parameter to bind to the SQL statement.
     */
    void bindBlob(const char *param, int size);

    /**
     * Execute this SQL statement.
     * @param fail generate exception if true (default), or just a warning
     * otherwise.
     */
    void execute(int fail = 1);

    /**
     * Execute query in one step.
     */
    static void execute(const char *query);

    /**
     * Execute query in one step, where the query is provided as a printf
     * formatted string.
     */
    static void executeWithParams(const char *format, ...);

    /**
     * Execute query, where the query is provided as a printf
     * formatted string, and return true if the query returns any rows or
     * false otherwise.
     */
    static bool returnsRows(const std::string &format);

    /**
     * @return 1 (true) if a transaction is active, 0 (false) otherwise.
     */
    static int isTransactionActive();

    /**
     * @return the sqlite connection.
     */
    static sqlite3 *getConnection();

    /**
     * @return the sqlite statement.
     */
    sqlite3_stmt *getStatement();

    static void closeConnection();

    static bool openConnection(const char *globalConnectId);

    static bool isConnected();

    /*@Doc:
    * Throw an exception when an error happens.
    */
    static void failOnError(const char *msg);

    /*@Doc:
    * Print a warning when an error happens.
    */
    static void warnOnError(const char *msg);

private:

    sqlite3_stmt *stmt{NULL};
    // saved for debugging purposes, e.g. in case the query execution fails
    std::string query;
    int columnCounter;

    // 10 minutes timeout, in case RASBASE is locked by another rasserver for writing
    static constexpr int SQLITE_BUSY_TIMEOUT{10 * 60 * 1000};
};

#endif
