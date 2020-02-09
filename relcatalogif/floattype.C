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
    setName(FloatType::Name);
    size = sizeof(r_Float);
    myType = FLOAT;
    myOId = OId(FLOAT, OId::ATOMICTYPEOID);
}

FloatType::FloatType() : RealType(FloatType::Name, sizeof(r_Float))
{
    myType = FLOAT;
    myOId = OId(FLOAT, OId::ATOMICTYPEOID);
}


void FloatType::printCell(std::ostream &stream, const char *cell) const
{
    stream << std::setprecision(std::numeric_limits<r_Float>::digits10 + 1) 
           << *reinterpret_cast<const r_Float *>(cell);
}

double *FloatType::convertToCDouble(const char *cell, r_Double *value) const
{
    *value = static_cast<r_Double>(*reinterpret_cast<const r_Float *>(cell));
    return value;
}

char *FloatType::makeFromCDouble(char *cell, const r_Double *value) const
{
  *reinterpret_cast<r_Float *>(cell) = static_cast<r_Float>(*value);
  return cell;
}

r_ULong *FloatType::convertToCULong(const char *cell, r_ULong *value) const
{
  auto tmp = *reinterpret_cast<const r_Float *>(cell);
  *value = tmp > UINT_MAX ? UINT_MAX : static_cast<r_ULong>(tmp);
  return value;
}

r_Long *FloatType::convertToCLong(const char *cell, r_Long *value) const
{
  auto tmp = *reinterpret_cast<const r_Float *>(cell);
  *value = tmp > INT_MAX ? INT_MAX : 
          (tmp < INT_MIN ? INT_MIN : static_cast<r_Long>(tmp));
  return value;
}

