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
#include "debug-srv.hh"
#include "sqlglobals.h"
#include "sqlitewrapper.hh"

#include "objectbroker.hh"
#include "raslib/rmdebug.hh"
#include "sqlerror.hh"
#include "relindexif/dbtcindex.hh"
#include "relindexif/indexid.hh"
#include "adminif.hh"
#include "relindexif/dbrcindexds.hh"
#include "relblobif/inlinetile.hh"
#include "dbref.hh"
#include "dbnamedobject.hh"
#include "externs.h"
#include "catalogmgr/typefactory.hh"

DBObject*
ObjectBroker::loadInlineTile(const OId& id) throw (r_Error)
{
    RMDBGENTER(11, RMDebug::module_adminif, "ObjectBroker", "loadInlineTile(" << id << ")");
    ENTER("ObjectBroker::loadInlineTile, oid=" << id);

    DBObject* retval = 0;
    OIdMap::iterator i = theTileIndexMappings.find(id);
    if (i != theTileIndexMappings.end())
    {
        DBTCIndexId dbtc((*i).second);
        retval = (DBObject*) dbtc->getInlineTile(id);
    }
    else
    {
        try
        {
            retval = new InlineTile(id);
            RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "found in db");
        }
        catch (r_Error& error)
        {
            RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "not found in db");
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
                retval = (DBObject*) dbtc->getInlineTile(id);
            }
            else
            {
                RMDBGEXIT(11, RMDebug::module_adminif, "ObjectBroker", "db error not found in db");
                LEAVE("ObjectBroker::getOIdOfSetType, object not found");
                throw r_Error(r_Error::r_Error_ObjectUnknown);
            }
        }
        DBObjectPPair myPair(retval->getOId(), retval);
        theInlineTiles.insert(myPair);
    }

    LEAVE("ObjectBroker::loadInlineTile, retval=" << retval);
    RMDBGEXIT(11, RMDebug::module_adminif, "ObjectBroker", "loadInlineTile(" << id << ")");
    return retval;
}

OId
ObjectBroker::getOIdOfSetType(const char* name) throw (r_Error)
{
    ENTER("ObjectBroker::getOIdOfSetType, name=" << name);

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
        RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is in db with " << retval);
    }
    else
    {
        RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is not in db");
        LEAVE("ObjectBroker::getOIdOfSetType, object not found");
        throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    LEAVE("ObjectBroker::getOIdOfSetType, retval=" << retval);
    return retval;
}

MDDType*
ObjectBroker::getMDDTypeByName(const char* name) throw (r_Error)
{
    ENTER("ObjectBroker::getMDDTypeByName, name=" << name);

    long long mddtoidv;

    MDDType* retval = 0;
    const int theMapsNo = 4;
    DBObjectPMap * theMaps[] = {&theMDDTypes, &theMDDBaseTypes, &theMDDDimensionTypes, &theMDDDomainTypes};

    for (int i = 0; i < theMapsNo; i++)
    {
        DBObjectPMap& theMap = *theMaps[i];
        //check if there is an object with that name already in memory
        for (DBObjectPMap::iterator iter = theMap.begin(); iter != theMap.end(); iter++)
        {
            if (strcmp(((DBNamedObject*) (*iter).second)->getName(), name) == 0)
            {
                retval = (MDDType*) (*iter).second;
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
            LEAVE("ObjectBroker::getMDDTypeByName(): type name exceeding max length: " << name);
            throw r_Error(TYPENAMEISTOOLONG);
        }

        SQLiteQuery query("SELECT MDDTypeOId FROM RAS_MDDTYPES_VIEW WHERE MDDTypeName = '%s'", name);
        if (query.nextRow())
        {
            mddtoidv = query.nextColumnLong();
            retval = (MDDType*) getObjectByOId(OId(mddtoidv));
            RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is in db with " << retval);
        }
        else
        {
            RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is not in db");
            LEAVE("ObjectBroker::getMDDTypeByName(): object not found");
            throw r_Error(r_Error::r_Error_ObjectUnknown);
        }
    }

    LEAVE("ObjectBroker::getMDDTypeByName, retval=" << retval);
    return retval;
}

OId
ObjectBroker::getOIdOfMDDType(const char* name) throw (r_Error)
{
    ENTER("ObjectBroker::getOIdOfMDDType, name=" << name);

    char mddtname[STRING_MAXLEN];
    long long mddtoid;

    OId retval;
    unsigned int len = strlen(name);
    if (len > DBNamedObject::MAXNAMELENGTH)
    {
        LEAVE("ObjectBroker::getOIdOfMDDType(): name exceeds max length:" << name);
        throw r_Error(TYPENAMEISTOOLONG);
    }
    (void) strncpy(mddtname, const_cast<char*>(name), (size_t) sizeof (mddtname));

    SQLiteQuery query("SELECT MDDTypeOId FROM RAS_MDDTYPES WHERE MDDTypeName = '%s'", name);
    if (query.nextRow())
    {
        mddtoid = query.nextColumnLong();
        retval = OId(mddtoid, OId::MDDTYPEOID);
        RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is in db with " << retval);
    }
    else
    {
        RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is not in db");
        LEAVE("ObjectBroker::getOIdOfMDDType(): object not in db");
        throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    LEAVE("ObjectBroker::getOIdOfMDDType, retval=" << retval);
    return retval;
}

OId
ObjectBroker::getOIdOfMDDBaseType(const char* name) throw (r_Error)
{
    ENTER("ObjectBroker::getOIdOfMDDBaseType, name=" << name);

    OId retval;
    unsigned int len = strlen(name);
    if (len > DBNamedObject::MAXNAMELENGTH)
    {
        LEAVE("ObjectBroker::getOIdOfMDDBaseType(): name exceeds max length:" << name);
        throw r_Error(TYPENAMEISTOOLONG);
    }

    SQLiteQuery query("SELECT MDDBaseTypeOId FROM RAS_MDDBASETYPES WHERE MDDTypeName = '%s'", name);
    if (query.nextRow())
    {
        long long mddboid = query.nextColumnLong();
        retval = OId(mddboid, OId::MDDBASETYPEOID);
        RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is in db with " << retval);
    }
    else
    {
        RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is not in db");
        LEAVE("ObjectBroker::getOIdOfMDDBaseType(): object not in db");
        throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    LEAVE("ObjectBroker::getOIdOfMDDBaseType, retval=" << retval);
    return retval;
}

OId
ObjectBroker::getOIdOfMDDDimensionType(const char* name) throw (r_Error)
{
    ENTER( "ObjectBroker::getOIdOfMDDDimensionType, name=" << name );

    OId retval;
    unsigned int len = strlen(name);
    if (len > DBNamedObject::MAXNAMELENGTH)
    {
        LEAVE( "ObjectBroker::getOIdOfMDDDimensionType(): name exceeds max length:" << name );
        throw r_Error(TYPENAMEISTOOLONG);
    }

    SQLiteQuery query("SELECT MDDDimTypeOId FROM RAS_MDDDIMTYPES WHERE MDDTypeName = '%s'", name);
    if (query.nextRow())
    {
        long long mdddioid = query.nextColumnLong();
        retval = OId(mdddioid, OId::MDDDIMTYPEOID);
        RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is in db with " << retval);
    }
    else
    {
        RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is not in db");
        LEAVE("ObjectBroker::getOIdOfMDDDimensionType(): object not in db");
        throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    LEAVE( "ObjectBroker::getOIdOfMDDDimensionType, retval=" << retval );
    return retval;
}

OId
ObjectBroker::getOIdOfMDDDomainType(const char* name) throw (r_Error)
{
    ENTER( "ObjectBroker::getOIdOfMDDDomainType, name=" << name );

    OId retval;
    unsigned int len = strlen(name);
    if (len > DBNamedObject::MAXNAMELENGTH)
    {
        LEAVE( "ObjectBroker::getOIdOfMDDDomainType(): name exceeds max length:" << name );
        throw r_Error(TYPENAMEISTOOLONG);
    }

    SQLiteQuery query("SELECT MDDDomTypeOId FROM RAS_MDDDOMTYPES WHERE MDDTypeName = '%s'", name);
    if (query.nextRow())
    {
        long long mdddooid = query.nextColumnLong();
        retval = OId(mdddooid, OId::MDDDOMTYPEOID);
        RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is in db with " << retval);
    }
    else
    {
        RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is not in db");
        LEAVE("ObjectBroker::getOIdOfMDDDomainType(): object not in db");
        throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    LEAVE( "ObjectBroker::getOIdOfMDDDomainType, retval=" << retval );
    return retval;
}

OId
ObjectBroker::getOIdOfStructType(const char* name) throw (r_Error)
{
    ENTER( "ObjectBroker::getOIdOfStructType, name=" << name );

    OId retval;
    unsigned int len = strlen(name);
    if (len > DBNamedObject::MAXNAMELENGTH)
    {
        LEAVE( "ObjectBroker::getOIdOfStructType(): name exceeds max length:" << name );
        throw r_Error(TYPENAMEISTOOLONG);
    }

    SQLiteQuery query("SELECT BaseTypeId FROM RAS_BASETYPENAMES WHERE BaseTypeName = '%s'", name);
    if (query.nextRow())
    {
        long long structoid = query.nextColumnLong();
        retval = OId(structoid, OId::STRUCTTYPEOID);
        RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is in db with " << retval);
    }
    else
    {
        RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is not in db");
        LEAVE( "ObjectBroker::getOIdOfStructType(): object not in db" );
        throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    LEAVE( "ObjectBroker::getOIdOfStructType, retval=" << retval );
    return retval;
}

OId
ObjectBroker::getOIdOfMDDSet(const char* name) throw (r_Error)
{
    ENTER( "ObjectBroker::getOIdOfMDDSet, name=" << name );

    OId retval;
    unsigned int len = strlen(name);
    if (len > DBNamedObject::MAXNAMELENGTH)
    {
        LEAVE( "ObjectBroker::getOIdOfMDDSet(): name exceeds max length:" << name );
        throw r_Error(TYPENAMEISTOOLONG);
    }

    SQLiteQuery query("SELECT MDDCollId FROM RAS_MDDCOLLNAMES WHERE MDDCollName = '%s'", name);
    if (query.nextRow())
    {
        long long colloid = query.nextColumnLong();
        retval = OId(colloid, OId::MDDCOLLOID);
        RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is in db with " << retval);
    }
    else
    {
            RMDBGONCE(11, RMDebug::module_adminif, "ObjectBroker", "is not in db");
            LEAVE( "ObjectBroker::getOIdOfMDDSet(): object not in db" );
            throw r_Error(r_Error::r_Error_ObjectUnknown);
    }

    LEAVE( "ObjectBroker::getOIdOfMDDSet, retval=" << retval );
    return retval;
}

OIdSet*
ObjectBroker::getAllSetTypes() throw (r_Error)
{
    RMDBGENTER(11, RMDebug::module_adminif, "ObjectBroker", "getAllSetTypes()");
    ENTER( "ObjectBroker::getAllSetTypes" );

    OIdSet* retval = new OIdSet();
    DBObjectPMap& theMap = ObjectBroker::getMap(OId::SETTYPEOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "inserted from memory " << (*i).first);
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT SetTypeId FROM RAS_SETTYPES ORDER BY SetTypeId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::SETTYPEOID);
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "read " << id << " " << id.getType());
        TALK( "got object " << id << " " << id.getType());
        retval->insert(id);
    }

    LEAVE( "ObjectBroker::getAllSetTypes" );
    RMDBGEXIT(11, RMDebug::module_adminif, "ObjectBroker", "getAllSetTypes() ");
    return retval;
}

OIdSet*
ObjectBroker::getAllMDDTypes() throw (r_Error)
{
    RMDBGENTER(11, RMDebug::module_adminif, "ObjectBroker", "getAllMDDTypes()");
    ENTER( "ObjectBroker::getAllMDDTypes" );

    OIdSet* retval = new OIdSet();
    DBObjectPMap& theMap = ObjectBroker::getMap(OId::MDDTYPEOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "inserted from memory " << (*i).first);
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT MDDTypeOId FROM RAS_MDDTYPES ORDER BY MDDTypeOId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::MDDTYPEOID);
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "read " << id << " " << id.getType());
        TALK( "got object " << id << " " << id.getType() );
        retval->insert(id);
    }

    LEAVE( "ObjectBroker::getAllMDDTypes, retval=" << retval );
    RMDBGEXIT(11, RMDebug::module_adminif, "ObjectBroker", "getAllMDDTypes() ");
    return retval;
}

OIdSet*
ObjectBroker::getAllMDDBaseTypes() throw (r_Error)
{
    RMDBGENTER(11, RMDebug::module_adminif, "ObjectBroker", "getAllMDDBaseTypes()");
    ENTER( "ObjectBroker::getAllMDDBaseTypes" );

    OIdSet* retval = new OIdSet();
    DBObjectPMap& theMap = ObjectBroker::getMap(OId::MDDBASETYPEOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "inserted from memory " << (*i).first);
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT MDDBaseTypeOId FROM RAS_MDDBASETYPES ORDER BY MDDBaseTypeOId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::MDDBASETYPEOID);
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "read " << id << " " << id.getType());
        TALK( "got object " << id << " " << id.getType() );
        retval->insert(id);
    }

    LEAVE( "ObjectBroker::getAllMDDBaseTypes, retval=" << retval );
    RMDBGEXIT(11, RMDebug::module_adminif, "ObjectBroker", "getAllMDDBaseTypes() ");
    return retval;
}

OIdSet*
ObjectBroker::getAllMDDDimensionTypes() throw (r_Error)
{
    RMDBGENTER(11, RMDebug::module_adminif, "ObjectBroker", "getAllMDDDimensionTypes()");
    ENTER( "ObjectBroker::getAllMDDDimensionTypes" );

    OIdSet* retval = new OIdSet();
    DBObjectPMap& theMap = ObjectBroker::getMap(OId::MDDDIMTYPEOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "inserted from memory " << (*i).first);
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT MDDDimTypeOId FROM RAS_MDDDIMTYPES ORDER BY MDDDimTypeOId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::MDDDIMTYPEOID);
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "read " << id << " " << id.getType());
        TALK( "got object " << id << " " << id.getType() );
        retval->insert(id);
    }

    LEAVE( "ObjectBroker::getAllMDDDimensionTypes, retval=" << retval );
    RMDBGEXIT(11, RMDebug::module_adminif, "ObjectBroker", "getAllMDDDimensionTypes() ");
    return retval;
}

OIdSet*
ObjectBroker::getAllMDDDomainTypes() throw (r_Error)
{
    RMDBGENTER(11, RMDebug::module_adminif, "ObjectBroker", "getAllMDDDomainTypes()");
    ENTER( "ObjectBroker::getAllMDDDomainTypes" );

    OIdSet* retval = new OIdSet();
    DBObjectPMap& theMap = ObjectBroker::getMap(OId::MDDDOMTYPEOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "inserted from memory " << (*i).first);
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT MDDDomTypeOId FROM RAS_MDDDOMTYPES ORDER BY MDDDomTypeOId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::MDDDOMTYPEOID);
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "read " << id << " " << id.getType());
        TALK( "got object " << id << " " << id.getType() );
        retval->insert(id);
    }

    LEAVE( "ObjectBroker::getAllMDDDomainTypes" );
    RMDBGEXIT(11, RMDebug::module_adminif, "ObjectBroker", "getAllMDDDomainTypes() ");
    return retval;
}

OIdSet*
ObjectBroker::getAllStructTypes() throw (r_Error)
{
    RMDBGENTER(11, RMDebug::module_adminif, "ObjectBroker", "getAllStructTypes()");
    ENTER( "ObjectBroker::getAllStructTypes" );

    OIdSet* retval = new OIdSet();
    DBObjectPMap& theMap = ObjectBroker::getMap(OId::STRUCTTYPEOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "inserted from memory " << (*i).first);
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT BaseTypeId FROM RAS_BASETYPENAMES ORDER BY BaseTypeId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::STRUCTTYPEOID);
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "read " << id << " " << id.getType());
        TALK( "got object " << id << " " << id.getType() );
        retval->insert(id);
    }

    LEAVE( "ObjectBroker::getAllStructTypes, retval=" << retval );
    RMDBGEXIT(11, RMDebug::module_adminif, "ObjectBroker", "getAllStructTypes() ");
    return retval;
}

OIdSet*
ObjectBroker::getAllMDDObjects() throw (r_Error)
{
    RMDBGENTER(11, RMDebug::module_adminif, "ObjectBroker", "getAllMDDObjects()");
    ENTER( "ObjectBroker::getAllMDDObjects" );

    OIdSet* retval = new OIdSet();
    DBObjectPMap& theMap = ObjectBroker::getMap(OId::MDDOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "inserted from memory " << (*i).first);
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT MDDId FROM RAS_MDDOBJECTS ORDER BY MDDId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::MDDOID);
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "read " << id << " " << id.getType());
        TALK( "got object " << id << " " << id.getType() );
        retval->insert(id);
    }

    LEAVE( "ObjectBroker::getAllMDDObjects, retval=" << retval );
    RMDBGEXIT(11, RMDebug::module_adminif, "ObjectBroker", "getAllMDDObjects() ");
    return retval;
}

OIdSet*
ObjectBroker::getAllMDDSets() throw (r_Error)
{
    RMDBGENTER(11, RMDebug::module_adminif, "ObjectBroker", "getAllMDDSets()");
    ENTER( "ObjectBroker::getAllMDDSets" );

    OIdSet* retval = new OIdSet();
    DBObjectPMap& theMap = ObjectBroker::getMap(OId::MDDCOLLOID);
    for (DBObjectPMap::iterator i = theMap.begin(); i != theMap.end(); i++)
    {
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "inserted from memory " << (*i).first);
        retval->insert((*i).first);
    }

    OId id;
    long long oid;

    SQLiteQuery query("SELECT MDDCollId FROM RAS_MDDCOLLNAMES ORDER BY MDDCollId");
    while (query.nextRow())
    {
        oid = query.nextColumnLong();
        id = OId(oid, OId::MDDCOLLOID);
        RMDBGMIDDLE(11, RMDebug::module_adminif, "ObjectBroker", "read " << id << " " << id.getType());
        TALK( "got object " << id << " " << id.getType() );
        retval->insert(id);
    }

    LEAVE( "ObjectBroker::getAllMDDSets, retval=" << retval );
    RMDBGEXIT(11, RMDebug::module_adminif, "ObjectBroker", "getAllMDDSets() ");
    return retval;
}
