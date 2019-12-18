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

#include "rasodmg/dirdecompose.hh"

r_Dir_Decompose &r_Dir_Decompose::operator<<(r_Range limit)
{
    intervals.push_back(limit);
    return *this;
}
r_Dir_Decompose &r_Dir_Decompose::prepend(r_Range limit)
{
    intervals.insert(intervals.begin(), limit);
    return *this;
}
size_t r_Dir_Decompose::get_num_intervals() const
{
    return intervals.size();
}
r_Range r_Dir_Decompose::get_partition(size_t number) const
{
    if (number >= intervals.size())
        throw r_Eindex_violation(0ll, static_cast<r_Range>(intervals.size()),
                                 static_cast<r_Range>(number));
    return intervals[number];
}
r_Sinterval
r_Dir_Decompose::get_total_interval()
{
    return r_Sinterval(intervals.front(), intervals.back());
}


void r_Dir_Decompose::print_status(std::ostream &os) const
{
    os << "r_Dir_Decompose[ num intervals = " << intervals.size() << " intervals = {";
    for (auto i: intervals)
        os << i << " ";
    os << "} ]";
}
std::ostream &operator<<(std::ostream &os, const r_Dir_Decompose &d)
{
    d.print_status(os);
    return os;
}
