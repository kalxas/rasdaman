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

#include "basetype.hh"                           // for BaseType
#include "mdddomaintype.hh"                      // for MDDDomainType
#include "mdddimensiontype.hh"                   // for MDDDimensionType
#include "mddbasetype.hh"                        // for MDDBaseType
#include "mddtype.hh"                            // for MDDType, MDDType::MD...
#include "type.hh"                               // for Type (ptr only), ost...
#include "raslib/mddtypes.hh"                    // for r_Dimension, r_Bytes
#include "raslib/minterval.hh"                   // for r_Minterval
#include "mymalloc/mymalloc.h"
#include "catalogmgr/typefactory.hh"             // for TypeFactory
#include <logging.hh>                            // for Writer, CTRACE, LTRACE

#include <boost/algorithm/string/predicate.hpp>  // for starts_with
#include <stdio.h>                               // for sprintf
#include <stdlib.h>                              // for free, malloc
#include <cstring>                               // for strcat, strlen, strcpy
#include <iostream>                              // for operator<<, basic_os...
#include <string>                                // for basic_string, string
#ifdef __APPLE__
#include <sys/malloc.h>
#else
#include <malloc.h>
#endif

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

char *MDDDimensionType::getTypeStructure() const
{
    auto dimStr = std::to_string(myDimension);
    char *baseType = myBaseType->getTypeStructure();
    char *result = static_cast<char *>(mymalloc(12 + strlen(baseType) + dimStr.size()));

    sprintf(result, "marray <%s, %s>", baseType, dimStr.c_str());
    
    free(baseType);
    return result;
}

char *MDDDimensionType::getNewTypeStructure() const
{
    std::ostringstream ss;
    if (boost::starts_with(myBaseType->getTypeName(), TypeFactory::ANONYMOUS_CELL_TYPE_PREFIX))
    {
        char *typeStructure = myBaseType->getNewTypeStructure();
        ss << typeStructure;
        free(typeStructure);
    }
    else
    {
        ss << TypeFactory::getSyntaxTypeFromInternalType(std::string(myBaseType->getTypeName()));
    }

    ss << " MDARRAY [";
    for (r_Dimension i = 0; i < myDimension; ++i)
    {
        if (i > 0)
            ss << ",";
        ss << "a" << i;
    }
    ss << "]";

    std::string result = ss.str();
    return strdup(result.c_str());
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

