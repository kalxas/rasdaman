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

#include "mddtype.hh"                 // for MDDType, MDDType::MDDONLYTYPE
#include "type.hh"                    // for Type, ostream
#include "reladminif/dbnamedobject.hh"// for DBNamedObject
#include "reladminif/oidif.hh"        // for OId, OId::MDDTYPEOID
#include "raslib/odmgtypes.hh"        // for MDDTYPE
#include "raslib/mddtypes.hh"         // for r_Bytes
#include "raslib/minterval.hh"        // for operator<<, r_Minterval

#include <logging.hh>                 // for Writer, CTRACE, LTRACE
#include <string.h>                   // for strcpy, strdup
#include <iostream>                   // for operator<<, basic_ostream, char...
#include <string>                     // for string

MDDType::MDDType(const OId &id) : Type(id)
{
    if (objecttype == OId::MDDTYPEOID)
    {
        readFromDb();
        mySubclass = MDDONLYTYPE;
    }
    myType = MDDTYPE;
}

MDDType::MDDType() : Type("unnamed mddtype")
{
    myType = MDDTYPE;
    mySubclass = MDDONLYTYPE;
    objecttype = OId::MDDTYPEOID;
}

MDDType::MDDType(const char *newTypeName) : Type(newTypeName)
{
    myType = MDDTYPE;
    mySubclass = MDDONLYTYPE;
    objecttype = OId::MDDTYPEOID;
}

MDDType::~MDDType() noexcept(false)
{
    validate();
}

std::string MDDType::getTypeStructure() const
{
    return "marray <>";
}

std::string MDDType::getNewTypeStructure() const
{
  return "marray <>";
}

void MDDType::print_status(std::ostream &s) const
{
    s << "\tr_Marray<>";
}

int MDDType::compatibleWith(const Type *aType) const
{
    return aType->getType() == MDDTYPE;
}

int MDDType::compatibleWithDomain(__attribute__((unused))
                                  const r_Minterval *aDomain) const
{
    return 1;
}

MDDType::MDDTypeEnum MDDType::getSubtype() const
{
    return mySubclass;
}

r_Bytes MDDType::getMemorySize() const
{
    return sizeof(MDDType::MDDTypeEnum) + DBNamedObject::getMemorySize();
}
