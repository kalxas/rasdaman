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

#include "rasodmg/set.hh"

class r_GMarray;

template<class T>
r_Set<T>::r_Set() : r_Collection<T>()
{
    this->allowsDuplicates = 0;
    this->isOrdered = 0;
    this->card = 0;
}

template<class T>
r_Set<T>::r_Set(const r_Set<T> &set) : r_Collection<T>(set)
{
    this->allowsDuplicates = 0;
    this->isOrdered = 0;
}

template<class T>
r_Set<T> &r_Set<T>::operator=(const r_Set<T> &o)
{
    if (this == &o)
        return *this;
    r_Collection<T>::operator=(o);
    this->allowsDuplicates = 0;
    this->isOrdered = 0;
    return *this;
}

template<class T>
void
r_Set<T>::insert_element(const T &element, int no_modification)
{
    typename r_Collection<T>::CNode *ptr = static_cast<typename r_Collection<T>::CNode *>(this->coll);

    while (ptr->next != NULL && *(static_cast<T *>(ptr->elem)) != element)
    {
        ptr = ptr->next;
    }

    if (ptr->elem == NULL || *(static_cast<T *>(ptr->elem)) != element)
    {
        r_Collection<T>::insert_element(element, no_modification);
    }
}

#include "rasodmg/ref.hh"
template class r_Set<r_Ref_Any>;
#include "rasodmg/gmarray.hh"
template class r_Set<r_GMarray *>;
#include "rasodmg/object.hh"
template class r_Set<r_Ref<r_Object>>;
template class r_Set<r_Ref<r_GMarray>>;
#include "genreftype.hh"
template class r_Set<GenRefElement *>;
