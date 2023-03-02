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

#include "settype.hh"           // for SetType
#include "mddtype.hh"           // for MDDType
#include "raslib/odmgtypes.hh"  // for SETTYPE
#include "reladminif/oidif.hh"

#include <string.h>  // for sprintf

SetType::SetType()
    : CollectionType("unnamed settype")
{
    myType = SETTYPE;
    objecttype = OId::SETTYPEOID;
}

SetType::SetType(const OId &id)
    : CollectionType(id)
{
    objecttype = OId::SETTYPEOID;
    readFromDb();
}

SetType::SetType(const char *newTypeName, MDDType *newMDDType)
    : CollectionType(newTypeName, newMDDType)
{
    myType = SETTYPE;
    objecttype = OId::SETTYPEOID;
}

SetType::~SetType() noexcept(false)
{
    validate();
}

std::string SetType::getTypeStructure() const
{
    auto baseType = myMDDType->getTypeStructure();
    auto resultLen = 6 + baseType.size();
    std::string ret;
    ret.reserve(resultLen);
    ret += "set <";
    ret += baseType;
    ret += ">";
    return ret;
}
