#include "config.h"
#include "mymalloc/mymalloc.h"
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

#include "debug-srv.hh"
#include "structtype.hh"
#include "raslib/rmdebug.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/externs.h"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/sqlitewrapper.hh"

void
StructType::insertInDb() throw (r_Error)
{
    RMDBGENTER(5, RMDebug::module_catalogif, "StructType", "insertInDb() " << myOId);
    long long structtypeid;
    char structtypename[VARCHAR_MAXLEN];
    long long elementtype;
    char elementname[VARCHAR_MAXLEN];
    short count;

    (void) strncpy(structtypename, (char*) getTypeName(), (size_t) sizeof (structtypename));
    structtypeid = myOId.getCounter();

    SQLiteQuery::executeWithParams("INSERT INTO RAS_BASETYPENAMES (BaseTypeId, BaseTypeName) VALUES (%lld, '%s')",
                                   structtypeid, structtypename);
    for (count = 0; count < getNumElems(); count++)
    {
        (void) strncpy(elementname, (char*) getElemName(count), (size_t) sizeof (elementname));
        //should not be necessary because of TypeFactory::addType()
        DBObject* obj = (DBObject*) getElemType(count);

        elementtype = obj->getOId();
        RMDBGMIDDLE(6, RMDebug::module_catalogif, "StructType", "element " << count << ". id\t:" << elementtype);

        SQLiteQuery::executeWithParams("INSERT INTO RAS_BASETYPES (BaseTypeId, ContentType, Count, ContentTypeName) VALUES (%lld, %lld, %d, '%s')",
                                       structtypeid, elementtype, count, elementname);
    }
    DBObject::insertInDb();
    RMDBGEXIT(5, RMDebug::module_catalogif, "StructType", "insertInDb()");
}

void
StructType::readFromDb() throw (r_Error)
{
    RMDBGENTER(5, RMDebug::module_catalogif, "StructType", "readFromDb() " << myOId);
    short count = 0;

    long long basetypeid;
    long long elementtypeid;
    char *basetypename;
    char *elementname;
    int count1; // added PB 2005­jan-09

    basetypeid = myOId.getCounter();

    SQLiteQuery query("SELECT BaseTypeName FROM RAS_BASETYPENAMES WHERE BaseTypeId = %lld",
                      basetypeid);
    if (query.nextRow())
    {
        basetypename = query.nextColumnString();
    }
    else
    {
        RMInit::logOut << "StructType::readFromDb() - base type: "
                << basetypeid << " not found in the database." << endl;
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "base type object not found in the database.");
    }
    setName(basetypename);

    SQLiteQuery query2("SELECT ContentTypeName, ContentType, Count FROM RAS_BASETYPES WHERE BaseTypeId = %lld ORDER BY Count",
                       basetypeid);
    while (query2.nextRow())
    {
        elementname = query2.nextColumnString();
        elementtypeid = query2.nextColumnLong();
        count1 = query2.nextColumnInt();

        RMDBGMIDDLE(7, RMDebug::module_catalogif, "StructType", count << ". contenttypeid is " << elementtypeid << " elementname is " << elementname);
        addElementPriv((char*) elementname, (BaseType*) ObjectBroker::getObjectByOId(OId(elementtypeid)));
        count++;
    }
    DBObject::readFromDb();

    RMDBGEXIT(5, RMDebug::module_catalogif, "StructType", "readFromDb() " << myOId);
}

void
StructType::deleteFromDb() throw (r_Error)
{
    RMDBGENTER(5, RMDebug::module_catalogif, "StructType", "deleteFromDb() " << myOId);
    long long basetypeid = myOId.getCounter();
    SQLiteQuery::executeWithParams("DELETE FROM RAS_BASETYPENAMES WHERE BaseTypeId = %lld",
                                   basetypeid);
    SQLiteQuery::executeWithParams("DELETE FROM RAS_BASETYPES WHERE BaseTypeId = %lld",
                                   basetypeid);
    DBObject::deleteFromDb();
    RMDBGEXIT(5, RMDebug::module_catalogif, "StructType", "deleteFromDb() " << myOId);
}
