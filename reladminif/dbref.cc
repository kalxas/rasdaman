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
/*************************************************************
 *
 *
 * PURPOSE:
 *   DBRef is a smart pointer for managing objects derived from
 *   the DbObject class.
 *
 *
 * COMMENTS:
 *
 ************************************************************/


#include "config.h"
#include "dbref.hh"
#include <iostream>
#include <stdio.h>
#include "objectbroker.hh"
#include "indexmgr/indexds.hh"
#include "relindexif/dbrcindexds.hh"
#include "relindexif/dbtcindex.hh"
#include "indexmgr/hierindexds.hh"
#include "debug/debug.hh"
#include <logging.hh>

template <class T> bool
DBRef<T>::pointerCaching = true;

template <class T> void
DBRef<T>::setPointerCaching(bool useIt)
{
    LTRACE << "setPointerCaching(" << useIt << ") " << pointerCaching;
    pointerCaching = useIt;
}

template <class T> bool
DBRef<T>::getPointerCaching()
{
    LTRACE << "getPointerCaching() " << pointerCaching;
    return pointerCaching;
}

template <class T>
DBRef<T>::DBRef(void)
    :   object(0),
        objId(DBOBJID_NONE),
        pointerValid(false)
{
    LTRACE << "DBRef()";
}


template <class T>
DBRef<T>::DBRef(const OId &id)
    :   object(0),
        objId(id),
        pointerValid(false)
{
    LTRACE << "DBRef(" << id << ")";
}


template <class T>
DBRef<T>::DBRef(long long id)
    :   object(0),
        objId(id),
        pointerValid(false)
{
    LTRACE << "DBRef(long long " << id << ")";
}


template <class T>
DBRef<T>::DBRef(const DBRef<T> &src)
    :   object(0),
        objId(src.objId),
        pointerValid(src.pointerValid)
{
    if (pointerCaching)
    {
        if (src.object)
        {
            object = src.object;
            objId = object->getOId();
            object->incrementReferenceCount();
        }
    }
    else
    {
        if (pointerValid && src.object)
        {
            object = src.object;
        }
    }

}


template <class T>
DBRef<T>::DBRef(T *newPtr)
    :   object(newPtr),
        objId(DBOBJID_NONE),
        pointerValid(true)
{

    if (object != 0)
    {
        objId = object->getOId();
        object->incrementReferenceCount();
        LTRACE << "DBRef(T* " << newPtr->getOId() << ")";
    }
    else
    {
        pointerValid = false;
        LTRACE << "DBRef(T* 0) " << objId;
    }
}


template <class T>
DBRef<T>::~DBRef(void) noexcept(false)
{
    if ((object != 0) && pointerCaching)
    {
        object->decrementReferenceCount();
    }
    object = 0;
}

template <class T>
bool DBRef<T>::operator<(const DBRef<T> &other) const
{
    int ret = operator==(other);
    return (ret == -1);
}

template <class T>
bool operator< (const DBRef<T> &me, const DBRef<T> &him)
{
    return me.operator < (him);
}

template <class T>
int DBRef<T>::operator==(const DBRef<T> &src) const
{
    int retval = 0;
    if (isInitialised())
    {
        if (src.isInitialised())
        {
            if (object)
            {
                if (object->isPersistent())
                {
                    //this persistent
                    if (src.object)
                    {
                        if (src.object->isPersistent())
                        {
                            if (object->getOId() < src.object->getOId())
                            {
                                retval = -1;
                            }
                            else if (object->getOId() > src.object->getOId())
                            {
                                retval = +1;
                            }
                            //else == -> 0
                        }
                        else     //src is transient
                        {
                            retval = +1;
                        }
                    }
                    else     //src is persistent
                    {
                        if (object->getOId() < src.objId)
                        {
                            retval = -1;
                        }
                        else
                        {
                            if (object->getOId() > src.objId)
                            {
                                retval = +1;
                            }
                            //else == -> 0
                        }
                    }
                }
                else     //this transient
                {
                    if (src.object)
                    {
                        if (src.object->isPersistent())
                        {
                            retval = -1;
                        }
                        else     //src is transient
                        {
                            if (object < src.object)
                            {
                                retval = -1;
                            }
                            else if (object > src.object)
                            {
                                retval = +1;
                            }
                            //else == -> 0
                        }
                    }
                    else     //src is persistent
                    {
                        retval = -1;
                    }
                }
            }
            else     //this is persistent
            {
                if (src.object)
                {
                    if (src.object->isPersistent())
                    {
                        if (objId < src.object->getOId())
                        {
                            retval = -1;
                        }
                        else
                        {
                            if (objId > src.object->getOId())
                            {
                                retval = +1;
                            }
                            //else == -> 0
                        }

                    }
                    else     //src not persistent
                    {
                        retval = +1;
                    }
                }
                else     //src is persistent
                {
                    if (objId < src.objId)
                    {
                        retval = -1;
                    }
                    else
                    {
                        if (objId > src.objId)
                        {
                            retval = +1;
                        }
                        //else == -> 0
                    }
                }
            }
        }
        else
        {
            retval = +1;
        }
    }
    else
    {
        if (src.isInitialised())
        {
            retval = -1;
        }
        //else is 0
    }
    return retval;
}


template <class T>
DBRef<T> &DBRef<T>::operator=(const DBRef<T> &src)
{
    if ((object != 0) && pointerCaching)
    {
        object->decrementReferenceCount();
    }
    object = src.object;
    pointerValid = src.pointerValid;
    objId = src.objId;
    if (pointerCaching)
    {
        if (object)
        {
            objId = object->getOId();
            object->incrementReferenceCount();
        }
    }
    else
    {
        if (object && pointerValid)
        {
            objId = object->getOId();
        }
    }

    return *this;
}


template<class T>
DBRef<T> &DBRef<T>::operator=(T *newPtr)
{
    if ((object != 0) && pointerCaching)
    {
        object->decrementReferenceCount();
    }

    object = newPtr;
    if (object == 0)
    {
        objId = DBOBJID_NONE;
        pointerValid = false;
    }
    else
    {
        objId = object->getOId();
        object->incrementReferenceCount();
        pointerValid = true;
    }
    return *this;
}


template <class T>
T &DBRef<T>::operator *(void)
{
    if (is_null())
    {
        LDEBUG << "DBRef::operator*(): object not found " << objId;
        LTRACE << "object was not found " << objId;
        r_Error err = r_Error(r_Error::r_Error_RefNull);
        throw err;
    }

    return *object;
}


template <class T>
const T &DBRef<T>::operator *(void) const
{
    if (is_null())
    {
        LDEBUG << "DBRef::operator*(): object not found " << objId;
        LTRACE << "object was not found " << objId;
        r_Error err = r_Error(r_Error::r_Error_RefNull);
        throw err;
    }

    return *object;
}


#ifndef __GNUG__

template <class T>
T &DBRef<T>::operator[](int idx) const
{

    if (is_null())
    {
        LDEBUG << "DBRef::operator[](): object not found " << objId;
        LTRACE << "object was not found " << objId;
        r_Error err = r_Error(r_Error::r_Error_RefNull);
        throw err;
    }

    return *((this + idx).object);
}

#endif

template <class T>
T *DBRef<T>::operator->(void)
{
    if (is_null())
    {
        LDEBUG << "DBRef::operator->(): object not found " << objId;
        LTRACE << "object was not found " << objId;
        r_Error err = r_Error(r_Error::r_Error_RefNull);
        throw err;
    }

    return object;
}


template <class T>
const T *DBRef<T>::operator->(void) const
{
    if (is_null())
    {
        LDEBUG << "DBRef::operator->(): object not found " << objId;
        LTRACE << "object was not found " << objId;
        r_Error err = r_Error(r_Error::r_Error_RefNull);
        throw err;
    }

    return object;
}


template <class T>
T *DBRef<T>::ptr(void)
{
    if (is_null())
    {
        LDEBUG << "DBRef::ptr(): object not found " << objId;
        LTRACE << "object was not found " << objId;
        r_Error err = r_Error(r_Error::r_Error_RefNull);
        throw err;
    }

    return object;
}


template <class T>
const T *DBRef<T>::ptr(void) const
{
    if (is_null())
    {
        LDEBUG << "DBRef::ptr(): object not found " << objId;
        LTRACE << "object was not found " << objId;
        r_Error err = r_Error(r_Error::r_Error_RefNull);
        throw err;
    }

    return object;
}

template <class T>
OId DBRef<T>::getObjId()
{
    return objId;
}


template <class T>
DBRef<T>::operator T *()
{
    if (is_null())
    {
        LDEBUG << "DBRef::T*(): object not found " << objId;
        LTRACE << "object was not found " << objId;
        r_Error err = r_Error(r_Error::r_Error_RefNull);
        throw err;
    }

    return object;
}


template <class T>
DBRef<T>::operator const T *() const
{
    if (is_null())
    {
        LDEBUG << "DBRef::T*(): object not found " << objId;
        LTRACE << "object was not found " << objId;
        r_Error err = r_Error(r_Error::r_Error_RefNull);
        throw err;
    }

    return object;
}


template <class T>
OId DBRef<T>::getOId(void) const
{
    if (object && pointerCaching)
    {
        (static_cast<DBRef<T>>(*this)).objId = object->getOId();
    }
    return objId;
}

template <class T>
void DBRef<T>::delete_object(void)
{
    if (!is_null())
    {
        object->setPersistent(false);
        object->decrementReferenceCount();
        object = 0;
        objId = DBOBJID_NONE;
    }
    else
    {
        r_Error err;
        if (objId.getType() == OId::INVALID)
        {
            err = r_Error(r_Error::r_Error_OIdInvalid);
        }
        else
        {
            err = r_Error(r_Error::r_Error_ObjectUnknown);
        }
        LTRACE << "delete_object() " << objId << " not ok";
        throw err;
    }
}

template <class T>
bool DBRef<T>::isInitialised() const
{
    bool retval = false;
    if (object)
    {
        retval = true;
    }
    else
    {
        if (objId.getType() != OId::INVALID)
        {
            retval = true;
        }
    }
    return retval;
}

template <class T>
bool DBRef<T>::is_valid(void) const
{
    bool retval = false;
    if (!is_null())
    {
        retval = true;
    }
    return retval;
}


template <class T>
void DBRef<T>::release()
{
    if ((object != 0) && pointerCaching)
    {
        object->decrementReferenceCount();
    }
    object = 0;
}


template <class T>
DBRef<T>::operator DBRef<DBObject>() const
{
    if (object && pointerCaching)
    {
        return DBRef<DBObject>(object);
    }
    else
    {
        return DBRef<DBObject>(objId);
    }
}

template <class T>
DBRef<T>::operator DBRef<InlineTile>() const
{
    if (object && pointerCaching)
    {
        if (object->getObjectType() == OId::INLINETILEOID)
        {
            return DBRef<InlineTile>((InlineTile *)object);
        }
    }
    else
    {
        if (objId.getType() == OId::INLINETILEOID)
        {
            return DBRef<InlineTile>(objId);
        }
    }
    LDEBUG << "DBRef::<InlineTile>(): operator mismatch" << objId;
    throw r_Error(r_Error::r_Error_DatabaseClassMismatch);
}

template <class T>
DBRef<T>::operator DBRef<DBTile>() const
{
    if (object && pointerCaching)
    {
        if ((object->getObjectType() == OId::BLOBOID) || (object->getObjectType() == OId::INLINETILEOID))
        {
            return DBRef<DBTile>((DBTile *)object);
        }
    }
    else
    {
        if ((objId.getType() == OId::BLOBOID) || (objId.getType() == OId::INLINETILEOID))
        {
            return DBRef<DBTile>(objId);
        }
    }
    if (object)
    {
        LDEBUG << "DBRef::DBRef<DBTile>(): object->getObjectType()=" << object->getObjectType();
    }
    LDEBUG << "DBRef::DBRef<DBTile>():  objId->getObjectType()=" <<  objId.getType();
    LDEBUG << "DBRef::DBRef<DBTile>(): operator mismatch" << objId;
    if (object)
    {
        LTRACE << "object->getObjectType()=" << object->getObjectType();
    }
    LTRACE << "objId->getObjectType()=" <<  objId.getType();
    throw r_Error(r_Error::r_Error_DatabaseClassMismatch);
}

template <class T>
DBRef<T>::operator DBRef<BLOBTile>() const
{
    if (object && pointerCaching)
    {
        if (object->getObjectType() == OId::BLOBOID || (object->getObjectType() == OId::INLINETILEOID))
        {
            return DBRef<BLOBTile>((BLOBTile *)object);
        }
    }
    else
    {
        if ((objId.getType() == OId::BLOBOID) || (objId.getType() == OId::INLINETILEOID))
        {
            return DBRef<BLOBTile>(objId);
        }
    }
    LDEBUG << "DBRef::DBRef<BLOBTile>(): operator mismatch" << objId;
    throw r_Error(r_Error::r_Error_DatabaseClassMismatch);
}

template <class T>
DBRef<T>::operator DBRef<DBTCIndex>() const
{
    if (object && pointerCaching)
    {
        if (object->getObjectType() == OId::DBTCINDEXOID)
        {
            return DBRef<DBTCIndex>((DBTCIndex *)object);
        }
    }
    else
    {
        if (objId.getType() == OId::DBTCINDEXOID)
        {
            return DBRef<DBTCIndex>(objId);
        }
    }
    LDEBUG << "DBRef::DBRef<DBTCIndex>(): operator mismatch" << objId;
    throw r_Error(r_Error::r_Error_DatabaseClassMismatch);
}

template <class T>
DBRef<T>::operator DBRef<DBHierIndex>() const
{
    if (object && pointerCaching)
    {
        if ((object->getObjectType() == OId::MDDHIERIXOID) || (object->getObjectType() == OId::DBTCINDEXOID))
        {
            return DBRef<DBHierIndex>((DBHierIndex *)object);
        }
    }
    else
    {
        if ((objId.getType() == OId::MDDHIERIXOID) || (objId.getType() == OId::DBTCINDEXOID))
        {
            return DBRef<DBHierIndex>(objId);
        }
    }
    LDEBUG << "DBRef::DBRef<DBHierIndex>(): operator mismatch" << objId;
    throw r_Error(r_Error::r_Error_DatabaseClassMismatch);
}


template <class T>
DBRef<T>::operator DBRef<DBRCIndexDS>() const
{
    if (object && pointerCaching)
    {
        if (object->getObjectType() == OId::MDDRCIXOID)
        {
            return DBRef<DBRCIndexDS>((DBRCIndexDS *)object);
        }
    }
    else
    {
        if (objId.getType() == OId::MDDRCIXOID)
        {
            return DBRef<DBRCIndexDS>(objId);
        }
    }
    LDEBUG << "DBRef::DBRef<DBRCIndexDS>(): operator mismatch" << objId;
    throw r_Error(r_Error::r_Error_DatabaseClassMismatch);
}


template <class T>
DBRef<T>::operator HierIndexDS *() const
{
    if (object && pointerCaching)
    {
        if ((object->getObjectType() == OId::MDDHIERIXOID) || (object->getObjectType() == OId::DBTCINDEXOID))
        {
            return (HierIndexDS *)object;
        }
    }
    else
    {
        if (objId.getType() == OId::MDDHIERIXOID)
        {
            DBRef<DBHierIndex> t(objId);
            return static_cast<HierIndexDS *>(t.ptr());
        }
        else
        {
            if (objId.getType() == OId::DBTCINDEXOID)
            {
                DBRef<DBTCIndex> t(objId);
                return static_cast<HierIndexDS *>(t.ptr());
            }
        }
    }
    LDEBUG << "DBRef::HierIndexDS*(): operator mismatch" << objId;
    throw r_Error(r_Error::r_Error_DatabaseClassMismatch);
}


template <class T>
DBRef<T>::operator IndexDS *() const
{
    if (object && pointerCaching)
    {
        if ((object->getObjectType() == OId::MDDHIERIXOID) || (object->getObjectType() == OId::DBTCINDEXOID) || (object->getObjectType() == OId::MDDRCIXOID))
        {
            return (IndexDS *)object;
        }
    }
    else
    {
        switch (objId.getType())
        {
        case OId::MDDHIERIXOID:
        {
            DBRef<DBHierIndex> t(objId);
            return static_cast<IndexDS *>(t.ptr());
        }
        case OId::DBTCINDEXOID:
        {
            DBRef<DBTCIndex> t(objId);
            return static_cast<IndexDS *>(t.ptr());
        }
        case OId::MDDRCIXOID:
        {
            DBRef<DBRCIndexDS> t(objId);
            return static_cast<IndexDS *>(t.ptr());
        }
        default:
            break;
        }
    }
    LDEBUG << "DBRef::IndexDS*(): operator mismatch" << objId;
    throw r_Error(r_Error::r_Error_DatabaseClassMismatch);
}


template <class T>
bool DBRef<T>::is_null(void) const
{
    if (object == 0 || (!pointerCaching && !pointerValid))
    {
        if (objId.getType() == OId::INVALID)
        {
            throw r_Error(r_Error::r_Error_OIdInvalid);
        }
        try
        {
            T *t = static_cast<T *>(ObjectBroker::getObjectByOId(objId));
            t->incrementReferenceCount();
            (const_cast<DBRef<T>*>(this))->object = t;
            LTRACE << "found object " << object << " with oid " << objId
                   << " in database and increased ref count";
        }
        catch (const r_Error &err)
        {
            if (err.get_kind() == r_Error::r_Error_ObjectUnknown)
            {
                return true;
            }
            else
            {
                throw;
            }
        }
    }
    return false;
}
