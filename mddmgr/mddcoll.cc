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

#include "mddcoll.hh"

#include "mymalloc/mymalloc.h"
#include "mddcolliter.hh"                         // for MDDCollIter
#include "mddobj.hh"                              // for MDDObj
#include "tilemgr/tile.hh"                        // for Tile
#include "relcatalogif/typefactory.hh"              // for TypeFactory, TypeFac...
#include "reladminif/databaseif.hh"               // for ostream
#include "reladminif/dbobjectiterator.hh"         // for DBObjectIterator
#include "reladminif/dbref.hh"                    // for DBRef
#include "reladminif/eoid.hh"                     // for EOId
#include "reladminif/lists.h"                     // for OIdSet
#include "reladminif/objectbroker.hh"             // for ObjectBroker
#include "reladminif/oidif.hh"                    // for OId, operator<<, OId...
#include "relmddif/dbmddset.hh"
#include "relmddif/dbmddobj.hh"
#include "relmddif/dbmddset.hh"
#include "relcatalogif/mdddomaintype.hh"          // for MDDDomainType
#include "relcatalogif/settype.hh"                // for SetType
#include "relcatalogif/chartype.hh"               // for CharType, CharType::...
#include "relcatalogif/collectiontype.hh"         // for CollectionType
#include "relcatalogif/dbnullvalues.hh"           // for DBNullvalues
#include "relcatalogif/mddtype.hh"                // for MDDType
#include "relcatalogif/structtype.hh"             // for StructType
#include "relmddif/dbmddset.hh"                   // for DBMDDSet
#include "raslib/error.hh"                        // for r_Error, COLLTYPE_NULL
#include "raslib/mddtypes.hh"                     // for r_Ptr, r_Array, r_Bytes
#include "raslib/minterval.hh"                    // for r_Minterval
#include "raslib/sinterval.hh"                    // for r_Sinterval
#include "common/util/vectorutils.hh"
#include "common/string/stringutil.hh"
#include "logging.hh"                             // for LTRACE

#include <iostream>                               // for operator<<, ostream
#include <stdlib.h>                               // for free, size_t
#include <string>                                 // for string, basic_string
#include <utility>                                // for pair
#include <unordered_set>

// MDD and SET names required for returning the list of types
// they can be any string and are required just by the internal structure
#define MOCK_MDD_COLLECTION_NAME "RAS_NAMETYPE"
#define MOCK_SET_COLLECTION_NAME "RAS_NAMESETTYPE"


MDDColl::MDDColl(const CollectionType *newType, const char *name)
{
    const char *theName = name;
    if (theName == nullptr)
    {
        theName = "rasdaman temporary collection";
    }
    dbColl = new DBMDDSet(theName, newType);
}

const char *MDDColl::getName() const
{
    return dbColl->getName();
}

const CollectionType *MDDColl::getCollectionType() const
{
    return dbColl->getCollType();
}

void MDDColl::setCollectionType(const CollectionType *collType)
{
    return dbColl->setCollType(collType);
}


unsigned long MDDColl::getCardinality() const
{
    return dbColl->getCardinality();
}

bool MDDColl::getOId(OId &pOId) const
{
    if (isPersistent())
    {
        pOId = dbColl->getOId();
        return true;
    }
    return false;
}

bool MDDColl::getEOId(EOId &pEOId) const
{
    if (isPersistent())
    {
        pEOId = dbColl->getEOId();
        return true;
    }
    return false;
}

void MDDColl::insert(const MDDObj *newObj)
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

void MDDColl::releaseAll()
{
    LDEBUG << "release all MDD objects in coll " << getName();
    MDDObj *tempMDD = nullptr;
    while (!mddCache.empty())
    {
        tempMDD = (*mddCache.begin()).second;
        (*mddCache.begin()).second = nullptr;
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
    // else released by release transfer structures
}

MDDColl::MDDColl(const DBMDDSetId &coll) : dbColl(coll) {}

DBMDDSetId MDDColl::getDBMDDSet() const
{
    return dbColl;
}

void MDDColl::insertIntoCache(const MDDObj *objToInsert) const
{
    LTRACE << "insertIntoCache(" << (r_Ptr)objToInsert << ")";
    mddCache[objToInsert->getDBMDDObjId().ptr()] =
        const_cast<MDDObj *>(objToInsert);
}

MDDObj *MDDColl::getMDDObj(const DBMDDObj *objToGet) const
{
    MDDObj *persMDDObjToGet = nullptr;
    MDDObjMap::const_iterator i = mddCache.find(const_cast<DBMDDObj *>(objToGet));
    if (i != mddCache.end())
    {
        persMDDObjToGet = static_cast<MDDObj *>((*i).second);
    }
    else
    {
        persMDDObjToGet = new MDDObj(const_cast<DBMDDObj *>(objToGet));
        insertIntoCache(persMDDObjToGet);
    }
    return persMDDObjToGet;
}

bool MDDColl::isPersistent() const
{
    return dbColl->isPersistent();
}

void MDDColl::printStatus(unsigned int level, std::ostream &stream) const
{
    dbColl->printStatus(level, stream);
    auto *indent = new char[level * 2 + 1];
    for (unsigned int j = 0; j < level * 2; j++)
    {
        indent[j] = ' ';
    }
    indent[level * 2] = '\0';
    stream << indent;
    for (auto i = mddCache.begin(); i != mddCache.end(); i++)
    {
        stream << (r_Ptr)(*i).second;
    }
    delete[] indent;
    indent = nullptr;
}

MDDCollIter *MDDColl::createIterator() const
{
    auto *iter = new MDDCollIter(const_cast<MDDColl *>(this));
    return iter;
}

void MDDColl::remove(const MDDObj *obj)
{
    if (obj != nullptr)
    {
        DBMDDObjId t2 = obj->getDBMDDObjId();
        dbColl->remove(t2);

        // remove from cache ;(
        auto i = mddCache.find(t2.ptr());
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

void MDDColl::removeAll()
{
    dbColl->removeAll();
}

const char *MDDColl::AllCollectionnamesName = "RAS_COLLECTIONNAMES";
const char *MDDColl::AllStructTypesName = "RAS_STRUCT_TYPES";
const char *MDDColl::AllMarrayTypesName = "RAS_MARRAY_TYPES";
const char *MDDColl::AllSetTypesName = "RAS_SET_TYPES";
const char *MDDColl::AllTypesName = "RAS_TYPES";

MDDColl *MDDColl::createMDDCollection(const char *name,
                                      const CollectionType *ct)
{
    if (name == nullptr)
    {
        LTRACE << "createMDDColl(NULL, colltype)";
        throw r_Error(r_Error::r_Error_NameNotUnique);
    }
    if (ct == nullptr)
    {
        LTRACE << "createMDDColl(" << name << ", NULL)";
        throw r_Error(COLLTYPE_NULL);
    }
    if (!ct->isPersistent())
    {
        r_Error t(RASTYPEUNKNOWN);
        t.setTextParameter("type", ct->getName());
        LTRACE << "createMDDColl(" << name << ", " << ct->getName() << " not persistent)";
        throw t;
    }
    // may generate an exception:
    DBMDDSetId newDBColl = new DBMDDSet(name, ct);
    return new MDDColl(newDBColl);
}

MDDColl *MDDColl::createMDDCollection(const char *name, const OId &o,
                                      const CollectionType *ct)
{
    // may generate an exception:
    if (name == nullptr)
    {
        LTRACE << "createMDDColl(NULL, " << o << ", colltype)";
        throw r_Error(r_Error::r_Error_NameNotUnique);
    }
    if (ct == nullptr)
    {
        LTRACE << "createMDDColl(" << name << ", " << o << ", NULL)";
        throw r_Error(COLLTYPE_NULL);
    }
    if (!ct->isPersistent())
    {
        r_Error t(RASTYPEUNKNOWN);
        t.setTextParameter("type", ct->getName());
        LTRACE << "createMDDColl(" << name << ", " << o << ", " << ct->getName()
               << " not persistent)";
        throw t;
    }
    DBMDDSetId newDBColl = new DBMDDSet(name, o, ct);
    return new MDDColl(newDBColl);
}

bool MDDColl::dropMDDCollection(const char *name)
{
    return DBMDDSet::deleteDBMDDSet(name);
}

bool MDDColl::dropMDDCollection(const OId &o)
{
    return DBMDDSet::deleteDBMDDSet(o);
}

MDDColl *MDDColl::getMDDCollection(const OId &collOId)
{
    DBMDDSetId t(collOId);
    // this will throw an exception
    t->isModified();
    auto *retval = new MDDColl(t);
    return retval;
}

bool MDDColl::isVirtual(const char *collName)
{
    return strcmp(collName, AllCollectionnamesName) == 0 ||
           strcmp(collName, AllStructTypesName) == 0 ||
           strcmp(collName, AllMarrayTypesName) == 0 ||
           strcmp(collName, AllSetTypesName) == 0 ||
           strcmp(collName, AllTypesName) == 0;
}

bool MDDColl::isVirtual(const std::string &collName)
{
    static std::unordered_set<std::string> virtualColls{
            "RAS_COLLECTIONNAMES", "RAS_STRUCT_TYPES", "RAS_MARRAY_TYPES",
            "RAS_SET_TYPES", "RAS_TYPES"};
    return virtualColls.count(collName) > 0;
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
        {
            return false;
        }
        else
        {
            throw;
        }
    }
}

std::vector<std::string> MDDColl::getVirtualCollection(const char *collName)
{
    std::vector<std::string> ret;

    if (strcmp(collName, AllCollectionnamesName) == 0)
    {
        std::unique_ptr<OIdSet> list(ObjectBroker::getAllObjects(OId::MDDCOLLOID));
        ret.reserve(list->size());
        for (const auto &tmpdbset : *list)
        {
            const auto dbmddsetId = static_cast<DBMDDSetId>(tmpdbset);
            ret.emplace_back(dbmddsetId->getName());
        }
        list->clear();
    }
    else if (strcmp(collName, AllStructTypesName) == 0)
    {
        DBObjectIterator<StructType> structIter = TypeFactory::createStructIter();
        while (structIter.not_done())
        {
            StructType *typePtr = structIter.get_element();
            if (!common::StringUtil::startsWithExactCase(
                    typePtr->getTypeName(), TypeFactory::ANONYMOUS_CELL_TYPE_PREFIX))
            {
                char *tmpTypeStructure = typePtr->getNewTypeStructure();
                std::string typeStructure{tmpTypeStructure};
                free(tmpTypeStructure);

                std::string result = "";
                result.append("CREATE TYPE ");
                result.append(typePtr->getTypeName());
                result.append(" AS ");
                result.append(typeStructure);
                ret.push_back(result);
            }
            structIter.advance();
        }
    }
    else if (strcmp(collName, AllMarrayTypesName) == 0)
    {
        DBObjectIterator<MDDType> mddIter = TypeFactory::createMDDIter();

        while (mddIter.not_done())
        {
            MDDType *typePtr = mddIter.get_element();
            char *tmpTypeStructure = typePtr->getNewTypeStructure();
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
            ret.push_back(result);

            mddIter.advance();
        }
    }
    else if (strcmp(collName, AllSetTypesName) == 0)
    {
        DBObjectIterator<SetType> it = TypeFactory::createSetIter();
        while (it.not_done())
        {
            SetType *typePtr = it.get_element();

            std::string result = "";
            result.append("CREATE TYPE ");
            result.append(typePtr->getTypeName());
            result.append(" AS SET (");
            result.append(typePtr->getMDDType()->getTypeName());
            DBNullvalues *nullValues = typePtr->getNullValues();
            if (nullValues)
            {
                result.append(" NULL VALUES ");
                result.append(nullValues->toString());
            }
            result.append(")");
            ret.push_back(result);

            it.advance();
        }
    }
    else if (strcmp(collName, AllTypesName) == 0)
    {
        common::VectorUtils::append(getVirtualCollection(AllStructTypesName), ret);
        common::VectorUtils::append(getVirtualCollection(AllMarrayTypesName), ret);
        common::VectorUtils::append(getVirtualCollection(AllSetTypesName), ret);
    }

    return ret;
}

MDDColl *MDDColl::getMDDCollection(const char *collName)
{
    MDDColl *retval = nullptr;
    DBMDDSetId dbset;
    if (strcmp(collName, AllCollectionnamesName) == 0)
    {
        // the domains are required because rasql returns only arrays
        // since our result is a string it can be returned as a 1D char array
        r_Minterval transDomain("[0:*]");
        r_Minterval nameDomain("[0:0]");
        const BaseType *bt = TypeFactory::mapType("Char");
        auto *mt = new MDDDomainType(MOCK_MDD_COLLECTION_NAME, bt, transDomain);
        TypeFactory::addTempType(mt);
        CollectionType *ct = new SetType(MOCK_SET_COLLECTION_NAME, mt);
        TypeFactory::addTempType(ct);
        retval = new MDDColl(ct, AllCollectionnamesName);
        OIdSet *list = ObjectBroker::getAllObjects(OId::MDDCOLLOID);
        MDDObj *transObj = nullptr;
        std::shared_ptr<Tile> transTile;
        const char *nameBuffer = nullptr;
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
        const BaseType *bt = TypeFactory::mapType(CharType::Name);
        auto *mt = new MDDDomainType(MOCK_MDD_COLLECTION_NAME, bt, transDomain);
        TypeFactory::addTempType(mt);
        CollectionType *ct = new SetType(MOCK_SET_COLLECTION_NAME, mt);
        TypeFactory::addTempType(ct);
        retval = new MDDColl(ct, AllStructTypesName);

        DBObjectIterator<StructType> structIter = TypeFactory::createStructIter();
        MDDObj *transObj = nullptr;
        Tile *transTile = nullptr;

        while (structIter.not_done())
        {
            StructType *typePtr = NULL;
            try
            {
                typePtr = structIter.get_element();
            }
            catch (const r_Ebase_dbms &e)
            {
                LWARNING << "Failed reading struct type: " << e.what();
                structIter.advance();
                continue;
            }

            char *typeStructure = typePtr->getNewTypeStructure();

            if (!common::StringUtil::startsWithExactCase(typePtr->getTypeName(), TypeFactory::ANONYMOUS_CELL_TYPE_PREFIX))
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
        const BaseType *bt = TypeFactory::mapType(CharType::Name);
        auto *mt = new MDDDomainType(MOCK_MDD_COLLECTION_NAME, bt, transDomain);
        TypeFactory::addTempType(mt);
        CollectionType *ct = new SetType(MOCK_SET_COLLECTION_NAME, mt);
        TypeFactory::addTempType(ct);
        retval = new MDDColl(ct, AllMarrayTypesName);

        MDDObj *transObj = nullptr;
        Tile *transTile = nullptr;

        DBObjectIterator<MDDType> mddIter = TypeFactory::createMDDIter();

        while (mddIter.not_done())
        {
            MDDType *typePtr = NULL;
            try
            {
                typePtr = mddIter.get_element();
            }
            catch (const r_Ebase_dbms &e)
            {
                LWARNING << "Failed reading marray type: " << e.what();
                mddIter.advance();
                continue;
            }

            char *tmpTypeStructure = typePtr->getNewTypeStructure();
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
        const BaseType *bt = TypeFactory::mapType(CharType::Name);
        auto *mt = new MDDDomainType(MOCK_MDD_COLLECTION_NAME, bt, transDomain);
        TypeFactory::addTempType(mt);
        CollectionType *ct = new SetType(MOCK_SET_COLLECTION_NAME, mt);
        TypeFactory::addTempType(ct);
        retval = new MDDColl(ct, AllSetTypesName);

        DBObjectIterator<StructType> structIter = TypeFactory::createStructIter();
        MDDObj *transObj = nullptr;
        Tile *transTile = nullptr;

        DBObjectIterator<SetType> setIter = TypeFactory::createSetIter();
        while (setIter.not_done())
        {
            SetType *typePtr = NULL;
            try
            {
                typePtr = setIter.get_element();
            }
            catch (const r_Ebase_dbms &e)
            {
                LWARNING << "Failed reading set type: " << e.what();
                setIter.advance();
                continue;
            }

            std::string result = "";
            result.append("CREATE TYPE ");
            result.append(typePtr->getTypeName());
            result.append(" AS SET (");
            result.append(typePtr->getMDDType()->getTypeName());

            DBNullvalues *nullValues = typePtr->getNullValues();
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
    else if (strcmp(collName,AllTypesName)==0)
    {
        r_Minterval transDomain("[0:*]");
        r_Minterval nameDomain("[0:0]");
        const BaseType *bt = TypeFactory::mapType(CharType::Name);
        auto *mt = new MDDDomainType(MOCK_MDD_COLLECTION_NAME, bt, transDomain);
        TypeFactory::addTempType(mt);
        CollectionType *ct = new SetType(MOCK_SET_COLLECTION_NAME, mt);
        TypeFactory::addTempType(ct);
        retval = new MDDColl(ct, AllTypesName);
        auto typesRet = getVirtualCollection(AllTypesName);
        MDDObj *transObj = nullptr;
        Tile *transTile = nullptr;
        
        while (!typesRet.empty())
        {
            auto result = typesRet.back();
            typesRet.pop_back();

            nameDomain[0].set_high(static_cast<r_Range>(result.length()));
            transObj = new MDDObj(mt, nameDomain);
            transTile = new Tile(nameDomain, bt, result.c_str(), (r_Bytes)0, r_Array);
            transObj->insertTile(transTile);
            retval->insert(transObj);
        }
    }
    else
    {
        dbset = DBMDDSet::getDBMDDSet(collName);
        retval = new MDDColl(dbset);
    }
    return retval;
}

bool MDDColl::removeMDDObject(const OId &collOId, const OId &mddOId)
{
    bool retval = true;
    DBMDDSetId coll(collOId);
    DBMDDObjId mdd(mddOId);

    if (coll.is_null())
    {
        // does not exist
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

