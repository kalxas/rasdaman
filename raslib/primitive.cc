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

#include "raslib/primitive.hh"
#include "raslib/primitivetype.hh"
#include "raslib/error.hh"

#include <logging.hh>

#include <sstream>
#include <cstring>
#include <fstream>
#include <cstdlib>

r_Primitive::r_Primitive(const char *newBuffer, const r_Primitive_Type *newType)
    : r_Scalar(newType)
{
    if (valueType)
    {
        valueBuffer = new char[valueType->size()];
        if (newBuffer)
            memcpy(static_cast<void *>(valueBuffer), static_cast<const void *>(newBuffer), valueType->size());
        else
            memset(static_cast<void *>(valueBuffer), 0, valueType->size());
    }
}

r_Primitive::r_Primitive(const r_Primitive &obj)
    : r_Scalar(obj)
{
    valueBuffer = new char[valueType->size()];
    if (obj.valueBuffer)
        memcpy(static_cast<void *>(valueBuffer), static_cast<void *>(obj.valueBuffer), valueType->size());
    else
        memset(static_cast<void *>(valueBuffer), 0, valueType->size());
}

r_Primitive::~r_Primitive()
{
     delete[] valueBuffer;
     valueBuffer = NULL;
}

bool
r_Primitive::isPrimitive() const
{
    return true;
}

r_Scalar *
r_Primitive::clone() const
{
    return new r_Primitive(*this);
}

const r_Primitive &
r_Primitive::operator=(const r_Primitive &obj)
{
    if (this != &obj)
    {
        r_Scalar::operator=(obj);
        if (valueBuffer)
        {
            delete[] valueBuffer;
            valueBuffer = NULL;
        }
        if (valueType)
        {
            valueBuffer = new char[valueType->size()];
            if (obj.valueBuffer)
                memcpy(static_cast<void *>(valueBuffer), static_cast<void *>(obj.valueBuffer), valueType->size());
            else
                memset(static_cast<void *>(valueBuffer), 0, valueType->size());
        }
    }

    return *this;
}

const char *
r_Primitive::get_buffer() const
{
    return valueBuffer;
}

r_Boolean
r_Primitive::get_boolean() const
{
    checkBufferAndType();
    return static_cast<r_Primitive_Type *>(valueType)->get_boolean(valueBuffer);
}
r_Char
r_Primitive::get_char() const
{
    checkBufferAndType();
    return static_cast<r_Primitive_Type *>(valueType)->get_char(valueBuffer);
}
r_Octet
r_Primitive::get_octet() const
{
    checkBufferAndType();
    return static_cast<r_Primitive_Type *>(valueType)->get_octet(valueBuffer);
}
r_Short
r_Primitive::get_short() const
{
    checkBufferAndType();
    return static_cast<r_Primitive_Type *>(valueType)->get_short(valueBuffer);
}
r_UShort
r_Primitive::get_ushort() const
{
    checkBufferAndType();
    return static_cast<r_Primitive_Type *>(valueType)->get_ushort(valueBuffer);
}
r_Long
r_Primitive::get_long() const
{
    checkBufferAndType();
    return static_cast<r_Primitive_Type *>(valueType)->get_long(valueBuffer);
}
r_ULong
r_Primitive::get_ulong() const
{
    checkBufferAndType();
    return static_cast<r_Primitive_Type *>(valueType)->get_ulong(valueBuffer);
}
r_Float
r_Primitive::get_float() const
{
    checkBufferAndType();
    return static_cast<r_Primitive_Type *>(valueType)->get_float(valueBuffer);
}
r_Double
r_Primitive::get_double() const
{
    checkBufferAndType();
    return static_cast<r_Primitive_Type *>(valueType)->get_double(valueBuffer);
}

void
r_Primitive::set_boolean(r_Boolean val)
{
    checkBufferAndType(r_Type::BOOL);
    memmove(valueBuffer, &val, valueType->size());
}
void
r_Primitive::set_char(r_Char val)
{
    checkBufferAndType(r_Type::CHAR);
    memmove(valueBuffer, &val, valueType->size());
}
void
r_Primitive::set_octet(r_Octet val)
{
    checkBufferAndType(r_Type::OCTET);
    memmove(valueBuffer, &val, valueType->size());
}
void
r_Primitive::set_short(r_Short val)
{
    checkBufferAndType(r_Type::SHORT);
    memmove(valueBuffer, &val, valueType->size());
}
void
r_Primitive::set_ushort(r_UShort val)
{
    checkBufferAndType(r_Type::USHORT);
    memmove(valueBuffer, &val, valueType->size());
}
void
r_Primitive::set_long(r_Long val)
{
    checkBufferAndType(r_Type::LONG);
    memmove(valueBuffer, &val, valueType->size());
}
void
r_Primitive::set_ulong(r_ULong val)
{
    checkBufferAndType(r_Type::ULONG);
    memmove(valueBuffer, &val, valueType->size());
}
void
r_Primitive::set_float(r_Float val)
{
    checkBufferAndType(r_Type::FLOAT);
    memmove(valueBuffer, &val, valueType->size());
}
void
r_Primitive::set_double(r_Double val)
{
    checkBufferAndType(r_Type::DOUBLE);
    memmove(valueBuffer, &val, valueType->size());
}

void r_Primitive::checkBufferAndType() const
{
    if (!valueBuffer)
    {
        LERROR << "buffer null";
        throw r_Error(r_Error::r_Error_RefInvalid);
    }
    if (!valueType)
    {
        LERROR << "type null";
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}
void r_Primitive::checkBufferAndType(r_Type::r_Type_Id type)
{
    if (!valueType)
    {
        LERROR << "type null";
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
    if (valueType->type_id() != type)
    {
        LERROR << "given value does not match the primitive type " << type;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
    if (!valueBuffer)
    {
        valueBuffer = new char[valueType->size()];
    }
}

std::ostream &operator<<(std::ostream &s, const r_Primitive &obj)
{
    obj.print_status(s);
    return s;
}

void
r_Primitive::print_status(std::ostream &s) const
{
    if (valueType && valueBuffer)
        valueType->print_value(valueBuffer, s);
    else
        s << "<nn>" << std::flush;
}
