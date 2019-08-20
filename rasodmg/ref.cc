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

#include "rasodmg/database.hh"
#include "rasodmg/ref.hh"


#include <sstream>

#include "rasodmg/transaction.hh"
#include "clientcomm/clientcomm.hh"

#include <logging.hh>


// forward declaration needed because of EARLY_TEMPLATE
class r_Transaction;

// In case of early templates take care non-template code is only
// compiled once.
#ifndef __EXECUTABLE__

r_Ref_Any::r_Ref_Any()
{
    LTRACE << "r_Ref_Any", "r_Ref_Any()";
}



r_Ref_Any::r_Ref_Any(const r_Ref_Any &obj)
{
    LTRACE << "r_Ref_Any( const r_Ref_Any& )";
    memptr       = obj.memptr;
    oid          = obj.oid;
    ta           = obj.ta;
}



r_Ref_Any::r_Ref_Any(const r_OId &initOId, r_Transaction *taArg)
    : ta{taArg}, memptr(0), oid{initOId}
{
    LTRACE << "r_Ref_Any( const r_OId& )";
}



r_Ref_Any::r_Ref_Any(r_Object *ptr)
    : ta{ptr->get_transaction()}, memptr(ptr), oid()
{
    LTRACE << "r_Ref_Any( r_Object* )";
}



r_Ref_Any::r_Ref_Any(void *ptr)
    : memptr(ptr), oid()
{
    LTRACE << "r_Ref_Any( void* )";
}


r_Ref_Any::r_Ref_Any(const r_OId &initOId, r_Object *ptr, r_Transaction *taArg)
    : ta{taArg}, memptr(ptr), oid(initOId)
{
    LTRACE << "r_Ref_Any( const r_OId &oid, const r_Object* )";
}



r_Ref_Any::~r_Ref_Any()
{
    //LTRACE << "~r_Ref_Any()";

    // object should not be delete from databse when reference destructor is called
    //  if( memptr ){
    //    delete memptr;
    //    memptr = 0;
    //  }
}



r_Ref_Any &
r_Ref_Any::operator=(const r_Ref_Any &objptr)
{
    ta        = objptr.ta;
    memptr    = objptr.memptr;
    oid       = objptr.oid;

    return *this;
}



r_Ref_Any &
r_Ref_Any::operator=(r_Object *ptr)
{
    ta        = ptr->get_transaction();
    memptr    = ptr;
    oid       = r_OId();

    return *this;
}



void
r_Ref_Any::destroy()
{
    LTRACE << "destroy()";

    if (memptr && !oid.is_valid())
    {
        delete static_cast<r_Object *>(memptr);
        memptr = 0;
    }
}


void
r_Ref_Any::delete_object()
{
    LTRACE << "delete_object()";

    if (memptr)
    {
        delete static_cast<r_Object *>(memptr);
        memptr = 0;
    }
}



r_Ref_Any::operator const void *() const
{
    return memptr;
}



r_Ref_Any::operator void *()
{
    return memptr;
}



r_Ref_Any::operator r_Point *()
{
    return static_cast<r_Point *>(memptr);
}



r_Ref_Any::operator r_Sinterval *()
{
    return static_cast<r_Sinterval *>(memptr);
}



r_Ref_Any::operator r_Minterval *()
{
    return static_cast<r_Minterval *>(memptr);
}



r_Ref_Any::operator r_OId *()
{
    return static_cast<r_OId *>(memptr);
}



r_Ref_Any::operator r_Scalar *()
{
    return static_cast<r_Scalar *>(memptr);
}



r_Ref_Any::operator r_Primitive *()
{
    return static_cast<r_Primitive *>(memptr);
}



r_Ref_Any::operator r_Structure *()
{
    return static_cast<r_Structure *>(memptr);
}



int
r_Ref_Any::operator!() const
{
    return !is_null();
}



int
r_Ref_Any::is_null() const
{
    return (memptr == 0) && !oid.is_valid();
}



int
r_Ref_Any::operator==(const r_Ref_Any &ref) const
{
    LTRACE << "operator==( const r_Ref_Any& )";
    return // both refs are not valid or ...
        (is_null() && ref.is_null()) ||
        // both oids are valid and the same or ...
        (oid.is_valid() && oid == ref.oid) ||
        // both oids are not valid and memory pointers are the same
        (!oid.is_valid() && !ref.oid.is_valid() && memptr == ref.memptr);
}



int
r_Ref_Any::operator!=(const r_Ref_Any &ref) const
{
    LTRACE << "operator!=( const r_Ref_Any& )";
    return !operator==(ref);
}



int
r_Ref_Any::operator==(const r_Object *ptr) const
{
    LTRACE << "operator==( const r_Object* )";
    return memptr == static_cast<void *>(const_cast<r_Object *>(ptr));
}



int
r_Ref_Any::operator!=(const r_Object *ptr) const
{
    LTRACE << "operator!=( const r_Object* )";
    return !operator==(ptr);
}




void *
r_Ref_Any::get_memory_ptr() const
{
    return memptr;
}

r_Transaction *r_Ref_Any::get_transaction() const
{
    return ta;
}

#endif  // __EXECUTABLE__



template<class T>
r_Ref<T>::r_Ref()
{
    LTRACE << "r_Ref()";
}



template<class T>
r_Ref<T>::r_Ref(const r_Ref<T> &obj)
{
    LTRACE << "r_Ref( const r_Ref<T>& )";
    ta           = obj.ta;
    memptr       = obj.memptr;
    oid          = obj.oid;
}



template<class T>
r_Ref<T>::r_Ref(const r_OId &initOId, r_Transaction *taArg)
    : ta{taArg}, memptr(0), oid{initOId}
{
    LTRACE << "r_Ref( const r_OId& )";
}



template<class T>
r_Ref<T>::r_Ref(const r_Ref_Any &obj)
{
    LTRACE << "r_Ref( const r_Ref_Any& )";

    ta        = obj.get_transaction();
    memptr    = static_cast<T *>(obj.get_memory_ptr());
    oid       = obj.get_oid();
}



template<class T>
r_Ref<T>::r_Ref(T *newPtr, r_Transaction *taArg)
    : ta{taArg}, memptr(newPtr), oid()
{
    LTRACE << "r_Ref( const T* )";
}



template<class T>
r_Ref<T>::r_Ref(const r_OId &initOId, T *newPtr, r_Transaction *taArg)
    : ta{taArg}, memptr(newPtr), oid(initOId)
{
    LTRACE << "r_Ref( const r_OId &oid, const T* )";
}



template<class T>
r_Ref<T>::~r_Ref()
{
    //LTRACE << "~r_Ref()";

    // object should not be delete from databse when reference destructor is called
    //  if( memptr ){
    //    delete memptr;
    //    memptr = 0;
    //  }
}



template<class T>
r_Ref<T>::operator r_Ref_Any() const
{
    LTRACE << "operator r_Ref_Any()";
    return r_Ref_Any(oid, (r_Object *)memptr, ta);
}


/*
template<class T>
r_Ref<T>::operator const r_Ref_Any() const
{
  LTRACE << "operator const r_Ref_Any()";
  return r_Ref_Any( oid, memptr );
}
*/

template<class T>
r_Ref<T> &
r_Ref<T>::operator=(const r_Ref_Any &newPtr)
{
    ta        = newPtr.get_transaction();
    memptr    = static_cast<T *>(newPtr.get_memory_ptr());
    oid       = newPtr.get_oid();

    return *this;
}



template<class T>
r_Ref<T> &
r_Ref<T>::operator=(T *newPtr)
{
    memptr    = newPtr;
    oid       = r_OId();

    return *this;
}



/*
template<class T>
r_Ref<T>&
r_Ref<T>::operator=( r_Ref<T>& objptr )
{
  memptr    = objptr.memptr;
  oid       = objptr.oid;

  return *this;
}
*/


template<class T>
r_Ref<T> &
r_Ref<T>::operator=(const r_Ref<T> &objptr)
{
    ta        = objptr.ta;
    memptr    = objptr.memptr;
    oid       = objptr.oid;

    return *this;
}



template<class T>
const T &
r_Ref<T>::operator*() const
{
    LTRACE << "operator*()";
    if (!memptr)
    {
        load_object();
    }

    if (!memptr)
    {
        throw r_Error(r_Error::r_Error_RefNull);
    }

    return *memptr;
}


template<class T>
T &
r_Ref<T>::operator*()
{
    LTRACE << "operator*()";
    if (!memptr)
    {
        load_object();
    }

    if (!memptr)
    {
        throw r_Error(r_Error::r_Error_RefNull);
    }

    return *memptr;
}



template<class T>
const T *
r_Ref<T>::operator->() const
{
    LTRACE << "operator->()";
    if (!memptr)
    {
        load_object();
    }

    if (!memptr)
    {
        throw r_Error(r_Error::r_Error_RefNull);
    }

    return memptr;
}

template<class T>
T *
r_Ref<T>::operator->()
{
    LTRACE << "operator->()";
    if (!memptr)
    {
        load_object();
    }

    if (!memptr)
    {
        throw r_Error(r_Error::r_Error_RefNull);
    }

    return memptr;
}



template<class T>
const T *
r_Ref<T>::ptr() const
{
    LTRACE << "ptr()";
    if (!memptr)
    {
        load_object();
    }

    return memptr;
}


template<class T>
T *
r_Ref<T>::ptr()
{
    LTRACE << "ptr()";
    if (!memptr)
    {
        load_object();
    }

    return memptr;
}



template<class T>
int
r_Ref<T>::operator!() const
{
    return !is_null();
}



template<class T>
int
r_Ref<T>::is_null() const
{
    return (memptr == 0) && !oid.is_valid();
}



template<class T>
int
r_Ref<T>::operator==(const r_Ref<T> &refR) const
{
    LTRACE << "operator==( const r_Ref<T>& )";
    return // both refs are not valid or ...
        (is_null() && refR.is_null()) ||
        // both oids are valid and the same or ...
        (oid.is_valid() && oid == refR.oid) ||
        // both oids are not valid and memory pointers are the same
        (!oid.is_valid() && !refR.oid.is_valid() && memptr == refR.memptr);
}



template<class T>
int
r_Ref<T>::operator!=(const r_Ref<T> &refR) const
{
    LTRACE << "operator!=( const r_Ref<T>& )";
    return !operator==(refR);
}



template<class T>
int
r_Ref<T>::operator==(const T *newPtr) const
{
    LTRACE << "operator==( const T* )";
    return memptr == newPtr;
}



template<class T>
int
r_Ref<T>::operator!=(const T *newPtr) const
{
    LTRACE << "operator!=( const T* )";
    return !operator==(newPtr);
}


template<class T>
void
r_Ref<T>::destroy()
{
    LTRACE << "destroy()";

    if (memptr && !oid.is_valid())
    {
        delete memptr;
        memptr = 0;
    }
}



template<class T>
void
r_Ref<T>::delete_object()
{
    LTRACE << "delete_object()";

    if (memptr)
    {
        delete memptr;
        memptr = 0;
    }
}



template<class T>
T *
r_Ref<T>::get_memory_ptr() const
{
    return memptr;
}

#ifdef DEF_TRANSACTION

template<class T>
void
r_Ref<T>::load_object() const
{
    if (oid.is_valid())
    {
        auto *tmpTa = ta == NULL ? r_Transaction::actual_transaction : ta;
        if (tmpTa == 0 || tmpTa->get_status() != r_Transaction::active)
        {
            throw r_Error(r_Error::r_Error_TransactionNotOpen);
        }

        auto *db = ta->getDatabase();
        if (db == 0 || db->get_status() == r_Database::not_open)
        {
            throw r_Error(r_Error::r_Error_DatabaseClosed);
        }

        // load object and take its memory pointer
        r_Ref<T> ref = ta->load_object(oid);
        memptr       = ref.get_memory_ptr();
    }
}

#endif
