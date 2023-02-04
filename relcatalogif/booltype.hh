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
// -*-C++-*- (for Emacs)

#ifndef _BOOLTYPE_HH_
#define _BOOLTYPE_HH_

#include "uintegraltype.hh"     // for UIntegralType
#include "raslib/odmgtypes.hh"  // for r_ULong
#include <iosfwd>               // for ostream

class OId;

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:

BoolType is the base type used for boolean cell values (e.g.  result
of comparison operations, see \Ref{Ops}). The value of a Bool is
stored in one char. BoolType is a persistence capable class.
*/

/**
  * \ingroup Relcatalogifs
  */
class BoolType : public UIntegralType
{
public:
    explicit BoolType(const OId &id);

    BoolType();

    BoolType(const BoolType &) = default;

    BoolType &operator=(const BoolType &) = default;

    ~BoolType() override = default;

    /**
     * Prints a cell cell in hex on stream followed by a space.
     */
    void printCell(std::ostream &stream, const char *cell) const override;

    r_ULong *convertToCULong(const char *cell, r_ULong *value) const override;

    char *makeFromCULong(char *cell, const r_ULong *value) const override;

    static const char *Name;

};

#endif
