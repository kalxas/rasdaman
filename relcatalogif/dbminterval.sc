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

/*************************************************************************
 *
 *
 * PURPOSE:
 *      Code with embedded SQL for PostgreSQL DBMS
 *
 *
 * COMMENTS:
 *      none
 *
 ***********************************************************************/

#include "dbminterval.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/sqlitewrapper.hh"
#include "raslib/sinterval.hh"
#include <logging.hh>

DBMinterval::DBMinterval() : DBObject(), r_Minterval()
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

r_Bytes DBMinterval::getMemorySize() const
{
    return DBObject::getMemorySize() + sizeof(r_Minterval) +
           dimensionality * (4 + 4 + 1 + 1);
}

void DBMinterval::insertInDb()
{
    long long domainid;
    unsigned int count;
    r_Dimension dimension2;
    char high[STRING_MAXLEN];
    char low[STRING_MAXLEN];

    domainid = myOId.getCounter();
    dimension2 = dimensionality;

    SQLiteQuery::executeWithParams(
        "INSERT INTO RAS_DOMAINS ( DomainId, Dimension) VALUES  ( %lld, %d)",
        domainid, dimension2);

    for (count = 0; count < dimensionality; count++)
    {
        if (intervals[count].is_low_fixed())
        {
            sprintf(low, "%lld", intervals[count].low());
        }
        else
        {
            strcpy(low, "NULL");
        }
        if (intervals[count].is_high_fixed())
        {
            sprintf(high, "%lld", intervals[count].high());
        }
        else
        {
            strcpy(high, "NULL");
        }

        SQLiteQuery::executeWithParams(
            "INSERT INTO RAS_DOMAINVALUES ( DomainId, DimensionCount, Low, High ) "
            "VALUES  ( %lld, %d, %s, %s)",
            domainid, count, low, high);
    }

    DBObject::insertInDb();
}

void DBMinterval::updateInDb()
{
    long long domainid;
    unsigned int count;
    r_Dimension dimension2;
    char high[STRING_MAXLEN];
    char low[STRING_MAXLEN];

    domainid = myOId.getCounter();

    SQLiteQuery query("SELECT Dimension FROM RAS_DOMAINS WHERE DomainId = %lld",
                      domainid);
    if (query.nextRow())
    {
        dimension2 = static_cast<r_Dimension>(query.nextColumnInt());
    }
    else
    {
        LERROR << "DBMinterval::updateInDb() - domain object: " << domainid
               << " not found in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "domain object not found in the database.");
    }

    if (dimension2 < dimensionality)
    {
        // insert more rows in RAS_DOMAINVALUES
        for (count = dimension2; count < dimensionality; count++)
        {
            SQLiteQuery::executeWithParams(
                "INSERT INTO RAS_DOMAINVALUES ( DomainId, DimensionCount) VALUES  ( %lld, %d )",
                domainid, count);
        }
        dimension2 = dimensionality;
        SQLiteQuery::executeWithParams(
            "UPDATE RAS_DOMAINS SET Dimension = %d WHERE DomainId = %lld",
            dimension2, domainid);
    }
    else
    {
        if (dimension2 > dimensionality)
        {
            // delete superfluous dimensions
            for (count = dimension2; count > dimensionality; count--)
            {
                SQLiteQuery::executeWithParams(
                    "DELETE FROM RAS_DOMAINVALUES WHERE DomainId = %lld AND DimensionCount = %d",
                    domainid, count);
            }
            dimension2 = dimensionality;
            SQLiteQuery::executeWithParams(
                "UPDATE RAS_DOMAINS SET Dimension = %d WHERE DomainId = %lld",
                dimension2, domainid);
        }
        else
        {
            // only update dimension boundaries
        }
    }

    for (count = 0; count < dimensionality; count++)
    {
        if (intervals[count].is_low_fixed())
        {
            sprintf(low, "%lld", intervals[count].low());
        }
        else
        {
            strcpy(low, "NULL");
        }
        if (intervals[count].is_high_fixed())
        {
            sprintf(high, "%lld", intervals[count].high());
        }
        else
        {
            strcpy(high, "NULL");
        }

        SQLiteQuery::executeWithParams(
            "UPDATE RAS_DOMAINVALUES SET Low = %s, High = %s WHERE DomainId = %lld AND DimensionCount = %d",
            low, high, domainid, count);
    }

    DBObject::updateInDb();
}

void DBMinterval::deleteFromDb()
{
    long long domainid = myOId.getCounter();
    SQLiteQuery::executeWithParams("DELETE FROM RAS_DOMAINS WHERE DomainId = %lld", domainid);
    SQLiteQuery::executeWithParams("DELETE FROM RAS_DOMAINVALUES WHERE DomainId = %lld", domainid);
    DBObject::deleteFromDb();
}

void DBMinterval::readFromDb()
{
    char undefined = '*';
    long long domainid;
    long count;
    long dimension2;

    domainid = myOId.getCounter();

    SQLiteQuery query("SELECT Dimension FROM RAS_DOMAINS WHERE DomainId = %lld",
                      domainid);
    if (query.nextRow())
    {
        dimension2 = query.nextColumnInt();
    }
    else
    {
        LERROR << "DBMinterval::readFromDb() - domain object: " << domainid
               << " not found in the database..";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "domain object not found in the database.");
    }

    dimensionality = dimension2;
    delete[] intervals;
    intervals = new r_Sinterval[dimensionality];
    streamInitCnt = 0;

    for (count = 0; count < dimension2; count++)
    {
        SQLiteQuery query2(
            "SELECT Low, High FROM RAS_DOMAINVALUES WHERE DimensionCount = %d AND DomainId = %lld",
            count, domainid);
        if (query2.nextRow())
        {
            if (!query2.currColumnNull())
            {
                intervals[count].set_low((r_Range)query2.nextColumnInt());
            }
            else
            {
                intervals[count].set_low(undefined);
                query2.nextColumn();
            }
            if (!query2.currColumnNull())
            {
                intervals[count].set_high((r_Range)query2.nextColumnInt());
            }
            else
            {
                intervals[count].set_high(undefined);
                query2.nextColumn();
            }
        }
        else
        {
            LERROR << "DBMinterval::readFromDb() - domain object: " << domainid
                   << " has no dimension " << count << " description in the database.";
            throw r_Ebase_dbms(SQLITE_NOTFOUND,
                "domain object has no dimension description in the database.");
        }

        streamInitCnt++;
    }

    DBObject::readFromDb();
}
