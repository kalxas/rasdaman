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

#ifndef _D_SCALAR_
#define _D_SCALAR_

#include <iosfwd>

class r_Base_Type;

//@ManMemo: Module: {\bf raslib}
/**
  * \ingroup raslib
  */

/**

 Class r_Scalar represents a scalar type value which
 is either r_Primitive or r_Structure.

*/

class r_Scalar
{
public:
    r_Scalar(const r_Base_Type *newType);
    r_Scalar(const r_Scalar &obj);
    virtual ~r_Scalar();

    virtual r_Scalar *clone() const = 0;
    const r_Scalar &operator=(const r_Scalar &);

    virtual void print_status(std::ostream &s) const = 0;

    virtual const r_Base_Type *get_type() const;

    virtual bool isStructure() const;
    virtual bool isComplex() const;
    virtual bool isPrimitive() const;

protected:
    r_Base_Type *valueType{NULL};
};

//@ManMemo: Module: {\bf raslib}
/**
  Output stream operator for objects of type <tt>const</tt> r_Scalar.
*/
extern std::ostream &operator<<(std::ostream &s, const r_Scalar &obj);

#endif
