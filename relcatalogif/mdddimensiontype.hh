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

#ifndef _MDDDIMENSIONTYPE_HH_
#define _MDDDIMENSIONTYPE_HH_

#include <iosfwd>
#include "raslib/mddtypes.hh"
#include "mddbasetype.hh"

class OId;

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
  The MDDBaseType class is used as a type for MDDs where
  the base type and the dimensionality is specified.
*/

/**
  * \ingroup Relcatalogifs
  */
class MDDDimensionType : public MDDBaseType
{
public:
    explicit MDDDimensionType(const OId &id);

    MDDDimensionType(const char *newTypeName, const BaseType *newBaseType,
                     r_Dimension newDimension);

    MDDDimensionType();

    MDDDimensionType(const MDDDimensionType &) = default;

    MDDDimensionType &operator=(const MDDDimensionType &) = default;

    void print_status(std::ostream &s) const override;
    /*@Doc:
    writes the state of the object to the specified stream
    */

    r_Dimension getDimension() const;
    /*@Doc:
    return dimensionality
    */

    ~MDDDimensionType() noexcept(false) override;

    int compatibleWith(const Type *aType) const override;
    /*@Doc:
    is compatible if:
        aType is MDDDimType or MDDDomType and
        the basetypes are compatible
        and dimensionality is the same
    */

    std::string getTypeStructure() const override;

    std::string getNewTypeStructure() const override;

    r_Bytes getMemorySize() const override;

protected:
    void insertInDb() override;
    void readFromDb() override;
    void deleteFromDb() override;

    r_Dimension myDimension{};
};

#endif
