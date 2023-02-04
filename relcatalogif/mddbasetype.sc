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

#include "mddbasetype.hh"
#include "basetype.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlitewrapper.hh"
#include "reladminif/sqlglobals.h"
#include <logging.hh>
#include <fmt/core.h>

void MDDBaseType::insertInDb()
{
    long long basetypeid = getBaseType()->getOId();
    SQLiteQuery::execute(fmt::format(
        "INSERT INTO RAS_MDDBASETYPES ( MDDBaseTypeOId, MDDTypeName, BaseTypeId) "
        "VALUES ({}, '{}', {})", myOId.getCounter(), getName(), basetypeid));
    DBObject::insertInDb();
}

void MDDBaseType::readFromDb()
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif

    SQLiteQuery query(fmt::format(
        "SELECT BaseTypeId, MDDTypeName FROM RAS_MDDBASETYPES WHERE MDDBaseTypeOId = {}",
        myOId.getCounter()));
    if (query.nextRow())
    {
        auto basetypeid = query.nextColumnLong();
        setName(query.nextColumnString());
        query.finalize();
        myBaseType = static_cast<BaseType *>(ObjectBroker::getObjectByOId(OId(basetypeid)));
    }
    else
    {
        LERROR << "mdd base type " << myOId.getCounter() << " not found in RAS_MDDBASETYPES.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "mdd type object not found in RAS_MDDBASETYPES.");
    }
    DBObject::readFromDb();

#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif
}

void MDDBaseType::deleteFromDb()
{
    SQLiteQuery::execute(fmt::format(
        "DELETE FROM RAS_MDDBASETYPES WHERE MDDBaseTypeOId = {}", myOId.getCounter()));
    DBObject::deleteFromDb();
}
