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
#pragma once

#include "binaryrepresentation.hh"  // for BinaryRepresentation
#include "oidif.hh"                 // for OId, OId::OIdType
#include "raslib/error.hh"
#include "raslib/mddtypes.hh"   // for r_Bytes

#include <iosfwd>

class DBObject;
class EOId;
template <class T>
class DBRef;
using DBObjectId = DBRef<DBObject>;

//@ManMemo: Module: {\bf reladminif}.
/*@Doc:
Base class for all persistent classes.  it supplies functionality for reference counting,
management by ObjectBroker, modification management, cache size.

a persistent DBObject has a unique OId.  this oid identifies it in the database.
*/
/**
  * \ingroup Reladminifs
  */
class DBObject
{
public:
    virtual void destroy(void);
    /*@Doc:
    deletes the object if and only if
        the refence count is zero and
        the object is not cached and
        the transaction is not readonly.
    */

    int getReferenceCount(void) const;
    /*@Doc:
    Returns the number of references that exist for this object
    */

    void incrementReferenceCount(void);
    /*@Doc:
    Reference counting functions. These should be private with friend DBRef, but the compiler
    doesn't allow friend template classes, only instances. Don't call directly, only DBRef
    is allowed to.
    */

    void decrementReferenceCount(void);
    /*@Doc:
    Reference counting functions. These should be private with friend DBRef, but the compiler
    doesn't allow friend template classes, only instances. Don't call directly, only DBRef
    is allowed to.
    */

    virtual ~DBObject() noexcept(false);
    /*@Doc:
    deregisters this object with the ObjectBroker.
    the oid is not invalidated to allow the ObjectBroker to kill it.
    */

    DBObject();
    /*@Doc:
    initializes all attributes.  the oid is set to 0.  the objecttype is set to OId::INVALID.
    */

    DBObject(const DBObject &);
    /*@Doc:
    clones all attributes.
    */

    DBObject(const OId &id);
    /*@Doc:
    initializes only referenceCount, myOId, objecttype. _isCached is set to 0.
    Subclasses may contain a call to readFromDb which can throw a r_Error_ObjectUnknown
    or database related exception.
    */

    virtual void setPersistent(bool newPersistent);
    /*@Doc:
    setPersistent(true) makes the object persistent as soon as validate is called.
    a r_Error_TransactionReadOnly is thrown when the transaction is readonly.
    setPersistent(false) deletes the object from the database as soon as validate is called.
    if the state of the object changes in a read only transaction
    (e.g. persistent->non persistent) a r_Error_TransactionReadOnly is thrown.
    */

    bool isPersistent() const;
    /*@Doc:
    tells if an object is persistent.
    */

    void validate();
    /*@Doc:
    writes the object to database/deletes it or updates it.
    any r_Errors from insertInDb, updateInDb, deleteFromDb, readFromDb are passed to the
    caller.
    */

    virtual void setModified();
    /*@Doc:
    marks this object dirty.
    a r_Error_TransactionReadOnly should be thrown when the transaction is readonly.
    this is because of o2 related stuff in dbmddobject.  should be taken care of.
    */

    bool isModified() const;
    /*@Doc:
    tells if this object is dirty.
    */

    OId getOId() const;
    /*@Doc:
    returns the oid of this object
    */

    EOId getEOId() const;
    /*@Doc:
    returns the EOId of this object.
    */

    OId::OIdType getObjectType() const;
    /*@Doc:
    returns the type of this object (MDDSet, ...).  objects which are not
    yet persistent also return their object type.
    */

    DBObject &operator=(const DBObject &) = default;
    /*@Doc:
    clones all DBObject attributes except reference count.
    */

    bool isCached() const;
    /*@Doc:
    tells if this object is cached.
    */

    virtual void setCached(bool newCached);
    /*@Doc:

    */

    virtual r_Bytes getMemorySize() const;
    /*@Doc:
    returns the memory space used by this object.
    */

    virtual void printStatus(unsigned int level, std::ostream &stream) const;
    /*@Doc:
    prints the indent, OIdType, myOId as long and myOId as double.
    */

    virtual r_Bytes getTotalStorageSize() const;
    /*@Doc:
    returns the space taken up by this object in the database.
    as dbobject has no persistent attributes it returns 0.
    */

    virtual BinaryRepresentation getBinaryRepresentation() const;
    /*@Doc:
    returns the binary representation fit for storage in the database
    */

    virtual void setBinaryRepresentation(const BinaryRepresentation &);
    /*@Doc:
    set the objects state from the binary represenation
    */

    virtual char *getBinaryName() const;
    /*Doc:
    create the name for binary represenation from the oid
    */

#ifdef RMANBENCHMARK
    static RMTimer readTimer;

    static RMTimer updateTimer;

    static RMTimer insertTimer;

    static RMTimer deleteTimer;
#endif

protected:
    virtual void release();
    /*@Doc:
    releases all dynamic memory (references to other persistent obkjects).
    this is needed for cross transactional caches.
    dbobject does not have any dynamic memory.
    */

    virtual void readFromDb();
    /*@Doc:
    reads the object from database.  myOId must be set!!!
    a r_Error_ObjectUnknown is thrown when the oid is not in the database.
    */

    virtual void insertInDb();
    /*@Doc:
    writes the object into the database.  the object must not be in the database.
    a r_Error_TransactionReadOnly is thrown when the transaction is readonly.
    this implementation checks for readOnlyTA
    */

    virtual void deleteFromDb();
    /*@Doc:
    deletes an object in the database.
    a r_Error_TransactionReadOnly is thrown when the transaction is readonly.
    this implementation checks for readOnlyTA
    */

    virtual void updateInDb();
    /*@Doc:
    updates an existing object in the database
    a r_Error_TransactionReadOnly is thrown when the transaction is readonly.
    this implementation checks for readOnlyTA
    */

    bool _isPersistent{false};
    /*@Doc:
    tells, if this object should be in the database or not.
    */

    bool _isInDatabase{false};
    /*@Doc:
    tells, if this object is already in the database.
    */

    bool _isModified{false};
    /*@Doc:
    tells, if this object has changed an should be updated in the database.
    */

    bool _isCached{false};
    /*@Doc:
    tells , if this objects is cached or not
    */

    bool _validationFailed{false};
    /*@Doc:
    set to true if validate() failed on this object
    */

    OId myOId;
    /*@Doc:
    the oid of this object.  is set by setPersistent(1) or by the constructor
    of optional persistent objects (types).
    */

    OId::OIdType objecttype{OId::INVALID};
    /*@Doc:
    the type of this object.  needed by setPersistent(1) to allocate the correct oid.
    */

    int referenceCount{0};
    /*@Doc:
    The number of references that exist for this file. Use get_reference() for reading
    its value from derived classes. Modified by incr_reference and decr_reference.
    */

private:

    void sharedObjectInit(void);
    /*@Doc:
    Initialization code shared by all constructors.
    does not do anything.
    */
};

