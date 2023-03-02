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
/*****************************************************************************
 *
 *
 * PURPOSE:
 *
 *
 *
 * COMMENTS:
 *   uses embedded SQL
 *
 *****************************************************************************/

#include "dbnamedobject.hh"
#include "dbobject.hh"  // for DBObject
#include <logging.hh>

#include <string.h>  // for strncpy, strlen
#include <ostream>   // for operator<<, ostream, basic_ostream, char_traits

// Beware: keep this value less or equal to STRING_MAXLEN in externs.h!
#define MAXNAMELENGTH_CONST 200
unsigned int DBNamedObject::MAXNAMELENGTH = MAXNAMELENGTH_CONST;

const char *DBNamedObject::defaultName = "unamed object\0";

void DBNamedObject::printStatus(unsigned int level, std::ostream &stream) const
{
    DBObject::printStatus(level, stream);
    stream << " Name: " << myName;
}

DBNamedObject::DBNamedObject(const OId &id)
    : DBObject(id)
{
}

DBNamedObject::DBNamedObject()
    : DBObject(), myName(defaultName)
{
}

DBNamedObject::DBNamedObject(const DBNamedObject &old)
    : DBObject(old), myName(old.getName())
{
}

DBNamedObject::DBNamedObject(const char *name)
    : DBObject(), myName(name)
{
}

DBNamedObject::DBNamedObject(const OId &id, const char *name)
    : DBObject(id), myName(name)
{
}

DBNamedObject &DBNamedObject::operator=(const DBNamedObject &old)
{
    if (this != &old)
    {
        DBObject::operator=(old);
        myName = old.getName();
    }
    return *this;
}

const char *DBNamedObject::getName() const
{
    return myName.c_str();
}

void DBNamedObject::setName(const char *newname)
{
    unsigned int len = strlen(newname);
    if (len > MAXNAMELENGTH)
    {
        len = MAXNAMELENGTH;
    }
    myName = newname;
}

void DBNamedObject::setName(const short length, const char *data)
{
    unsigned int len = 0;
    if (static_cast<unsigned int>(length) > MAXNAMELENGTH)
    {
        len = MAXNAMELENGTH;
    }
    else
    {
        len = static_cast<unsigned int>(length);
    }
    char *tmp = new char[len + 1];
    strncpy(tmp, data, len);
    *(tmp + len) = 0;
    myName = tmp;
    delete[] tmp;
}

r_Bytes DBNamedObject::getMemorySize() const
{
    return myName.size() + DBObject::getMemorySize() + sizeof(unsigned short);
}
