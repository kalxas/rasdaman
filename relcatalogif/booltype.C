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

#include "booltype.hh"
#include "reladminif/oidif.hh"    // for OId

BoolType::BoolType(const OId &id) : UIntegralType(id)
{
    setName(BoolType::Name);
    size = sizeof(r_Boolean);
    myType = BOOLTYPE;
    myOId = OId(BOOLTYPE, OId::ATOMICTYPEOID);
}

BoolType::BoolType() : UIntegralType(BoolType::Name, sizeof(r_Boolean))
{
    myType = BOOLTYPE;
    myOId = OId(BOOLTYPE, OId::ATOMICTYPEOID);
}

void BoolType::printCell(std::ostream &stream, const char *cell) const
{
    stream << ((*cell == 0) ? "FALSE " : "TRUE  ");
}

r_ULong *BoolType::convertToCULong(const char *cell, r_ULong *value) const
{
    *value = *reinterpret_cast<const unsigned char *>(cell) ? 1 : 0;
    return value;
}

char *BoolType::makeFromCULong(char *cell, const r_ULong *value) const
{
    *reinterpret_cast<unsigned char *>(cell) = static_cast<unsigned char>(*value ? 1 : 0);
    return cell;
}
