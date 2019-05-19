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
#include "mymalloc/mymalloc.h"
#include <logging.hh>

#include <stdlib.h>     // for free, malloc
#include <string.h>     // for strncpy, strlen
#include <ostream>      // for operator<<, ostream, basic_ostream, char_traits

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
    : DBObject(id), myName(nullptr), myNameSize(0)
{
    LTRACE << "DBNamedObject(" << myOId << ")";
}

DBNamedObject::DBNamedObject() : DBObject(), myName(nullptr), myNameSize(0)
{
    LTRACE << "DBNamedObject()";
    setName(defaultName);
}

DBNamedObject::DBNamedObject(const DBNamedObject &old)
    : DBObject(old), myName(nullptr), myNameSize(0)
{
    LTRACE << "DBNamedObject(const DBNamedObject& old)";
    setName(old.getName());
}

DBNamedObject::DBNamedObject(const char *name)
    : DBObject(), myName(nullptr), myNameSize(0)
{
    LTRACE << "DBNamedObject(" << name << ")";
    setName(name);
}

DBNamedObject::DBNamedObject(const OId &id, const char *name)
    : DBObject(id), myName(nullptr), myNameSize(0)
{
    LTRACE << "DBNamedObject(" << myOId << ", " << name << ")";
    setName(name);
}

DBNamedObject::~DBNamedObject() noexcept(false)
{
    if (myName)
    {
        free(myName);
        myName = nullptr;
    }
    myNameSize = 0;
}

DBNamedObject &DBNamedObject::operator=(const DBNamedObject &old)
{
    LTRACE << "operator=(" << old.getName() << ") " << myName;
    if (this != &old)
    {
        DBObject::operator=(old);
        setName(old.getName());
    }
    return *this;
}

const char *DBNamedObject::getName() const
{
    return myName;
}

void DBNamedObject::setName(const char *newname)
{
    if (myName)
    {
        LTRACE << "myName\t:" << myName;
        free(myName);
        myName = nullptr;
    }
    unsigned int len = strlen(newname);
    if (len > MAXNAMELENGTH)
    {
        len = MAXNAMELENGTH;
    }
    myName = static_cast<char *>(mymalloc((len + 1) * sizeof(char)));
    myNameSize = (len + 1) * sizeof(char);
    strncpy(myName, newname, len);
    *(myName + len) = 0;
}

void DBNamedObject::setName(const short length, const char *data)
{
    if (myName)
    {
        LTRACE << "myName\t:" << myName;
        free(myName);
        myName = nullptr;
    }
    unsigned int len = 0;
    if (static_cast<unsigned int>(length) > MAXNAMELENGTH)
    {
        len = MAXNAMELENGTH;
    }
    else
    {
        len = static_cast<unsigned int>(length);
    }
    myName = static_cast<char *>(malloc((len + 1) * sizeof(char)));
    myNameSize = (len + 1) * sizeof(char);
    strncpy(myName, data, len);
    *(myName + len) = 0;
    LTRACE << "myName\t:" << myName;
}

r_Bytes DBNamedObject::getMemorySize() const
{
    return sizeof(char) * myNameSize + DBObject::getMemorySize() + sizeof(unsigned short);
}

