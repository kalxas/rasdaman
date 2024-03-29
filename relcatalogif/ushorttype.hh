// -*-C++-*- (for Emacs)

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

#ifndef _USHORTTYPE_HH_
#define _USHORTTYPE_HH_

#include <iosfwd>               // for ostream
#include "uintegraltype.hh"     // for UIntegralType
#include "raslib/odmgtypes.hh"  // for r_ULong

class OId;

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
UShortType is the base type used for 16bit unsigned integer cell
values. The value of a UShort is stored in four chars.
*/

/**
  * \ingroup Relcatalogifs
  */
class UShortType : public UIntegralType
{
public:
    explicit UShortType(const OId &id);

    UShortType();

    UShortType(const UShortType &) = default;

    UShortType &operator=(const UShortType &) = default;

    ~UShortType() override = default;

    void printCell(std::ostream &stream, const char *cell) const override;

    r_ULong *convertToCULong(const char *cell, r_ULong *value) const override;
    r_Long *convertToCLong(const char *cell, r_Long *value) const override;

    char *makeFromCULong(char *cell, const r_ULong *value) const override;
    char *makeFromCLong(char *cell, const r_Long *value) const override;

    static const char *Name;
};

#endif
