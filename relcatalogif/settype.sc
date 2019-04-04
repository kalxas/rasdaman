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

#include "settype.hh"
#include "mddtype.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlitewrapper.hh"
#include <logging.hh>

void
SetType::insertInDb()
{
    long long mddtypeid;
    char settypename[VARCHAR_MAXLEN];
    long long settypeid;
    long long nullvalueoid;

    (void) strncpy(settypename, const_cast<char *>(getTypeName()), (size_t) sizeof(settypename));
    settypeid = myOId.getCounter();
    mddtypeid = getMDDType()->getOId();

    SQLiteQuery::executeWithParams("INSERT INTO RAS_SETTYPES ( SetTypeId, SetTypeName, MDDTypeOId) VALUES (%lld, '%s', %lld)",
                                   settypeid, settypename, mddtypeid);
    if (nullValues != NULL)
    {
        nullValues->setPersistent(true);
        nullvalueoid = nullValues->getOId().getCounter();
        SQLiteQuery::executeWithParams("INSERT INTO RAS_NULLVALUES ( SetTypeOId, NullValueOId) VALUES (%lld, %lld)",
                                       settypeid, nullvalueoid);
    }
    DBObject::insertInDb();
}

void
SetType::deleteFromDb()
{
    long long settypeid = myOId.getCounter();

    SQLiteQuery::executeWithParams("DELETE FROM RAS_SETTYPES WHERE SetTypeId = %lld",
                                   settypeid);
    SQLiteQuery::executeWithParams("DELETE FROM RAS_NULLVALUES WHERE SetTypeOId = %lld",
                                   settypeid);
    if (nullValues != NULL)
    {
        nullValues->setPersistent(false);
        nullValues->setCached(false);
    }

    DBObject::deleteFromDb();
}

void
SetType::readFromDb()
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif
    long long mddtypeid;
    char *settypename;
    long long settypeid;
    long long nullvalueoid;

    settypeid = myOId.getCounter();
    mddtypeid = 0;

    SQLiteQuery query("SELECT SetTypeName, MDDTypeOId FROM RAS_SETTYPES WHERE SetTypeId = %lld",
                      settypeid);
    if (query.nextRow())
    {
        settypename = query.nextColumnString();
        mddtypeid = query.nextColumnLong();
    }
    else
    {
        LERROR << "SetType::readFromDb() - set type: "
               << settypeid << " not found in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "set type object not found in the database.");
    }
    setName(settypename);
    myType = SETTYPE;
    myMDDType = (MDDType *) ObjectBroker::getObjectByOId(OId(mddtypeid));

    SQLiteQuery queryNull("SELECT NullValueOId FROM RAS_NULLVALUES WHERE SetTypeOId = %lld",
                          settypeid);
    if (queryNull.nextRow())
    {
        nullvalueoid = queryNull.nextColumnLong();
        nullValues = (DBNullvalues *) ObjectBroker::getObjectByOId(OId(nullvalueoid, OId::DBNULLVALUESOID));
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
