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
#include "octettype.hh"
#include "reladminif/oidif.hh"        // for OId

#include <limits.h>       // for SCHAR_MAX, SCHAR_MIN
#include <iomanip>        // for operator<<, setw

OctetType::OctetType(const OId &id) : IntegralType(id)
{
    setName(OctetType::Name);
    size = sizeof(r_Octet);
    myType = OCTET;
    myOId = OId(OCTET, OId::ATOMICTYPEOID);
}

OctetType::OctetType() : IntegralType(OctetType::Name, 1)
{
    myType = OCTET;
    myOId = OId(OCTET, OId::ATOMICTYPEOID);
}

void OctetType::printCell(std::ostream &stream, const char *cell) const
{
    stream << std::setw(4) 
           << static_cast<r_Long>(*reinterpret_cast<const r_Octet *>(cell));
}

r_Long *OctetType::convertToCLong(const char *cell, r_Long *value) const
{
    *value = static_cast<r_Long>(*reinterpret_cast<const r_Octet *>(cell));
    return value;
}

r_ULong *OctetType::convertToCULong(const char *cell, r_ULong *value) const
{
    const auto tmp = *reinterpret_cast<const r_Octet *>(cell);
    *value = tmp < 0 ? 0 : static_cast<r_ULong>(tmp);
    return value;
}

char *OctetType::makeFromCLong(char *cell, const r_Long *value) const
{
    // restricting long to value range of octet
    r_Octet myvalue;
    if (*value > SCHAR_MAX)
        myvalue = SCHAR_MAX;
    else if (*value < SCHAR_MIN)
        myvalue = SCHAR_MIN;
    else
        myvalue = static_cast<r_Octet>(*value);

    *reinterpret_cast<r_Octet *>(cell) = myvalue;
    return cell;
}

char *OctetType::makeFromCULong(char *cell, const r_ULong *value) const
{
    *reinterpret_cast<r_Octet *>(cell) =
        *value > SCHAR_MAX ? SCHAR_MAX : static_cast<r_Octet>(*value);
    return cell;
}
