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
/**
 * SOURCE: mddcoll.cc
 *
 * MODULE: cachetamgr
 * CLASS:  MDDColl
 *
 * COMMENTS:
 *   none
 *
*/

#include "config.h"
#include "mymalloc/mymalloc.h"

#include <iostream>
#include "mddcoll.hh"
#include "mddcolliter.hh"
#include "relmddif/dbmddset.hh"
#include "mddobj.hh"
#include "relmddif/dbmddobj.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/oidif.hh"
#include "relcatalogif/collectiontype.hh"   // from base catalogif DBMS interface module
#include "reladminif/databaseif.hh"
#include "relmddif/dbmddset.hh"
#include "reladminif/eoid.hh"
#include "tilemgr/tile.hh"
#include <logging.hh>

#include "relcatalogif/settype.hh"
#include "relcatalogif/mdddomaintype.hh"
#include "relcatalogif/mdddimensiontype.hh"
#include "relcatalogif/alltypes.hh"
#include "catalogmgr/typefactory.hh"

#include <boost/algorithm/string/predicate.hpp>

// MDD and SET names required for returning the list of types
// they can be any string and are required just by the internal structure
#define MOCK_MDD_COLLECTION_NAME "RAS_NAMETYPE"
#define MOCK_SET_COLLECTION_NAME "RAS_NAMESETTYPE"


#include <cstring>

MDDColl::MDDColl(const CollectionType* newType, const char* name)
{
    LTRACE << "MDDColl(" << newType->getName() << ", " << (name ? name : "null") << ") " << (r_Ptr)this;
    const char* theName = name;
    if (theName == NULL)
    {
        theName = "rasdaman temporary collection";
    }
    dbColl = new DBMDDSet(theName, newType);
}

const char*
MDDColl::getName() const
{
    return dbColl->getName();
}

const CollectionType*
MDDColl::getCollectionType() const
{
    return dbColl->getCollType();
}

void
MDDColl::setCollectionType(const CollectionType* collType)
{
    return dbColl->setCollType(collType);
}

unsigned long
MDDColl::getCardinality() const
{
    return dbColl->getCardinality();
}

bool
MDDColl::getOId(OId& pOId) const
{
    if (isPersistent())
    {
        pOId = dbColl->getOId();
        return true;
    }
    return false;
}

bool
MDDColl::getEOId(EOId& pEOId) const
{
    if (isPersistent())
    {
        pEOId = dbColl->getEOId();
        return true;
    }
    return false;
}

void
MDDColl::insert(const MDDObj* newObj)
{
#ifdef DEBUG
    if (newObj == 0)
    {
        LERROR << "MDDColl::insert(const MDDObj*) assertion failed";
        throw r_Error(MDD_NOT_VALID);
    }
#endif
    LTRACE << "insert(" << (r_Ptr)newObj << ")";
    dbColl->insert(newObj->getDBMDDObjId());
    insertIntoCache(newObj);
#ifdef DEBUG
    dbColl->printStatus(0, RMInit::dbgOut);
#endif
}

void
MDDColl::releaseAll()
{
    LDEBUG << "release all MDD objects in coll " << getName();
    MDDObj* tempMDD = 0;
    while (!mddCache.empty())
    {
        tempMDD = (*mddCache.begin()).second;
        (*mddCache.begin()).second = NULL;
        delete tempMDD;
        mddCache.erase(mddCache.begin());
    }
}

MDDColl::~MDDColl()
{
    if (isPersistent())
    {
        releaseAll();
    }
    //else released by release transfer structures
}

MDDColl::MDDColl(const DBMDDSetId& coll)
    : dbColl(coll)
{
}

DBMDDSetId
MDDColl::getDBMDDSet() const
{
    return dbColl;
}

void
MDDColl::insertIntoCache(const MDDObj* objToInsert) const
{
    LTRACE << "insertIntoCache(" << (r_Ptr)objToInsert << ")";
    mddCache[objToInsert->getDBMDDObjId().ptr()] = const_cast<MDDObj*>(objToInsert);
}

MDDObj*
MDDColl::getMDDObj(const DBMDDObj* objToGet) const
{
    MDDObj* persMDDObjToGet = NULL;
    MDDObjMap::const_iterator i = mddCache.find(const_cast<DBMDDObj*>(objToGet));
    if (i != mddCache.end())
    {
        persMDDObjToGet = static_cast<MDDObj*>((*i).second);
    }
    else
    {
        persMDDObjToGet = new MDDObj(const_cast<DBMDDObj*>(objToGet));
        insertIntoCache(persMDDObjToGet);
    }
    return persMDDObjToGet;
}

bool
MDDColl::isPersistent() const
{
    return dbColl->isPersistent();
}

void
MDDColl::printStatus(unsigned int level, std::ostream& stream) const
{
    dbColl->printStatus(level, stream);
    char* indent = new char[level * 2 + 1];
    for (unsigned int j = 0; j < level * 2 ; j++)
    {
        indent[j] = ' ';
    }
    indent[level * 2] = '\0';
    stream << indent;
    for (MDDObjMap::iterator i = mddCache.begin(); i != mddCache.end(); i++)
    {
        stream << (r_Ptr)(*i).second;
    }
    delete[] indent;
    indent = 0;
}

MDDCollIter*
MDDColl::createIterator() const
{
    MDDCollIter* iter = new MDDCollIter(const_cast<MDDColl*>(this));
    return iter;
}

void
MDDColl::remove(const MDDObj* obj)
{
    if (obj != NULL)
    {
        DBMDDObjId t2 = obj->getDBMDDObjId();
        dbColl->remove(t2);

        //remove from cache ;(
        MDDObjMap::iterator i = mddCache.find(t2.ptr());
        if (i != mddCache.end())
        {
            LTRACE << "remove(" << (r_Ptr)obj << ") found in cache";
            mddCache.erase(i);
        }
        else
        {
            LTRACE << "remove(" << (r_Ptr)obj << ") not in collection cache";
        }
    }
    else
    {
        LTRACE << "remove(MDDObj*) NULL";
        throw r_Error(MDD_NOT_VALID);
    }
}

/*
void
MDDColl::removeFromCache(const PersMDDObj* objToRemove)
    {
    DBMDDObj* objIdVoidAddress = objToRemove->getDBMDDObjId().ptr();
    MDDObjMap::iterator i = mddCache.find(objIdVoidAddress);

    if (i != mddCache.end())
        {
        persMDDObjToRemove = (*i).second;
        (*i).second = NULL;
        delete persMDDObjToRemove;
        mddCache.erase(i);
#ifdef DEBUG
        if (mddCache.find(objIdVoidAddress) != mddCache.end())
        {
            LERROR << "MDDColl::removeMDDObjfromCache() object multiple times in cache";
            throw r_Error(MDD_EXISTS_MULTIPLE_TIMES);
        }
#endif
        }
    else    {
        LTRACE << "removeMDDObjfromCache(" << objToRemove << ") not in collection";
        }
    }
*/

void
MDDColl::removeAll()
{
    dbColl->removeAll();
}


const char*
MDDColl::AllCollectionnamesName = "RAS_COLLECTIONNAMES";

const char*
MDDColl::AllStructTypesName = "RAS_STRUCT_TYPES";

const char*
MDDColl::AllMarrayTypesName = "RAS_MARRAY_TYPES";

const char*
MDDColl::AllSetTypesName = "RAS_SET_TYPES";

MDDColl*
MDDColl::createMDDCollection(const char* name, const CollectionType* ct)
{
    if (name == NULL)
    {
        LTRACE << "createMDDColl(NULL, colltype)";
        throw r_Error(r_Error::r_Error_NameNotUnique);
    }
    if (ct == NULL)
    {
        LTRACE << "createMDDColl(" << name << ", NULL)";
        throw r_Error(COLLTYPE_NULL);
    }
    if (!ct->isPersistent())
    {
        r_Error t(209);
        t.setTextParameter("type", ct->getName());
        LTRACE << "createMDDColl(" << name << ", " << ct->getName() << " not persistent)";
        throw t;
    }
    // may generate an exception:
    DBMDDSetId newDBColl = new DBMDDSet(name, ct);
    return new MDDColl(newDBColl);
}

MDDColl*
MDDColl::createMDDCollection(const char* name, const OId& o, const CollectionType* ct)
{
    // may generate an exception:
    if (name == NULL)
    {
        LTRACE << "createMDDColl(NULL, " << o << ", colltype)";
        throw r_Error(r_Error::r_Error_NameNotUnique);
    }
    if (ct == NULL)
    {
        LTRACE << "createMDDColl(" << name << ", " << o << ", NULL)";
        throw r_Error(COLLTYPE_NULL);
    }
    if (!ct->isPersistent())
    {
        r_Error t(209);
        t.setTextParameter("type", ct->getName());
        LTRACE << "createMDDColl(" << name << ", " << o << ", " << ct->getName() << " not persistent)";
        throw t;
    }
    DBMDDSetId newDBColl = new DBMDDSet(name, o, ct);
    return new MDDColl(newDBColl);
}

bool
MDDColl::dropMDDCollection(const char* name)
{
    return DBMDDSet::deleteDBMDDSet(name);
}

bool
MDDColl::dropMDDCollection(const OId& o)
{
    return DBMDDSet::deleteDBMDDSet(o);
}

MDDColl*
MDDColl::getMDDCollection(const OId& collOId)
{
    DBMDDSetId t(collOId);
    //this will throw an exception
    t->isModified();
    MDDColl* retval = new MDDColl(t);
    return retval;
}

bool MDDColl::isVirtual(const char *collName) {
    return strcmp(collName, AllCollectionnamesName) == 0 ||
           strcmp(collName, AllStructTypesName) == 0 ||
           strcmp(collName, AllMarrayTypesName) == 0 ||
           strcmp(collName, AllSetTypesName) == 0;
}

bool MDDColl::collExists(const char *collName)
{
    try
    {
        return isVirtual(collName) ||
               DBMDDSet::getDBMDDSet(collName).isInitialised();
    }
    catch (const r_Error &ex)
    {
        if (ex.get_kind() == r_Error::r_Error_ObjectUnknown)
            return false;
        else
            throw;
    }
}

MDDColl*
MDDColl::getMDDCollection(const char* collName)
{
    MDDColl* retval = 0;
    DBMDDSetId dbset;
    if (strcmp(collName, AllCollectionnamesName) == 0)
    {
        // the domains are required because rasql returns only arrays
        // since our result is a string it can be returned as a 1D char array
        r_Minterval transDomain("[0:*]");
        r_Minterval nameDomain("[0:0]");
        const BaseType* bt = TypeFactory::mapType("Char");
        MDDDomainType* mt = new MDDDomainType(MOCK_MDD_COLLECTION_NAME, bt, transDomain);
        TypeFactory::addTempType(mt);
        CollectionType* ct = new SetType(MOCK_SET_COLLECTION_NAME, mt);
        TypeFactory::addTempType(ct);
        retval = new MDDColl(ct, AllCollectionnamesName);
        OIdSet* list = ObjectBroker::getAllObjects(OId::MDDCOLLOID);
        MDDObj* transObj = 0;
        boost::shared_ptr<Tile> transTile;
        const char* nameBuffer = 0;
        size_t namelen = 0;
        while (!list->empty())
        {
            dbset = *(list->begin());
            LTRACE << "Coll OId     : " << dbset.getOId();
            nameBuffer = dbset->getName();
            LTRACE << "Coll Name    : " << nameBuffer;
            namelen = strlen(nameBuffer);
            LTRACE << "Coll Name Len: " << namelen;
            LTRACE << "Domain       : " << namelen;
            nameDomain[0].set_high(static_cast<r_Range>(namelen));
            transObj = new MDDObj(mt, nameDomain);
            transTile.reset(new Tile(nameDomain, bt, nameBuffer, (r_Bytes)0, r_Array));
            transObj->insertTile(transTile);
            retval->insert(transObj);
            list->erase(list->begin());
        }
        delete list;
    }
    else if (strcmp(collName, AllStructTypesName) == 0)
    {
        // the domains are required because rasql returns only arrays
        // since our result is a string it can be returned as a 1D char array
        r_Minterval transDomain("[0:*]");
        r_Minterval nameDomain("[0:0]");
        const BaseType* bt = TypeFactory::mapType(CharType::Name);
        MDDDomainType* mt = new MDDDomainType(MOCK_MDD_COLLECTION_NAME, bt, transDomain);
        TypeFactory::addTempType(mt);
        CollectionType* ct = new SetType(MOCK_SET_COLLECTION_NAME, mt);
        TypeFactory::addTempType(ct);
        retval = new MDDColl(ct, AllStructTypesName);

        TypeIterator<StructType> structIter = TypeFactory::createStructIter();
        MDDObj* transObj = 0;
        Tile* transTile = 0;

        while (structIter.not_done())
        {
            StructType* typePtr       = structIter.get_element();
            char*       typeStructure = typePtr->getNewTypeStructure();

            if (!boost::starts_with(typePtr->getTypeName(), TypeFactory::ANONYMOUS_CELL_TYPE_PREFIX))
            {
                std::string result = "";
                result.append("CREATE TYPE ");
                result.append(typePtr->getTypeName());
                result.append(" AS ");
                result.append(typeStructure);

                nameDomain[0].set_high(static_cast<r_Range>(result.length()));
                transObj = new MDDObj(mt, nameDomain);
                transTile = new Tile(nameDomain, bt, result.c_str(), (r_Bytes)0, r_Array);
                transObj->insertTile(transTile);
                retval->insert(transObj);
            }
            free(typeStructure);
            typeStructure = NULL;

            structIter.advance();
        }
    }
    else if (strcmp(collName, AllMarrayTypesName) == 0)
    {
        // the domains are required because rasql returns only arrays
        // since our result is a string it can be returned as a 1D char array
        r_Minterval transDomain("[0:*]");
        r_Minterval nameDomain("[0:0]");
        const BaseType* bt = TypeFactory::mapType(CharType::Name);
        MDDDomainType* mt = new MDDDomainType(MOCK_MDD_COLLECTION_NAME, bt, transDomain);
        TypeFactory::addTempType(mt);
        CollectionType* ct = new SetType(MOCK_SET_COLLECTION_NAME, mt);
        TypeFactory::addTempType(ct);
        retval = new MDDColl(ct, AllMarrayTypesName);

        TypeIterator<StructType> structIter = TypeFactory::createStructIter();
        MDDObj* transObj = 0;
        Tile* transTile = 0;

        TypeIterator<MDDType> mddIter = TypeFactory::createMDDIter();

        while (mddIter.not_done())
        {
            MDDType* typePtr = mddIter.get_element();

            char* tmpTypeStructure = typePtr->getNewTypeStructure();
            std::string typeStructure{tmpTypeStructure};
            free(tmpTypeStructure);

            if (typePtr->getSubtype() == MDDType::MDDBASETYPE ||
                typePtr->getSubtype() == MDDType::MDDONLYTYPE)
            {
                LDEBUG << "Internal MDD type cannot be serialized: " << typeStructure;
                mddIter.advance();
                continue;
            }

            std::string result = "";
            result.append("CREATE TYPE ");
            result.append(typePtr->getTypeName());
            result.append(" AS ");
            result.append(typeStructure);

            nameDomain[0].set_high(static_cast<r_Range>(result.length()));
            transObj = new MDDObj(mt, nameDomain);
            transTile = new Tile(nameDomain, bt, result.c_str(), (r_Bytes)0, r_Array);
            transObj->insertTile(transTile);
            retval->insert(transObj);

            mddIter.advance();
        }
    }
    else if (strcmp(collName, AllSetTypesName) == 0)
    {
        // the domains are required because rasql returns only arrays
        // since our result is a string it can be returned as a 1D char array
        r_Minterval transDomain("[0:*]");
        r_Minterval nameDomain("[0:0]");
        const BaseType* bt = TypeFactory::mapType(CharType::Name);
        MDDDomainType* mt = new MDDDomainType(MOCK_MDD_COLLECTION_NAME, bt, transDomain);
        TypeFactory::addTempType(mt);
        CollectionType* ct = new SetType(MOCK_SET_COLLECTION_NAME, mt);
        TypeFactory::addTempType(ct);
        retval = new MDDColl(ct, AllSetTypesName);

        TypeIterator<StructType> structIter = TypeFactory::createStructIter();
        MDDObj* transObj = 0;
        Tile* transTile = 0;

        TypeIterator<SetType> setIter = TypeFactory::createSetIter();
        while (setIter.not_done())
        {
            SetType* typePtr       = setIter.get_element();

            std::string result = "";
            result.append("CREATE TYPE ");
            result.append(typePtr->getTypeName());
            result.append(" AS SET (");
            result.append(typePtr->getMDDType()->getTypeName());

            DBNullvalues* nullValues = typePtr->getNullValues();
            if (nullValues)
            {
                result.append(" NULL VALUES ");
                result.append(nullValues->toString());
            }

            result.append(")");

            nameDomain[0].set_high(static_cast<r_Range>(result.length()));
            transObj = new MDDObj(mt, nameDomain);
            transTile = new Tile(nameDomain, bt, result.c_str(), (r_Bytes)0, r_Array);
            transObj->insertTile(transTile);
            retval->insert(transObj);

            setIter.advance();
        }

    }
    else
    {
        dbset = DBMDDSet::getDBMDDSet(collName);
        retval = new MDDColl(dbset);
    }
    return retval;
}

bool
MDDColl::removeMDDObject(const OId& collOId, const OId& mddOId)
{
    bool retval = true;
    DBMDDSetId coll(collOId);
    DBMDDObjId mdd(mddOId);

    if (coll.is_null())
    {
        //does not exist
        retval = false;
    }
    else
    {
        if (mdd.is_null())
        {
            retval = false;
        }
        else
        {
            coll->remove(mdd);
        }
    }
    return retval;
}

