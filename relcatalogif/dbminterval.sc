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

#include "dbminterval.hh"
#include "reladminif/sqlitewrapper.hh"
#include "reladminif/sqlglobals.h"
#include "raslib/sinterval.hh"
#include <logging.hh>

DBMinterval::DBMinterval()
    : DBObject(), r_Minterval()
{
    objecttype = OId::DBMINTERVALOID;
}

DBMinterval::DBMinterval(const OId &id)
    : DBObject(id), r_Minterval()
{
    objecttype = OId::DBMINTERVALOID;
    readFromDb();
}

DBMinterval::DBMinterval(r_Dimension dim)
    : DBObject(), r_Minterval(dim)
{
    objecttype = OId::DBMINTERVALOID;
}

DBMinterval::DBMinterval(const char *dom)
    : DBObject(), r_Minterval(const_cast<char *>(dom))
{
    objecttype = OId::DBMINTERVALOID;
}

DBMinterval::DBMinterval(const DBMinterval &old)
    : DBObject(old), r_Minterval(old)
{
    objecttype = OId::DBMINTERVALOID;
}

DBMinterval::DBMinterval(const r_Minterval &old)
    : DBObject(), r_Minterval(old)
{
    objecttype = OId::DBMINTERVALOID;
}

DBMinterval::DBMinterval(const r_Minterval& old, const std::vector<std::string> &axisNames2)
    : DBObject(), r_Minterval(old, axisNames2)
{
    objecttype = OId::DBMINTERVALOID;
}

DBMinterval::~DBMinterval() noexcept(false)
{
    validate();
}

DBMinterval &DBMinterval::operator=(const DBMinterval &old)
{
    if (this == &old)
    {
        return *this;
    }
    r_Minterval::operator=(old);
    setModified();
    return *this;
}

DBMinterval &DBMinterval::operator=(const r_Minterval &old)
{
    if (this == &old)
    {
        return *this;
    }
    r_Minterval::operator=(old);
    setModified();
    return *this;
}

r_Bytes DBMinterval::getMemorySize() const
{
    return DBObject::getMemorySize() +
           sizeof(r_Minterval) +
           dimensionality * (4 + 4 + 1 + 1);
}

void DBMinterval::setBounds(r_Dimension count, std::string &high, std::string &low) const
{
    low  = intervals[count].is_low_fixed()
         ? std::to_string(intervals[count].low())  : std::string{"NULL"};
    high = intervals[count].is_high_fixed()
         ? std::to_string(intervals[count].high()) : std::string{"NULL"};
}

r_Dimension DBMinterval::getDimensionInDb() const
{
    SQLiteQuery query("SELECT Dimension FROM RAS_DOMAINS WHERE DomainId = %lld", myOId.getCounter());
    if (query.nextRow())
    {
        return static_cast<r_Dimension>(query.nextColumnInt());
    }
    else
    {
        LERROR << "minterval object " << myOId.getCounter() << " not found in table RAS_DOMAINS.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "minterval object not found in table RAS_DOMAINS.");
    }

}

void DBMinterval::insertInDb()
{
    const auto domainid = myOId.getCounter();

    SQLiteQuery::executeWithParams(
        "INSERT INTO RAS_DOMAINS ( DomainId, Dimension) VALUES  ( %lld, %d)",
        domainid, dimensionality);

    for (decltype(dimensionality) count = 0; count < dimensionality; count++)
    {
        std::string high, low;
        setBounds(count, high, low);

        std::string axisName1;

        if (axisNames.empty())
        {
            axisName1 = "d" + std::to_string(count);
        }
        else
        {
            axisName1 = axisNames.at(count);
        }
        SQLiteQuery::executeWithParams(
            "INSERT INTO RAS_DOMAINVALUES ( DomainId, DimensionCount, Low, High, AxisName ) "
            "VALUES  ( %lld, %d, %s, %s, \"%s\")", domainid, count, low.c_str(), high.c_str(), axisName1.c_str());
    }

    DBObject::insertInDb();
}

void DBMinterval::updateInDb()
{
    const auto domainid = myOId.getCounter();
    auto dimensionInDb = getDimensionInDb();

    if (dimensionInDb != dimensionality)
    {
        if (dimensionInDb < dimensionality)
        {
            // insert more rows in RAS_DOMAINVALUES
            for (r_Dimension count = dimensionInDb; count < dimensionality; count++)
            {
                std::string axisName1 = "d" + std::to_string(count);
                SQLiteQuery::executeWithParams(
                    "INSERT INTO RAS_DOMAINVALUES ( DomainId, DimensionCount, AxisName) VALUES  ( %lld, %d, \"%s\" )",
                    domainid, count, axisName1.c_str());
            }
        }
        else
        {
            // delete superfluous dimensions
            SQLiteQuery::executeWithParams(
                "DELETE FROM RAS_DOMAINVALUES WHERE DomainId = %lld AND DimensionCount > %d",
                domainid, dimensionality);
        }
        SQLiteQuery::executeWithParams(
            "UPDATE RAS_DOMAINS SET Dimension = %d WHERE DomainId = %lld", dimensionInDb, domainid);
    }

    for (r_Dimension count = 0; count < dimensionality; count++)
    {
        std::string high, low;
        setBounds(count, high, low);

        SQLiteQuery::executeWithParams(
            "UPDATE RAS_DOMAINVALUES SET Low = %s, High = %s WHERE DomainId = %lld AND DimensionCount = %d",
            low.c_str(), high.c_str(), domainid, count);
    }

    DBObject::updateInDb();
}

void DBMinterval::deleteFromDb()
{
    SQLiteQuery::executeWithParams("DELETE FROM RAS_DOMAINS WHERE DomainId = %lld", myOId.getCounter());
    SQLiteQuery::executeWithParams("DELETE FROM RAS_DOMAINVALUES WHERE DomainId = %lld", myOId.getCounter());
    DBObject::deleteFromDb();
}

bool checkAxisNameColumn();
bool checkAxisNameColumn()
{
    static const int colCountWithAxisNameCol = 5;
    SQLiteQuery checkColumn("PRAGMA table_info('RAS_DOMAINVALUES')");
    int count = 0;
    while (checkColumn.nextRow())
        ++count;
    return count == colCountWithAxisNameCol;
}

void DBMinterval::readFromDb()
{
    static const char unbounded = '*';

    const auto domainid = myOId.getCounter();

    dimensionality = getDimensionInDb();
    delete [] intervals, intervals = new r_Sinterval[dimensionality];

    streamInitCnt = dimensionality;
    for (r_Dimension count = 0; count < dimensionality; count++)
    {
        static bool hasAxisNameColumn = checkAxisNameColumn();

        const char *queryStr = "SELECT AxisName, Low, High FROM RAS_DOMAINVALUES WHERE DimensionCount = %d AND DomainId = %lld";
        if (!hasAxisNameColumn)
            queryStr = "SELECT Low, High FROM RAS_DOMAINVALUES WHERE DimensionCount = %d AND DomainId = %lld";
        SQLiteQuery query(queryStr, count, domainid);
        if (query.nextRow())
        {
            // get axis name
            std::string axisName;
            if (hasAxisNameColumn && !query.currColumnNull())
            {
                axisName = query.nextColumnString();
            }
            else
            {
                axisName = "d" + std::to_string(count);
                if (hasAxisNameColumn) query.nextColumn();
            }
            axisNames.push_back(axisName);
            
            if (!query.currColumnNull())
                intervals[count].set_low(query.nextColumnLong());
            else
                intervals[count].set_low(unbounded), query.nextColumn();

            if (!query.currColumnNull())
                intervals[count].set_high(query.nextColumnLong());
            else
                intervals[count].set_high(unbounded), query.nextColumn();
        }
        else
        {
            LERROR << "minterval object " << domainid << " has no dimension " << count 
                   << " in RAS_DOMAINVALUES.";
            throw r_Ebase_dbms(SQLITE_NOTFOUND,
                "minterval object has no dimension description in RAS_DOMAINVALUES.");
        }
    }
    DBObject::readFromDb();
}
