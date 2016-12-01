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
/*************************************************************************
 *
 *
 * PURPOSE:
 *   Code with embedded SQL for relational DBMS
 *
 *
 * COMMENTS:
 *   none
 *
 ***********************************************************************/

// mainly for ostringstream:
#include "config.h"
#include <iostream>
#include <string>
#include <sstream>
#include <fstream>
#include <cstring>

using namespace std;

#include "objectbroker.hh"
#include "dbobject.hh"
#include "adminif.hh"
#include "externs.h"
#include "raslib/error.hh"
#include "eoid.hh"

#include <easylogging++.h>

#ifdef RMANBENCHMARK
RMTimer DBObject::readTimer = RMTimer("DBObject", "read");
RMTimer DBObject::updateTimer = RMTimer("DBObject", "update");
RMTimer DBObject::insertTimer = RMTimer("DBObject", "insert");
RMTimer DBObject::deleteTimer = RMTimer("DBObject", "delete");
#endif

const char*
BinaryRepresentation::fileTag = "RMAN";

void
DBObject::printStatus(unsigned int level, std::ostream& stream) const
{
    char* indent = new char[level * 2 + 1];
    for (unsigned int j = 0; j < level * 2 ; j++)
    {
        indent[j] = ' ';
    }
    indent[level * 2] = '\0';

    stream << indent;
    stream << myOId;
    delete[] indent;
    indent = 0;
}

r_Bytes
DBObject::getTotalStorageSize() const
{
    return 0;
}

r_Bytes
DBObject::getMemorySize() const
{
    return sizeof(DBObject);
}

void
DBObject::setCached(bool newCached)
{
    LTRACE << "setCached(" << (int)newCached << ") " << myOId;
    _isCached = newCached;
}

bool
DBObject::isCached() const
{
    LTRACE << "isCached()" << (int) _isCached;
    return _isCached;
}

void
DBObject::destroy()
{
    if (referenceCount == 0)
    {
        if (!_isCached)
        {
            //exception may be possible when !isModified()
            if (!AdminIf::isReadOnlyTA())
            {
                LTRACE << "deleting object " << myOId;
                delete this;//is dynamic and may be deleted
            }
            else
            {
                if (!_isPersistent)
                {
                    LTRACE << "deleting object " << myOId;
                    //is dynamic and may be deleted
                    delete this;
                }
            }
        }
    }
}

void
DBObject::release()
{
    LTRACE << "release() ";
    //no dynamic memory
}

void DBObject::incrementReferenceCount(void)
{
    LTRACE << "incrementReferenceCount() " << referenceCount;
    referenceCount++;
}

void DBObject::decrementReferenceCount(void)
{
    LTRACE << "decrementReferenceCount() " << referenceCount;
    referenceCount--;
    if (referenceCount == 0)
    {
        destroy();
    }
}

int
DBObject::getReferenceCount(void) const
{
    LTRACE << "getReferenceCount() " << referenceCount;
    return referenceCount;
}

//public:
DBObject::DBObject()
    :   _isPersistent(false),
        _isInDatabase(false),
        _isModified(true),
        _isCached(false),
        myOId(0),
        objecttype(OId::INVALID),
        referenceCount(0)
{
    LTRACE << "DBObject() " << myOId;
}

DBObject::DBObject(const DBObject& old)
    :   _isPersistent(old._isPersistent),
        _isInDatabase(old._isInDatabase),
        _isModified(old._isModified),
        _isCached(old._isCached),
        myOId(old.myOId),
        objecttype(old.objecttype),
        referenceCount(old.referenceCount)
{
    LTRACE << "DBObject(const DBObject& old)" << myOId;
}

//constructs an object and reads it from the database.  the oid must match the type of the object.
//a r_Error::r_Error_ObjectUnknown is thrown when the oid is not in the database.
DBObject::DBObject(const OId& id) throw (r_Error)
    :   _isCached(false),
        myOId(id),
        objecttype(id.getType()),
        referenceCount(0)
{
    //flags must be set by readFromDb()
    LTRACE << "DBObject(" << myOId << ")";
}

DBObject&
DBObject::operator=(const DBObject& old)
{
    LTRACE << "operator=(" << old.myOId << ")";
    if (this != &old)
    {
        _isPersistent = old._isPersistent;
        _isInDatabase = old._isInDatabase;
        _isModified = old._isModified;
        _isCached = old._isCached;
        objecttype = old.objecttype;
        myOId = old.myOId;
    }
    return *this;
}

//setPersistent(true) makes the object persistent as soon as validate is called.
//a r_Error::r_Error_TransactionReadOnly is thrown when the transaction is readonly.
void
DBObject::setPersistent(bool newPersistent) throw (r_Error)
{
    if (newPersistent)
    {
        //make object persistent
        if (!_isPersistent)
        {
            //object is not persistent
            if (!AdminIf::isReadOnlyTA())
            {
                //may be written to database
                OId::allocateOId(myOId, objecttype);
                _isPersistent = true;
                _isModified = true;
                ObjectBroker::registerDBObject(this);
                LTRACE << "persistent\t: yes, was not persistent";
            }
            else     //read only transaction
            {
                LFATAL << "DBObject::setPersistent() read only transaction";
                throw r_Error(r_Error::r_Error_TransactionReadOnly);
            }
        }
        else     //is already persitent
        {
            LTRACE << "persistent\t: yes, was already persistent";
        }
    }
    else     //delete the object from database
    {
        if (_isPersistent)
        {
            if (!AdminIf::isReadOnlyTA())
            {
                //may be deleted to database
                LTRACE << "persistent\t: no, was persistent";
                _isPersistent = false;
                _isModified = true;
            }
            else     //read only transaction
            {
                LFATAL << "DBObject::setPersistent() read only transaction";
                throw r_Error(r_Error::r_Error_TransactionReadOnly);
            }
        }
        else
        {
            LTRACE << "persistent\t: no, was not persistent";
        }
    }
}

//tells if an object is persistent.
bool
DBObject::isPersistent() const
{
    LTRACE << "isPersistent()" << (int)_isPersistent;
    return _isPersistent;
}

//writes the object to database/deletes it or updates it.
//a r_Error::r_Error_TransactionReadOnly is thrown when the transaction is readonly.
void
DBObject::validate() throw (r_Error)
{
    if (_isModified)
    {
        if (!AdminIf::isReadOnlyTA())
        {
            if (!AdminIf::isAborted())
            {
                if (_isInDatabase)
                {
                    if (_isPersistent)
                    {
                        LTRACE << "is persistent and modified and in database";
#ifdef RMANBENCHMARK
                        updateTimer.resume();
#endif
                        this->updateInDb();
#ifdef RMANBENCHMARK
                        updateTimer.pause();
#endif
                    }
                    else
                    {
                        LTRACE << "is not persistent and in database";
#ifdef RMANBENCHMARK
                        deleteTimer.resume();
#endif
                        this->deleteFromDb();
#ifdef RMANBENCHMARK
                        deleteTimer.pause();
#endif
                    }
                }
                else
                {
                    if (_isPersistent)
                    {
                        LTRACE << "is persistent and modified and not in database";

#ifdef RMANBENCHMARK
                        insertTimer.resume();
#endif
                        this->insertInDb();
#ifdef RMANBENCHMARK
                        insertTimer.pause();
#endif
                    }
                    else
                    {
                        //do not do anything: not in db and not persistent
                    }
                }
            }
            else
            {
                //do not do anything: is aborted
            }
        }
        else
        {
            //do not do anything: is read only
        }
    }
    else
    {
        //do not do anything: not modified
    }
}

void
DBObject::setModified() throw (r_Error)
{
    if (!AdminIf::isReadOnlyTA())
    {
        _isModified = true;
    }
    else
    {
        LTRACE << "readonly transaction " << myOId;
        //this happens really a lot.
        //LFATAL << "DBObject::setModified() read only transaction";
        //throw r_Error(r_Error::r_Error_TransactionReadOnly);
        _isModified = true;
    }
}

bool
DBObject::isModified() const
{
    LTRACE << "isModified() " << (int)_isModified;
    return _isModified;
}

OId
DBObject::getOId() const
{
//    LTRACE << "getOId() " << myOId;
    return myOId;
}

EOId
DBObject::getEOId() const
{
    LTRACE << "getEOId() " << myOId;
    return EOId(myOId);
}

OId::OIdType
DBObject::getObjectType() const
{
    LTRACE << "getObjectType() " << objecttype;
    return objecttype;
}

DBObject::~DBObject()
{
    ObjectBroker::deregisterDBObject(myOId);
}

void
DBObject::updateInDb() throw (r_Error)
{
    _isModified = false;
    _isInDatabase = true;
    _isPersistent = true;
}

//writes the object into the database.  the object must not be in the database.
void
DBObject::insertInDb() throw (r_Error)
{
    _isModified = false;
    _isInDatabase = true;
    _isPersistent = true;
}

void
DBObject::deleteFromDb() throw (r_Error)
{
    _isModified = false;
    _isInDatabase = false;
    _isPersistent = false;
}

void
DBObject::readFromDb() throw (r_Error)
{
    _isPersistent = true;
    _isModified = false;
    _isInDatabase = true;
}

BinaryRepresentation
DBObject::getBinaryRepresentation() const throw (r_Error)
{
    LFATAL << "getBinaryRepresentation() for " << objecttype << " not implemented";
    throw r_Error(BINARYEXPORTNOTSUPPORTEDFOROBJECT);
}

void
DBObject::setBinaryRepresentation(__attribute__((unused)) const BinaryRepresentation& br) throw (r_Error)
{
    LFATAL << "setBinaryRepresentation() for " << objecttype << " not implemented";
    throw r_Error(BINARYIMPORTNOTSUPPORTEDFOROBJECT);
}

char*
DBObject::getBinaryName() const
{
    //if we use 64bit oids we have at most 20 digits + "_" + type
    ostringstream o;
    o << static_cast<int>(objecttype) << '_' << myOId.getCounter() << ".raw";
    const char* temp = o.str().c_str();
    char* retval = new char[strlen(temp) + 1];
    memcpy(retval, temp, strlen(temp) + 1);
    return retval;
}
