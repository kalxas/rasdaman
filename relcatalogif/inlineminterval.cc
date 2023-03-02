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
// This is -*- C++ -*-
/*
 * dbminterval.pc
 *
 * 17-May-99    hoefner     First created.
 * 27-May-99    webbasan    Added prefix "RAS_" to tablenames.
 */

#include "inlineminterval.hh"
#include "raslib/sinterval.hh"  // for r_Sinterval

InlineMinterval::InlineMinterval()
    : r_Minterval(0u)
{
}

InlineMinterval::InlineMinterval(r_Dimension dim)
    : r_Minterval(dim)
{
}

InlineMinterval::InlineMinterval(r_Dimension dim,
                                 const r_Range *lowerbound, const r_Range *upperbound,
                                 const char *lowerfixed, const char *upperfixed)
    : r_Minterval(dim)
{
    static const char unbounded = '*';
    streamInitCnt = dim;

    for (r_Dimension count = 0; count < dimension(); count++)
    {
        if (!lowerfixed[count])
        {
            intervals[count].set_high(unbounded);
            intervals[count].set_low(lowerbound[count]);
        }
        else
        {
            intervals[count].set_low(unbounded);
        }
        if (!upperfixed[count])
        {
            intervals[count].set_high(upperbound[count]);
        }
        else
        {
            intervals[count].set_high(unbounded);
        }
    }
}

InlineMinterval::InlineMinterval(const InlineMinterval &old)
    : r_Minterval(old)
{
}

InlineMinterval::InlineMinterval(const r_Minterval &old)
    : r_Minterval(old)
{
}

InlineMinterval &InlineMinterval::operator=(const InlineMinterval &old)
{
    if (this == &old)
    {
        return *this;
    }
    r_Minterval::operator=(old);
    return *this;
}

InlineMinterval &InlineMinterval::operator=(const r_Minterval &old)
{
    if (this == &old)
    {
        return *this;
    }
    r_Minterval::operator=(old);
    return *this;
}

InlineMinterval::~InlineMinterval()
{
}

void InlineMinterval::insertInDb(r_Range *lowerbound, r_Range *upperbound,
                                 char *lowerfixed, char *upperfixed) const
{
    static const char unbounded = '*';
    for (r_Dimension count = 0; count < dimension(); count++)
    {
        if (intervals[count].is_low_fixed())
        {
            lowerbound[count] = intervals[count].low();
            lowerfixed[count] = 0;
        }
        else
        {
            lowerfixed[count] = unbounded;
        }
        if (intervals[count].is_high_fixed())
        {
            upperbound[count] = intervals[count].high();
            upperfixed[count] = 0;
        }
        else
        {
            upperfixed[count] = unbounded;
        }
    }
}
