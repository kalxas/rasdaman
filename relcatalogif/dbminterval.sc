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
#include <fmt/core.h>

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

void DBMinterval::updateMinterval(const r_Minterval &domain) {
  r_Minterval::operator=(domain);
  setModified();
}

r_Bytes DBMinterval::getMemorySize() const
{
    return DBObject::getMemorySize() +
           sizeof(r_Minterval) +
           dimension() * (4 + 4 + 1 + 1);
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
    SQLiteQuery query(fmt::format("SELECT Dimension FROM RAS_DOMAINS WHERE DomainId = {}", myOId.getCounter()));
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

    SQLiteQuery::execute(fmt::format(
        "INSERT INTO RAS_DOMAINS ( DomainId, Dimension) VALUES  ({}, {})",
        domainid, dimension()));

    for (r_Dimension count = 0; count < dimension(); count++)
    {
        std::string high, low;
        setBounds(count, high, low);

        std::string axisName1;
        
        
        if (!has_axis_names())
        {
            axisName1 = "D" + std::to_string(count);
        }
        else
        {
            axisName1 = intervals[count].get_axis_name();
        }
        SQLiteQuery::execute(fmt::format(
            "INSERT INTO RAS_DOMAINVALUES ( DomainId, DimensionCount, Low, High, AxisName ) "
            "VALUES  ( {}, {}, {}, {}, \"{}\")", domainid, count, low.c_str(), high.c_str(), axisName1.c_str()));
    }

    DBObject::insertInDb();
}

void DBMinterval::updateInDb()
{
    const auto domainid = myOId.getCounter();
    auto dimensionInDb = getDimensionInDb();

    if (dimensionInDb != dimension())
    {
        if (dimensionInDb < dimension())
        {
            // insert more rows in RAS_DOMAINVALUES
            for (r_Dimension count = dimensionInDb; count < dimension(); count++)
            {
                std::string axisName1 = "D" + std::to_string(count);
                SQLiteQuery::execute(fmt::format(
                    "INSERT INTO RAS_DOMAINVALUES ( DomainId, DimensionCount, AxisName) VALUES  ( {}, {}, \"{}\" )",
                    domainid, count, axisName1.c_str()));
            }
        }
        else
        {
            // delete superfluous dimensions
            SQLiteQuery::execute(fmt::format(
                "DELETE FROM RAS_DOMAINVALUES WHERE DomainId = {} AND DimensionCount > {}",
                domainid, dimension()));
        }
        SQLiteQuery::execute(fmt::format(
            "UPDATE RAS_DOMAINS SET Dimension = {} WHERE DomainId = {}", dimensionInDb, domainid));
    }

    for (r_Dimension count = 0; count < dimension(); count++)
    {
        std::string high, low;
        setBounds(count, high, low);

        SQLiteQuery::execute(fmt::format(
            "UPDATE RAS_DOMAINVALUES SET Low = {}, High = {} WHERE DomainId = {} AND DimensionCount = {}",
            low.c_str(), high.c_str(), domainid, count));
    }

    DBObject::updateInDb();
}

void DBMinterval::deleteFromDb()
{
    SQLiteQuery::execute(fmt::format("DELETE FROM RAS_DOMAINS WHERE DomainId = {}", myOId.getCounter()));
    SQLiteQuery::execute(fmt::format("DELETE FROM RAS_DOMAINVALUES WHERE DomainId = {}", myOId.getCounter()));
    DBObject::deleteFromDb();
}

bool checkAxisNameColumn();
bool checkAxisNameColumn()
{
    static const int colCountWithAxisNameCol = 5;
    static std::string checkQuery = "PRAGMA table_info('RAS_DOMAINVALUES')";
    SQLiteQuery checkColumn(checkQuery);
    int count = 0;
    while (checkColumn.nextRow())
        ++count;
    return count == colCountWithAxisNameCol;
}

void DBMinterval::readFromDb()
{
    static const char unbounded = '*';
    static bool hasAxisNameColumn = checkAxisNameColumn();

    const auto domainid = myOId.getCounter();

    auto dimensionality = getDimensionInDb();
    intervals = std::vector<r_Sinterval>(dimensionality);

    for (r_Dimension count = 0; count < dimensionality; count++)
    {

        const char *queryStr = "SELECT AxisName, Low, High FROM RAS_DOMAINVALUES WHERE DimensionCount = {} AND DomainId = {}";
        if (!hasAxisNameColumn)
            queryStr = "SELECT Low, High FROM RAS_DOMAINVALUES WHERE DimensionCount = {} AND DomainId = {}";
        SQLiteQuery query(fmt::format(queryStr, count, domainid));
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
                axisName = "D" + std::to_string(count);
                if (hasAxisNameColumn)
                    query.nextColumn();
            }
            intervals[count].set_axis_name(axisName);
            
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
