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

#ifndef _D_SET_
#define _D_SET_

#include "rasodmg/collection.hh"

//@ManMemo: Module: {\bf rasodmg}
/**
  * \ingroup Rasodmgs
  */

/**
  The class implements a set container. It inherits most of the
  functionality from r_Collection. The set can not have
  any duplicates and it is not ordered.

*/
template <class T>
class r_Set : public r_Collection<T>
{
public:
    /// default constructor
    r_Set();
    /// copy constructor
    r_Set(const r_Set<T> &set);
    r_Set<T> &operator=(const r_Set<T> &set);
    /// virtual destructor
    virtual ~r_Set() = default;

    /// inserts an element at the beginning (no duplicates)
    virtual void insert_element(const T &element, int no_modification = 0);
    /**
      The method inserts an element into the collection. If \c no_modification
      is set, the \c mark_modified() method of r_Object is not invoked and, therefore,
      a modification will not be recognized at the commit point.
    */
};

class r_Ref_Any;
extern template class r_Set<r_Ref_Any>;
class r_GMarray;
extern template class r_Set<r_GMarray *>;

template <typename T>
class r_Ref;
class r_Object;
extern template class r_Set<r_Ref<r_Object>>;
extern template class r_Set<r_Ref<r_GMarray>>;

struct GenRefElement;
extern template class r_Set<GenRefElement *>;

//#ifdef EARLY_TEMPLATE
//#ifdef __EXECUTABLE__
//#include "rasodmg/set.cc"
//#endif
//#endif

#endif
