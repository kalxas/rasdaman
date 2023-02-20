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

#ifndef _D_COMPLEXTYPE_TYPE_
#define _D_COMPLEXTYPE_TYPE_

#include "raslib/primitivetype.hh"
#include "raslib/odmgtypes.hh"
#include "raslib/mddtypes.hh"

/**
  * \ingroup raslib
  */
class r_Complex_Type :  public r_Primitive_Type
{
public:
    r_Complex_Type();
    r_Complex_Type(const char *newTypeName, const r_Type::r_Type_Id newTypeId);
    r_Complex_Type(const r_Complex_Type &) = default;
    r_Complex_Type &operator=(const r_Complex_Type &oldObj) = default;
    virtual ~r_Complex_Type() = default;

    virtual r_Type *clone() const;
    virtual void print_status(std::ostream &s) const;
    virtual void print_value(const char *storage, std::ostream &s) const;

    r_Double get_re(const char *cell) const;
    r_Double get_im(const char *cell) const;

    r_Long get_re_long(const char *cell) const;
    r_Long get_im_long(const char *cell) const;

    void set_re(char *cell, r_Double re);
    void set_im(char *cell, r_Double im);
    void set_re_long(char *cell, r_Long re);
    void set_im_long(char *cell, r_Long im);

    virtual void convertToLittleEndian(char *cells, r_Area noCells) const;
    virtual void convertToBigEndian(char *cells, r_Area noCells) const;
    
    virtual bool isComplexType() const;

private:
    template <typename T>
    void swapEndianessDouble(char *cells, r_Area noCells) const;
    template <typename T>
    void swapEndianessLong(char *cells, r_Area noCells) const;
    
    r_Bytes imOff{};
};

//@Doc: write the status of a complex type to a stream
extern std::ostream &operator<<(std::ostream &str, const r_Complex_Type &type);

#endif
