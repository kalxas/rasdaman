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
#ifndef _TILECACHEVALUE_HH_
#define _TILECACHEVALUE_HH_

#include "raslib/mddtypes.hh"
#include "reladminif/oidif.hh"
#include "debug/debug-srv.hh"

#include <set>

// enable this to turn on only cache debugging
//#define DEBUG_CACHE

#define OID_KEY(key) (key.getCounter())

class CacheValue
{
public:
    /// create new item for caching
    CacheValue(char* data, r_Bytes size, bool update, OId& newOId, long blobOid = -1, void* tile = NULL, r_Data_Format dataformat = r_Array);
    
    /// destructor
    ~CacheValue();
    
    /// return cached data
    char* getData();
    
    /// size of cached data
    r_Bytes getSize();
    
    /// oid of cached data
    OId getOId();
    
    /// blob oid (in database) of cached data
    long getBlobOid();
    
    /// data format of cached data
    r_Data_Format getDataFormat();
    
    /// get a set of the tiles that reference this cached data
    std::set<void*> getReferencingTiles();
    void setReferencingTiles(std::set<void*> newTiles);
    void addReferencingTiles(std::set<void*> newTiles);
    void addReferencingTile(void* newTile);
    void removeReferencingTile(void* tile);
    
    /// is cached data supposed to be updated in the database?
    bool isUpdate();
    
    /// is cached data supposed to be written to file storage
    bool isFileStorage();

    /// set if cached data is supposed to be updated in the database
    void setUpdate(bool update);
    
    /// set if cached data is supposed to be updated in the database
    void setFileStorage(bool fileStorage);

private:
    
    char* data;
    bool update;
    r_Bytes size;
    OId myOId;
    long blobOid;
    std::set<void*> referencingTiles;
    r_Data_Format dataFormat;
    bool fileStorage;
};

#endif
