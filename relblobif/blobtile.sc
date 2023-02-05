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
/*************************************************************
 *
 * PURPOSE:
 * implements blobtile interface on SQLite / filesystem.
 *
 ************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <set>

#include "blobtile.hh"
#include "blobfs.hh"
#include "inlinetile.hh"
#include "reladminif/dbref.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/sqlitewrapper.hh"
#include "mymalloc/mymalloc.h"

#include <logging.hh>
#include <fmt/core.h>

using namespace std;

void BLOBTile::updateInDb()
{
    long long blobOid = myOId.getCounter();
    LDEBUG << "updating tile with id " << blobOid << ", size " << size;

    SQLiteQuery checkQuery(fmt::format("SELECT BlobId FROM RAS_TILES WHERE BlobId = {}", blobOid));
    if (!checkQuery.nextRow())
    {
        LERROR << "no tile with id " << blobOid << " found.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "tile not found in database");
    }
    checkQuery.finalize();

    SQLiteQuery::execute(fmt::format(
          "UPDATE RAS_TILES SET DataFormat = {} WHERE BlobId = {}",
          dataFormat, blobOid));

    BlobData blob(blobOid, size, cells);
    BlobFS::getInstance().update(blob);

    DBObject::updateInDb();
}

void BLOBTile::insertInDb()
{
    long long blobOid = myOId.getCounter();
    SQLiteQuery checkQuery(fmt::format("SELECT BlobId FROM RAS_TILES WHERE BlobId = {}", blobOid));
    if (checkQuery.nextRow())
    {
        LERROR << "tile with id " << blobOid << " already exists.";
        throw r_Ebase_dbms(SQLITE_ERROR,
                           "a tile with the same id already exists in the "
                           "database; see the server log for more details.");
    }
    checkQuery.finalize();

    SQLiteQuery::execute(fmt::format(
        "INSERT INTO RAS_TILES ( BlobId, DataFormat ) VALUES  ( {}, {} )",
        blobOid, dataFormat));

    BlobData blob(blobOid, size, cells);
    BlobFS::getInstance().insert(blob);

    DBObject::insertInDb();
}

void BLOBTile::deleteFromDb()
{
    long long blobOid = myOId.getCounter();
    LDEBUG << "deleting tile with id " << blobOid;

    SQLiteQuery checkQuery(fmt::format("SELECT BlobId FROM RAS_TILES WHERE BlobId = {}", blobOid));
    if (!checkQuery.nextRow())
    {
        LERROR << "no tile with id " << blobOid << " found.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "tile not found in database");
    }
    checkQuery.finalize();

    SQLiteQuery::execute(fmt::format("DELETE FROM RAS_TILES WHERE BlobId = {}", blobOid));
    BlobData blob(blobOid);
    BlobFS::getInstance().remove(blob);
    DBObject::deleteFromDb();
}

// delete a range of tuple(s) from ras_tiles table, update map ref;
// tuples are identified by target and a range

void BLOBTile::kill(const OId &target, unsigned int range)
{
    if (range == 0) // single tuple
    {
        DBObject *targetobj = ObjectBroker::isInMemory(target);
        if (targetobj)
        {
            targetobj->setPersistent(false);
        }

        long long blobOid = target.getCounter();
        LDEBUG << "deleting (kill) tile with id " << blobOid;

        SQLiteQuery checkQuery(fmt::format("SELECT BlobId FROM RAS_TILES WHERE BlobId = {}", blobOid));
        if (!checkQuery.nextRow())
        {
            // This is not an error case and is ignored, as kill is often repeatedly
            // called with the same blob id.
            return;
        }
        checkQuery.finalize();

        SQLiteQuery::execute(fmt::format("DELETE FROM RAS_TILES WHERE BlobId = {}", blobOid));
        BlobData blob(blobOid);
        BlobFS::getInstance().remove(blob);
    }
    else
    {
        DBObjectPMap &mapRef = ObjectBroker::getMap(target.getType());
        DBObjectPMap::iterator it = mapRef.begin();
        DBObjectPMap::iterator theEnd = mapRef.end();
        OId end(target.getCounter() + range, target.getType());
        while (it != theEnd)
        {
            if (target <= (const OId &)(*it).first && (*it).first <= (const OId &) end)
            {
                (*it).second->setPersistent(false);
            }
        }

        long long blobOid = target.getCounter();
        long long blobOidEnd = end.getCounter();
        LDEBUG << "deleting (kill) tiles with ids " << blobOid << " - " << blobOidEnd;

        SQLiteQuery query(fmt::format("SELECT BlobId FROM RAS_TILES WHERE {} <= BlobId AND BlobId <= {}",
                                      blobOid, blobOidEnd));
        while (query.nextRow())
        {
            blobOid = query.nextColumnLong();
            BlobData blob(blobOid);
            BlobFS::getInstance().remove(blob);
        }
        query.finalize();
        SQLiteQuery::execute(fmt::format(
            "DELETE FROM RAS_TILES WHERE {} <= BlobId AND BlobId <= {}",
            blobOid, blobOidEnd));
    }
}

long long BLOBTile::getAnyTileOid()
{
    long long ret = NO_TILE_FOUND;

    std::string q = "SELECT name FROM sqlite_master WHERE type='table' AND name='RAS_TILES'";
    SQLiteQuery checkTable(std::move(q));
    if (checkTable.nextRow())
    {
        q = "SELECT BlobId FROM RAS_TILES LIMIT 1";
        SQLiteQuery checkQuery(std::move(q));
        if (checkQuery.nextRow())
        {
            ret = checkQuery.nextColumnLong();
        }
    }
    return ret;
}

// read tuple from ras_tiles, identified by blobtile.cc var myOId
// allocates necessary mem into ptr 'cells' and fills it; must be freed elsewhere
// external var 'size' is set to the number of bytes read

void BLOBTile::readFromDb()
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif

    long long blobOid = myOId.getCounter();
    LDEBUG << "reading tile with id " << blobOid;

    dataFormat = BLOBTile::getTileDataFormat(blobOid);
    currentFormat = dataFormat;

    BlobData blob(blobOid, size, cells);
    BlobFS::getInstance().select(blob);
    size = blob.size;
    cells = blob.data;

//#ifdef DEBUG
//    LTRACE << "tile contents:";
//    for (int a = 0; a < size; a++)
//    {
//        LTRACE << " " << hex << (int)(cells[a]);
//    }
//    LTRACE << dec;
//#endif

    DBObject::readFromDb();

#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif
}

r_Data_Format BLOBTile::getTileDataFormat(long long blobOid)
{
    SQLiteQuery query(fmt::format("SELECT DataFormat FROM RAS_TILES WHERE BlobId = {}", blobOid));
    if (query.nextRow())
    {
        return static_cast<r_Data_Format>(query.nextColumnInt());
    }
    else
    {
        LERROR << "no tile with id " << blobOid << " found.";
        throw r_Error(r_Error::r_Error_ObjectUnknown, "tile id " + std::to_string(blobOid));
    }
}
