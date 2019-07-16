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

#include "doubletype.hh"
#include "raslib/odmgtypes.hh"   // for DOUBLE
#include "reladminif/oidif.hh"   // for OId
#include <iomanip>
#include <limits>

DoubleType::DoubleType(const OId &id) : RealType(id)
{
    size = sizeof(r_Double);
    setName(DoubleType::Name);
    myType = DOUBLE;
    myOId = OId(DOUBLE, OId::ATOMICTYPEOID);
}

DoubleType::DoubleType() : RealType(DoubleType::Name, sizeof(r_Double))
{
    myType = DOUBLE;
    myOId = OId(DOUBLE, OId::ATOMICTYPEOID);
}

void DoubleType::printCell(std::ostream &stream, const char *cell) const
{
    stream << std::setprecision(std::numeric_limits<r_Double>::digits10 + 1)
           << *reinterpret_cast<const r_Double *>(cell);
}

double *DoubleType::convertToCDouble(const char *cell, r_Double *value) const
{
    *value = *reinterpret_cast<const r_Double *>(cell);
    return value;
}

char *DoubleType::makeFromCDouble(char *cell, const r_Double *value) const
{
    *reinterpret_cast<r_Double *>(cell) = *value;
    return cell;
}
