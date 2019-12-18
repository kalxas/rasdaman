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
 * SOURCE: marray.cc
 *
 * MODULE: rasodmg
 * CLASS:  r_Marray
 *
 * COMMENTS:
 *      None
*/

#include "config.h"
#include "rasodmg/marray.hh"

#include "rasodmg/database.hh"
#include "clientcomm/clientcomm.hh"

#include "raslib/rmdebug.hh"

#include <string.h>  // for memcpy()
#include <iostream>
#include <iomanip>



template<class T>
r_Marray<T>::r_Marray()
    : r_GMarray()
{
}



template<class T>
r_Marray<T>::r_Marray(const r_Minterval &initDomain, r_Storage_Layout *stl)
    : r_GMarray(initDomain, sizeof(T), stl)
{
}



template<class T>
r_Marray<T>::r_Marray(const r_Minterval &initDomain, const T &value, r_Storage_Layout *stl)
    : r_GMarray(initDomain, sizeof(T), stl)
{
    T *dataPtr = (T *)data;

    for (unsigned long i = 0; i < domain.cell_count(); i++)
    {
        dataPtr[i] = value;
    }
}



template<class T>
r_Marray<T>::r_Marray(const r_Minterval &initDomain, r_InitFunction function, r_Storage_Layout *stl)
    : r_GMarray(initDomain, sizeof(T), stl)
{
    r_Dimension d;
    int         done = 0;
    r_Point     pt(domain.dimension());

    // memory pointer of type T
    T *dataPtr = (T *)data;

    // initialize the iterating point to the lowest values in each dimension
    for (d = 0; d < pt.dimension(); d++)
    {
        pt[d] = domain[d].low();
    }

    // iterate point pt through the spatial domain and apply the
    // initializing function for each point
    while (!done)
    {
        // execute function on cell
        dataPtr[domain.cell_offset(pt)] = (*function)(pt);

        // increment coordinate
        d = 0;
        while (++pt[d] > domain[d].high())
        {
            pt[d] = domain[d].low();
            d++;
            if (d >= domain.dimension())
            {
                done = 1;
                break;
            }
        }
    }
}



template<class T>
r_Marray<T>::r_Marray(const r_Marray<T> &obj)
    : r_GMarray(obj)
{
}



template<class T>
r_Marray<T>::r_Marray(r_GMarray &obj)
    : r_GMarray(obj)
{
}



template<class T>
r_Marray<T>::~r_Marray()
{
}



template<class T>
const r_Marray<T> &r_Marray<T>::operator=(const r_Marray<T> &marray)
{
    return (r_Marray<T> &) r_GMarray::operator=(marray);
}



template<class T>
r_Marray<T>
r_Marray<T>::operator[](long cordnt) const

{
    // check if self does not just represent a cell
    if (domain.dimension() == 0)
    {
        throw (r_Eindex_violation(cordnt, 0, 0));
    }

    // check if the index is valid
    if (cordnt < domain[0].low() || cordnt > domain[0].high())
    {
        throw (r_Eindex_violation(cordnt, domain[0].low(), domain[0].high()));
    }

    // build a new spatial domain
    r_Minterval newDomain(domain.dimension() - 1);

    // and initialize it
    for (unsigned int i = 0; i < newDomain.dimension(); i++)
    {
        newDomain[i] = domain[i + 1];
    }

    // build a new Marray
    r_Marray<T> newMDD(newDomain);

    // and fill it with data
    unsigned long newCellCount = newDomain.cell_count();
    unsigned long byteCount    = (newDomain.dimension() ? newDomain.cell_count() : 1) * type_length;
    T            *dataPtr      = (T *)data; // typed pointer to the data

    memcpy(newMDD.data, &(dataPtr[(cordnt - static_cast<long>(domain[0].low())) * static_cast<long>(newCellCount)]),
            static_cast<unsigned int>(byteCount));

    return newMDD;
}



template<class T>
r_Marray<T>
r_Marray<T>::operator[](const r_Minterval &mint) const

{
    unsigned long offset;
    r_Point  pt;
    int      pt_valid;

    // first test dimensionality
    if (domain.dimension() != mint.dimension())
    {
        throw (r_Edim_mismatch(domain.dimension(), mint.dimension()));
    }

    // build a new Marray with undefined cells
    r_Marray<T> newMDD(mint);
    T          *typedDataPtr = (T *)newMDD.data;

    // iterate through the domain and fill the values where available
    for (offset = 0; offset < mint.cell_count(); offset++)
    {
        pt = mint.cell_point(offset);

        // Test if pt is a valid index in the spatial domain of the
        // self object.
        pt_valid = 1;
        for (unsigned int dim = 0; dim < domain.dimension() && pt_valid; dim++)
        {
            pt_valid &= pt[dim] >= domain[dim].low() && pt[dim] <= domain[dim].high();
        }

        if (pt_valid)
        {
            typedDataPtr[offset] = operator[](pt);
        }

        // The points where pt_valid is not true are undefined, so nothing has to be
        // done in the catch clause.
        // Attention: Purify is reporting that unitialized memory is read when accessing
        //            these points.
    }

    return newMDD;
}



template<class T>
const T &
r_Marray<T>::operator[](const r_Point &point) const

{
    // first test dimensionality
    if (point.dimension() != domain.dimension())
    {
        throw (r_Edim_mismatch(point.dimension(), domain.dimension()));
    }

    T *typedDataPtr = (T *)data;

    try
    {
        return typedDataPtr[ domain.cell_offset(point) ];
    }
    catch (...)   // exception can be r_Eindex_violation
    {
        throw;      // rethrow it
    }
}



template<class T>
T &
r_Marray<T>::operator[](const r_Point &point)

{
    // first test dimensionality
    if (point.dimension() != domain.dimension())
    {
        throw (r_Edim_mismatch(point.dimension(), domain.dimension()));
    }

    T *typedDataPtr = (T *)data;

    try
    {
        return typedDataPtr[ domain.cell_offset(point) ];
    }
    catch (...)   // exception can be r_Eindex_violation
    {
        throw;      // rethrow it
    }
}



template<class T>
r_Marray<T>::operator T()

{
    // check if the spatial domain of self is really zero
    if (domain.dimension() > 0 || data == 0)
    {
        throw r_Eno_cell();
    }

    return *((T *)data);
}



template<class T>
void
r_Marray<T>::print_status(std::ostream &s)
{
    r_GMarray::print_status(s);
}



