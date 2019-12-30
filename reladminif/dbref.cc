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

#include "dbref.hh"
#include "relblobif/blobtile.hh"
#include "relblobif/inlinetile.hh"
#include "relindexif/dbrcindexds.hh"
#include "relindexif/dbtcindex.hh"
#include "relmddif/dbmddobj.hh"
#include "indexmgr/hierindexds.hh"
#include "indexmgr/indexds.hh"
#include "objectbroker.hh"
#include <stdio.h>

template <class T>
bool DBRef<T>::pointerCaching = true;

template <class T>
void DBRef<T>::setPointerCaching(bool useIt)
{
    pointerCaching = useIt;
}

template <class T>
bool DBRef<T>::getPointerCaching()
{
    return pointerCaching;
}

template <class T>
DBRef<T>::DBRef(const OId &id)
    : objId(id)
{
}

template <class T>
DBRef<T>::DBRef(long long id)
    : objId(id)
{
}

template <class T>
DBRef<T>::DBRef(const DBRef<T> &src)
    : objId(src.objId), pointerValid(src.pointerValid)
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
    else if (pointerValid && src.object)
    {
        object = src.object;
    }
}

template <class T>
DBRef<T>::DBRef(T *newPtr)
    : object(newPtr), objId(OId()), pointerValid(true)
{
    if (object != nullptr)
    {
        objId = object->getOId();
        object->incrementReferenceCount();
    }
    else
    {
        pointerValid = false;
    }
}

template <class T>
DBRef<T>::~DBRef(void) noexcept(false)
{
    if ((object != nullptr) && pointerCaching)
    {
        object->decrementReferenceCount();
    }
    object = nullptr;
}

template <class T>
bool DBRef<T>::operator<(const DBRef<T> &other) const
{
    int ret = operator==(other);
    return (ret == -1);
}

template <class T>
bool operator<(const DBRef<T> &me, const DBRef<T> &him)
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
                    // this persistent
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
                            // else == -> 0
                        }
                        else    // src is transient
                        {
                            retval = +1;
                        }
                    }
                    else    // src is persistent
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
                            // else == -> 0
                        }
                    }
                }
                else    // this transient
                {
                    if (src.object)
                    {
                        if (src.object->isPersistent())
                        {
                            retval = -1;
                        }
                        else    // src is transient
                        {
                            if (object < src.object)
                            {
                                retval = -1;
                            }
                            else if (object > src.object)
                            {
                                retval = +1;
                            }
                            // else == -> 0
                        }
                    }
                    else    // src is persistent
                    {
                        retval = -1;
                    }
                }
            }
            else    // this is persistent
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
                            // else == -> 0
                        }

                    }
                    else    // src not persistent
                    {
                        retval = +1;
                    }
                }
                else    // src is persistent
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
                        // else == -> 0
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
        // else is 0
    }
    return retval;
}

template <class T>
DBRef<T> &DBRef<T>::operator=(const DBRef<T> &src)
{
    if ((object != nullptr) && pointerCaching)
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

template <class T>
DBRef<T> &DBRef<T>::operator=(T *newPtr)
{
    if ((object != nullptr) && pointerCaching)
    {
        object->decrementReferenceCount();
    }

    object = newPtr;
    if (object == nullptr)
    {
        objId = OId();
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
T &DBRef<T>::operator*(void)
{
    if (is_null())
    {
        throw r_Error(r_Error::r_Error_RefNull);
    }

    return *object;
}

template <class T>
const T &DBRef<T>::operator*(void) const
{
    if (is_null())
    {
        throw r_Error(r_Error::r_Error_RefNull);
    }

    return *object;
}

#ifndef __GNUG__

template <class T>
T &DBRef<T>::operator[](int idx) const
{
    if (is_null())
    {
        throw r_Error(r_Error::r_Error_RefNull);
    }

    return *((this + idx).object);
}

#endif

template <class T>
T *DBRef<T>::operator->(void)
{
    if (is_null())
    {
        throw r_Error(r_Error::r_Error_RefNull);
    }

    return object;
}

template <class T>
const T *DBRef<T>::operator->(void) const
{
    if (is_null())
    {
        throw r_Error(r_Error::r_Error_RefNull);
    }

    return object;
}

template <class T>
T *DBRef<T>::ptr(void)
{
    if (is_null())
    {
        throw r_Error(r_Error::r_Error_RefNull);
    }

    return object;
}

template <class T>
const T *DBRef<T>::ptr(void) const
{
    if (is_null())
    {
        throw r_Error(r_Error::r_Error_RefNull);
    }

    return object;
}

template <class T>
DBRef<T>::operator T *()
{
    if (is_null())
    {
        throw r_Error(r_Error::r_Error_RefNull);
    }

    return object;
}

template <class T>
DBRef<T>::operator const T *() const
{
    if (is_null())
    {
        throw r_Error(r_Error::r_Error_RefNull);
    }

    return object;
}

template <class T>
OId DBRef<T>::getObjId() const
{
    return objId;
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
        object = nullptr;
        objId = OId();
    }
    else
    {
        if (objId.getType() == OId::INVALID)
        {
            throw r_Error(r_Error::r_Error_OIdInvalid);
        }
        else
        {
            throw r_Error(r_Error::r_Error_ObjectUnknown);
        }
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
    if ((object != nullptr) && pointerCaching)
    {
        object->decrementReferenceCount();
    }
    object = nullptr;
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
    throw r_Error(r_Error::r_Error_DatabaseClassMismatch);
}

template <class T>
DBRef<T>::operator DBRef<DBTile>() const
{
    if (object && pointerCaching)
    {
        if ((object->getObjectType() == OId::BLOBOID) ||
                (object->getObjectType() == OId::INLINETILEOID))
        {
            return DBRef<DBTile>((DBTile *)object);
        }
    }
    else
    {
        if ((objId.getType() == OId::BLOBOID) ||
                (objId.getType() == OId::INLINETILEOID))
        {
            return DBRef<DBTile>(objId);
        }
    }
    throw r_Error(r_Error::r_Error_DatabaseClassMismatch);
}

template <class T>
DBRef<T>::operator DBRef<BLOBTile>() const
{
    if (object && pointerCaching)
    {
        if (object->getObjectType() == OId::BLOBOID ||
                (object->getObjectType() == OId::INLINETILEOID))
        {
            return DBRef<BLOBTile>((BLOBTile *)object);
        }
    }
    else
    {
        if ((objId.getType() == OId::BLOBOID) ||
                (objId.getType() == OId::INLINETILEOID))
        {
            return DBRef<BLOBTile>(objId);
        }
    }
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
    throw r_Error(r_Error::r_Error_DatabaseClassMismatch);
}

template <class T>
DBRef<T>::operator DBRef<DBHierIndex>() const
{
    if (object && pointerCaching)
    {
        if ((object->getObjectType() == OId::MDDHIERIXOID) ||
                (object->getObjectType() == OId::DBTCINDEXOID))
        {
            return DBRef<DBHierIndex>((DBHierIndex *)object);
        }
    }
    else
    {
        if ((objId.getType() == OId::MDDHIERIXOID) ||
                (objId.getType() == OId::DBTCINDEXOID))
        {
            return DBRef<DBHierIndex>(objId);
        }
    }
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
    throw r_Error(r_Error::r_Error_DatabaseClassMismatch);
}

template <class T>
DBRef<T>::operator HierIndexDS *() const
{
    if (object && pointerCaching)
    {
        if ((object->getObjectType() == OId::MDDHIERIXOID) ||
                (object->getObjectType() == OId::DBTCINDEXOID))
        {
            return (HierIndexDS *)object;
        }
    }
    else
    {
        if (objId.getType() == OId::MDDHIERIXOID)
        {
            DBRef<DBHierIndex> t(objId);
            return reinterpret_cast<HierIndexDS *>(t.ptr());
        }
        else
        {
            if (objId.getType() == OId::DBTCINDEXOID)
            {
                DBRef<DBTCIndex> t(objId);
                return reinterpret_cast<HierIndexDS *>(t.ptr());
            }
        }
    }
    throw r_Error(r_Error::r_Error_DatabaseClassMismatch);
}

template <class T>
DBRef<T>::operator IndexDS *() const
{
    if (object && pointerCaching)
    {
        if ((object->getObjectType() == OId::MDDHIERIXOID) ||
                (object->getObjectType() == OId::DBTCINDEXOID) ||
                (object->getObjectType() == OId::MDDRCIXOID))
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
            return reinterpret_cast<IndexDS *>(t.ptr());
        }
        case OId::DBTCINDEXOID:
        {
            DBRef<DBTCIndex> t(objId);
            return reinterpret_cast<IndexDS *>(t.ptr());
        }
        case OId::MDDRCIXOID:
        {
            DBRef<DBRCIndexDS> t(objId);
            return reinterpret_cast<IndexDS *>(t.ptr());
        }
        default:
            break;
        }
    }
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


template <class T>
bool DBRef<T>::is_null_ref(void) const
{
    return object == 0 || (!pointerCaching && !pointerValid);
}

template class DBRef<DBObject>;
template class DBRef<DBHierIndex>;
template class DBRef<DBRCIndexDS>;
template class DBRef<DBTCIndex>;
template class DBRef<BLOBTile>;
template class DBRef<DBTile>;
template class DBRef<InlineTile>;
template class DBRef<DBMDDObj>;
#include "relmddif/dbmddset.hh"
template class DBRef<DBMDDSet>;
#include "relcatalogif/dbminterval.hh"
template class DBRef<DBMinterval>;
#include "relcatalogif/dbnullvalues.hh"
template class DBRef<DBNullvalues>;
#include "relstorageif/dbstoragelayout.hh"
template class DBRef<DBStorageLayout>;
#include "relcatalogif/structtype.hh"
template class DBRef<StructType>;
#include "relcatalogif/settype.hh"
template class DBRef<SetType>;
#include "relcatalogif/mddtype.hh"
template class DBRef<MDDType>;
