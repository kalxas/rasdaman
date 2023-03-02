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
#include "objectbroker.hh"                   // for ObjectBroker
#include "dbobject.hh"                       // for DBObject
#include "oidif.hh"                          // for OId, operator<<, OId:...
#include "dbnamedobject.hh"                  // for DBNamedObject
#include "dbref.hh"                          // for DBRef
#include "lists.h"                           // for DBObjectPMap, OIdMap
#include "relindexif/dbrcindexds.hh"         // for DBRCIndexDS
#include "relindexif/dbtcindex.hh"           // for DBTCIndex
#include "relindexif/hierindex.hh"           // for DBHierIndex
#include "relmddif/dbmddobj.hh"              // for DBMDDObj
#include "relmddif/dbmddset.hh"              // for DBMDDSet
#include "relblobif/blobtile.hh"             // for BLOBTile
#include "relstorageif/dbstoragelayout.hh"   // for DBStorageLayout
#include "relcatalogif/dbminterval.hh"       // for DBMinterval
#include "relcatalogif/booltype.hh"          // for BoolType, BoolType::Name
#include "relcatalogif/chartype.hh"          // for CharType, CharType::Name
#include "relcatalogif/complextype.hh"       // for ComplexType1, Complex...
#include "relcatalogif/dbnullvalues.hh"      // for DBNullvalues
#include "relcatalogif/doubletype.hh"        // for DoubleType, DoubleTyp...
#include "relcatalogif/floattype.hh"         // for FloatType, FloatType:...
#include "relcatalogif/longtype.hh"          // for LongType, LongType::Name
#include "relcatalogif/mddbasetype.hh"       // for MDDBaseType
#include "relcatalogif/mdddimensiontype.hh"  // for MDDDimensionType
#include "relcatalogif/mdddomaintype.hh"     // for MDDDomainType
#include "relcatalogif/mddtype.hh"           // for MDDType
#include "relcatalogif/octettype.hh"         // for OctetType, OctetType:...
#include "relcatalogif/settype.hh"           // for SetType
#include "relcatalogif/shorttype.hh"         // for ShortType, ShortType:...
#include "relcatalogif/structtype.hh"        // for StructType
#include "relcatalogif/ulongtype.hh"         // for ULongType, ULongType:...
#include "relcatalogif/ushorttype.hh"        // for UShortType, UShortTyp...
#include "raslib/error.hh"                   // for r_Error, INVALID_OIDTYPE
#include <logging.hh>                        // for Writer, CTRACE, CFATAL

#ifdef __APPLE__
#include <sys/malloc.h>
#else
#include <malloc.h>
#endif
#include <cassert>
#include <cstring>  // for strcmp
#include <set>      // for _Rb_tree_iterator, set
#include <utility>  // for pair
#include <vector>   // for vector

class DBTile;
class InlineTile;
#ifdef LINUX
template class DBRef<BLOBTile>;
template class DBRef<DBTile>;
template class DBRef<InlineTile>;
#endif

using namespace std;

OId::OIdType
    ObjectBroker::clearingObjectsOfType = OId::INVALID;

LongType *ObjectBroker::theLong = nullptr;
ShortType *ObjectBroker::theShort = nullptr;
OctetType *ObjectBroker::theOctet = nullptr;
ULongType *ObjectBroker::theULong = nullptr;
UShortType *ObjectBroker::theUShort = nullptr;
CharType *ObjectBroker::theChar = nullptr;
BoolType *ObjectBroker::theBool = nullptr;
DoubleType *ObjectBroker::theDouble = nullptr;
FloatType *ObjectBroker::theFloat = nullptr;
ComplexType1 *ObjectBroker::theComplex1 = nullptr;
ComplexType2 *ObjectBroker::theComplex2 = nullptr;
CInt16 *ObjectBroker::theCInt16 = nullptr;
CInt32 *ObjectBroker::theCInt32 = nullptr;

DBObjectPMap ObjectBroker::theAtomicTypes;
DBObjectPMap ObjectBroker::theSetTypes;
DBObjectPMap ObjectBroker::theMDDTypes;
DBObjectPMap ObjectBroker::theMDDBaseTypes;
DBObjectPMap ObjectBroker::theMDDDimensionTypes;
DBObjectPMap ObjectBroker::theMDDDomainTypes;
DBObjectPMap ObjectBroker::theStructTypes;
DBObjectPMap ObjectBroker::theDBMintervals;
DBObjectPMap ObjectBroker::theDBNullvalues;
DBObjectPMap ObjectBroker::theDBMDDObjs;
DBObjectPMap ObjectBroker::theMDDSets;
DBObjectPMap ObjectBroker::theDBStorages;
DBObjectPMap ObjectBroker::theDBHierIndexs;
DBObjectPMap ObjectBroker::theDBTCIndexs;
DBObjectPMap ObjectBroker::theBLOBTiles;
DBObjectPMap ObjectBroker::theInlineTiles;
DBObjectPMap ObjectBroker::theRCIndexes;
OIdMap ObjectBroker::theTileIndexMappings;

bool ObjectBroker::freeMemory()
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
        delete (*it).second;
        retval = true;
    }
    return retval;
}

void ObjectBroker::init()
{
    LDEBUG << "initializing object caches";
#define INIT_ATOMIC_TYPE(typeVar, typeClass) \
    ObjectBroker::typeVar = new typeClass(); \
    theAtomicTypes.emplace(ObjectBroker::typeVar->getOId(), ObjectBroker::typeVar);

    INIT_ATOMIC_TYPE(theLong, LongType)
    INIT_ATOMIC_TYPE(theShort, ShortType)
    INIT_ATOMIC_TYPE(theOctet, OctetType)
    INIT_ATOMIC_TYPE(theULong, ULongType)
    INIT_ATOMIC_TYPE(theUShort, UShortType)
    INIT_ATOMIC_TYPE(theChar, CharType)
    INIT_ATOMIC_TYPE(theBool, BoolType)
    INIT_ATOMIC_TYPE(theDouble, DoubleType)
    INIT_ATOMIC_TYPE(theFloat, FloatType)
    INIT_ATOMIC_TYPE(theComplex1, ComplexType1)
    INIT_ATOMIC_TYPE(theComplex2, ComplexType2)
    INIT_ATOMIC_TYPE(theCInt16, CInt16)
    INIT_ATOMIC_TYPE(theCInt32, CInt32)
}

void ObjectBroker::deinit()
{
    LDEBUG << "deinitializing object caches";
#define DEINIT_ATOMIC_TYPE(typeVar) \
    delete ObjectBroker::typeVar, ObjectBroker::typeVar = nullptr;

    DEINIT_ATOMIC_TYPE(theLong)
    DEINIT_ATOMIC_TYPE(theShort)
    DEINIT_ATOMIC_TYPE(theOctet)
    DEINIT_ATOMIC_TYPE(theULong)
    DEINIT_ATOMIC_TYPE(theUShort)
    DEINIT_ATOMIC_TYPE(theChar)
    DEINIT_ATOMIC_TYPE(theBool)
    DEINIT_ATOMIC_TYPE(theDouble)
    DEINIT_ATOMIC_TYPE(theFloat)
    DEINIT_ATOMIC_TYPE(theComplex1)
    DEINIT_ATOMIC_TYPE(theComplex2)
    DEINIT_ATOMIC_TYPE(theCInt16)
    DEINIT_ATOMIC_TYPE(theCInt32)

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

DBObject *ObjectBroker::getObjectByOId(const OId &id)
{
    if (id.getType() == OId::INVALID)
    {
        return nullptr;  // invalid OId, can't return any valid object
    }

    auto *cached = ObjectBroker::isInMemory(id);
    if (cached)
    {
        return cached;  // return already cached
    }
    else
    {
        return loadObjectByOId(id);  // not cached, have to load it from the database
    }
}

DBObject *ObjectBroker::isInMemory(const OId &id)
{
    assert(id.getType() != OId::INVALID);
    DBObject *retval = nullptr;
    DBObjectPMap &theMap = ObjectBroker::getMap(id.getType());
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

DBObject *
ObjectBroker::loadObjectByOId(const OId &id)
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
            case OId::MDDOID:
                return loadDBMDDObj(id);
            case OId::MDDCOLLOID:
                return loadMDDSet(id);
            case OId::MDDTYPEOID:
                return loadMDDType(id);
            case OId::MDDBASETYPEOID:
                return loadMDDBaseType(id);
            case OId::MDDDIMTYPEOID:
                return loadMDDDimensionType(id);
            case OId::MDDDOMTYPEOID:
                return loadMDDDomainType(id);
            case OId::STRUCTTYPEOID:
                return loadStructType(id);
            case OId::SETTYPEOID:
                return loadSetType(id);
            case OId::BLOBOID:
                return loadBLOBTile(id);
            case OId::DBMINTERVALOID:
                return loadDBMinterval(id);
            case OId::DBNULLVALUESOID:
                return loadDBNullvalues(id);
            case OId::STORAGEOID:
                return loadDBStorage(id);
            case OId::MDDHIERIXOID:
                return loadDBHierIndex(id);
            case OId::DBTCINDEXOID:
                return loadDBTCIndex(id);
            case OId::INLINETILEOID:
                return loadInlineTile(id);
            case OId::MDDRCIXOID:
                return loadDBRCIndexDS(id);
            case OId::ATOMICTYPEOID:
                LERROR << "Atomic type not found in memory.";
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
                throw;
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

void ObjectBroker::registerDBObject(DBObject *obj)
{
    DBObjectPPair myPair(obj->getOId(), obj);
    auto it = ObjectBroker::getMap(obj->getOId().getType()).insert(myPair);
    if (!it.second)
    {
        LDEBUG << "Object is already registered: " << obj->getOId();
    }
}

void ObjectBroker::deregisterDBObject(const OId &id)
{
    if (id.getType() != OId::INVALID && id.getType() != clearingObjectsOfType)
    {
        DBObjectPMap &t = ObjectBroker::getMap(id.getType());
        auto i = t.find(id);
        if (i != t.end())
        {
            (*i).second = nullptr;
            t.erase(i);
        }
    }
}

OIdSet *ObjectBroker::getAllObjects(OId::OIdType type)
{
    switch (type)
    {
    case OId::MDDCOLLOID:
        LDEBUG << "loading all MDD collections";
        return getAllMDDSets();
    case OId::MDDOID:
        LDEBUG << "loading all MDD objects";
        return getAllMDDObjects();
    case OId::MDDTYPEOID:
        LDEBUG << "loading all MDD types";
        return getAllMDDTypes();
    case OId::MDDBASETYPEOID:
        LDEBUG << "loading all MDD base types";
        return getAllMDDBaseTypes();
    case OId::MDDDIMTYPEOID:
        LDEBUG << "loading all MDD dimension types";
        return getAllMDDDimensionTypes();
    case OId::MDDDOMTYPEOID:
        LDEBUG << "loading all MDD domain types";
        return getAllMDDDomainTypes();
    case OId::STRUCTTYPEOID:
        LDEBUG << "loading all struct base types";
        return getAllStructTypes();
    case OId::SETTYPEOID:
        LDEBUG << "loading all collection types";
        return getAllSetTypes();
    case OId::ATOMICTYPEOID:
        LDEBUG << "loading all atomic base types";
        return getAllAtomicTypes();
    default:
        LERROR << "Retrival of all cached objects of type " << type << " failed: invalid OId type.";
        throw r_Error(INVALID_OIDTYPE);
    }
}

OId ObjectBroker::getOIdByName(OId::OIdType type, const char *name)
{
    switch (type)
    {
    case OId::MDDCOLLOID:
        return getOIdOfMDDSet(name);
    case OId::MDDTYPEOID:
        return getOIdOfMDDType(name);
    case OId::MDDBASETYPEOID:
        return getOIdOfMDDBaseType(name);
    case OId::MDDDIMTYPEOID:
        return getOIdOfMDDDimensionType(name);
    case OId::MDDDOMTYPEOID:
        return getOIdOfMDDDomainType(name);
    case OId::SETTYPEOID:
        return getOIdOfSetType(name);
    case OId::STRUCTTYPEOID:
        return getOIdOfStructType(name);
    case OId::ATOMICTYPEOID:
        if (strcmp(name, ULongType::Name) == 0)
        {
            return theULong->getOId();
        }
        else if (strcmp(name, BoolType::Name) == 0)
        {
            return theBool->getOId();
        }
        else if (strcmp(name, CharType::Name) == 0)
        {
            return theChar->getOId();
        }
        else if (strcmp(name, UShortType::Name) == 0)
        {
            return theUShort->getOId();
        }
        else if (strcmp(name, LongType::Name) == 0)
        {
            return theLong->getOId();
        }
        else if (strcmp(name, ShortType::Name) == 0)
        {
            return theShort->getOId();
        }
        else if (strcmp(name, OctetType::Name) == 0)
        {
            return theOctet->getOId();
        }
        else if (strcmp(name, DoubleType::Name) == 0)
        {
            return theDouble->getOId();
        }
        else if (strcmp(name, FloatType::Name) == 0)
        {
            return theFloat->getOId();
        }
        else if (strcmp(name, ComplexType1::Name) == 0)
        {
            return theComplex1->getOId();
        }
        else if (strcmp(name, ComplexType2::Name) == 0)
        {
            return theComplex2->getOId();
        }
        else if (strcmp(name, CInt16::Name) == 0)
        {
            return theCInt16->getOId();
        }
        else if (strcmp(name, CInt32::Name) == 0)
        {
            return theCInt32->getOId();
        }
        else
        {
            return OId();  // invalid OId
        }
    default:
        LERROR << "Retrival of OId of type " << type << " and name '" << name << "' failed: invalid OId type.";
        throw r_Error(INVALID_OIDTYPE);
    }
}

DBObject *
ObjectBroker::getObjectByName(OId::OIdType type, const char *name)
{
    DBObjectPMap *theMap = 0;
    switch (type)
    {
    case OId::MDDCOLLOID:
        theMap = &theMDDSets;
        break;
    case OId::MDDTYPEOID:
        theMap = &theMDDTypes;
        break;
    case OId::MDDBASETYPEOID:
        theMap = &theMDDBaseTypes;
        break;
    case OId::MDDDIMTYPEOID:
        theMap = &theMDDDimensionTypes;
        break;
    case OId::MDDDOMTYPEOID:
        theMap = &theMDDDomainTypes;
        break;
    case OId::STRUCTTYPEOID:
        theMap = &theStructTypes;
        break;
    case OId::SETTYPEOID:
        theMap = &theSetTypes;
        break;
    case OId::ATOMICTYPEOID:
        theMap = &theAtomicTypes;
        break;
    default:
        LERROR << "Retrival of object of type " << type << " and name '" << name
               << "' failed: invalid OId type.";
        throw r_Error(INVALID_OIDTYPE);
    }

    // check if there is an object with that name already in memory
    // TODO: theMap should be a bidirectional map, this linear iteration mode is inefficient
    DBObject *retval = 0;
    for (auto iter = theMap->begin(); iter != theMap->end(); iter++)
    {
        const char *objName = static_cast<DBNamedObject *>(iter->second)->getName();
        if (objName && strcmp(objName, name) == 0)
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

void ObjectBroker::clearMap(DBObjectPMap &theMap)
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

void ObjectBroker::validateMap(DBObjectPMap &theMap)
{
    for (auto &p: theMap)
    {
        p.second->validate();
    }
}

void ObjectBroker::clearBroker()
{
    // do not ever clear the ATOMICTYPEOID map! those are on the stack, not heap!
    // Important: the order matters, start from most basic datastructures and progress to more complex ones.

    LDEBUG << "Clearing ObjectBroker...";
    static const std::vector<OId::OIdType> objTypes =
        {
            OId::STRUCTTYPEOID, OId::DBMINTERVALOID, OId::DBNULLVALUESOID,
            OId::MDDDOMTYPEOID, OId::MDDDIMTYPEOID, OId::MDDBASETYPEOID, OId::MDDTYPEOID, OId::SETTYPEOID,
            OId::BLOBOID,
            OId::INLINETILEOID, OId::STORAGEOID,
            OId::MDDOID, OId::MDDCOLLOID,
            OId::MDDHIERIXOID, OId::MDDRCIXOID, OId::DBTCINDEXOID};

    // validate from smaller to upper
    for (auto objType: objTypes)
    {
        LTRACE << "Validating map for objects of type: " << objType;
        validateMap(getMap(objType));
    }
    // clear in reverse order
    for (auto it = objTypes.rbegin(); it != objTypes.rend(); ++it)
    {
        LTRACE << "Clearing map for objects of type: " << *it;
        clearMap(getMap(*it));
    }

    theTileIndexMappings.clear();

    LDEBUG << "ObjectBroker cleared successfully.";
}

DBObjectPMap &ObjectBroker::getMap(OId::OIdType type)
{
    switch (type)
    {
    case OId::MDDOID:
        return theDBMDDObjs;
    case OId::MDDCOLLOID:
        return theMDDSets;
    case OId::MDDTYPEOID:
        return theMDDTypes;
    case OId::MDDBASETYPEOID:
        return theMDDBaseTypes;
    case OId::MDDDIMTYPEOID:
        return theMDDDimensionTypes;
    case OId::MDDDOMTYPEOID:
        return theMDDDomainTypes;
    case OId::STRUCTTYPEOID:
        return theStructTypes;
    case OId::SETTYPEOID:
        return theSetTypes;
    case OId::BLOBOID:
        return theBLOBTiles;
    case OId::INLINETILEOID:
        return theInlineTiles;
    case OId::DBMINTERVALOID:
        return theDBMintervals;
    case OId::DBNULLVALUESOID:
        return theDBNullvalues;
    case OId::STORAGEOID:
        return theDBStorages;
    case OId::DBTCINDEXOID:
        return theDBTCIndexs;
    case OId::MDDHIERIXOID:
        return theDBHierIndexs;
    case OId::ATOMICTYPEOID:
        return theAtomicTypes;
    case OId::MDDRCIXOID:
        return theRCIndexes;
    default:
        LERROR << "Retrival of object of type " << type << " failed: invalid OId type.";
        throw r_Error(INVALID_OIDTYPE);
    }
}

DBObject *ObjectBroker::loadDBStorage(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBStorageLayout(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *ObjectBroker::loadSetType(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new SetType(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *ObjectBroker::loadMDDType(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new MDDType(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *ObjectBroker::loadMDDBaseType(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new MDDBaseType(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *ObjectBroker::loadMDDDimensionType(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new MDDDimensionType(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *ObjectBroker::loadMDDDomainType(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new MDDDomainType(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *ObjectBroker::loadStructType(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new StructType(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *
ObjectBroker::loadDBMinterval(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBMinterval(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *
ObjectBroker::loadDBNullvalues(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBNullvalues(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *
ObjectBroker::loadDBMDDObj(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBMDDObj(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *
ObjectBroker::loadMDDSet(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBMDDSet(id));
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *
ObjectBroker::loadDBTCIndex(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBTCIndex(id));
    retval->setCached(true);
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *
ObjectBroker::loadDBHierIndex(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBHierIndex(id));
    retval->setCached(true);
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *
ObjectBroker::loadDBRCIndexDS(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new DBRCIndexDS(id));
    retval->setCached(true);
    registerDBObject(retval.get());
    return retval.release();
}

DBObject *
ObjectBroker::loadBLOBTile(const OId &id)
{
    unique_ptr<DBObject> retval;
    retval.reset(new BLOBTile(id));
    registerDBObject(retval.get());
    return retval.release();
}

void ObjectBroker::registerTileIndexMapping(const OId &tileoid, const OId &indexoid)
{
    theTileIndexMappings.emplace(tileoid, indexoid);
}

void ObjectBroker::deregisterTileIndexMapping(const OId &tileoid, const OId &indexoid)
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

OIdSet *ObjectBroker::getAllAtomicTypes()
{
    auto *retval = new OIdSet();
    DBObjectPMap &theMap = ObjectBroker::getMap(OId::ATOMICTYPEOID);
    for (auto i = theMap.begin(); i != theMap.end(); i++)
    {
        LTRACE << "inserted from memory " << (*i).first;
        retval->insert((*i).first);
    }
    return retval;
}
