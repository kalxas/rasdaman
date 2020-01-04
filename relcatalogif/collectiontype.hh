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

#ifndef _COLLECTIONTYPE_HH_
#define _COLLECTIONTYPE_HH_

#include <iosfwd>                             // for ostream

#include "raslib/mddtypes.hh"                 // for r_Bytes
#include "type.hh"                            // for Type, ostream
#include "dbnullvalues.hh"


class OId;
class MDDType;

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
  CollectionType is the base class for classes that deal with
  collections of MDDs (the only subclass at the moment is SetType).
*/

/**
  * \ingroup Relcatalogifs
  */
class CollectionType : public Type
{
public:
    CollectionType();

    CollectionType(const MDDType *newMDDType);

    CollectionType(const OId &id);

    CollectionType(const CollectionType &);

    CollectionType &operator=(const CollectionType &) = default;

    ~CollectionType() override = default;

    int compatibleWith(const Type *aType) const override;

    r_Bytes getMemorySize() const override;

    DBNullvalues *getNullValues() const;
    /*@Doc:
    return null values associated with this type
    */

    void setNullValues(const r_Nullvalues &newNullValues);
    /*@Doc:
    associate null values with this type
    */

    const MDDType *getMDDType() const;

    void print_status(std::ostream &s) const;

protected:
    const MDDType *myMDDType{NULL};
    
    DBNullvalues *nullValues{NULL};

    CollectionType(const char *name);

    CollectionType(const char *name, const MDDType *newMDDType);
};

#endif

