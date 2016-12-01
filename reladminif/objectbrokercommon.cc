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

#include "raslib/minterval.hh"
#include "objectbroker.hh"
#include "dbnamedobject.hh"
#include "relstorageif/dbstoragelayout.hh"
#include "adminif.hh"
#include "relcatalogif/alltypes.hh"
#include "relindexif/hierindex.hh"
#include "relblobif/blobtile.hh"
#include "relcatalogif/dbminterval.hh"
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
#include <easylogging++.h>

#ifdef LINUX
template class DBRef<BLOBTile>;
template class DBRef<DBTile>;
template class DBRef<InlineTile>;
#endif

using namespace std;

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
ObjectBroker::freeMemory() throw (r_Error)
{
    LTRACE << "memoryOverFlow()";
    bool retval = false;
    DBRef<BLOBTile>::setPointerCaching(false);
    DBRef<DBTile>::setPointerCaching(false);
    DBRef<InlineTile>::setPointerCaching(false);
    if (!ObjectBroker::theBLOBTiles.empty())
    {
        int theLucky = ObjectBroker::theBLOBTiles.size() / 2;
        DBObjectPMap::iterator it = ObjectBroker::theBLOBTiles.begin();
        for (int i = 0; i < theLucky; i++, it++);
        delete(*it).second;
        retval = true;
    }
    return retval;
}

void
ObjectBroker::init()
{
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

    DBObject* atomicTypes[] = {theComplex2, theComplex1, theFloat, theDouble, theOctet, theShort, theLong, theUShort, theBool, theChar, theULong};
#ifdef DEBUG
    if (sizeof(atomicTypes) / sizeof(DBObject*) != TypeFactory::MaxBuiltInId)
    {
        LFATAL << "ObjectBroker::init() not all atomic types were added!";
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
    LTRACE << "deinit()";
    if (ObjectBroker::theLong != 0)
    {
        delete ObjectBroker::theLong;
        ObjectBroker::theLong = 0;
    }

    if (ObjectBroker::theShort != 0)
    {
        delete ObjectBroker::theShort;
        ObjectBroker::theShort = 0;
    }

    if (ObjectBroker::theOctet != 0)
    {
        delete ObjectBroker::theOctet;
        ObjectBroker::theOctet = 0;
    }

    if (ObjectBroker::theULong != 0)
    {
        delete ObjectBroker::theULong;
        ObjectBroker::theULong = 0;
    }

    if (ObjectBroker::theUShort != 0)
    {
        delete ObjectBroker::theUShort;
        ObjectBroker::theUShort = 0;
    }

    if (ObjectBroker::theChar != 0)
    {
        delete ObjectBroker::theChar;
        ObjectBroker::theChar = 0;
    }

    if (ObjectBroker::theBool != 0)
    {
        delete ObjectBroker::theBool;
        ObjectBroker::theBool = 0;
    }

    if (ObjectBroker::theDouble != 0)
    {
        delete ObjectBroker::theDouble;
        ObjectBroker::theDouble = 0;
    }

    if (ObjectBroker::theFloat != 0)
    {
        delete ObjectBroker::theFloat;
        ObjectBroker::theFloat = 0;
    }

    if (ObjectBroker::theComplex1 != 0)
    {
        delete ObjectBroker::theComplex1;
        ObjectBroker::theComplex1 = 0;
    }

    if (ObjectBroker::theComplex2 != 0)
    {
        delete ObjectBroker::theComplex2;
        ObjectBroker::theComplex2 = 0;
    }

    theAtomicTypes.clear();

    theSetTypes.clear();

    theMDDTypes.clear();

    theMDDBaseTypes.clear();

    theMDDDimensionTypes.clear();

    theMDDDomainTypes.clear();

    theStructTypes.clear();

    theDBMintervals.clear();

    theDBMDDObjs.clear();

    theMDDSets.clear();

    theDBStorages.clear();

    theDBHierIndexs.clear();

    theDBTCIndexs.clear();

    theBLOBTiles.clear();

    theInlineTiles.clear();

    theRCIndexes.clear();

    theTileIndexMappings.clear();
}

DBObject*
ObjectBroker::getObjectByOId(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    if (id.getType() == OId::INVALID)
    {
        retval = 0;
    }
    else
    {
        retval = ObjectBroker::isInMemory(id);
        if (retval == 0)
        {
            // TODO: check if we have enough memory available, if not, remove
            // some not used objects from memory before loading from the DB
            switch (id.getType())
            {
            case OId::MDDOID:
                retval = loadDBMDDObj(id);
                break;
            case OId::MDDCOLLOID:
                retval = loadMDDSet(id);
                break;
            case OId::MDDTYPEOID:
                retval = loadMDDType(id);
                break;
            case OId::MDDBASETYPEOID:
                retval = loadMDDBaseType(id);
                break;
            case OId::MDDDIMTYPEOID:
                retval = loadMDDDimensionType(id);
                break;
            case OId::MDDDOMTYPEOID:
                retval = loadMDDDomainType(id);
                break;
            case OId::STRUCTTYPEOID:
                retval = loadStructType(id);
                break;
            case OId::SETTYPEOID:
                retval = loadSetType(id);
                break;
            case OId::BLOBOID:
                retval = loadBLOBTile(id);
                break;
            case OId::DBMINTERVALOID:
                retval = loadDBMinterval(id);
                break;
            case OId::STORAGEOID:
                retval = loadDBStorage(id);
                break;
            case OId::MDDHIERIXOID:
                retval = loadDBHierIndex(id);
                break;
            case OId::DBTCINDEXOID:
                retval = loadDBTCIndex(id);
                break;
            case OId::INLINETILEOID:
                retval = loadInlineTile(id);
                break;
            case OId::MDDRCIXOID:
                retval = loadDBRCIndexDS(id);
                break;
            case OId::ATOMICTYPEOID:
                LERROR << "Atomic type not found in memory.";
            default:
                LFATAL << "Retrival of Object Failed (Internal State 3).\n" << "Please contact Customer Support.";
                throw r_Error(INVALID_OIDTYPE);
                break;
            }
        }
    }
    return retval;
}


DBObject*
ObjectBroker::isInMemory(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    DBObjectPMap& theMap = ObjectBroker::getMap(id.getType());
    DBObjectPMap::iterator i = theMap.find(id);
    if (i != theMap.end())
    {
        retval = (*i).second;
        LTRACE << "found object with that id in map at " << (unsigned long)retval << " with id " << retval->getOId();
    }
    else
    {
        LTRACE << "did not find object with that id in map";
    }
    return retval;
}

void
ObjectBroker::registerDBObject(DBObject* obj)
{
    DBObjectPPair myPair(obj->getOId(), obj);
    ObjectBroker::getMap(obj->getOId().getType()).insert(myPair);
}

void
ObjectBroker::deregisterDBObject(const OId& id)
{
    if (id.getType() != OId::INVALID)
    {
        LTRACE << "size of map before\t: " << ObjectBroker::getMap(id.getType()).size();
        DBObjectPMap& t = ObjectBroker::getMap(id.getType());
        DBObjectPMap::iterator i = t.find(id);
        if (i != t.end())
        {
            (*i).second = 0;
            t.erase(i);
        }
        LTRACE << "size of map after \t: " << ObjectBroker::getMap(id.getType()).size();
    }
}

OIdSet*
ObjectBroker::getAllObjects(OId::OIdType type)
{
    OIdSet* retval = 0;
    switch (type)
    {
    case OId::MDDCOLLOID:
        retval = getAllMDDSets();
        break;
    case OId::MDDOID:
        retval = getAllMDDObjects();
        break;
    case OId::MDDTYPEOID:
        retval = getAllMDDTypes();
        break;
    case OId::MDDBASETYPEOID:
        retval = getAllMDDBaseTypes();
        break;
    case OId::MDDDIMTYPEOID:
        retval = getAllMDDDimensionTypes();
        break;
    case OId::MDDDOMTYPEOID:
        retval = getAllMDDDomainTypes();
        break;
    case OId::STRUCTTYPEOID:
        retval = getAllStructTypes();
        break;
    case OId::SETTYPEOID:
        retval = getAllSetTypes();
        break;
    case OId::ATOMICTYPEOID:
        retval = getAllAtomicTypes();
        break;

    default:
        LTRACE << "getAllObjects(" << type << ")";
        LFATAL << "Retrival of Object Failed (Internal State 4).\n" << "Please contact Customer Support.";
        throw r_Error(INVALID_OIDTYPE);
        break;
    }
    return retval;
}


OId
ObjectBroker::getOIdByName(OId::OIdType type, const char* name) throw (r_Error)
{
    OId id;
    switch (type)
    {
    case OId::MDDCOLLOID:
        id = getOIdOfMDDSet(name);
        break;
    case OId::MDDTYPEOID:
        id = getOIdOfMDDType(name);
        break;
    case OId::MDDBASETYPEOID:
        id = getOIdOfMDDBaseType(name);
        break;
    case OId::MDDDIMTYPEOID:
        id = getOIdOfMDDDimensionType(name);
        break;
    case OId::MDDDOMTYPEOID:
        id = getOIdOfMDDDomainType(name);
        break;
    case OId::STRUCTTYPEOID:
        id = getOIdOfStructType(name);
        break;
    case OId::SETTYPEOID:
        id = getOIdOfSetType(name);
        break;
    case OId::ATOMICTYPEOID:
        if (strcmp(name, ULongType::Name) == 0)
        {
            id = theULong->getOId();
        }
        else if (strcmp(name, BoolType::Name) == 0)
        {
            id = theBool->getOId();
        }
        else if (strcmp(name, CharType::Name) == 0)
        {
            id = theChar->getOId();
        }
        else if (strcmp(name, UShortType::Name) == 0)
        {
            id = theUShort->getOId();
        }
        else if (strcmp(name, LongType::Name) == 0)
        {
            id = theLong->getOId();
        }
        else if (strcmp(name, ShortType::Name) == 0)
        {
            id = theShort->getOId();
        }
        else if (strcmp(name, OctetType::Name) == 0)
        {
            id = theOctet->getOId();
        }
        else if (strcmp(name, DoubleType::Name) == 0)
        {
            id = theDouble->getOId();
        }
        else if (strcmp(name, FloatType::Name) == 0)
        {
            id = theFloat->getOId();
        }
        else if (strcmp(name, ComplexType1::Name) == 0)
        {
            id = theComplex1->getOId();
        }
        else if (strcmp(name, ComplexType2::Name) == 0)
        {
            id = theComplex2->getOId();
        }
        break;

    default:
        LFATAL << "Retrival of Object Failed (Internal State 5).\n" << "Please contact Customer Support.";
        throw r_Error(INVALID_OIDTYPE);
        break;
    }
    return id;
}


DBObject*
ObjectBroker::getObjectByName(OId::OIdType type, const char* name) throw (r_Error)
{
    DBObject* retval = 0;
    DBObjectPMap* theMap = 0;
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
        LFATAL << "Retrival of Object Failed (Internal State 6).\n" << "Please contact Customer Support.";
        throw r_Error(INVALID_OIDTYPE);
        break;
    }

    //check if there is an object with that name already in memory
    for (DBObjectPMap::iterator iter = theMap->begin(); iter != theMap->end(); iter++)
    {
        if (strcmp((static_cast<DBNamedObject*>((*iter).second))->getName(), name) == 0)
        {
            LTRACE << name << " equals " << ((DBNamedObject*)(*iter).second)->getName();
            retval = (*iter).second;
            break;
        }
        else
        {
            LTRACE << name << " equals NOT " << ((DBNamedObject*)(*iter).second)->getName();
        }
    }

    //no - no matching object.  try loading from db
    if (!retval)
    {
        retval = ObjectBroker::getObjectByOId(ObjectBroker::getOIdByName(type, name));
    }
    return retval;
}

void
ObjectBroker::completelyClearMap(DBObjectPMap& theMap) throw (r_Error)
{
    DBObjectPVector test;
    test.reserve(theMap.size());
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        LTRACE << "preparing to delete " << (*i).second->getOId() << " " << (*i).second->getOId().getType();
        (*i).second->validate();
        test.push_back((*i).second);
        //(*i).second = 0; not good because of circular dependencies in the destructors
    }
    for (DBObjectPVector::iterator i2 = test.begin(); i2 != test.end(); i2++)
    {
        LTRACE << "deleting " << (*i2)->getOId() << " " << (*i2)->getOId().getType();
        delete(*i2);
    }
    test.clear();
}

void
ObjectBroker::clearBroker() throw (r_Error)
{
    //do not ever clear the ATOMICTYPEOID map! those are on the stack, not heap!
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::MDDCOLLOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::MDDOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::MDDHIERIXOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::MDDRCIXOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::DBTCINDEXOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::INLINETILEOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::STORAGEOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::SETTYPEOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::MDDDOMTYPEOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::MDDDIMTYPEOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::MDDBASETYPEOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::MDDTYPEOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::STRUCTTYPEOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::DBMINTERVALOID));
    ObjectBroker::completelyClearMap(ObjectBroker::getMap(OId::BLOBOID));
    theTileIndexMappings.clear();
}

DBObjectPMap&
ObjectBroker::getMap(OId::OIdType type) throw (r_Error)
{
    DBObjectPMap* theMap = 0;
    switch (type)
    {
    case OId::MDDOID:
        theMap = &theDBMDDObjs;
        break;
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
    case OId::BLOBOID:
        theMap = &theBLOBTiles;
        break;
    case OId::INLINETILEOID:
        theMap = &theInlineTiles;
        break;
    case OId::DBMINTERVALOID:
        theMap = &theDBMintervals;
        break;
    case OId::STORAGEOID:
        theMap = &theDBStorages;
        break;
    case OId::DBTCINDEXOID:
        theMap = &theDBTCIndexs;
        break;
    case OId::MDDHIERIXOID:
        theMap = &theDBHierIndexs;
        break;
    case OId::ATOMICTYPEOID:
        theMap = &theAtomicTypes;
        break;
    case OId::MDDRCIXOID:
        theMap = &theRCIndexes;
        break;
    default:
        LTRACE << "getMap(" << type << ")";
        LFATAL << "Retrival of Object Failed (Internal State 7).\n" << "Please contact Customer Support.";
        throw r_Error(INVALID_OIDTYPE);
        break;
    }
    return *theMap;
}

DBObject*
ObjectBroker::loadDBStorage(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new DBStorageLayout(id);
        LTRACE << "found in db";
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        if (retval)
        {
            delete retval;
            retval = 0;
        }
        throw error;
    }
    return retval;
}

DBObject*
ObjectBroker::loadSetType(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new SetType(id);
        LTRACE << "found in db";
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        if (retval)
        {
            delete retval;
            retval = 0;
        }
        throw error;
    }
    return retval;
}

DBObject*
ObjectBroker::loadMDDType(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new MDDType(id);
        LTRACE << "found in db";
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        if (retval)
        {
            delete retval;
            retval = 0;
        }
        throw error;
    }
    return retval;
}


DBObject*
ObjectBroker::loadMDDBaseType(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new MDDBaseType(id);
        LTRACE << "found in db";
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        if (retval)
        {
            delete retval;
            retval = 0;
        }
        throw error;
    }
    return retval;
}


DBObject*
ObjectBroker::loadMDDDimensionType(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new MDDDimensionType(id);
        LTRACE << "found in db";
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        if (retval)
        {
            delete retval;
            retval = 0;
        }
        throw error;
    }
    return retval;
}


DBObject*
ObjectBroker::loadMDDDomainType(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new MDDDomainType(id);
        LTRACE << "found in db";
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        if (retval)
        {
            delete retval;
            retval = 0;
        }
        throw error;
    }
    return retval;
}


DBObject*
ObjectBroker::loadStructType(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new StructType(id);
        LTRACE << "found in db";
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        if (retval)
        {
            delete retval;
            retval = 0;
        }
        throw error;
    }
    return retval;
}


DBObject*
ObjectBroker::loadDBMinterval(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new DBMinterval(id);
        LTRACE << "found in db";
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        if (retval)
        {
            delete retval;
            retval = 0;
        }
        throw error;
    }
    return retval;
}


DBObject*
ObjectBroker::loadDBMDDObj(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new DBMDDObj(id);
        LTRACE << "found in db";
        LDEBUG << "found object, inserting " << retval->getOId() << " into result list";
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        if (retval)
        {
            delete retval;
            retval = 0;
        }
        throw error;
    }

    return retval;
}


DBObject*
ObjectBroker::loadMDDSet(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new DBMDDSet(id);
        LTRACE << "found in db";
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        if (retval)
        {
            delete retval;
            retval = 0;
        }
        throw error;
    }
    return retval;
}

DBObject*
ObjectBroker::loadDBTCIndex(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new DBTCIndex(id);
        LTRACE << "found in db";
        retval->setCached(true);
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        if (retval)
        {
            delete retval;
            retval = 0;
        }
        throw error;
    }
    return retval;
}

DBObject*
ObjectBroker::loadDBHierIndex(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new DBHierIndex(id);
        LTRACE << "found in db";
        retval->setCached(true);
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        if (retval)
        {
            delete retval;
            retval = 0;
        }
        throw error;
    }
    return retval;
}


DBObject*
ObjectBroker::loadBLOBTile(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new BLOBTile(id);
        LTRACE << "found in db";
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        if (retval)
        {
            delete retval;
            retval = 0;
        }
        throw error;
    }
    return retval;
}

DBObject*
ObjectBroker::loadDBRCIndexDS(const OId& id) throw (r_Error)
{
    DBObject* retval = 0;
    try
    {
        retval = new DBRCIndexDS(id);
        retval->setCached(true);
        LTRACE << "found in db";
        registerDBObject(retval);
    }
    catch (r_Error& error)
    {
        LTRACE << "not found in db";
        delete retval;
        throw error;
    }
    return retval;
}

void
ObjectBroker::registerTileIndexMapping(const OId& tileoid, const OId& indexoid)
{
    OIdPair p(tileoid, indexoid);
    theTileIndexMappings.insert(p);
}

void
ObjectBroker::deregisterTileIndexMapping(const OId& tileoid, const OId& indexoid)
{
    OIdPair p(tileoid, indexoid);
    OIdMap::iterator i = theTileIndexMappings.find(tileoid);
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
ObjectBroker::getAllAtomicTypes() throw (r_Error)
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
