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
/**
 * SOURCE: persmddobj.cc
 *
 * MODULE: cachetamgr
 * CLASS:   MDDObj
 *
 * COMMENTS:
 * none
 *
*/

#include "mddobj.hh"

#include "indexmgr/mddobjix.hh"         // for MDDObjIx
#include "tilemgr/tile.hh"              // for Tile
#include "reladminif/eoid.hh"           // for EOId
#include "mymalloc/mymalloc.h"
#include "relcatalogif/mdddomaintype.hh"
#include "relcatalogif/structtype.hh"
#include "relblobif/dbtile.hh"          // for DBTile
#include "relblobif/tileid.hh"          // for DBTileId
#include "relcatalogif/basetype.hh"     // for BaseType
#include "relcatalogif/mddbasetype.hh"  // for MDDBaseType
#include "relmddif/dbmddobj.hh"         // for DBMDDObj
#include "relstorageif/storageid.hh"    // for DBStorageLayoutId
#include "raslib/error.hh"              // for r_Error, MDDTYPE_NULL, LAYOUTALGO...
#include "raslib/mddtypes.hh"           // for r_Ptr, r_Dimension, r_Directory_I...
#include "raslib/sinterval.hh"          // for r_Sinterval
#include "raslib/point.hh"
#include "storagemgr/sstoragelayout.hh"     // for StorageLayout
#include "relcatalogif/collectiontype.hh"
#include <logging.hh>

#include <iostream>                     // for ostream
#include <stdlib.h>
#include <cstring>
#include <memory>
#include <cassert>

#define COLOR_PALET_SIZE 15

using std::shared_ptr;
using std::make_shared;

const r_Minterval &MDDObj::checkStorage(const r_Minterval &domain2)
{
    r_Minterval domain(domain2.dimension());
    if (myStorageLayout->getIndexType() == r_Reg_Computed_Index)
    {
        if (myStorageLayout->getTilingScheme() != r_RegularTiling)
        {
            LERROR << "MDDObj::checkStorage(" << domain2
                   << ") the rc index needs a regular tiling defined";
            throw r_Error(RCINDEXWITHOUTREGULARTILING);
        }
        r_Dimension dim = domain2.dimension();
        // make sure the tileConfig is fixed
        r_Minterval tileConfig = myStorageLayout->getTileConfiguration();
        myStorageLayout->setTileConfiguration(tileConfig);
        r_Point mddDomainExtent = domain2.get_extent();
        r_Point tileConfigExtent = tileConfig.get_extent();
        for (r_Dimension i = 0; i < dim; i++)
        {
            if (!domain2[i].is_high_fixed() || !domain2[i].is_low_fixed() ||
                    !tileConfig[i].is_high_fixed() || !tileConfig[i].is_low_fixed())
            {
                LERROR << "MDDObj::checkStorage(" << domain2 << ") the rc index needs a domain and tile configuration with "
                       "fixed domains in all dimensions.  Dimension " << i << " seems not to be fixed.";
                throw r_Error(RCINDEXWITHINCOMPATIBLEMARRAYTYPE);
            }
            if (mddDomainExtent[i] % tileConfigExtent[i] != 0)
            {
                LERROR << "MDDObj::checkStorage(" << domain2 << ") the tile configuration (" << tileConfig
                       << ") does not fit the domain of the marray (" << domain << ").";
                throw r_Error(TILECONFIGMARRAYINCOMPATIBLE);
            }
        }
    }
    return domain2;
}

MDDObj::MDDObj(const MDDBaseType *mddType, const r_Minterval &domain)
    :   NullValuesHandler(), myDBMDDObj(), myMDDIndex(nullptr), myStorageLayout(nullptr)
{
    if (!mddType)
    {
        LERROR << "MDD type is NULL.";
        throw r_Error(MDDTYPE_NULL);
    }

    LTRACE << "MDDObj(" << mddType->getName() << ", " << domain << ") " << (r_Ptr)this;
    myStorageLayout = new StorageLayout(r_Directory_Index);
    myStorageLayout->setCellSize(static_cast<int>(mddType->getBaseType()->getSize()));
    myMDDIndex = new MDDObjIx(*myStorageLayout, domain, mddType->getBaseType(), false);
    myDBMDDObj = new DBMDDObj(mddType, domain, myMDDIndex->getDBMDDObjIxId(), myStorageLayout->getDBStorageLayout());
}

MDDObj::MDDObj(const MDDBaseType *mddType, const r_Minterval &domain, r_Nullvalues *newNullValues)
    :   NullValuesHandler(), myDBMDDObj(), myMDDIndex(nullptr), myStorageLayout(nullptr)
{
    if (!mddType)
    {
        LERROR << "MDD type is NULL.";
        throw r_Error(MDDTYPE_NULL);
    }

    LTRACE << "MDDObj(" << mddType->getName() << ", " << domain << ") " << (r_Ptr)this;
    setNullValues(newNullValues);
    myStorageLayout = new StorageLayout(r_Directory_Index);
    myStorageLayout->setCellSize(static_cast<int>(mddType->getBaseType()->getSize()));
    myMDDIndex = new MDDObjIx(*myStorageLayout, domain, mddType->getBaseType(), false);
    myDBMDDObj = new DBMDDObj(mddType, domain, myMDDIndex->getDBMDDObjIxId(), myStorageLayout->getDBStorageLayout());
}

MDDObj::MDDObj(const MDDBaseType *mddType, const r_Minterval &domain,
               const OId &newOId, const StorageLayout &ms)
    :   NullValuesHandler(), myDBMDDObj(), myMDDIndex(nullptr), myStorageLayout(nullptr)
{
    if (!mddType)
    {
        LERROR << "MDD type is NULL.";
        throw r_Error(MDDTYPE_NULL);
    }

    LTRACE << "MDDObj(" << mddType->getName() << ", " << domain << ", " << newOId << ", " << ms.getDBStorageLayout().getOId() << ") " << (r_Ptr)this;
    myStorageLayout = new StorageLayout(ms);
    myStorageLayout->setCellSize(static_cast<int>(mddType->getBaseType()->getSize()));
    myMDDIndex = new MDDObjIx(*myStorageLayout, checkStorage(domain), mddType->getBaseType());
    myDBMDDObj = new DBMDDObj(mddType, domain, myMDDIndex->getDBMDDObjIxId(), ms.getDBStorageLayout(), newOId);
}

MDDObj::MDDObj(const MDDBaseType *mddType, const r_Minterval &domain, const OId &newOId)
    : myDBMDDObj(), myMDDIndex(nullptr), myStorageLayout(nullptr)
{
    if (!mddType)
    {
        LERROR << "MDD type is NULL.";
        throw r_Error(MDDTYPE_NULL);
    }
    myStorageLayout = new StorageLayout();
    myMDDIndex = new MDDObjIx(*myStorageLayout, checkStorage(domain), mddType->getBaseType());
    LTRACE << "MDDObj(" << mddType->getName() << ", " << domain << ", " << newOId;
    myDBMDDObj = new DBMDDObj(mddType, domain, myMDDIndex->getDBMDDObjIxId(), myStorageLayout->getDBStorageLayout(), newOId);
}

MDDObj::MDDObj(const DBMDDObjId &dbmddobj)
    :   NullValuesHandler(), myDBMDDObj(dbmddobj), myMDDIndex(nullptr), myStorageLayout(nullptr)
{
    LTRACE << "MDDObj(DBRef " << dbmddobj.getOId() << ") " << (r_Ptr)this;
    myStorageLayout = new StorageLayout(myDBMDDObj->getDBStorageLayout());
    myStorageLayout->setCellSize(static_cast<int>(myDBMDDObj->getCellType()->getSize()));
    myMDDIndex = new MDDObjIx(myDBMDDObj->getDBIndexDS(), *myStorageLayout, myDBMDDObj->getMDDBaseType()->getBaseType());
}

MDDObj::MDDObj(const OId &givenOId)
    :   NullValuesHandler(), myDBMDDObj(OId()), myMDDIndex(nullptr), myStorageLayout(nullptr)
{
    LTRACE << "MDDObj(" << givenOId << ") " << (r_Ptr) this;
    myDBMDDObj = DBMDDObjId(givenOId);
    myStorageLayout = new StorageLayout(myDBMDDObj->getDBStorageLayout());
    myStorageLayout->setCellSize(static_cast<int>(myDBMDDObj->getCellType()->getSize()));
    myMDDIndex = new MDDObjIx(myDBMDDObj->getDBIndexDS(), *myStorageLayout, myDBMDDObj->getMDDBaseType()->getBaseType());
}

MDDObj::MDDObj(const MDDBaseType *mddType, const r_Minterval &domain, const StorageLayout &ms)
    :   NullValuesHandler(), myDBMDDObj(OId()), myMDDIndex(nullptr), myStorageLayout(nullptr)
{
    LTRACE << "MDDObj(" << mddType->getName() << ", " << domain << ", "
           << ms.getDBStorageLayout().getOId() << ") " << (r_Ptr) this;
    if (!mddType)
    {
        LERROR << "MDD type is NULL.";
        throw r_Error(MDDTYPE_NULL);
    }
    myStorageLayout = new StorageLayout(ms);
    myStorageLayout->setCellSize(static_cast<int>(mddType->getBaseType()->getSize()));
    myMDDIndex = new MDDObjIx(*myStorageLayout, checkStorage(domain), mddType->getBaseType());
    myDBMDDObj = new DBMDDObj(mddType, domain, myMDDIndex->getDBMDDObjIxId(), ms.getDBStorageLayout());
    myDBMDDObj->setPersistent();
}

void MDDObj::insertTile(Tile *newTile)
{
    insertTile(shared_ptr<Tile>(newTile));
}

/*
insert tile:
 tiles may have to be retiled.
 the storage layout returns the domains into which the tile should be divided before insertion.
 if there is not enough data to fill a complete layout domain, then 0 will be set.
*/
void MDDObj::insertTile(shared_ptr<Tile> newTile)
{
    auto layoutDoms = myStorageLayout->getLayout(newTile->getDomain());
#ifdef RASDEBUG
    LTRACE << "storage layout returned the following domains";
    for (auto domit = layoutDoms.begin(); domit != layoutDoms.end(); domit++)
    {
        LTRACE << "  " << *domit;
    }
#endif

    shared_ptr<Tile> tile;
    shared_ptr<Tile> tile2;
    r_Area tempArea = 0;
    r_Area completeArea = 0;
    r_Minterval tempDom;
    r_Minterval tileDom = newTile->getDomain();
    std::vector<shared_ptr<Tile>> *indexTiles = NULL;
    char *newContents = NULL;
    size_t sizeOfData = 0;
    bool checkEquality = true;
    for (std::vector<r_Minterval>::iterator it = layoutDoms.begin(); it != layoutDoms.end(); it++)
    {
        if (checkEquality && tileDom == *it)
        {
            // normal case.  just insert the tile.
            // this case also means that there was no insertion in the previous loops
            LTRACE << "tile domain is same as layout domain, just inserting data";
            myMDDIndex->insertTile(newTile);
            // set to NULL so it will not get deleted at the end of the method
            newTile.reset();
            if (layoutDoms.size() != 1)
            {
                LERROR << "MDDObj::insertTile(Tile " << tileDom << ") the layout has more than one element but the tile domain completely covers the layout domain";
                throw r_Error(LAYOUTALGORITHMPROBLEM);
            }
        }
        else     // we need to check if there is already a tile defined here
        {
            // this could have been created in a previous loop run
            // we are using retiling here.  *it is therefore an indivisible layout domain.
            LTRACE << "tile domain (" << tileDom << ") is not the same as layout domain (" << *it << ")";
            indexTiles = myMDDIndex->intersect(*it);
            if (indexTiles && indexTiles->size() > 0)
            {
                // there was a tile in the run before, which overlapped with this layout domain
                // there may only be one entry in the index for this domain.
                LTRACE << "found tiles (" << indexTiles->size() << ") in layout domain " << *it;
                if (indexTiles->size() != 1)
                {
                    LERROR << "MDDObj::insertTile(Tile " << tileDom << ") the index contains many entries for one layout domain";
                    throw r_Error(LAYOUTALGORITHMPROBLEM);
                }
                // update the existing tile with the new data
                tempDom = (*it).create_intersection(tileDom);
                (*(indexTiles->begin()))->copyTile(tempDom, newTile.get(), tempDom);
                //LDEBUG << "updated tile to";
                // (*(indexTiles->begin()))->printStatus(99,RMInit::dbgOut);
            }
            else     // there was no tile overlapping the current layout domain yet
            {
                // create a new tile covering the whole layout domain
                // must be computed everytime because layoutDoms may change in size
                LTRACE << "found no tiles in layout domain " << *it;
                // generate a tile of the domain : layout domain
                tile.reset(new Tile(*it, getMDDBaseType()->getBaseType(), newTile->getDataFormat()));

                tempDom = (*it).create_intersection(tileDom);
                // only update the actual data - the rest was set to 0
                tile->copyTile(tempDom, newTile.get(), tempDom);
                LTRACE << "created tile with domain " << tile->getDomain();
                //LDEBUG << "insert tile";
                //  tile->printStatus(99,RMInit::dbgOut);
                //FIXME: should not be neccessary
                myMDDIndex->insertTile(tile);
            }
            if (indexTiles)
            {
                delete indexTiles;
                indexTiles = NULL;
            }
        }
        checkEquality = false;
    }
    if (newTile)
    {
        LTRACE << "deleting inserted tile";
        if (newTile->isPersistent())
        {
            newTile->getDBTile()->setPersistent(false);
        }
        newTile.reset();
    }
}

std::vector<shared_ptr<Tile>> *MDDObj::intersect(const r_Minterval &searchInter) const
{
    auto *retval = myMDDIndex->intersect(searchInter);
#ifdef DEBUG
    if (retval)
    {
        for (auto it = retval->begin(); it != retval->end(); it++)
        {
            LTRACE << "FOUND " << (*it)->getDomain() << " ";
        }
    }
#endif
    return retval;
}

std::vector<shared_ptr<Tile>> *MDDObj::getTiles() const
{
    return myMDDIndex->getTiles();
}

const char *MDDObj::pointQuery(const r_Point &searchPoint) const
{
    return myMDDIndex->pointQuery(searchPoint);
}

DBMDDObjId MDDObj::getDBMDDObjId() const
{
    return myDBMDDObj;
}

const MDDBaseType *MDDObj::getMDDBaseType() const
{
    return myDBMDDObj->getMDDBaseType();
}

r_Minterval MDDObj::getDefinitionDomain() const
{
    return myDBMDDObj->getDefinitionDomain();
}

r_Minterval MDDObj::getCurrentDomain() const
{
    return myMDDIndex->getCurrentDomain();
}

const char *MDDObj::getCellTypeName() const
{
    return myDBMDDObj->getCellTypeName();
}

const BaseType *MDDObj::getCellType() const
{
    return myDBMDDObj->getCellType();
}

r_Dimension MDDObj::getDimension() const
{
    return myDBMDDObj->dimensionality();
}

bool MDDObj::isPersistent() const
{
    return myDBMDDObj->isPersistent();
}

int MDDObj::getOId(OId *pOId) const
{
    *pOId = myDBMDDObj->getOId();
    return (pOId->getCounter() == 0);
}

int MDDObj::getEOId(EOId *pEOId) const
{
    *pEOId = myDBMDDObj->getEOId();
    return (pEOId->getCounter() == 0);
}

void MDDObj::printStatus(unsigned int level, std::ostream &stream) const
{
    myDBMDDObj->printStatus(level, stream);
    myMDDIndex->printStatus(level, stream);
}

void MDDObj::removeTile(shared_ptr<Tile> &tileToRemove)
{
    LTRACE << "removing tile: " << tileToRemove->getDBTile().getOId().getCounter() << ", with sdom: " << tileToRemove->getDomain()
           << ", from index: " << myMDDIndex->getDBMDDObjIxId().getOId().getCounter();
    auto found = myMDDIndex->removeTile(tileToRemove);
    if (found)
    {
        // frees its memory. Persistent freeing??
        LTRACE << "tile removed from index, deleting from RASBASE...";
        tileToRemove->getDBTile().delete_object();
        tileToRemove.reset();
    }
    else
    {
        LTRACE << "tile not found in index.";
    }
}

MDDObj::~MDDObj() noexcept(false)
{
    if (myMDDIndex)
    {
        delete myMDDIndex;
        myMDDIndex = nullptr;
    }
    if (myStorageLayout)
    {
        delete myStorageLayout;
        myStorageLayout = nullptr;
    }
}

void MDDObj::releaseTiles()
{
    myMDDIndex->releasePersTiles();
}

StorageLayout *MDDObj::getStorageLayout() const
{
    return myStorageLayout;
}

void MDDObj::setUpdateNullValues(r_Nullvalues *newNullValues)
{
    nullValues = newNullValues;
    if (newNullValues)
    {
        myDBMDDObj->setNullValues(*nullValues);
    }
}

std::string MDDObj::getArrayInfo(common::PrintTiles printTiles) const
{
    switch (printTiles) {
      case common::PrintTiles::EMBEDDED:
      case common::PrintTiles::NONE:
        return getEmbeddedArrayInfo(printTiles);
      case common::PrintTiles::JSON:
        return getJsonArrayInfo();
      case common::PrintTiles::SVG:
        return getSvgArrayInfo();
      default:
        LERROR << "Unsupported printtiles argument.";
        throw r_Error(379);
    }
}

std::string MDDObj::getEmbeddedArrayInfo(common::PrintTiles printTiles) const
{
    std::ostringstream info;
    auto oid = static_cast<double>(myDBMDDObj->getOId());
    info << "{\n \"oid\": \"" << oid;
  
    auto baseType = this->getMDDBaseType()->getTypeStructure();
    info << "\",\n \"baseType\": \"" << baseType;
    
    if (collType)
        info << "\",\n \"setTypeName\": \"" << collType->getName();
    info << "\",\n \"mddTypeName\": \"" << myDBMDDObj->getMDDBaseType()->getTypeName();
  
    std::unique_ptr<std::vector<std::shared_ptr<Tile>>> tiles{};
    tiles.reset(this->getTiles());
    info << "\",\n \"tileNo\": \"" << tiles->size();

    size_t totalSize = 0;
    for (const auto &tile : *tiles)
        totalSize += tile->getSize();
    info << "\",\n \"totalSize\": \"" << totalSize;

    const auto *sl = this->getStorageLayout();
    if (sl)
    {
        info << "\",\n \"tiling\": {\n";
        info << "\t\"tilingScheme\": \"";
        switch (sl->getTilingScheme())
        {
        case r_NoTiling:          info << "no_tiling"; break;
        case r_RegularTiling:     info << "regular"; break;
        case r_StatisticalTiling: info << "statistic"; break;
        case r_InterestTiling:    info << "interest"; break;
        case r_AlignedTiling:     info << "aligned"; break;
        case r_DirectionalTiling: info << "directional"; break;
        case r_SizeTiling:        info << "size"; break;
        default:                  info << "unknown"; break;
        }
        info << "\",\n\t\"tileSize\": \"" << sl->getTileSize();
        auto tileConf = sl->getTileConfiguration();
        if (tileConf.has_axis_names())
        {
            std::vector<std::string> emptyAxisNames(tileConf.dimension());
            tileConf.set_axis_names(emptyAxisNames);
        }
        info << "\",\n\t\"tileConfiguration\": \"" << tileConf << "\"";
  
        if (printTiles == common::PrintTiles::EMBEDDED)
        {
            info << ",\n\t\"tileDomains\":\n\t";
            printArrayTiles(tiles, info);
        }
  
        info << "\n },\n \"index\": {\n\t\"type\": \"";
        switch (sl->getIndexType())
        {
        case r_Invalid_Index:         info << "invalid"; break;
        case r_Auto_Index:            info << "a_index"; break;
        case r_Directory_Index:       info << "d_index"; break;
        case r_Reg_Directory_Index:   info << "rd_index"; break;
        case r_RPlus_Tree_Index:      info << "rpt_index"; break;
        case r_Reg_RPlus_Tree_Index:  info << "rrpt_index"; break;
        case r_Reg_Computed_Index:    info << "rc_index"; break;
        case r_Index_Type_NUMBER:     info << "it_index"; break;
        case r_Tile_Container_Index:  info << "tc_index"; break;
        default:                      info << "unknown"; break;
        }
        info << "\",\n\t\"PCTmax\": \"" << sl->getDBStorageLayout()->getPCTMax();
        info << "\",\n\t\"PCTmin\": \"" << sl->getDBStorageLayout()->getPCTMin();
        info << "\"\n }";
    }
    info << "\n}";
    return info.str();
}

std::string MDDObj::getJsonArrayInfo() const
{
    std::ostringstream info;
    std::unique_ptr<std::vector<std::shared_ptr<Tile>>> tiles{};
    tiles.reset(this->getTiles());
    printArrayTiles(tiles, info);
    return info.str();
}

void MDDObj::printArrayTiles(const std::unique_ptr<std::vector<std::shared_ptr<Tile>>> &tiles,
                             std::ostream &info) const
{
    info << "[";
    bool first = true;
    for (const auto &tile : *tiles)
    {
        info << "\n\t\t\"" << tile->getDomain() << "\"";
        if (!first)
            info << ",";
        else
            first = false;
    }
    info << "\n\t]";
}

std::string MDDObj::getSvgArrayInfo() const
{
    std::ostringstream info;
    auto domain = this->getCurrentDomain();
    if (domain.dimension() != 2)
    {
        LERROR << "Cannot export tile info to svg format for non-2D collection.";
        throw r_Error(449);
    }
    std::string colors[COLOR_PALET_SIZE] = {"blue", "red", "brown", "grey", "black", "yellow", "purple", "pink", "olive", "gold", "aqua",
                                            "darkorange", "indigo", "khaki", "seashell"};
    r_Point origin = domain.get_origin();
    r_Point high = domain.get_high();
    info << "<svg width=\"" << high[0] - origin[0] << "\" height=\"" << high[1] - origin[1] << "\">";
    
    std::unique_ptr<std::vector<std::shared_ptr<Tile>>> tiles{};
    tiles.reset(this->getTiles());
    for (unsigned int i = 0; i < tiles->size(); i++)
    {
        auto tileDomain = tiles->at(i)->getDomain();
        r_Point originTile = tileDomain.get_origin();
        r_Point highTile = tileDomain.get_high();
        int x = originTile[0];
        int y = originTile[1];
        int width = highTile[0] - x;
        int height = highTile[1] - y;
        int id = (tiles->at(i).get())->getDBTile()->getOId().getCounter();
        info << "\n\t <rect x=\"" << x << "\" y=\"" << y << "\" width=\"" << width
             << "\" height=\"" << height << "\" id=\"" << id 
             << "\" style=\"fill:" << colors[i % COLOR_PALET_SIZE] << ";\" stroke=\"black\"></rect>";
    }
    info << "\n</svg>\n";
    return info.str();
}

template <class T>
void fillTile(T fillValArg, size_t cellCount, char *startPointArg)
{
    T *startPoint = reinterpret_cast<T *>(startPointArg);
    std::fill(startPoint, startPoint + cellCount, fillValArg);
}

void
MDDObj::fillTileWithNullvalues(char *resDataPtr, size_t cellCount) const
{
    if (this->getNullValues())
    {
        NullValuesHandler::fillTileWithNullvalues(resDataPtr, cellCount, getCellType());
    }
    else
    {
        fillTile<r_Char>(0, cellCount * getCellType()->getSize(), resDataPtr);
    }
}

void MDDObj::setCollType(const CollectionType *newCollType)
{
    collType = newCollType;
}
const CollectionType *MDDObj::getCollType() const
{
    return collType;
}

void MDDObj::setDbDomain(const r_Minterval &domain) {
  myDBMDDObj->setDbDomain(domain);
}
