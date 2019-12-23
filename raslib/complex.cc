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

#include "raslib/complex.hh"
#include "raslib/error.hh"
#include "raslib/complextype.hh"
#include <logging.hh>

r_Complex::r_Complex(const char *newBuffer, const r_Complex_Type *newType)
    :   r_Primitive(newBuffer, newType)
{
    checkTypeAndBuffer();
}

r_Complex::r_Complex(const r_Complex &obj)
    :   r_Primitive(obj)
{
    checkTypeAndBuffer();
}

r_Scalar *
r_Complex::clone() const
{
    return new r_Complex(*this);
}

const r_Complex &
r_Complex::operator=(const r_Complex &obj)
{
    r_Primitive::operator=(obj);
    return *this;
}

// float / double complex 

r_Double
r_Complex::get_re() const
{
    return static_cast<r_Complex_Type *>(valueType)->get_re(get_buffer());
}
r_Double
r_Complex::get_im() const
{
    return static_cast<r_Complex_Type *>(valueType)->get_im(get_buffer());
}
void
r_Complex::set_re(r_Double re)
{
    static_cast<r_Complex_Type *>(valueType)->set_re(valueBuffer, re);
}
void
r_Complex::set_im(r_Double im)
{
    static_cast<r_Complex_Type *>(valueType)->set_im(valueBuffer, im);
}

// short / long complex 

r_Long
r_Complex::get_re_long() const
{
    return static_cast<r_Complex_Type *>(valueType)->get_re_long(valueBuffer);
}
r_Long
r_Complex::get_im_long() const
{
    return static_cast<r_Complex_Type *>(valueType)->get_im_long(valueBuffer);
}
void
r_Complex::set_re_long(r_Long re)
{
    static_cast<r_Complex_Type *>(valueType)->set_re_long(valueBuffer, re);
}
void
r_Complex::set_im_long(r_Long im)
{
    static_cast<r_Complex_Type *>(valueType)->set_im_long(valueBuffer, im);
}

void r_Complex::checkTypeAndBuffer() const
{
    if (!valueType || !valueType->isComplexType())
    {
        LERROR << "value type is not a complex or not initialised";
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
    if (!valueBuffer)
    {
        LERROR << "value buffer not allocated";
        throw r_Error(r_Error::r_Error_RefInvalid);
    }
}

bool
r_Complex::isComplex() const
{
    return true;
}

