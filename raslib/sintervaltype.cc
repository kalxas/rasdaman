/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#include "raslib/sintervaltype.hh"
#include <ostream>

r_Type *
r_Sinterval_Type::clone() const
{
    return new r_Sinterval_Type(*this);
}

r_Type::r_Type_Id
r_Sinterval_Type::type_id() const
{
    return SINTERVALTYPE;
}

bool r_Sinterval_Type::isSintervalType() const
{
    return true;
}

void r_Sinterval_Type::convertToLittleEndian(char *, r_Area) const
{
}

void r_Sinterval_Type::convertToBigEndian(char *, r_Area) const
{
}

void r_Sinterval_Type::print_status(std::ostream &s) const
{
    s << "interval";
}

std::ostream &operator<<(std::ostream &str, const r_Sinterval_Type &type)
{
    type.print_status(str);
    return str;
}
