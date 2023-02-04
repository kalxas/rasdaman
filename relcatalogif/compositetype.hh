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

#ifndef _COMPOSITETYPE_HH_
#define _COMPOSITETYPE_HH_

#include "basetype.hh"

class OId;

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
CompositeType is the abstract base class for all structured
\Ref{BaseType} subclasses, at the moment only \Ref{StructType}.
*/

/**
  * \ingroup Relcatalogifs
  */
class CompositeType : public BaseType
{
public:
    CompositeType();

    explicit CompositeType(unsigned int newSize);

    explicit CompositeType(const OId &id);

    CompositeType(const CompositeType &) = default;

    CompositeType &operator=(const CompositeType &) = default;

    ~CompositeType() override = default;

    unsigned int getSize() const override;
    /*@Doc:
    get size of cells of this base type.
    */

protected:
    unsigned int size{};
    /*@Doc:
    size of one cell of this base type in number of chars.
    */

    CompositeType(const char *name, unsigned int newSize);
};

#endif
