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

// This is -*- C++ -*-
/*************************************************************
 *
 *
 * PURPOSE:
 *
 *
 * COMMENTS:
 *   uses embedded SQL
 *
 * PURPOSE
 *  has common code for all database interface implementations
 */

#include "config.h"
#include "mymalloc/mymalloc.h"

#include "blobtile.hh"               // for BLOBTile
#include "dbtile.hh"                 // for DBTile
#include "tileid.hh"
#include "inlinetile.hh"
#include "raslib/error.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/objectbroker.hh"
#include <logging.hh>

#include <stdlib.h>                  // for malloc
#include <cstring>                   // for memset

// defined in rasserver.cc
extern char globalConnectId[256];

const long long BLOBTile::NO_TILE_FOUND;

BLOBTile::BLOBTile(r_Data_Format dataformat) : DBTile(dataformat)
{
    LTRACE << "BLOBTile(" << dataformat << ")";
    objecttype = OId::BLOBOID;
}

/*************************************************************
 * Method name...: BLOBTile(r_Bytes newSize, char c)
 *
 * Arguments.....:
 *   newSize: size in number of chars
 *   c:       value for all cells
 * Return value..: none
 * Description...: creates a new BLOBTile containing with all
 *                 cells set to c.
 ************************************************************/

BLOBTile::BLOBTile(r_Bytes newSize, char c, r_Data_Format dataformat)
    : DBTile(newSize, c, dataformat)
{
    LTRACE << "BLOBTile(" << newSize << ", char " << (int)c << ", " << dataformat << ")";
    objecttype = OId::BLOBOID;
}

BLOBTile::BLOBTile(r_Bytes newSize, r_Data_Format dataformat)
    : DBTile(newSize, dataformat)
{
    LTRACE << "BLOBTile(" << newSize << ", " << dataformat << ")";
    objecttype = OId::BLOBOID;
}

/*************************************************************
 * Method name...: BLOBTile(r_Bytes newSize,
 *                           char* newCells)
 *
 * Arguments.....:
 *   newSize:  size in number of chars
 *   newCells: char array with the new cells (must
 *             have at least newSize elements)
 * Return value..: none
 * Description...: creates a new BLOBTile. The elements of
 *                 the char array are copied!
 ************************************************************/

BLOBTile::BLOBTile(r_Bytes newSize, const char *newCells, r_Data_Format dataformat)
    : DBTile(newSize, newCells, dataformat)
{
    LTRACE << "BLOBTile(" << size << ", data, " << dataformat << ")";
    objecttype = OId::BLOBOID;
}

BLOBTile::BLOBTile(r_Bytes newSize, bool takeOwnershipOfNewCells,
                   char *newCells, r_Data_Format dataformat)
    : DBTile(newSize, takeOwnershipOfNewCells, newCells, dataformat)
{
    LTRACE << "BLOBTile(" << size << ", data, " << dataformat
           << ", takeOwnershipOfNewCells: " << takeOwnershipOfNewCells << ")";
    objecttype = OId::BLOBOID;
}

BLOBTile::BLOBTile(r_Bytes newSize, const char *newCells,
                   r_Data_Format dataformat, const OId &id)
    : DBTile(newSize, newCells, dataformat)
{
    LTRACE << "BLOBTile(" << size << ", data, " << dataformat << ", " << id << ")";
    objecttype = OId::BLOBOID;
    myOId = id;
    // copied from DBObject::setPersistent()
    // if we don't do this the blob will get a new oid
    _isPersistent = true;
    _isModified = true;
    ObjectBroker::registerDBObject(this);
}

BLOBTile::BLOBTile(const OId &id) : DBTile(id)
{
    readFromDb();
}

BLOBTile::BLOBTile(const OId &id, r_Bytes newSize, r_Data_Format newFmt)
    : DBTile(id)
{
    _isInDatabase = false;
    _isPersistent = true;
    _isModified = false;
    _isCached = false;
    dataFormat = newFmt;
    currentFormat = r_Array;
    size = newSize;
    LTRACE << "BLOBTile(oid " << id << ", " << size << ", format " << newFmt << ")";
    cells = static_cast<char *>(mymalloc(size * sizeof(char)));

    // memset seems unnecessary here
//    memset(cells, 0, size);

    ObjectBroker::registerDBObject(this);
}

BLOBTile::~BLOBTile() noexcept(false)
{
    validate();
}

