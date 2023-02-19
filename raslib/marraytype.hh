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

#ifndef _D_MARRAY_TYPE_
#define _D_MARRAY_TYPE_

#include "raslib/type.hh"

class r_Base_Type;

//@ManMemo: Module: {\bf raslib}
/**
  * \ingroup raslib
  */

/**
  This class represents the marray type in the ODMG conformant
  representation of the RasDaMan type system.
*/
class r_Marray_Type : public r_Type
{
public:
    /// constructor getting basetype
    explicit r_Marray_Type(const r_Base_Type &);

    /// copy constructor
    /// if base type is NULL an exception will be raised.
    /// this is possible
    r_Marray_Type(const r_Marray_Type &);
    
    /// destructor
    ~r_Marray_Type() override;

    /// assignment operator
    /// if base type is NULL an exception will be raised.
    /// this is possible
    const r_Marray_Type &operator=(const r_Marray_Type &);

    bool isMarrayType() const override;

    /// get base type
    const r_Base_Type &base_type() const;

    /// clone operation
    r_Type *clone() const override;

    /// retrieve id of the type.
    r_Type::r_Type_Id type_id() const override;

    /// converts array of cells from NT byte order to Unix byte order.
    void convertToLittleEndian(char *cells, r_Area noCells) const override;

    /// converts array of cells from Unix byte order to NT byte order.
    void convertToBigEndian(char *cells, r_Area noCells) const override;

    /// writes state of object to specified stream
    void print_status(std::ostream &s) const override;

protected:
    /// default constructor
    /// should be used by noone
    r_Marray_Type();

    /// base type
    r_Base_Type *baseType{NULL};
};

//@Doc: write the status of a marray type to a stream
extern std::ostream &operator<<(std::ostream &str, const r_Marray_Type &type);

#endif

