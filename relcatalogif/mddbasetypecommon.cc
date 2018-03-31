#include "mymalloc/mymalloc.h"
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
/*************************************************************
 *
 *
 * PURPOSE:
 *  code common to all database interface implementations
 *
 * CHANGE HISTORY (append further entries):
 */

#include "config.h"
#ifdef __APPLE__
#include <sys/malloc.h>
#else
#include <malloc.h>
#endif
#include "mddbasetype.hh"
#include "mdddomaintype.hh"
#include "basetype.hh"
#include <iostream>
#include "reladminif/sqlerror.hh"
#include "reladminif/externs.h"
#include "reladminif/objectbroker.hh"
#include <logging.hh>
#include <cstring>

r_Bytes
MDDBaseType::getMemorySize() const
{
    return MDDType::getMemorySize() + myBaseType->getMemorySize() + sizeof(BaseType*);
}

MDDBaseType::MDDBaseType(const OId& id) throw (r_Error)
    :   MDDType(id)
{
    if (objecttype == OId::MDDBASETYPEOID)
    {
        mySubclass = MDDBASETYPE;
        readFromDb();
    }
}

MDDBaseType::MDDBaseType(const char* newTypeName, const BaseType* newBaseType)
    :   MDDType(newTypeName)
{
    objecttype = OId::MDDBASETYPEOID;
    myBaseType = newBaseType;
    mySubclass = MDDBASETYPE;
}

MDDBaseType::MDDBaseType()
    :   MDDType("unnamed mddbasetype")
{
    objecttype = OId::MDDBASETYPEOID;
    myBaseType = 0;
    mySubclass = MDDBASETYPE;
}

MDDBaseType::MDDBaseType(const char* tname)
    :   MDDType(tname)
{
    objecttype = OId::MDDBASETYPEOID;
    myBaseType = 0;
    mySubclass = MDDBASETYPE;
}

MDDBaseType::MDDBaseType(const MDDBaseType& old)
    :   MDDType(old)
{
    myBaseType = old.myBaseType;
}

MDDBaseType& MDDBaseType::operator=(const MDDBaseType& old)
{
    // Gracefully handle self assignment
    if (this == &old)
    {
        return *this;
    }
    MDDType::operator=(old);
    myBaseType = old.myBaseType;
    return *this;
}

char*
MDDBaseType::getTypeStructure() const
{
    char* baseType = myBaseType->getTypeStructure();
    char* result = static_cast<char*>(mymalloc(10 + strlen(baseType)));

    strcpy(result, "marray <");
    strcat(result, baseType);
    strcat(result, ">");

    free(baseType);
    return result;
}

char*
MDDBaseType::getNewTypeStructure() const
{
    const char* baseType = TypeFactory::getSyntaxTypeFromInternalType(std::string(myBaseType->getTypeName())).c_str();
    char* result = static_cast<char*>(mymalloc(10 + strlen(baseType)));

    strcpy(result, "marray {");
    strcat(result, baseType);
    strcat(result, "}");

    return result;
}

void
MDDBaseType::print_status(ostream& s) const
{
    s << "\tr_Marray" << "<" << myBaseType->getTypeName() << "\t>";
}

const BaseType*
MDDBaseType::getBaseType() const
{
    return myBaseType;
}

MDDBaseType::~MDDBaseType()
{
    validate();
}

int
MDDBaseType::compatibleWith(const Type* aType) const
{
    int retval;
    if ((static_cast<MDDType*>(const_cast<Type*>(aType)))->getSubtype() != MDDBASETYPE
            && (static_cast<MDDType*>(const_cast<Type*>(aType)))->getSubtype() != MDDDOMAINTYPE
            && (static_cast<MDDType*>(const_cast<Type*>(aType)))->getSubtype() != MDDDIMENSIONTYPE)
    {
        LTRACE << "not mddbasetype or subclass";
        retval = 0;
    }
    else
    {
        // myBaseType has to be specified
        if (myBaseType->compatibleWith((static_cast<MDDBaseType*>(const_cast<Type*>(aType)))->getBaseType()))
        {
            retval = 1;
        }
        else
        {
            LTRACE << "basetypes not compatible";
            retval = 0;
        }
    }
    return retval;
}

int
MDDBaseType::compatibleWithDomain(const r_Minterval* aDomain) const
{
    int retval;

    // create an MDDDomainType with aDomain and myBaseType
    MDDDomainType tempType("tempType", myBaseType, *aDomain);
    // use normal compatibleWith
    retval = this->compatibleWith(&tempType);
    return retval;
}

