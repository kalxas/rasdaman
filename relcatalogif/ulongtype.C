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

#include "ulongtype.hh"

#include <iomanip>        // for operator<<, setw

ULongType::ULongType(const OId &id) : UIntegralType(id)
{
    setName(ULongType::Name);
    size = sizeof(r_ULong);
    myOId = OId(ULONG, OId::ATOMICTYPEOID);
    myType = ULONG;
}

ULongType::ULongType() : UIntegralType(ULongType::Name, sizeof(r_ULong))
{
    myOId = OId(ULONG, OId::ATOMICTYPEOID);
    myType = ULONG;
}

void ULongType::printCell(std::ostream &stream, const char *cell) const
{
    stream << std::setw(8) << *reinterpret_cast<const r_ULong *>(cell);
}

r_ULong *ULongType::convertToCULong(const char *cell, r_ULong *value) const
{
    *value = *reinterpret_cast<const r_ULong *>(cell);
    return value;
}

r_Long *ULongType::convertToCLong(const char *cell, r_Long *value) const
{
    auto tmp = *reinterpret_cast<const r_ULong *>(cell);
    *value = tmp > INT_MAX ? INT_MAX : static_cast<r_Long>(tmp);
    return value;
}

char *ULongType::makeFromCULong(char *cell, const r_ULong *value) const
{
    *reinterpret_cast<r_ULong *>(cell) = *value;
    return cell;
}

char *ULongType::makeFromCLong(char *cell, const r_Long *value) const
{
    *reinterpret_cast<r_ULong *>(cell) = *value < 0 ? 0u : static_cast<r_ULong>(*value);
    return cell;
}
