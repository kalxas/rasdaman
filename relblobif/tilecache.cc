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
#include "tilecache.hh"

#include <algorithm>

using namespace std;

CacheType TileCache::cache;
CacheLRU TileCache::lru;

long TileCache::cacheLimit = 0L;
long TileCache::cacheSize = 0L;

void TileCache::insert(KeyType key, ValueType value)
{
    if (key == 0)
    {
        // invalid key
        return;
    }
    
    TENTER("TileCache::insert( " << key << " )");
    
    CacheValue* tileToCache = value;
    if (contains(key))
    {
        CacheValue* tile = cache[key];
        if (tile == NULL)
        {
            RMInit::logOut << "Error: cached NULL value!" << endl;
            remove(key);
        }
        else
        {
            cacheSize -= tile->getSize();
            updateValue(tile);
            cache.erase(key);
            delete tile;
        }
        TTALK("already inserted");
    }
    
    cache.insert(CachePairType(key, tileToCache));
    TTALK("inserted to cache, check if contains = " << contains(key));
    updateValue(tileToCache);
    cacheSize += tileToCache->getSize();
    readjustCache();
    
    TLEAVE("TileCache::insert()");
}

ValueType TileCache::get(KeyType key)
{
    TENTER("TileCache::get( " << key << " )");
    CacheValue* ret = NULL;
    if (contains(key))
    {
        ret = cache[key];
        if (ret != NULL)
        {
            updateValue(ret);
        }
    }
    else
    {
        TTALK("key not found");
    }
    TLEAVE("TileCache::get()");
    return ret;
}

bool TileCache::contains(KeyType key)
{
    bool ret = cache.find(key) != cache.end();
    TTALK("TileCache::contains( " << key << " = " << ret << " )");
    return ret;
}

ValueType TileCache::remove(KeyType key)
{
    TENTER("TileCache::remove( " << key << " )");
    CacheValue* ret = NULL;
    if (contains(key))
    {
        ret = cache[key];
        if (ret != NULL)
        {
            cacheSize -= ret->getSize();
            removeValue(ret);
            BLOBTile::writeCachedToDb(ret);
        }
        cache.erase(key);
    }
    else
    {
        TTALK("key not found");
    }
    TLEAVE("TileCache::remove()");
    return ret;
}

void TileCache::clear()
{
    TENTER("TileCache::clear() - clear cache of size: " << cache.size());
    
    typedef CacheType::iterator it_type;
    for (it_type it = cache.begin(); it != cache.end(); it++)
    {
        TTALK("TileCache::clear() - removing key " << it->first);
        CacheValue* value = it->second;
        BLOBTile::writeCachedToDb(value);
    }
    cache.clear();
    lru.clear();
    cacheSize = 0;
    
    TLEAVE("TileCache::clear()");
}

void TileCache::readjustCache()
{
    TENTER("TileCache::readjustCache( cache size = " << cacheSize << ", cache limit = " << cacheLimit << " )");
    if (cacheSize > cacheLimit)
    {
        long count = 0;
        TTALK("freeing up space from cache...");
        while (cacheSize > cacheLimit && lru.size() > 0)
        {
            CacheValue* value = lru.back();
            lru.pop_back();
            remove(value->getOId().getCounter());
            ++count;
        }
        TTALK("removed " << count << " blobs from cache.");
    }
    TLEAVE("TileCache::readjustCache()");
}

void TileCache::updateValue(ValueType value)
{
    TENTER("TileCache::updateValue()");
    CacheLRU::iterator pos = std::find(lru.begin(), lru.end(), value);
    if (pos != lru.end())
    {
        TTALK("moving to beginning of LRU list.");
        lru.splice(lru.begin(), lru, pos);
    }
    else
    {
        TTALK("inserting at beginning of LRU list.");
        lru.insert(lru.begin(), value);
    }
    TLEAVE("TileCache::updateValue()");
}

void TileCache::removeValue(ValueType value)
{
    CacheLRU::iterator pos = std::find(lru.begin(), lru.end(), value);
    if (pos != lru.end())
    {
        lru.erase(pos);
    }
}

void TileCache::insert(OId& key, ValueType value)
{
    insert(OID_KEY(key), value);
}

ValueType TileCache::get(OId& key)
{
    return get(OID_KEY(key));
}

bool TileCache::contains(OId& key)
{
    return contains(OID_KEY(key));
}

ValueType TileCache::remove(OId& key)
{
    remove(OID_KEY(key));
}