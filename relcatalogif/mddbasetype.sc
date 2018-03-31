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
/*************************************************************
 *
 *
 * PURPOSE:
 *   Code with embedded SQL for PostgreSQL DBMS
 *
 *
 * COMMENTS:
 *   uses embedded SQL
 *
 ************************************************************/

#include "config.h"
#include "debug-srv.hh"
#include "mddbasetype.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/externs.h"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/sqlitewrapper.hh"
#include <logging.hh>

void
MDDBaseType::insertInDb() throw (r_Error)
{
    long long mddtypeid;
    long long mddbasetypeid;
    char mddtypename[STRING_MAXLEN];

    (void) strncpy(mddtypename, const_cast<char*>(getName()), (size_t) sizeof(mddtypename));
    DBObject* obj = (DBObject*)const_cast<BaseType*>(getBaseType());
    mddbasetypeid = obj->getOId();
    mddtypeid = myOId.getCounter();

    SQLiteQuery::executeWithParams("INSERT INTO RAS_MDDBASETYPES ( MDDBaseTypeOId, MDDTypeName, BaseTypeId) VALUES (%lld, '%s', %lld)",
                                   mddtypeid, mddtypename, mddbasetypeid);
    DBObject::insertInDb();
}

void
MDDBaseType::readFromDb() throw (r_Error)
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif
    long long mddtypeid;
    long long mddbasetypeid;
    char* mddtypename;

    mddtypeid = myOId.getCounter();
    mddbasetypeid = 0;

    SQLiteQuery query("SELECT BaseTypeId, MDDTypeName FROM RAS_MDDBASETYPES WHERE MDDBaseTypeOId = %lld",
                      mddtypeid);
    if (query.nextRow())
    {
        mddbasetypeid = query.nextColumnLong();
        mddtypename = query.nextColumnString();
    }
    else
    {
        LFATAL << "MDDBaseType::readFromDb() - mdd type: "
               << mddtypeid << " not found in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "mdd type object not found in the database.");
    }

    setName(mddtypename);
    myBaseType = (BaseType*) ObjectBroker::getObjectByOId(OId(mddbasetypeid));
#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif
    DBObject::readFromDb();
}

void
MDDBaseType::deleteFromDb() throw (r_Error)
{
    long long mddtypeid = myOId.getCounter();
    SQLiteQuery::executeWithParams("DELETE FROM RAS_MDDBASETYPES WHERE MDDBaseTypeOId = %lld",
                                   mddtypeid);
    DBObject::deleteFromDb();
}
