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

#include "basetype.hh"
#include "mdddimensiontype.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/sqlitewrapper.hh"
#include <logging.hh>

void MDDDimensionType::insertInDb()
{
    long long basetypeid = getBaseType()->getOId();
    SQLiteQuery::executeWithParams(
        "INSERT INTO RAS_MDDDIMTYPES ( MDDDimTypeOId, MDDTypeName, BaseTypeId, "
        "Dimension) VALUES (%lld, '%s', %lld, %d)",
        myOId.getCounter(), getName(), basetypeid, myDimension);
    DBObject::insertInDb();
}

void MDDDimensionType::readFromDb()
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif

    SQLiteQuery query(
        "SELECT Dimension, BaseTypeId, MDDTypeName FROM RAS_MDDDIMTYPES WHERE MDDDimTypeOId = %lld",
        myOId.getCounter());
    if (query.nextRow())
    {
        myDimension = static_cast<r_Dimension>(query.nextColumnInt());
        auto basetypeid = query.nextColumnLong();
        setName(query.nextColumnString());
        query.finalize();
        myBaseType = static_cast<BaseType *>(ObjectBroker::getObjectByOId(OId(basetypeid)));
    }
    else
    {
        LERROR << "mdd dimension type " << myOId.getCounter() << " not found in RAS_MDDDIMTYPES.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "mdd type object not found in RAS_MDDDIMTYPES.");
    }

    DBObject::readFromDb();
#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif
}

void MDDDimensionType::deleteFromDb()
{
    SQLiteQuery::executeWithParams(
        "DELETE FROM RAS_MDDDIMTYPES WHERE MDDDimTypeOId = %lld", myOId.getCounter());
    DBObject::deleteFromDb();
}
