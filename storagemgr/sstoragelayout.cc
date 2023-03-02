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
 * SOURCE: sstoragelayout.cc
 *
 * MODULE: indexmgr
 * CLASS:   StorageLayout
 *
 * CHANGE HISTORY (append further entries):
 * when              who                what
 * -----------------------------------------------------------------------
 * 10-Sep-97       furtado        creation of preliminary version.
 * 09-Oct-97       sivan          set, get functions
 * 13-Oct-97       furtado        extended functionality, class hierarchy
 * 4-Nov-98        furtado        added RegDirIx< >.
 * 13-Nov-00       hoefner        startet to do something with this class
 * 07-Jan-09       Shams          add tiling set to the getLayout method
 * 07-Jan-09       Shams          add some methods for supporting tiling
 * 09-April-14     uadhikari      bug fix for 'area of interest' tiling
 * COMMENTS:
 *   none
 *
 ****************************************************************************/

#include "sstoragelayout.hh"                // for StorageLayout
#include "relstorageif/dbstoragelayout.hh"  // for DBStorageLayout
#include "stgmddconfig.hh"                  // for StgMddConfig
#include "relstorageif/storageid.hh"        // for DBStorageLayoutId
#include "reladminif/oidif.hh"              // for operator<<, OId
#include "mddmgr/mddobj.hh"
#include "rasodmg/alignedtiling.hh"
#include "rasodmg/dirtiling.hh"
#include "rasodmg/stattiling.hh"
#include "raslib/rmdebug.hh"
#include "raslib/mddtypes.hh"   // for r_Dimension, r_Bytes
#include "raslib/minterval.hh"  // for r_Minterval, operator<<
#include "raslib/point.hh"      // for r_Point, operator<<
#include <logging.hh>           // for Writer, CTRACE

#include <string>  // for string
#include <vector>  // for vector

using namespace std;

// this number should be DBMS dependent.. default in postgres seems to be 8kB, in MySQL 16kB, etc.
const r_Bytes StorageLayout::DBSPageSize = 4096;

r_Bytes StorageLayout::DefaultMinimalTileSize = DBSPageSize;

r_Bytes StorageLayout::DefaultPCTMax = 2 * DBSPageSize;

// 4MB
r_Bytes StorageLayout::DefaultTileSize = 4194304;

unsigned int StorageLayout::DefaultIndexSize = 0;

r_Index_Type StorageLayout::DefaultIndexType = r_RPlus_Tree_Index;  // DirTilesIx; // AutoIx;

r_Tiling_Scheme StorageLayout::DefaultTilingScheme = r_AlignedTiling;

r_Minterval StorageLayout::DefaultTileConfiguration("[0:1023,0:1023]");

r_Data_Format StorageLayout::DefaultDataFormat = r_Array;

StorageLayout::StorageLayout(r_Index_Type ixType)
    : myLayout(new DBStorageLayout())
{
    setIndexType(ixType);
    extraFeatures = new StgMddConfig();
    LTRACE << "StorageLayout(" << ixType << ")";
}

StorageLayout::StorageLayout()
    : myLayout(new DBStorageLayout())
{
    extraFeatures = new StgMddConfig();
    LTRACE << "StorageLayout()";
}

StorageLayout::StorageLayout(const DBStorageLayoutId &id)
    : myLayout(id)
{
    extraFeatures = new StgMddConfig();
    LTRACE << "StorageLayout(" << id.getOId() << ")";
}

StorageLayout::StorageLayout(const StorageLayout &other)
    : extraFeatures(nullptr), myLayout(other.myLayout)
{
    StgMddConfig *o = other.extraFeatures;
    if (o)
    {
        if (extraFeatures == nullptr)
        {
            extraFeatures = new StgMddConfig();
        }
        extraFeatures->setBBoxes(o->getBBoxes());
        extraFeatures->setBorderThreshold(o->getBorderThreshold());
        extraFeatures->setCellSize(o->getCellSize());
        extraFeatures->setDirDecompose(o->getDirDecompose());
        extraFeatures->setInterestThreshold(o->getInterestThreshold());
        extraFeatures->setTilingSizeStrategy_AOI(o->getTilingSizeStrategy_AOI());
    }
}

DBStorageLayoutId StorageLayout::getDBStorageLayout() const
{
    return myLayout;
}

r_Index_Type StorageLayout::getIndexType() const
{
    return myLayout->getIndexType();
}

r_Tiling_Scheme StorageLayout::getTilingScheme() const
{
    return myLayout->getTilingScheme();
}

r_Bytes StorageLayout::getTileSize() const
{
    return myLayout->getTileSize();
}

r_Bytes StorageLayout::getMinimalTileSize() const
{
    return StorageLayout::DBSPageSize;
}

r_Minterval StorageLayout::getTileConfiguration() const
{
    return myLayout->getTileConfiguration();
}

void StorageLayout::setIndexType(r_Index_Type it)
{
    myLayout->setIndexType(it);
}

void StorageLayout::setDataFormat(r_Data_Format cs)
{
    myLayout->setDataFormat(cs);
}

void StorageLayout::setTilingScheme(r_Tiling_Scheme ts)
{
    myLayout->setTilingScheme(ts);
}

void StorageLayout::setTileSize(r_Bytes newSize)
{
    myLayout->setTileSize(newSize);
}

void StorageLayout::setTileConfiguration(const r_Minterval &tc)
{
    myLayout->setTileConfiguration(tc);
}

r_Data_Format StorageLayout::getDataFormat(
    __attribute__((unused)) const r_Point &where) const
{
    return myLayout->getDataFormat();
}

void StorageLayout::setBBoxes(const vector<r_Minterval> &input)
{
    extraFeatures->setBBoxes(input);
}

void StorageLayout::setSubTiling()
{
    extraFeatures->setSubTiling();
}

void StorageLayout::resetSubTiling()
{
    extraFeatures->resetSubTiling();
}

void StorageLayout::setInterestThreshold(double i)
{
    extraFeatures->setInterestThreshold(i);
}

void StorageLayout::setBorderThreshold(unsigned int b)
{
    extraFeatures->setBorderThreshold(b);
}

void StorageLayout::setCellSize(int i)
{
    extraFeatures->setCellSize(i);
}

void StorageLayout::setDirDecomp(vector<r_Dir_Decompose> *dir)
{
    vector<r_Dir_Decompose> dec;
    for (unsigned int i = 0; i < dir->size(); ++i)
    {
        dec.push_back(dir->at(i));
    }
    extraFeatures->setDirDecompose(dec);
}

void StorageLayout::setExtraFeatures(StgMddConfig *newExtraFeatures)
{
    extraFeatures = newExtraFeatures;
}

void StorageLayout::setTilingSizeStrategy_AOI(r_Interest_Tiling::Tilesize_Limit input)
{
    extraFeatures->setTilingSizeStrategy_AOI(input);
}

std::vector<r_Minterval>
StorageLayout::getLayout(const r_Minterval &tileDomain)
{
    std::vector<r_Minterval> retval;
    if (myLayout->supportsTilingScheme())
    {
        switch (myLayout->getTilingScheme())
        {
        case r_RegularTiling:
            if (myLayout->getTileConfiguration().dimension() == tileDomain.dimension())
                retval = calcRegLayout(tileDomain);
            else
                retval.push_back(tileDomain);
            break;
        case r_InterestTiling:
            retval = calcInterestLayout(tileDomain);
            break;
        case r_StatisticalTiling:
            retval = calcStatisticLayout(tileDomain);
            break;
        case r_AlignedTiling:
            if (myLayout->getTileConfiguration().dimension() == tileDomain.dimension())
                retval = calcAlignedLayout(tileDomain);
            else
                retval.push_back(tileDomain);
            break;
        case r_DirectionalTiling:
            retval = calcDirectionalLayout(tileDomain);
            break;
        case r_SizeTiling:
            LWARNING << "Size tiling scheme not supported.";
            retval.push_back(tileDomain);
            break;
        default:
            LWARNING << "Unknown tiling scheme.";
        case r_NoTiling:
            retval.push_back(tileDomain);
            break;
        }
    }
    else
    {
        retval.push_back(tileDomain);
    }
    return retval;
}

StorageLayout::~StorageLayout()
{
    if (extraFeatures)
    {
        delete extraFeatures;
        extraFeatures = nullptr;
    }
}

std::vector<r_Minterval>
StorageLayout::calcRegLayout(const r_Minterval &tileDomain) const
{
    std::vector<r_Minterval> retval;
    r_Minterval base = myLayout->getTileConfiguration();
    LTRACE << "tiling configuration: " << base;
    r_Point borigin = base.get_origin();
    r_Point bhigh = base.get_high();
    r_Point bextent = base.get_extent();
    r_Point torigin = tileDomain.get_origin();
    r_Point thigh = tileDomain.get_high();
    r_Point textent = tileDomain.get_extent();
    r_Dimension bdim = base.dimension();
    r_Point transex(bdim);
    r_Point transexmax(bdim);
    r_Point transco(bdim);
    r_Point transcotemp(bdim);
    r_Point trans(bdim);

    LTRACE << "base       : origin " << borigin << ", high " << bhigh << ", extent " << bextent << ", dimension " << bdim;
    LTRACE << "tile domain: origin " << torigin << ", high " << thigh << ", extent " << textent;

    // go through all dimensions of the base tile configuration
    for (r_Dimension i = 0; i < bdim; i++)
    {
        const auto origindiff = torigin[i] - borigin[i];
        const auto highdiff = thigh[i] - bhigh[i];

        if (highdiff % bextent[i] > 0)
            transexmax[i] = highdiff / bextent[i] + 1;
        else
            transexmax[i] = highdiff / bextent[i];
        if (origindiff % bextent[i] < 0)
            transex[i] = origindiff / bextent[i] - 1;
        else
            transex[i] = origindiff / bextent[i];

        trans[i] = transex[i];
        transco[i] = transex[i] * bextent[i];
    }

    // generate domains according to tiling layout
    while (true)
    {
        // current dimension, start from the last one
        auto currdim = bdim - 1;

        // setup translation vector
        transcotemp = bextent * trans;
        // advance current dimension
        for (r_Dimension j = trans[currdim]; j <= transexmax[currdim]; j++)
        {
            transcotemp[currdim] = bextent[currdim] * j;
            retval.push_back(base.create_translation(transcotemp));
        }
        if (currdim == 0)
            break;
        --currdim;

        //
        // advance the next available dimension
        //
        // 1. find the next available dimension
        while (currdim > 0 && trans[currdim] == transexmax[currdim])
            --currdim;
        // if none found we're done
        if (currdim == 0 && trans[currdim] == transexmax[currdim])
            break;

        // 2. advance dimension
        ++trans[currdim];
        // 3. reset later dimensions
        ++currdim;
        while (currdim < bdim)
        {
            trans[currdim] = transex[currdim];
            ++currdim;
        }
    }

#ifdef DEBUG
    for (auto i = retval.begin(); i != retval.end(); i++)
        LTRACE << *i;
#endif
    return retval;
}

std::vector<r_Minterval>
StorageLayout::calcInterestLayout(const r_Minterval &tileDomain)
{
    r_Interest_Tiling tiling(tileDomain.dimension(), extraFeatures->getBBoxes(),
                             myLayout->getTileSize(), extraFeatures->getTilingSizeStrategy_AOI());
    return tiling.compute_tiles(tileDomain, static_cast<r_Bytes>(extraFeatures->getCellSize()));
}

std::vector<r_Minterval>
StorageLayout::calcAlignedLayout(const r_Minterval &tileDomain)
{
    r_Aligned_Tiling tiling(myLayout->getTileConfiguration(), myLayout->getTileSize());
    return tiling.compute_tiles(tileDomain, static_cast<r_Bytes>(extraFeatures->getCellSize()));
}

std::vector<r_Minterval>
StorageLayout::calcDirectionalLayout(const r_Minterval &tileDomain)
{
    r_Dir_Tiling tiling(tileDomain.dimension(),
                        extraFeatures->getDirDecompose(), myLayout->getTileSize(),
                        extraFeatures->getSubTiling() ? r_Dir_Tiling::WITH_SUBTILING : r_Dir_Tiling::WITHOUT_SUBTILING);
    return tiling.compute_tiles(tileDomain, static_cast<r_Bytes>(extraFeatures->getCellSize()));
}

std::vector<r_Minterval>
StorageLayout::calcStatisticLayout(const r_Minterval &tileDomain)
{
    std::vector<r_Access> accesses;
    for (const auto &bbox: extraFeatures->getBBoxes())
        accesses.emplace_back(bbox);
    auto borderT = extraFeatures->getBorderThreshold();
    auto interestT = extraFeatures->getInterestThreshold() < 0
                         ? r_Stat_Tiling::DEF_INTERESTING_THR
                         : extraFeatures->getInterestThreshold();
    r_Stat_Tiling tiling(tileDomain.dimension(), accesses, myLayout->getTileSize(), borderT, interestT);
    return tiling.compute_tiles(tileDomain, static_cast<r_Bytes>(extraFeatures->getCellSize()));
}

r_Minterval
StorageLayout::getDefaultTileCfg(__attribute__((unused)) int baseTypeSize, r_Dimension sourceDimension)
{
    std::string newDomain = "[";
    for (unsigned int i = 0; i < sourceDimension; i++)
    {
        if (i > 0)
            newDomain += ", ";
        newDomain += "0:*";
    }
    newDomain += "]";
    return r_Minterval(newDomain.c_str());
}
