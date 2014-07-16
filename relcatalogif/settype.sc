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
#include "raslib/rmdebug.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlitewrapper.hh"

void
SetType::insertInDb() throw (r_Error)
{
    RMDBGENTER(5, RMDebug::module_catalogif, "SetType", "insertInDb() " << myOId << " " << getTypeName());
    ENTER("SetType::insertInDb()")

            long long mddtypeid;
    char settypename[VARCHAR_MAXLEN];
    long long settypeid;
    long long nullvalueoid;

    (void) strncpy(settypename, (char*) getTypeName(), (size_t) sizeof (settypename));
    settypeid = myOId.getCounter();
    mddtypeid = getMDDType()->getOId();

    SQLiteQuery::executeWithParams("INSERT INTO RAS_SETTYPES ( SetTypeId, SetTypeName, MDDTypeOId) VALUES (%lld, '%s', %lld)",
                                   settypeid, settypename, mddtypeid);
    DBObject::insertInDb();

    LEAVE("SetType::insertInDb()");
    RMDBGEXIT(5, RMDebug::module_catalogif, "SetType", "insertInDb() " << myOId);
}

void
SetType::deleteFromDb() throw (r_Error)
{
    RMDBGENTER(5, RMDebug::module_catalogif, "SetType", "deleteFromDb() " << myOId << " " << getTypeName());
    long long settypeid = myOId.getCounter();

    SQLiteQuery::executeWithParams("DELETE FROM RAS_SETTYPES WHERE SetTypeId = %lld",
                                   settypeid);

    DBObject::deleteFromDb();
    RMDBGEXIT(5, RMDebug::module_catalogif, "SetType", "deleteFromDb() " << myOId);
}

void
SetType::readFromDb() throw (r_Error)
{
    RMDBGENTER(5, RMDebug::module_catalogif, "SetType", "readFromDb() " << myOId);
    ENTER("SetType::readFromDb()");
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
        RMInit::logOut << "SetType::readFromDb() - set type: "
                << settypeid << " not found in the database." << endl;
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "set type object not found in the database.");
    }
    setName(settypename);
    myType = SETTYPE;
    myMDDType = (MDDType*) ObjectBroker::getObjectByOId(OId(mddtypeid));

    DBObject::readFromDb();

#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif

    LEAVE("SetType::readFromDb()");
    RMDBGEXIT(5, RMDebug::module_catalogif, "SetType", "readFromDb() " << myOId);
}
