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

#include "dbobjectiterator.hh"
#include "dbref.hh"  // for DBRef

template <class T>
DBObjectIterator<T>::DBObjectIterator(const DBObjectIterator<T> &oidlist)
    : mySet(nullptr), counter(0)
{
    mySet = new OIdSet(*(oidlist.mySet));
    myIter = mySet->begin();
}

template <class T>
DBObjectIterator<T>::DBObjectIterator(const OIdSet &oidlist)
    : mySet(nullptr), counter(0)
{
    mySet = new OIdSet(oidlist);
    myIter = mySet->begin();
}

template <class T>
void DBObjectIterator<T>::reset()
{
    myIter = mySet->begin();
    counter = 0;
}

template <class T>
bool DBObjectIterator<T>::not_done() const
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
            if (counter == mySet->size())
            {
                retval = false;
            }
            else
            {
                retval = true;
            }
        }
    }
    return retval;
}

template <class T>
void DBObjectIterator<T>::advance()
{
    myIter++;
    counter++;
}

template <class T>
DBRef<T> DBObjectIterator<T>::get_element() const
{
    return DBRef<T>(*myIter);
}

template <class T>
DBObjectIterator<T>::~DBObjectIterator()
{
    delete mySet;
    mySet = nullptr;
    counter = 0;
}

#include "relmddif/dbmddobj.hh"
template class DBObjectIterator<DBMDDObj>;
#include "relmddif/dbmddset.hh"
template class DBObjectIterator<DBMDDSet>;
#include "relcatalogif/mddtype.hh"
template class DBObjectIterator<MDDType>;
#include "relcatalogif/structtype.hh"
template class DBObjectIterator<StructType>;
#include "relcatalogif/settype.hh"
template class DBObjectIterator<SetType>;
