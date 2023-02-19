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

#include "raslib/basetype.hh"

r_Base_Type::r_Base_Type(const char *newTypeName, r_Bytes newTypeSize)
    :   r_Type(newTypeName),
        typeSize(newTypeSize)
{
}

r_Base_Type::r_Base_Type(const r_Base_Type &oldObj)
    : r_Type(oldObj),
      typeSize(oldObj.typeSize)
{
}

const r_Base_Type &
r_Base_Type::operator=(const r_Base_Type &oldObj)
{
    // Gracefully handle self assignment
    if (this == &oldObj)
    {
        return *this;
    }

    r_Type::operator=(oldObj);
    typeSize = oldObj.typeSize;

    return *this;
}

bool
r_Base_Type::isBaseType() const
{
    return true;
}

r_Bytes
r_Base_Type::size() const
{
    return typeSize;
}

