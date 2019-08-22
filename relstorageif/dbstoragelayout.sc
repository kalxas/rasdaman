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

#include "dbstoragelayout.hh"
#include "storagemgr/sstoragelayout.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/sqlitewrapper.hh"
#include <logging.hh>
#include <string>

using std::endl;

DBStorageLayout::DBStorageLayout() : DBObject(),
    indexType(StorageLayout::DefaultIndexType),
    indexSize(StorageLayout::DefaultIndexSize),
    tilingScheme(StorageLayout::DefaultTilingScheme),
    tileSize(StorageLayout::DefaultTileSize),
    dataFormat(StorageLayout::DefaultDataFormat),
    pctMin(StorageLayout::DefaultMinimalTileSize),
    pctMax(StorageLayout::DefaultPCTMax)
{
    objecttype = OId::STORAGEOID;
}

DBStorageLayout::DBStorageLayout(const OId &id) : DBObject(id),
    indexType(StorageLayout::DefaultIndexType),
    indexSize(StorageLayout::DefaultIndexSize),
    tilingScheme(StorageLayout::DefaultTilingScheme),
    tileSize(StorageLayout::DefaultTileSize),
    dataFormat(StorageLayout::DefaultDataFormat),
    pctMin(StorageLayout::DefaultMinimalTileSize),
    pctMax(StorageLayout::DefaultPCTMax)
{
    objecttype = OId::STORAGEOID;
    readFromDb();
}

DBStorageLayout::~DBStorageLayout() noexcept(false)
{
    validate();
}

void DBStorageLayout::printStatus(unsigned int level, std::ostream &stream) const
{
    std::string indent(level * 2, ' ');
    stream << indent << "DBStorageLayout:" << endl
           << indent << "\tTileSize" << (supportsTileSize() ? "" : " (Def.)") << "\t\t\t:" << getTileSize() << endl
           << indent << "\tPCTMin" << (supportsPCTMin() ? "" : " (Def.)\t") << "\t\t:" << getPCTMin() << endl
           << indent << "\tPCTMax" << (supportsPCTMax() ? "" : " (Def.)\t") << "\t\t:" << getPCTMax() << endl
           << indent << "\tIndexSize" << (supportsIndexSize() ? "" : " (Def.)") << "\t\t:" << getIndexSize() << endl
           << indent << "\tIndexType" << (supportsIndexType() ? "\t" : " (Def.)") << "\t\t:" << getIndexType() << endl
           << indent << "\tTilingScheme" << (supportsTilingScheme() ? "\t" : " (Def.)") << "\t\t:" << getTilingScheme() << endl
           << indent << "\tTileConfiguration" << (supportsTileConfiguration() ? "\t" : " (Def.)") << "\t:" << getTileConfiguration() << endl
           << indent << "\tDataFormat" << (supportsDataFormat() ? "\t" : " (Def.)") << "\t\t:" << getDataFormat() << endl
           << indent;
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

#define READ_COLUMN(paramSupports, param, defaultValue, colType, paramType) \
if (query.currColumnNull()) { \
    _supports##paramSupports = false; \
    param = StorageLayout::defaultValue; \
    query.nextColumn(); \
} else { \
    _supports##paramSupports = true; \
    param = static_cast<paramType>(query.nextColumn##colType()); \
}

void DBStorageLayout::readFromDb()
{
    SQLiteQuery query(
        "SELECT DomainId, TileSize, PCTMin, PCTMax, IndexType, TilingScheme, "
        "DataFormat, IndexSize FROM RAS_STORAGE WHERE StorageId = %lld",
        myOId.getCounter());
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
            tileConfiguration = DBMintervalId(
                        OId(query.nextColumnLong(), OId::DBMINTERVALOID));
        }
        
        READ_COLUMN(TileSize, tileSize, DefaultTileSize, Long, r_Bytes)
        READ_COLUMN(PCTMin, pctMin, DefaultMinimalTileSize, Long, r_Bytes)
        READ_COLUMN(PCTMax, pctMax, DefaultPCTMax, Long, r_Bytes)
        READ_COLUMN(IndexType, indexType, DefaultIndexType, Int, r_Index_Type)
        READ_COLUMN(Tiling, tilingScheme, DefaultTilingScheme, Int, r_Tiling_Scheme)
        READ_COLUMN(DataFormat, dataFormat, DefaultDataFormat, Int, r_Data_Format)
        READ_COLUMN(IndexSize, indexSize, DefaultIndexSize, Int, unsigned int)
    }
    else
    {
        LERROR << "storage id: " << myOId.getCounter() << " not found in the database.";
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

#define BIND_PARAM(cond, DataType, param) \
    if (cond()) \
        insert.bind##DataType(param); \
    else \
        insert.bindNull();

void DBStorageLayout::insertInDb()
{
    SQLiteQuery insert(
        "INSERT INTO RAS_STORAGE(StorageId,DomainId,TileSize,PCTMin,PCTMax,IndexType,"
        "TilingScheme,DataFormat,IndexSize) VALUES (%lld, ?, ?, ?, ?, ?, ?, ?, ?)", myOId.getCounter());

    if (supportsTileConfiguration())
        tileConfiguration->setPersistent(true);

    BIND_PARAM(supportsTileConfiguration, Long, tileConfiguration->getOId().getCounter())
    BIND_PARAM(supportsTileSize, Long, static_cast<long long>(tileSize))
    BIND_PARAM(supportsPCTMin, Long, static_cast<long long>(pctMin))
    BIND_PARAM(supportsPCTMax, Long, static_cast<long long>(pctMax))
    BIND_PARAM(supportsIndexType, Int, indexType)
    BIND_PARAM(supportsTilingScheme, Int, tilingScheme)
    BIND_PARAM(supportsDataFormat, Int, dataFormat)
    BIND_PARAM(supportsIndexSize, Long, indexSize)

    insert.execute();

    DBObject::insertInDb();
}

void DBStorageLayout::deleteFromDb()
{
    SQLiteQuery::executeWithParams(
        "DELETE FROM RAS_STORAGE WHERE StorageId = %lld", myOId.getCounter());
    tileConfiguration->setPersistent(false);
    DBObject::deleteFromDb();
}
