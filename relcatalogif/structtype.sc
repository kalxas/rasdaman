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

#include "structtype.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlitewrapper.hh"
#include <logging.hh>

void StructType::insertInDb()
{
    long long structtypeid;
    char structtypename[VARCHAR_MAXLEN];
    long long elementtype;
    char elementname[VARCHAR_MAXLEN];
    unsigned int count;

    (void)strncpy(structtypename, const_cast<char *>(getTypeName()),
                  (size_t)sizeof(structtypename));
    structtypeid = myOId.getCounter();

    SQLiteQuery::executeWithParams(
        "INSERT INTO RAS_BASETYPENAMES (BaseTypeId, BaseTypeName) VALUES (%lld, '%s')",
        structtypeid, structtypename);
    for (count = 0; count < getNumElems(); count++)
    {
        (void)strncpy(elementname, const_cast<char *>(getElemName(count)),
                      (size_t)sizeof(elementname));
        // should not be necessary because of TypeFactory::addType()
        DBObject *obj = (DBObject *)const_cast<BaseType *>(getElemType(count));

        elementtype = obj->getOId();
        LTRACE << "element " << count << ". id\t:" << elementtype;

        SQLiteQuery::executeWithParams(
            "INSERT INTO RAS_BASETYPES (BaseTypeId, ContentType, Count, "
            "ContentTypeName) VALUES (%lld, %lld, %d, '%s')",
            structtypeid, elementtype, count, elementname);
    }
    DBObject::insertInDb();
}

void StructType::readFromDb()
{
    short count = 0;

    long long basetypeid;
    long long elementtypeid;
    const char *basetypename;

    basetypeid = myOId.getCounter();

    SQLiteQuery query(
        "SELECT BaseTypeName FROM RAS_BASETYPENAMES WHERE BaseTypeId = %lld",
        basetypeid);
    if (query.nextRow())
    {
        basetypename = query.nextColumnString();
    }
    else
    {
        LERROR << "StructType::readFromDb() - base type: " << basetypeid
               << " not found in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND,
                           "base type object not found in the database.");
    }
    setName(basetypename);

    SQLiteQuery query2(
        "SELECT ContentTypeName, ContentType, Count FROM RAS_BASETYPES WHERE "
        "BaseTypeId = %lld ORDER BY Count",
        basetypeid);
    while (query2.nextRow())
    {
        const char *elementname = query2.nextColumnString();
        elementtypeid = query2.nextColumnLong();

        LTRACE << count << ". contenttypeid is " << elementtypeid
               << " elementname is " << elementname;
        addElementPriv(elementname, (BaseType *)ObjectBroker::getObjectByOId(
                           OId(elementtypeid)));
        count++;
    }
    DBObject::readFromDb();
}

void StructType::deleteFromDb()
{
    long long basetypeid = myOId.getCounter();
    SQLiteQuery::executeWithParams(
        "DELETE FROM RAS_BASETYPENAMES WHERE BaseTypeId = %lld", basetypeid);
    SQLiteQuery::executeWithParams(
        "DELETE FROM RAS_BASETYPES WHERE BaseTypeId = %lld", basetypeid);
    DBObject::deleteFromDb();
}
