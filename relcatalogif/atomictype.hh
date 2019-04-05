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

/*************************************************************
 *
 *
 * PURPOSE:
 *   The AtomicType class is the superclass for all for
 *   atomic types (e.g. Octet, ULong) describing the type of a
 *   cell
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#ifndef _ATOMICTYPE_HH_
#define _ATOMICTYPE_HH_

#include "basetype.hh"

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
AtomicType is the abstract base class for all non-structured
\Ref{BaseType} subclasses, i.e. base types like \Ref{ULongType} or
\Ref{BoolType}.
*/
/**
  * \defgroup Relcatalogifs Relcatalogif Classes
  */

/**
  * \ingroup Relcatalogifs
  */

class AtomicType : public BaseType
{
public:
    unsigned int getSize() const override;
    /*@Doc:
    get size of cells of this base type.
    */

    AtomicType(unsigned int newSize);
    /*@Doc:
    constructor.
    */

    AtomicType(const AtomicType &old);
    /*@Doc:
    copy constructor.
    */

    AtomicType(const OId &id);
    /*@Doc:
    */

    AtomicType &operator=(const AtomicType &old);
    /*@Doc:
    */

    /// generate equivalent C type names
    void generateCTypeName(std::vector<const char *> &names) const override;
    void generateCTypePos(std::vector<int> &positions, int offset = 0) const override;
    void getTypes(std::vector<const BaseType *> &types) const override;

    ~AtomicType() override;
    /*@Doc:
    */

protected:
    unsigned int size;
    /*@Doc:
    size of one cell of this base type in number of chars.
    */

    AtomicType(const char *name, unsigned int newSize);
    /*@Doc:
    */
};

#endif
