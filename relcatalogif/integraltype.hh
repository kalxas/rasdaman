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

#ifndef _INTEGRALTYPE_HH_
#define _INTEGRALTYPE_HH_

#include "atomictype.hh"

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
IntegralType is the abstract base class for all integral signed
\Ref{BaseType} subclasses, i.e. base types like \Ref{LongType} or
\Ref{ShortType}. It provides conversions to/from unsigned and
double. It's subclasses must implement conversions to/from long.
*/

/**
  * \ingroup Relcatalogifs
  */
class IntegralType : public AtomicType
{
public:
    explicit IntegralType(unsigned int newSize)
        : AtomicType(newSize) {}

    IntegralType(const IntegralType &) = default;

    explicit IntegralType(const OId &id)
        : AtomicType(id) {}

    ~IntegralType() override = default;

    r_ULong *convertToCULong(const char *, r_ULong *) const override;
    char *makeFromCULong(char *, const r_ULong *) const override;

    double *convertToCDouble(const char *, r_Double *) const override;
    char *makeFromCDouble(char *, const r_Double *) const override;

protected:
    IntegralType(const char *name, unsigned int newSize)
        : AtomicType(name, newSize) {}
};

#include "integraltype.icc"

#endif
