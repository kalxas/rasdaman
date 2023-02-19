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

#ifndef D_SINTERVAL_TYPE_HH
#define D_SINTERVAL_TYPE_HH

#include "raslib/type.hh"

//@ManMemo: Module: {\bf raslib}
/**
  * \ingroup raslib
  */

/**
  This class represents the sinterval type in the ODMG conformant
  representation of the RasDaMan type system.
*/

class r_Sinterval_Type : public r_Type
{
public:
    r_Sinterval_Type() = default;
    r_Sinterval_Type(const r_Sinterval_Type &) = default;
    ~r_Sinterval_Type() override = default;

    r_Type *clone() const override;

    /// retrieve id of the type.
    r_Type::r_Type_Id type_id() const override;

    /// converts array of cells from NT byte order to Unix byte order.
    void convertToLittleEndian(char *cells, r_Area noCells) const override;

    /// converts array of cells from Unix byte order to NT byte order.
    void convertToBigEndian(char *cells, r_Area noCells) const override;

    /// writes state of object to specified stream
    void print_status(std::ostream &s) const override;

    bool isSintervalType() const override;
};

//@Doc: write the status of a sinterval type to a stream
extern std::ostream &operator<<(std::ostream &str, const r_Sinterval_Type &type);

#endif



