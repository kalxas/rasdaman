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
#ifndef _TILECACHE_HH_
#define _TILECACHE_HH_

#include "blobtile.hh"
#include "tilecachevalue.hh"
#include <map>
#include <list>
#include <set>

typedef int KeyType;
typedef CacheValue* ValueType;
typedef std::pair<KeyType, ValueType> CachePairType;
typedef std::list<ValueType> CacheLRU;
typedef std::map<KeyType, ValueType> CacheType;

/**
 * Manage caching of blobs.
 * 
 * @author Dimitar Misev
 */
class TileCache
{
public:
    /// cache blob with key = oid
    static void insert(KeyType key, ValueType value);
    static void insert(OId& key, ValueType value);
    
    /// retrieve cached blob given its oid; returns NULL in case of a miss
    static ValueType get(KeyType key);
    static ValueType get(OId& key);
    
    /// check if cache contains blob with given oid
    static bool contains(KeyType key);
    static bool contains(OId& key);
    
    /// remove cached blob; this triggers validateReal on the cached blob
    static ValueType remove(KeyType key);
    static ValueType remove(OId& key);
    
    /// remove all blobs, effectively emptying the cache
    static void clear();
    
    /// remove least recently used blobs from the cache, when cache size > cache limit
    static void readjustCache();
    
    /// cache size limit in bytes
    static long cacheLimit;

private:
    /// oid -> blob
    static CacheType cache;
    
    /// list for maintaining cache lru policy
    static CacheLRU lru;
    
    /// cache size
    static long cacheSize;
    
    /// methods for setting the most recently used blob to value in the LRU list
    static void updateValue(ValueType value);
    /// remove value from the LRU list
    static void removeValue(ValueType value);
    
    /// give the test class private access
//    friend class TileCacheTest;
};

#endif
