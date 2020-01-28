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

#include "raslib/complextype.hh"
#include "raslib/endian.hh"
#include "raslib/error.hh"

#include <logging.hh>

r_Complex_Type::r_Complex_Type()
    :   r_Primitive_Type()
{
}

r_Complex_Type::r_Complex_Type(const char *newTypeName, const r_Type::r_Type_Id newTypeId)
    :   r_Primitive_Type(newTypeName, newTypeId)
{
    switch (typeId)
    {
    case COMPLEXTYPE1:
        imOff = sizeof(r_Float);  break;
    case COMPLEXTYPE2:
        imOff = sizeof(r_Double); break;
    case CINT16:
        imOff = sizeof(r_Short);  break;
    case CINT32:
        imOff = sizeof(r_Long);   break;
    default:
        LERROR << "invalid complex typeId " << typeId;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

r_Type *
r_Complex_Type::clone() const
{
    return new r_Complex_Type(*this);
}

r_Double
r_Complex_Type::get_re(const char *cell) const
{
    switch (typeId)
    {
    case COMPLEXTYPE1:
        return static_cast<r_Double>(*reinterpret_cast<const r_Float*>(cell));
    case COMPLEXTYPE2:
        return *reinterpret_cast<const r_Double*>(cell);
    default:
        LERROR << "invalid complex typeId " << typeId;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

r_Double
r_Complex_Type::get_im(const char *cell) const
{
    switch (typeId)
    {
    case COMPLEXTYPE1:
        return static_cast<r_Double>(*reinterpret_cast<const r_Float*>(cell + imOff));
    case COMPLEXTYPE2:
        return *reinterpret_cast<const r_Double*>(cell + imOff);
    default:
        LERROR << "invalid complex typeId " << typeId;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}
//versions of getters for integer complex numbers
r_Long
r_Complex_Type::get_re_long(const char *cell) const
{
    switch (typeId)
    {
    case CINT16:
        return *reinterpret_cast<const r_Short*>(cell);
    case CINT32:
        return *reinterpret_cast<const r_Long*>(cell);
    default:
        LERROR << "invalid complex typeId " << typeId;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

r_Long
r_Complex_Type::get_im_long(const char *cell) const
{
    switch (typeId)
    {
    case CINT16:
        return *reinterpret_cast<const r_Short*>(cell + imOff);
    case CINT32:
        return *reinterpret_cast<const r_Long*>(cell + imOff);
    default:
        LERROR << "invalid complex typeId " << typeId;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}


void
r_Complex_Type::set_re(char *cell, r_Double re)
{
    switch (typeId)
    {
    case COMPLEXTYPE1:
    {
        auto ref = static_cast<r_Float>(re);
        memmove(cell, &ref, imOff);
        break;
    }
    case COMPLEXTYPE2:
        memmove(cell, &re, imOff);
        break;
    default:
        LERROR << "invalid complex typeId " << typeId;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

void
r_Complex_Type::set_im(char *cell, r_Double im)
{
    switch (typeId)
    {
    case COMPLEXTYPE1:
    {
        auto imf = static_cast<r_Float>(im);
        memmove(cell + imOff, &imf, imOff);
        break;
    }
    case COMPLEXTYPE2:
        memmove(cell + imOff, &im, imOff);
        break;
    default:
        LERROR << "invalid complex typeId " << typeId;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

//setters for complex integers
void
r_Complex_Type::set_re_long(char *cell, r_Long re)
{
    switch (typeId)
    {
    case CINT16:
    {
        auto ref = static_cast<r_Short>(re);
        memmove(cell, &ref, imOff);
        break;
    }
    case CINT32:
        memmove(cell, &re, imOff);
        break;
    default:
        LERROR << "invalid complex typeId " << typeId;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

void
r_Complex_Type::set_im_long(char *cell, r_Long im)
{
    switch (typeId)
    {
    case CINT16:
    {
        auto imf = static_cast<r_Long>(im);
        memmove(cell + imOff, &imf, imOff);
        break;
    }
    case CINT32:
        memmove(cell + imOff, &im, imOff);
        break;
    default:
        LERROR << "invalid complex typeId " << typeId;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}
void
r_Complex_Type::print_status(std::ostream &s) const
{
    switch (typeId)
    {
    case COMPLEXTYPE1: s << "complex(float, float)"; break;
    case COMPLEXTYPE2: s << "complex(double, double)"; break;
    case CINT16:       s << "complex(short, short)"; break;
    case CINT32:       s << "complex(long, long)"; break;
    default:
        LERROR << "invalid complex typeId " << typeId;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

void
r_Complex_Type::print_value(const char *storage, std::ostream &s) const
{
    switch (typeId)
    {
    case COMPLEXTYPE1:
    case COMPLEXTYPE2:
        s << "(" << get_re(storage) << "," << get_im(storage) << ")";
        break;
    case CINT16:
    case CINT32:
        s << "(" << get_re_long(storage) << "," << get_im_long(storage) << ")";
        break;
    default:
        LERROR << "invalid complex typeId " << typeId;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

void
r_Complex_Type::convertToLittleEndian(char *cells, r_Area noCells) const
{
    switch (typeId)
    {
    case COMPLEXTYPE1:
        swapEndianessDouble<r_Float>(cells, noCells);
        break;
    case COMPLEXTYPE2:
        swapEndianessDouble<r_Double>(cells, noCells);
        break;
    case CINT16:
        swapEndianessLong<r_Short>(cells, noCells);
        break;
    case CINT32:
        swapEndianessLong<r_Long>(cells, noCells);
        break;
    default:
        LERROR << "invalid complex typeId " << typeId;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

void
r_Complex_Type::convertToBigEndian(char *cells, r_Area noCells) const
{
    convertToLittleEndian(cells, noCells);
}

template <typename T>
void r_Complex_Type::swapEndianessDouble(char *cells, r_Area noCells) const
{
    for (r_Area i = 0; i < noCells; ++i)
    {
        *reinterpret_cast<T*>(cells + i * typeSize) =
                r_Endian::swap(static_cast<T>(get_re(cells + i * typeSize)));
        *reinterpret_cast<T*>(cells + i * typeSize + imOff) =
                r_Endian::swap(static_cast<T>(get_im(cells + i * typeSize)));
    }
}

template <typename T>
void r_Complex_Type::swapEndianessLong(char *cells, r_Area noCells) const
{
    for (r_Area i = 0; i < noCells; ++i)
    {
        *reinterpret_cast<T*>(cells + i * typeSize) =
                r_Endian::swap(static_cast<T>(get_re_long(cells + i * typeSize)));
        *reinterpret_cast<T*>(cells + i * typeSize + imOff) =
                r_Endian::swap(static_cast<T>(get_im_long(cells + i * typeSize)));
    }
}

bool
r_Complex_Type::isComplexType() const
{
    return true;
}

std::ostream &operator<<(std::ostream &str, const r_Complex_Type &type)
{
    type.print_status(str);
    return str;
}
