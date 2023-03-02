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

#ifndef R_COMPLEX_HH
#define R_COMPLEX_HH

#include "raslib/odmgtypes.hh"
#include "raslib/primitive.hh"

class r_Complex_Type;

//@ManMemo: Module: {\bf raslib}
/**
  * \ingroup raslib
  */

/**

 Class r_Complex represents a complex type value.

*/
class r_Complex : public r_Primitive
{
public:
    explicit
        /// constructs a scalar type value
        r_Complex(const char *newBuffer, const r_Complex_Type *newType);

    /// copy constructor
    r_Complex(const r_Complex &obj);

    /// destructor
    ~r_Complex() override = default;

    virtual bool isComplex() const override;

    /// clone operator
    virtual r_Scalar *clone() const override;

    /// operator for assigning a primitive
    const r_Complex &operator=(const r_Complex &);

    r_Double get_re() const;
    r_Double get_im() const;

    void set_re(r_Double);
    void set_im(r_Double);

    r_Long get_re_long() const;
    r_Long get_im_long() const;

    void set_re_long(r_Long);
    void set_im_long(r_Long);

private:
    void checkTypeAndBuffer() const;
};

#endif
