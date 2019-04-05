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
#include "dbmddset.hh"                 // for DBMDDSet, DBMDDSet::DBMDDObjIdSet
#include "dbmddobj.hh"                 // for DBMDDObj
#include "reladminif/dbref.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/externs.h"
#include "reladminif/objectbroker.hh"
#include "relcatalogif/collectiontype.hh"
#include "reladminif/dbobjectiditerator.hh"
#include "raslib/error.hh"             // for r_Error, r_Error::r_Error_Obje...
#include <logging.hh>                  // for Writer, CTRACE, LTRACE, CFATAL

#include <iostream>                    // for operator<<, ostream, std::endl, bas...
#include <set>                         // for _Rb_tree_const_iterator

void DBMDDSet::printStatus(unsigned int level, std::ostream &stream) const
{
    auto *indent = new char[level * 2 + 1];
    for (unsigned int j = 0; j < level * 2; j++)
    {
        indent[j] = ' ';
    }
    indent[level * 2] = '\0';
    DBObject::printStatus(level, stream);
    stream << indent;
    stream << "Collection Entries ";
    for (auto i = mySet.begin(); i != mySet.end(); i++)
    {
        if (isPersistent())
        {
            stream << (*i).getOId() << " ";
        }
        else
        {
            stream << (r_Ptr)(*i).ptr() << " ";
        }
    }
    stream << std::endl;
    delete[] indent;
    indent = nullptr;
}

bool DBMDDSet::contains_element(const DBMDDObjId &elem) const
{
    bool retval = false;
    auto i = mySet.find(elem);
    if (i != mySet.end())
    {
        retval = true;
    }
    return retval;
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
    DBMDDObjId t;
    while (!mySet.empty())
    {
        (const_cast<DBMDDObj *>((*(mySet.begin())).ptr()))->decrementPersRefCount();
        mySet.erase(mySet.begin());
    }
    setModified();
}

DBMDDSet::~DBMDDSet() noexcept(false)
{
    validate();
    collType = nullptr;
    mySet.clear();
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
    LTRACE << "releaseAll() " << myOId << "\tdoes not do anything";
}

DBMDDSetId DBMDDSet::getDBMDDSet(const char *name)
{
    DBMDDSetId set(static_cast<DBMDDSet *>(ObjectBroker::getObjectByName(OId::MDDCOLLOID, name)));
    return set;
}

DBMDDSetId DBMDDSet::getDBMDDSet(const OId &o)
{
    DBMDDSetId set(static_cast<DBMDDSet *>(ObjectBroker::getObjectByOId(o)));
    return set;
}

bool DBMDDSet::deleteDBMDDSet(const OId &oid)
{
    bool retval = true;
    try
    {
        DBObject *set = ObjectBroker::getObjectByOId(oid);
        set->setPersistent(false);
    }
    catch (r_Error &e)
    {
        if (e.get_kind() == r_Error::r_Error_ObjectUnknown)
        {
            retval = false;
        }
        else
        {
            throw;
        }
    }
    return retval;
}

bool DBMDDSet::deleteDBMDDSet(const char *name)
{
    bool retval = true;
    try
    {
        DBObject *set = ObjectBroker::getObjectByName(OId::MDDCOLLOID, name);
        set->setPersistent(false);
    }
    catch (r_Error &e)
    {
        if (e.get_kind() == r_Error::r_Error_ObjectUnknown)
        {
            retval = false;
        }
        else
        {
            throw;
        }
    }
    return retval;
}

DBMDDSet::DBMDDSet(const char *name, const CollectionType *type)
    : DBNamedObject(name), collType(type)
{
    if (name == nullptr)
    {
        setName("unnamed collection");
    }
    if (type == nullptr)
    {
        LERROR << "the collection type for " << name << " is NULL";
        throw r_Error(COLLECTIONTYPEISNULL);
    }
    objecttype = OId::MDDCOLLOID;
}

void DBMDDSet::setPersistent(bool state)
{
    DBMDDSet *set = nullptr;
    if (state)
    {
        if (!collType->isPersistent())
        {
            r_Error t(RASTYPEUNKNOWN);
            t.setTextParameter("type", collType->getName());
            LTRACE << "setPersistent(" << state << ") " << getName() << ", "
                   << collType->getName() << " not persistent";
            throw t;
        }
        try
        {
            set = static_cast<DBMDDSet *>(
                      ObjectBroker::getObjectByName(OId::MDDCOLLOID, getName()));
        }
        catch (r_Error &err)
        {
            if (err.get_kind() == r_Error::r_Error_ObjectUnknown)
            {
                set = nullptr;
            }
            else
            {
                throw;
            }
        }
        if (set)
        {
            LERROR << "mdd collection with name \"" << getName() << "\" exists already";
            throw r_Error(r_Error::r_Error_NameNotUnique);
        }
    }
    else
    {
        removeAll();
    }
    DBNamedObject::setPersistent(state);
}

DBMDDSet::DBMDDSet(const OId &id)
    : DBNamedObject(id), collType(nullptr)
{
    objecttype = OId::MDDCOLLOID;
    readFromDb();
}

void DBMDDSet::updateInDb()
{
    deleteFromDb();
    insertInDb();
    DBObject::updateInDb();
}

