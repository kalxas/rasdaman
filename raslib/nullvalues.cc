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

#include "raslib/nullvalues.hh"
#include <limits>
#include <cassert>

const double_t r_Nullvalues::unlimitedLow = std::numeric_limits<double>::lowest();
const double_t r_Nullvalues::unlimitedHigh = std::numeric_limits<double>::max();

r_Nullvalues::r_Nullvalues(std::vector<std::pair<r_Double, r_Double>> &&nullvaluesArg)
    : nullvalues{nullvaluesArg}
{
}

double r_Nullvalues::getFirstNullValue() const
{
    assert(!nullvalues.empty());
    // check first the non-interval null values
    for (const auto &p: nullvalues)
    {
        if (p.first == p.second)
            return p.first;
    }
    // check interval null values
    for (const auto &p: nullvalues)
    {
        if (p.first != unlimitedLow)
            return p.first;
        if (p.second != unlimitedHigh)
            return p.second;
    }
    // nothing found, should never happen
    assert(false && "there must be at least one non-unlimited null value");
    return 0.0;
}

std::string r_Nullvalues::toString() const
{
    std::string ret = "[";
    for (const auto &p: nullvalues)
    {
        if (ret.size() > 1)  // ret == "[" at the start
            ret += ",";

        // std::to_string seems to fail for nan on Ubuntu 18.04 (see #2156)
        // so that's why this is explicitly handled here.
        ret += !std::isnan(p.first) ? std::to_string(p.first) : "nan";
        if (p.first != p.second)
        {
            ret += ":";
            ret += !std::isnan(p.second) ? std::to_string(p.second) : "nan";
        }
    }
    ret += "]";
    return ret;
}
