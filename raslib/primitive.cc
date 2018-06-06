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
/**
 * SOURCE:   primitive.cc
 *
 * MODULE:   raslib
 * CLASS:    r_Primitive
 *
 * COMMENTS:
 *
*/

#include "config.h"
#include "mymalloc/mymalloc.h"

#include "raslib/primitive.hh"
#include "raslib/primitivetype.hh"
#include "raslib/error.hh"

#include <logging.hh>

#include <sstream>
#include <string.h>
#include <fstream>
#include <stdlib.h>

r_Primitive::r_Primitive(const char* newBuffer, const r_Primitive_Type* newType)
    : r_Scalar(newType)
{
    if (valueType)
    {
        valueBuffer = static_cast<char*>(mymalloc(valueType->size()));
        if (newBuffer)
        {
            memcpy(static_cast<void*>(valueBuffer), static_cast<void*>(const_cast<char*>(newBuffer)), valueType->size());
        }
        else
        {
            memset(static_cast<void*>(valueBuffer), 0, valueType->size());
        }
    }
}

r_Primitive::r_Primitive(const r_Primitive& obj)
    : r_Scalar(obj),
      valueBuffer(NULL)
{
    valueBuffer = static_cast<char*>(mymalloc(valueType->size()));
    if (obj.valueBuffer)
    {
        memcpy(static_cast<void*>(valueBuffer), static_cast<void*>(obj.valueBuffer), valueType->size());
    }
    else
    {
        memset(static_cast<void*>(valueBuffer), 0, valueType->size());
    }
}

r_Primitive::~r_Primitive()
{
    if (valueBuffer)
    {
        free(valueBuffer);
    }
}

bool
r_Primitive::isPrimitive() const
{
    return true;
}

r_Scalar*
r_Primitive::clone() const
{
    return new r_Primitive(*this);
}



const r_Primitive&
r_Primitive::operator=(const r_Primitive& obj)
{
    if (this != &obj)
    {
        // assign scalar
        r_Scalar::operator=(obj);

        if (valueBuffer)
        {
            free(valueBuffer);
            valueBuffer = NULL;
        }

        if (valueType)
        {
            valueBuffer = static_cast<char*>(mymalloc(valueType->size()));
            if (obj.valueBuffer)
            {
                memcpy(static_cast<void*>(valueBuffer), static_cast<void*>(obj.valueBuffer), valueType->size());
            }
            else
            {
                memset(static_cast<void*>(valueBuffer), 0, valueType->size());
            }
        }
    }

    return *this;
}



const char*
r_Primitive::get_buffer() const
{
    return valueBuffer;
}



void
r_Primitive::print_status(std::ostream& s) const
{
    if (valueType && valueBuffer)
    {
        valueType->print_value(valueBuffer, s);
    }
    else
    {
        s << "<nn>" << std::flush;
    }
}



r_Boolean
r_Primitive::get_boolean() const
{
    if (!valueBuffer || !valueType)
    {
        LFATAL << "r_Primitive::get_boolean() buffer null or type null ";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    return (static_cast<r_Primitive_Type*>(valueType))->get_boolean(valueBuffer);
}



r_Char
r_Primitive::get_char() const
{
    if (!valueBuffer || !valueType)
    {
        LFATAL << "r_Primitive::get_char() buffer null or type null";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    return (static_cast<r_Primitive_Type*>(valueType))->get_char(valueBuffer);
}



r_Octet
r_Primitive::get_octet() const
{
    if (!valueBuffer || !valueType)
    {
        LFATAL << "r_Primitive::get_octet() buffer null or type null";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    return (static_cast<r_Primitive_Type*>(valueType))->get_octet(valueBuffer);
}



r_Short
r_Primitive::get_short() const
{
    if (!valueBuffer || !valueType)
    {
        LFATAL << "r_Primitive::get_short() buffer null or type null";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    return (static_cast<r_Primitive_Type*>(valueType))->get_short(valueBuffer);
}



r_UShort
r_Primitive::get_ushort() const
{
    if (!valueBuffer || !valueType)
    {
        LFATAL << "r_Primitive::get_ushort() buffer null or type null";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    return (static_cast<r_Primitive_Type*>(valueType))->get_ushort(valueBuffer);
}



r_Long
r_Primitive::get_long() const
{
    if (!valueBuffer || !valueType)
    {
        LFATAL << "r_Primitive::get_long() buffer null or type null";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    return (static_cast<r_Primitive_Type*>(valueType))->get_long(valueBuffer);
}



r_ULong
r_Primitive::get_ulong() const
{
    if (!valueBuffer || !valueType)
    {
        LFATAL << "r_Primitive::get_ulong() buffer null or type null";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    return (static_cast<r_Primitive_Type*>(valueType))->get_ulong(valueBuffer);
}



r_Float
r_Primitive::get_float() const
{
    if (!valueBuffer || !valueType)
    {
        LFATAL << "r_Primitive::get_float() buffer null or type null";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    return (static_cast<r_Primitive_Type*>(valueType))->get_float(valueBuffer);
}



r_Double
r_Primitive::get_double() const
{
    if (!valueBuffer || !valueType)
    {
        LFATAL << "r_Primitive::get_double() buffer null or type null";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    return (static_cast<r_Primitive_Type*>(valueType))->get_double(valueBuffer);
}


void
r_Primitive::set_boolean(r_Boolean val)
{
    if (!valueType || valueType->type_id() != r_Type::BOOL)
    {
        LFATAL << "r_Primitive::set_boolean(" << val << ") not a boolean";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    if (!valueBuffer)
    {
        valueBuffer = static_cast<char*>(mymalloc(valueType->size()));
    }

    memmove(valueBuffer, &val, valueType->size());
}



void
r_Primitive::set_char(r_Char val)
{
    if (!valueType || valueType->type_id() != r_Type::CHAR)
    {
        LFATAL << "r_Primitive::set_char(" << val << ") not a char";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    if (!valueBuffer)
    {
        valueBuffer = static_cast<char*>(mymalloc(valueType->size()));
    }

    memmove(valueBuffer, &val, valueType->size());
}



void
r_Primitive::set_octet(r_Octet val)
{
    if (!valueType || valueType->type_id() != r_Type::OCTET)
    {
        LFATAL << "r_Primitive::set_octet(" << val << ") not a octet";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    if (!valueBuffer)
    {
        valueBuffer = static_cast<char*>(mymalloc(valueType->size()));
    }

    memmove(valueBuffer, &val, valueType->size());
}



void
r_Primitive::set_short(r_Short val)
{
    if (!valueType || valueType->type_id() != r_Type::SHORT)
    {
        LFATAL << "r_Primitive::set_short(" << val << ") not a short";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    if (!valueBuffer)
    {
        valueBuffer = static_cast<char*>(mymalloc(valueType->size()));
    }

    memmove(valueBuffer, &val, valueType->size());
}



void
r_Primitive::set_ushort(r_UShort val)
{
    if (!valueType || valueType->type_id() != r_Type::USHORT)
    {
        LFATAL << "r_Primitive::set_ushort(" << val << ") not a ushort";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    if (!valueBuffer)
    {
        valueBuffer = static_cast<char*>(mymalloc(valueType->size()));
    }

    memmove(valueBuffer, &val, valueType->size());
}



void
r_Primitive::set_long(r_Long val)
{
    if (!valueType || valueType->type_id() != r_Type::LONG)
    {
        LFATAL << "r_Primitive::set_long(" << val << ") not a long";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    if (!valueBuffer)
    {
        valueBuffer = static_cast<char*>(mymalloc(valueType->size()));
    }

    memmove(valueBuffer, &val, valueType->size());
}



void
r_Primitive::set_ulong(r_ULong val)
{
    if (!valueType || valueType->type_id() != r_Type::ULONG)
    {
        LFATAL << "r_Primitive::set_ulong(" << val << ") not a ulong";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    if (!valueBuffer)
    {
        valueBuffer = static_cast<char*>(mymalloc(valueType->size()));
    }

    memmove(valueBuffer, &val, valueType->size());
}



void
r_Primitive::set_float(r_Float val)
{
    if (!valueType || valueType->type_id() != r_Type::FLOAT)
    {
        LFATAL << "r_Primitive::set_float(" << val << ") not a float";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    if (!valueBuffer)
    {
        valueBuffer = static_cast<char*>(mymalloc(valueType->size()));
    }

    memmove(valueBuffer, &val, valueType->size());
}



void
r_Primitive::set_double(r_Double val)
{
    if (!valueType || valueType->type_id() != r_Type::DOUBLE)
    {
        LFATAL << "r_Primitive::set_double(" << val << ") not a double";
        r_Error err(r_Error::r_Error_TypeInvalid);
        throw err;
    }

    if (!valueBuffer)
    {
        valueBuffer = static_cast<char*>(mymalloc(valueType->size()));
    }
    memmove(valueBuffer, &val, valueType->size());
}


std::ostream& operator<<(std::ostream& s, const r_Primitive& obj)
{
    obj.print_status(s);
    return s;
}

