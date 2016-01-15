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
#include "config.h"
#include "mymalloc/mymalloc.h"
#include "mdddomaintype.hh"
#include "mdddimensiontype.hh"
#ifdef __APPLE__
#include <sys/malloc.h>
#else
#include <malloc.h>
#endif
#include "basetype.hh"
#include <iostream>
#include "reladminif/sqlerror.hh"
#include "reladminif/externs.h"
#include "reladminif/objectbroker.hh"
#include "dbminterval.hh"
#include <easylogging++.h>
#include <cstring>
#include <string>

r_Bytes
MDDDomainType::getMemorySize() const
{
    return MDDBaseType::getMemorySize() + sizeof(DBMinterval*) + myDomain->getMemorySize();
}

MDDDomainType::MDDDomainType(const OId& id) throw (r_Error)
    :   MDDBaseType(id),
        myDomain(0)
{
    if (objecttype == OId::MDDDOMTYPEOID)
    {
        mySubclass = MDDDOMAINTYPE;
        readFromDb();
    }
    LTRACE << "Domain\t:" << *myDomain;
}

MDDDomainType::MDDDomainType(const char* newTypeName, const BaseType* newBaseType, const r_Minterval& newDomain)
    :   MDDBaseType(newTypeName, newBaseType)
{
    objecttype = OId::MDDDOMTYPEOID;
    myDomain = new DBMinterval(newDomain);
    myDomain->setCached(true);
    mySubclass = MDDDOMAINTYPE;
}

MDDDomainType::MDDDomainType()
    :   MDDBaseType("unnamed mdddomaintype"),
        myDomain(0)
{
    objecttype = OId::MDDDOMTYPEOID;
    mySubclass = MDDDOMAINTYPE;
}

MDDDomainType::MDDDomainType(const MDDDomainType& old)
    :   MDDBaseType(old)
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

MDDDomainType&
MDDDomainType::operator=(const MDDDomainType& old)
{
    // Gracefully handle self assignment
    if (this == &old)
        return *this;
    MDDBaseType::operator=(old);
    if (myDomain)
    {
        myDomain->setPersistent(false);
        myDomain->setCached(false);
        delete myDomain;
    }
    myDomain = new DBMinterval(*old.myDomain);
    myDomain->setCached(true);
    return *this;
}

char*
MDDDomainType::getTypeStructure() const
{
    char* baseType = myBaseType->getTypeStructure();
    char* mdom = myDomain->get_string_representation();
    char* result = static_cast<char*>(mymalloc(12 + strlen(baseType) + strlen(mdom)));

    strcpy(result, "marray <");
    strcat(result, baseType);
    strcat(result, ", ");
    strcat(result, mdom);
    strcat(result, ">");
    free(mdom);
    free(baseType);
    return result;
}

char* MDDDomainType::getNewTypeStructure() const
{
    std::ostringstream ss;

    ss << "MARRAY { "
       << TypeFactory::getSyntaxTypeFromInternalType(std::string(myBaseType->getTypeName()))
       << " } , "
       << myDomain->get_string_representation();

    std::string result = ss.str();
    return strdup(result.c_str());
}

const r_Minterval*
MDDDomainType::getDomain() const
{
    return myDomain;
}

void
MDDDomainType::print_status( ostream& s ) const
{
    s << "\tr_Marray" << "<" << myBaseType->getTypeName() << ", ";
    myDomain->print_status(s);
    s << "\t>";
}

MDDDomainType::~MDDDomainType()
{
    validate();
    if (myDomain)
        delete myDomain;
    myDomain = 0;
}

int
MDDDomainType::compatibleWith(const Type* aType) const
{
    bool retval = false;
    if (aType->getType() == MDDTYPE)
    {
        MDDTypeEnum ttype = (static_cast<const MDDType*>(aType))->getSubtype();
        if (ttype == MDDDOMAINTYPE)
        {
            if (myBaseType->compatibleWith((static_cast<const MDDBaseType*>(aType))->getBaseType()))
            {
                if ((static_cast<const MDDDomainType*>(aType))->getDomain()->dimension() == myDomain->dimension())
                {
                    if (myDomain->covers(*(static_cast<const MDDDomainType*>(aType))->getDomain()))
                    {
                        retval = true;
                    }
                    else
                    {
                        LTRACE << "domain marray types have incompatible domains";
                    }
                }
                else
                {
                    LTRACE << "domain marray types have different dimensions";
                }
            }
            else
            {
                LTRACE << "basetypes are not equal";
            }
        }
        else
        {
            if (ttype == MDDDIMENSIONTYPE)
            {
                if (myBaseType->compatibleWith((static_cast<const MDDBaseType*>(aType))->getBaseType()))
                {
                    if (myDomain->dimension() == (static_cast<const MDDDimensionType*>(aType))->getDimension())
                    {
                        retval = true;
                    }
                    else
                    {
                        LTRACE << "dimension marray type has wrong dimension";
                    }
                }
                else
                {
                    LTRACE << "basetypes are not equal";
                }
            }
            else
            {
                LTRACE << "not a dimension/domain type";
            }
        }
    }
    else
    {
       LERROR << "MDDDomainType::compatibleWith() was passed a type that is not an marray type (" << aType->getName();
    }
    return retval;
}

void
MDDDomainType::setPersistent(bool t) throw (r_Error)
{
    MDDBaseType::setPersistent(t);
    myDomain->setPersistent(t);
}

