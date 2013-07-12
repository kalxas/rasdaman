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
#include "tilecachevalue.hh"

#include <cstdlib>
#include <iostream>

CacheValue::CacheValue(char* newData, r_Bytes newSize, bool newInsert, bool newUpdate, OId& newOId, long newBlobOid, r_Data_Format newDataformat) :
data(newData), insert(newInsert), update(newUpdate), size(newSize), myOId(newOId), blobOid(newBlobOid), dataFormat(newDataformat)
{
    TENTER("CacheValue::CacheValue( ptr = " << (void*)this << ", data = " << (void*)newData << ", size = " << newSize << ", oid = " << newOId.getCounter() << " )");
    TLEAVE("CacheValue::CacheValue()");
}

CacheValue::~CacheValue()
{
    TENTER("CacheValue::~CacheValue( ptr = " << (void*)this << " )");
    if (data)
    {
        TTALK("freeing data = " << (void*)data);
        free(data);
        data = NULL;
    }
    TLEAVE("CacheValue::~CacheValue()");
}

char* CacheValue::getData()
{
    return data;
}

r_Data_Format CacheValue::getDataFormat()
{
    return dataFormat;
}

long CacheValue::getBlobOid()
{
    return blobOid;
}

r_Bytes CacheValue::getSize()
{
    return size;
}

OId CacheValue::getOId()
{
    return myOId;
}

bool CacheValue::isInsert()
{
    return insert;
}

bool CacheValue::isUpdate()
{
    return update;
}

void CacheValue::setInsert(bool newInsert)
{
    insert = newInsert;
}

void CacheValue::setUpdate(bool newUpdate)
{
    update = newUpdate;
}