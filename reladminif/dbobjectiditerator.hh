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
/*
 * 2007-may-10  CJ     fixed compilation errors (gcc 4.1)
*/

#pragma once

#include "lists.h"

#include <functional>  // for less
#include <set>         // for set

class ObjectBroker;
template <class T>
class DBObjectIdIterator;
template <class T>
class DBRef;

//@ManMemo: Module: {\bf reladminif}.
/*@Doc:

this object gets a set of oids.  when an oid is accessed the iterator returns a
DBRef to the oid.

*/

/**
  * \ingroup Reladminifs
  */
template <class T>
class DBObjectIdIterator
{
public:
    DBObjectIdIterator(const std::set<DBRef<T>, std::less<DBRef<T>>> &oidlist);
    /*@Doc:
    constructs a new Iterator.  the OIdSet will be deleted by the DBOBjectIdIterator.
    there may be oids of objects with other classes present.  you should be carefull
    when using this feature - as in mddtypes/mddbasetypes/mdddim/domtypes
    */

    DBObjectIdIterator(const DBObjectIdIterator<T> &it);
    /*@Doc:
    */

    ~DBObjectIdIterator();
    /*@Doc:
    deletes the OIdSet passed to it in the constructor
    */

    void reset();
    /*@Doc:
    resets the iterator to the beginning
    */

    bool not_done() const;
    /*@Doc:
    checks if there are more elements
    */

    void advance();
    /*@Doc:
    advances the iterator one entry
    */

    DBRef<T> get_element() const;
    /*@Doc:
    returns an dbref<object>
    */

private:
    typename std::set<DBRef<T>, std::less<DBRef<T>>>::iterator myIter;
    /*@Doc:
    internal pointer where the iterator is
    */

    std::set<DBRef<T>, std::less<DBRef<T>>> *mySet;
    /*@Doc:
    the actual list which is used to lookup objects by the objectbroker
    */

    unsigned int counter;
    /*@Doc:
    holds the actual position in set
    */
};

class DBMDDObj;
extern template class DBObjectIdIterator<DBMDDObj>;

