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

#include "dbmddset.hh"                 // for DBMDDSet, DBMDDSet::DBMDDObjIdSet
#include "dbmddobj.hh"                 // for DBMDDObj
#include "mddid.hh"                    // for DBMDDObjId, DBMDDSetId, DBMDDO...
#include "reladminif/dbnamedobject.hh"   // for DBNamedObject
#include "reladminif/dbref.hh"           // for DBRef
#include "reladminif/dbobject.hh"        // for DBObject
#include "reladminif/dbobjectiditerator.hh"
#include "reladminif/objectbroker.hh"    // for ObjectBroker
#include "reladminif/oidif.hh"           // for OId, operator<<, OId::MDDCOLLOID
#include "reladminif/lists.h"            // for OIdSet
#include "relcatalogif/collectiontype.hh"  // for CollectionType
#include "relcatalogif/type.hh"            // for ostream, std::endl
#include "raslib/mddtypes.hh"          // for r_Bytes, r_Ptr
#include "raslib/error.hh"             // for r_Error, r_Error::r_Error_Obje...
#include <logging.hh>                  // for Writer, CTRACE, LTRACE, CFATAL

#include <iostream>                    // for operator<<, ostream, std::endl, bas...
#include <set>                         // for _Rb_tree_const_iterator

DBMDDSet::DBMDDSet(const char *name, const CollectionType *type)
    : DBNamedObject(name), collType(type)
{
    if (name == nullptr)
    {
        setName("unnamed collection");
    }
    if (type == nullptr)
    {
        LERROR << "Creating an MDD collection object " << name << " with null type.";
        throw r_Error(COLLECTIONTYPEISNULL);
    }
    objecttype = OId::MDDCOLLOID;
}

DBMDDSet::DBMDDSet(const OId &id)
    : DBNamedObject(id)
{
    objecttype = OId::MDDCOLLOID;
    readFromDb();
}

DBMDDSet::~DBMDDSet() noexcept(false)
{
    validate();
    collType = nullptr;
    mySet.clear();
}

void DBMDDSet::printStatus(unsigned int level, std::ostream &stream) const
{
    DBObject::printStatus(level, stream);
    stream << std::string(level * 2, ' ');
    stream << "Collection Entries: ";
    for (auto i = mySet.begin(); i != mySet.end(); i++)
    {
        if (isPersistent())
            stream << (*i).getOId() << " ";
        else
            stream << (r_Ptr)(*i).ptr() << " ";
    }
    stream << std::endl;
}

bool DBMDDSet::contains_element(const DBMDDObjId &elem) const
{
    return mySet.find(elem) != mySet.end();
}

void DBMDDSet::remove(DBMDDObjId &elem)
{
    auto i = mySet.find(elem);
    if (i != mySet.end())
    {
        elem->decrementPersRefCount();
        mySet.erase(i);
        setModified();
    }
}

void DBMDDSet::removeAll()
{
    while (!mySet.empty())
    {
        (const_cast<DBMDDObj *>((*mySet.begin()).ptr()))->decrementPersRefCount();
        mySet.erase(mySet.begin());
    }
    setModified();
}

const CollectionType *DBMDDSet::getCollType() const
{
    return collType;
}

void DBMDDSet::setCollType(const CollectionType *collTypeArg)
{
    collType = collTypeArg;
    setModified();
}


r_Bytes DBMDDSet::getMemorySize() const
{
    return DBNamedObject::getMemorySize() + sizeof(OIdSet) +
           mySet.size() * sizeof(OId);
}

void DBMDDSet::insert(DBMDDObjId elem)
{
    if (!contains_element(elem))
    {
        setModified();
        elem->incrementPersRefCount();
        mySet.insert(elem);
    }
}

DBMDDObjIdIter *DBMDDSet::newIterator() const
{
    return new DBMDDObjIdIter(mySet);
}

unsigned int DBMDDSet::getCardinality() const
{
    return mySet.size();
}

void DBMDDSet::deleteName()
{
    setName("\0");
    setModified();
}

void DBMDDSet::releaseAll()
{
}

DBMDDSetId DBMDDSet::getDBMDDSet(const char *name)
{
    return DBMDDSetId(static_cast<DBMDDSet *>(ObjectBroker::getObjectByName(OId::MDDCOLLOID, name)));
}

DBMDDSetId DBMDDSet::getDBMDDSet(const OId &o)
{
  return DBMDDSetId(static_cast<DBMDDSet *>(ObjectBroker::getObjectByOId(o)));
}

bool DBMDDSet::deleteDBMDDSet(const OId &oid)
{
    try
    {
        ObjectBroker::getObjectByOId(oid)->setPersistent(false);
    }
    catch (r_Error &e)
    {
        if (e.get_kind() == r_Error::r_Error_ObjectUnknown)
            return false;
        else
            throw;
    }
    return true;
}

bool DBMDDSet::deleteDBMDDSet(const char *name)
{
    try
    {
        ObjectBroker::getObjectByName(OId::MDDCOLLOID, name)->setPersistent(false);
    }
    catch (r_Error &e)
    {
        if (e.get_kind() == r_Error::r_Error_ObjectUnknown)
            return false;
        else
            throw;
    }
    return true;
}

void DBMDDSet::setPersistent(bool state)
{
    if (state)
    {
        if (!collType->isPersistent())
        {
            r_Error t(RASTYPEUNKNOWN);
            t.setTextParameter("type", collType->getName());
            LERROR << "Set persistence on MDD collection object " << getName()
                   << " with non-persistent type " << collType->getName();
            throw t;
        }
        DBMDDSet *set = nullptr;
        try
        {
            set = static_cast<DBMDDSet *>(
                ObjectBroker::getObjectByName(OId::MDDCOLLOID, getName()));
        }
        catch (r_Error &e)
        {
            if (e.get_kind() == r_Error::r_Error_ObjectUnknown)
                set = nullptr;
            else
                throw;
        }
        if (set)
        {
            LERROR << "MDD collection with name " << getName() << " exists already.";
            throw r_Error(r_Error::r_Error_NameNotUnique);
        }
    }
    else
    {
        removeAll();
    }
    DBNamedObject::setPersistent(state);
}

void DBMDDSet::updateInDb()
{
    deleteFromDb();
    insertInDb();
    DBObject::updateInDb();
}

