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

#include "atomictype.hh"
#include "reladminif/oidif.hh"      // for OId
#include "raslib/odmgtypes.hh"      // for BOOLTYPE, CHAR, COMPLEXTYPE1, COMPLEX...
#include "raslib/error.hh"          // for r_Error, INTERNALDLPARSEERROR

AtomicType::AtomicType(unsigned int newSize)
    : AtomicType("unnamed atomictype", newSize)
{
}

AtomicType::AtomicType(const char *name, unsigned int newSize)
    : BaseType(name), size(newSize)
{
    objecttype = OId::ATOMICTYPEOID;
    _isPersistent = 1;
    _isInDatabase = 1;
    _isModified = 0;
}

AtomicType::AtomicType(const AtomicType &old)
    : AtomicType(old.getName(), old.size)
{
}

AtomicType::AtomicType(const OId &id)
    : BaseType(id)
{
    objecttype = OId::ATOMICTYPEOID;
    _isPersistent = 1;
    _isInDatabase = 1;
    _isModified = 0;
}

/// generate equivalent C type names
void AtomicType::generateCTypeName(std::vector<const char *> &names) const
{
    switch (myType)
    {
    case ULONG:
        names.push_back("unsigned int");
        break;
    case USHORT:
        names.push_back("unsigned short");
        break;
    case BOOLTYPE:
        names.push_back("unsigned char");
        break;
    case LONG:
        names.push_back("int");;
        break;
    case SHORT:
        names.push_back("short");
        break;
    case OCTET:
        names.push_back("unsigned char");
        break;
    case DOUBLE:
        names.push_back("double");
        break;
    case FLOAT:
        names.push_back("float");
        break;
    case CHAR:
        names.push_back("char");
        break;
    case COMPLEXTYPE1:
        names.push_back("float");
        names.push_back("float");
        break;
    case COMPLEXTYPE2:
        names.push_back("double");
        names.push_back("double");
        break;
    case CINT16:
        names.push_back("short");
        names.push_back("short");
        break;
    case CINT32:
        names.push_back("long");
        names.push_back("long");
        break;
    default:
        throw r_Error(INTERNALDLPARSEERROR);
    }
}

void AtomicType::getTypes(std::vector<const BaseType *> &types) const
{
    types.push_back(this);
    return;
}

void AtomicType::generateCTypePos(std::vector<int> &positions, int offset) const
{
    positions.push_back(offset);
}

unsigned int AtomicType::getSize() const
{
    return size;
}

