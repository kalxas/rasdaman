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
#include "reladminif/oidif.hh"  // for OId, OId::ATOMICTYPEOID
#include "shorttype.hh"
#include "reladminif/oidif.hh"  // for OId

#include <limits.h>       // for SHRT_MAX, SHRT_MIN
#include <iomanip>        // for operator<<, setw

ShortType::ShortType(const OId &id) : IntegralType(id)
{
    setName(ShortType::Name);
    myType = SHORT;
    size = sizeof(r_Short);
    myOId = OId(SHORT, OId::ATOMICTYPEOID);
}

ShortType::ShortType() : IntegralType(ShortType::Name, sizeof(r_Short))
{
    myType = SHORT;
    myOId = OId(SHORT, OId::ATOMICTYPEOID);
}

void ShortType::printCell(std::ostream &stream, const char *cell) const
{
    stream << std::setw(5) << *reinterpret_cast<const r_Short *>(cell);
}

r_Long *ShortType::convertToCLong(const char *cell, r_Long *value) const
{
    *value = *reinterpret_cast<const r_Short *>(cell);
    return value;
}

r_ULong *ShortType::convertToCULong(const char *cell, r_ULong *value) const
{
    const auto tmp = *reinterpret_cast<const r_Short*>(cell);
    *value = tmp < 0 ? 0 : static_cast<r_ULong>(tmp);
    return value;
}

char *ShortType::makeFromCLong(char *cell, const r_Long *value) const
{
    r_Short myvalue;
    if (*value > SHRT_MAX)
        myvalue = SHRT_MAX;
    else if (*value < SHRT_MIN)
        myvalue = SHRT_MIN;
    else
        myvalue = static_cast<r_Short>(*value);

    *reinterpret_cast<r_Short *>(cell) = myvalue;
    return cell;
}

char *ShortType::makeFromCULong(char *cell, const r_ULong *value) const
{
    *reinterpret_cast<r_Short *>(cell) =
        *value > SHRT_MAX ? SHRT_MAX : static_cast<r_Short>(*value);
    return cell;
}
