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
#include "dbtile.hh"
#include "reladminif/externs.h"
#include "reladminif/sqlerror.hh"
#include "raslib/error.hh"
#include "reladminif/objectbroker.hh"
#include "blobtile.hh"
#include "inlinetile.hh"
#include "reladminif/dbref.hh"
#include <logging.hh>

#include "unistd.h"
#include <iostream>
#include <cstring>
#include <vector>



r_Data_Format
DBTile::getDataFormat() const
{
    LTRACE << "getDataFormat() const " << myOId << " " << dataFormat;
    return dataFormat;
}

r_Data_Format
DBTile::getCurrentFormat() const
{
    return currentFormat;
}

void
DBTile::setCurrentFormat(const r_Data_Format& dataformat) const
{
    currentFormat = dataformat;
}

void
DBTile::setDataFormat(const r_Data_Format& dataformat)
{
    dataFormat = dataformat;
    setModified();
}

r_Bytes
DBTile::getMemorySize() const
{
    return size * sizeof(char) + sizeof(char*) + sizeof(r_Data_Format) + DBObject::getMemorySize() + sizeof(r_Bytes);
}

void
DBTile::setCells(char* newCells)
{
    if (cells != newCells)
    {
        cells = newCells;
        ownCells = true;
        setModified();
    }
}

void
DBTile::setNoModificationData(char* newCells) const
{
    if (cells != newCells)
    {
        if (cells != NULL && ownCells)
        {
            LDEBUG << "DBTile::setNoModificationData() freeing blob cells";
            free(cells);
            // cells = NULL;    // added PB 2005-jan-10
        }
        cells = newCells;
        ownCells = true;
    }
}

void
DBTile::setNoModificationSize(r_Bytes newSize) const
{
    size = newSize;
}

char*
DBTile::getCells()
{
    setModified();
    return cells;
}

const char*
DBTile::getCells() const
{
    return cells;
}

char
DBTile::getCell(r_Bytes index) const
{
    return getCells()[index];
}

r_Bytes
DBTile::getSize() const
{
    return size;
}

void
DBTile::setCell(r_Bytes index, char newCell)
{
    setModified();
    getCells()[index] = newCell;
}

DBTile::DBTile(r_Data_Format dataformat)
    :   DBObject(),
        size(0),
        cells(NULL),
        dataFormat(dataformat),
        currentFormat(r_Array),
        ownCells{true}
{
    LTRACE << "DBTile::DBTile() empty tile, data format: " << dataFormat;
}

DBTile::DBTile(r_Bytes newSize, r_Data_Format dataformat)
    :   DBObject(),
        size(newSize),
        cells(NULL),
        dataFormat(dataformat),
        currentFormat(r_Array),
        ownCells{true}
{
    LTRACE << "DBTile::DBTile() allocating " << newSize << " bytes for blob cells, not initialized with any value ";
    cells = static_cast<char*>(mymalloc(newSize));
}

DBTile::DBTile(r_Bytes newSize, char c, r_Data_Format dataformat)
    :   DBObject(),
        size(newSize),
        cells(NULL),
        dataFormat(dataformat),
        currentFormat(r_Array),
        ownCells{true}
{
    LTRACE << "DBTile::DBTile() allocating " << newSize << " bytes for blob cells initialized with value " << (long)c;
    cells = static_cast<char*>(mymalloc(newSize));
    memset(cells, c, size);
}

DBTile::DBTile(r_Bytes newSize, r_Bytes patSize, const char* pat, r_Data_Format dataformat)
    :   DBObject(),
        size(newSize),
        cells(NULL),
        dataFormat(dataformat),
        currentFormat(r_Array),
        ownCells{true}
{
    LTRACE << "DBTile::DBTile() allocating " << newSize << " bytes for blob cells with pattern of size " << patSize;
    cells = static_cast<char*>(mymalloc(newSize * sizeof(char)));

    r_Bytes i = 0;
    r_Bytes j = 0;

    if (patSize >= size)
    {
        // fill cells with pattern
        for (j = 0; j < size; j++)
        {
            cells[j] = pat[j];
        }
    }
    else
    {
        // fill cells with repeated pattern
        for (i = 0; i < size; i += patSize)
        {
            for (j = 0; j < patSize; j++)
            {
                cells[(i + j)] = pat[j];
            }
        }
        // pad end with 0
        if (i != size)
        {
            // no padding necessary
            i -= patSize;
            for (; i < size; i++)
            {
                cells[i] = 0;
            }
        }
        else
        {
            // fill cells with 0
            for (i = 0; i < size; i++)
            {
                cells[i] = 0;
            }
        }
    }
}

DBTile::DBTile(r_Bytes newSize, const char* newCells, r_Data_Format dataformat)
    :   DBObject(),
        size(newSize),
        cells(0),
        dataFormat(dataformat),
        currentFormat(r_Array),
        ownCells{true}
{
    LTRACE << "DBTile::DBTile() allocating " << newSize << " bytes for blob cells with copying the given data";

    cells = static_cast<char*>(mymalloc(size * sizeof(char)));
    memcpy(cells, newCells, newSize);
}

DBTile::DBTile(r_Bytes newSize, bool takeOwnershipOfNewCells, char* newCells, r_Data_Format dataformat)
    :   DBObject(),
        size(newSize),
        cells(0),
        dataFormat(dataformat),
        currentFormat(r_Array),
        ownCells{true}
{
    if (takeOwnershipOfNewCells)
    {
        LTRACE << "DBTile::DBTile() allocating " << newSize << " bytes without copying the given data";
        cells = newCells;
    }
    else
    {
        LTRACE << "DBTile::DBTile() allocating " << newSize << " bytes with copying the given data";
        cells = static_cast<char*>(mymalloc(size * sizeof(char)));
        memcpy(cells, newCells, newSize);
    }
}

DBTile::DBTile(const OId& id)
    :   DBObject(id),
        size(0),
        cells(NULL),
        currentFormat(r_Array),
        ownCells{true}
{
    LTRACE << "DBTile::DBTile() oid: " << id;
}

DBTile::~DBTile() noexcept(false)
{
    if (!ownCells)
    {
        LTRACE << "DBTile::~DBTile() does not own data, will not free blob cells of size: " << size;
        return;
    }

    if (cells)
    {
        if (TileCache::cacheLimit > 0)
        {
            if (!TileCache::contains(myOId))
            {
                LTRACE << "DBTile::~DBTile() not cached, freeing blob cells of size: " << size;
                free(cells);
                cells = NULL;
            }
            else
            {
                LTRACE << "DBTile::~DBTile() cached, will not free cells: " << size;
                CacheValue* value = TileCache::get(myOId);
                value->removeReferencingTile(this);
                cells = NULL;
            }
        }
        else
        {
            LTRACE << "DBTile::~DBTile() freeing blob cells of size: " << size;
            free(cells);
            cells = NULL;
        }
    }
    else
    {
        LTRACE << "DBTile::~DBTile() blob cells null, nothing to free";
    }
}

void
DBTile::resize(r_Bytes newSize)
{
    LTRACE << "resize(" << newSize << ") " << myOId;
    if (size != newSize)
    {
        setModified();
        if (cells && ownCells)
        {
            LTRACE << "freeing existing blob cells";
            free(cells);
            cells = NULL;
        }
        LTRACE << "allocating " << newSize << " bytes for blob cells.";
        cells = static_cast<char*>(mymalloc(newSize * sizeof(char)));
        if (cells == NULL)
        {
            LERROR << "failed allocating " << newSize << " bytes of memory for tile.";
            throw new r_Error(r_Error::r_Error_MemoryAllocation);
        }
        size = newSize;
    }
}

void
DBTile::printStatus(unsigned int level, std::ostream& stream) const
{
    DBObject::printStatus(level, stream);
    stream << " r_Data_Format " << dataFormat << " size " << size << " ";
#ifdef DEBUG
    for (int a = 0; a < size; a++)
    {
        stream << " " << (int)(cells[a]);
    }
    stream << endl;
#endif
}

std::ostream&
operator << (std::ostream& stream, DBTile& b)
{
    stream << "\tDBTile at " << &b << endl;
    stream << "\t\tOId\t\t:" << b.myOId << endl;
    stream << "\t\tId\t\t:" << b.myOId.getCounter() << endl;
    stream << "\t\tSize\t\t:" << b.size << endl;
    stream << "\t\tModified\t:" << static_cast<int>(b._isModified) << endl;
    stream << "\t\tCells\t\t:";
#ifdef DEBUG
    for (int a = 0; a < b.size; a++)
    {
        stream << " " << (int)(b.cells[a]);
    }
    stream << endl;
#endif
    return stream;
}

