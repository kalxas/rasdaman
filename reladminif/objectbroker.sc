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
// This is -*- C++ -*-


#include "objectbroker.hh"
#include "adminif.hh"
#include "dbnamedobject.hh"
#include "dbref.hh"
#include "sqlglobals.h"
#include "sqlitewrapper.hh"
#include "sqlerror.hh"
#include "relblobif/inlinetile.hh"
#include "relindexif/dbrcindexds.hh"
#include "relindexif/dbtcindex.hh"
#include "relindexif/indexid.hh"
#include "common/exceptions/exception.hh"

#include "catalogmgr/typefactory.hh"

#include <logging.hh>


DBObject *ObjectBroker::loadInlineTile(const OId &id)
{
    DBObject *retval = 0;
    OIdMap::iterator i = theTileIndexMappings.find(id);
    if (i != theTileIndexMappings.end())
    {
        DBTCIndexId dbtc((*i).second);
        retval = (DBObject *) dbtc->getInlineTile(id);
    }
    else
    {
        try
        {
            retval = new InlineTile(id);
            LTRACE << "found in db";
        }
        catch (r_Error &error)
        {
            LTRACE << "not found in db";
            if (retval)
            {
                delete retval;
                retval = 0;
            }
        }
        if (retval == 0)
        {
            long indexid;
            long inlineid;

            indexid = 0;
            inlineid = id.getCounter();

            SQLiteQuery query("SELECT IndexId FROM RAS_ITMAP WHERE TileId = %lld", inlineid);
            if (query.nextRow())
            {
                indexid = query.nextColumnLong();
                DBTCIndexId dbtc(OId(indexid, OId::DBTCINDEXOID));
                retval = (DBObject *) dbtc->getInlineTile(id);
            }
            else
            {
                throw r_Error(r_Error::r_Error_ObjectUnknown);
            }
        }
        DBObjectPPair myPair(retval->getOId(), retval);
        theInlineTiles.insert(myPair);
    }

    return retval;
}

OId ObjectBroker::getOIdOfSetType(const char *name)
{
    long long setoid;

    OId retval;
    unsigned int len = strlen(name);
    if (len > DBNamedObject::MAXNAMELENGTH)
    {
        throw r_Error(TYPENAMEISTOOLONG);
    }

    SQLiteQuery query("SELECT SetTypeId FROM RAS_SETTYPES WHERE SetTypeName = '%s'", name);
    if (query.nextRow())
    {
        setoid = query.nextColumnLong();
        retval = OId(setoid, OId::SETTYPEOID);
        LTRACE << "is in db with " << retval;
    }
    else
    {
        LTRACE << "is not in db";
        throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    return retval;
}

MDDType *ObjectBroker::getMDDTypeByName(const char *name)
{
    long long mddtoidv;

    MDDType *retval = 0;
    const int theMapsNo = 4;
    DBObjectPMap *theMaps[] = {&theMDDTypes, &theMDDBaseTypes, &theMDDDimensionTypes, &theMDDDomainTypes};

    for (int i = 0; i < theMapsNo; i++)
    {
        DBObjectPMap &theMap = *theMaps[i];
        //check if there is an object with that name already in memory
        for (DBObjectPMap::iterator iter = theMap.begin(); iter != theMap.end(); iter++)
        {
            if (strcmp(((DBNamedObject *)(*iter).second)->getName(), name) == 0)
            {
                retval = (MDDType *)(*iter).second;
                break;
            }
        }
        if (retval != 0)
        {
            break;
        }
    }

    if (retval == 0)
    {
        unsigned int len = strlen(name);
        if (len > DBNamedObject::MAXNAMELENGTH)
        {
            throw r_Error(TYPENAMEISTOOLONG);
        }

        SQLiteQuery query("SELECT MDDTypeOId FROM RAS_MDDTYPES_VIEW WHERE MDDTypeName = '%s'", name);
        if (query.nextRow())
        {
            mddtoidv = query.nextColumnLong();
            retval = (MDDType *) getObjectByOId(OId(mddtoidv));
            LTRACE << "is in db with " << retval;
        }
        else
        {
            LTRACE << "is not in db";
            throw r_Error(r_Error::r_Error_ObjectUnknown);
        }
    }

    return retval;
}

OId ObjectBroker::getOIdOfMDDType(const char *name)
{
    char mddtname[STRING_MAXLEN];
    long long mddtoid;

    OId retval;
    unsigned int len = strlen(name);
    if (len > DBNamedObject::MAXNAMELENGTH)
    {
        throw r_Error(TYPENAMEISTOOLONG);
    }
    (void) strncpy(mddtname, const_cast<char *>(name), (size_t) sizeof(mddtname));

    SQLiteQuery query("SELECT MDDTypeOId FROM RAS_MDDTYPES WHERE MDDTypeName = '%s'", name);
    if (query.nextRow())
    {
        mddtoid = query.nextColumnLong();
        retval = OId(mddtoid, OId::MDDTYPEOID);
        LTRACE << "is in db with " << retval;
    }
    else
    {
        LTRACE << "is not in db";
        throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    return retval;
}

OId ObjectBroker::getOIdOfMDDBaseType(const char *name)
{
    OId retval;
    unsigned int len = strlen(name);
    if (len > DBNamedObject::MAXNAMELENGTH)
    {
        throw r_Error(TYPENAMEISTOOLONG);
    }

    SQLiteQuery query("SELECT MDDBaseTypeOId FROM RAS_MDDBASETYPES WHERE MDDTypeName = '%s'", name);
    if (query.nextRow())
    {
        long long mddboid = query.nextColumnLong();
        retval = OId(mddboid, OId::MDDBASETYPEOID);
        LTRACE << "is in db with " << retval;
    }
    else
    {
        LTRACE << "is not in db";
        throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    return retval;
}

OId ObjectBroker::getOIdOfMDDDimensionType(const char *name)
{
    OId retval;
    unsigned int len = strlen(name);
    if (len > DBNamedObject::MAXNAMELENGTH)
    {
        throw r_Error(TYPENAMEISTOOLONG);
    }

    SQLiteQuery query("SELECT MDDDimTypeOId FROM RAS_MDDDIMTYPES WHERE MDDTypeName = '%s'", name);
    if (query.nextRow())
    {
        long long mdddioid = query.nextColumnLong();
        retval = OId(mdddioid, OId::MDDDIMTYPEOID);
        LTRACE << "is in db with " << retval;
    }
    else
    {
        LTRACE << "is not in db";
        throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    return retval;
}

OId ObjectBroker::getOIdOfMDDDomainType(const char *name)
{
    OId retval;
    unsigned int len = strlen(name);
    if (len > DBNamedObject::MAXNAMELENGTH)
    {
        throw r_Error(TYPENAMEISTOOLONG);
    }

    SQLiteQuery query("SELECT MDDDomTypeOId FROM RAS_MDDDOMTYPES WHERE MDDTypeName = '%s'", name);
    if (query.nextRow())
    {
        long long mdddooid = query.nextColumnLong();
        retval = OId(mdddooid, OId::MDDDOMTYPEOID);
        LTRACE << "is in db with " << retval;
    }
    else
    {
        LTRACE << "is not in db";
        throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    return retval;
}

OId ObjectBroker::getOIdOfStructType(const char *name)
{
    OId retval;
    unsigned int len = strlen(name);
    if (len > DBNamedObject::MAXNAMELENGTH)
    {
        throw r_Error(TYPENAMEISTOOLONG);
    }

    SQLiteQuery query("SELECT BaseTypeId FROM RAS_BASETYPENAMES WHERE BaseTypeName = '%s'", name);
    if (query.nextRow())
    {
        long long structoid = query.nextColumnLong();
        retval = OId(structoid, OId::STRUCTTYPEOID);
        LTRACE << "is in db with " << retval;
    }
    else
    {
        LTRACE << "is not in db";
        throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    return retval;
}

OId ObjectBroker::getOIdOfMDDSet(const char *name)
{
    OId retval;
    unsigned int len = strlen(name);
    if (len > DBNamedObject::MAXNAMELENGTH)
    {
        throw r_Error(TYPENAMEISTOOLONG);
    }

    SQLiteQuery query("SELECT MDDCollId FROM RAS_MDDCOLLNAMES WHERE MDDCollName = '%s'", name);
    if (query.nextRow())
    {
        long long colloid = query.nextColumnLong();
        retval = OId(colloid, OId::MDDCOLLOID);
        LTRACE << "is in db with " << retval;
    }
    else
    {
        LTRACE << "is not in db";
        throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    return retval;
}

OIdSet *ObjectBroker::getAllSetTypes()
{
    OIdSet *retval = new OIdSet();
    DBObjectPMap &theMap = ObjectBroker::getMap(OId::SETTYPEOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        LTRACE << "inserted from memory " << (*i).first;
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT SetTypeId FROM RAS_SETTYPES ORDER BY SetTypeId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::SETTYPEOID);
        LTRACE << "read " << id << " " << id.getType();
        LDEBUG << "got object " << id << " " << id.getType();
        retval->insert(id);
    }

    return retval;
}

OIdSet *ObjectBroker::getAllMDDTypes()
{
    OIdSet *retval = new OIdSet();
    DBObjectPMap &theMap = ObjectBroker::getMap(OId::MDDTYPEOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        LTRACE << "inserted from memory " << (*i).first;
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT MDDTypeOId FROM RAS_MDDTYPES ORDER BY MDDTypeOId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::MDDTYPEOID);
        LTRACE << "read " << id << " " << id.getType();
        LDEBUG << "got object " << id << " " << id.getType();
        retval->insert(id);
    }

    return retval;
}

OIdSet *ObjectBroker::getAllMDDBaseTypes()
{
    OIdSet *retval = new OIdSet();
    DBObjectPMap &theMap = ObjectBroker::getMap(OId::MDDBASETYPEOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        LTRACE << "inserted from memory " << (*i).first;
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT MDDBaseTypeOId FROM RAS_MDDBASETYPES ORDER BY MDDBaseTypeOId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::MDDBASETYPEOID);
        LTRACE << "read " << id << " " << id.getType();
        LDEBUG << "got object " << id << " " << id.getType();
        retval->insert(id);
    }

    return retval;
}

OIdSet *ObjectBroker::getAllMDDDimensionTypes()
{
    OIdSet *retval = new OIdSet();
    DBObjectPMap &theMap = ObjectBroker::getMap(OId::MDDDIMTYPEOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        LTRACE << "inserted from memory " << (*i).first;
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT MDDDimTypeOId FROM RAS_MDDDIMTYPES ORDER BY MDDDimTypeOId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::MDDDIMTYPEOID);
        LTRACE << "read " << id << " " << id.getType();
        LDEBUG << "got object " << id << " " << id.getType();
        retval->insert(id);
    }

    return retval;
}

OIdSet *ObjectBroker::getAllMDDDomainTypes()
{
    OIdSet *retval = new OIdSet();
    DBObjectPMap &theMap = ObjectBroker::getMap(OId::MDDDOMTYPEOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        LTRACE << "inserted from memory " << (*i).first;
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT MDDDomTypeOId FROM RAS_MDDDOMTYPES ORDER BY MDDDomTypeOId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::MDDDOMTYPEOID);
        LTRACE << "read " << id << " " << id.getType();
        LDEBUG << "got object " << id << " " << id.getType();
        retval->insert(id);
    }

    return retval;
}

OIdSet *ObjectBroker::getAllStructTypes()
{
    OIdSet *retval = new OIdSet();
    DBObjectPMap &theMap = ObjectBroker::getMap(OId::STRUCTTYPEOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        LTRACE << "inserted from memory " << (*i).first;
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT BaseTypeId FROM RAS_BASETYPENAMES ORDER BY BaseTypeId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::STRUCTTYPEOID);
        LTRACE << "read " << id << " " << id.getType();
        LDEBUG << "got object " << id << " " << id.getType();
        retval->insert(id);
    }

    return retval;
}

OIdSet *ObjectBroker::getAllMDDObjects()
{
    OIdSet *retval = new OIdSet();
    DBObjectPMap &theMap = ObjectBroker::getMap(OId::MDDOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        LTRACE << "inserted from memory " << (*i).first;
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT MDDId FROM RAS_MDDOBJECTS ORDER BY MDDId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::MDDOID);
        LTRACE << "read " << id << " " << id.getType();
        LDEBUG << "got object " << id << " " << id.getType();
        retval->insert(id);
    }
    return retval;
}

OIdSet *ObjectBroker::getAllMDDSets()
{
    auto retval = std::unique_ptr<OIdSet>(new OIdSet());
    DBObjectPMap &theMap = ObjectBroker::getMap(OId::MDDCOLLOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        LTRACE << "inserted from memory " << (*i).first;
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT MDDCollId FROM RAS_MDDCOLLNAMES ORDER BY MDDCollId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::MDDCOLLOID);
        LTRACE << "read " << id << " " << id.getType();
        LDEBUG << "got object " << id << " " << id.getType();
        retval->insert(id);
    }

    return retval.release();
}
