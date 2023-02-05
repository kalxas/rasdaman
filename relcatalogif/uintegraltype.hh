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
#ifndef _UINTEGRALTYPE_HH_
#define _UINTEGRALTYPE_HH_

#include "atomictype.hh"

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
UIntegralType is the abstract base class for all integral
\Ref{BaseType} subclasses, i.e. base types like \Ref{ULongType} or
\Ref{BoolType}. It provides conversions to/from long and
double. It's subclasses must implement conversions to/from unsigned
long.
*/

/**
  * \ingroup Relcatalogifs
  */
class UIntegralType : public AtomicType
{
public:
    explicit UIntegralType(unsigned int newSize) : AtomicType(newSize) {}

    UIntegralType(const UIntegralType &)  = default;

    explicit UIntegralType(const OId &id) : AtomicType(id) {}

    ~UIntegralType() override = default;

    r_Long *convertToCLong(const char *, r_Long *) const override;
    char *makeFromCLong(char *, const r_Long *) const override;

    double *convertToCDouble(const char *, r_Double *) const override;
    char *makeFromCDouble(char *, const r_Double *) const override;

protected:
    UIntegralType(const char *name, unsigned int newSize)
        : AtomicType(name, newSize) {}
};

#include "uintegraltype.icc"

#endif
