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
/*************************************************************************
 *
 *
 * PURPOSE:
 *      Code with embedded SQL for PostgreSQL DBMS
 *
 *
 * COMMENTS:
 *      none
 *
 ***********************************************************************/

#include "config.h"
#include "mdddimensiontype.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/sqlitewrapper.hh"
#include <logging.hh>

void MDDDimensionType::insertInDb()
{
    long long mddtypeid;
    long long mddbasetypeid;
    short dimension;
    char mddtypename[STRING_MAXLEN];

    dimension = myDimension;
    mddtypeid = myOId.getCounter();
    (void)strncpy(mddtypename, const_cast<char *>(getName()),
                  (size_t)sizeof(mddtypename));
    DBObject *obj = (DBObject *)const_cast<BaseType *>(getBaseType());
    mddbasetypeid = obj->getOId();
    LTRACE << " typeid " << mddtypeid << " name " << mddtypename
           << " basetypeoid " << mddbasetypeid << "dimension " << dimension;

    SQLiteQuery::executeWithParams(
        "INSERT INTO RAS_MDDDIMTYPES ( MDDDimTypeOId, MDDTypeName, BaseTypeId, "
        "Dimension) VALUES (%lld, '%s', %lld, %d)",
        mddtypeid, mddtypename, mddbasetypeid, dimension);
    DBObject::insertInDb();
}

void MDDDimensionType::readFromDb()
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif
    long long mddtypeid;
    long long mddbasetypeid;
    char *mddtypename;
    short dimension;

    mddtypeid = myOId.getCounter();
    mddbasetypeid = 0;
    dimension = 0;

    SQLiteQuery query(
        "SELECT Dimension, BaseTypeId, MDDTypeName FROM RAS_MDDDIMTYPES WHERE "
        "MDDDimTypeOId = %lld",
        mddtypeid);
    if (query.nextRow())
    {
        dimension = query.nextColumnInt();
        mddbasetypeid = query.nextColumnLong();
        setName(query.nextColumnString());
    }
    else
    {
        LERROR << "MDDDimensionType::readFromDb() - mdd type: " << mddtypeid
               << " not found in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND,
                           "mdd type object not found in the database.");
    }

    myDimension = static_cast<r_Dimension>(dimension);
    myBaseType = (BaseType *)ObjectBroker::getObjectByOId(OId(mddbasetypeid));
#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif
    LTRACE << "myBaseType at " << myBaseType;
    DBObject::readFromDb();
}

void MDDDimensionType::deleteFromDb()
{
    long long mddtypeid = myOId.getCounter();
    SQLiteQuery::executeWithParams(
        "DELETE FROM RAS_MDDDIMTYPES WHERE MDDDimTypeOId = %lld", mddtypeid);
    DBObject::deleteFromDb();
}
