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
 * Code with embedded SQL for PostgreSQL DBMS
 *
 *
 * COMMENTS:
 *   uses embedded SQL
 *
 ************************************************************/

#include "config.h"
#include "debug-srv.hh"

#include <cstdlib>
#include "dbmddset.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/sqlitewrapper.hh"
#include "relcatalogif/collectiontype.hh"
#include "../common/src/logging/easylogging++.hh"

DBMDDSet::DBMDDSet(const char* name, const OId& id, const CollectionType* type) throw (r_Error)
: DBNamedObject(id, name),
collType(type)
{
    if (name == NULL)
        setName("unnamed collection");
    if (type == NULL)
    {
        LTRACE << "DBMDDSet(" << name << ", NULL)";
        throw r_Error(r_Error::r_Error_General);
    }
    if (!type->isPersistent())
    {
        r_Error t(RASTYPEUNKNOWN);
        t.setTextParameter("type", type->getName());
        LTRACE << "DBMDDSet(" << name << ", " << type->getName() << " not persistent)";
        throw t;
    }
    DBMDDSet* set = NULL;
    try
    {
        set = (DBMDDSet*) ObjectBroker::getObjectByName(OId::MDDCOLLOID, getName());
    }
    catch (r_Error& err)
    {
        if (err.get_kind() == r_Error::r_Error_ObjectUnknown)
            set = NULL;
        else
            throw;
    }
    if (set)
    {
        LTRACE << "already have a set with name " << getName();
        LFATAL << "DBMDDSet::DBMDDSet() mdd collection with name \"" << getName() << "\" exists already";
        throw r_Error(r_Error::r_Error_NameNotUnique);
    }
    long testoid1;

    testoid1 = id.getCounter();

    // (1) --- fetch tuple from database
    SQLiteQuery query("SELECT MDDCollId FROM RAS_MDDCOLLNAMES WHERE MDDCollId = %lld", testoid1);
    if (query.nextRow())
    {
        LFATAL << "DBMDDObj::DBMDDObj() - mdd object: "
                << testoid1 << " already exists in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "mdd object already exists in the database.");
    }
    else
    {
        _isInDatabase = false;
        _isPersistent = true;
        _isModified = true;
        objecttype = OId::MDDCOLLOID;
        myOId = id;
        ObjectBroker::registerDBObject(this);
    }
}

void
DBMDDSet::insertInDb() throw (r_Error)
{
    long long mddoid;
    long long mddcolloid;
    long long colltypeoid;

    mddcolloid = myOId.getCounter();
    colltypeoid = collType->getOId().getCounter();

    SQLiteQuery::executeWithParams("INSERT INTO RAS_MDDCOLLNAMES ( MDDCollName, MDDCollId, SetTypeId) VALUES ('%s',%lld,%lld)",
                                   getName(), mddcolloid, colltypeoid);
    for (DBMDDObjIdSet::iterator i = mySet.begin(); i != mySet.end(); i++)
    {
        mddoid = (*i).getOId().getCounter();
        LTRACE << "mddobject with id " << mddoid;

        SQLiteQuery::executeWithParams("INSERT INTO RAS_MDDCOLLECTIONS ( MDDId, MDDCollId) VALUES (%lld,%lld)",
                                       mddoid, mddcolloid);
        LTRACE << "wrote mddobjoid\t: " << (*i).getOId();
    }
    DBObject::insertInDb();
}

void
DBMDDSet::deleteFromDb() throw (r_Error)
{
    long long mddcolloid1 = myOId.getCounter();
    SQLiteQuery::executeWithParams("DELETE FROM RAS_MDDCOLLNAMES WHERE MDDCollId = %lld", mddcolloid1);
    SQLiteQuery::executeWithParams("DELETE FROM RAS_MDDCOLLECTIONS WHERE MDDCollId = %lld", mddcolloid1);
    DBObject::deleteFromDb();
}

void
DBMDDSet::readFromDb() throw (r_Error)
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif
    long long mddoid2;
    long long mddcolloid2;
    long long colltypeoid2;
    char *collname2 = NULL;

    mddcolloid2 = myOId.getCounter();


    SQLiteQuery query("SELECT MDDCollName, SetTypeId FROM RAS_MDDCOLLNAMES WHERE MDDCollId = %lld", mddcolloid2);
    if (query.nextRow())
    {
        collname2 = strdup(query.nextColumnString());
        colltypeoid2 = query.nextColumnLong();
    }
    else
    {
        LFATAL << "DBMDDSet::readFromDb() - set object: "
                << mddcolloid2 << " not found in the database.";
        if (collname2)
        {
            free(collname2);
            collname2 = NULL;
        }
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "set object not found in the database.");
    }

    setName(collname2);
    if (collname2)
    {
        free(collname2);
        collname2 = NULL;
    }
    collType = (const CollectionType*) ObjectBroker::getObjectByOId(OId(colltypeoid2, OId::SETTYPEOID));

    SQLiteQuery cquery("SELECT MDDId FROM RAS_MDDCOLLECTIONS WHERE MDDCollId = %lld ORDER BY MDDId", mddcolloid2);
    while (cquery.nextRow())
    {
        mddoid2 = cquery.nextColumnLong();
        mySet.insert(OId(mddoid2, OId::MDDOID));
    }

    DBObject::readFromDb();
#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif
}
