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

#ifndef _DOUBLETYPE_HH_
#define _DOUBLETYPE_HH_

#include <iosfwd>     // for ostream

#include "realtype.hh"  // for RealType

class OId;

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
DoubleType is the base type used for 64bit floating-point cell
values. The value of a Double is stored in 8 chars.
*/

/**
  * \ingroup Relcatalogifs
  */
class DoubleType : public RealType
{
public:
    DoubleType();

    explicit DoubleType(const OId &id);

    DoubleType(const DoubleType &) = default;

    DoubleType &operator=(const DoubleType &) = default;

    ~DoubleType() override = default;

    /**
     * Prints a cell cell on stream followed by a space.
     */
    void printCell(std::ostream &stream, const char *cell) const override;

    double *convertToCDouble(const char *cell, double *value) const override;
    char *makeFromCDouble(char *cell, const double *value) const override;
    
    r_ULong *convertToCULong(const char *cell, r_ULong *value) const override;
    r_Long *convertToCLong(const char *cell, r_Long *value) const override;

    static const char *Name;

};

#endif
