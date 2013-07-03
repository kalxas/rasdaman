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
#include "raslib/rmdebug.hh"
#include "debug/debug-srv.hh"

#include <algorithm>

//#define DEBUG_CACHE

#define OID_KEY(key) (key.getCounter())

#ifdef DEBUG_CACHE
    #define TENTER(msg) RMInit::logOut << "ENTER " << msg << endl;
#else
    #define TENTER(msg) ENTER(msg);
#endif

#ifdef DEBUG_CACHE
    #define TLEAVE(msg) RMInit::logOut << "LEAVE " << msg << endl;
#else
    #define TLEAVE(msg) LEAVE(msg);
#endif

#ifdef DEBUG_CACHE
    #define TTALK(msg) RMInit::logOut << "  " << msg << endl;
#else
    #define TTALK(msg) TALK(msg);
#endif

CacheType TileCache::cache;
CacheLRU TileCache::lru;

long TileCache::cacheLimit = 0L;
long TileCache::cacheSize = 0L;

bool TileCache::insert(KeyType key, ValueType value)
{
    if (key == 0)
    {
        // invalid key
        return false;
    }
    
    TENTER("TileCache::insert( " << key << " )");
    
    bool ret = false;
    BLOBTile* tileToCache = value;
    printBlob(tileToCache, "tile to cache");
    if (!contains(key))
    {
        BLOBTile* cached = tileToCache->clone();
        printBlob(cached, "cloned tile to be cached");
        cache.insert(CachePairType(key, cached));
        updateValue(cached);
        cacheSize += cached->getSize();
        readjustCache();
        ret = true;
    }
    else
    {
        BLOBTile* tile = cache[key];
        if (tile == NULL)
        {
            RMInit::logOut << "Error: cached NULL value!" << endl;
            remove(key);
        }
        else if (tile == tileToCache)
        {
            BLOBTile* cloned = tileToCache->clone();
            update(key, cloned, false);
        }
        else
        {
            tile->from(tileToCache);
            printBlob(tile, "cached tile");
            updateValue(tile);
        }
        TTALK("already inserted");
    }
    TLEAVE("TileCache::insert()");
    return ret;
}

bool TileCache::update(KeyType key, ValueType value, bool deleteTile)
{
    if (key == 0)
    {
        return false;
    }
    TENTER("TileCache::update( " << key << " )");
    
    bool ret = false;
    BLOBTile* tileToCache = value;
    printBlob(tileToCache, "tile to cache");
    if (!contains(key))
    {
        insert(key, value);
    }
    else
    {
        BLOBTile* tile = cache[key];
        if (tile == NULL)
        {
            RMInit::logOut << "Error: cached NULL value!" << endl;
            remove(key);
        }
        else
        {
            cache.erase(key);
            removeValue(tile);
            cacheSize -= tile->getSize();
            printBlob(tile, "removed tile");
            tile->_isModified = false;
            if (deleteTile)
            {
                delete tile;
            }
            
            cache.insert(CachePairType(key, tileToCache));
            updateValue(tileToCache);
            cacheSize += tileToCache->getSize();
            readjustCache();
        }
        TTALK("already inserted");
    }
    TLEAVE("TileCache::update()");
    return ret;
}

ValueType TileCache::get(KeyType key)
{
    TENTER("TileCache::get( " << key << " )");
    BLOBTile* ret = NULL;
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
    return cache.find(key) != cache.end();
}

void TileCache::remove(KeyType key)
{
    TENTER("TileCache::remove( " << key << " )");
    if (contains(key))
    {
        BLOBTile* tile = cache[key];
        if (tile != NULL)
        {
            printBlob(tile, "tile to remove from cache");
            cacheSize -= tile->getSize();
            removeValue(tile);
            tile->validateReal();
            tile->destroyReal();
            delete tile;
        }
        cache.erase(key);
    }
    else
    {
        TTALK("key not found");
    }
    TLEAVE("TileCache::remove()");
}

void TileCache::removeKey(KeyType key)
{
    TENTER("TileCache::remove( " << key << " )");
    if (contains(key))
    {
        BLOBTile* tile = cache[key];
        if (tile != NULL)
        {
            printBlob(tile, "tile to remove from cache");
            cacheSize -= tile->getSize();
            removeValue(tile);
        }
        cache.erase(key);
    }
    else
    {
        TTALK("key not found");
    }
    TLEAVE("TileCache::remove()");
}

void TileCache::clear()
{
    TENTER("TileCache::clear() - clear cache of size: " << cache.size());
    
    typedef CacheType::iterator it_type;
    for (it_type it = cache.begin(); it != cache.end(); it++)
    {
        TTALK("TileCache::clear() - removing key " << it->first);
        BLOBTile* tile = it->second;
        tile->validateReal();
        tile->destroyReal();
        delete tile;
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
        clear();
//        long count = 0;
//        TTALK("freeing up space from cache...");
//        while (cacheSize > cacheLimit && lru.size() > 0)
//        {
//            BLOBTile* tile = lru.back();
//            lru.pop_back();
//            remove(tile->myOId);
//            ++count;
//        }
//        TTALK("removed " << count << " blobs from cache.");
    }
    TLEAVE("TileCache::readjustCache()");  
}

void TileCache::updateValue(ValueType value)
{
    CacheLRU::iterator pos = std::find(lru.begin(), lru.end(), value);
    if (pos != lru.end())
    {
        lru.splice(lru.begin(), lru, pos);
    }
    else
    {
        lru.insert(lru.begin(), value);
    }
}

void TileCache::removeValue(ValueType value)
{
    CacheLRU::iterator pos = std::find(lru.begin(), lru.end(), value);
    if (pos != lru.end())
    {
        lru.erase(pos);
    }
}

bool TileCache::insert(OId& key, ValueType value)
{
    return insert(OID_KEY(key), value);
}

bool TileCache::update(OId& key, ValueType value)
{
    return update(OID_KEY(key), value);
}

ValueType TileCache::get(OId& key)
{
    return get(OID_KEY(key));
}

bool TileCache::contains(OId& key)
{
    return contains(OID_KEY(key));
}

void TileCache::remove(OId& key)
{
    remove(OID_KEY(key));
}

void TileCache::removeKey(OId& key)
{
    removeKey(OID_KEY(key));
}

void
TileCache::printBlob(BLOBTile* tile, char *msg)
{
#ifdef DEBUG_CACHE
    unsigned long int addr = (unsigned long int) tile;
    
    RMInit::logOut << " -> " << msg << endl;
    RMInit::logOut << "    ptr        " << addr << endl;
    RMInit::logOut << "    oid        " << tile->myOId << endl;
    RMInit::logOut << "    size       " << tile->size << endl;
    RMInit::logOut << "    modified   " << tile->_isModified << endl;
    RMInit::logOut << "    cached     " << tile->_isCached << endl;
    RMInit::logOut << "    in db      " << tile->_isInDatabase << endl;
    RMInit::logOut << "    persistent " << tile->_isPersistent << endl;
    RMInit::logOut << "" << endl;
#endif
}
