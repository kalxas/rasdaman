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

#include "config.h"
#include "reladminif/sqlerror.hh"
#include "reladminif/externs.h"
#include "dbminterval.hh"
#include "raslib/rmdebug.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/sqlitewrapper.hh"

DBMinterval::DBMinterval()
    :   r_Minterval(),
        DBObject()
{
    objecttype = OId::DBMINTERVALOID;
}

DBMinterval::DBMinterval(const OId& id) throw (r_Error)
    :   r_Minterval(),
        DBObject(id)
{
    objecttype = OId::DBMINTERVALOID;
    readFromDb();
}

DBMinterval::DBMinterval(r_Dimension dim)
    :   r_Minterval(dim),
        DBObject()
{
    objecttype = OId::DBMINTERVALOID;
}

DBMinterval::DBMinterval(const char* dom)
    :   r_Minterval((char*)dom),
        DBObject()
{
    objecttype = OId::DBMINTERVALOID;
}

DBMinterval::DBMinterval(const DBMinterval& old)
    :   r_Minterval(old),
        DBObject(old)
{
    objecttype = OId::DBMINTERVALOID;
}

DBMinterval::DBMinterval(const r_Minterval& old)
    :   r_Minterval(old),
        DBObject()
{
    objecttype = OId::DBMINTERVALOID;
}

DBMinterval::~DBMinterval()
{
    RMDBGENTER(4, RMDebug::module_catalogif, "DBMinterval", "~DBMinterval() " << myOId);
    validate();
    RMDBGEXIT(4, RMDebug::module_catalogif, "DBMinterval", "~DBMinterval() " << myOId);
}

DBMinterval&
DBMinterval::operator=(const DBMinterval& old)
{
    RMDBGENTER(11, RMDebug::module_catalogif, "DBMinterval", "operator=(" << old.getOId() << ") with me " << myOId);
    if (this == &old)
        return *this;
    r_Minterval::operator=(old);
    setModified();
    RMDBGEXIT(11, RMDebug::module_catalogif, "DBMinterval", "operator=(" << old.getOId() << ") with me " << myOId);
    return *this;
}

DBMinterval&
DBMinterval::operator=(const r_Minterval& old)
{
    if (this == &old)
        return *this;
    r_Minterval::operator=(old);
    setModified();
    return *this;
}

r_Bytes
DBMinterval::getMemorySize() const
{
    return DBObject::getMemorySize() + sizeof(r_Minterval) + dimensionality * (4 + 4 + 1 + 1);
}

void
DBMinterval::insertInDb() throw (r_Error)
{
    long long domainid;
    int     count;
    int     dimension;
    char    high[STRING_MAXLEN];
    char    low[STRING_MAXLEN];

    domainid = myOId.getCounter();
    dimension = dimensionality;

    SQLiteQuery::executeWithParams("INSERT INTO RAS_DOMAINS ( DomainId, Dimension) VALUES  ( %lld, %d)",
                                   domainid, dimension);

    for (count = 0; count < dimensionality; count++)
    {
        if (intervals[count].is_low_fixed())
        {
            sprintf(low, "%d", intervals[count].low());
        }
        else
        {
            strcpy(low, "NULL");
        }
        if (intervals[count].is_high_fixed())
        {
            sprintf(high, "%d", intervals[count].high());
        }
        else
        {
            strcpy(high, "NULL");
        }

        SQLiteQuery::executeWithParams("INSERT INTO RAS_DOMAINVALUES ( DomainId, DimensionCount, Low, High ) VALUES  ( %lld, %d, %s, %s)",
                                       domainid, count, low, high);
    }

    DBObject::insertInDb();
}

void
DBMinterval::updateInDb() throw (r_Error)
{
    long long domainid;
    int     count;
    int     dimension;
    char    high[STRING_MAXLEN];
    char    low[STRING_MAXLEN];

    domainid = myOId.getCounter();

    SQLiteQuery query("SELECT Dimension FROM RAS_DOMAINS WHERE DomainId = %lld", domainid);
    if (query.nextRow())
    {
        dimension = query.nextColumnInt();
    }
    else
    {
        RMInit::logOut << "DBMinterval::updateInDb() - domain object: "
                << domainid << " not found in the database." << endl;
        throw r_Ebase_dbms( SQLITE_NOTFOUND, "domain object not found in the database." );
    }

    if (dimension < dimensionality)
    {
        //insert more rows in RAS_DOMAINVALUES
        for (count = dimension; count < dimensionality; count++)
        {
            SQLiteQuery::executeWithParams("INSERT INTO RAS_DOMAINVALUES ( DomainId, DimensionCount) VALUES  ( %lld, %d )",
                                           domainid, count);
        }
        dimension = dimensionality;
        SQLiteQuery::executeWithParams("UPDATE RAS_DOMAINS SET Dimension = %d WHERE DomainId = %lld",
                                       dimension, domainid);
    }
    else
    {
        if (dimension > dimensionality)
        {
            //delete superfluous dimensions
            for (count = dimension; count > dimensionality; count--)
            {
                SQLiteQuery::executeWithParams("DELETE FROM RAS_DOMAINVALUES WHERE DomainId = %lld AND DimensionCount = %d",
                                               domainid, count);
            }
            dimension = dimensionality;
            SQLiteQuery::executeWithParams("UPDATE RAS_DOMAINS SET Dimension = %d WHERE DomainId = %lld",
                                           dimension, domainid);
        }
        else
        {
            //only update dimension boundaries
        }
    }

    for (count = 0; count < dimensionality; count++)
    {
        if (intervals[count].is_low_fixed())
        {
            sprintf(low, "%d", intervals[count].low());
        }
        else
        {
            strcpy(low, "NULL");
        }
        if (intervals[count].is_high_fixed())
        {
            sprintf(high, "%d", intervals[count].high());
        }
        else
        {
            strcpy(high, "NULL");
        }

        SQLiteQuery::executeWithParams("UPDATE RAS_DOMAINVALUES SET Low = %s, High = %s WHERE DomainId = %lld AND DimensionCount = %d",
                                       low, high, domainid, count);
    }

    DBObject::updateInDb();
}

void
DBMinterval::deleteFromDb() throw (r_Error)
{
    long long domainid = myOId.getCounter();
    SQLiteQuery::executeWithParams("DELETE FROM RAS_DOMAINS WHERE DomainId = %lld", domainid);
    SQLiteQuery::executeWithParams("DELETE FROM RAS_DOMAINVALUES WHERE DomainId = %lld", domainid);
    DBObject::deleteFromDb();
}

void
DBMinterval::readFromDb() throw (r_Error)
{
    char undefined = '*';
    long long domainid;
    long    count;
    long    low;
    short   lowind;
    long    high;
    short   highind;
    long    dimension;

    domainid = myOId.getCounter();

    SQLiteQuery query("SELECT Dimension FROM RAS_DOMAINS WHERE DomainId = %lld", domainid);
    if (query.nextRow())
    {
        dimension = query.nextColumnInt();
    }
    else
    {
        RMInit::logOut << "DBMinterval::readFromDb() - domain object: "
                << domainid << " not found in the database.." << endl;
        throw r_Ebase_dbms( SQLITE_NOTFOUND, "domain object not found in the database." );
    }

    dimensionality = dimension;
    delete[] intervals;
    intervals = new r_Sinterval[dimensionality];
    streamInitCnt = 0;

    for (count = 0; count < dimension; count++)
    {
        SQLiteQuery query("SELECT Low, High FROM RAS_DOMAINVALUES WHERE DimensionCount = %d AND DomainId = %lld",
                          count, domainid);
        if (query.nextRow())
        {
            if (!query.currColumnNull())
            {
                intervals[count].set_low((r_Range)query.nextColumnInt());
            }
            else
            {
                intervals[count].set_low(undefined);
                query.nextColumn();
            }
            if (!query.currColumnNull())
            {
                intervals[count].set_high((r_Range)query.nextColumnInt());
            }
            else
            {
                intervals[count].set_high(undefined);
                query.nextColumn();
            }
        }
        else
        {
            RMInit::logOut << "DBMinterval::readFromDb() - domain object: "
                    << domainid << " has no dimension " << count << " description in the database." << endl;
            throw r_Ebase_dbms( SQLITE_NOTFOUND, "domain object has no dimension description in the database." );
        }

        streamInitCnt++;
    }

    DBObject::readFromDb();
}
