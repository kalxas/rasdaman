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
 * SOURCE: iterator.cc
 *
 * MODULE: rasodmg
 * CLASS:  r_Iterator
 *
 * COMMENTS:
 *      None
*/

#include "rasodmg/iterator.hh"
#include <iostream>

using namespace std;

#ifdef OBJECT_NOT_SET
#undef __EXECUTABLE__
#endif

#ifdef EARLY_TEMPLATE
#ifndef __EXECUTABLE__
#define __EXECUTABLE__
#define ITERATOR_NOT_SET
#endif
#endif

#include "rasodmg/collection.hh"

#ifdef ITERATOR_NOT_SET
#undef __EXECUTABLE__
#endif

template<class T>
r_Iterator<T>::r_Iterator(const r_Iterator<T> &iter)
{
    collection = iter.collection;
    ptr = iter.ptr;
    ndone = iter.ndone;
}

template<class T>
r_Iterator<T>::r_Iterator(r_Collection<T> &source, int removed_objects)
{
    collection = &source;    
    ptr = static_cast<typename r_Collection<T>::CNode *>(
                removed_objects ? source.removed_objects : source.coll);
    ndone = ptr->elem != 0;
}

template<class T>
r_Iterator<T> &
r_Iterator<T>::operator=(const r_Iterator<T> &iter)
{
    if (this != &iter)
    {
        collection = iter.collection;
        ptr        = iter.ptr;
        ndone      = iter.ndone;
    }
    return *this;
}

template<class T>
int
r_Iterator<T>::is_equal(const r_Iterator<T> &iter) const
{
    return collection == iter.collection && ptr == iter.ptr;
}

template<class T>
int
operator==(const r_Iterator<T> &iter1, const r_Iterator<T> &iter2)
{
    return iter1.is_equal(iter2);
}

template<class T>
int
operator!=(const r_Iterator<T> &iter1, const r_Iterator<T> &iter2)
{
    return !iter1.is_equal(iter2);
}

template<class T>
r_Iterator<T> &
r_Iterator<T>::operator++()
{
    // ++prefix operator
    if (!ndone)
        throw r_Error(r_Error::r_Error_IteratorExhausted);
    if (ptr->next != 0)
        ptr = ptr->next;
    else
        ndone = false;
    return *this;
}

template<class T>
r_Iterator<T>
r_Iterator<T>::operator++(int)
{
    // postfix++ operator
    // create a copy of this, increment the original and return the copy
    r_Iterator<T> result(*this);
    operator++();
    return result;
}

template<class T>
T
r_Iterator<T>::operator*()
{
    if (!ndone || ptr->elem == 0)
        throw r_Error(r_Error::r_Error_IteratorExhausted);
    
    return *(ptr->elem);
}

template<class T>
T
r_Iterator<T>::get_element() const
{
    if (!ndone || ptr->elem == 0)
        throw r_Error(r_Error::r_Error_IteratorExhausted);
    else
        return *(ptr->elem);
}

template<class T>
bool
r_Iterator<T>::next(T &element)
{
    if (!ndone || ptr->elem == 0)
        return false;
    element = *(ptr->elem);
    advance();
    return true;
}

template<class T>
void
r_Iterator<T>::reset(int removed_objects)
{
    ptr = static_cast<typename r_Collection<T>::CNode *>(
                removed_objects ? collection->removed_objects : collection->coll);
    ndone = (ptr->elem != 0);
}

template<class T>
void
r_Iterator<T>::advance()
{
    if (!ndone)
        throw r_Error(r_Error::r_Error_IteratorExhausted);
    if (ptr->next != 0)
        ptr = ptr->next;
    else
        ndone = false;
}
