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

#include "raslib/marraytype.hh"
#include "raslib/basetype.hh"
#include "raslib/error.hh"
#include <logging.hh>

r_Marray_Type::r_Marray_Type()
    :   r_Type()
{
}

r_Marray_Type::r_Marray_Type(const r_Base_Type &newBaseType)
    :   r_Type(),
        baseType(static_cast<r_Base_Type *>(newBaseType.clone()))
{
}

r_Marray_Type::r_Marray_Type(const r_Marray_Type &oldObj)
    :   r_Type(oldObj)
{
    if (oldObj.baseType)
    {
        baseType =  static_cast<r_Base_Type *>(oldObj.baseType->clone());
    }
    else
    {
        LERROR << "the base type is NULL.";
        throw r_Error(MARRAYTYPEHASNOELEMENTTYPE);
    }
}

const r_Marray_Type &
r_Marray_Type::operator=(const r_Marray_Type &oldObj)
{
    if (this == &oldObj)
        return *this;

    r_Type::operator=(oldObj);
    delete baseType;
    baseType = NULL;

    if (oldObj.baseType)
    {
        baseType = static_cast<r_Base_Type *>(oldObj.baseType->clone());
    }
    else
    {
        LERROR << "the base type is NULL.";
        throw r_Error(MARRAYTYPEHASNOELEMENTTYPE);
    }
    return *this;
}

bool
r_Marray_Type::isMarrayType() const
{
    return true;
}

const r_Base_Type &
r_Marray_Type::base_type() const
{
    return *baseType;
}

r_Type *
r_Marray_Type::clone() const
{
    return new r_Marray_Type(*this);
}

r_Type::r_Type_Id
r_Marray_Type::type_id() const
{
    return MARRAYTYPE;
}

void
r_Marray_Type::convertToLittleEndian(char *, r_Area) const
{
}

void
r_Marray_Type::convertToBigEndian(char *, r_Area) const
{
}

void
r_Marray_Type::print_status(std::ostream &s) const
{
    s << "marray< ";
    baseType->print_status(s);
    s << " >";
}

r_Marray_Type::~r_Marray_Type()
{
    delete baseType;
    baseType = NULL;
}

std::ostream &operator<<(std::ostream &str, const r_Marray_Type &type)
{
    type.print_status(str);
    return str;
}
