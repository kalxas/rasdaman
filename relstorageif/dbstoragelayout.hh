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

#ifndef _DBSTORAGELAYOUT_HH_
#define _DBSTORAGELAYOUT_HH_

#include "relcatalogif/dbminterval.hh"
#include "reladminif/dbobject.hh"
#include "reladminif/dbref.hh"

#include <string>
#include <vector>

class DBStorageLayout;
template <class T>
class DBRef;
using DBStorageLayoutId = DBRef<DBStorageLayout>;

//@ManMemo: Module: relstorageif
/**
    Describes a physical storage layout for an MDD object or collection. Every
    storage parameter which is not defined using the proper set* methods will
    result in a default value to be returned. The supports* methods will tell
    if the value is a default value defined at instantiation time through the
    static storagemgr/StorageLayout attributes or an explicitly defined value.
    For information on the meaning of these attributes refer to
    storagemgr/storagelayout.
*/
/**
  * \defgroup Relstorageifs Relstorageif Classes
  */
class DBStorageLayout : public DBObject
{
public:
    //@Man: Creation
    //@{
    /// Construct object that uses system defaults.
    DBStorageLayout();
    //@}

    //@Man: Destruction
    //@{
    ~DBStorageLayout() noexcept(false) override;
    //@}

    //@Man: check operations
    //@{
    bool supportsTileSize() const;
    bool supportsPCTMin() const;
    bool supportsPCTMax() const;
    bool supportsIndexSize() const;
    bool supportsIndexType() const;
    bool supportsTilingScheme() const;
    // is checked by OId::INVALID on tilingConfiguration
    bool supportsTileConfiguration() const;
    bool supportsDataFormat() const;
    //@}

    //@Man: Get operations
    //@{
    r_Bytes getPCTMin() const;
    r_Bytes getPCTMax() const;
    unsigned int getIndexSize() const;
    r_Index_Type getIndexType() const;
    r_Tiling_Scheme getTilingScheme() const;
    r_Bytes getTileSize() const;
    r_Minterval getTileConfiguration() const;
    r_Data_Format getDataFormat() const;
    //@}

    //@Man: Set operations
    //@{
    void setPCTMin(r_Bytes bytes);
    void setPCTMax(r_Bytes bytes);
    void setIndexSize(unsigned int entries);
    void setIndexType(r_Index_Type it);
    void setTilingScheme(r_Tiling_Scheme ts);
    void setTileSize(r_Bytes ts);
    void setTileConfiguration(const r_Minterval &tc);
    void setDataFormat(r_Data_Format df);
    //@}

    void printStatus(unsigned int level, std::ostream &stream) const override;

protected:
    DBStorageLayout(const OId &id);

    friend class ObjectBroker;

    //@Man: Operations
    //@{
    void readFromDb() override;
    void insertInDb() override;
    void deleteFromDb() override;
    void updateInDb() override;
    //@}

private:
    //@Man: Actual Parameters:
    //@{

    /// Name of the storage layout represented by this object
    // char* stName;

    //@Man: Index Structure:
    //@{
    /// Which type of index should be used
    r_Index_Type indexType;
    /// Default index node size
    unsigned int indexSize;
    //@}

    //@Man: Tiling:
    //@{
    /// How the object should be tiled
    r_Tiling_Scheme tilingScheme;
    /// Tile size in bytes.
    r_Bytes tileSize;

    /** 
     * Default configuration of the tiles.
     * 
     * Describe the shape of the tiles. For instance, [2:4,0:1,0:2]. The tiling
     * will start at the point [2,0,0]. Tiles will be appended from there
     * according to the tileConfig.
     */
    DBMintervalId tileConfiguration{new DBMinterval()};
    //@}

    //@Man: DataFormat
    //@{
    /// How the tiles of the object should be compressed
    r_Data_Format dataFormat;
    //@}

    r_Bytes pctMin;
    r_Bytes pctMax;
    //@}

    bool _supportsTileSize{false};
    bool _supportsPCTMin{false};
    bool _supportsPCTMax{false};
    bool _supportsIndexSize{false};
    bool _supportsIndexType{false};
    bool _supportsTiling{false};
    bool _supportsTileConfiguration{false};
    bool _supportsDataFormat{false};
};

#endif
