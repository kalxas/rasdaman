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
 *
 *
 * COMMENTS:
 *
 ************************************************************/


#include "config.h"
#include "reladminif/oidif.hh"
#include "reladminif/dbref.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/externs.h"
#include "reladminif/sqlglobals.h"
#include "catalogmgr/typefactory.hh"
#include "relcatalogif/mddbasetype.hh"
#include "relcatalogif/basetype.hh"
#include "reladminif/objectbroker.hh"
#include "relcatalogif/dbminterval.hh"
#include "relstorageif/dbstoragelayout.hh"
#include "dbmddobj.hh"
#include "relindexif/indexid.hh"
#include "indexmgr/indexds.hh"
#include "reladminif/sqlitewrapper.hh"
#include <easylogging++.h>

#include "debug-srv.hh"

DBMDDObj::DBMDDObj()
    : DBObject(),
      persistentRefCount(0),
      mddType(NULL),
      myDomain(NULL),
      storageLayoutId(new DBStorageLayout()),
      objIxId(),
      nullValues(NULL)
{
    objecttype = OId::MDDOID;
    myDomain = new DBMinterval();
    myDomain->setPersistent(true);
    storageLayoutId->setPersistent(true);

}

DBMDDObj::DBMDDObj(const OId& id) throw (r_Error)
    : DBObject(id),
      persistentRefCount(0),
      mddType(NULL),
      myDomain(NULL),
      storageLayoutId(0LL),
      objIxId(),
      nullValues(NULL)
{
    objecttype = OId::MDDOID;
    readFromDb();
}

DBMDDObj::DBMDDObj(const MDDBaseType* newMDDType,
                   const r_Minterval& domain,
                   const DBObjectId& newObjIx,
                   const DBStorageLayoutId& newSL,
                   const OId& newOId) throw (r_Error)
    : DBObject(),
      persistentRefCount(0),
      mddType(newMDDType),
      myDomain(NULL),
      storageLayoutId(newSL),
      objIxId(newObjIx.getOId()),
      nullValues(NULL)
{
    objecttype = OId::MDDOID;
    long long testoid1;

    testoid1 = newOId.getCounter();

    // (1) --- fetch tuple from database
    SQLiteQuery query("SELECT MDDId FROM RAS_MDDOBJECTS WHERE MDDId = %lld", testoid1);
    if (query.nextRow())
    {
        ((DBObjectId) newObjIx)->setPersistent(false);
        ((DBObject*) const_cast<DBStorageLayout*>(newSL.ptr()))->setPersistent(false);
        LFATAL << "DBMDDObj::DBMDDObj() - mdd object: "
               << testoid1 << " already exists in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "mdd object already exists in the database.");
    }

    if (newMDDType->isPersistent())
    {
        mddType = newMDDType;
    }
    else
    {
        mddType = (const MDDBaseType*) TypeFactory::addMDDType(newMDDType);
    }
    myDomain = new DBMinterval(domain);
    _isPersistent = true;
    _isModified = true;
    myOId = newOId;
    setPersistent(true);
}

DBMDDObj::DBMDDObj(const DBMDDObj& old)
    : DBObject(old),
      persistentRefCount(0),
      mddType(NULL),
      myDomain(NULL),
      storageLayoutId(),
      objIxId(),
      nullValues(NULL)
{
    if (old.myDomain)
    {
        if (old.myDomain->isPersistent())
        {
            myDomain = (DBMinterval*) ObjectBroker::getObjectByOId(old.myDomain->getOId());
        }
        else
        {
            myDomain = new DBMinterval(*old.myDomain);
            myDomain->setPersistent(true);
        }
    }
    else
    {
        myDomain = NULL;
    }

    objIxId = old.objIxId;
    storageLayoutId = old.storageLayoutId;
    persistentRefCount = old.persistentRefCount;
    mddType = old.mddType;
}

DBMDDObj::DBMDDObj(const MDDBaseType* newMDDType, const r_Minterval& domain, const DBObjectId& newObjIx, const DBStorageLayoutId& newSL)
    : DBObject(),
      persistentRefCount(0),
      mddType(NULL),
      myDomain(NULL),
      storageLayoutId(newSL),
      objIxId(newObjIx),
      nullValues(NULL)
{
    objecttype = OId::MDDOID;
    myDomain = new DBMinterval(domain);
    mddType = newMDDType;
}

DBMDDObj::~DBMDDObj()
{
    validate();
    if (myDomain)
    {
        delete myDomain;
    }
    myDomain = NULL;

    if (nullValues)
    {
        delete nullValues;
    }
    nullValues = NULL;
}

DBStorageLayoutId
DBMDDObj::getDBStorageLayout() const
{
    return storageLayoutId;
}

r_Bytes
DBMDDObj::getMemorySize() const
{
    return DBObject::getMemorySize() + sizeof(long) + sizeof(MDDBaseType*) + sizeof(DBMinterval*) + sizeof(OId) + myDomain->getMemorySize() + mddType->getMemorySize() + sizeof(OId);
}

const MDDBaseType*
DBMDDObj::getMDDBaseType() const
{
    return mddType;
}

const BaseType*
DBMDDObj::getCellType() const
{
    return mddType->getBaseType();
}

r_Dimension
DBMDDObj::dimensionality() const
{
    return myDomain->dimension();
}

void
DBMDDObj::setCached(bool ic)
{
    DBObject::setCached(ic);
    if (myDomain)
    {
        myDomain->setCached(ic);
    }
}

//this should only receive an setPersistent(false)

void
DBMDDObj::setPersistent(bool o) throw (r_Error)
{
    DBObject::setPersistent(o);
    if (!o)
    {
        setCached(false);
    }
    if (myDomain)
    {
        myDomain->setPersistent(o);
    }
    DBObjectId testIx(objIxId);
    if (testIx.is_null())
    {
        LTRACE << "index object is not valid " << myOId << " index " << objIxId.getOId();
        throw r_Error(INDEX_OF_MDD_IS_NULL);
    }
    else
    {
        testIx->setPersistent(o);
        if (o)
        {
            objIxId.release();
        }
    }

    if (storageLayoutId.is_null())
    {
        LTRACE << "layout object is not valid " << myOId << " layout " << storageLayoutId.getOId();
        LFATAL << "DBMDDObj::setPersistent() layout object is not valid";
        throw r_Error(STORAGE_OF_MDD_IS_NULL);
    }
    else
    {
        storageLayoutId->setPersistent(o);
    }
    if (o && !mddType->isPersistent())
    {
        mddType = (const MDDBaseType*) TypeFactory::addMDDType(mddType);
    }

}

const char*
DBMDDObj::getCellTypeName() const
{
    return mddType->getBaseType()->getTypeName();
}

r_Minterval
DBMDDObj::getDefinitionDomain() const
{
    return *myDomain;
}

r_Bytes
DBMDDObj::getHeaderSize() const
{
    r_Bytes sz = sizeof(MDDBaseType*) + sizeof(r_Minterval*) + sizeof(DBObjectId) + sizeof(DBObject) + sizeof(DBStorageLayoutId);
    return sz;
}

void
DBMDDObj::printStatus(unsigned int level, ostream& stream) const
{
    DBObject::printStatus(level, stream);
    stream << *myDomain << endl;
    mddType->printStatus(level + 1, stream);
    DBObjectId testIx(objIxId);
    if (!testIx.is_null())
    {
        testIx->printStatus(level + 1, stream);
    }
    else
    {
        stream << "index is invalid " << objIxId.getOId();
    }
    if (storageLayoutId.is_null())
    {
        stream << "storagelayout is invalid " << storageLayoutId.getOId();
    }
    else
    {
        storageLayoutId->printStatus(level + 1, stream);
    }
}

void
DBMDDObj::setIx(const DBObjectId& newIx)
{
    if (isPersistent())
    {
        if (objIxId.getOId() != newIx.getOId())
        {
            objIxId = newIx.getOId();
            setModified();
        }
    }
    else
    {
        objIxId = newIx;
    }
}

void
DBMDDObj::updateInDb() throw (r_Error)
{
    long long mddoid3{};
    long long objindex3{};
    long persRefCount3{};
    long long nullvalueoid{};
    long long oldnullvalueoid{};
    long long settypeoid{};
    long count{};

    objindex3 = objIxId.getOId();
    mddoid3 = myOId.getCounter();
    persRefCount3 = persistentRefCount;

    SQLiteQuery::executeWithParams("UPDATE RAS_MDDOBJECTS SET PersRefCount = %ld, NodeOId = %lld WHERE MDDId = %lld",
                                   persRefCount3, objindex3, mddoid3);

    if (nullValues != NULL)
    {
        LDEBUG << "Updating null values";
        nullValues->setPersistent(true);
        nullvalueoid = nullValues->getOId().getCounter();

        {
            SQLiteQuery query("SELECT c.settypeid FROM ras_mddcollnames as c, ras_mddcollections as m WHERE m.mddcollid = c.mddcollid and m.mddid = %lld", mddoid3);
            if (query.nextRow())
            {
                settypeoid = query.nextColumnLong();
            }
            else
            {
                LFATAL << "Collection type not found in the database.";
                throw r_Ebase_dbms(SQLITE_NOTFOUND, "Collection type not found in the database.");
            }
        }

        {
            SQLiteQuery query("SELECT COUNT(settypeoid) FROM RAS_NULLVALUES WHERE settypeoid = %lld", settypeoid);
            if (query.nextRow())
            {
                count = query.nextColumnInt();
            }
        }

        if (count > 0)
        {
            SQLiteQuery query("SELECT nullvalueoid FROM RAS_NULLVALUES WHERE settypeoid = %lld", settypeoid);
            if (query.nextRow())
            {
                oldnullvalueoid = query.nextColumnLong();
            }
            DBMinterval* oldNullValues = (DBMinterval*) ObjectBroker::getObjectByOId(OId(oldnullvalueoid, OId::DBMINTERVALOID));
            if (oldNullValues)
            {
                oldNullValues->setPersistent(false);
                oldNullValues->validate();
                delete oldNullValues;
            }
            SQLiteQuery::executeWithParams("DELETE FROM RAS_NULLVALUES WHERE nullvalueoid = %lld", oldnullvalueoid);
        }

        SQLiteQuery::executeWithParams("INSERT INTO RAS_NULLVALUES (settypeoid, NullValueOId) VALUES (%lld, %lld)", settypeoid, nullvalueoid);
    }

    DBObject::updateInDb();
}

void
DBMDDObj::insertInDb() throw (r_Error)
{
    long long mddoid{};
    long long basetypeid{};
    long long storage{};
    long long domainid{};
    long long objindex{};
    long persRefCount{};

    storage = storageLayoutId->getOId();
    objindex = objIxId.getOId();
    mddoid = myOId.getCounter();
    basetypeid = mddType->getOId();
    domainid = myDomain->getOId().getCounter();
    persRefCount = persistentRefCount;

    SQLiteQuery::executeWithParams("INSERT INTO RAS_MDDOBJECTS ( MDDId, BaseTypeOId, DomainId, PersRefCount, NodeOId, StorageOId) VALUES (%lld, %lld, %lld, %ld, %lld, %lld)",
                                   mddoid, basetypeid, domainid, persRefCount, objindex, storage);
    DBObject::insertInDb();
}

void
DBMDDObj::deleteFromDb() throw (r_Error)
{
    long long mddoid1 = myOId.getCounter();
    SQLiteQuery::executeWithParams("DELETE FROM RAS_MDDOBJECTS WHERE MDDId = %lld", mddoid1);
    DBObject::deleteFromDb();
}

void
DBMDDObj::readFromDb() throw (r_Error)
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif
    long long mddoid2{};
    long long basetypeid2{};
    long long domainid2{};
    long long objindex2{};
    long persRefCount2{};
    long long storage2{};

    mddoid2 = myOId.getCounter();

    SQLiteQuery query("SELECT BaseTypeOId, DomainId, PersRefCount, NodeOId, StorageOId FROM RAS_MDDOBJECTS WHERE MDDId = %lld", mddoid2);
    if (query.nextRow())
    {
        basetypeid2 = query.nextColumnLong();
        domainid2 = query.nextColumnLong();
        persRefCount2 = query.nextColumnLong();
        objindex2 = query.nextColumnLong();
        storage2 = query.nextColumnLong();
    }
    else
    {
        LFATAL << "DBMDDObj::readFromDb() - mdd object: "
               << mddoid2 << " not found in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "mdd object not found in the database.");
    }

    objIxId = OId(objindex2);
    storageLayoutId = OId(storage2);
    persistentRefCount = persRefCount2;
    mddType = (MDDBaseType*) ObjectBroker::getObjectByOId(OId(basetypeid2));
    myDomain = (DBMinterval*) ObjectBroker::getObjectByOId(OId(domainid2, OId::DBMINTERVALOID));
    myDomain->setCached(true);

    DBObject::readFromDb();
#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif
}

DBObjectId
DBMDDObj::getDBIndexDS() const
{
    return objIxId;
}

int
DBMDDObj::getPersRefCount() const
{
    return persistentRefCount;
}

void
DBMDDObj::incrementPersRefCount()
{
    persistentRefCount++;
    setModified();
}

void
DBMDDObj::decrementPersRefCount()
{
    persistentRefCount--;
    if (persistentRefCount == 0)
    {
        setPersistent(false);
    }
    setModified();
}

DBMinterval*
DBMDDObj::getNullValues() const
{
    if (nullValues != NULL)
    {
        LDEBUG << "returning null values: " << nullValues->get_string_representation();
    }
    return nullValues;
}

void
DBMDDObj::setNullValues(const r_Minterval& newNullValues)
{
    nullValues = new DBMinterval(newNullValues);
    setModified();
}
