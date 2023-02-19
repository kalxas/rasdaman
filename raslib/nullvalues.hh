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

#ifndef _D_NULLVALUES_
#define _D_NULLVALUES_

#include "raslib/odmgtypes.hh"

#include <vector>
#include <string>
#include <cmath>

/**
  * \ingroup raslib
  */
class r_Nullvalues
{
public:
    
    /// the * in *:hi, it is the smallest double number
    static const double_t unlimitedLow;
    /// the * in lo:*, it is the maximum double number
    static const double_t unlimitedHigh;
    
    r_Nullvalues() = default;
    explicit r_Nullvalues(std::vector<std::pair<r_Double, r_Double>> &&nullvaluesArg);

    const std::vector<std::pair<r_Double, r_Double>> &getNullvalues() const
    {
        return nullvalues;
    }

    /// check whether a value is in an interval (hence a null value)
    template <typename T>
    inline bool isNullNonFloat(const T value) const
    {
        for (const auto &p : nullvalues)
        {
            if (value >= (p.first - DBL_EPSILON) && value <= (p.second + DBL_EPSILON))
            {
                return true;
            }
        }
        return false;
    }

    /// check whether a value is in an interval (hence a null value)
    /// TODO: this needs to be improved, performance and scope
    template <typename T>
    inline bool isNullFloat(const T value) const
    {
        for (const auto &p : nullvalues)
        {
            if ((value >= (p.first - DBL_EPSILON) && value <= (p.second + DBL_EPSILON)) ||
                (std::isnan(value) && std::isnan(p.first)))
            {
                return true;
            }
        }
        return false;
    }
    
    /// if the null set contains any non-interval null values, then the first such 
    /// null value is returned, otherwise the first interval bound which is not
    /// '*' is returned; useful in cases where a single nodata value is needed,
    /// e.g. when encoding the data
    double getFirstNullValue() const;

    std::string toString() const;

protected:
    std::vector<std::pair<r_Double, r_Double>> nullvalues;
};


#endif
