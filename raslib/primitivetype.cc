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

#include "raslib/primitivetype.hh"
#include "raslib/odmgtypes.hh"
#include "raslib/endian.hh"
#include "raslib/error.hh"

#include <logging.hh>

#include <iomanip>
#include <cstring>

r_Primitive_Type::r_Primitive_Type(const char *newTypeName,
                                   const r_Type::r_Type_Id newTypeId)
    : r_Base_Type(newTypeName, 0), typeId(newTypeId)
{
    switch (typeId)
    {
    case BOOL:
    case OCTET:
    case CHAR:
        typeSize = sizeof(r_Char);
        break;
    case USHORT:
    case SHORT:
        typeSize = sizeof(r_Short);
        break;
    case LONG:
    case ULONG:
        typeSize = sizeof(r_Long);
        break;
    case FLOAT:
        typeSize = sizeof(r_Float);
        break;
    case DOUBLE:
        typeSize = sizeof(r_Double);
        break;
    case COMPLEXTYPE1:
        typeSize = 2 * sizeof(r_Float);
        break;
    case COMPLEXTYPE2:
        typeSize = 2 * sizeof(r_Double);
        break;
    case CINT16:
        typeSize = 2 * sizeof(r_Short);
        break;
    case CINT32:
        typeSize = 2 * sizeof(r_Long);
        break;
    default:
        LWARNING << "unknown typeId " << typeId;
        break;
    }
}

r_Primitive_Type::r_Primitive_Type(const r_Primitive_Type &oldObj)
    : r_Base_Type(oldObj), typeId(oldObj.typeId)
{
}

const r_Primitive_Type &
r_Primitive_Type::operator=(const r_Primitive_Type &oldObj)
{
    // Gracefully handle self assignment
    if (this == &oldObj)
        return *this;

    r_Base_Type::operator=(oldObj);
    typeId = oldObj.typeId;
    return *this;
}

r_Type *
r_Primitive_Type::clone() const
{
    return new r_Primitive_Type(*this);
}

r_Type::r_Type_Id
r_Primitive_Type::type_id() const
{
    return typeId;
}

bool r_Primitive_Type::isPrimitiveType() const
{
    return true;
}

void r_Primitive_Type::convertToLittleEndian(char *cells, r_Area noCells) const
{
    switch (typeId)
    {
    case BOOL:
    case OCTET:
    case CHAR:
        break;  // skip
    case USHORT:
    case SHORT:
        for (r_Bytes i = 0; i < noCells; i++)
        {
            char c1 = cells[i * typeSize];
            char c0 = cells[i * typeSize + 1];
            cells[i * typeSize] = c0;
            cells[i * typeSize + 1] = c1;
        }
        break;
    case ULONG:
    case LONG:
        for (r_Bytes i = 0; i < noCells; i++)
        {
            char c3 = cells[i * typeSize];
            char c2 = cells[i * typeSize + 1];
            char c1 = cells[i * typeSize + 2];
            char c0 = cells[i * typeSize + 3];
            cells[i * typeSize] = c0;
            cells[i * typeSize + 1] = c1;
            cells[i * typeSize + 2] = c2;
            cells[i * typeSize + 3] = c3;
        }
        break;
    case FLOAT:
        for (r_Bytes i = 0; i < noCells; ++i)
            reinterpret_cast<r_Float *>(cells)[i] = r_Endian::swap(reinterpret_cast<r_Float *>(cells)[i]);
        break;

    case DOUBLE:
        for (r_Bytes i = 0; i < noCells; ++i)
            reinterpret_cast<r_Double *>(cells)[i] = r_Endian::swap(reinterpret_cast<r_Double *>(cells)[i]);
        break;
    default:
        LWARNING << "cannot convert endianness of typeId " << typeId;
        break;
    }
}

void r_Primitive_Type::convertToBigEndian(char *cells, r_Area noCells) const
{
    return convertToLittleEndian(cells, noCells);
}

void r_Primitive_Type::print_status(std::ostream &s) const
{
    s << typeId;
}

void r_Primitive_Type::print_value(const char *storage, std::ostream &s) const
{
    switch (typeId)
    {
    case r_Type::BOOL: s << std::setw(5) << (get_boolean(storage) ? "T" : "F"); break;
    case r_Type::CHAR: s << std::setw(5) << static_cast<int>(get_char(storage)); break;
    case r_Type::OCTET: s << std::setw(5) << static_cast<int>(get_octet(storage)); break;
    case r_Type::SHORT: s << std::setw(5) << get_short(storage); break;
    case r_Type::USHORT: s << std::setw(5) << get_ushort(storage); break;
    case r_Type::LONG: s << std::setw(5) << get_long(storage); break;
    case r_Type::ULONG: s << std::setw(5) << get_ulong(storage); break;
    case r_Type::FLOAT: s << std::setw(5) << get_float(storage); break;
    case r_Type::DOUBLE: s << std::setw(5) << get_double(storage); break;
    default:
        LWARNING << "cannot print value of typeId " << typeId;
        break;
    }
}

//FIXME
// We have to return the value in the most powerfull type(e.g. now r_Double) without loss
// This may change in future
r_Double
r_Primitive_Type::get_value(const char *storage) const
{
    switch (typeId)
    {
    case r_Type::ULONG: return get_ulong(storage);
    case r_Type::USHORT: return get_ushort(storage);
    case r_Type::BOOL: return get_boolean(storage);
    case r_Type::LONG: return get_long(storage);
    case r_Type::SHORT: return get_short(storage);
    case r_Type::OCTET: return get_octet(storage);
    case r_Type::DOUBLE: return get_double(storage);
    case r_Type::FLOAT: return static_cast<r_Double>(get_float(storage));
    case r_Type::CHAR: return get_char(storage);
    default:
        LERROR << "cannot get value as double of typeid " << type_id();
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

//FIXME
// We have to set the value from the most powerfull type(e.g. now r_Double) without loss
// This may change in future
void r_Primitive_Type::set_value(char *storage, r_Double val)
{
    switch (typeId)
    {
    case r_Type::ULONG: set_ulong(storage, static_cast<r_ULong>(val)); break;
    case r_Type::USHORT: set_ushort(storage, static_cast<r_UShort>(val)); break;
    case r_Type::BOOL: set_boolean(storage, static_cast<r_Boolean>(val)); break;
    case r_Type::LONG: set_long(storage, static_cast<r_Long>(val)); break;
    case r_Type::SHORT: set_short(storage, static_cast<r_Short>(val)); break;
    case r_Type::OCTET: set_octet(storage, static_cast<r_Octet>(val)); break;
    case r_Type::DOUBLE: set_double(storage, static_cast<r_Double>(val)); break;
    case r_Type::FLOAT: set_float(storage, static_cast<r_Float>(val)); break;
    case r_Type::CHAR: set_char(storage, static_cast<r_Char>(val)); break;
    default:
        LERROR << "cannot set value as double of typeid " << type_id();
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

//FIXME
// We have to set the value from the most powerfull type(e.g. now r_Double) without loss
// This may change in future
void r_Primitive_Type::get_limits(r_Double &min, r_Double &max)
{
    switch (typeId)
    {
    case r_Type::ULONG: get_limits_Ulong(min, max); break;
    case r_Type::USHORT: get_limits_Ushort(min, max); break;
    case r_Type::BOOL: get_limits_char(min, max); break;
    case r_Type::LONG: get_limits_long(min, max); break;
    case r_Type::SHORT: get_limits_short(min, max); break;
    case r_Type::OCTET: get_limits_octet(min, max); break;
    case r_Type::DOUBLE: get_limits_double(min, max); break;
    case r_Type::FLOAT: get_limits_float(min, max); break;
    case r_Type::CHAR: get_limits_char(min, max); break;
    default:
        LERROR << "cannot set limits of typeid " << type_id();
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

r_Boolean
r_Primitive_Type::get_boolean(const char *cell) const
{
    checkType(r_Type::BOOL);
    return *(reinterpret_cast<const r_Boolean *>(cell));
}
r_Char
r_Primitive_Type::get_char(const char *cell) const
{
    checkType(r_Type::CHAR);
    return *(reinterpret_cast<const r_Char *>(cell));
}
r_Octet
r_Primitive_Type::get_octet(const char *cell) const
{
    checkType(r_Type::OCTET);
    return *(reinterpret_cast<const r_Octet *>(cell));
}
r_Short
r_Primitive_Type::get_short(const char *cell) const
{
    checkType(r_Type::SHORT);
    return *(reinterpret_cast<const r_Short *>(cell));
}
r_UShort
r_Primitive_Type::get_ushort(const char *cell) const
{
    checkType(r_Type::USHORT);
    return *(reinterpret_cast<const r_UShort *>(cell));
}
r_Long
r_Primitive_Type::get_long(const char *cell) const
{
    checkType(r_Type::LONG);
    return *(reinterpret_cast<const r_Long *>(cell));
}
r_ULong
r_Primitive_Type::get_ulong(const char *cell) const
{
    checkType(r_Type::ULONG);
    return *(reinterpret_cast<const r_ULong *>(cell));
}
r_Float
r_Primitive_Type::get_float(const char *cell) const
{
    checkType(r_Type::FLOAT);
    return *(reinterpret_cast<const r_Float *>(cell));
}
r_Double
r_Primitive_Type::get_double(const char *cell) const
{
    checkType(r_Type::DOUBLE);
    return *(reinterpret_cast<const r_Double *>(cell));
}

void r_Primitive_Type::set_boolean(char *cell, r_Boolean val)
{
    checkType(r_Type::BOOL);
    memmove(cell, &val, typeSize);
}
void r_Primitive_Type::set_char(char *cell, r_Char val)
{
    checkType(r_Type::CHAR);
    memmove(cell, &val, typeSize);
}
void r_Primitive_Type::set_octet(char *cell, r_Octet val)
{
    checkType(r_Type::OCTET);
    memmove(cell, &val, typeSize);
}
void r_Primitive_Type::set_short(char *cell, r_Short val)
{
    checkType(r_Type::SHORT);
    memmove(cell, &val, typeSize);
}
void r_Primitive_Type::set_ushort(char *cell, r_UShort val)
{
    checkType(r_Type::USHORT);
    memmove(cell, &val, typeSize);
}
void r_Primitive_Type::set_long(char *cell, r_Long val)
{
    checkType(r_Type::LONG);
    memmove(cell, &val, typeSize);
}
void r_Primitive_Type::set_ulong(char *cell, r_ULong val)
{
    checkType(r_Type::ULONG);
    memmove(cell, &val, typeSize);
}
void r_Primitive_Type::set_float(char *cell, r_Float val)
{
    checkType(r_Type::FLOAT);
    memmove(cell, &val, typeSize);
}
void r_Primitive_Type::set_double(char *cell, r_Double val)
{
    checkType(r_Type::DOUBLE);
    memmove(cell, &val, typeSize);
}

void r_Primitive_Type::checkType(r_Type::r_Type_Id cellType) const
{
    if (typeId != cellType)
    {
        LERROR << "value type " << cellType << " does not match the current type " << typeId;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

std::ostream &operator<<(std::ostream &str, const r_Primitive_Type &type)
{
    type.print_status(str);
    return str;
}
