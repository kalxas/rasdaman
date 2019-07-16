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
/*************************************************************
 *
 *
 * PURPOSE:
 *   uses ODMG-conformant O2 classes
 *
 *
 * COMMENTS:
 *   none
 *
 ************************************************************/

#include "chartype.hh"
#include "reladminif/oidif.hh"    // for OId

#include <limits.h>       // for UCHAR_MAX
#include <iomanip>        // for operator<<, setw

CharType::CharType() : UIntegralType(CharType::Name, sizeof(r_Char))
{
    myType = CHAR;
    myOId = OId(CHAR, OId::ATOMICTYPEOID);
}

CharType::CharType(const CharType &old)
    : UIntegralType(CharType::Name, old.size)
{
}

CharType::CharType(__attribute__((unused)) const OId &id)
    : UIntegralType(OId(CHAR, OId::ATOMICTYPEOID))
{
    setName(CharType::Name);
    myType = CHAR;
    size = sizeof(r_Char);
}

void CharType::printCell(std::ostream &stream, const char *cell) const
{
    stream << std::setw(4) << static_cast<r_ULong>(*reinterpret_cast<const r_Char *>(cell));
}

r_ULong *CharType::convertToCULong(const char *cell, r_ULong *value) const
{
    *value = static_cast<r_ULong>(*reinterpret_cast<const r_Char *>(cell));
    return value;
}

char *CharType::makeFromCULong(char *cell, const r_ULong *value) const
{
    *reinterpret_cast<r_Char *>(cell) =
        static_cast<r_Char>(*value > UCHAR_MAX ? UCHAR_MAX : *value);
    return cell;
}
