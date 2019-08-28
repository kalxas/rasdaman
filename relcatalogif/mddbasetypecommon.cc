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
// This is -*- C++ -*-

#include "basetype.hh"             // for BaseType
#include "mddbasetype.hh"          // for MDDBaseType
#include "mdddomaintype.hh"        // for MDDDomainType
#include "mddtype.hh"              // for MDDType, MDDType::MDDBASETYPE, MDD...
#include "raslib/mddtypes.hh"      // for r_Bytes
#include "type.hh"                 // for Type (ptr only), ostream
#include "catalogmgr/typefactory.hh" // for TypeFactory
#include "mymalloc/mymalloc.h"

#include <logging.hh>              // for Writer, CTRACE, LTRACE
#include <stdlib.h>                // for malloc, free
#include <cstring>                 // for strcat, strcpy, strlen
#include <iostream>                // for operator<<, basic_ostream, char_tr...
#include <string>                  // for string

MDDBaseType::MDDBaseType()
    : MDDType("unnamed mddbasetype")
{
    objecttype = OId::MDDBASETYPEOID;
    myBaseType = nullptr;
    mySubclass = MDDBASETYPE;
}

MDDBaseType::MDDBaseType(const OId &id)
    : MDDType(id)
{
    if (objecttype == OId::MDDBASETYPEOID)
    {
        mySubclass = MDDBASETYPE;
        readFromDb();
    }
}

MDDBaseType::MDDBaseType(const char *newTypeName, const BaseType *newBaseType)
    : MDDType(newTypeName)
{
    objecttype = OId::MDDBASETYPEOID;
    myBaseType = newBaseType;
    mySubclass = MDDBASETYPE;
}

MDDBaseType::MDDBaseType(const char *tname)
    : MDDType(tname)
{
    objecttype = OId::MDDBASETYPEOID;
    myBaseType = nullptr;
    mySubclass = MDDBASETYPE;
}

MDDBaseType::~MDDBaseType() noexcept(false)
{
    validate();
}

char *MDDBaseType::getTypeStructure() const
{
    char *baseType = myBaseType->getTypeStructure();
    char *result = static_cast<char *>(mymalloc(10 + strlen(baseType)));

    strcpy(result, "marray <");
    strcat(result, baseType);
    strcat(result, ">");
    
    free(baseType);
    return result;
}

char *MDDBaseType::getNewTypeStructure() const
{
    auto baseTypeStr = TypeFactory::getSyntaxTypeFromInternalType(std::string(myBaseType->getTypeName()));
    const char *baseType = baseTypeStr.c_str();
    char *result = static_cast<char *>(mymalloc(10 + strlen(baseType)));

    sprintf(result, "marray {%s}", baseType);
    return result;
}

void MDDBaseType::print_status(std::ostream &s) const
{
    s << "\tr_Marray<" << myBaseType->getTypeName() << "\t>";
}

const BaseType *MDDBaseType::getBaseType() const
{
    return myBaseType;
}

int MDDBaseType::compatibleWith(const Type *aType) const
{
    if (static_cast<const MDDType *>(aType)->getSubtype() != MDDBASETYPE &&
        static_cast<const MDDType *>(aType)->getSubtype() != MDDDOMAINTYPE &&
        static_cast<const MDDType *>(aType)->getSubtype() != MDDDIMENSIONTYPE)
    {
        // not mddbasetype or subclass
        return 0;
    }
    else
    {
        // myBaseType has to be specified
        return myBaseType->compatibleWith(static_cast<const MDDBaseType *>(aType)->getBaseType());
    }
}

int MDDBaseType::compatibleWithDomain(const r_Minterval *aDomain) const
{
    // create an MDDDomainType with aDomain and myBaseType
    MDDDomainType tempType("tempType", myBaseType, *aDomain);
    // use normal compatibleWith
    return this->compatibleWith(&tempType);
}

r_Bytes MDDBaseType::getMemorySize() const
{
    return MDDType::getMemorySize() + myBaseType->getMemorySize() + sizeof(BaseType *);
}
