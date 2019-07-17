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
#include "mdddomaintype.hh"
#include "dbminterval.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/sqlitewrapper.hh"
#include <logging.hh>
#include <cstring>

void MDDDomainType::insertInDb()
{
    const auto domainid = myDomain->getOId().getCounter();
    long long basetypeid = getBaseType()->getOId();
    SQLiteQuery::executeWithParams(
        "INSERT INTO RAS_MDDDOMTYPES ( MDDDomTypeOId, MDDTypeName, BaseTypeId, DomainId ) "
        "VALUES (%lld, '%s', %lld, %lld)", myOId.getCounter(), getName(), basetypeid, domainid);
    DBObject::insertInDb();
}

void MDDDomainType::readFromDb()
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif

    SQLiteQuery query(
        "SELECT BaseTypeId, MDDTypeName, DomainId FROM RAS_MDDDOMTYPES WHERE MDDDomTypeOId = %lld",
        myOId.getCounter());
    if (query.nextRow())
    {
        auto basetypeid = query.nextColumnLong();
        setName(query.nextColumnString());
        auto domainid = query.nextColumnLong();
        query.finalize();

        myBaseType = static_cast<BaseType *>(ObjectBroker::getObjectByOId(OId(basetypeid)));
        myDomain = static_cast<DBMinterval *>(ObjectBroker::getObjectByOId(OId(domainid, OId::DBMINTERVALOID)));
        myDomain->setCached(true);
    }
    else
    {
        LERROR << "mdd domain type " << myOId.getCounter() << " not found in RAS_MDDDOMTYPES.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "mdd type object not found in RAS_MDDDOMTYPES.");
    }
    DBObject::readFromDb();
#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif
}

void MDDDomainType::deleteFromDb()
{
    SQLiteQuery::executeWithParams(
        "DELETE FROM RAS_MDDDOMTYPES WHERE MDDDomTypeOId = %lld", myOId.getCounter());
    myDomain->setPersistent(false);
    myDomain->setCached(false);
    DBObject::deleteFromDb();
}
