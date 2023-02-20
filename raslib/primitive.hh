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

#ifndef _D_PRIMITIVE_
#define _D_PRIMITIVE_

#include "raslib/scalar.hh"
#include "raslib/odmgtypes.hh"
#include "raslib/type.hh"
#include <iosfwd>

class r_Primitive_Type;

//@ManMemo: Module: {\bf raslib}
/**
  * \ingroup raslib
  */

/**
 Class r_Primitive represents a primitive (atomic) type value.
*/
class r_Primitive : public r_Scalar
{
public:

    r_Primitive(const char *newBuffer, const r_Primitive_Type *newType);
    r_Primitive(const r_Primitive &obj);
    ~r_Primitive() override;

    /// clone operator
    r_Scalar *clone() const override;

    /// operator for assigning a primitive
    const r_Primitive &operator=(const r_Primitive &);

    /// gets the pointer to the buffer
    const char *get_buffer() const;

    /// debug output
    void print_status(std::ostream &s) const override;

    bool isPrimitive() const override;

    //@Man: Type-safe value access methods. In case of type mismatch, an exception is raised.
    //@{
    r_Boolean get_boolean() const;
    r_Char    get_char()    const;
    r_Octet   get_octet()   const;
    r_Short   get_short()   const;
    r_UShort  get_ushort()  const;
    r_Long    get_long()    const;
    r_ULong   get_ulong()   const;
    r_Float   get_float()   const;
    r_Double  get_double()  const;

    void set_boolean(r_Boolean);
    void set_char(r_Char);
    void set_octet(r_Octet);
    void set_short(r_Short);
    void set_ushort(r_UShort);
    void set_long(r_Long);
    void set_ulong(r_ULong);
    void set_float(r_Float);
    void set_double(r_Double);
    //@}

protected:
    void checkBufferAndType() const;
    void checkBufferAndType(r_Type::r_Type_Id type);
    
    /// buffer
    char *valueBuffer{NULL};
};



//@ManMemo: Module: {\bf raslib}
/**
  Output stream operator for objects of type <tt>const</tt> r_Primitive.
*/
extern std::ostream &operator<<(std::ostream &s, const r_Primitive &obj);

#endif

