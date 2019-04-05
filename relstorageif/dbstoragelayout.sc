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
/****************************************************************************
 *
 *
 * PURPOSE:
 * Code with embedded SQL for PostgreSQL DBMS
 *
 *
 * COMMENTS:
 *  none
 *
 ****************************************************************************/

#include "config.h"
#include "dbstoragelayout.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/externs.h"
#include "reladminif/sqlglobals.h"
#include "storagemgr/sstoragelayout.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlitewrapper.hh"
#include <logging.hh>

using std::endl;

DBStorageLayout::DBStorageLayout()
    : DBObject(),
      indexType(StorageLayout::DefaultIndexType),
      indexSize(StorageLayout::DefaultIndexSize),
      tilingScheme(StorageLayout::DefaultTilingScheme),
      tileSize(StorageLayout::DefaultTileSize),
      tileConfiguration(new DBMinterval()),
      dataFormat(StorageLayout::DefaultDataFormat),
      pctMin(StorageLayout::DefaultMinimalTileSize),
      pctMax(StorageLayout::DefaultPCTMax),
      _supportsTileSize(false),
      _supportsPCTMin(false),
      _supportsPCTMax(false),
      _supportsIndexSize(false),
      _supportsIndexType(false),
      _supportsTiling(false),
      _supportsTileConfiguration(false),
      _supportsDataFormat(false)
{
    objecttype = OId::STORAGEOID;
}

DBStorageLayout::DBStorageLayout(const OId &id)
    : DBObject(id),
      indexType(StorageLayout::DefaultIndexType),
      indexSize(StorageLayout::DefaultIndexSize),
      tilingScheme(StorageLayout::DefaultTilingScheme),
      tileSize(StorageLayout::DefaultTileSize),
      tileConfiguration(new DBMinterval()),
      dataFormat(StorageLayout::DefaultDataFormat),
      pctMin(StorageLayout::DefaultMinimalTileSize),
      pctMax(StorageLayout::DefaultPCTMax),
      _supportsTileSize(false),
      _supportsPCTMin(false),
      _supportsPCTMax(false),
      _supportsIndexSize(false),
      _supportsIndexType(false),
      _supportsTiling(false),
      _supportsTileConfiguration(false),
      _supportsDataFormat(false)
{
    objecttype = OId::STORAGEOID;
    readFromDb();
}

void DBStorageLayout::printStatus(unsigned int level, std::ostream &stream) const
{
    char *indent = new char[level * 2 + 1];
    for (unsigned int j = 0; j < level * 2; j++)
    {
        indent[j] = ' ';
    }
    indent[level * 2] = '\0';

    stream << indent;
    stream << "DBStorageLayout:" << endl;
    if (supportsTileSize())
    {
        stream << "\tTileSize\t\t\t:";
    }
    else
    {
        stream << "\tTileSize (Def.)\t\t\t:";
    }
    stream << getTileSize() << endl;
    stream << indent;
    if (supportsPCTMin())
    {
        stream << "\tPCTMin\t\t:";
    }
    else
    {
        stream << "\tPCTMin (Def.)\t\t\t:";
    }
    stream << getPCTMin() << endl;
    stream << indent;
    if (supportsPCTMax())
    {
        stream << "\tPCTMax\t\t:";
    }
    else
    {
        stream << "\tPCTMax (Def.)\t\t\t:";
    }
    stream << getPCTMax() << endl;
    stream << indent;
    if (supportsIndexSize())
    {
        stream << "\tIndexSize\t\t:";
    }
    else
    {
        stream << "\tIndexSize (Def.)\t\t:";
    }
    stream << getIndexSize() << endl;
    stream << indent;
    if (supportsIndexType())
    {
        stream << "\tIndexType\t\t\t:";
    }
    else
    {
        stream << "\tIndexType (Def.)\t\t:";
    }
    stream << getIndexType() << endl;
    stream << indent;
    if (supportsTilingScheme())
    {
        stream << "\tTilingScheme\t\t\t:";
    }
    else
    {
        stream << "\tTilingScheme (Def.)\t\t:";
    }
    stream << getTilingScheme() << endl;
    stream << indent;
    if (supportsTileConfiguration())
    {
        stream << "\tTileConfiguration\t\t:";
    }
    else
    {
        stream << "\tTileConfiguration (Def.)\t:";
    }
    stream << getTileConfiguration() << endl;
    stream << indent;
    if (supportsDataFormat())
    {
        stream << "\tDataFormat\t\t\t:";
    }
    else
    {
        stream << "\tDataFormat (Def.)\t\t:";
    }
    stream << getDataFormat() << endl;
    stream << indent;

    delete[] indent;
}

bool DBStorageLayout::supportsIndexType() const
{
    return _supportsIndexType;
}

bool DBStorageLayout::supportsDataFormat() const
{
    return _supportsDataFormat;
}

bool DBStorageLayout::supportsTilingScheme() const
{
    return _supportsTiling;
}

bool DBStorageLayout::supportsTileSize() const
{
    return _supportsTileSize;
}

bool DBStorageLayout::supportsIndexSize() const
{
    return _supportsIndexSize;
}

bool DBStorageLayout::supportsPCTMin() const
{
    return _supportsPCTMin;
}

bool DBStorageLayout::supportsPCTMax() const
{
    return _supportsPCTMax;
}

bool DBStorageLayout::supportsTileConfiguration() const
{
    return _supportsTileConfiguration;
}

r_Index_Type DBStorageLayout::getIndexType() const
{
    return indexType;
}

r_Data_Format DBStorageLayout::getDataFormat() const
{
    return dataFormat;
}

r_Tiling_Scheme DBStorageLayout::getTilingScheme() const
{
    return tilingScheme;
}

r_Bytes DBStorageLayout::getTileSize() const
{
    return tileSize;
}

unsigned int DBStorageLayout::getIndexSize() const
{
    return indexSize;
}

r_Bytes DBStorageLayout::getPCTMin() const
{
    return pctMin;
}

r_Bytes DBStorageLayout::getPCTMax() const
{
    return pctMax;
}

r_Minterval DBStorageLayout::getTileConfiguration() const
{
    return *tileConfiguration;
}

void DBStorageLayout::setIndexType(r_Index_Type it)
{
    _supportsIndexType = true;
    indexType = it;
    setModified();
}

void DBStorageLayout::setDataFormat(r_Data_Format cs)
{
    _supportsDataFormat = true;
    dataFormat = cs;
    setModified();
}

void DBStorageLayout::setTilingScheme(r_Tiling_Scheme ts)
{
    _supportsTiling = true;
    tilingScheme = ts;
    setModified();
}

void DBStorageLayout::setTileSize(r_Bytes tsize)
{
    _supportsTileSize = true;
    tileSize = tsize;
    setModified();
}

void DBStorageLayout::setTileConfiguration(const r_Minterval &tc)
{
    _supportsTileConfiguration = true;
    *tileConfiguration = tc;
    setModified();
}

void DBStorageLayout::setIndexSize(unsigned int newindexSize)
{
    _supportsIndexSize = true;
    indexSize = newindexSize;
    setModified();
}

void DBStorageLayout::setPCTMin(r_Bytes newpctMin)
{
    _supportsPCTMin = true;
    pctMin = newpctMin;
    setModified();
}

void DBStorageLayout::setPCTMax(r_Bytes newpctMax)
{
    _supportsPCTMax = true;
    pctMax = newpctMax;
    setModified();
}

DBStorageLayout::~DBStorageLayout() noexcept(false)
{
    validate();
}

/*
TABLE RAS_STORAGE (
    StorageId   INTEGER NOT NULL UNIQUE,
    DomainId    INTEGER,
    TileSize    INTEGER,
    PCTMin      INTEGER,
    PCTMax      INTEGER,
    IndexSize   INTEGER,
    IndexType   SMALLINT,
    TilingScheme    SMALLINT,
    DataFormat  SMALLINT
    )
 */

void DBStorageLayout::readFromDb()
{
    long long storageid1;

    storageid1 = myOId.getCounter();

    SQLiteQuery query(
        "SELECT DomainId, TileSize, PCTMin, PCTMax, IndexType, TilingScheme, "
        "DataFormat, IndexSize FROM RAS_STORAGE WHERE StorageId = %lld",
        storageid1);
    if (query.nextRow())
    {
        if (query.currColumnNull())
        {
            _supportsTileConfiguration = false;
            *tileConfiguration = StorageLayout::DefaultTileConfiguration;
            query.nextColumn();
        }
        else
        {
            _supportsTileConfiguration = true;
            tileConfiguration = OId(query.nextColumnLong(), OId::DBMINTERVALOID);
        }

        if (query.currColumnNull())
        {
            _supportsTileSize = false;
            tileSize = StorageLayout::DefaultTileSize;
            query.nextColumn();
        }
        else
        {
            _supportsTileSize = true;
            tileSize = query.nextColumnLong();
        }

        if (query.currColumnNull())
        {
            _supportsPCTMin = false;
            pctMin = StorageLayout::DefaultMinimalTileSize;
            query.nextColumn();
        }
        else
        {
            _supportsPCTMin = true;
            pctMin = query.nextColumnLong();
        }

        if (query.currColumnNull())
        {
            _supportsPCTMax = false;
            pctMax = StorageLayout::DefaultPCTMax;
            query.nextColumn();
        }
        else
        {
            _supportsPCTMax = true;
            pctMax = query.nextColumnLong();
        }

        if (query.currColumnNull())
        {
            _supportsIndexType = false;
            indexType = StorageLayout::DefaultIndexType;
            query.nextColumn();
        }
        else
        {
            _supportsIndexType = true;
            indexType = (r_Index_Type) query.nextColumnInt();
        }

        if (query.currColumnNull())
        {
            _supportsTiling = false;
            tilingScheme = StorageLayout::DefaultTilingScheme;
            query.nextColumn();
        }
        else
        {
            _supportsTiling = true;
            tilingScheme = (r_Tiling_Scheme) query.nextColumnInt();
        }

        if (query.currColumnNull())
        {
            _supportsDataFormat = false;
            dataFormat = StorageLayout::DefaultDataFormat;
            query.nextColumn();
        }
        else
        {
            _supportsDataFormat = true;
            dataFormat = (r_Data_Format) query.nextColumnInt();
        }

        if (query.currColumnNull())
        {
            _supportsIndexSize = false;
            indexSize = StorageLayout::DefaultIndexSize;
            query.nextColumn();
        }
        else
        {
            _supportsIndexSize = true;
            indexSize = query.nextColumnLong();
        }
    }
    else
    {
        LERROR << "storage id: " << storageid1 << " not found in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "mdd storage data not found in the database.");
    }

    DBObject::readFromDb();
}

void DBStorageLayout::updateInDb()
{
    deleteFromDb();
    insertInDb();
    DBObject::updateInDb();
}

void DBStorageLayout::insertInDb()
{
    long long storageid2;
    long long domainid2;

    storageid2 = myOId.getCounter();
    SQLiteQuery insert(
        "INSERT INTO RAS_STORAGE(StorageId,DomainId,TileSize,PCTMin,PCTMax,IndexType,"
        "TilingScheme,DataFormat,IndexSize) VALUES (%lld, ?, ?, ?, ?, ?, ?, ?, ?)", storageid2);

    if (supportsTileConfiguration())
    {
        tileConfiguration->setPersistent(true);
        domainid2 = tileConfiguration->getOId().getCounter();
        insert.bindLong(domainid2);
    }
    else
    {
        insert.bindNull();
    }

    if (supportsTileSize())
    {
        insert.bindLong(tileSize);
    }
    else
    {
        insert.bindNull();
    }

    if (supportsPCTMin())
    {
        insert.bindLong(pctMin);
    }
    else
    {
        insert.bindNull();
    }

    if (supportsPCTMax())
    {
        insert.bindLong(pctMax);
    }
    else
    {
        insert.bindNull();
    }

    if (supportsIndexType())
    {
        insert.bindInt(indexType);
    }
    else
    {
        insert.bindNull();
    }

    if (supportsTilingScheme())
    {
        insert.bindInt(tilingScheme);
    }
    else
    {
        insert.bindNull();
    }

    if (supportsDataFormat())
    {
        insert.bindInt(dataFormat);
    }
    else
    {
        insert.bindNull();
    }

    if (supportsIndexSize())
    {
        insert.bindLong(indexSize);
    }
    else
    {
        insert.bindNull();
    }
    insert.execute();

    DBObject::insertInDb();
}

void DBStorageLayout::deleteFromDb()
{
    long long storageid3;

    storageid3 = myOId.getCounter();
    SQLiteQuery::executeWithParams("DELETE FROM RAS_STORAGE WHERE StorageId = %lld", storageid3);
    tileConfiguration->setPersistent(false);
    DBObject::deleteFromDb();
}

