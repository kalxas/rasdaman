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

#include "reladminif/dbref.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/oidif.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/sqlitewrapper.hh"
#include "indexmgr/indexds.hh"
#include "relindexif/indexid.hh"
#include "relstorageif/dbstoragelayout.hh"
#include "relcatalogif/basetype.hh"
#include "relcatalogif/dbminterval.hh"
#include "relcatalogif/mddbasetype.hh"
#include "catalogmgr/typefactory.hh"
#include "dbmddobj.hh"
#include <logging.hh>

DBMDDObj::DBMDDObj()
    : DBObject(), myDomain{new DBMinterval()}, storageLayoutId(new DBStorageLayout()), objIxId()
{
    objecttype = OId::MDDOID;
    myDomain->setPersistent(true);
    storageLayoutId->setPersistent(true);
}

DBMDDObj::DBMDDObj(const OId &id)
    : DBObject(id), storageLayoutId(0LL), objIxId()
{
    objecttype = OId::MDDOID;
    readFromDb();
}

DBMDDObj::DBMDDObj(const MDDBaseType *newMDDType, const r_Minterval &domain,
                   const DBObjectId &newObjIx, const DBStorageLayoutId &newSL,
                   const OId &newOId)
    : DBObject(), mddType(newMDDType), storageLayoutId(newSL), objIxId(newObjIx.getOId())
{
    objecttype = OId::MDDOID;

    SQLiteQuery query("SELECT MDDId FROM RAS_MDDOBJECTS WHERE MDDId = %lld", newOId.getCounter());
    if (query.nextRow())
    {
        ((DBObjectId) newObjIx)->setPersistent(false);
        const_cast<DBStorageLayout *>(newSL.ptr())->setPersistent(false);
        LERROR << "mdd object " << newOId.getCounter() << " already exists in table RAS_MDDOBJECTS.";
        throw r_Error(r_Error::r_Error_NameNotUnique);
    }

    if (newMDDType->isPersistent())
        mddType = newMDDType;
    else
        mddType = (const MDDBaseType *) TypeFactory::addMDDType(newMDDType);

    myDomain = new DBMinterval(domain);
    _isPersistent = true;
    _isModified = true;
    myOId = newOId;
    setPersistent(true);
}

DBMDDObj::DBMDDObj(const MDDBaseType *newMDDType, const r_Minterval &domain,
                   const DBObjectId &newObjIx, const DBStorageLayoutId &newSL)
    : DBObject(), myDomain{new DBMinterval(domain)}, storageLayoutId(newSL), objIxId(newObjIx)
{
    objecttype = OId::MDDOID;
    mddType = newMDDType;
}

DBMDDObj::~DBMDDObj() noexcept(false)
{
    validate();
    delete myDomain, myDomain = NULL;
    delete nullValues, nullValues = NULL;
}

const MDDBaseType *DBMDDObj::getMDDBaseType() const
{
    return mddType;
}

DBStorageLayoutId
DBMDDObj::getDBStorageLayout() const
{
    return storageLayoutId;
}

const char *DBMDDObj::getCellTypeName() const
{
    return mddType->getBaseType()->getTypeName();
}

const BaseType *DBMDDObj::getCellType() const
{
    return mddType->getBaseType();
}

r_Dimension DBMDDObj::dimensionality() const
{
    return myDomain->dimension();
}

r_Minterval DBMDDObj::getDefinitionDomain() const
{
    return *myDomain;
}

void DBMDDObj::printStatus(unsigned int level, std::ostream &stream) const
{
    DBObject::printStatus(level, stream);
    stream << *myDomain << std::endl;
    mddType->printStatus(level + 1, stream);
    DBObjectId testIx(objIxId);
    if (!testIx.is_null())
        testIx->printStatus(level + 1, stream);
    else
        stream << "index is invalid " << objIxId.getOId();

    if (storageLayoutId.is_null())
        stream << "storagelayout is invalid " << storageLayoutId.getOId();
    else
        storageLayoutId->printStatus(level + 1, stream);
}

void DBMDDObj::setIx(const DBObjectId &newIx)
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

DBObjectId DBMDDObj::getDBIndexDS() const
{
    return objIxId;
}

// this should only receive an setPersistent(false)

void DBMDDObj::setPersistent(bool o)
{
    DBObject::setPersistent(o);
    if (!o)
        setCached(false);
    if (myDomain)
        myDomain->setPersistent(o);
    
    DBObjectId testIx(objIxId);
    if (testIx.is_null())
    {
        LERROR << "index " << objIxId.getOId() << " for object " << myOId << " is null.";
        throw r_Error(INDEX_OF_MDD_IS_NULL);
    }
    else
    {
        testIx->setPersistent(o);
        if (o) objIxId.release();
    }

    if (storageLayoutId.is_null())
    {
        LERROR << "layout " << storageLayoutId.getOId() << " for object " << myOId << " is null.";
        throw r_Error(STORAGE_OF_MDD_IS_NULL);
    }
    else
    {
        storageLayoutId->setPersistent(o);
    }

    if (o && !mddType->isPersistent())
        mddType = static_cast<const MDDBaseType *>(TypeFactory::addMDDType(mddType));
}

void DBMDDObj::setCached(bool ic)
{
    DBObject::setCached(ic);
    if (myDomain)
        myDomain->setCached(ic);
}

void DBMDDObj::incrementPersRefCount()
{
    ++persistentRefCount;
    setModified();
}

void DBMDDObj::decrementPersRefCount()
{
    --persistentRefCount;
    if (persistentRefCount == 0)
        setPersistent(false);

    setModified();
}

int DBMDDObj::getPersRefCount() const
{
    return persistentRefCount;
}

r_Bytes DBMDDObj::getHeaderSize() const
{
    return sizeof(MDDBaseType *) + sizeof(r_Minterval *) +
           sizeof(DBObjectId) + sizeof(DBObject) + sizeof(DBStorageLayoutId);
}

r_Bytes
DBMDDObj::getMemorySize() const
{
    return DBObject::getMemorySize() + sizeof(long) + sizeof(MDDBaseType *) +
           sizeof(DBMinterval *) + sizeof(OId) + myDomain->getMemorySize() +
           mddType->getMemorySize() + sizeof(OId);
}

DBNullvalues *DBMDDObj::getNullValues() const
{
    return nullValues;
}

void
DBMDDObj::setNullValues(const r_Nullvalues &newNullValues)
{
    nullValues = new DBNullvalues(newNullValues);
    setModified();
}

void DBMDDObj::updateInDb()
{
    const auto mddoid = myOId.getCounter();
    auto persRefCount3 = persistentRefCount;

    SQLiteQuery::executeWithParams(
        "UPDATE RAS_MDDOBJECTS SET PersRefCount = %ld, NodeOId = %lld WHERE MDDId = %lld",
        persRefCount3, static_cast<long long>(objIxId.getOId()), mddoid);

    if (nullValues != NULL)
    {
        LDEBUG << "Updating null values";
        nullValues->setPersistent(true);
        auto nullvalueoid = nullValues->getOId().getCounter();

        long long settypeoid{};
        {
            SQLiteQuery query(
                "SELECT c.settypeid FROM ras_mddcollnames as c, ras_mddcollections "
                "as m WHERE m.mddcollid = c.mddcollid and m.mddid = %lld", mddoid);
            if (query.nextRow())
            {
                settypeoid = query.nextColumnLong();
            }
            else
            {
                LERROR << "Collection type for MDD object " << mddoid << " not found in RAS_MDDCOLLECTIONS.";
                throw r_Ebase_dbms(SQLITE_NOTFOUND, "Collection type for MDD object not found in RAS_MDDCOLLECTIONS.");
            }
        }

        SQLiteQuery countQuery("SELECT COUNT(settypeoid) FROM RAS_NULLVALUES WHERE settypeoid = %lld", settypeoid);
        if (countQuery.nextRow() && countQuery.nextColumnInt() > 0)
        {
            SQLiteQuery query("SELECT nullvalueoid FROM RAS_NULLVALUES WHERE settypeoid = %lld", settypeoid);
            if (query.nextRow())
            {
                auto oldnullvalueoid = query.nextColumnLong();
                auto *oldNullValues = static_cast<DBNullvalues *>(
                    ObjectBroker::getObjectByOId(OId(oldnullvalueoid, OId::DBNULLVALUESOID)));
                if (oldNullValues)
                {
                    oldNullValues->setPersistent(false);
                    oldNullValues->validate();
                    delete oldNullValues;
                }
                SQLiteQuery::executeWithParams("DELETE FROM RAS_NULLVALUES WHERE nullvalueoid = %lld", oldnullvalueoid);
            }
        }
        SQLiteQuery::executeWithParams("INSERT INTO RAS_NULLVALUES (settypeoid, NullValueOId) VALUES (%lld, %lld)", settypeoid, nullvalueoid);
    }

    DBObject::updateInDb();
}

void DBMDDObj::insertInDb()
{
    long long storage = storageLayoutId->getOId();
    long long objindex = objIxId.getOId();
    long long basetypeid = mddType->getOId();
    long long domainid = myDomain->getOId().getCounter();

    SQLiteQuery::executeWithParams(
        "INSERT INTO RAS_MDDOBJECTS ( MDDId, BaseTypeOId, DomainId, PersRefCount, NodeOId, StorageOId) "
        "VALUES (%lld, %lld, %lld, %ld, %lld, %lld)",
        myOId.getCounter(), basetypeid, domainid, persistentRefCount, objindex, storage);
    DBObject::insertInDb();
}

void DBMDDObj::deleteFromDb()
{
    SQLiteQuery::executeWithParams("DELETE FROM RAS_MDDOBJECTS WHERE MDDId = %lld", myOId.getCounter());
    DBObject::deleteFromDb();
}

void DBMDDObj::readFromDb()
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif
    SQLiteQuery query(
        "SELECT BaseTypeOId, DomainId, PersRefCount, NodeOId, StorageOId "
        "FROM RAS_MDDOBJECTS WHERE MDDId = %lld", myOId.getCounter());
    if (query.nextRow())
    {
        mddType = static_cast<MDDBaseType *>(ObjectBroker::getObjectByOId(OId(query.nextColumnLong())));
        myDomain = static_cast<DBMinterval *>(ObjectBroker::getObjectByOId(OId(query.nextColumnLong(), OId::DBMINTERVALOID)));
        myDomain->setCached(true);
        persistentRefCount = query.nextColumnLong();
        objIxId = OId(query.nextColumnLong());
        storageLayoutId = OId(query.nextColumnLong());
    }
    else
    {
        LERROR << "mdd object " << myOId.getCounter() << " not found in table RAS_MDDOBJECTS.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "mdd object not found in table RAS_MDDOBJECTS.");
    }

    DBObject::readFromDb();
#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif
}
