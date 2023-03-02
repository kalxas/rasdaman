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

#include "basetype.hh"                  // for BaseType
#include "mdddomaintype.hh"             // for MDDDomainType
#include "mdddimensiontype.hh"          // for MDDDimensionType
#include "mddbasetype.hh"               // for MDDBaseType
#include "mddtype.hh"                   // for MDDType, MDDType::MD...
#include "type.hh"                      // for Type (ptr only), ost...
#include "raslib/mddtypes.hh"           // for r_Dimension, r_Bytes
#include "raslib/minterval.hh"          // for r_Minterval
#include "relcatalogif/typefactory.hh"  // for TypeFactory
#include <logging.hh>                   // for Writer, CTRACE, LTRACE

#include <boost/algorithm/string/predicate.hpp>  // for starts_with
#include <stdio.h>                               // for sprintf
#include <cstring>                               // for strcat, strlen, strcpy
#include <iostream>                              // for operator<<, basic_os...
#include <string>                                // for basic_string, string

MDDDimensionType::MDDDimensionType(const OId &id)
    : MDDBaseType(id)
{
    if (objecttype == OId::MDDDIMTYPEOID)
    {
        mySubclass = MDDDIMENSIONTYPE;
        readFromDb();
    }
}

MDDDimensionType::MDDDimensionType(const char *newTypeName, const BaseType *newBaseType,
                                   r_Dimension newDimension)
    : MDDBaseType(newTypeName, newBaseType), myDimension(newDimension)
{
    objecttype = OId::MDDDIMTYPEOID;
    mySubclass = MDDDIMENSIONTYPE;
}

MDDDimensionType::MDDDimensionType()
    : MDDBaseType("unnamed mdddimensiontype")
{
    objecttype = OId::MDDDIMTYPEOID;
    mySubclass = MDDDIMENSIONTYPE;
}

std::string MDDDimensionType::getTypeStructure() const
{
    auto dimStr = std::to_string(myDimension);
    auto baseType = myBaseType->getTypeStructure();
    auto resultLen = 12 + baseType.size() + dimStr.size();

    std::string ret;
    ret.reserve(resultLen);
    ret += "marray <";
    ret += baseType;
    ret += ",";
    ret += dimStr;
    ret += ">";

    return ret;
}

std::string MDDDimensionType::getNewTypeStructure() const
{
    std::string baseType;
    if (boost::starts_with(myBaseType->getTypeName(), TypeFactory::ANONYMOUS_CELL_TYPE_PREFIX))
        baseType = myBaseType->getNewTypeStructure();
    else
        baseType = TypeFactory::getSyntaxTypeFromInternalType(myBaseType->getTypeName());

    std::string ret;
    ret.reserve(baseType.size() + 11 + (myDimension * 3));

    ret += baseType;
    ret += " MDARRAY [";
    for (r_Dimension i = 1; i <= myDimension; ++i)
    {
        if (i > 1)
            ret += ",";
        ret += "D";
        ret += std::to_string(i);
    }
    ret += "]";
    return ret;
}

void MDDDimensionType::print_status(std::ostream &s) const
{
    s << "\tr_Marray<" << myBaseType->getTypeName() << "\t, " << myDimension << ">";
}

r_Dimension MDDDimensionType::getDimension() const
{
    return myDimension;
}

MDDDimensionType::~MDDDimensionType() noexcept(false)
{
    validate();
}

int MDDDimensionType::compatibleWith(const Type *aType) const
{
    const auto *mddType = static_cast<const MDDType *>(aType);
    if (mddType->getSubtype() != MDDDOMAINTYPE && mddType->getSubtype() != MDDDIMENSIONTYPE)
    {
        LTRACE << "not a domain- or dimensiontype";
        return 0;
    }
    else
    {
        if (!myBaseType->compatibleWith(static_cast<const MDDBaseType *>(mddType)->getBaseType()))
        {
            LTRACE << "basetypes are not compatible";
            return 0;
        }
        else if (mddType->getSubtype() == MDDDIMENSIONTYPE)
        {
            LTRACE << "check for dimension equality";
            return (myDimension == static_cast<const MDDDimensionType *>(aType)->getDimension());
        }
        else
        {
            LTRACE << "check for dimension equality";
            return (myDimension == static_cast<const MDDDomainType *>(aType)->getDomain()->dimension());
        }
    }
}

r_Bytes MDDDimensionType::getMemorySize() const
{
    return MDDBaseType::getMemorySize() + sizeof(r_Dimension);
}
