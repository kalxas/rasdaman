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

#include "structtype.hh"
#include "basetype.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlitewrapper.hh"
#include <logging.hh>
#include <fmt/core.h>

void StructType::insertInDb()
{
    SQLiteQuery::execute(fmt::format(
        "INSERT INTO RAS_BASETYPENAMES (BaseTypeId, BaseTypeName) VALUES ({}, '{}')",
        myOId.getCounter(), getTypeName()));
    for (unsigned int count = 0; count < getNumElems(); count++)
    {
        long long elemtypeid = getElemType(count)->getOId();
        const char* elemname = getElemName(count);
        SQLiteQuery::execute(fmt::format(
            "INSERT INTO RAS_BASETYPES (BaseTypeId, ContentType, Count, ContentTypeName) "
            "VALUES ({}, {}, {}, '{}')",
            myOId.getCounter(), elemtypeid, count, elemname));
    }
    DBObject::insertInDb();
}

void StructType::readFromDb()
{
    {
        SQLiteQuery query(fmt::format(
            "SELECT BaseTypeName FROM RAS_BASETYPENAMES WHERE BaseTypeId = {}", myOId.getCounter()));
        if (query.nextRow())
        {
            setName(query.nextColumnString());
        }
        else
        {
            LERROR << "struct type " << myOId.getCounter() << " not found in RAS_BASETYPENAMES.";
            throw r_Ebase_dbms(SQLITE_NOTFOUND, "struct type object not found in RAS_BASETYPENAMES.");
        }
    }
    {
        SQLiteQuery query2(fmt::format(
            "SELECT ContentTypeName, ContentType FROM RAS_BASETYPES WHERE "
            "BaseTypeId = {} ORDER BY Count", myOId.getCounter()));
        while (query2.nextRow())
        {
            const char *elementname = query2.nextColumnString();
            const auto elemtypeid = query2.nextColumnLong();
            
            auto *et = static_cast<BaseType *>(ObjectBroker::getObjectByOId(OId(elemtypeid)));
            addElementPriv(elementname, et);
        }
    }
    DBObject::readFromDb();
}

void StructType::deleteFromDb()
{
    SQLiteQuery::execute(fmt::format(
        "DELETE FROM RAS_BASETYPENAMES WHERE BaseTypeId = {}", myOId.getCounter()));
    SQLiteQuery::execute(fmt::format(
        "DELETE FROM RAS_BASETYPES WHERE BaseTypeId = {}", myOId.getCounter()));
    DBObject::deleteFromDb();
}
