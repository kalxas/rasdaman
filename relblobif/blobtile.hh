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
// -*-C++-*- (for Emacs)

/*************************************************************
 *
 *
 * PURPOSE:
 *   The Blobtile class is used to store the tiles in the database.
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#pragma once

#include "config.h"
#include "blobfs.hh"
#include "dbtile.hh"
#include "tilecache.hh"
#include "tilecachevalue.hh"
#include "raslib/mddtypes.hh"

class r_Error;
class OId;

//@ManMemo: Module: {\bf relblobif}.

/*@Doc:

BLOBTile is the persistent class for storing the contents of MDD tiles
in the database. Each instance represents the contents of a tile of a MDD Object
from the database. BLOBTiles are just arrays of unsigned characters.
In main memory they are encapsulated in the class \Ref{PersTile}.

At the moment a BLOBTile is loaded into main memory, when it is
accessed the first time. This usually happens, when the RasDaMan DBMS
accesses the contents of a \Ref{PersTile}.

{\bf Interdependencies}

BLOBTile is an interface class with the base DBMS. It is, therefore,
highly dependent on the base DBMS used.
*/
/**
  * \defgroup Relblobifs Relblobif Classes
  */

/**
  * \ingroup Relblobifs
  */

class BLOBTile : public DBTile
{
public:
    //@Man: constructors
    //@{
    BLOBTile(r_Data_Format dataformat = r_Array);
    /*@Doc:
    constructs a new empty BLOBTile and gets an id for it.
    */

    BLOBTile(const OId &BlobId);
    /*@Doc:
    constructs a BlobTile out of the database
    */

    BLOBTile(const OId &BlobId, r_Bytes newSize, r_Data_Format newFmt);
    /*@Doc:
    constructs a new BLOBTile of size newSize filled with zeros.
    the tile will think it is not modified and also not in the db but persistent.
    this is used by the rc index.
    */

    BLOBTile(r_Bytes newSize, r_Data_Format dataformat);
    /*@Doc:
    constructs a new BLOBTile of size newSize, not initialized with any value.
    */

    BLOBTile(r_Bytes newSize, char c, r_Data_Format dataformat);
    /*@Doc:
    constructs a new BLOBTile of size newSize filled with c.
    */

    BLOBTile(r_Bytes newSize, const char *newCells, r_Data_Format dataformat);
    /*@Doc:
    constructs a new BLOBTile of size newSize filled with the contents of newCells.
    The newCells are copied if takeNewCellsOwnership is false, otherwise the pointer
    newCells is directly owned by DBTile.
    */

    BLOBTile(r_Bytes newSize, bool takeOwnershipOfNewCells, char *newCells, r_Data_Format dataformat);
    /*@Doc:
    constructs a new BLOBTile of size newSize filled with the contents of newCells.
    */

    BLOBTile(r_Bytes newSize, const char *newCells, r_Data_Format dataformat, const OId &myOId);
    /*@Doc:
    constructs a new BLOBTile of size newSize filled with the contents of newCells.
    the oid will be assigned to this blob.  used by regular computed index.
    */

    //@}

    ~BLOBTile() noexcept(false) override;
    /*@Doc:
    validates the object.  deletes it cells.
    */

    static void kill(const OId &target, unsigned int range = 0);
    /*@Doc:
    delete a blobtile without loading it first into memory.
    used by the indexes.
    delete the blobtile and range consecutive tiles.
    */

    static long long getAnyTileOid();
    /*@Doc:
    return any tile oid present in ras_tiles; this is used to determine if
    the old file tile organization is used or the new nested organization.
    */

    static r_Data_Format getTileDataFormat(long long blobOid);

    static const long long NO_TILE_FOUND = -1;

protected:
    void updateInDb() override;
    /*@Doc:
    update the contents of a Tile in the db
    */

    void insertInDb() override;
    /*@Doc:
    inserts the Blob into the db.
    */

    void readFromDb() override;
    /*@Doc:
    read blob from db into blobtile
    */

    void deleteFromDb() override;
    /*@Doc:
    deletes a blob from TILES, sets size to 0 and flags to -1
    */

    static void writeCachedToDb(CacheValue *value);
    /*@Doc:
     write cached data to the database
    */

private:

    friend class TileCache;
};

