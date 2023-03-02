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

#include "mddobjix.hh"                   // for MDDObjIx
#include "hierindexds.hh"                // for HierIndexDS
#include "keyobject.hh"                  // for KeyObject, operator<<
#include "indexds.hh"                    // for IndexDS
#include "sdirindexlogic.hh"             // for SDirIndexLogic
#include "srcindexlogic.hh"              // for SRCIndexLogic
#include "srptindexlogic.hh"             // for SRPTIndexLogic
#include "transdirix.hh"                 // for TransDirIx
#include "reladminif/lists.h"            // for KeyObjectVector
#include "reladminif/dbobject.hh"        // for DBObjectId, DBObject
#include "relindexif/dbrcindexds.hh"     // for DBRCIndexDS
#include "relindexif/dbtcindex.hh"       // for DBTCIndex
#include "relindexif/hierindex.hh"       // for DBHierIndex
#include "relblobif/tileid.hh"           // for DBTileId
#include "tilemgr/tile.hh"               // for Tile
#include "storagemgr/sstoragelayout.hh"  // for StorageLayout
#include "raslib/error.hh"               // for r_Error, ILLEGAL_INDEX_TYPE
#include "raslib/mddtypes.hh"            // for r_Auto_Index, r_Directory_Index
#include "raslib/minterval.hh"           // for r_Minterval, operator<<
#include "raslib/point.hh"               // for r_Point
#include <logging.hh>

#include <iostream>  // for operator<<, ostream, basic_ost...
#include <memory>    // for allocator_traits<>::value_type
#include <vector>    // for vector, vector<>::iterator
#include <cassert>

using std::shared_ptr;
using std::vector;

MDDObjIx::MDDObjIx(const StorageLayout &sl, const r_Minterval &dim)
    : actualIx(new TransDirIx(dim.dimension())), myStorageLayout(sl)
{
    lastAccessTiles.reserve(10);
    initializeLogicStructure();
}

MDDObjIx::MDDObjIx(const StorageLayout &sl, const r_Minterval &dim,
                   const BaseType *bt, bool persistent)
    : cellBaseType(bt), _isPersistent(persistent), myStorageLayout(sl)
{
    lastAccessTiles.reserve(10);
    initializeLogicStructure();
    if (isPersistent())
    {
        switch (myStorageLayout.getIndexType())
        {
        case r_Auto_Index:
        case r_RPlus_Tree_Index:
            actualIx = new DBHierIndex(dim.dimension(), false, true);
            break;
        case r_Reg_Computed_Index:
            actualIx = new DBRCIndexDS(dim, SRCIndexLogic::computeNumberOfTiles(myStorageLayout, dim));
            break;
        case r_Tile_Container_Index:
            actualIx = new DBTCIndex(dim.dimension(), false);
            break;
        case r_Directory_Index:
            actualIx = new DBHierIndex(dim.dimension(), false, true);
            break;
        default:
            LERROR << "unknown index: " << myStorageLayout.getIndexType();
            throw r_Error(UNKNOWN_INDEX_TYPE);
        }
    }
    else
    {
        // only dirindex supports transient indexes
        actualIx = new TransDirIx(dim.dimension());
    }
#ifdef RMANBENCHMARK
    initializeTimerPointers();
#endif
}

MDDObjIx::MDDObjIx(DBObjectId newDBIx, const StorageLayout &sl, const BaseType *bt)
    : cellBaseType(bt), actualIx(newDBIx), _isPersistent(true), myStorageLayout(sl)
{
    initializeLogicStructure();
    lastAccessTiles.reserve(10);
#ifdef RMANBENCHMARK
    initializeTimerPointers();
#endif
}

MDDObjIx::~MDDObjIx()
{
    releasePersTiles();
    if (actualIx)
    {
        actualIx->destroy();
        actualIx = nullptr;
    }

#ifdef RMANBENCHMARK
    pointQueryTimer->setOutput(0);
    if (pointQueryTimer) delete pointQueryTimer;
    intersectTimer->setOutput(0);
    if (intersectTimer) delete intersectTimer;
    getTilesTimer->setOutput(0);
    if (getTilesTimer) delete getTilesTimer;
#endif
}

void MDDObjIx::printStatus(unsigned int level, std::ostream &stream) const
{
    stream << "MDDObjIx [ last access interval = " << lastAccess
           << " tile cache size = " << lastAccessTiles.size() << " index structure = ";
    actualIx->printStatus(level, stream);
}

void MDDObjIx::insertTile(shared_ptr<Tile> newTile)
{
    if (isPersistent())
        newTile->setPersistent();

    KeyObject t(newTile);
    do_insertObj(actualIx, t, myStorageLayout);
    setNewLastAccess(newTile, false);
}

bool MDDObjIx::removeTile(shared_ptr<Tile> tileToRemove)
{
    // removes from cache, if it's there
    removeTileFromLastAccesses(tileToRemove);
    // removes from the index itself
    KeyObject t(tileToRemove);
    return do_removeObj(actualIx, t, myStorageLayout);
}

vector<shared_ptr<Tile>> *MDDObjIx::intersect(const r_Minterval &searchInter) const
{
#ifdef RMANBENCHMARK
    if (RManBenchmark >= 3) intersectTimer->start();
#endif
    LDEBUG << "getting all tiles from index intersecting domain " << searchInter;
    vector<shared_ptr<Tile>> *result = lastAccessIntersect(searchInter);
    if (!result)
    {
        KeyObjectVector resultKeys;
        do_intersect(actualIx, searchInter, resultKeys, myStorageLayout);
        LDEBUG << "index intersection done, found " << resultKeys.size() << " items.";
        result = new vector<shared_ptr<Tile>>();
        if (!resultKeys.empty())
        {
            auto resSize = resultKeys.size();
            result->reserve(resSize);
            if (isPersistent())
            {
// this checks if there are double tiles in the result
#ifdef DEBUG
                DomainMap t;
                DomainMap::iterator it;
                for (size_t i = 0; i < resSize; i++)
                {
                    DomainPair p(resultKeys[i].getObject().getOId(), resultKeys[i].getDomain());
                    if ((it = t.find(p.first)) != t.end())
                    {
                        LTRACE << "intersect(" << searchInter
                               << ") received double tile: " << resultKeys[i];
                        for (unsigned int j = 0; j < resultKeys.size(); j++)
                            LTRACE << resultKeys[j];
                        throw r_Error(TILE_MULTIPLE_TIMES_RETRIEVED);
                    }
                    t.insert(p);
                }
#endif
                for (size_t i = 0; i < resSize; i++)
                {
                    LDEBUG << "found persistent entry in index with domain " << resultKeys[i].getDomain();
                    result->push_back(shared_ptr<Tile>(
                        new Tile(resultKeys[i].getDomain(), cellBaseType, DBTileId(resultKeys[i].getObject()))));
                }
            }
            else
            {
                for (size_t i = 0; i < resSize; i++)
                {
                    if (resultKeys[i].getTransObject() == nullptr)
                    {
                        auto resultDomain = resultKeys[i].getDomain();
                        auto newResult = std::make_shared<Tile>(
                            resultDomain, cellBaseType, DBTileId(resultKeys[i].getObject()));
                        result->push_back(newResult);
                    }
                    else
                    {
                        LDEBUG << "found non-persistent trans object entry in index with domain " << resultKeys[i].getDomain();
                        result->push_back(resultKeys[i].getTransObject());
                    }
                }
            }
            (const_cast<MDDObjIx *>(this))->setNewLastAccess(searchInter, result);
        }
    }
    LDEBUG << "got all intersecting tiles.";
#ifdef RMANBENCHMARK
    if (RManBenchmark >= 3) intersectTimer->stop();
#endif
    return result;
}

const char *MDDObjIx::pointQuery(const r_Point &searchPoint) const
{
    const char *result = nullptr;
    auto resultTile = containPointQuery(searchPoint);
    if (resultTile)
        result = resultTile->getCell(searchPoint);
    return result;
}

shared_ptr<Tile> MDDObjIx::containPointQuery(const r_Point &searchPoint) const
{
#ifdef RMANBENCHMARK
    if (RManBenchmark >= 4) pointQueryTimer->start();
#endif

    auto resultTile = lastAccessPointQuery(searchPoint);
    if (!resultTile)
    {
        KeyObject resultKey;
        do_pointQuery(actualIx, searchPoint, resultKey, myStorageLayout);
        if (resultKey.isInitialised())
        {
            if (isPersistent() || resultKey.getTransObject() == nullptr)
            {
                resultTile.reset(new Tile(resultKey.getDomain(), cellBaseType,
                                          DBTileId(resultKey.getObject())));
                // for rcindex
                (const_cast<DBObject *>(resultKey.getObject().ptr()))->setCached(false);
            }
            else
            {
                resultTile = resultKey.getTransObject();
            }
            (const_cast<MDDObjIx *>(this))->setNewLastAccess(resultTile);
        }
    }

#ifdef RMANBENCHMARK
    if (RManBenchmark >= 4) pointQueryTimer->stop();
#endif
    return resultTile;
}

vector<shared_ptr<Tile>> *MDDObjIx::getTiles() const
{
#ifdef RMANBENCHMARK
    if (RManBenchmark >= 3) getTilesTimer->start();
#endif
    vector<shared_ptr<Tile>> *result = nullptr;
    KeyObjectVector resultKeys;
    do_getObjs(actualIx, resultKeys, myStorageLayout);
    if (!resultKeys.empty())
    {
        result = new vector<shared_ptr<Tile>>();
        unsigned int resSize = resultKeys.size();
        result->reserve(resSize);
        if (isPersistent())
        {
// this checks if there are double tiles in the result
#ifdef DEBUG
            DomainMap tmap;
            DomainMap::iterator it;
            for (unsigned int cnt = 0; cnt < resSize; cnt++)
            {
                DomainPair p(resultKeys[cnt].getObject().getOId(),
                             resultKeys[cnt].getDomain());
                if ((it = tmap.find(p.first)) != tmap.end())
                {
                    LTRACE << "getTiles() received double tile: " << resultKeys[cnt];
                    for (unsigned int j = 0; j < resultKeys.size(); j++)
                    {
                        LTRACE << resultKeys[j];
                    }
                    throw r_Error(TILE_MULTIPLE_TIMES_RETRIEVED);
                }
                tmap.insert(p);
            }
#endif
            for (unsigned int i = 0; i < resSize; i++)
            {
                result->push_back(
                    shared_ptr<Tile>(new Tile(resultKeys[i].getDomain(), cellBaseType,
                                              DBTileId(resultKeys[i].getObject()))));
            }
        }
        else
        {
            for (unsigned int i = 0; i < resultKeys.size(); i++)
            {
                if (resultKeys[i].getTransObject() == nullptr)
                    result->push_back(
                        shared_ptr<Tile>(new Tile(resultKeys[i].getDomain(), cellBaseType,
                                                  DBTileId(resultKeys[i].getObject()))));
                else
                    result->push_back(resultKeys[i].getTransObject());
            }
        }
        r_Minterval emptyInterval;
        (const_cast<MDDObjIx *>(this))->setNewLastAccess(emptyInterval, result);
    }
#ifdef RMANBENCHMARK
    if (RManBenchmark >= 3) getTilesTimer->stop();
#endif
    return result;
}

void MDDObjIx::releasePersTiles()
{
    if (isPersistent())
    {
        lastAccessTiles.clear();
    }
}

DBObjectId MDDObjIx::getDBMDDObjIxId() const
{
    return actualIx;
}

r_Minterval MDDObjIx::getCurrentDomain() const
{
    assert(actualIx);
    return actualIx->getCoveredDomain();
}

r_Dimension MDDObjIx::getDimension() const
{
    assert(actualIx);
    return actualIx->getDimension();
}

bool MDDObjIx::isPersistent() const
{
    return _isPersistent;
}

void MDDObjIx::setNewLastAccess(const r_Minterval &newLastAccess,
                                const std::vector<shared_ptr<Tile>> *newLastTiles)
{
    lastAccess = newLastAccess;
    releasePersTiles();
    lastAccessTiles = *newLastTiles;
}

void MDDObjIx::setNewLastAccess(shared_ptr<Tile> newLastTile, bool clear)
{
    if (clear)
    {
        releasePersTiles();
        lastAccessTiles.erase(lastAccessTiles.begin(), lastAccessTiles.end());
    }
    if (newLastTile)
    {
        r_Minterval region = newLastTile->getDomain();
        lastAccess = region;
        lastAccessTiles.push_back(newLastTile);
    }
}

std::vector<shared_ptr<Tile>> *MDDObjIx::lastAccessIntersect(const r_Minterval &searchInter) const
{
    std::vector<shared_ptr<Tile>> *interResult = nullptr;
    if (lastAccess.dimension() > 0 && lastAccess.covers(searchInter))
    {
        LTRACE << "Search in the cache ";
        interResult = new std::vector<shared_ptr<Tile>>();
        interResult->reserve(10);
        for (unsigned int i = 0; i < lastAccessTiles.size(); i++)
        {
            if (lastAccessTiles[i]->getDomain().intersects_with(searchInter))
                interResult->push_back(lastAccessTiles[i]);
        }
        if (interResult->size() == 0)
        {
            delete interResult;
            interResult = nullptr;
        }
    }
    return interResult;
}

shared_ptr<Tile> MDDObjIx::lastAccessPointQuery(const r_Point &searchPoint) const
{
    shared_ptr<Tile> result;

    if ((lastAccess.dimension() != 0) && (lastAccess.covers(searchPoint)))
    {
        for (unsigned int i = 0; !result && i < lastAccessTiles.size(); i++)
        {
            if (lastAccessTiles[i]->getDomain().covers(searchPoint))
            {
                result = lastAccessTiles[i];
            }
        }
    }
    return result;
}

bool MDDObjIx::removeTileFromLastAccesses(shared_ptr<Tile> tileToRemove)
{
    bool found = false;
    std::vector<shared_ptr<Tile>>::iterator iter;
    for (iter = lastAccessTiles.begin(); iter != lastAccessTiles.end(); iter++)
    {
        if (*iter == tileToRemove)
        {
            found = true;
            lastAccessTiles.erase(iter);
            break;
        }
    }
    if (found)
    {
        lastAccess = r_Minterval();
    }
    return found;
}

void MDDObjIx::initializeLogicStructure()
{
    switch (myStorageLayout.getIndexType())
    {
    case r_Auto_Index:
    case r_RPlus_Tree_Index:
    case r_Tile_Container_Index:
        do_getObjs = SRPTIndexLogic::getObjects;
        do_insertObj = SRPTIndexLogic::insertObject;
        do_pointQuery = SRPTIndexLogic::containPointQuery;
        do_removeObj = SRPTIndexLogic::removeObject;
        do_intersect = SRPTIndexLogic::intersect;
        break;
    case r_Reg_Computed_Index:
        do_getObjs = SRCIndexLogic::getObjects;
        do_insertObj = SRCIndexLogic::insertObject;
        do_pointQuery = SRCIndexLogic::containPointQuery;
        do_removeObj = SRCIndexLogic::removeObject;
        do_intersect = SRCIndexLogic::intersect;
        break;
    case r_Directory_Index:
        // chosen before this
        do_getObjs = SDirIndexLogic::getObjects;
        do_insertObj = SDirIndexLogic::insertObject;
        do_pointQuery = SDirIndexLogic::containPointQuery;
        do_removeObj = SDirIndexLogic::removeObject;
        do_intersect = SDirIndexLogic::intersect;
        break;
    default:
        LERROR << "unknown index: " << myStorageLayout.getIndexType();
        throw r_Error(ILLEGAL_INDEX_TYPE);
    }
}

#ifdef RMANBENCHMARK
void MDDObjIx::initializeTimerPointers()
{
    pointQueryTimer = new RMTimer("DirIx", "pointQuery");
    intersectTimer = new RMTimer("DirIx", "intersect");
    getTilesTimer = new RMTimer("DirIx", "getTiles");
}
#endif
