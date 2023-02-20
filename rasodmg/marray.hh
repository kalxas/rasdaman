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

#ifndef _D_MARRAY_
#define _D_MARRAY_

#include "rasodmg/gmarray.hh"

#include <iosfwd>

//@ManMemo: Module: {\bf rasodmg}

/**
 The central class of the library for representing an MDD
 object is named r_Marray. Through overloading operators,
 the handling of an MDD object is similar to the usage of
 a normal C or C++ array from the programmers point of view.

*/

/**
  * \ingroup Rasodmgs
  */
template<class T>
class r_Marray : public r_GMarray
{
public:
    /// function type for initialization function
    typedef T(*r_InitFunction)(const r_Point &);

    /// default constructor (no memory is allocated!)
    r_Marray();

    /// constructor for uninitialized MDD objects
    explicit r_Marray(const r_Minterval &, r_Storage_Layout *stl = 0);
    /**
      If a storage layout pointer is provided, the object refered to is
      taken and memory control moves to the r_Marray class.
      The user has to take care, that each creation of r_Marray
      objects get a new storage layout object.
    */

    /// constructor for constant MDD objects
    r_Marray(const r_Minterval &, const T &, r_Storage_Layout *stl = 0);
    /**
      If a storage layout pointer is provided, the object refered to is
      taken and memory control moves to the r_Marray class.
      The user has to take care, that each creation of r_Marray
      objects get a new storage layout object.
    */

    /// constructor with initializing function
    explicit r_Marray(const r_Minterval &, r_InitFunction, r_Storage_Layout *stl = 0);
    /**
      If a storage layout pointer is provided, the object refered to is
      taken and memory control moves to the r_Marray class.
      The user has to take care, that each creation of r_Marray
      objects get a new storage layout object.
    */

    /// copy constructor
    r_Marray(const r_Marray<T> &);

    /// constructor getting an object of type r_GMarray
    explicit r_Marray(r_GMarray &);
    /*
      This constructor is used for converting general r_GMarray objects
      to cell type safe r_Marray objects. Care has to be taken because
      the memory of the r_GMarray can not be used anymore; it is passed
      to the r_Marray<T> object.
    */

    /// destructor
    virtual ~r_Marray();

    /// assignment: cleanup + copy
    const r_Marray &operator= (const r_Marray &);

    /// subscript operator for projection in the 1st dimension
    r_Marray<T> operator[](long) const;

    /// subscript operator for restriction/extension combination
    r_Marray<T> operator[](const r_Minterval &) const;

    /// subscript operator for read access of a cell
    const T &operator[](const r_Point &) const;

    /// subscript operator for write access of a cell
    T &operator[](const r_Point &);

    /// cast operator for converting to base type for cell access
    operator T();

    /// writes the state of the object to the specified stream
    virtual void print_status(std::ostream &s);
};

#ifdef EARLY_TEMPLATE
#ifdef __EXECUTABLE__
#include "rasodmg/marray.cc"
#endif
#endif

#endif
