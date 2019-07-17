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

#include "longtype.hh"
#include "reladminif/oidif.hh"    // for OId

#include <iomanip>        // for operator<<, setw

LongType::LongType(const OId &id) : IntegralType(id)
{
    setName(LongType::Name);
    size = sizeof(r_Long);
    myType = LONG;
    myOId = OId(LONG, OId::ATOMICTYPEOID);
}

LongType::LongType() : IntegralType(LongType::Name, sizeof(r_Long))
{
    myType = LONG;
    myOId = OId(LONG, OId::ATOMICTYPEOID);
}

void LongType::printCell(std::ostream &stream, const char *cell) const
{
    stream << std::setw(8)
           << *reinterpret_cast<const r_Long *>(cell);
}

r_Long *LongType::convertToCLong(const char *cell, r_Long *value) const
{
    *value = *reinterpret_cast<const r_Long *>(cell);
    return value;
}

char *LongType::makeFromCLong(char *cell, const r_Long *value) const
{
    *reinterpret_cast<r_Long *>(cell) = *value;
    return cell;
}
