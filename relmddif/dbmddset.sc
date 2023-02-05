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

#include "dbmddset.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/sqlitewrapper.hh"
#include "relcatalogif/collectiontype.hh"
#include <logging.hh>
#include <cstdlib>
#include <fmt/core.h>

DBMDDSet::DBMDDSet(const char *name, const OId &id, const CollectionType *type)
    : DBNamedObject(id, name), collType(type)
{
    if (name == NULL)
    {
        setName("unnamed collection");
    }
    if (type == NULL)
    {
        LERROR << "Creating an MDD collection object " << name << " with null type.";
        throw r_Error(COLLECTIONTYPEISNULL);
    }
    if (!type->isPersistent())
    {
        r_Error t(RASTYPEUNKNOWN);
        t.setTextParameter("type", type->getName());
        LERROR << "Creating an MDD collection object " << name << " with non-persistent type " << type->getName();
        throw t;
    }
    DBMDDSet *set = NULL;
    try
    {
        set = static_cast<DBMDDSet *>(ObjectBroker::getObjectByName(OId::MDDCOLLOID, getName()));
    }
    catch (r_Error &err)
    {
        if (err.get_kind() != r_Error::r_Error_ObjectUnknown)
            throw;
    }
    if (set)
    {
        LERROR << "MDD collection with name " << getName() << " exists already";
        throw r_Error(r_Error::r_Error_NameNotUnique);
    }

    SQLiteQuery query(fmt::format("SELECT MDDCollId FROM RAS_MDDCOLLNAMES WHERE MDDCollId = {}", id.getCounter()));
    if (query.nextRow())
    {
        LERROR << "MDD collection with id " << id.getCounter() << " already exists in RAS_MDDCOLLNAMES.";
        throw r_Error(r_Error::r_Error_NameNotUnique);
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

void DBMDDSet::insertInDb()
{
    SQLiteQuery::execute(fmt::format(
        "INSERT INTO RAS_MDDCOLLNAMES ( MDDCollName, MDDCollId, SetTypeId) VALUES ('{}',{},{})",
        getName(), myOId.getCounter(), collType->getOId().getCounter()));

    for (auto i = mySet.begin(); i != mySet.end(); i++)
    {
        SQLiteQuery::execute(fmt::format(
            "INSERT INTO RAS_MDDCOLLECTIONS ( MDDId, MDDCollId) VALUES ({},{})",
            (*i).getOId().getCounter(), myOId.getCounter()));
    }
    DBObject::insertInDb();
}

void DBMDDSet::deleteFromDb()
{
    SQLiteQuery::execute(fmt::format("DELETE FROM RAS_MDDCOLLNAMES WHERE MDDCollId = {}", myOId.getCounter()));
    SQLiteQuery::execute(fmt::format("DELETE FROM RAS_MDDCOLLECTIONS WHERE MDDCollId = {}", myOId.getCounter()));
    DBObject::deleteFromDb();
}

void DBMDDSet::readFromDb()
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif
    long long mddoid2;

    SQLiteQuery query(fmt::format("SELECT MDDCollName, SetTypeId FROM RAS_MDDCOLLNAMES "
                                  "WHERE MDDCollId = {}", myOId.getCounter()));
    if (query.nextRow())
    {
        setName(query.nextColumnString());
        collType = static_cast<CollectionType *>(
            ObjectBroker::getObjectByOId(OId(query.nextColumnLong(), OId::SETTYPEOID)));
    }
    else
    {
        LERROR << "MDD collection object " << myOId.getCounter() << " not found in RAS_MDDCOLLNAMES";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "MDD collection object not found in RAS_MDDCOLLNAMES.");
    }

    SQLiteQuery cquery(fmt::format("SELECT MDDId FROM RAS_MDDCOLLECTIONS "
                                   "WHERE MDDCollId = {} ORDER BY MDDId", myOId.getCounter()));
    while (cquery.nextRow())
    {
        mySet.insert(DBMDDObjId(OId(cquery.nextColumnLong(), OId::MDDOID)));
    }
    DBObject::readFromDb();
#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif
}

DBMDDSetId DBMDDSet::getDBMDDSetContainingDBMDDObj(const OId &id)
{
  SQLiteQuery query(fmt::format("SELECT MDDCollId FROM RAS_MDDCOLLECTIONS "
                                "WHERE MDDId = {}", id.getCounter()));
  if (query.nextRow())
  {
      auto setId = query.nextColumnLong();
      OId setOId{setId, OId::MDDCOLLOID};
      return DBMDDSetId(static_cast<DBMDDSet *>(ObjectBroker::getObjectByOId(setOId)));
  }
  else
  {
      LERROR << "MDD collection containing MDD with oid " << id.getCounter() << " not found in RAS_MDDCOLLECTIONS";
      throw r_Ebase_dbms(SQLITE_NOTFOUND, "MDD collection object not found in RAS_MDDCOLLECTIONS.");
  }
}
