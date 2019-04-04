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
#include "tilecache.hh"

#include <cstdlib>
#include <iostream>
#include <logging.hh>

using namespace std;

CacheValue::CacheValue(char *newData, r_Bytes newSize, bool newUpdate, OId &newOId, long newBlobOid, void *tile, r_Data_Format newDataformat) :
    data(newData), update(newUpdate), size(newSize), myOId(newOId), blobOid(newBlobOid), dataFormat(newDataformat)
{
    referencingTiles.insert(tile);
}

CacheValue::~CacheValue()
{
    if (data && referencingTiles.empty())
    {
        LDEBUG << "freeing data = " << (void *)data;
        free(data);
        data = NULL;
    }
}

char *CacheValue::getData()
{
    return data;
}

r_Data_Format CacheValue::getDataFormat()
{
    return dataFormat;
}

set<void *> CacheValue::getReferencingTiles()
{
    return referencingTiles;
}

void CacheValue::setReferencingTiles(set<void *> newTiles)
{
    referencingTiles = newTiles;
}

void CacheValue::addReferencingTiles(std::set<void *> newTiles)
{
    set<void *>::iterator it;
    for (it = newTiles.begin(); it != newTiles.end(); it++)
    {
        referencingTiles.insert(*it);
    }
}

void CacheValue::addReferencingTile(void *newTile)
{
    referencingTiles.insert(newTile);
}

void CacheValue::removeReferencingTile(void *tile)
{
    referencingTiles.erase(tile);
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

bool CacheValue::isUpdate()
{
    return update;
}

bool CacheValue::isFileStorage()
{
    return fileStorage;
}

void CacheValue::setUpdate(bool newUpdate)
{
    update = newUpdate;
}

void CacheValue::setFileStorage(bool newFileStorage)
{
    fileStorage = newFileStorage;
}