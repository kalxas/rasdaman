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

#include "raslib/property.hh"
#include "raslib/basetype.hh"
#include "raslib/error.hh"

#include <logging.hh>

r_Property::r_Property(const char *newTypeName, const r_Base_Type &newType)
    : r_Meta_Object(newTypeName),
      myType(static_cast<r_Base_Type *>(newType.clone()))
{
}

r_Property::r_Property(const r_Property &oldObj)
    : r_Meta_Object(oldObj)
{
    if (oldObj.myType)
    {
        myType = static_cast<r_Base_Type *>(oldObj.myType->clone());
    }
    else
    {
        LERROR << "property does not have a base type";
        throw r_Error(PROPERTYTYPEHASNOELEMENTTYPE);
    }
}

const r_Property &
r_Property::operator=(const r_Property &oldObj)
{
    // Gracefully handle self assignment
    if (this != &oldObj)
    {
        r_Meta_Object::operator=(oldObj);
        if (myType)
        {
            delete myType;
            myType = NULL;
        }

        if (oldObj.myType)
        {
            myType = static_cast<r_Base_Type *>(oldObj.myType->clone());
        }
        else
        {
            LERROR << "r_Property::operator=(oldObj) property does not have a base type";
            throw r_Error(PROPERTYTYPEHASNOELEMENTTYPE);
        }
    }

    return *this;
}

r_Property::~r_Property()
{
    if (myType)
    {
        delete myType;
        myType = NULL;
    }
}

const r_Base_Type &
r_Property::type_of() const
{
    if (!myType)
    {
        LERROR << "property does not have a base type";
        throw r_Error(PROPERTYTYPEHASNOELEMENTTYPE);
    }
    return *myType;
}
