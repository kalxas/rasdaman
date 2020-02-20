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
#include "dbminterval.hh"                        // for DBMinterval
#include "mddbasetype.hh"                        // for MDDBaseType
#include "mdddimensiontype.hh"                   // for MDDDimensionType
#include "mdddomaintype.hh"                      // for MDDDomainType
#include "mddtype.hh"                            // for MDDType::MDDDOMAINTYPE
#include "type.hh"                               // for Type, ostream
#include "relcatalogif/typefactory.hh"             // for TypeFactory, TypeFac...
#include "raslib/odmgtypes.hh"                   // for MDDTYPE
#include "raslib/error.hh"                       // for r_Error, r_Error::r_...
#include "raslib/mddtypes.hh"                    // for r_Bytes
#include "raslib/minterval.hh"                   // for operator<<, r_Minterval
#include "mymalloc/mymalloc.h"
#include <logging.hh>                            // for Writer, CTRACE, LTRACE

#include <boost/algorithm/string/predicate.hpp>  // for starts_with
#include <cstring>                               // for strcat, strlen, strcpy
#include <iostream>                              // for operator<<, basic_os...
#include <string>                                // for basic_string, string

MDDDomainType::MDDDomainType()
    : MDDBaseType("unnamed mdddomaintype")
{
    objecttype = OId::MDDDOMTYPEOID;
    mySubclass = MDDDOMAINTYPE;
}

MDDDomainType::MDDDomainType(const OId &id)
    : MDDBaseType(id)
{
    if (objecttype == OId::MDDDOMTYPEOID)
    {
        mySubclass = MDDDOMAINTYPE;
        readFromDb();
    }
}

MDDDomainType::MDDDomainType(const char *newTypeName, const BaseType *newBaseType,
                             const r_Minterval &newDomain)
    : MDDBaseType(newTypeName, newBaseType)
{
    objecttype = OId::MDDDOMTYPEOID;
    myDomain = new DBMinterval(newDomain);
    myDomain->setCached(true);
    mySubclass = MDDDOMAINTYPE;
}

MDDDomainType::MDDDomainType(const char *newTypeName, const BaseType *newBaseType,
                             const r_Minterval &newDomain, const std::vector<std::string> &axisNames2)
    : MDDBaseType(newTypeName, newBaseType)
{
    objecttype = OId::MDDDOMTYPEOID;
    myDomain = new DBMinterval(newDomain, axisNames2);
    myDomain->setCached(true);
    mySubclass = MDDDOMAINTYPE;
}

MDDDomainType &MDDDomainType::operator=(const MDDDomainType &old)
{
    // Gracefully handle self assignment
    if (this == &old)
    {
        return *this;
    }
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

MDDDomainType::~MDDDomainType() noexcept(false)
{
    validate();
    if (myDomain)
    {
        delete myDomain;
    }
    myDomain = nullptr;
}

char *MDDDomainType::getTypeStructure() const
{
    char *baseType = myBaseType->getTypeStructure();
    char *mdom = myDomain->get_string_representation();
    char *result = static_cast<char *>(mymalloc(12 + strlen(baseType) + strlen(mdom)));

    sprintf(result, "marray <%s, %s>", baseType, mdom);

    free(mdom);
    free(baseType);
    return result;
}

char *MDDDomainType::getNewTypeStructure() const
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

    ss << " MDARRAY "
       << myDomain->get_named_axis_string_representation();

    std::string result = ss.str();
    return strdup(result.c_str());
}

const r_Minterval *MDDDomainType::getDomain() const
{
    return myDomain;
}

void MDDDomainType::print_status(std::ostream &s) const
{
    s << "\tr_Marray<" << myBaseType->getTypeName() << ", ";
    myDomain->print_status(s);
    s << "\t>";
}

int MDDDomainType::compatibleWith(const Type *aType) const
{
    if (aType->getType() == MDDTYPE)
    {
        MDDTypeEnum ttype = (static_cast<const MDDType *>(aType))->getSubtype();
        if (ttype == MDDDOMAINTYPE)
        {
            if (myBaseType->compatibleWith(static_cast<const MDDBaseType *>(aType)->getBaseType()))
            {
                if (static_cast<const MDDDomainType *>(aType)->getDomain()->dimension() == myDomain->dimension())
                {
                    if (myDomain->covers(*(static_cast<const MDDDomainType *>(aType))->getDomain()))
                    {
                        return true;
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
        else if (ttype == MDDDIMENSIONTYPE)
        {
            if (myBaseType->compatibleWith(static_cast<const MDDBaseType *>(aType)->getBaseType()))
            {
                if (myDomain->dimension() == static_cast<const MDDDimensionType *>(aType)->getDimension())
                {
                    return true;
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
    else
    {
        LERROR << "cannot check compatibility of an MDD domain type and non MDD type (" << aType->getName() << ")";
    }
    return false;
}

void MDDDomainType::setPersistent(bool t)
{
    MDDBaseType::setPersistent(t);
    myDomain->setPersistent(t);
}

r_Bytes MDDDomainType::getMemorySize() const
{
    return MDDBaseType::getMemorySize() + sizeof(DBMinterval *) +
           myDomain->getMemorySize();
}
