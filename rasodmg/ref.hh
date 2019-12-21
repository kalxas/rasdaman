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
/**
 * INCLUDE:  ref.hh
 *
 * MODULE:   rasodmg
 * CLASS:    r_Ref, r_Ref_Any
 *
 * COMMENTS:
 *      None
*/

#ifndef _D_REF_
#define _D_REF_

#include "raslib/oid.hh"

class r_Object;
class r_Point;
class r_Sinterval;
class r_Minterval;
class r_Scalar;
class r_Primitive;
class r_Structure;
class r_Transaction;

//@ManMemo: Module: {\bf rasodmg}

/**
  The class r_Ref_Any is defined to support a reference to any
  type. Its primary purpose is to handle generic refernces and allow
  conversions of r_Refs in the type hierarchy. A r_Ref_Any
  object can be used as an intermediary between any two types r_Ref<X>
  and r_Ref<Y> where X and Y are different types. A r_Ref<T> can
  always be converted to a r_Ref_Any; there is a function to perform
  the conversion in the r_Ref<T> template. Each r_Ref<T> class
  has a constructor and assignment operator that takes a reference to a
  r_Ref_Any.
*/

/**
  * \ingroup Rasodmgs
  */
class r_Ref_Any
{
public:
    /// default constructor
    r_Ref_Any();

    /// copy constructor
    r_Ref_Any(const r_Ref_Any &);

    /// constructor for creating a reference with an oid
    r_Ref_Any(const r_OId &initOId, r_Transaction *ta = NULL);
    /**
      Dereferencing the self object results in loading the object with {\tt initOId}.
    */

    /// constructor getting a pointer to a persistent capable object
    r_Ref_Any(r_Object *);

    /// constructor getting a general pointer
    r_Ref_Any(void *);

    /// destructor deletes referenced object from main memory and database
    ~r_Ref_Any();

    /// assignment operator for assigning a r_Ref_Any pointer
    r_Ref_Any &operator=(const r_Ref_Any &);

    /// assignment operator for assigning a pointer to a persistent capable object
    r_Ref_Any &operator=(r_Object *);

    /// delete from main memory
    void destroy();

    /// deletes referenced object from main memory and database
    void delete_object();

    //@Man: Cast operators:
    //@{
    ///

    ///
    operator const void *() const;
    ///
    operator void *();
    ///
    operator r_Point *();
    ///
    operator r_Sinterval *();
    ///
    operator r_Minterval *();
    ///
    operator r_OId *();
    ///
    operator r_Scalar *();
    ///
    operator r_Structure *();
    ///
    operator r_Primitive *();

    ///
    //@}


    /// operator for validity test
    int operator!() const;

    /// method for reference validity test
    int is_null() const;
    /**
      The method delivers true iff the oid and/or the memory pointer are valid.
    */

    //@Man: Comparison operators:
    //@{
    ///

    ///
    int operator==(const r_Ref_Any &) const;
    ///
    int operator!=(const r_Ref_Any &) const;
    /// compares the memory pointer (does not load the object)
    int operator==(const r_Object *) const;
    /// compares the memory pointer (does not load the object)
    int operator!=(const r_Object *) const;

    ///
    //@}

    /// get oid
    inline const r_OId &get_oid() const;

    //@Man: Methods for internal use only
    //@{
    ///
    /// constructor getting oid and memory pointer
    r_Ref_Any(const r_OId &, r_Object *, r_Transaction *ta = NULL);
    ///
    inline unsigned int is_oid_valid() const;
    /// get memory pointer (without loading the object)
    void *get_memory_ptr() const;
    /// get transaction
    r_Transaction *get_transaction() const;
    ///
    //@}

protected:
    r_Transaction *ta{NULL};

private:
    /// main memory pointer
    void *memptr{NULL};

    /// object identifier
    r_OId oid;
};




//@ManMemo: Module: {\bf rasodmg}

/**
  References of type r_Ref in many respects behave like
  C++ pointers but provide an additional mechanism that guarantees
  integrity in references to persistent objects. r_Ref
  implements a so called {\bf smart pointer} which behaves like
  a C++ pointer but can do additional things in time of dereferencing
  the pointer. In case that no valid memory pointer is available,
  which means that the object is not present, and an oid is existing,
  the object belonging to the oid is read from the db and the new
  memory pointer is given back.
*/

/**
  * \ingroup Rasodmgs
  */
template<class T>
class r_Ref
{
public:
    /// default constructor
    r_Ref();

    /// constructor for r_Ref_Any objects
    r_Ref(const r_Ref_Any &);

    /// constructor for creating a reference with an oid
    r_Ref(const r_OId &initOId, r_Transaction *ta = NULL);
    /**
      Dereferencing the self object results in loading the object with {\tt initOId}.
    */

    /// copy constructor
    r_Ref(const r_Ref<T> &);

    /// destructor deletes referenced object from main memory and database
    ~r_Ref();

    /// cast to r_Ref_Any
    operator r_Ref_Any() const;

    // cast to const r_Ref_Any
    // operator const r_Ref_Any() const;

    /// assignment operator for assigning a r_Ref_Any
    r_Ref<T> &operator=(const r_Ref_Any &);

    /// assignment operator for assigning a C pointer
    r_Ref<T> &operator=(T *);

    // assignment operator for assigning a r_Ref pointer
    // r_Ref<T>& operator=( r_Ref<T>& );

    /// assignment operator for assigning a r_Ref pointer
    r_Ref<T> &operator=(const r_Ref<T> &);

    /// dereference operator (error kinds: r_Error_RefNull, r_Error_RefInvalid)
    const T &operator*() const;

    /// dereference operator (error kinds: r_Error_RefNull, r_Error_RefInvalid)
    T &operator*();
    /**
      If the memory pointer is zero and the oid is valid, the object is loaded from the server
      and a reference to the object in memory is returned.
    */

    const T *operator->() const;

    /// operator for dereferencing the reference (error kinds: r_Error_RefNull, r_Error_RefInvalid)
    T *operator->();
    /**
      If the memory pointer is zero and the oid is valid, the object is loaded from the server
      and the new memory location is returned.
    */

    const T *ptr() const;

    /// method for dereferencing the reference (error kinds: r_Error_RefNull, r_Error_RefInvalid)
    T *ptr();
    /**
      If the memory pointer is zero and the oid is valid, the object is loaded from the server
      and the new memory location is returned.
    */

    /// operator for validity test
    int operator!() const;

    /// method for reference validity test
    int is_null() const;
    /**
      The method delivers true iff the oid and/or the memory pointer are valid.
    */

    //@Man: Comparison operators:
    //@{
    ///

    ///
    int operator==(const r_Ref<T> &refR) const;
    ///
    int operator!=(const r_Ref<T> &refR) const;
    /// compares the memory pointer (does not load the object)
    int operator==(const T *) const;
    /// compares the memory pointer (does not load the object)
    int operator!=(const T *) const;

    ///
    //@}

    /// delete from main memory
    void destroy();

    /// deletes referenced object from main memory and database
    void delete_object();

    /// get oid
    inline const r_OId &get_oid() const;

    //@Man: Methods for internal use only
    //@{
    ///
    /// constructor getting memory pointer
    r_Ref(T *, r_Transaction *ta = NULL);

    /// constructor getting oid and memory pointer
    r_Ref(const r_OId &, T *, r_Transaction *ta = NULL);

    /// get memory pointer (without loading the object)
    T *get_memory_ptr() const;

    ///
    inline unsigned int is_oid_valid() const;

    ///
    //@}

protected:
    r_Transaction *ta{NULL};

private:
    /// loads an object from database
    void load_object() const;

    /// main memory pointer
    mutable T   *memptr;

    /// object identifier
    r_OId oid;
};

#include "rasodmg/ref.icc"

#endif
