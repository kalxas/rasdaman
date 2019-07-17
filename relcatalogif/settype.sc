// This is -*- C++ -*-

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
#include "settype.hh"
#include "mddtype.hh"
#include "dbnullvalues.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/sqlitewrapper.hh"
#include <logging.hh>

void SetType::insertInDb()
{
    long long mddtypeid = getMDDType()->getOId();
    SQLiteQuery::executeWithParams(
        "INSERT INTO RAS_SETTYPES ( SetTypeId, SetTypeName, MDDTypeOId) VALUES (%lld, '%s', %lld)",
        myOId.getCounter(), getTypeName(), mddtypeid);
    if (nullValues != NULL)
    {
        nullValues->setPersistent(true);
        SQLiteQuery::executeWithParams(
            "INSERT INTO RAS_NULLVALUES ( SetTypeOId, NullValueOId) VALUES (%lld, %lld)",
            myOId.getCounter(), nullValues->getOId().getCounter());
    }
    DBObject::insertInDb();
}

void SetType::deleteFromDb()
{
    SQLiteQuery::executeWithParams(
        "DELETE FROM RAS_SETTYPES WHERE SetTypeId = %lld", myOId.getCounter());
    SQLiteQuery::executeWithParams(
        "DELETE FROM RAS_NULLVALUES WHERE SetTypeOId = %lld", myOId.getCounter());
    if (nullValues != NULL)
    {
        nullValues->setPersistent(false);
        nullValues->setCached(false);
    }
    DBObject::deleteFromDb();
}

void SetType::readFromDb()
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif

    SQLiteQuery query(
        "SELECT SetTypeName, MDDTypeOId FROM RAS_SETTYPES WHERE SetTypeId = %lld", myOId.getCounter());
    if (query.nextRow())
    {
        setName(query.nextColumnString());
        myType = SETTYPE;
        auto mddtypeid = query.nextColumnLong();
        query.finalize();

        myMDDType = static_cast<MDDType *>(ObjectBroker::getObjectByOId(OId(mddtypeid)));
    }
    else
    {
        LERROR << "set type " << myOId.getCounter() << " not found in RAS_SETTYPES.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "set type object not found in RAS_SETTYPES.");
    }

    SQLiteQuery queryNull(
        "SELECT NullValueOId FROM RAS_NULLVALUES WHERE SetTypeOId = %lld", myOId.getCounter());
    if (queryNull.nextRow())
    {
        auto nullvalueoid = OId(queryNull.nextColumnLong(), OId::DBNULLVALUESOID);
        queryNull.finalize();
        
        nullValues = static_cast<DBNullvalues *>(ObjectBroker::getObjectByOId(nullvalueoid));
        nullValues->setCached(true);
        LDEBUG << "Got null values: " << nullValues->toString();
    }
    else
    {
        LDEBUG << "No null values found.";
    }
    DBObject::readFromDb();

#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif
}
