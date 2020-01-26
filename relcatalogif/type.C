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
/*************************************************************
 *
 *
 * PURPOSE:
 *   uses ODMG-conformant O2 classes
 *
 *
 * COMMENTS:
 *   none
 *
 ************************************************************/

#include "type.hh"
#include "mymalloc/mymalloc.h"
#include "reladminif/oidif.hh"  // for OId
#include "raslib/error.hh"      // for r_Error, INTERNALDLPARSEERROR
#include "logging.hh"           // for LERROR

#include <ctype.h>              // for tolower
#include <cstring>              // for strlen, strcpy

Type::Type() : DBNamedObject("unnamed type")
{
}

Type::Type(const OId &id) : DBNamedObject(id)
{
}

Type::Type(const char *name) : DBNamedObject(name)
{
}

void Type::generateCTypeName(std::vector<const char *> &) const
{
    LERROR << "no equivalent type found for C";
    throw r_Error(INTERNALDLPARSEERROR);
}

void Type::generateCTypePos(std::vector<int> &, int ) const
{
    LERROR << "no equivalent type found for C";
    throw r_Error(INTERNALDLPARSEERROR);
}

void Type::getTypes(std::vector<const BaseType *> &) const
{
    LERROR << "no type information was found";
    throw r_Error(INTERNALDLPARSEERROR);
}

void Type::destroy()
{
    // does nothing to prevent types from being deleted because of reference counts
}

const char *Type::getTypeName() const
{
    return getName();
}

char *Type::getTypeStructure() const
{
    // default implementation for all non-structured base types.
    char *result = strdup(getTypeName());
    for (size_t i = 0; i < strlen(result); ++i)
        result[i] = static_cast<char>(tolower(result[i]));

    return result;
}

char *Type::getNewTypeStructure() const
{
    return getTypeStructure();
}

TypeEnum Type::getType() const
{
    return myType;
}

int Type::compatibleWith(const Type * /* aType */) const
{
  return 0;
}

bool Type::operator==(const Type &o) const
{
  return myType == o.myType;
}
