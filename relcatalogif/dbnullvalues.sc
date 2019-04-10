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

#include "config.h"
#include "dbnullvalues.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/sqlitewrapper.hh"
#include <limits>
#include <logging.hh>

DBNullvalues::DBNullvalues() : DBObject(), r_Nullvalues()
{
    objecttype = OId::DBNULLVALUESOID;
}

DBNullvalues::DBNullvalues(const OId &id) : DBObject(id), r_Nullvalues()
{
    objecttype = OId::DBNULLVALUESOID;
    readFromDb();
}

DBNullvalues::DBNullvalues(const DBNullvalues &old)
    : DBObject(old), r_Nullvalues(old)
{
    objecttype = OId::DBNULLVALUESOID;
}

DBNullvalues::DBNullvalues(const r_Nullvalues &old)
    : DBObject(), r_Nullvalues(old)
{
    objecttype = OId::DBNULLVALUESOID;
}

DBNullvalues::~DBNullvalues() noexcept(false)
{
    validate();
}

DBNullvalues &DBNullvalues::operator=(const DBNullvalues &old)
{
    if (this == &old)
    {
        return *this;
    }
    r_Nullvalues::operator=(old);
    setModified();
    return *this;
}

DBNullvalues &DBNullvalues::operator=(const r_Nullvalues &old)
{
    if (this == &old)
    {
        return *this;
    }
    r_Nullvalues::operator=(old);
    setModified();
    return *this;
}

r_Bytes DBNullvalues::getMemorySize() const
{
    return DBObject::getMemorySize() + sizeof(r_Nullvalues) +
           nullvalues.size() * sizeof(nullvalues[0]);
}

void DBNullvalues::insertInDb()
{
    for (size_t i = 0; i < nullvalues.size(); ++i)
    {
        if (std::isnan(nullvalues[i].first))
        {
            SQLiteQuery::executeWithParams(
                "INSERT INTO RAS_NULLVALUEPAIRS ( NullValueOId, Count, Low, High ) "
                "VALUES ( %lld, %d, NULL, NULL)",
                myOId.getCounter(), i);
        }
        else
        {
            SQLiteQuery::executeWithParams(
                "INSERT INTO RAS_NULLVALUEPAIRS ( NullValueOId, Count, Low, High ) "
                "VALUES ( %lld, %d, %f, %f)",
                myOId.getCounter(), i, nullvalues[i].first, nullvalues[i].second);
        }
    }
    DBObject::insertInDb();
}

void DBNullvalues::updateInDb()
{
    for (size_t i = 0; i < nullvalues.size(); ++i)
    {
        if (std::isnan(nullvalues[i].first))
        {
            SQLiteQuery::executeWithParams(
                "INSERT OR REPLACE INTO RAS_NULLVALUEPAIRS ( NullValueOId, Count, Low, High ) "
                "VALUES ( %lld, %d, NULL, NULL)",
                myOId.getCounter(), i);
        }
        else
        {
            SQLiteQuery::executeWithParams(
                "INSERT OR REPLACE INTO RAS_NULLVALUEPAIRS ( NullValueOId, Count, Low, High ) "
                "VALUES ( %lld, %d, %f, %f)",
                myOId.getCounter(), i, nullvalues[i].first, nullvalues[i].second);
        }
    }
    DBObject::updateInDb();
}

void DBNullvalues::deleteFromDb()
{
    SQLiteQuery::executeWithParams(
        "DELETE FROM RAS_NULLVALUEPAIRS "
        "WHERE NullValueOId = %lld",
        myOId.getCounter());
    DBObject::deleteFromDb();
}

void DBNullvalues::readFromDb()
{
    SQLiteQuery query(
        "SELECT * FROM RAS_NULLVALUEPAIRS "
        "WHERE NullValueOId = %lld "
        "ORDER BY Count ASC",
        myOId.getCounter());

    while (query.nextRow())
    {
        query.nextColumn();  // skip OId column
        query.nextColumn();  // skip count column, they are ordered already
        double low = (query.currColumnNull())
                     ? std::numeric_limits<double>::quiet_NaN()
                     : query.nextColumnDouble();
        double high = (query.currColumnNull())
                      ? std::numeric_limits<double>::quiet_NaN()
                      : query.nextColumnDouble();
        LDEBUG << "read null values: " << low << ":" << high;
        nullvalues.emplace_back(low, high);
    }

    if (nullvalues.empty())
    {
        LERROR << "DBNullvalues::readFromDb() - nullvalues object: "
               << myOId.getCounter() << " not found in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND,
                           "nullvalues data not found in the database.");
    }

    DBObject::readFromDb();
}
