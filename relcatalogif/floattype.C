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

#include "floattype.hh"
#include "raslib/odmgtypes.hh"   // for FLOAT
#include "reladminif/oidif.hh"    // for OId
#include <float.h>        // for FLT_MAX
#include <iomanip>        // for operator<<, setw
#include <limits>

FloatType::FloatType(const OId &id) : RealType(id)
{
    readFromDb();
}

FloatType::FloatType() : RealType(FloatType::Name, 4)
{
    myType = FLOAT;
    myOId = OId(FLOAT, OId::ATOMICTYPEOID);
}

FloatType::FloatType(const FloatType &old)  = default;

FloatType &FloatType::operator=(const FloatType &old)
{
    if (this == &old)
    {
        return *this;
    }
    AtomicType::operator=(old);
    return *this;
}

FloatType::~FloatType() = default;

void FloatType::readFromDb()
{
    setName(FloatType::Name);
    size = 4;
    myType = FLOAT;
    myOId = OId(FLOAT, OId::ATOMICTYPEOID);
}

void FloatType::printCell(std::ostream &stream, const char *cell) const
{
    stream << std::setprecision(std::numeric_limits<float>::digits10 + 1) 
           << *reinterpret_cast<const float *>(cell);
}

double *FloatType::convertToCDouble(const char *cell, double *value) const
{
    *value = static_cast<double>(*reinterpret_cast<const float *>(cell));
    return value;
}

char *FloatType::makeFromCDouble(char *cell, const double *value) const
{
    *reinterpret_cast<float *>(cell) = static_cast<float>(*value);
    return cell;
}

