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
 * SOURCE: Minterval.cc
 *
 * MODULE: raslib
 * CLASS:   r_Minterval
 *
 * COMMENTS:
 *
*/

#include "config.h"
#include "raslib/minterval.hh"
#include "raslib/odmgtypes.hh"
#include "raslib/dlist.hh"
#include "mymalloc/mymalloc.h"
#include "minterval.hh"


#include <logging.hh>

#include <math.h>
#include <string.h>
#include <stdlib.h>

#include <sstream>
#include <memory>

using namespace std;

r_Minterval::r_Minterval(const r_Point& low, const r_Point& high)
{
    dimensionality = low.dimension();
    if (dimensionality != high.dimension())
    {
        throw r_Edim_mismatch(low.dimension(), high.dimension());
    }
    std::unique_ptr<r_Sinterval> temp_intervals(new r_Sinterval[ dimensionality ]);


    for (int i = 0; i < low.dimension(); ++i)
    {
        r_Sinterval aux;
        if (low[i] > high[i])
        {
            throw r_Elimits_mismatch(low[i], high[i]);
        }
        aux.set_low(low[i]);
        aux.set_high(high[i]);
        temp_intervals.get()[i] = aux;
    }

    intervals = temp_intervals.release();
}

r_Minterval::r_Minterval(r_Dimension dim)
    : intervals(NULL),
      dimensionality(dim),
      streamInitCnt(0)
{
    intervals = new r_Sinterval[ dimensionality ];
}

void
r_Minterval::constructorinit(char* mIntStr)
{

    if (!mIntStr)
    {
        LERROR << "cannot create minterval from null string.";
        throw r_Eno_interval();
    }

    char* p = NULL; // for counting ','
    // for parsing the string
    std::istringstream str(mIntStr);
    char c = 0;
    r_Sinterval sint;
    r_Range b = 0; // bound for Sinterval

    if (intervals != NULL)
    {
        delete intervals;
        intervals = NULL;
    }

    // calculate dimensionality
    p = mIntStr;
    while ((p = strchr(++p, ',')))
    {
        dimensionality++;
    }

    // allocate space for intervals
    intervals = new r_Sinterval[ dimensionality ];

    // check for left bracket '['
    str >> c;
    if (c != '[')
    {
        // error, should perhaps raise exception
        dimensionality = 0;
        delete[] intervals;
        intervals = NULL;
        LERROR << "cannot create minterval from string (" << mIntStr 
                << ") that is not of the pattern [a:b,c:d,..].";
        throw r_Eno_interval();
    }

    // for each dimension: get sinterval
    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        // --- evaluate lower bound ------------------------------
        str >> c;           // test read first char
        if (c == '*')           // low bound is '*'
        {
            sint.set_low('*');
        }
        else                // low bound must be a number
        {
            str.putback(c);
            str >> b;       // read type r_Range
            if (! str)          // check for proper int recognition
            {
                LERROR << "minterval constructor from string (" << mIntStr 
                        << ") failed on lower bound of dimension " << i;
                throw r_Eno_interval();
            }
            sint.set_low(b);    // store lo bound
        }

        // --- check for ':' between lower and upper bound -------
        str >> c;
        if (c != ':')
        {
            // error
            dimensionality = 0;
            delete[] intervals;
            intervals = NULL;
            LERROR << "cannot create minterval from string (" << mIntStr 
                    << ") that is not of the pattern [a:b,c:d,..].";
            throw r_Eno_interval();
        }

        // --- evaluate upper bound ------------------------------
        str >> c;
        if (c == '*')
        {
            sint.set_high('*');
        }
        else
        {
            str.putback(c);
            str >> b;
            if (! str)
            {
                LERROR << "minterval constructor from string (" << mIntStr 
                        << ") failed on upper bound of dimension " << i;
                throw r_Eno_interval();
            }
            sint.set_high(b);
        }
        str >> c;

        // --- next dimension needs either ',' separator or ']' end tag
        if ((i != dimensionality - 1 && c != ',') || (i == dimensionality - 1 && c != ']'))
        {
            dimensionality = 0;
            delete[] intervals;
            intervals = NULL;
            LERROR << "cannot create minterval from string (" << mIntStr 
                    << ") that is not of the pattern [a:b,c:d,..].";
            throw r_Eno_interval();
        }

        intervals[i] = sint;

        sint.set_interval('*', '*');
    }
}

r_Minterval::r_Minterval(char* mIntStr)
    :   intervals(NULL),
        dimensionality(1),
        streamInitCnt(0)
{
    constructorinit(mIntStr);
}

r_Minterval::r_Minterval(const char* mIntStr)
    :   intervals(NULL),
        dimensionality(1),
        streamInitCnt(0)
{
    char* temp = static_cast<char*>(mymalloc((1 + strlen(mIntStr)) * sizeof(char)));
    strcpy(temp, mIntStr);

    try
    {
        constructorinit(temp);
        free(temp);
    }
    catch (r_Error err)
    {
        free(temp);
        throw;
    }

    temp = 0;
}

r_Minterval&
r_Minterval::operator<<(const r_Sinterval& newInterval)
{
    if (streamInitCnt >= dimensionality)
    {
        LERROR << "cannot add interval (" << newInterval << "), domain is already full";
        throw r_Einit_overflow();
    }

    intervals[streamInitCnt++] = newInterval;
    return *this;
}

r_Minterval&
r_Minterval::operator<<(r_Range p)
{
    if (streamInitCnt >= dimensionality)
    {
        LERROR << "cannot add interval (" << p << ":" << p << "), domain is already full";
        throw r_Einit_overflow();
    }

    intervals[streamInitCnt++] = r_Sinterval(p, p);
    return *this;
}

r_Minterval::r_Minterval()
    :   intervals(NULL),
        dimensionality(0),
        streamInitCnt(0)
{
    //LTRACE << "r_Minterval(), this=" << this;
}

//cannot use the initialise function because it will crash
r_Minterval::r_Minterval(const r_Minterval& minterval)
    :   intervals(NULL),
        dimensionality(0),
        streamInitCnt(0)
{
//    LTRACE << "r_Minterval(const r_Minterval&), this=" << this;
    dimensionality = minterval.dimensionality;
    streamInitCnt = minterval.streamInitCnt;
    if (minterval.intervals)
    {
        intervals = new r_Sinterval[dimensionality];
        for (r_Dimension i = 0; i < dimensionality; i++)
        {
            intervals[i] = minterval[i];
        }
    }
}

r_Minterval::~r_Minterval()
{
    r_deactivate();
}

void
r_Minterval::r_deactivate()
{
    if (intervals)
    {
        delete[] intervals;
        intervals = NULL;
    }
}

bool
r_Minterval::intersects_with(const r_Minterval& minterval) const
{
    if (dimensionality != minterval.dimension())
    {
#ifdef DEBUG
        LDEBUG << "cannot check if " << this << " and " << minterval <<
               " intersect, mintervals do not share the same dimension.";
#endif
        return false;
    }

    // none of the interval pairs are allowed to be disjoint
    for (r_Dimension i = 0; i < dimensionality; ++i)
        if (!intervals[i].intersects_with(minterval[i]))
            return false;

    return true;
}

const r_Sinterval &
r_Minterval::at_unsafe(r_Dimension dim) const
{
    return intervals[dim];
}
r_Sinterval &
r_Minterval::at_unsafe(r_Dimension dim)
{
    return intervals[dim];
}

#ifndef OPT_INLINE
const r_Sinterval &
r_Minterval::operator[](r_Dimension i) const
{
    if (i < dimensionality)
    {
        return intervals[i];
    }
    else
    {
        LERROR << "interval index " << i << " out of bounds on minterval " << *this;
        throw r_Eindex_violation(0, dimensionality - 1, i);
    }
}

r_Sinterval&
r_Minterval::operator[](r_Dimension i)
{
    if (i < dimensionality)
    {
        return intervals[i];
    }
    else
    {
        LERROR << "interval index " << i << " out of bounds on minterval " << *this;
        throw r_Eindex_violation(0, dimensionality - 1, i);
    }
}
#endif

const r_Minterval&
r_Minterval::operator=(const r_Minterval& minterval)
{
    if (this != &minterval)
    {
        if (intervals && dimensionality != minterval.dimension())
        {
            delete[] intervals;
            intervals = NULL;
        }

        dimensionality = minterval.dimension();
        streamInitCnt   = minterval.streamInitCnt;

        if (minterval.intervals)
        {
            if (!intervals)
            {
                intervals = new r_Sinterval[ dimensionality ];
            }

            for (r_Dimension i = 0; i < dimensionality; i++)
            {
                intervals[i] = minterval[i];
            }
        }
    }

    return *this;
}

bool
r_Minterval::operator==(const r_Minterval& mint) const
{
    bool returnValue = false;

    if (dimensionality == mint.dimensionality)
    {
        returnValue = true;

        for (r_Dimension i = 0; i < dimensionality && returnValue ; i++)
        {
            if (intervals[i] != mint[i])
            {
                returnValue = false;
                break;
            }
        }
    }

    return returnValue;
}

bool
r_Minterval::equal_extents(const r_Minterval &other) const
{
    if (dimension() == other.dimension())
    {
        for (r_Dimension i = 0; i < dimensionality; i++)
            if (intervals[i].get_extent() != other[i].get_extent())
                return false;
        return true;
    }
    return false;
}

bool
r_Minterval::operator!=(const r_Minterval& mint) const
{
    return !operator==(mint);
}

r_Point
r_Minterval::get_origin() const
{

    if (is_origin_fixed())
    {
        r_Point pt(dimensionality);
        for (r_Dimension i = 0; i < dimensionality; i++)
        {
            pt[i] = intervals[i].low();
        }
        return pt;
    }
    else
    {
        LERROR << "cannot get origin of open minterval " << *this;
        throw r_Error(INTERVALOPEN);
    }
}

r_Point
r_Minterval::get_high() const
{
    if (is_high_fixed())
    {
        r_Point pt(dimensionality);
        for (r_Dimension i = 0; i < dimensionality; i++)
        {
            pt[i] = intervals[i].high();
        }
        return pt;
    }
    else
    {
        LERROR << "cannot get upper bounds of open minterval " << *this;
        throw r_Error(INTERVALOPEN);
    }
}

r_Point
r_Minterval::get_extent() const
{
    if (is_origin_fixed() && is_high_fixed())
    {
        r_Point pt(dimensionality);
        for (r_Dimension i = 0; i < dimensionality; i++)
        {
            pt[i] = intervals[i].get_extent();
        }
        return pt;
    }
    else
    {
        LERROR << "cannot get extent of open minterval " << *this;
        throw r_Error(INTERVALOPEN);
    }
}

r_Minterval&
r_Minterval::reverse_translate(const r_Point& t)
{
    if (dimensionality != t.dimension())
    {
        LERROR << "cannot reverse_translate minterval " << *this 
                << " by a point of mismatching dimension " << t;
        throw (r_Edim_mismatch(dimensionality, t.dimension()));
    }
    if (!is_origin_fixed() || !is_high_fixed())
    {
        LERROR << "cannot reverse_translate open minterval " << *this;
        throw r_Error(INTERVALOPEN);
    }

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        intervals[i].set_interval(intervals[i].low() - t[i], intervals[i].high() - t[i]);
    }

    return *this;
}

r_Minterval&
r_Minterval::translate(const r_Point& t)
{
    if (dimensionality != t.dimension())
    {
        LERROR << "cannot translate minterval " << *this 
                << " by a point of mismatching dimension " << t;
        throw (r_Edim_mismatch(dimensionality, t.dimension()));
    }

    if (!is_origin_fixed() || !is_high_fixed())
    {
        LERROR << "cannot translate open minterval " << *this;
        throw r_Error(INTERVALOPEN);
    }

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        intervals[i].set_interval(intervals[i].low() + t[i], intervals[i].high() + t[i]);
    }

    return *this;
}

r_Minterval
r_Minterval::create_reverse_translation(const r_Point& t) const
{
    r_Minterval result(*this);

    result.reverse_translate(t);

    return result;
}

r_Minterval
r_Minterval::create_translation(const r_Point& t) const
{
    r_Minterval result(*this);

    result.translate(t);

    return result;
}

r_Minterval&
r_Minterval::scale(const double& d)
{
    vector<double> scaleVec;

    //create scale vector
    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        scaleVec.push_back(d);
    }

    scale(scaleVec);

    return *this;
}

r_Minterval&
r_Minterval::scale(const vector<double>& scaleVec)
{
    double high = 0., low = 0.;

    // if the size of scale vector is different from dimensionality, undefined behaviour
    if (scaleVec.size() != dimensionality)
    {
        throw r_Edim_mismatch(scaleVec.size(), dimensionality);
    }

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        // do explicit rounding, because the cast down in set_interval doesn't do the good rounding for negative values -- PB 2005-jun-19
        low  = floor(scaleVec[i] * intervals[i].low());

        //correction by 1e-6 to avoid the strage bug when high was a
        //integer value and floor return value-1(e.g. query 47.ql)
        high = floor(scaleVec[i] * (intervals[i].high() + 1) + 0.000001) - 1;

        // apparently the above correction doesn't work for certain big numbers,
        // e.g. 148290:148290 is scaled to 74145:74144 (invalid) by factor 0.5 -- DM 2012-may-25
        if (high < low)
        {
            high = low;
        }

        intervals[i].set_interval(static_cast<r_Range>(low), static_cast<r_Range>(high));
    }
    return *this;
}

r_Minterval
r_Minterval::create_scale(const double& d) const
{
    r_Minterval result(*this);

    result.scale(d);

    return result;
}


r_Minterval
r_Minterval::create_scale(const vector<double>& scaleVec) const
{
    r_Minterval result(*this);

    result.scale(scaleVec);

    return result;
}

r_Minterval&
r_Minterval::union_of(const r_Minterval& mint1, const r_Minterval& mint2)
{
    if (mint1.dimension() != mint2.dimension())
    {
        LERROR << "cannot create union of mintervals of mismatching dimensions: " << mint1 << " and " << mint2;
        throw (r_Edim_mismatch(mint1.dimension(), mint2.dimension()));
    }

    // cleanup + initializing of this
    if (dimensionality != mint1.dimension())
    {
        if (intervals)
        {
            delete[] intervals;
        }
        dimensionality = mint1.dimension();
        streamInitCnt   = dimensionality;
        intervals = new r_Sinterval[ dimensionality ];
    }
    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        intervals[i].union_of(mint1[i], mint2[i]);
    }

    return *this;
}

r_Minterval&
r_Minterval::union_with(const r_Minterval& mint)
{
    if (dimensionality != mint.dimension())
    {
        LERROR << "cannot create union of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimensionality, mint.dimension()));
    }

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        intervals[i].union_with(mint[i]);
    }

    return *this;
}

r_Minterval&
r_Minterval::operator+=(const r_Minterval& mint)
{
    return union_with(mint);
}

r_Minterval
r_Minterval::create_union(const r_Minterval& mint) const
{
    if (dimensionality != mint.dimension())
    {
        LERROR << "cannot create union of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimensionality, mint.dimension()));
    }

    r_Minterval result(dimensionality);

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        result << intervals[i].create_union(mint[i]);
    }

    return result;
}

r_Minterval
r_Minterval::operator+(const r_Minterval& mint) const
{
    return create_union(mint);
}

r_Minterval&
r_Minterval::difference_of(const r_Minterval& mint1, const r_Minterval& mint2)
{
    if (mint1.dimension() != mint2.dimension())
    {
        LERROR << "cannot create difference of mintervals of mismatching dimensions: " << mint1 << " and " << mint2;
        throw (r_Edim_mismatch(mint1.dimension(), mint2.dimension()));
    }

    if (dimensionality != mint1.dimension())
    {
        // cleanup + initializing of this
        if (intervals)
        {
            delete[] intervals;
        }

        dimensionality = mint1.dimension();
        streamInitCnt = dimensionality;
        intervals = new r_Sinterval[ dimensionality ];
    }

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        intervals[i].difference_of(mint1[i], mint2[i]);
    }

    return *this;
}

r_Minterval&
r_Minterval::difference_with(const r_Minterval& mint)
{
    if (dimensionality != mint.dimension())
    {
        LERROR << "cannot create difference of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimensionality, mint.dimension()));
    }

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        intervals[i].difference_with(mint[i]);
    }

    return *this;
}

r_Minterval&
r_Minterval::operator-=(const r_Minterval& mint)
{
    return difference_with(mint);
}

r_Minterval
r_Minterval::create_difference(const r_Minterval& mint) const
{
    if (dimensionality != mint.dimension())
    {
        LERROR << "cannot create difference of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimensionality, mint.dimension()));
    }

    r_Minterval result(dimensionality);

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        result << intervals[i].create_difference(mint[i]);
    }

    return result;
}

r_Minterval
r_Minterval::operator-(const r_Minterval& mint) const
{
    return create_difference(mint);
}

r_Minterval&
r_Minterval::intersection_of(const r_Minterval& mint1, const r_Minterval& mint2)
{
    if (mint1.dimension() != mint2.dimension())
    {
        LERROR << "cannot create intersection of mintervals of mismatching dimensions: " << mint1 << " and " << mint2;
        throw (r_Edim_mismatch(mint1.dimension(), mint2.dimension()));
    }
    if (dimensionality != mint1.dimension())
    {
        // cleanup + initializing of this
        if (intervals)
        {
            delete[] intervals;
        }

        dimensionality = mint1.dimension();
        streamInitCnt = dimensionality;
        intervals = new r_Sinterval[ dimensionality ];
    }

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        intervals[i].intersection_of(mint1[i], mint2[i]);
    }
    return *this;
}

r_Minterval&
r_Minterval::intersection_with(const r_Minterval& mint)
{
    if (dimensionality != mint.dimension())
    {
        LERROR << "cannot create intersection of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimensionality, mint.dimension()));
    }

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        intervals[i].intersection_with(mint[i]);
    }

    return *this;
}

r_Minterval&
r_Minterval::operator*=(const r_Minterval& mint)
{
    return intersection_with(mint);
}

r_Minterval
r_Minterval::create_intersection(const r_Minterval& mint) const
{
    if (dimensionality != mint.dimension())
    {
        LERROR << "cannot create intersection of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimensionality, mint.dimension()));
    }

    r_Minterval result(dimensionality);

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        result << intervals[i].create_intersection(mint[i]);
    }

    return result;
}

r_Minterval
r_Minterval::operator*(const r_Minterval& mint) const
{
    return create_intersection(mint);
}

r_Minterval&
r_Minterval::closure_of(const r_Minterval& mint1, const r_Minterval& mint2)
{
    if (mint1.dimension() != mint2.dimension())
    {
        LERROR << "cannot create closure of mintervals of mismatching dimensions: " << mint1 << " and " << mint2;
        throw (r_Edim_mismatch(mint1.dimension(), mint2.dimension()));
    }
    if (mint1.dimension() != dimensionality)
    {
        // cleanup + initializing of this
        if (intervals)
        {
            delete[] intervals;
        }

        dimensionality = mint1.dimension();
        streamInitCnt = dimensionality;
        intervals = new r_Sinterval[ dimensionality ];
    }

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        intervals[i].closure_of(mint1[i], mint2[i]);
    }

    return *this;
}

r_Minterval&
r_Minterval::closure_with(const r_Minterval& mint)
{
    if (dimensionality != mint.dimension())
    {
        LERROR << "cannot create closure of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimensionality, mint.dimension()));
    }

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        intervals[i].closure_with(mint[i]);
    }

    return *this;
}

r_Minterval
r_Minterval::create_closure(const r_Minterval& mint) const
{
    if (dimensionality != mint.dimension())
    {
        LERROR << "cannot create closure of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimensionality, mint.dimension()));
    }

    r_Minterval result(dimensionality);

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        result << intervals[i].create_closure(mint[i]);
    }

    return result;
}

r_Minterval
r_Minterval::trim_along_slice(const r_Minterval& mint, const std::vector<r_Dimension>& projDims) const
{
    if(dimensionality < mint.dimension())
    {
        LERROR << "r_Minterval:trim_along_slice(" << mint << ") dimensions (" << dimensionality << ") do not coincide";
        throw (r_Edim_mismatch(dimensionality, mint.dimension()));
    }
    else if(projDims.size() >= dimensionality)
    {
        LERROR << "r_Minterval:trim_along_slice(" << projDims.size() << ") dimensions (" << dimensionality << ") do not coincide";
        throw (r_Edim_mismatch(dimensionality, mint.dimension()));        
    }
    
    for(size_t i = 0; i < projDims.size(); i++)
    {
        if(projDims[i] >= dimensionality)
        {
            LERROR << "r_Minterval:trim_along_slice(" << projDims[i] << ") dimensions (" << dimensionality << ") do not coincide";
            throw (r_Edim_mismatch(dimensionality, projDims[i]));            
        }
    }
    
    r_Minterval result(dimensionality);
    
    size_t projCtr = 0;
    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        if(projDims[projCtr] == i && projCtr < projDims.size())
        {
            // trimming along the slice
            result << mint[projCtr].create_intersection(intervals[i]);
            projCtr++;
        }
        else
        {
            // no trimming happens -- dimension is kept
            result << intervals[i];
        }
    }
    
    return result;
}

r_Minterval
r_Minterval::project_along_dims(const std::vector<r_Dimension>& projDims) const
{
    for(size_t i = 0; i < projDims.size(); i++)
    {
        if(projDims[i] >= dimensionality)
        {
            LERROR << "r_Minterval:project_along_dims(" << projDims[i] << ") dimensions (" << dimensionality << ") do not coincide";
            throw (r_Edim_mismatch(dimensionality, projDims[i]));            
        }
    }    
    
    r_Minterval result(projDims.size());
    
    for (auto projDimIter = projDims.begin(); projDimIter != projDims.end(); projDimIter++)
    {
        result << intervals[*projDimIter];
    }
    
    return result;
}

void
r_Minterval::print_status(std::ostream& s) const
{
    s << "[";

    if (dimensionality > 0)
    {
        for (r_Dimension i = 0; i < dimensionality - 1; i++)
        {
            s << intervals[i] << ",";
        }

        s << intervals[dimensionality - 1];
    }

    s << "]";
}

char*
r_Minterval::get_string_representation() const
{
    // initialize string stream
    std::ostringstream domainStream;
    // write into string stream
    domainStream << (*this);

    // allocate memory taking the final string
    char* returnString = strdup(domainStream.str().c_str());

    return returnString;
}

std::string 
r_Minterval::to_string() const
{
    char* stringRep = this->get_string_representation();
    std::string returnValue(stringRep);
    std::free(stringRep);
    return returnValue;
}

std::string
r_Minterval::get_named_axis_string_representation() const
{
    std::ostringstream ss;
    ss << "[";

    bool isFirst = true;

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        if (!isFirst)
        {
            ss << ",";
        }

        ss << "a" << i;

        if (intervals[i].is_low_fixed() || intervals[i].is_high_fixed())
        {
            ss << "(" << intervals[i] << ")";
        }

        isFirst = false;
    }

    ss << "]";

    return ss.str();
}

r_Area
r_Minterval::cell_count() const
{
    r_Area cellCount = 1;
    if(dimensionality != 0) 
    {
        r_Point ptExt = get_extent();
        
        for (r_Dimension i = 0; i < dimensionality; i++) 
        {
            cellCount *= static_cast<r_Area> (ptExt[i]);
        }
    }

    return cellCount;
}

// offset in cells for linear access of the data element referred by point in the data memory area
// Lower dimensions are higher valued which means that the highest dimension is stored in a sequence.
r_Area
r_Minterval::cell_offset(const r_Point& point) const
{
    r_Dimension i = 0;
    r_Area offset = 0;
    r_Point ptExt;

    if (dimensionality != point.dimension())
    {
        LERROR << "cannot calculate cell offset, dimension of minterval (" 
                << *this << ") does not match dimension of point " << point.dimension() << ".";
        throw r_Edim_mismatch(point.dimension(), dimensionality);
    }

    ptExt = get_extent();

    // calculate offset
    for (i = 0; i < dimensionality - 1; i++)
    {
        if (point[i] < intervals[i].low() || point[i] > intervals[i].high())
        {
            LERROR << "point " << point << " is out of range for minterval " << *this << ".";
            throw (r_Eindex_violation(point[i], intervals[i].low(), intervals[i].high()));
        }

        offset = (offset + static_cast<long long unsigned int>(point[i] - intervals[i].low())) * static_cast<long long unsigned int>(ptExt[i + 1]);
    }

    // now i = dimensionality - 1
    if (point[i] < intervals[i].low() || point[i] > intervals[i].high())
    {
        LERROR << "point " << point << " is out of range for minterval " << *this << ".";
        throw (r_Eindex_violation(point[i], intervals[i].low(), intervals[i].high()));
    }
    offset += static_cast<long long unsigned int>(point[i] - intervals[i].low());

    return offset;
}

r_Area
r_Minterval::efficient_cell_offset(const r_Point& point) const
{
    r_Area offset = 0;
    r_Point ptExt = get_extent();

    r_Dimension i = 0;
    // calculate offset
    for (; i < dimensionality - 1; i++)
    {
        offset = (offset + static_cast<long long unsigned int>(point[i] - intervals[i].low())) * 
                static_cast<long long unsigned int>(ptExt[i + 1]);
    }

    offset += static_cast<long long unsigned int>(point[i] - intervals[i].low());

    return offset;
}

// Arguments.....: linear offset
// Return value..: point object which corresponds to the linear offset of the argument
// Description...: The method calucaltes the spatial domain coordinates as a point out of an offset specification. Lower dimensions are higher valued which means that the highest dimension is stored in a sequence.
r_Point
r_Minterval::cell_point(r_Area offset) const
{
    r_Dimension i;
    unsigned int factor = 1;
    r_Point pt(dimensionality), ptExt;

    if (offset >= cell_count())
    {
        LERROR << "cannot get point, offset " << offset << " offset is out of range on domain with " << cell_count() << " cells.";
        throw r_Eno_cell();
    }

    ptExt = get_extent();

    for (i = 0; i < dimensionality; i++)
    {
        factor *= ptExt[i];
    }

    for (i = 0; i < dimensionality; i++)
    {
        factor /= ptExt[i];
        pt[i]   = intervals[i].low() + static_cast<r_Range>((offset - (offset % factor)) / factor);
        offset %= factor;
    }

    return pt;
}

void
r_Minterval::delete_dimension(r_Dimension dim)
{
    if (dim >= dimensionality)
    {
        LERROR << "cannot delete dimension " << dim << " from minterval " << *this << ", out of range.";
        throw r_Eindex_violation(0, dimensionality - 1, dim);
    }

    dimensionality -= 1;
    streamInitCnt = dimensionality;
    r_Sinterval* newIntervals = new r_Sinterval[ dimensionality ];

    for (r_Dimension i = 0, j = 0; i < dimensionality; i++, j++)
    {
        if (i == dim)
        {
            j++;
        }
        newIntervals[i] = intervals[j];
    }

    delete[] intervals;

    intervals = newIntervals;
}

void
r_Minterval::delete_non_trims(const std::vector<bool> &trims)
{
    if (trims.size() != dimension())
        return;
    for (r_Dimension i = 0, j = 0; i < dimensionality; ++i)
    {
        if (!trims[i])
            this->delete_dimension(j);
        else
            ++j;
    }
}

void r_Minterval::transpose(r_Dimension a, r_Dimension b)
{
    if (a >= dimensionality)
    {
        LERROR << "cannot transpose intervals " << a << " and " << b << " in minterval " << *this << ", out of range.";
        throw r_Eindex_violation(0, dimensionality - 1, a);
    }
    if (b >= dimensionality)
    {
        LERROR << "cannot transpose intervals " << a << " and " << b << " in minterval " << *this << ", out of range.";
        throw r_Eindex_violation(0, dimensionality - 1, b);
    }
    if (a != b)
    {
        r_Sinterval tmp = intervals[a];
        intervals[a] = intervals[b];
        intervals[b] = tmp;
    }
}

r_Bytes
r_Minterval::get_storage_size() const
{
    r_Bytes sz = sizeof(r_Sinterval*) + 2 * sizeof(r_Dimension);

    if (dimensionality > 0)
    {
        sz += dimensionality * intervals->get_storage_size();
    }

    return sz;
}

bool
r_Minterval::is_mergeable(const r_Minterval& b) const
{
    bool is_merg = true;
    // An alias to this object
    const r_Minterval& a = *this;

    // The blocks must have the same dimensionality to be mergeable
    if (a.dimensionality != b.dimensionality)
    {
        is_merg = false;
    }
    else
    {

        // Count the number of adjacent frontiers
        int ones_differences = 0;

        // For all dimensions
        for (r_Dimension i = 0; i < dimensionality; i++)
        {
            // Diferente origins
            if (a[i].low() != b[i].low())
            {
                if ((a[i].low() == b[i].high() + 1) || (b[i].low() == a[i].high() + 1))
                    // If borders are adjacent
                {
                    // Update counter
                    ++ones_differences;
                }
                else
                {
                    // Else non-mergeable blocks
                    is_merg = false;
                    break;
                }
            }
            else
            {
                // Check ending
                if (a[i].high() != b[i].high())
                {
                    is_merg = false;
                    // Not the same, can't be
                    break;
                    // mergeable
                }
            }
        }

        // Only one adjacent borded
        if (is_merg && (ones_differences != 1))
        {
            is_merg = false;
        }
        // allowed
    }
    return is_merg;
}

std::ostream& operator<<(std::ostream& s, const r_Minterval& d)
{
    d.print_status(s);
    return s;
}

std::ostream& operator<<(std::ostream& os, const std::vector<r_Minterval>& vec)
{
    os << " Vector { ";

    unsigned int size = vec.size();
    for (unsigned int i = 0; i < size; i++)
    {
        os << vec[i] << std::endl;
    }

    os << " } ";
    return os;
}

std::ostream& operator<<(std::ostream& s, const vector<double>& doubleVec)
{
    vector<double>::const_iterator iter, iterEnd;

    iter = doubleVec.begin();
    iterEnd = doubleVec.end();
    s << "{";
    while (iter != iterEnd)
    {
        s << *iter;
        ++iter;
        if (iter != iterEnd)
        {
            s << ", ";
        }
    }
    s << "}";

    return s;
}
