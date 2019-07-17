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

#include "atomictype.hh"  // for AtomicType
#include "ushorttype.hh"

#include <limits.h>       // for USHRT_MAX
#include <iomanip>        // for operator<<, setw

UShortType::UShortType() : UIntegralType(UShortType::Name, sizeof(r_UShort))
{
    myType = USHORT;
    myOId = OId(USHORT, OId::ATOMICTYPEOID);
}

UShortType::UShortType(const OId &id) : UIntegralType(id)
{
    size = sizeof(r_UShort);
    setName(UShortType::Name);
    myType = USHORT;
    myOId = OId(USHORT, OId::ATOMICTYPEOID);
}

void UShortType::printCell(std::ostream &stream, const char *cell) const
{
    stream << std::setw(5) << *reinterpret_cast<const r_UShort *>(cell);
}

r_ULong *UShortType::convertToCULong(const char *cell, r_ULong *value) const
{
    *value = *reinterpret_cast<const r_UShort *>(cell);
    return value;
}

char *UShortType::makeFromCULong(char *cell, const r_ULong *value) const
{
    *reinterpret_cast<r_UShort *>(cell) = 
        (*value > USHRT_MAX ? USHRT_MAX : static_cast<r_UShort>(*value));
    return cell;
}
