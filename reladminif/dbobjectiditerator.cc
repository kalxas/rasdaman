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

#include "dbobjectiditerator.hh"
#include "dbref.hh"  // for DBRef

template <class T>
DBObjectIdIterator<T>::DBObjectIdIterator(const DBObjectIdIterator<T> &oidlist)
    : mySet(nullptr), counter(0)
{
    mySet = oidlist.mySet;
    myIter = mySet->begin();
}

template <class T>
DBObjectIdIterator<T>::DBObjectIdIterator(
    const std::set<DBRef<T>, std::less<DBRef<T>>> &oidlist)
    : mySet(nullptr), counter(0)
{
    mySet = const_cast<std::set<DBRef<T>, std::less<DBRef<T>>> *>(&oidlist);
    myIter = mySet->begin();
}

template <class T>
void DBObjectIdIterator<T>::reset()
{
    myIter = mySet->begin();
    counter = 0;
}

template <class T>
bool DBObjectIdIterator<T>::not_done() const
{
    bool retval = false;
    if (myIter == mySet->end())
    {
        retval = false;
    }
    else
    {
        if (mySet->empty())
        {
            retval = false;
        }
        else
        {
            retval = true;
        }
    }
    return retval;
}

template <class T>
void DBObjectIdIterator<T>::advance()
{
    myIter++;
    counter++;
}

template <class T>
DBRef<T> DBObjectIdIterator<T>::get_element() const
{
    return (*myIter);
}

template <class T>
DBObjectIdIterator<T>::~DBObjectIdIterator()
{
    mySet = nullptr;
    counter = 0;
}

#include "relmddif/dbmddobj.hh"
template class DBObjectIdIterator<DBMDDObj>;
