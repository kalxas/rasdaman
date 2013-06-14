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
#include "version.h"
#include <cstdlib>

#define __EXECUTABLE__
#define DEBUG_MAIN

#include "applications/directql/template_inst.hh"
#include "raslib/template_inst.hh"

#include "debug/debug-clt.hh"
#include "raslib/rmdebug.hh"
#include "relblobif/blobtile.hh"
#include "relblobif/tilecache.hh"
#include "reladminif/oidif.hh"
#include "reladminif/databaseif.hh"
#include "reladminif/transactionif.hh"
#include "testing.h"

using namespace std;

// define external vars
char globalConnectId[256] = "RASBASE";
char globalDbUser[255] = {0};
char globalDbPasswd[255] = {0};

// 100x100 blob
#define BLOB_SIZE 10000
// number of blobs to use in tests
#define BLOB_NO 5
// normal cache limit
#define CACHE_LIMIT 100000
// cache limit that triggers cache readjustment
#define CACHE_LIMIT_READJUST 40000

TileCacheTest::TileCacheTest()
{
    TileCache::cacheLimit = CACHE_LIMIT;
    oids.clear();
}

void TileCacheTest::insertBlobs()
{
    int i;
    for (i = 0; i < BLOB_NO; i++)
    {
        BLOBTile* tile = createBlob();
        tile->validate();
        
        EXPECT_EQ(TileCache::cache.size(), (i+1));
        EXPECT_EQ(TileCache::cacheSize, (i+1) * BLOB_SIZE);
        EXPECT_EQ(TileCache::lru.front()->getOId(), tile->getOId());
        EXPECT_EQ(TileCache::lru.size(), (i+1));
    }
}

void TileCacheTest::insertBlobsReadjust()
{
    TileCache::cacheLimit = CACHE_LIMIT_READJUST;
    int i;
    for (i = 0; i < BLOB_NO; i++)
    {
        BLOBTile* tile = createBlob();
        tile->validate();
        
        int size = i + 1;
        if (i == BLOB_NO - 1)
        {
            size = i;
        }
        EXPECT_EQ(TileCache::cache.size(), size);
        EXPECT_EQ(TileCache::cacheSize, size * BLOB_SIZE);
        EXPECT_EQ(TileCache::lru.front()->getOId(), tile->getOId());
        EXPECT_EQ(TileCache::lru.size(), size);
    }
}

void TileCacheTest::getBlobs()
{
    int i = 0;
    vector<OId>::iterator it;
    for (it = oids.begin(); it != oids.end(); it++)
    {
        OId myOId = *it;
        BLOBTile* tile = new BLOBTile(myOId);
        
        int size = i + 1;
        if (TileCache::cacheSize == TileCache::cacheLimit)
        {
            size = TileCache::cacheLimit / BLOB_SIZE;
        }
        
        EXPECT_EQ(TileCache::cache.size(), size);
        EXPECT_EQ(TileCache::cacheSize, size * BLOB_SIZE);
        EXPECT_EQ(TileCache::lru.front()->getOId(), tile->getOId());
        EXPECT_EQ(TileCache::lru.size(), size);
        ++i;
    }
}

void TileCacheTest::removeBlobs()
{
    int i = BLOB_NO;
    vector<OId>::iterator it;
    for (it = oids.begin(); it != oids.end(); it++)
    {
        OId oid = *it;
        TileCache::remove(oid);
        
        EXPECT_EQ(TileCache::cache.size(), (i-1));
        EXPECT_EQ(TileCache::cacheSize, (i-1) * BLOB_SIZE);
        EXPECT_EQ(TileCache::lru.size(), (i-1));
        --i;
    }
}

void TileCacheTest::deleteBlobs()
{
    int i = oids.size();
    vector<OId>::iterator it;
    for (it = oids.begin(); it != oids.end(); it++)
    {
        OId myOId = *it;
        BLOBTile* tile = new BLOBTile(myOId);
        tile->setPersistent(false);
        tile->validate();
        --i;
    }
        
    EXPECT_EQ(TileCache::cache.size(), 0);
    EXPECT_EQ(TileCache::cacheSize, 0);
    EXPECT_EQ(TileCache::lru.size(), 0);
}

void TileCacheTest::clearCache()
{
    TileCache::clear();
    EXPECT_EQ(TileCache::cache.size(), 0);
    EXPECT_EQ(TileCache::cacheSize, 0);
    EXPECT_EQ(TileCache::lru.size(), 0);
    TileCache::cacheLimit = CACHE_LIMIT;
}

BLOBTile* TileCacheTest::createBlob()
{
    OId myOId;
    OId::allocateOId(myOId, OId::BLOBOID);
    oids.push_back(myOId);
    BLOBTile* ret = new BLOBTile(myOId, BLOB_SIZE, r_Array);
    ret->setModified();
    TileCache::printBlob(ret, "created tile");
    return ret;
}

int main(int argc, char **argv)
{
    RMInit::logOut.rdbuf(cout.rdbuf());
    RMInit::dbgOut.rdbuf(cout.rdbuf());
    
    TransactionIf ta;
    DatabaseIf db;
    db.open(globalConnectId);
    ta.begin(&db);
    
    TileCacheTest test;
    
    // create blobs
    RUN_TEST(test.insertBlobs());
    // and insert to database (removing from cache triggers validate)
    RUN_TEST(test.removeBlobs());
    
    // create new blobs, but this time lower cache limit
    RUN_TEST(test.insertBlobsReadjust());
    // test cache clearing
    RUN_TEST(test.clearCache());
    
    // cache empty now, retrieve blobs from database
    RUN_TEST(test.getBlobs());
    // empty cache again
    RUN_TEST(test.clearCache());
    // test blob retrieval with limited cache size
    TileCache::cacheLimit = CACHE_LIMIT_READJUST;
    RUN_TEST(test.getBlobs());
    
    // delete blobs from the database
    RUN_TEST(test.deleteBlobs());
    
    ta.commit();
    db.close();
    
    return Test::getResult();
}

