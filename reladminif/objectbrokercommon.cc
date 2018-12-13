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
#include "config.h"
#include <map>
#include <set>
#include <cstring>
#include <cstdlib>
#include <cassert>
#include <malloc.h>

#include "raslib/minterval.hh"
#include "objectbroker.hh"
#include "dbnamedobject.hh"
#include "relstorageif/dbstoragelayout.hh"
#include "adminif.hh"
#include "relcatalogif/alltypes.hh"
#include "relindexif/hierindex.hh"
#include "relblobif/blobtile.hh"
#include "relcatalogif/dbminterval.hh"
#include "relcatalogif/dbnullvalues.hh"
#include "relblobif/inlinetile.hh"
#include "relindexif/dbtcindex.hh"
#include "sqlerror.hh"
#include "relindexif/indexid.hh"
#include "relmddif/mddid.hh"
#include "dbref.hh"
#include "relmddif/dbmddobj.hh"
#include "catalogmgr/typefactory.hh"
#include "relmddif/dbmddset.hh"
#include "relindexif/dbrcindexds.hh"
#include "debug.hh"
#include <logging.hh>

#ifdef LINUX
template class DBRef<BLOBTile>;
template class DBRef<DBTile>;
template class DBRef<InlineTile>;
#endif

using namespace std;

OId::OIdType
ObjectBroker::clearingObjectsOfType = OId::INVALID;

LongType*
ObjectBroker::theLong = 0;

ShortType*
ObjectBroker::theShort = 0;

OctetType*
ObjectBroker::theOctet = 0;

ULongType*
ObjectBroker::theULong = 0;

UShortType*
ObjectBroker::theUShort = 0;

CharType*
ObjectBroker::theChar = 0;

BoolType*
ObjectBroker::theBool = 0;

DoubleType*
ObjectBroker::theDouble = 0;

FloatType*
ObjectBroker::theFloat = 0;

ComplexType1*
ObjectBroker::theComplex1 = 0;

ComplexType2*
ObjectBroker::theComplex2 = 0;

DBObjectPMap
ObjectBroker::theAtomicTypes;

DBObjectPMap
ObjectBroker::theSetTypes;

DBObjectPMap
ObjectBroker::theMDDTypes;

DBObjectPMap
ObjectBroker::theMDDBaseTypes;

DBObjectPMap
ObjectBroker::theMDDDimensionTypes;

DBObjectPMap
ObjectBroker::theMDDDomainTypes;

DBObjectPMap
ObjectBroker::theStructTypes;

DBObjectPMap
ObjectBroker::theDBMintervals;

DBObjectPMap
ObjectBroker::theDBNullvalues;

DBObjectPMap
ObjectBroker::theDBMDDObjs;

DBObjectPMap
ObjectBroker::theMDDSets;

DBObjectPMap
ObjectBroker::theDBStorages;

DBObjectPMap
ObjectBroker::theDBHierIndexs;

DBObjectPMap
ObjectBroker::theDBTCIndexs;

DBObjectPMap
ObjectBroker::theBLOBTiles;

DBObjectPMap
ObjectBroker::theInlineTiles;

DBObjectPMap
ObjectBroker::theRCIndexes;

OIdMap
ObjectBroker::theTileIndexMappings;

bool
ObjectBroker::freeMemory()
{
    LDEBUG << "memory overflow: attempting to remove a blob tile to free memory.";
    bool retval = false;
    DBRef<BLOBTile>::setPointerCaching(false);
    DBRef<DBTile>::setPointerCaching(false);
    DBRef<InlineTile>::setPointerCaching(false);
    if (!ObjectBroker::theBLOBTiles.empty())
    {
        int indexToDelete = ObjectBroker::theBLOBTiles.size() / 2;
        DBObjectPMap::iterator it = ObjectBroker::theBLOBTiles.begin();
        std::advance(it, indexToDelete);
        delete(*it).second;
        retval = true;
    }
    return retval;
}

void
ObjectBroker::init()
{
    LDEBUG << "initializing object caches";
    ObjectBroker::theLong = new LongType();
    ObjectBroker::theShort = new ShortType();
    ObjectBroker::theOctet = new OctetType();
    ObjectBroker::theULong = new ULongType();
    ObjectBroker::theUShort = new UShortType();
    ObjectBroker::theChar = new CharType();
    ObjectBroker::theBool = new BoolType();
    ObjectBroker::theDouble = new DoubleType();
    ObjectBroker::theFloat = new FloatType();
    ObjectBroker::theComplex1 = new ComplexType1();
    ObjectBroker::theComplex2 = new ComplexType2();

    DBObject *atomicTypes[] =
        {theComplex2, theComplex1, theFloat, theDouble, theOctet, theShort,
         theLong, theUShort, theBool, theChar, theULong};
#ifdef DEBUG
    if (sizeof(atomicTypes) / sizeof(DBObject*) != TypeFactory::MaxBuiltInId)
    {
        LERROR << "ObjectBroker::init() not all atomic types were added!";
        exit(1);
    }
#endif
    for (unsigned int a = 0; a < sizeof(atomicTypes) / sizeof(DBObject*); a++)
    {
        DBObjectPPair myPair(atomicTypes[a]->getOId(), atomicTypes[a]);
        theAtomicTypes.insert(myPair);
    }
}

void
ObjectBroker::deinit()
{
    LTRACE << "deinitializing object caches";
    delete ObjectBroker::theLong; ObjectBroker::theLong = NULL;
    delete ObjectBroker::theShort; ObjectBroker::theShort = NULL;
    delete ObjectBroker::theOctet; ObjectBroker::theOctet = NULL;
    delete ObjectBroker::theULong; ObjectBroker::theULong = NULL;
    delete ObjectBroker::theUShort; ObjectBroker::theUShort = NULL;
    delete ObjectBroker::theChar; ObjectBroker::theChar = NULL;
    delete ObjectBroker::theBool; ObjectBroker::theBool = NULL;
    delete ObjectBroker::theDouble; ObjectBroker::theDouble = NULL;
    delete ObjectBroker::theFloat; ObjectBroker::theFloat = NULL;
    delete ObjectBroker::theComplex1; ObjectBroker::theComplex1 = NULL;
    delete ObjectBroker::theComplex2; ObjectBroker::theComplex2 = NULL;

    // clear maps and other datastructures, in order from "simplest" to more complex objects
    theAtomicTypes.clear();
    theStructTypes.clear();
    theSetTypes.clear();
    theDBMintervals.clear();
    theDBNullvalues.clear();

    theMDDTypes.clear();
    theMDDBaseTypes.clear();
    theMDDDimensionTypes.clear();
    theMDDDomainTypes.clear();

    theBLOBTiles.clear();
    theInlineTiles.clear();

    theDBMDDObjs.clear();
    theMDDSets.clear();

    theDBStorages.clear();
    theDBHierIndexs.clear();
    theDBTCIndexs.clear();
    theRCIndexes.clear();
    theTileIndexMappings.clear();
}

DBObject*
ObjectBroker::getObjectByOId(const OId& id)
{
    if (id.getType() == OId::INVALID)
        return NULL;                // invalid OId, can't return any valid object

    auto *cached = ObjectBroker::isInMemory(id);
    if (cached)
        return cached;              // return already cached
    else
        return loadObjectByOId(id); // not cached, have to load it from the database
}

DBObject*
ObjectBroker::isInMemory(const OId& id)
{
    assert(id.getType() != OId::INVALID);

    DBObject* retval = 0;
    DBObjectPMap& theMap = ObjectBroker::getMap(id.getType());
    auto i = theMap.find(id);
    if (i != theMap.end())
    {
        retval = (*i).second;
        LTRACE << "object with id " << id << " is cached in memory.";
    }
    else
    {
        LTRACE << "object with id " << id << " is not cached in memory.";
    }
    return retval;
}

DBObject*
ObjectBroker::loadObjectByOId(const OId& id)
{
    assert(id.getType() != OId::INVALID);

    // Will iterate as long as:
    //  1. object is successfully loaded and returned
    //  2. object failed loading due to insufficient memory, and it was possible to free some of the cached memory
    //
    // Bottom line: this depends on ObjectBroker::freeMemory() working properly, i.e. returning true only when it
    //              really managed to remove some cached object. So long as that is correct, the loop will terminate,
    //              as there is a finite number of cached objects.
    while (true)
    {
        try
        {
            // the cases exit the function, either via return or through an exception
            switch (id.getType())
            {
                case OId::MDDOID:         return loadDBMDDObj(id);
                case OId::MDDCOLLOID:     return loadMDDSet(id);
                case OId::MDDTYPEOID:     return loadMDDType(id);
                case OId::MDDBASETYPEOID: return loadMDDBaseType(id);
                case OId::MDDDIMTYPEOID:  return loadMDDDimensionType(id);
                case OId::MDDDOMTYPEOID:  return loadMDDDomainType(id);
                case OId::STRUCTTYPEOID:  return loadStructType(id);
                case OId::SETTYPEOID:     return loadSetType(id);
                case OId::BLOBOID:        return loadBLOBTile(id);
                case OId::DBMINTERVALOID: return loadDBMinterval(id);
                case OId::DBNULLVALUESOID:return loadDBNullvalues(id);
                case OId::STORAGEOID:     return loadDBStorage(id);
                case OId::MDDHIERIXOID:   return loadDBHierIndex(id);
                case OId::DBTCINDEXOID:   return loadDBTCIndex(id);
                case OId::INLINETILEOID:  return loadInlineTile(id);
                case OId::MDDRCIXOID:     return loadDBRCIndexDS(id);
                case OId::ATOMICTYPEOID:  LERROR << "Atomic type not found in memory.";
                default:
                {
                    LERROR << "Retrival of object failed, invalid object type: " << id.getType();
                    throw r_Error(INVALID_OIDTYPE);
                }
            }
        }
        catch (const std::bad_alloc &ex)
        {
            // failed allocating memory: free some, and if successful retry loading again
            if (!ObjectBroker::freeMemory())
            {
                LERROR << "Not enough memory to load object with OId " << id << "; freeing all cached memory did not help.";
                throw ex;
            }
            else
            {
                // it's good to have this here as a debug statement, in case freeMemory() is faulty this will help
                // catch the infinite loop
                LDEBUG << "Not enough memory to load object with OId " << id << "; successfully freed some cached memory, retrying.";
            }
        }
    }
}

void
ObjectBroker::registerDBObject(DBObject* obj)
{
    DBObjectPMap& t = ObjectBroker::getMap(obj->getOId().getType());
    t.emplace(obj->getOId(), obj);
}

void
ObjectBroker::deregisterDBObject(const OId& id)
{
    if (id.getType() != OId::INVALID && id.getType() != clearingObjectsOfType)
    {
        DBObjectPMap& t = ObjectBroker::getMap(id.getType());
        auto i = t.find(id);
        if (i != t.end())
        {
            (*i).second = 0;
            i = t.erase(i);
        }
    }
}

OIdSet*
ObjectBroker::getAllObjects(OId::OIdType type)
{
    switch (type)
    {
    case OId::MDDCOLLOID:     return getAllMDDSets();
    case OId::MDDOID:         return getAllMDDObjects();
    case OId::MDDTYPEOID:     return getAllMDDTypes();
    case OId::MDDBASETYPEOID: return getAllMDDBaseTypes();
    case OId::MDDDIMTYPEOID:  return getAllMDDDimensionTypes();
    case OId::MDDDOMTYPEOID:  return getAllMDDDomainTypes();
    case OId::STRUCTTYPEOID:  return getAllStructTypes();
    case OId::SETTYPEOID:     return getAllSetTypes();
    case OId::ATOMICTYPEOID:  return getAllAtomicTypes();
    default: {
        LERROR << "Retrival of all cached objects of type " << type << " failed: invalid OId type.";
        throw r_Error(INVALID_OIDTYPE);
    }
    }
}


OId
ObjectBroker::getOIdByName(OId::OIdType type, const char* name)
{
    switch (type)
    {
    case OId::MDDCOLLOID:     return getOIdOfMDDSet(name);
    case OId::MDDTYPEOID:     return getOIdOfMDDType(name);
    case OId::MDDBASETYPEOID: return getOIdOfMDDBaseType(name);
    case OId::MDDDIMTYPEOID:  return getOIdOfMDDDimensionType(name);
    case OId::MDDDOMTYPEOID:  return getOIdOfMDDDomainType(name);
    case OId::SETTYPEOID:     return getOIdOfSetType(name);
    case OId::STRUCTTYPEOID:  return getOIdOfStructType(name);
    case OId::ATOMICTYPEOID:
        if (strcmp(name, ULongType::Name) == 0)
            return theULong->getOId();
        else if (strcmp(name, BoolType::Name) == 0)
            return theBool->getOId();
        else if (strcmp(name, CharType::Name) == 0)
            return theChar->getOId();
        else if (strcmp(name, UShortType::Name) == 0)
            return theUShort->getOId();
        else if (strcmp(name, LongType::Name) == 0)
            return theLong->getOId();
        else if (strcmp(name, ShortType::Name) == 0)
            return theShort->getOId();
        else if (strcmp(name, OctetType::Name) == 0)
            return theOctet->getOId();
        else if (strcmp(name, DoubleType::Name) == 0)
            return theDouble->getOId();
        else if (strcmp(name, FloatType::Name) == 0)
            return theFloat->getOId();
        else if (strcmp(name, ComplexType1::Name) == 0)
            return theComplex1->getOId();
        else if (strcmp(name, ComplexType2::Name) == 0)
            return theComplex2->getOId();
        else
            return OId(); // invalid OId
    default:
        LERROR << "Retrival of OId of type " << type << " and name '" << name << "' failed: invalid OId type.";
        throw r_Error(INVALID_OIDTYPE);
    }
}


DBObject*
ObjectBroker::getObjectByName(OId::OIdType type, const char* name)
{
    DBObjectPMap* theMap = 0;
    switch (type)
    {
    case OId::MDDCOLLOID:     theMap = &theMDDSets; break;
    case OId::MDDTYPEOID:     theMap = &theMDDTypes; break;
    case OId::MDDBASETYPEOID: theMap = &theMDDBaseTypes; break;
    case OId::MDDDIMTYPEOID:  theMap = &theMDDDimensionTypes; break;
    case OId::MDDDOMTYPEOID:  theMap = &theMDDDomainTypes; break;
    case OId::STRUCTTYPEOID:  theMap = &theStructTypes; break;
    case OId::SETTYPEOID:     theMap = &theSetTypes; break;
    case OId::ATOMICTYPEOID:  theMap = &theAtomicTypes; break;
    default:
        LERROR << "Retrival of object of type " << type << " and name '" << name << "' failed: invalid OId type.";
        throw r_Error(INVALID_OIDTYPE);
    }

    // check if there is an object with that name already in memory
    // TODO: theMap should be a bidirectional map, this linear iteration mode is inefficient
    DBObject* retval = 0;
    for (DBObjectPMap::iterator iter = theMap->begin(); iter != theMap->end(); iter++)
    {
        if (strcmp((static_cast<DBNamedObject*>((*iter).second))->getName(), name) == 0)
        {
            retval = (*iter).second;
            break;
        }
    }

    // no matching object. try loading from db
    if (!retval)
    {
        retval = ObjectBroker::getObjectByOId(ObjectBroker::getOIdByName(type, name));
    }
    return retval;
}

void
ObjectBroker::clearMap(DBObjectPMap& theMap)
{
    if (theMap.size() > 0)
    {
        auto it = theMap.begin();
        DBObject *obj = (*it).second;
        if (obj)
        {
            // this prevents deregisterObject from modifying the map
            // while it's being modified below
            clearingObjectsOfType = obj->getOId().getType();
        }
    }
    for (auto &p: theMap)
    {
        delete p.second;
    }
    theMap.clear();
    clearingObjectsOfType = OId::INVALID;
}

void
ObjectBroker::validateMap(DBObjectPMap& theMap)
{
    for (auto &p: theMap)
        p.second->validate();
}

void
ObjectBroker::clearBroker()
{
    //do not ever clear the ATOMICTYPEOID map! those are on the stack, not heap!
    // Important: the order matters, start from most basic datastructures and progress to more complex ones.

    LDEBUG << "Clearing ObjectBroker...";
    static const std::vector<OId::OIdType> objTypes = {
            OId::STRUCTTYPEOID, OId::DBMINTERVALOID, OId::DBNULLVALUESOID,
            OId::MDDDOMTYPEOID, OId::MDDDIMTYPEOID, OId::MDDBASETYPEOID, OId::MDDTYPEOID, OId::SETTYPEOID,
            OId::BLOBOID,
            OId::INLINETILEOID, OId::STORAGEOID,
            OId::MDDOID, OId::MDDCOLLOID,
            OId::MDDHIERIXOID, OId::MDDRCIXOID, OId::DBTCINDEXOID
    };

    // validate from smaller to upper
    for (auto objType: objTypes) {
        LTRACE << "Validating map for objects of type: " << objType;
        validateMap(getMap(objType));
    }
    // clear in reverse order
    for (auto it = objTypes.rbegin(); it != objTypes.rend(); ++it) {
        LTRACE << "Clearing map for objects of type: " << *it;
        clearMap(getMap(*it));
    }

    theTileIndexMappings.clear();

    // It seems that free() doesn't always fully release the memory.
    // The malloc_trim(0) below will attempt to release free memory from the top of the heap.
    malloc_trim(0);
    LDEBUG << "ObjectBroker cleared successfully.";
}

DBObjectPMap&
ObjectBroker::getMap(OId::OIdType type)
{
    switch (type)
    {
    case OId::MDDOID:         return theDBMDDObjs;
    case OId::MDDCOLLOID:     return theMDDSets;
    case OId::MDDTYPEOID:     return theMDDTypes;
    case OId::MDDBASETYPEOID: return theMDDBaseTypes;
    case OId::MDDDIMTYPEOID:  return theMDDDimensionTypes;
    case OId::MDDDOMTYPEOID:  return theMDDDomainTypes;
    case OId::STRUCTTYPEOID:  return theStructTypes;
    case OId::SETTYPEOID:     return theSetTypes;
    case OId::BLOBOID:        return theBLOBTiles;
    case OId::INLINETILEOID:  return theInlineTiles;
    case OId::DBMINTERVALOID: return theDBMintervals;
    case OId::DBNULLVALUESOID:return theDBNullvalues;
    case OId::STORAGEOID:     return theDBStorages;
    case OId::DBTCINDEXOID:   return theDBTCIndexs;
    case OId::MDDHIERIXOID:   return theDBHierIndexs;
    case OId::ATOMICTYPEOID:  return theAtomicTypes;
    case OId::MDDRCIXOID:     return theRCIndexes;
    default:
        LERROR << "Retrival of object of type " << type << " failed: invalid OId type.";
        throw r_Error(INVALID_OIDTYPE);
    }
}

DBObject*
ObjectBroker::loadDBStorage(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBStorageLayout(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadSetType(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new SetType(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadMDDType(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new MDDType(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadMDDBaseType(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new MDDBaseType(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadMDDDimensionType(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new MDDDimensionType(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadMDDDomainType(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new MDDDomainType(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadStructType(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new StructType(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadDBMinterval(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBMinterval(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadDBNullvalues(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBNullvalues(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadDBMDDObj(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBMDDObj(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadMDDSet(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBMDDSet(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadDBTCIndex(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBTCIndex(id));
    retval->setCached(true);
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadDBHierIndex(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBHierIndex(id));
    retval->setCached(true);
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadDBRCIndexDS(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBRCIndexDS(id));
    retval->setCached(true);
    registerDBObject(retval.get());
    return retval.release();
}

DBObject*
ObjectBroker::loadBLOBTile(const OId& id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new BLOBTile(id));
    registerDBObject(retval.get());
    return retval.release();
}

void
ObjectBroker::registerTileIndexMapping(const OId& tileoid, const OId& indexoid)
{
    theTileIndexMappings.emplace(tileoid, indexoid);
}

void
ObjectBroker::deregisterTileIndexMapping(const OId& tileoid, const OId& indexoid)
{
    auto i = theTileIndexMappings.find(tileoid);
    if (i != theTileIndexMappings.end())
    {
        theTileIndexMappings.erase(i);
    }
    else
    {
        LTRACE << "deregisterIndexTileMapping(" << indexoid << ", " << tileoid << ") NOT FOUND";
    }
}

OIdSet*
ObjectBroker::getAllAtomicTypes()
{
    OIdSet* retval = new OIdSet();
    DBObjectPMap& theMap = ObjectBroker::getMap(OId::ATOMICTYPEOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        LTRACE << "inserted from memory " << (*i).first;
        retval->insert((*i).first);
    }
    return retval;
}
