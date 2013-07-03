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
#include "mymalloc/mymalloc.h"

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

#include <unistd.h>
#include <stdio.h>
#include <vector>
#include <iostream>
#include <cstring>

#include "blobtile.hh"

#include "raslib/error.hh"
#include "reladminif/externs.h"
#include "raslib/rmdebug.hh"
#include "debug/debug-srv.hh"
#include "reladminif/adminif.hh"
#include "reladminif/sqlerror.hh"
#include "tileid.hh"
#include "inlinetile.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/dbref.hh"

// defined in rasserver.cc
extern char globalConnectId[256];

BLOBTile::BLOBTile(r_Data_Format dataformat)
    :   DBTile(dataformat)
{
    RMDBGONCE(3, RMDebug::module_blobif, "BLOBTile", "BLOBTile(" << dataformat << ")");
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
    :   DBTile(newSize, c, dataformat)
{
    RMDBGONCE(3, RMDebug::module_blobif, "BLOBTile", "BLOBTile(" << newSize << ", data, " << dataformat << ")");
    objecttype = OId::BLOBOID;
}


/*************************************************************
 * Method name...:   BLOBTile(r_Bytes newSize,
 *                             int patSize, char* pat);
 *
 * Arguments.....:
 *   newSize: size in number of chars
 *   patSize: number of chars in pattern
 *   pat:     char array with the pattern
 * Return value..: none
 * Description...: creates a new BLOBTile containing the
 *                 repeated pattern pat. newSize shoud be
 *                 a multiply of patSize, otherwise the
 *                 cells are filled up with 0.
 ************************************************************/

BLOBTile::BLOBTile(r_Bytes newSize, r_Bytes patSize, const char* pat, r_Data_Format dataformat)
    :   DBTile(newSize, patSize, pat, dataformat)
{
    RMDBGONCE(3, RMDebug::module_blobif, "BLOBTile", "BLOBTile(" << newSize << ", " << patSize << ", pattern, " << dataformat << ")");
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

BLOBTile::BLOBTile(r_Bytes newSize, const char* newCells, r_Data_Format dataformat)
    :   DBTile(newSize, newCells, dataformat)
{
    RMDBGONCE(3, RMDebug::module_blobif, "BLOBTile", "BLOBTile(" << size << ", data, " << dataformat << ")");
    objecttype = OId::BLOBOID;
}

BLOBTile::BLOBTile(r_Bytes newSize, const char* newCells, r_Data_Format dataformat, const OId& id)
    :   DBTile(newSize, newCells, dataformat)
{
    RMDBGONCE(3, RMDebug::module_blobif, "BLOBTile", "BLOBTile(" << size << ", data, " << dataformat << ", " << id << ")");
    objecttype = OId::BLOBOID;
    myOId = id;
    // copied from DBObject::setPersistent()
    // if we don't do this the blob will get a new oid
    _isPersistent = true;
    _isModified = true;
    ObjectBroker::registerDBObject(this);
}

BLOBTile::BLOBTile(const OId& id) throw (r_Error)
    :   DBTile(id)
{
    RMDBGENTER(3, RMDebug::module_blobif, "BLOBTile", "BLOBTile(" << id <<")");
    readFromDb();
    RMDBGEXIT(3, RMDebug::module_blobif, "BLOBTile", "BLOBTile(" << id << ")");
}

BLOBTile::BLOBTile(const OId& id, r_Bytes newSize, r_Data_Format newFmt)
    :   DBTile(id)
{
    _isInDatabase = false;
    _isPersistent = true;
    _isModified = false;
    _isCached = false;
    dataFormat = newFmt;
    currentFormat = r_Array;
    size = newSize;
    cells = (char*)mymalloc(size * sizeof(char));
    memset(cells, 0, size);
    ObjectBroker::registerDBObject(this);
}

BLOBTile::~BLOBTile()
{
    RMDBGENTER(3, RMDebug::module_blobif, "BLOBTile", "~BLOBTile() " << myOId);
    if (TileCache::cacheLimit == 0)
    {
        validateReal();
        destroyReal();
    }
    else
    {
        validate();
    }
    RMDBGEXIT(3, RMDebug::module_blobif, "BLOBTile", "~BLOBTile() " << myOId << ")");
}

char*   BLOBTile::BLOBBuffer = NULL;
r_Bytes BLOBTile::BLOBBufferLength = 131072;

void
BLOBTile::destroyReal() throw (r_Error)
{
    RMDBGENTER(3, RMDebug::module_blobif, "BLOBTile", "destroyReal() " << myOId);
    if (cells)
    {
        TALK("BLOBTile::destroyReal() freeing blob cells");
        free(cells);
        cells = NULL;
    }
    RMDBGEXIT(3, RMDebug::module_blobif, "BLOBTile", "destroyReal() " << myOId << ")");
}

BLOBTile*
BLOBTile::clone()
{
    BLOBTile* ret = new BLOBTile(dataFormat);
    ret->_isCached = _isCached;
    ret->_isInDatabase = _isInDatabase;
    ret->_isModified = _isModified;
    ret->_isPersistent = _isPersistent;
    ret->cells = cells;
    ret->currentFormat = currentFormat;
    ret->myOId = myOId;
    ret->referenceCount = referenceCount;
    ret->size = size;
    return ret;
}

void
BLOBTile::from(BLOBTile* tile)
{
    _isCached = tile->_isCached;
    _isInDatabase = tile->_isInDatabase;
    _isModified = tile->_isModified;
    _isPersistent = tile->_isPersistent;
    cells = tile->cells;
    currentFormat = tile->currentFormat;
    myOId = tile->myOId;
    referenceCount = tile->referenceCount;
    size = tile->size;
}


//writes the object to database/deletes it or updates it.
//a r_Error::r_Error_TransactionReadOnly is thrown when the transaction is readonly.
void
BLOBTile::validateReal() throw (r_Error)
{
    RMDBGENTER(9, RMDebug::module_adminif, "DBObject", "validate() " << myOId);
    ENTER("BLOBTile::validateReal()");
    if (_isModified)
    {
        if (!AdminIf::isReadOnlyTA())
        {
            if (!AdminIf::isAborted())
            {
                if (_isInDatabase)
                {
                    if (_isPersistent)
                    {
                        RMDBGMIDDLE(11, RMDebug::module_adminif, "DBObject", "is persistent and modified and in database");
#ifdef RMANBENCHMARK
                        updateTimer.resume();
#endif
                        this->updateInDbReal();
#ifdef RMANBENCHMARK
                        updateTimer.pause();
#endif
                    }
                    else
                    {
                        RMDBGMIDDLE(11, RMDebug::module_adminif, "DBObject", "is not persistent and in database");
#ifdef RMANBENCHMARK
                        deleteTimer.resume();
#endif
                        this->deleteFromDbReal();
                        this->destroyReal();
#ifdef RMANBENCHMARK
                        deleteTimer.pause();
#endif
                    }
                }
                else
                {
                    if (_isPersistent)
                    {
                        RMDBGMIDDLE(11, RMDebug::module_adminif, "DBObject", "is persistent and modified and not in database");

#ifdef RMANBENCHMARK
                        insertTimer.resume();
#endif
                        this->insertInDbReal();
#ifdef RMANBENCHMARK
                        insertTimer.pause();
#endif
                    }
                    else
                    {
                        //do not do anything: not in db and not persistent
                    }
                }
            }
            else
            {
                //do not do anything: is aborted
            }
        }
        else
        {
            //do not do anything: is read only
        }
    }
    else
    {
        //do not do anything: not modified
    }
    LEAVE("BLOBTile::validateReal()");
    RMDBGEXIT(9, RMDebug::module_adminif, "DBObject", "validate() " << myOId);
}

// -----------------------------------------------------------------------------


// delete one tuple from ras_tiles table, update map ref
// tuple is identified by blobtile.cc var myOId
void
BLOBTile::deleteFromDb() throw (r_Error)
{
    RMDBGENTER(3, RMDebug::module_blobif, "BLOBTile", "deleteFromDb() " << myOId);
    ENTER( "BLOBTile::deleteFromDb" );
    TileCache::removeKey(myOId);
    deleteFromDbReal();
    LEAVE( "BLOBTile::deleteFromDb, myOId=" << myOId );
    RMDBGEXIT(3, RMDebug::module_blobif, "BLOBTile", "deleteFromDb() " << myOId);
}

// delete a range of tuple(s) from ras_tiles table, update map ref
// tuples are identified by target and a range
void
BLOBTile::kill(const OId& target, unsigned int range)
{
    RMDBGENTER(0, RMDebug::module_blobif, "BLOBTile", "kill(" << target << ", " << range <<")");
    ENTER( "BLOBTile::kill, target=" << target << ", range=" << range );
    TileCache::removeKey(target);
    killReal(target, range);
    LEAVE( "BLOBTile::kill" );
    RMDBGEXIT(0, RMDebug::module_blobif, "BLOBTile", "kill(" << target << " " << target.getType() << ")");
}

// insert new blob into ras_tiles table, update map ref
// tuple is identified by blobtile.cc var myOId
// data is taken from buffer 'cells' containing 'size' bytes
void
BLOBTile::insertInDb() throw (r_Error)
{
    RMDBGENTER(3, RMDebug::module_blobif, "BLOBTile", "insertInDb() " << myOId);
    ENTER( "BLOBTile::insertInDb" );
    if (TileCache::cacheLimit > 0)
    {
        TileCache::insert(myOId, this);
    }
    else
    {
        insertInDbReal();
    }
    LEAVE( "BLOBTile::insertInDb(), myOId=" << myOId );
    RMDBGEXIT(3, RMDebug::module_blobif, "BLOBTile", "insertInDb() " << myOId);
} // insertInDb()

// update blob in ras_tiles table, identified by variable myOId (from blobtile.cc), update map ref
void
BLOBTile::updateInDb() throw (r_Error)
{
    RMDBGENTER(3, RMDebug::module_blobif, "BLOBTile", "updateInDb() " << myOId);
    ENTER( "BLOBTile::updateInDb" );
    
    if (TileCache::cacheLimit > 0)
    {
        TileCache::insert(myOId, this);
    }
    else
    {
        updateInDbReal();
    }
    LEAVE( "BLOBTile::updateInDb(), myOId=" << myOId );
    RMDBGEXIT(3, RMDebug::module_blobif, "BLOBTile", "updateInDb() " << myOId);
} // insertInDb()

// read with cache consideration
void
BLOBTile::readFromDb() throw (r_Error)
{
    RMDBGENTER(3, RMDebug::module_blobif, "BLOBTile", "readFromDb() " << myOId);
    ENTER( "BLOBTile::readFromDb" );
    
    if (TileCache::cacheLimit > 0)
    {
        if (TileCache::contains(myOId))
        {
            BLOBTile* cachedTile = TileCache::get(myOId);
            if (cachedTile != NULL)
            {
                from(cachedTile);
                TileCache::update(myOId, this);
            }
            else
            {
                readFromDbReal();
                TileCache::insert(myOId, this);
            }
        }
        else
        {
            readFromDbReal();
            TileCache::insert(myOId, this);
        }
    }
    else
    {
        readFromDbReal();
    }
    
    LEAVE( "BLOBTile::readFromDb" );
    RMDBGEXIT(3, RMDebug::module_blobif, "BLOBTile", "readFromDb() " << myOId);
}
